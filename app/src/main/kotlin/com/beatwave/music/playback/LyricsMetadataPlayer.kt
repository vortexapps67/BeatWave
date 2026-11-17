/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.playback

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Wraps the player handed to the media session so the now playing metadata can
 * carry the current lyric line, which is what Android Auto shows.
 * Only the session sees this; the player itself and the in-app UI keep the real
 * metadata.
 *
 * A short line takes the title row, with the song below it. A long one would get cut off, so it is
 * split across the title and artist rows instead.
 */
@UnstableApi
class LyricsMetadataPlayer(
    player: Player,
    private val lyricLine: (mediaId: String?) -> String?,
) : ForwardingPlayer(player) {
    private val listeners = CopyOnWriteArraySet<Player.Listener>()
    private var publishedLine: String? = null

    override fun addListener(listener: Player.Listener) {
        super.addListener(listener)
        listeners += listener
    }

    override fun removeListener(listener: Player.Listener) {
        super.removeListener(listener)
        listeners -= listener
    }

    override fun getMediaMetadata(): MediaMetadata {
        val metadata = super.getMediaMetadata()
        val line = lyricLine(currentMediaItem?.mediaId) ?: return metadata
        return metadata.withLyricLine(line)
    }

    /** Pushes the metadata to the session again if the line changed since the last push. */
    fun invalidateLyricLine() {
        val line = lyricLine(currentMediaItem?.mediaId)
        if (line == publishedLine) return
        publishedLine = line
        val metadata = mediaMetadata
        listeners.forEach { it.onMediaMetadataChanged(metadata) }
    }

    private fun MediaMetadata.withLyricLine(line: String): MediaMetadata {
        val builder = buildUpon()
        if (line.length <= SINGLE_ROW_MAX_CHARS) {
            // The title row is the big, bright one in the car, so the line goes
            // there and the song moves down to the smaller row.
            val song = listOfNotNull(displayTitle ?: title, artist)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
            builder
                .setTitle(line)
                .setDisplayTitle(line)
                .setArtist(song)
                .setSubtitle(song)
        } else {
            val split = splitPoint(line)
            val top = line.substring(0, split).trim()
            val bottom = line.substring(split).trim()
            builder
                .setTitle(top)
                .setDisplayTitle(top)
                .setArtist(bottom)
                .setSubtitle(bottom)
        }
        return builder.build()
    }

    /** The space closest to the middle, so both rows come out about the same length. */
    private fun splitPoint(line: String): Int {
        val middle = line.length / 2
        val before = line.lastIndexOf(' ', middle)
        val after = line.indexOf(' ', middle)
        return when {
            before < 0 && after < 0 -> middle
            before < 0 -> after
            after < 0 -> before
            middle - before <= after - middle -> before
            else -> after
        }
    }

    companion object {
        /** About what fits on the artist row of a typical Android Auto screen. */
        private const val SINGLE_ROW_MAX_CHARS = 32
    }
}
