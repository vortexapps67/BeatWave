/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
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
import kotlin.math.roundToInt

/**
 * 8D audio controls: the on/off switch and how fast the image orbits.
 *
 * Speed is shown as seconds per lap rather than Hz — "12s per rotation" is
 * something you can hear and predict; "0.08 Hz" is not.
 */
@Composable
fun EightDDialog(
    enabled: Boolean,
    rotationHz: Float,
    onEnabledChange: (Boolean) -> Unit,
    onRotationHzChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.eight_d_audio)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                val secondsPerLap = (1f / rotationHz).roundToInt()
                Text(
                    text = stringResource(R.string.eight_d_rotation_speed, secondsPerLap),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
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
