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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sin

/**
 * Fluid Glow V2: Ambient Screen Edge Lighting
 *
 * Casts a synchronized, multi-spectral breathing glow along the screen boundaries,
 * matching dominant extracted colors from the playing track and pulsing with
 * rhythmic tempo dynamics.
 */
@Composable
fun AmbientEdgeGlow(
    colors: List<Color>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    intensity: Float = 0.85f,
) {
    if (colors.isEmpty() || intensity <= 0f) return

    val infiniteTransition = rememberInfiniteTransition(label = "AmbientEdgeGlowPulse")

    // Rhythmic breathing pulse (simulates beat drops and bass presence)
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 1100 else 3200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseProgress"
    )

    // Slow orbital color rotation around edges
    val phaseRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 16000 else 36000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseRotation"
    )

    val safeColors = remember(colors) {
        if (colors.size >= 2) colors else listOf(colors.firstOrNull() ?: Color(0xFF6200EE), Color(0xFF03DAC5))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val baseAlpha = (intensity * pulseProgress * 0.45f).coerceIn(0f, 1f)
                val edgeGlowDepth = size.minDimension * 0.18f * (0.85f + pulseProgress * 0.25f)
                val glowWidth = edgeGlowDepth.coerceAtLeast(32f)

                val c1 = safeColors[0].copy(alpha = baseAlpha)
                val c2 = safeColors[1 % safeColors.size].copy(alpha = baseAlpha * 0.85f)
                val c3 = safeColors.getOrElse(2) { safeColors[0] }.copy(alpha = baseAlpha * 0.7f)
                val transparent = Color.Transparent

                // Top Edge Glow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(c1, c2.copy(alpha = c2.alpha * 0.4f), transparent),
                        startY = 0f,
                        endY = glowWidth * 1.2f
                    ),
                    topLeft = Offset.Zero,
                    size = Size(size.width, glowWidth * 1.2f)
                )

                // Bottom Edge Glow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(transparent, c3.copy(alpha = c3.alpha * 0.4f), c2),
                        startY = size.height - glowWidth * 1.2f,
                        endY = size.height
                    ),
                    topLeft = Offset(0f, size.height - glowWidth * 1.2f),
                    size = Size(size.width, glowWidth * 1.2f)
                )

                // Left Edge Glow
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(c2, c1.copy(alpha = c1.alpha * 0.3f), transparent),
                        startX = 0f,
                        endX = glowWidth
                    ),
                    topLeft = Offset.Zero,
                    size = Size(glowWidth, size.height)
                )

                // Right Edge Glow
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(transparent, c3.copy(alpha = c3.alpha * 0.3f), c1),
                        startX = size.width - glowWidth,
                        endX = size.width
                    ),
                    topLeft = Offset(size.width - glowWidth, 0f),
                    size = Size(glowWidth, size.height)
                )

                // Corner blooms for organic fluid effect
                drawCornerBloom(c1, Offset(0f, 0f), glowWidth * 1.8f)
                drawCornerBloom(c2, Offset(size.width, 0f), glowWidth * 1.8f)
                drawCornerBloom(c3, Offset(0f, size.height), glowWidth * 1.8f)
                drawCornerBloom(c1, Offset(size.width, size.height), glowWidth * 1.8f)
            }
    )
}

private fun DrawScope.drawCornerBloom(color: Color, center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = color.alpha * 0.3f), Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}
