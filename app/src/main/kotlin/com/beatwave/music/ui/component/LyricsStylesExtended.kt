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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

/**
 * 1. NEON PULSE:
 * Cyberpunk electric neon tubes with reactive glowing aura, vibrant cyan-to-magenta
 * animated gradient, dynamic breathing bloom, and rhythmic word illumination.
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
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neonGlowPulse"
    )

    val sweepOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "neonSweepOffset"
    )

    val neonCyan = Color(0xFF00F5FF)
    val neonMagenta = Color(0xFFFF007F)
    val neonViolet = Color(0xFF9D00FF)

    val neonBrush = remember(sweepOffset, isActive) {
        if (isActive) {
            Brush.linearGradient(
                colors = listOf(neonCyan, neonMagenta, neonViolet, neonCyan),
                start = Offset(sweepOffset, 0f),
                end = Offset(sweepOffset + 600f, 0f)
            )
        } else {
            Brush.horizontalGradient(
                colors = listOf(
                    neonCyan.copy(alpha = 0.40f),
                    neonMagenta.copy(alpha = 0.40f)
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
                with(LocalDensity.current) { (baseFontSize * 0.20f).sp.toDp() }
            )
        ) {
            words.forEachIndexed { index, word ->
                val wordStartMs = (word.startTime * 1000).toLong()
                val wordEndMs = (word.endTime * 1000).toLong()
                val isWordActive = isActive && effectivePlaybackPosition in wordStartMs..wordEndMs
                val isWordPassed = (isActive && effectivePlaybackPosition > wordEndMs) || (!isActive && effectivePlaybackPosition > entry.time)

                val wordScale by animateFloatAsState(
                    targetValue = if (isWordActive) 1.10f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
                    label = "wordScale"
                )

                val wordAlpha = when {
                    isWordActive -> 1.0f
                    isWordPassed -> 0.95f
                    isActive -> 0.48f
                    else -> 0.35f
                }

                val wordShadow = when {
                    isWordActive -> Shadow(
                        color = neonCyan.copy(alpha = 0.90f * pulse),
                        offset = Offset.Zero,
                        blurRadius = 24f * pulse
                    )
                    isWordPassed && isActive -> Shadow(
                        color = neonMagenta.copy(alpha = 0.60f),
                        offset = Offset.Zero,
                        blurRadius = 14f
                    )
                    else -> null
                }

                Text(
                    text = word.text + if (index < words.size - 1) " " else "",
                    fontSize = baseFontSize.sp,
                    fontWeight = if (isWordActive) FontWeight.Black else if (isActive) FontWeight.Bold else FontWeight.SemiBold,
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
 * Kinetic pop typography with snappy elastic bounce physics on beat arrival,
 * pure high-contrast bold typography without background clutter.
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
        targetValue = if (isActive) 1.06f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = Spring.StiffnessMedium
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
            .padding(vertical = 4.dp)
    ) {
        if (words.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = arrangement,
                verticalArrangement = Arrangement.spacedBy(
                    with(LocalDensity.current) { (baseFontSize * 0.18f).sp.toDp() }
                )
            ) {
                words.forEachIndexed { index, word ->
                    val wordStartMs = (word.startTime * 1000).toLong()
                    val wordEndMs = (word.endTime * 1000).toLong()
                    val isWordActive = isActive && effectivePlaybackPosition in wordStartMs..wordEndMs
                    val isWordPassed = (isActive && effectivePlaybackPosition > wordEndMs) || (!isActive && effectivePlaybackPosition > entry.time)

                    val wordPopScale by animateFloatAsState(
                        targetValue = if (isWordActive) 1.15f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMedium),
                        label = "spotifyWordPop"
                    )

                    val wordColor = when {
                        isWordActive -> Color.White
                        isWordPassed -> Color.White.copy(alpha = 0.95f)
                        isActive -> Color.White.copy(alpha = 0.50f)
                        else -> Color.White.copy(alpha = 0.35f)
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
                color = if (isActive) Color.White else Color.White.copy(alpha = 0.38f),
                lineHeight = lineHeight.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 3. CINEMATIC MINIMAL:
 * High-fashion luxury editorial aesthetic (Apple Music Classical / Studio magazine).
 * Generous tracking, elegant typography, warm champagne focus,
 * and feather-smooth vertical rise on line activation.
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
    val luxuryIvory = Color(0xFFFAF7EE)
    val luxuryGold = Color(0xFFFFDFA6)

    val riseOffset by animateFloatAsState(
        targetValue = if (isActive) -4f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
        label = "cinematicRise"
    )

    val words = entry.words ?: emptyList()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = riseOffset
            }
    ) {
        if (words.isNotEmpty()) {
            val styledText = buildAnnotatedString {
                words.forEachIndexed { index, word ->
                    val wordStartMs = (word.startTime * 1000).toLong()
                    val wordEndMs = (word.endTime * 1000).toLong()
                    val isWordActive = isActive && effectivePlaybackPosition in wordStartMs..wordEndMs
                    val isWordPassed = (isActive && effectivePlaybackPosition > wordEndMs)

                    val wordColor = when {
                        isWordActive -> luxuryGold
                        isWordPassed -> luxuryIvory
                        isActive -> luxuryIvory.copy(alpha = 0.52f)
                        else -> luxuryIvory.copy(alpha = 0.30f)
                    }

                    val shadow = if (isWordActive) {
                        Shadow(color = luxuryGold.copy(alpha = 0.65f), offset = Offset.Zero, blurRadius = 14f)
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
                fontSize = (baseFontSize * 0.96f).sp,
                letterSpacing = 2.2.sp,
                textAlign = agentTextAlign,
                lineHeight = (lineHeight * 1.25f).sp,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = entry.text,
                fontSize = (baseFontSize * 0.96f).sp,
                letterSpacing = 2.2.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) luxuryIvory else luxuryIvory.copy(alpha = 0.30f),
                textAlign = agentTextAlign,
                lineHeight = (lineHeight * 1.25f).sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 4. GRADIENT SWEEP:
 * Mesmerizing continuous chromatic aurora wave sweeping smoothly across active lyrics,
 * with luminous liquid shimmer reflecting in real-time.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GradientSweepLyricsLine(
    entry: LyricsEntry,
    isActive: Boolean,
    effectivePlaybackPosition: Long,
    baseFontSize: Float,
    lineHeight: Float,
    agentTextAlign: TextAlign,
) {
    val arrangement = when (agentTextAlign) {
        TextAlign.Center -> Arrangement.Center
        TextAlign.Right -> Arrangement.End
        else -> Arrangement.Start
    }

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

    val sweepBrush = remember(sweepOffset, isActive) {
        if (isActive) {
            Brush.linearGradient(
                colors = auroraColors,
                start = Offset(sweepOffset, 0f),
                end = Offset(sweepOffset + 650f, 0f)
            )
        } else null
    }

    val words = entry.words ?: emptyList()

    if (words.isNotEmpty()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = arrangement,
            verticalArrangement = Arrangement.spacedBy(
                with(LocalDensity.current) { (baseFontSize * 0.18f).sp.toDp() }
            )
        ) {
            words.forEachIndexed { index, word ->
                val wordStartMs = (word.startTime * 1000).toLong()
                val wordEndMs = (word.endTime * 1000).toLong()
                val isWordActive = isActive && effectivePlaybackPosition in wordStartMs..wordEndMs
                val isWordPassed = (isActive && effectivePlaybackPosition > wordEndMs)

                val wordScale by animateFloatAsState(
                    targetValue = if (isWordActive) 1.08f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
                    label = "sweepWordScale"
                )

                val shadow = if (isWordActive) {
                    Shadow(color = Color(0xFF00FF87).copy(alpha = 0.85f), offset = Offset.Zero, blurRadius = 20f)
                } else null

                val styledWord = buildAnnotatedString {
                    val style = if (isWordActive && sweepBrush != null) {
                        SpanStyle(brush = sweepBrush, fontWeight = FontWeight.Black, shadow = shadow)
                    } else if (isWordPassed) {
                        SpanStyle(color = Color(0xFF00F5FF), fontWeight = FontWeight.Bold)
                    } else if (isActive) {
                        SpanStyle(color = Color.White.copy(alpha = 0.55f), fontWeight = FontWeight.SemiBold)
                    } else {
                        SpanStyle(color = Color.White.copy(alpha = 0.32f), fontWeight = FontWeight.Normal)
                    }
                    withStyle(style) {
                        append(word.text)
                    }
                    if (index < words.size - 1) append(" ")
                }

                Text(
                    text = styledWord,
                    fontSize = baseFontSize.sp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = wordScale
                        scaleY = wordScale
                    }
                )
            }
        }
    } else {
        val styledText = buildAnnotatedString {
            val style = if (isActive && sweepBrush != null) {
                SpanStyle(
                    brush = sweepBrush,
                    fontWeight = FontWeight.ExtraBold,
                    shadow = Shadow(color = Color(0xFF00F5FF).copy(alpha = 0.6f), blurRadius = 18f)
                )
            } else {
                SpanStyle(
                    color = Color.White.copy(alpha = 0.35f),
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
 * Monospace typography, chromatic aberration RGB split shadow, and smooth amber cursor.
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
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    val amberPhosphor = Color(0xFFFFB300)
    val synthwaveCyan = Color(0xFF00F5FF)

    val chromaticShadow = if (isActive) {
        Shadow(
            color = synthwaveCyan.copy(alpha = 0.75f),
            offset = Offset(-2.5f, 0f),
            blurRadius = 8f
        )
    } else null

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = when (agentTextAlign) {
            TextAlign.Center -> Arrangement.Center
            TextAlign.Right -> Arrangement.End
            else -> Arrangement.Start
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = entry.text,
            fontSize = (baseFontSize * 0.94f).sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) amberPhosphor else amberPhosphor.copy(alpha = 0.35f),
            lineHeight = lineHeight.sp,
            style = MaterialTheme.typography.bodyLarge.copy(
                shadow = chromaticShadow
            )
        )

        if (isActive) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .width((baseFontSize * 0.45f).dp)
                    .height((baseFontSize * 0.85f).dp)
                    .alpha(cursorAlpha)
                    .background(amberPhosphor)
            )
        }
    }
}


/**
 * 6. FISHEYE STICKER CYLINDER:
 * True cylindrical 3D rotating drum barrel with word-level cutout sticker badges
 * and contextual doodle emojis (cloud, sun, snowflake, eye, heart, flame, star),
 * matching the authentic music video fisheye aesthetic.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FisheyeStickerLyricsLine(
    entry: LyricsEntry,
    isActive: Boolean,
    effectivePlaybackPosition: Long,
    expressiveAccent: Color,
    baseFontSize: Float,
    lineHeight: Float,
    agentTextAlign: TextAlign = TextAlign.Center,
    agentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    showRomanized: Boolean = false,
    showTranslated: Boolean = false,
) {
    val arrangement = when (agentTextAlign) {
        TextAlign.Center -> Arrangement.Center
        TextAlign.Right -> Arrangement.End
        else -> Arrangement.Start
    }

    val wordList = entry.words
    val hasWords = wordList != null && wordList.isNotEmpty()

    fun getWordEmoji(rawWord: String): String? {
        val clean = rawWord.lowercase().trim().replace(Regex("[^a-z]"), "")
        return when {
            clean in setOf("sun", "light", "shine", "day", "bright", "sunny", "morning") -> "☀"
            clean in setOf("sky", "cloud", "clouds", "rain", "blue", "air", "high") -> "☁"
            clean in setOf("see", "look", "eyes", "eye", "watch", "sight", "blind") -> "👁"
            clean in setOf("cold", "ice", "freeze", "snow", "winter", "chilly") -> "❄"
            clean in setOf("love", "heart", "baby", "kiss", "trust", "sweet", "darling") -> "🤍"
            clean in setOf("fire", "burn", "hot", "flame", "blaze") -> "🔥"
            clean in setOf("night", "star", "dark", "moon", "midnight") -> "✨"
            clean in setOf("car", "drive", "road", "ride", "city", "speed") -> "🚗"
            clean in setOf("call", "phone", "talk", "hear", "voice") -> "📞"
            else -> null
        }
    }

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = arrangement,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (hasWords) {
            wordList!!.forEachIndexed { wordIndex, word ->
                val wordStartMs = (word.startTime * 1000).toLong()
                val wordEndMs = (word.endTime * 1000).toLong()
                val isWordActive = isActive && effectivePlaybackPosition >= wordStartMs && effectivePlaybackPosition <= wordEndMs
                val hasWordPassed = isActive && effectivePlaybackPosition > wordEndMs

                val badgeBg = when {
                    isWordActive -> Color.White
                    hasWordPassed -> Color.White.copy(alpha = 0.92f)
                    isActive -> Color.White.copy(alpha = 0.28f)
                    else -> Color.White.copy(alpha = 0.14f)
                }

                val textColor = when {
                    isWordActive || hasWordPassed -> Color.Black
                    isActive -> Color.White
                    else -> Color.White.copy(alpha = 0.72f)
                }

                val emoji = getWordEmoji(word.text)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp)
                ) {
                    if (emoji != null && (wordIndex % 2 == 0 || isWordActive)) {
                        Text(
                            text = emoji,
                            fontSize = (baseFontSize * 0.9f).sp,
                            modifier = Modifier.graphicsLayer {
                                rotationZ = if (wordIndex % 2 == 0) -8f else 8f
                            }
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeBg)
                                .then(
                                    if (isWordActive) {
                                        Modifier.graphicsLayer {
                                            scaleX = 1.06f
                                            scaleY = 1.06f
                                        }
                                    } else Modifier
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = word.text,
                                fontSize = (baseFontSize * 0.95f).sp,
                                fontWeight = if (isWordActive || hasWordPassed) FontWeight.ExtraBold else FontWeight.Bold,
                                color = textColor,
                                letterSpacing = (-0.4).sp,
                                lineHeight = (baseFontSize * 1.15f).sp
                            )
                        }

                        if (isWordActive) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }
        } else {
            val words = remember(entry.text) {
                entry.text.split(Regex("\\s+")).filter { it.isNotBlank() }
            }
            words.forEachIndexed { wordIndex, wordText ->
                val badgeBg = if (isActive) Color.White else Color.White.copy(alpha = 0.16f)
                val textColor = if (isActive) Color.Black else Color.White.copy(alpha = 0.75f)
                val emoji = getWordEmoji(wordText)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp)
                ) {
                    if (emoji != null && (wordIndex == 0 || wordIndex == words.size - 1)) {
                        Text(
                            text = emoji,
                            fontSize = (baseFontSize * 0.9f).sp,
                            modifier = Modifier.graphicsLayer {
                                rotationZ = if (wordIndex % 2 == 0) -7f else 7f
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = wordText,
                            fontSize = (baseFontSize * 0.95f).sp,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                            color = textColor,
                            letterSpacing = (-0.4).sp,
                            lineHeight = (baseFontSize * 1.15f).sp
                        )
                    }
                }
            }
        }
    }
}
