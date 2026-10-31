/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beatwave.music.lyrics.LyricsEntry
import kotlin.math.sin

/**
 * 1. NEON PULSE:
 * Cyberpunk electric neon tubes with reactive glowing aura, vibrant cyan-to-magenta
 * gradient, dynamic breathing bloom, and rhythmic word illumination.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NeonPulseLyricsLine(
    entry: LyricsEntry,
    isActive: Boolean,
    effectivePlaybackPosition: Long,
    expressiveAccent: Color,
    baseFontSize: Float,
    lineHeight: Float,
    agentTextAlign: TextAlign,
    showRomanized: Boolean = false,
    showTranslated: Boolean = false,
) {
    val arrangement = when (agentTextAlign) {
        TextAlign.Center -> Arrangement.Center
        TextAlign.Right -> Arrangement.End
        else -> Arrangement.Start
    }

    val infiniteTransition = rememberInfiniteTransition(label = "neonPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neonGlowPulse"
    )

    val neonCyan = Color(0xFF00F5FF)
    val neonMagenta = Color(0xFFFF007F)
    val neonViolet = Color(0xFFB026FF)

    val neonBrush = remember(pulse, isActive) {
        if (isActive) {
            Brush.horizontalGradient(
                colors = listOf(
                    neonCyan.copy(alpha = 0.95f),
                    neonMagenta.copy(alpha = 0.95f),
                    neonViolet.copy(alpha = 0.95f)
                )
            )
        } else {
            Brush.horizontalGradient(
                colors = listOf(
                    neonCyan.copy(alpha = 0.45f),
                    neonMagenta.copy(alpha = 0.45f)
                )
            )
        }
    }

    val words = entry.words ?: emptyList()

    if (words.isNotEmpty()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = arrangement,
            verticalArrangement = Arrangement.spacedBy(
                with(LocalDensity.current) { (baseFontSize * 0.25f).sp.toDp() }
            )
        ) {
            words.forEachIndexed { index, word ->
                val wordStartMs = (word.startTime * 1000).toLong()
                val wordEndMs = (word.endTime * 1000).toLong()
                val isWordActive = isActive && effectivePlaybackPosition in wordStartMs..wordEndMs
                val isWordPassed = (isActive && effectivePlaybackPosition > wordEndMs) || (!isActive && effectivePlaybackPosition > entry.time)

                val wordScale by animateFloatAsState(
                    targetValue = if (isWordActive) 1.08f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "wordScale"
                )

                val wordAlpha = when {
                    isWordActive -> 1.0f
                    isWordPassed -> 0.92f
                    isActive -> 0.45f
                    else -> 0.35f
                }

                val wordShadow = when {
                    isWordActive -> Shadow(
                        color = neonCyan.copy(alpha = 0.9f * pulse),
                        offset = Offset.Zero,
                        blurRadius = 26f * pulse
                    )
                    isWordPassed && isActive -> Shadow(
                        color = neonMagenta.copy(alpha = 0.6f),
                        offset = Offset.Zero,
                        blurRadius = 14f
                    )
                    else -> null
                }

                Text(
                    text = word.text + if (index < words.size - 1) " " else "",
                    fontSize = baseFontSize.sp,
                    fontWeight = if (isWordActive) FontWeight.Black else if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isWordActive) Color.White else neonCyan.copy(alpha = wordAlpha),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        shadow = wordShadow
                    ),
                    modifier = Modifier.graphicsLayer {
                        scaleX = wordScale
                        scaleY = wordScale
                    }
                )
            }
        }
    } else {
        // Line-synced / non-synced fallback with pulsing neon halo
        val shadow = if (isActive) {
            Shadow(
                color = neonCyan.copy(alpha = 0.85f * pulse),
                offset = Offset.Zero,
                blurRadius = 24f * pulse
            )
        } else null

        val styledText = buildAnnotatedString {
            val style = if (isActive) {
                SpanStyle(
                    brush = neonBrush,
                    fontWeight = FontWeight.ExtraBold,
                    shadow = shadow
                )
            } else {
                SpanStyle(
                    color = neonCyan.copy(alpha = 0.45f),
                    fontWeight = FontWeight.SemiBold
                )
            }
            withStyle(style = style) {
                append(entry.text)
            }
        }

        Text(
            text = styledText,
            fontSize = baseFontSize.sp,
            textAlign = agentTextAlign,
            lineHeight = lineHeight.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 2. SPOTIFY BOUNCE:
 * Kinetic pop typography with dynamic elastic bounce physics on beat arrival,
 * punchy bold typography, and a modern frosted pill highlight on the active line.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpotifyBounceLyricsLine(
    entry: LyricsEntry,
    isActive: Boolean,
    effectivePlaybackPosition: Long,
    expressiveAccent: Color,
    baseFontSize: Float,
    lineHeight: Float,
    agentTextAlign: TextAlign,
) {
    val arrangement = when (agentTextAlign) {
        TextAlign.Center -> Arrangement.Center
        TextAlign.Right -> Arrangement.End
        else -> Arrangement.Start
    }

    val bounceScale by animateFloatAsState(
        targetValue = if (isActive) 1.04f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "spotifyBounceScale"
    )

    val words = entry.words ?: emptyList()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = bounceScale
                scaleY = bounceScale
            }
            .then(
                if (isActive) {
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(expressiveAccent.copy(alpha = 0.16f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                } else {
                    Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                }
            )
    ) {
        if (words.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = arrangement,
                verticalArrangement = Arrangement.spacedBy(
                    with(LocalDensity.current) { (baseFontSize * 0.2f).sp.toDp() }
                )
            ) {
                words.forEachIndexed { index, word ->
                    val wordStartMs = (word.startTime * 1000).toLong()
                    val wordEndMs = (word.endTime * 1000).toLong()
                    val isWordActive = isActive && effectivePlaybackPosition in wordStartMs..wordEndMs
                    val isWordPassed = (isActive && effectivePlaybackPosition > wordEndMs) || (!isActive && effectivePlaybackPosition > entry.time)

                    val wordPopScale by animateFloatAsState(
                        targetValue = if (isWordActive) 1.12f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                        label = "spotifyWordPop"
                    )

                    val wordColor = when {
                        isWordActive -> Color.White
                        isWordPassed -> Color.White.copy(alpha = 0.95f)
                        isActive -> Color.White.copy(alpha = 0.45f)
                        else -> expressiveAccent.copy(alpha = 0.35f)
                    }

                    Text(
                        text = word.text + if (index < words.size - 1) " " else "",
                        fontSize = baseFontSize.sp,
                        fontWeight = if (isWordActive || isWordPassed) FontWeight.Black else FontWeight.Bold,
                        color = wordColor,
                        modifier = Modifier.graphicsLayer {
                            scaleX = wordPopScale
                            scaleY = wordPopScale
                        }
                    )
                }
            }
        } else {
            Text(
                text = entry.text,
                fontSize = baseFontSize.sp,
                textAlign = agentTextAlign,
                fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                color = if (isActive) Color.White else expressiveAccent.copy(alpha = 0.38f),
                lineHeight = lineHeight.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 3. CINEMATIC MINIMAL:
 * High-fashion luxury editorial aesthetic (Apple Music Classical / Studio magazine).
 * Ultra-wide tracking, elegant serif typography, warm champagne gold focus,
 * and feather-smooth depth of field.
 */
@Composable
fun CinematicMinimalLyricsLine(
    entry: LyricsEntry,
    isActive: Boolean,
    effectivePlaybackPosition: Long,
    baseFontSize: Float,
    lineHeight: Float,
    agentTextAlign: TextAlign,
) {
    val luxuryGold = Color(0xFFF6F0E2)
    val luxuryAccent = Color(0xFFFFDF9E)

    val words = entry.words ?: emptyList()

    if (words.isNotEmpty()) {
        val styledText = buildAnnotatedString {
            words.forEachIndexed { index, word ->
                val wordStartMs = (word.startTime * 1000).toLong()
                val wordEndMs = (word.endTime * 1000).toLong()
                val isWordActive = isActive && effectivePlaybackPosition in wordStartMs..wordEndMs
                val isWordPassed = (isActive && effectivePlaybackPosition > wordEndMs)

                val wordColor = when {
                    isWordActive -> luxuryAccent
                    isWordPassed -> luxuryGold
                    isActive -> luxuryGold.copy(alpha = 0.5f)
                    else -> luxuryGold.copy(alpha = 0.28f)
                }

                val shadow = if (isWordActive) {
                    Shadow(color = luxuryAccent.copy(alpha = 0.6f), offset = Offset.Zero, blurRadius = 12f)
                } else null

                withStyle(
                    SpanStyle(
                        color = wordColor,
                        fontWeight = if (isWordActive) FontWeight.Bold else FontWeight.Normal,
                        shadow = shadow
                    )
                ) {
                    append(word.text)
                }
                if (index < words.size - 1) append(" ")
            }
        }

        Text(
            text = styledText,
            fontSize = (baseFontSize * 0.95f).sp,
            fontFamily = FontFamily.Serif,
            letterSpacing = 2.4.sp,
            textAlign = agentTextAlign,
            lineHeight = (lineHeight * 1.15f).sp,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Text(
            text = entry.text,
            fontSize = (baseFontSize * 0.95f).sp,
            fontFamily = FontFamily.Serif,
            letterSpacing = 2.4.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) luxuryGold else luxuryGold.copy(alpha = 0.28f),
            textAlign = agentTextAlign,
            lineHeight = (lineHeight * 1.15f).sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 4. GRADIENT SWEEP:
 * Mesmerizing continuous chromatic aurora wave sweeping smoothly across active lyrics,
 * with luminous liquid shimmer reflecting in real-time.
 */
@Composable
fun GradientSweepLyricsLine(
    entry: LyricsEntry,
    isActive: Boolean,
    effectivePlaybackPosition: Long,
    baseFontSize: Float,
    lineHeight: Float,
    agentTextAlign: TextAlign,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradientSweep")
    val sweepOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auroraSweepOffset"
    )

    val auroraColors = listOf(
        Color(0xFF00F5FF), // Cyan
        Color(0xFF00FF87), // Neon Green
        Color(0xFFFF007F), // Vivid Pink
        Color(0xFFFFD700), // Gold
        Color(0xFF7000FF), // Electric Violet
        Color(0xFF00F5FF)  // Cyan wrap
    )

    val words = entry.words ?: emptyList()

    if (words.isNotEmpty()) {
        val styledText = buildAnnotatedString {
            words.forEachIndexed { index, word ->
                val wordStartMs = (word.startTime * 1000).toLong()
                val wordEndMs = (word.endTime * 1000).toLong()
                val isWordActive = isActive && effectivePlaybackPosition in wordStartMs..wordEndMs
                val isWordPassed = (isActive && effectivePlaybackPosition > wordEndMs)

                val wordColor = when {
                    isWordActive -> Color.White
                    isWordPassed -> Color(0xFF00F5FF)
                    isActive -> Color.White.copy(alpha = 0.55f)
                    else -> Color.White.copy(alpha = 0.32f)
                }

                val shadow = if (isWordActive) {
                    Shadow(color = Color(0xFF00FF87).copy(alpha = 0.85f), offset = Offset.Zero, blurRadius = 22f)
                } else null

                withStyle(
                    SpanStyle(
                        color = wordColor,
                        fontWeight = if (isWordActive) FontWeight.Black else FontWeight.Bold,
                        shadow = shadow
                    )
                ) {
                    append(word.text)
                }
                if (index < words.size - 1) append(" ")
            }
        }

        Text(
            text = styledText,
            fontSize = baseFontSize.sp,
            textAlign = agentTextAlign,
            lineHeight = lineHeight.sp,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        val sweepBrush = if (isActive) {
            Brush.linearGradient(
                colors = auroraColors,
                start = Offset(sweepOffset, 0f),
                end = Offset(sweepOffset + 600f, 0f)
            )
        } else null

        val styledText = buildAnnotatedString {
            val style = if (isActive && sweepBrush != null) {
                SpanStyle(
                    brush = sweepBrush,
                    fontWeight = FontWeight.ExtraBold,
                    shadow = Shadow(color = Color(0xFF00F5FF).copy(alpha = 0.6f), blurRadius = 16f)
                )
            } else {
                SpanStyle(
                    color = Color.White.copy(alpha = 0.32f),
                    fontWeight = FontWeight.SemiBold
                )
            }
            withStyle(style = style) {
                append(entry.text)
            }
        }

        Text(
            text = styledText,
            fontSize = baseFontSize.sp,
            textAlign = agentTextAlign,
            lineHeight = lineHeight.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 5. RETRO CASSETTE:
 * 80s Synthwave / Lo-Fi CRT terminal aesthetic.
 * Monospace typography, chromatic aberration RGB split shadow, and pulsing cassette cursor!
 */
@Composable
fun RetroCassetteLyricsLine(
    entry: LyricsEntry,
    isActive: Boolean,
    effectivePlaybackPosition: Long,
    baseFontSize: Float,
    lineHeight: Float,
    agentTextAlign: TextAlign,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cassetteBlink")
    val cursorBlink by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorBlink"
    )

    val amberPhosphor = Color(0xFFFFB000)
    val synthwaveCyan = Color(0xFF00F5FF)
    val synthwaveMagenta = Color(0xFFFF007F)

    // Chromatic aberration RGB split shadow
    val chromaticShadow = if (isActive) {
        Shadow(
            color = synthwaveCyan.copy(alpha = 0.8f),
            offset = Offset(-3f, 0f),
            blurRadius = 6f
        )
    } else null

    val words = entry.words ?: emptyList()

    val displayText = buildString {
        append(entry.text)
        if (isActive && cursorBlink > 0.5f) {
            append(" █")
        }
    }

    Text(
        text = displayText,
        fontSize = (baseFontSize * 0.92f).sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
        color = if (isActive) amberPhosphor else amberPhosphor.copy(alpha = 0.35f),
        textAlign = agentTextAlign,
        lineHeight = lineHeight.sp,
        style = MaterialTheme.typography.bodyLarge.copy(
            shadow = chromaticShadow
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
