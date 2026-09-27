/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.utils

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.ConnectivityManager
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.getSystemService
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.ContentMetadata
import com.beatwave.music.constants.AudioQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer

/**
 * Exports a song to a user-chosen file.
 *
 * Android ships no MP3 *encoder* (MediaCodec decodes MP3 but cannot produce it),
 * so the output container is M4A/AAC — playable essentially everywhere and, when
 * the source is already AAC, produced by a lossless remux rather than a re-encode.
 *
 * Bytes come from the offline caches first, so a downloaded or already-streamed
 * track exports instantly and without network.
 */
@UnstableApi
object SongExporter {

    /** Container/extension the export always produces. */
    const val EXTENSION = "m4a"
    const val MIME_TYPE = "audio/mp4a-latm"

    private const val TIMEOUT_US = 10_000L
    private const val TARGET_BITRATE = 192_000

    sealed interface Result {
        data object Success : Result
        data class Failure(val message: String) : Result
    }

    /**
     * @param songId the media id, which is also the cache key.
     * @param destination a SAF uri the caller obtained from the document picker.
     */
    suspend fun export(
        context: Context,
        songId: String,
        destination: Uri,
        playerCache: Cache,
        downloadCache: Cache,
        onProgress: (Float, String) -> Unit = { _, _ -> },
    ): Result = withContext(Dispatchers.IO) {
        val workDir = File(context.cacheDir, "export").apply { mkdirs() }
        val sourceFile = File(workDir, "src_$songId")
        val muxedFile = File(workDir, "out_$songId.$EXTENSION")

        try {
            onProgress(0.05f, "Locating audio…")

            val gotBytes = readFromCache(songId, playerCache, downloadCache, sourceFile) ||
                downloadSource(context, songId, sourceFile) { fraction ->
                    onProgress(0.05f + fraction * 0.55f, "Downloading…")
                }

            if (!gotBytes || sourceFile.length() == 0L) {
                return@withContext Result.Failure("Could not read the audio for this song")
            }

            onProgress(0.65f, "Preparing file…")
            val converted = writeM4a(sourceFile, muxedFile)
            if (!converted || muxedFile.length() == 0L) {
                return@withContext Result.Failure("Could not convert this track to $EXTENSION")
            }

            onProgress(0.9f, "Saving…")
            context.contentResolver.openFileDescriptor(destination, "w")?.use { pfd ->
                writeTruncated(pfd, muxedFile)
            } ?: return@withContext Result.Failure("Could not open the destination file")

            onProgress(1f, "Done")
            Result.Success
        } catch (e: Exception) {
            Timber.e(e, "Export failed for %s", songId)
            Result.Failure(e.message ?: "Export failed")
        } finally {
            sourceFile.delete()
            muxedFile.delete()
        }
    }

    /** Opening with "w" alone leaves trailing bytes when replacing a longer file. */
    private fun writeTruncated(pfd: ParcelFileDescriptor, source: File) {
        java.io.FileOutputStream(pfd.fileDescriptor).use { out ->
            out.channel.truncate(0)
            source.inputStream().use { it.copyTo(out) }
        }
    }

    /**
     * Copies the track out of the offline caches. Returns false when neither
     * cache holds the whole track, leaving the caller to fetch it.
     */
    private fun readFromCache(
        songId: String,
        playerCache: Cache,
        downloadCache: Cache,
        destination: File,
    ): Boolean {
        // Downloads are always keyed by the bare media id. The player cache may
        // additionally hold a lossless variant under "<id>#flac", which is FLAC
        // in a container this exporter does not remux, so it is not a candidate.
        for (cache in listOf(downloadCache, playerCache)) {
            val length = ContentMetadata.getContentLength(cache.getContentMetadata(songId))
            if (length <= 0L || !cache.isCached(songId, 0L, length)) continue

            // No upstream factory, so this source is cache-only and can never
            // reach the network. The uri is a placeholder: CacheDataSource keys
            // its lookup off setKey, and the span is known to be fully present.
            val source: DataSource = CacheDataSource.Factory()
                .setCache(cache)
                .setCacheWriteDataSinkFactory(null)
                .createDataSource()

            val copied = runCatching {
                source.open(
                    DataSpec.Builder()
                        .setUri(Uri.parse("beatwave://cache/$songId"))
                        .setKey(songId)
                        .setPosition(0)
                        .setLength(length)
                        .build()
                )
                destination.outputStream().use { out ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val read = source.read(buffer, 0, buffer.size)
                        if (read == androidx.media3.common.C.RESULT_END_OF_INPUT) break
                        out.write(buffer, 0, read)
                    }
                }
                true
            }.onFailure {
                Timber.w(it, "Cache read failed for %s", songId)
                destination.delete()
            }.getOrDefault(false)

            runCatching { source.close() }
            if (copied) return true
        }
        return false
    }

    private suspend fun downloadSource(
        context: Context,
        songId: String,
        destination: File,
        onProgress: (Float) -> Unit,
    ): Boolean {
        val connectivityManager = context.getSystemService<ConnectivityManager>() ?: return false

        val streamUrl = runCatching {
            YTPlayerUtils.playerResponseForPlayback(
                videoId = songId,
                audioQuality = AudioQuality.HIGH,
                connectivityManager = connectivityManager,
                context = context,
                // FLAC cannot ride in the MP4 container this exporter writes.
                allowLossless = false,
            ).getOrNull()?.streamUrl
        }.getOrNull() ?: return false

        return runCatching {
            val connection = java.net.URL(streamUrl).openConnection()
            connection.connect()
            val total = connection.contentLengthLong
            connection.getInputStream().use { input ->
                destination.outputStream().use { out ->
                    val buffer = ByteArray(64 * 1024)
                    var written = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        out.write(buffer, 0, read)
                        written += read
                        if (total > 0) onProgress((written.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
            true
        }.onFailure {
            Timber.w(it, "Stream download failed for %s", songId)
            destination.delete()
        }.getOrDefault(false)
    }

    /**
     * Writes [source] into an M4A at [destination]: a straight stream-copy when
     * the source is already AAC, and a decode/re-encode otherwise (YouTube's
     * Opus streams, which MP4 cannot carry).
     */
    private fun writeM4a(source: File, destination: File): Boolean {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(source.absolutePath)
            val trackIndex = (0 until extractor.trackCount).firstOrNull { i ->
                extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: return false

            extractor.selectTrack(trackIndex)
            val inputFormat = extractor.getTrackFormat(trackIndex)
            val mime = inputFormat.getString(MediaFormat.KEY_MIME).orEmpty()

            if (mime == MIME_TYPE) {
                remux(extractor, inputFormat, destination)
            } else {
                transcodeToAac(extractor, inputFormat, destination)
            }
        } catch (e: Exception) {
            Timber.e(e, "Conversion failed")
            false
        } finally {
            runCatching { extractor.release() }
        }
    }

    private fun remux(extractor: MediaExtractor, format: MediaFormat, destination: File): Boolean {
        val muxer = MediaMuxer(destination.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        return try {
            val outTrack = muxer.addTrack(format)
            muxer.start()

            val maxSize = format.takeIf { it.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE) }
                ?.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) ?: (256 * 1024)
            val buffer = ByteBuffer.allocate(maxSize)
            val info = MediaCodec.BufferInfo()

            while (true) {
                val size = extractor.readSampleData(buffer, 0)
                if (size < 0) break
                info.offset = 0
                info.size = size
                info.presentationTimeUs = extractor.sampleTime
                info.flags = extractor.sampleFlags
                muxer.writeSampleData(outTrack, buffer, info)
                extractor.advance()
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Remux failed")
            false
        } finally {
            runCatching { muxer.stop() }
            runCatching { muxer.release() }
        }
    }

    private fun transcodeToAac(
        extractor: MediaExtractor,
        inputFormat: MediaFormat,
        destination: File,
    ): Boolean {
        val sampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channels = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

        val decoder = MediaCodec.createDecoderByType(
            inputFormat.getString(MediaFormat.KEY_MIME).orEmpty()
        )
        val encoder = MediaCodec.createEncoderByType(MIME_TYPE)
        val muxer = MediaMuxer(destination.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        var muxerStarted = false
        var outTrack = -1

        return try {
            decoder.configure(inputFormat, null, null, 0)
            decoder.start()

            encoder.configure(
                MediaFormat.createAudioFormat(MIME_TYPE, sampleRate, channels).apply {
                    setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                    setInteger(MediaFormat.KEY_BIT_RATE, TARGET_BITRATE)
                    setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 64 * 1024)
                },
                null, null, MediaCodec.CONFIGURE_FLAG_ENCODE,
            )
            encoder.start()

            val decodeInfo = MediaCodec.BufferInfo()
            val encodeInfo = MediaCodec.BufferInfo()
            var extractorDone = false
            var decoderDone = false
            var encoderDone = false
            // The encoder's timestamps are derived from how many frames it has
            // actually been fed, not from the decoder's, so a gap in the source
            // never desynchronises the written track.
            var encodedFrames = 0L

            while (!encoderDone) {
                if (!extractorDone) {
                    val inIndex = decoder.dequeueInputBuffer(TIMEOUT_US)
                    if (inIndex >= 0) {
                        val buffer = decoder.getInputBuffer(inIndex)!!
                        val size = extractor.readSampleData(buffer, 0)
                        if (size < 0) {
                            decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            extractorDone = true
                        } else {
                            decoder.queueInputBuffer(inIndex, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                if (!decoderDone) {
                    when (val outIndex = decoder.dequeueOutputBuffer(decodeInfo, TIMEOUT_US)) {
                        MediaCodec.INFO_TRY_AGAIN_LATER, MediaCodec.INFO_OUTPUT_FORMAT_CHANGED,
                        MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> Unit

                        else -> if (outIndex >= 0) {
                            val pcm = decoder.getOutputBuffer(outIndex)!!
                            val endOfStream = decodeInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0

                            if (decodeInfo.size > 0) {
                                val encIndex = encoder.dequeueInputBuffer(TIMEOUT_US)
                                if (encIndex >= 0) {
                                    val encBuffer = encoder.getInputBuffer(encIndex)!!
                                    encBuffer.clear()
                                    pcm.position(decodeInfo.offset)
                                    pcm.limit(decodeInfo.offset + decodeInfo.size)
                                    val copy = minOf(encBuffer.remaining(), pcm.remaining())
                                    val slice = pcm.slice().apply { limit(copy) }
                                    encBuffer.put(slice)

                                    val ptsUs = encodedFrames * 1_000_000L / sampleRate
                                    encoder.queueInputBuffer(encIndex, 0, copy, ptsUs, 0)
                                    encodedFrames += copy / (2L * channels)
                                }
                            }

                            decoder.releaseOutputBuffer(outIndex, false)

                            if (endOfStream) {
                                decoderDone = true
                                val encIndex = encoder.dequeueInputBuffer(TIMEOUT_US)
                                if (encIndex >= 0) {
                                    encoder.queueInputBuffer(
                                        encIndex, 0, 0,
                                        encodedFrames * 1_000_000L / sampleRate,
                                        MediaCodec.BUFFER_FLAG_END_OF_STREAM,
                                    )
                                }
                            }
                        }
                    }
                }

                when (val encIndex = encoder.dequeueOutputBuffer(encodeInfo, TIMEOUT_US)) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        if (!muxerStarted) {
                            outTrack = muxer.addTrack(encoder.outputFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                    }

                    MediaCodec.INFO_TRY_AGAIN_LATER, MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> Unit

                    else -> if (encIndex >= 0) {
                        val encoded = encoder.getOutputBuffer(encIndex)!!
                        val isConfig = encodeInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0
                        if (encodeInfo.size > 0 && muxerStarted && !isConfig) {
                            encoded.position(encodeInfo.offset)
                            encoded.limit(encodeInfo.offset + encodeInfo.size)
                            muxer.writeSampleData(outTrack, encoded, encodeInfo)
                        }
                        encoder.releaseOutputBuffer(encIndex, false)
                        if (encodeInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) encoderDone = true
                    }
                }
            }
            muxerStarted
        } catch (e: Exception) {
            Timber.e(e, "Transcode failed")
            false
        } finally {
            runCatching { decoder.stop() }
            runCatching { decoder.release() }
            runCatching { encoder.stop() }
            runCatching { encoder.release() }
            if (muxerStarted) runCatching { muxer.stop() }
            runCatching { muxer.release() }
        }
    }

    /** Filesystem-safe "Artist - Title.m4a". */
    fun suggestedFileName(title: String, artist: String?): String {
        val base = listOfNotNull(artist?.takeIf { it.isNotBlank() }, title.takeIf { it.isNotBlank() })
            .joinToString(" - ")
            .ifBlank { "audio" }
        return base.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(120) + ".$EXTENSION"
    }
}
