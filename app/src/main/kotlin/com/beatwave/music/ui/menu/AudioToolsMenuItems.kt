/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.menu

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import com.beatwave.music.LocalDownloadUtil
import com.beatwave.music.R
import com.beatwave.music.constants.EightDDepthKey
import com.beatwave.music.constants.EightDEnabledKey
import com.beatwave.music.constants.EightDRotationHzKey
import com.beatwave.music.playback.audio.EightDAudioProcessor
import com.beatwave.music.models.MediaMetadata
import com.beatwave.music.ui.component.EightDDialog
import com.beatwave.music.ui.component.Material3MenuItemData
import com.beatwave.music.utils.SongExporter
import com.beatwave.music.utils.rememberPreference
import kotlinx.coroutines.launch

/**
 * The "export this track" and "8D audio" menu rows, plus the state they need.
 *
 * Lives here rather than inside a menu because the app ships two player menus —
 * [PlayerMenu] and [OldPlayerMenu], picked per player theme — and a feature
 * added to only one of them is invisible to half the users. Returning the rows
 * lets each menu place them in its own group ordering while the behaviour stays
 * defined once.
 *
 * Emits the 8D dialog itself, so callers only have to place the returned rows.
 */
@Composable
fun rememberAudioToolsMenuItems(
    mediaMetadata: MediaMetadata,
): List<Material3MenuItemData> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val downloadUtil = LocalDownloadUtil.current

    var isExporting by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("audio/mp4")
    ) { destination ->
        if (destination == null) return@rememberLauncherForActivityResult
        isExporting = true
        coroutineScope.launch {
            val result = SongExporter.export(
                context = context,
                songId = mediaMetadata.id,
                destination = destination,
                playerCache = downloadUtil.playerCache,
                downloadCache = downloadUtil.downloadCache,
            )
            isExporting = false
            val message = when (result) {
                is SongExporter.Result.Success -> context.getString(R.string.export_audio_saved)
                is SongExporter.Result.Failure ->
                    context.getString(R.string.export_audio_failed, result.message)
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    val (eightDEnabled, onEightDEnabledChange) = rememberPreference(
        EightDEnabledKey,
        defaultValue = false,
    )
    val (eightDRotationHz, onEightDRotationHzChange) = rememberPreference(
        EightDRotationHzKey,
        defaultValue = 0.125f,
    )
    val (eightDDepth, onEightDDepthChange) = rememberPreference(
        EightDDepthKey,
        defaultValue = EightDAudioProcessor.DEFAULT_DEPTH,
    )
    var showEightDDialog by rememberSaveable { mutableStateOf(false) }

    if (showEightDDialog) {
        EightDDialog(
            enabled = eightDEnabled,
            rotationHz = eightDRotationHz,
            depth = eightDDepth,
            onEnabledChange = onEightDEnabledChange,
            onRotationHzChange = onEightDRotationHzChange,
            onDepthChange = onEightDDepthChange,
            onDismiss = { showEightDDialog = false },
        )
    }

    return listOf(
        Material3MenuItemData(
            title = {
                Text(
                    text = if (isExporting) {
                        stringResource(R.string.export_audio_in_progress)
                    } else {
                        stringResource(R.string.export_audio)
                    }
                )
            },
            description = { Text(text = stringResource(R.string.export_audio_desc)) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.download),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
            onClick = {
                if (!isExporting) {
                    exportLauncher.launch(
                        SongExporter.suggestedFileName(
                            title = mediaMetadata.title,
                            artist = mediaMetadata.artists.joinToString { it.name },
                        )
                    )
                }
            },
        ),
        Material3MenuItemData(
            title = { Text(text = stringResource(R.string.eight_d_audio)) },
            description = {
                Text(
                    text = if (eightDEnabled) {
                        stringResource(R.string.eight_d_audio_on)
                    } else {
                        stringResource(R.string.eight_d_audio_desc)
                    }
                )
            },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.spatial_tracking_apple),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            },
            trailingContent = {
                Switch(checked = eightDEnabled, onCheckedChange = onEightDEnabledChange)
            },
            onClick = { showEightDDialog = true },
        ),
    )
}
