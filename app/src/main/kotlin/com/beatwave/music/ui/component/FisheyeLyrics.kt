/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beatwave.music.constants.AppleMusicLyricsBlurKey
import com.beatwave.music.lyrics.LyricsEntry
import com.beatwave.music.ui.screens.settings.LyricsPosition
import com.beatwave.music.utils.rememberPreference
import kotlin.math.abs

/**
 * Fisheye Lyrics style featuring a kinetic 3D barrel / convex lens perspective wrap:
 * - Centered active line: Enormous, heavy bold typography (uppercase/chunky), fully opaque white with subtle glow, crisp focus.
 * - Inactive lines: Perspective wrap around a 3D cylinder or convex lens (negative rotationX above, positive rotationX below),
 *   smooth scale drop-off, subtle horizontal scale squeeze for barrel distortion, progressive blur and alpha falloff.
 * - Kinetic word-by-word active glow / highlight interpolation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FisheyeLyricsLine(
    entry: LyricsEntry,
    nextEntryTime: Long?,
    effectivePlaybackPosition: Long,
    isSynced: Boolean,
    isActive: Boolean,
    distanceFromCurrent: Int,
    lyricsTextPosition: LyricsPosition,
    textColor: Color,
    showRomanized: Boolean,
    showTranslated: Boolean,
    textSize: Float,
    lineSpacing: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean,
    isSelectionModeActive: Boolean,
    isAutoScrollActive: Boolean,
    expressiveAccent: Color,
    modifier: Modifier = Modifier
) {
    val (appleMusicLyricsBlur) = rememberPreference(AppleMusicLyricsBlurKey, true)
    val absDistance = abs(distanceFromCurrent)

    // 1. Perspective 3D rotationX around cylinder:
    // Lines above current active line tilt backward (negative rotationX)
    // Lines below current active line tilt forward/downward (positive rotationX)
    val targetRotationX = when {
        !isSynced || isSelectionModeActive || isActive -> 0f
        distanceFromCurrent == -1 -> -18f
        distanceFromCurrent == -2 -> -30f
        distanceFromCurrent <= -3 -> -40f
        distanceFromCurrent == 1 -> 18f
        distanceFromCurrent == 2 -> 30f
        distanceFromCurrent >= 3 -> 40f
        else -> 0f
    }

    val animatedRotationX by animateFloatAsState(
        targetValue = targetRotationX,
        animationSpec = tween(durationMillis = 350),
        label = "fisheyeRotationX"
    )

    // 2. Scale & Barrel curve distortion (horizontal squeeze creating convex bulge)
    val targetScaleY = when {
        !isSynced || isSelectionModeActive -> 1f
        isActive -> 1.15f
        absDistance == 1 -> 0.88f
        absDistance == 2 -> 0.74f
        absDistance == 3 -> 0.62f
        else -> 0.52f
    }

    val targetScaleX = when {
        !isSynced || isSelectionModeActive -> 1f
        isActive -> 1.15f
        absDistance == 1 -> 0.82f // subtle extra horizontal squeeze for convex barrel curve
        absDistance == 2 -> 0.66f
        absDistance == 3 -> 0.52f
        else -> 0.42f
    }

    val animatedScaleX by animateFloatAsState(
        targetValue = targetScaleX,
        animationSpec = tween(durationMillis = 350),
        label = "fisheyeScaleX"
    )

    val animatedScaleY by animateFloatAsState(
        targetValue = targetScaleY,
        animationSpec = tween(durationMillis = 350),
        label = "fisheyeScaleY"
    )

    // 3. Progressive Blur falloff: active line has crisp focus (0.dp)
    val targetBlur = if (!appleMusicLyricsBlur || !isAutoScrollActive || isActive || !isSynced || isSelectionModeActive) {
        0f
    } else {
        when (absDistance) {
            1 -> 1.5f
            2 -> 4f
            3 -> 7f
            else -> 10f
        }
    }

    val animatedBlur by animateFloatAsState(
        targetValue = targetBlur,
        animationSpec = tween(durationMillis = 350),
        label = "fisheyeBlur"
    )

    // 4. Alpha falloff: active line is fully opaque white (1f)
    val targetAlpha = when {
        !isSynced || (isSelectionModeActive && isSelected) -> 1f
        isActive -> 1f
        absDistance == 1 -> 0.60f
        absDistance == 2 -> 0.35f
        absDistance == 3 -> 0.18f
        else -> 0.08f
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 300),
        label = "fisheyeAlpha"
    )

    val duration = remember(entry.time, nextEntryTime) {
        if (nextEntryTime != null) nextEntryTime - entry.time else 4000L
    }

    val activeDuration = remember(duration) {
        (duration * 0.95).toLong().coerceAtLeast(300L)
    }

    // Word segmentation: precise word timestamps or character-proportional estimation
    val wordData = remember(entry.text, entry.words, activeDuration) {
        val isHindiText = com.beatwave.music.lyrics.LyricsUtils.isHindi(entry.text)
        if (!isHindiText && entry.words != null && entry.words.isNotEmpty()) {
            entry.words.map { word ->
                val wordStart = ((word.startTime * 1000).toLong() - entry.time).coerceAtLeast(0L)
                val wordEnd = ((word.endTime * 1000).toLong() - entry.time).coerceAtLeast(wordStart + 50L)
                Triple(word.text, wordStart, wordEnd)
            }
        } else {
            val words = entry.text.split(" ").filter { it.isNotEmpty() }
            if (words.isEmpty()) {
                listOf(Triple(entry.text, 0L, activeDuration))
            } else {
                val totalChars = entry.text.length
                var accumulatedTime = 0L
                words.mapIndexed { index, word ->
                    val wordLength = word.length
                    val includeSpace = index < words.size - 1
                    val charCount = if (includeSpace) wordLength + 1 else wordLength
                    val wordStart = accumulatedTime
                    val wordDur = if (totalChars > 0) (activeDuration * charCount.toFloat() / totalChars).toLong() else activeDuration
                    val wordEnd = wordStart + wordDur
                    accumulatedTime += wordDur
                    Triple(word, wordStart, wordEnd)
                }
            }
        }
    }

    val density = LocalDensity.current.density
    val itemModifier = modifier
        .fillMaxWidth()
        .graphicsLayer {
            this.cameraDistance = 16f * density
            this.rotationX = animatedRotationX
            this.scaleX = animatedScaleX
            this.scaleY = animatedScaleY
            this.alpha = animatedAlpha
            this.transformOrigin = TransformOrigin.Center
        }
        .clip(RoundedCornerShape(16.dp))
        .combinedClickable(
            enabled = true,
            onClick = onClick,
            onLongClick = onLongClick
        )
        .background(
            if (isSelected && isSelectionModeActive)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            else Color.Transparent
        )
        .padding(horizontal = 20.dp, vertical = (6 * lineSpacing).dp)
        .blur(animatedBlur.dp)

    // Fisheye projection is centered around the convex apex
    val agentAlignment = when {
        entry.isBackground -> Alignment.CenterHorizontally
        entry.agent == "v1" -> Alignment.Start
        entry.agent == "v2" -> Alignment.End
        entry.agent == "v1000" -> Alignment.CenterHorizontally
        lyricsTextPosition == LyricsPosition.RIGHT -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

    val agentTextAlign = when {
        entry.isBackground -> TextAlign.Center
        entry.agent == "v1" -> TextAlign.Left
        entry.agent == "v2" -> TextAlign.Right
        entry.agent == "v1000" -> TextAlign.Center
        lyricsTextPosition == LyricsPosition.RIGHT -> TextAlign.Right
        else -> TextAlign.Center
    }

    val baseFontSize = (textSize * 1.25f).sp
    val baseLineHeight = (textSize * 1.35f * lineSpacing.coerceAtMost(1.3f)).sp

    Column(
        modifier = itemModifier,
        horizontalAlignment = agentAlignment
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = when (agentTextAlign) {
                TextAlign.Center -> Arrangement.Center
                TextAlign.Right -> Arrangement.End
                else -> Arrangement.Center
            },
            verticalArrangement = Arrangement.spacedBy(
                with(LocalDensity.current) { (textSize * (lineSpacing.coerceAtMost(1.3f) - 1f)).sp.toDp() }
            )
        ) {
            wordData.forEachIndexed { index, (wordText, startRelative, endRelative) ->
                val lineRelTime = (effectivePlaybackPosition - entry.time).coerceAtLeast(0L)
                val wordDuration = (endRelative - startRelative).coerceAtLeast(1L)

                val progress by animateFloatAsState(
                    targetValue = when {
                        !isSynced || !isActive -> if (isActive) 1f else 0f
                        lineRelTime >= endRelative -> 1f
                        lineRelTime < startRelative -> 0f
                        else -> (lineRelTime - startRelative).toFloat() / wordDuration
                    },
                    animationSpec = tween(durationMillis = 150, easing = LinearEasing),
                    label = "fisheyeWordProgress"
                )

                // Enormous, heavy bold typography (uppercase / chunky)
                val chunkyWord = remember(wordText) { wordText.uppercase() }
                val fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold

                // Word-by-word active glow / highlight
                val wordBrush = if (isActive && isSynced) {
                    when {
                        progress >= 1f -> Brush.horizontalGradient(listOf(Color.White, Color.White))
                        progress <= 0f -> Brush.horizontalGradient(
                            listOf(Color.White.copy(alpha = 0.40f), Color.White.copy(alpha = 0.40f))
                        )
                        else -> Brush.horizontalGradient(
                            0.0f to Color.White,
                            (progress - 0.05f).coerceAtLeast(0f) to Color.White,
                            (progress + 0.05f).coerceAtMost(1f) to Color.White.copy(alpha = 0.40f),
                            1.0f to Color.White.copy(alpha = 0.40f)
                        )
                    }
                } else {
                    Brush.horizontalGradient(listOf(textColor, textColor))
                }

                val wordShadow = when {
                    !isActive || !isSynced -> null
                    progress in 0.01f..0.99f -> Shadow(
                        color = Color.White.copy(alpha = 0.95f),
                        offset = Offset.Zero,
                        blurRadius = 20f
                    )
                    progress >= 1f -> Shadow(
                        color = Color.White.copy(alpha = 0.35f),
                        offset = Offset.Zero,
                        blurRadius = 8f
                    )
                    else -> null
                }

                Text(
                    text = chunkyWord,
                    fontSize = baseFontSize,
                    style = TextStyle(
                        brush = wordBrush,
                        fontWeight = fontWeight,
                        letterSpacing = 0.5.sp,
                        lineHeight = baseLineHeight,
                        textAlign = agentTextAlign,
                        shadow = wordShadow
                    )
                )

                if (index != wordData.lastIndex) {
                    Text(
                        text = " ",
                        fontSize = baseFontSize,
                        color = if (isActive && isSynced) {
                            if (lineRelTime >= endRelative) Color.White else Color.White.copy(alpha = 0.40f)
                        } else textColor,
                        lineHeight = baseLineHeight,
                        style = TextStyle(
                            shadow = if (isActive && isSynced && lineRelTime >= endRelative) {
                                Shadow(
                                    color = Color.White.copy(alpha = 0.30f),
                                    offset = Offset.Zero,
                                    blurRadius = 6f
                                )
                            } else null
                        )
                    )
                }
            }
        }

        // Romanized text support
        if (showRomanized) {
            val romanizedText by entry.romanizedTextFlow.collectAsState()
            romanizedText?.let { romanized ->
                Text(
                    text = romanized.uppercase(),
                    fontSize = (baseFontSize.value * 0.55f).sp,
                    color = Color.White.copy(alpha = if (isActive) 0.85f else 0.45f),
                    textAlign = agentTextAlign,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                    lineHeight = (baseFontSize.value * 0.55f * lineSpacing.coerceAtMost(1.3f)).sp
                )
            }
        }

        // Translated text support
        if (showTranslated) {
            val translatedText by entry.translatedTextFlow.collectAsState()
            translatedText?.let { translated ->
                Text(
                    text = translated,
                    fontSize = (baseFontSize.value * 0.60f).sp,
                    color = expressiveAccent.copy(alpha = if (isActive) 0.90f else 0.50f),
                    textAlign = agentTextAlign,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                    lineHeight = (baseFontSize.value * 0.60f * lineSpacing.coerceAtMost(1.3f)).sp
                )
            }
        }
    }
}
