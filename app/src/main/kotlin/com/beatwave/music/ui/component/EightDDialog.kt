/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.beatwave.music.R
import com.beatwave.music.playback.audio.EightDAudioProcessor
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A named speed/width combination, so the common cases are one tap instead of
 * two slider drags. The sliders stay available underneath for anything else.
 */
private data class EightDPreset(
    val labelRes: Int,
    val rotationHz: Float,
    val depth: Float,
)

private val EightDPresets = listOf(
    EightDPreset(R.string.eight_d_preset_subtle, rotationHz = 0.07f, depth = 0.55f),
    EightDPreset(R.string.eight_d_preset_classic, rotationHz = 0.125f, depth = 0.85f),
    EightDPreset(R.string.eight_d_preset_intense, rotationHz = 0.25f, depth = 1f),
)

/**
 * 8D audio controls: on/off, how fast the image orbits, and how wide it swings.
 *
 * Speed is shown as seconds per lap rather than Hz — "8s per lap" is something
 * you can hear and predict; "0.125 Hz" is not.
 */
@Composable
fun EightDDialog(
    enabled: Boolean,
    rotationHz: Float,
    depth: Float,
    onEnabledChange: (Boolean) -> Unit,
    onRotationHzChange: (Float) -> Unit,
    onDepthChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.eight_d_audio)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.eight_d_audio_long_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.eight_d_audio),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Switch(checked = enabled, onCheckedChange = onEnabledChange)
                }

                HorizontalDivider()

                Text(
                    text = stringResource(R.string.eight_d_presets),
                    style = MaterialTheme.typography.labelLarge,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EightDPresets.forEach { preset ->
                        // Float equality would almost never match once a slider
                        // has been touched, so treat "close enough" as selected.
                        val selected = abs(preset.rotationHz - rotationHz) < 0.005f &&
                            abs(preset.depth - depth) < 0.02f
                        FilterChip(
                            selected = selected,
                            enabled = enabled,
                            onClick = {
                                onRotationHzChange(preset.rotationHz)
                                onDepthChange(preset.depth)
                            },
                            label = { Text(stringResource(preset.labelRes)) },
                        )
                    }
                }

                val secondsPerLap = (1f / rotationHz).roundToInt()
                Text(
                    text = stringResource(R.string.eight_d_rotation_speed, secondsPerLap),
                    style = MaterialTheme.typography.bodyMedium,
                )
                // Inverted: dragging right should feel like "faster", but a
                // faster orbit is a LOWER seconds-per-lap, so the slider maps
                // max..min rather than min..max.
                Slider(
                    value = EightDAudioProcessor.MAX_ROTATION_HZ +
                        EightDAudioProcessor.MIN_ROTATION_HZ - rotationHz,
                    onValueChange = { shown ->
                        onRotationHzChange(
                            EightDAudioProcessor.MAX_ROTATION_HZ +
                                EightDAudioProcessor.MIN_ROTATION_HZ - shown
                        )
                    },
                    valueRange = EightDAudioProcessor.MIN_ROTATION_HZ..EightDAudioProcessor.MAX_ROTATION_HZ,
                    enabled = enabled,
                )

                Text(
                    text = stringResource(R.string.eight_d_width, (depth * 100).roundToInt()),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = depth,
                    onValueChange = onDepthChange,
                    valueRange = 0.2f..1f,
                    enabled = enabled,
                )

                Text(
                    text = stringResource(R.string.eight_d_headphones_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.ok)) }
        },
    )
}
