/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beatwave.music.constants.LiveBackgroundStyle
import kotlin.math.cos
import kotlin.math.sin

/**
 * 5 Distinct, High-Performance 60fps Live Animated Backgrounds for Player View
 */
@Composable
fun LivePlayerBackground(
    style: LiveBackgroundStyle,
    albumColors: List<Color> = emptyList(),
    isPlaying: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07070B))
    ) {
        when (style) {
            LiveBackgroundStyle.COSMIC_AURORA -> CosmicAuroraBackground(albumColors, isPlaying)
            LiveBackgroundStyle.LIQUID_MESH -> LiquidMeshBackground(albumColors, isPlaying)
            LiveBackgroundStyle.NEON_PULSE -> NeonPulseBackground(albumColors, isPlaying)
            LiveBackgroundStyle.STARFIELD_NEBULA -> StarfieldNebulaBackground(albumColors, isPlaying)
            LiveBackgroundStyle.PRISM_WAVE -> PrismWaveBackground(albumColors, isPlaying)
        }

        // Vignette gradient overlay for luxury contrast with player controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )
    }
}

/**
 * Style 1: Cosmic Aurora
 * Harmonic undulating polar aurora wave ribbons that drift across the screen.
 */
@Composable
private fun CosmicAuroraBackground(
    albumColors: List<Color>,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "auroraAnim")
    val duration = if (isPlaying) 9000 else 24000

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auroraWave"
    )

    val verticalDrift by infiniteTransition.animateFloat(
        initialValue = -40f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auroraDrift"
    )

    val color1 = albumColors.getOrNull(0) ?: Color(0xFF00FFA3) // Emerald Aurora
    val color2 = albumColors.getOrNull(1) ?: Color(0xFF6366F1) // Cosmic Indigo
    val color3 = albumColors.getOrNull(2) ?: Color(0xFF00E5FF) // Electric Teal
    val color4 = Color(0xFF2E0854) // Deep Space Plum

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .blur(32.dp)
    ) {
        val w = size.width
        val h = size.height

        // Background atmosphere
        drawRect(
            brush = Brush.verticalGradient(
                listOf(color4, Color(0xFF06060F), Color(0xFF030D1A))
            )
        )

        // Wave ribbon 1
        val path1 = Path().apply {
            moveTo(0f, h * 0.35f + verticalDrift)
            val steps = 12
            for (i in 0..steps) {
                val x = w * (i.toFloat() / steps)
                val y = h * 0.35f + verticalDrift + sin(wavePhase + i * 0.5f) * (h * 0.14f)
                lineTo(x, y)
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = path1,
            brush = Brush.linearGradient(
                colors = listOf(color1.copy(alpha = 0.55f), color2.copy(alpha = 0.40f), Color.Transparent),
                start = Offset(0f, h * 0.3f),
                end = Offset(w, h * 0.8f)
            )
        )

        // Wave ribbon 2
        val path2 = Path().apply {
            moveTo(0f, h * 0.55f - verticalDrift)
            val steps = 12
            for (i in 0..steps) {
                val x = w * (i.toFloat() / steps)
                val y = h * 0.55f - verticalDrift + cos(wavePhase * 1.2f + i * 0.4f) * (h * 0.16f)
                lineTo(x, y)
            }
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = path2,
            brush = Brush.linearGradient(
                colors = listOf(color3.copy(alpha = 0.50f), color1.copy(alpha = 0.35f), Color.Transparent),
                start = Offset(w, h * 0.4f),
                end = Offset(0f, h * 0.9f)
            )
        )
    }
}

/**
 * Style 2: Liquid Mesh (Apple Style)
 * Organic fluid metaball blobs slowly morphing and blending with album colors.
 */
@Composable
private fun LiquidMeshBackground(
    albumColors: List<Color>,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquidMeshAnim")
    val duration = if (isPlaying) 12000 else 30000

    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "meshTime"
    )

    val c1 = albumColors.getOrNull(0) ?: Color(0xFFF43F5E)
    val c2 = albumColors.getOrNull(1) ?: Color(0xFF8B5CF6)
    val c3 = albumColors.getOrNull(2) ?: Color(0xFF06B6D4)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .blur(36.dp)
    ) {
        val w = size.width
        val h = size.height

        drawRect(Color(0xFF09090E))

        // Blob 1: Upper right moving circularly
        val b1x = w * (0.65f + 0.18f * cos(animTime))
        val b1y = h * (0.30f + 0.14f * sin(animTime))
        val b1r = w * 0.55f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c1.copy(alpha = 0.65f), Color.Transparent),
                center = Offset(b1x, b1y),
                radius = b1r
            ),
            center = Offset(b1x, b1y),
            radius = b1r
        )

        // Blob 2: Bottom left moving in figure-8
        val b2x = w * (0.32f + 0.16f * sin(animTime * 1.3f))
        val b2y = h * (0.68f + 0.12f * cos(animTime))
        val b2r = w * 0.60f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c2.copy(alpha = 0.60f), Color.Transparent),
                center = Offset(b2x, b2y),
                radius = b2r
            ),
            center = Offset(b2x, b2y),
            radius = b2r
        )

        // Blob 3: Center dynamic pulse
        val b3x = w * (0.50f + 0.10f * cos(animTime * 0.8f))
        val b3y = h * (0.50f + 0.15f * sin(animTime * 0.8f))
        val b3r = w * 0.45f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c3.copy(alpha = 0.50f), Color.Transparent),
                center = Offset(b3x, b3y),
                radius = b3r
            ),
            center = Offset(b3x, b3y),
            radius = b3r
        )
    }
}

/**
 * Style 3: Cyberpunk Neon Pulse
 * Dynamic concentric glowing rings and radial energy waves in sync with playback.
 */
@Composable
private fun NeonPulseBackground(
    albumColors: List<Color>,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neonPulseAnim")
    val duration = if (isPlaying) 2600 else 6000

    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "neonPulse"
    )

    val neonCyan = albumColors.getOrNull(0) ?: Color(0xFF00F5FF)
    val neonMagenta = albumColors.getOrNull(1) ?: Color(0xFFFF007F)
    val neonViolet = Color(0xFF7000FF)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val centerPoint = Offset(w * 0.5f, h * 0.48f)
        val maxR = size.minDimension * 0.85f

        drawRect(Color(0xFF05050A))

        // Center ambient aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(neonViolet.copy(alpha = 0.28f), Color.Transparent),
                center = centerPoint,
                radius = maxR * 0.6f
            ),
            center = centerPoint,
            radius = maxR * 0.6f
        )

        // 3 concentric animated expanding energy waves
        for (ring in 0..2) {
            val ringProgress = (pulsePhase + ring * 0.333f) % 1f
            val ringRadius = maxR * ringProgress
            val ringAlpha = (1f - ringProgress) * 0.40f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        neonCyan.copy(alpha = ringAlpha * 0.3f),
                        neonMagenta.copy(alpha = ringAlpha),
                        Color.Transparent
                    ),
                    center = centerPoint,
                    radius = ringRadius.coerceAtLeast(1f)
                ),
                center = centerPoint,
                radius = ringRadius
            )
        }
    }
}

/**
 * Style 4: Starfield Nebula
 * Deep space celestial star particles drifting across glowing nebula clouds.
 */
@Composable
private fun StarfieldNebulaBackground(
    albumColors: List<Color>,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nebulaAnim")
    val duration = if (isPlaying) 14000 else 32000

    val nebulaRot by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "nebulaRot"
    )

    val twinkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    val nebulaColor = albumColors.getOrNull(0) ?: Color(0xFF6366F1)
    val starColor = Color(0xFFF8FAFC)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(Color(0xFF030308))

        // Glowing cosmic nebula core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    nebulaColor.copy(alpha = 0.30f),
                    Color(0xFF3B0764).copy(alpha = 0.22f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.45f),
                radius = w * 0.70f
            ),
            center = Offset(w * 0.5f, h * 0.45f),
            radius = w * 0.70f
        )

        // Drifting star particles (seeded predictably)
        for (i in 0..48) {
            val pseudoX = ((i * 37) % 100) / 100f
            val pseudoY = ((i * 59) % 100) / 100f
            val starR = if (i % 5 == 0) 2.4f else 1.2f
            val alphaFactor = if (i % 2 == 0) twinkleAlpha else (1.3f - twinkleAlpha)

            drawCircle(
                color = starColor.copy(alpha = (alphaFactor * 0.65f).coerceIn(0.1f, 0.9f)),
                radius = starR,
                center = Offset(w * pseudoX, h * pseudoY)
            )
        }
    }
}

/**
 * Style 5: Prism Spectral Wave
 * Anamorphic prismatic glass refraction with iridescent sweeping light flares.
 */
@Composable
private fun PrismWaveBackground(
    albumColors: List<Color>,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "prismAnim")
    val duration = if (isPlaying) 7000 else 18000

    val sweepProgress by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "prismSweep"
    )

    val prismColors = listOf(
        Color(0xFFFF0055).copy(alpha = 0.40f), // Ruby
        Color(0xFFFF9900).copy(alpha = 0.35f), // Gold
        Color(0xFF00FFCC).copy(alpha = 0.45f), // Mint
        Color(0xFF0099FF).copy(alpha = 0.40f), // Cobalt
        Color(0xFFCC00FF).copy(alpha = 0.35f)  // Violet
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(Color(0xFF08080D))

        val startX = w * sweepProgress
        val startY = 0f
        val endX = startX + w * 0.65f
        val endY = h

        drawRect(
            brush = Brush.linearGradient(
                colors = prismColors + listOf(Color.Transparent),
                start = Offset(startX, startY),
                end = Offset(endX, endY)
            )
        )
    }
}

/**
 * Interactive style selection item with live animated thumbnail preview
 */
@Composable
fun LiveBackgroundStyleItemCard(
    style: LiveBackgroundStyle,
    title: String,
    tagline: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color(0xFF14141A),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live animated mini thumbnail preview
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                LivePlayerBackground(
                    style = style,
                    isPlaying = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                )
                Text(
                    text = tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = null
            )
        }
    }
}
