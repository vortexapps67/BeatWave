/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.lyrics

import android.content.Context
import android.util.LruCache
import com.beatwave.music.constants.LyricsProviderOrderKey
import com.beatwave.music.constants.PreferredLyricsProvider
import com.beatwave.music.constants.PreferredLyricsProviderKey
import com.beatwave.music.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.beatwave.music.extensions.toEnum
import com.beatwave.music.models.MediaMetadata
import com.beatwave.music.utils.NetworkConnectivityObserver
import com.beatwave.music.utils.dataStore
import com.beatwave.music.utils.reportException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class LyricsHelper
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val networkConnectivity: NetworkConnectivityObserver,
) {
    /**
     * Resolves the ordered list of lyrics providers from the user's saved priority order.
     * Falls back to migrating the legacy [PreferredLyricsProvider] enum if the new order
     * preference has not been written yet, ensuring a smooth upgrade for existing users.
     */
    private suspend fun resolveLyricsProviders(): List<LyricsProvider> {
        val preferences = context.dataStore.data.first()
        val orderString = preferences[LyricsProviderOrderKey].orEmpty()

        if (orderString.isNotBlank()) {
            return LyricsProviderRegistry.getOrderedProviders(orderString)
        }

        // Migration path: place the old preferred provider first in the default order
        val preferredEnum = preferences[PreferredLyricsProviderKey]
            .toEnum(PreferredLyricsProvider.YOULYPLUS)
        val preferredName = LyricsProviderRegistry.getProviderNameForEnum(preferredEnum)
        val defaultOrder = LyricsProviderRegistry.getDefaultProviderOrder()
        val migratedOrder = listOf(preferredName) + defaultOrder.filter { it != preferredName }
        return migratedOrder.mapNotNull { LyricsProviderRegistry.getProviderByName(it) }
    }



    private val cache = LruCache<String, List<LyricsResult>>(MAX_CACHE_SIZE)
    private var currentLyricsJob: Job? = null

    suspend fun getLyrics(mediaMetadata: MediaMetadata): LyricsWithProvider {
        currentLyricsJob?.cancel()

        val cached = cache.get(mediaMetadata.id)?.firstOrNull()
        if (cached != null && cached.lyrics != LYRICS_NOT_FOUND) {
            return LyricsWithProvider(cached.lyrics, cached.providerName)
        }

        // Check network connectivity before making network requests
        val isNetworkAvailable = try {
            networkConnectivity.isCurrentlyConnected()
        } catch (e: Exception) {
            true
        }

        if (!isNetworkAvailable) {
            return LyricsWithProvider(LYRICS_NOT_FOUND, "Unknown")
        }

        val providers = resolveLyricsProviders().filter { it.isEnabled(context) }
        if (providers.isEmpty()) {
            return LyricsWithProvider(LYRICS_NOT_FOUND, "Unknown")
        }

        val artists = mediaMetadata.artists.joinToString { it.name }
        val title = mediaMetadata.title
        val duration = mediaMetadata.duration
        val album = mediaMetadata.album?.title
        val id = mediaMetadata.id

        // Fast parallel prioritized fetch with structured channel
        val channel = kotlinx.coroutines.channels.Channel<LyricsWithProvider>(kotlinx.coroutines.channels.Channel.BUFFERED)
        val scope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)
        
        val jobs = providers.mapIndexed { index, provider ->
            scope.launch {
                // Slight priority staggering (0ms for top 3, 400ms for subsequent) so top providers take precedence
                if (index >= 3) {
                    kotlinx.coroutines.delay(400L)
                }
                try {
                    val result = kotlinx.coroutines.withTimeoutOrNull(4500L) {
                        provider.getLyrics(id, title, artists, duration, album)
                    }
                    val lyrics = result?.getOrNull()
                    if (!lyrics.isNullOrBlank() && lyrics != LYRICS_NOT_FOUND) {
                        channel.trySend(LyricsWithProvider(lyrics, provider.name))
                    }
                } catch (e: Exception) {
                    reportException(e)
                }
            }
        }

        // Wait for first valid lyrics or completion of all providers
        var finalResult = LyricsWithProvider(LYRICS_NOT_FOUND, "Unknown")
        try {
            val completedProviders = kotlinx.coroutines.withTimeoutOrNull(6000L) {
                // Read first successful from channel
                val firstSuccess = channel.receiveCatching().getOrNull()
                if (firstSuccess != null) {
                    finalResult = firstSuccess
                }
            }
        } catch (e: Exception) {
            // Ignored
        } finally {
            channel.close()
            jobs.forEach { it.cancel() }
            scope.cancel()
        }

        if (finalResult.lyrics != LYRICS_NOT_FOUND) {
            cache.put(id, listOf(LyricsResult(finalResult.provider, finalResult.lyrics)))
        }

        return finalResult
    }

    suspend fun getAllLyrics(
        mediaId: String,
        songTitle: String,
        songArtists: String,
        duration: Int,
        album: String? = null,
        callback: (LyricsResult) -> Unit,
    ) {
        currentLyricsJob?.cancel()

        val cacheKey = "$songArtists-$songTitle".replace(" ", "")
        cache.get(cacheKey)?.let { results ->
            results.forEach {
                callback(it)
            }
            return
        }

        val isNetworkAvailable = try {
            networkConnectivity.isCurrentlyConnected()
        } catch (e: Exception) {
            true
        }

        if (!isNetworkAvailable) {
            return
        }

        val allResult = java.util.Collections.synchronizedList(mutableListOf<LyricsResult>())
        val providers = resolveLyricsProviders().filter { it.isEnabled(context) }
        
        currentLyricsJob = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.IO).launch {
            val providerJobs = providers.map { provider ->
                launch {
                    try {
                        kotlinx.coroutines.withTimeoutOrNull(6000L) {
                            provider.getAllLyrics(mediaId, songTitle, songArtists, duration, album) { lyrics ->
                                if (lyrics.isNotBlank() && lyrics != LYRICS_NOT_FOUND) {
                                    val result = LyricsResult(provider.name, lyrics)
                                    allResult += result
                                    callback(result)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        reportException(e)
                    }
                }
            }
            providerJobs.forEach { it.join() }
            if (allResult.isNotEmpty()) {
                cache.put(cacheKey, allResult)
            }
        }

        currentLyricsJob?.join()
    }

    fun cancelCurrentLyricsJob() {
        currentLyricsJob?.cancel()
        currentLyricsJob = null
    }

    companion object {
        private const val MAX_CACHE_SIZE = 100
    }
}

data class LyricsResult(
    val providerName: String,
    val lyrics: String,
)

data class LyricsWithProvider(
    val lyrics: String,
    val provider: String,
)