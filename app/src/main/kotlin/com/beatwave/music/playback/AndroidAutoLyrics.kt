/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.playback

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import com.beatwave.music.constants.AndroidAutoLyricsKey
import com.beatwave.music.db.MusicDatabase
import com.beatwave.music.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.beatwave.music.lyrics.LyricsEntry
import com.beatwave.music.lyrics.LyricsUtils
import com.beatwave.music.utils.dataStore
import com.beatwave.music.utils.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Synced lyrics for Android Auto.
 *
 * A media app can't draw its own screen in the car, Android Auto only renders
 * what the media session publishes. So lyrics reach the car two ways:
 * the current line is swapped into the now playing metadata (see
 * [LyricsMetadataPlayer]), and the "Lyrics" browse tab shows it with the next
 * line below, where tapping it seeks to the start of the line.
 */
class AndroidAutoLyrics(
    private val context: Context,
    private val scope: CoroutineScope,
    private val database: MusicDatabase,
    private val positionMs: () -> Long,
    private val isPlaying: () -> Boolean,
) {
    /** Fired on the main thread when the current line, the song or the setting changes. */
    var onChanged: () -> Unit = {}

    @Volatile
    var enabled = false
        private set
    @Volatile
    var songId: String? = null
        private set
    @Volatile
    var lines: List<LyricsEntry> = emptyList()
        private set
    @Volatile
    var currentIndex = -1
        private set

    private val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    private val isAutomotive = context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)

    /**
     * Only in the car does the lyric line replace the song info, so the phone's own
     * notification stays normal. Newer Android Auto versions don't put the phone in
     * car mode, so a connected Android Auto controller counts too.
     */
    @Volatile
    var isInCar = false
        private set

    private val carControllers = AtomicInteger(0)

    /** Called by the session callback when Android Auto connects to or leaves the session. */
    fun onCarControllerConnected(connected: Boolean) {
        if (connected) {
            carControllers.incrementAndGet()
        } else {
            carControllers.updateAndGet { (it - 1).coerceAtLeast(0) }
        }
        scope.launch { updateInCar() }
    }

    private fun checkInCar() =
        isAutomotive ||
            carControllers.get() > 0 ||
            uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_CAR

    private fun updateInCar() {
        val inCar = checkInCar()
        if (inCar != isInCar) {
            isInCar = inCar
            onChanged()
        }
    }

    /** The line to show in the now playing metadata for [mediaId], or null to leave it alone. */
    fun metadataLine(mediaId: String?): String? {
        if (!enabled || mediaId == null || mediaId != songId || !isInCar) return null
        return lines.getOrNull(currentIndex)?.text?.trim()?.takeIf { it.isNotEmpty() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start(songIds: Flow<String?>) {
        // Read once up front: Android Auto can ask for the tabs before the flow below
        // first emits, and would get a tab list without Lyrics.
        enabled = context.dataStore.get(AndroidAutoLyricsKey, true)

        scope.launch {
            context.dataStore.data
                .map { it[AndroidAutoLyricsKey] ?: true }
                .distinctUntilChanged()
                .collect {
                    enabled = it
                    onChanged()
                }
        }

        scope.launch {
            songIds
                .distinctUntilChanged()
                .flatMapLatest { id ->
                    if (id == null) {
                        flowOf(null to emptyList())
                    } else {
                        database.lyrics(id).map { id to parseSynced(it?.lyrics) }
                    }
                }.collect { (id, parsed) ->
                    songId = id
                    lines = parsed
                    currentIndex = -1
                    tick(force = true)
                }
        }

        // Car mode is a system call, so it's checked about once a second, not per metadata read.
        scope.launch {
            var sinceCarCheck = IDLE_MS
            while (isActive) {
                val ticking = enabled && lines.isNotEmpty() && isPlaying()
                if (sinceCarCheck >= IDLE_MS) {
                    sinceCarCheck = 0
                    updateInCar()
                }
                if (ticking) tick(force = false)
                val wait = if (ticking) TICK_MS else IDLE_MS
                sinceCarCheck += wait
                delay(wait)
            }
        }
    }

    private fun tick(force: Boolean) {
        val index =
            if (lines.isEmpty()) -1 else LyricsUtils.findCurrentLineIndex(lines, positionMs())
        if (force || index != currentIndex) {
            currentIndex = index
            onChanged()
        }
    }

    /** Unsynced lyrics have no timestamps, so there's no current line to follow. */
    private fun parseSynced(raw: String?): List<LyricsEntry> {
        if (raw.isNullOrBlank() || raw == LYRICS_NOT_FOUND || !raw.trimStart().startsWith("[")) {
            return emptyList()
        }
        return runCatching { LyricsUtils.parseLyrics(raw) }
            .getOrDefault(emptyList())
            .filterNot { it.isBackground }
    }

    companion object {
        private const val TICK_MS = 200L
        private const val IDLE_MS = 1000L

        /** Package of the Android Auto app, which connects to the session while projecting. */
        const val ANDROID_AUTO_PACKAGE = "com.google.android.projection.gearhead"
    }
}
