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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Fisheye Lyrics style featuring a refined, subtle cylindrical perspective wrap
 * and signature white capsule pill word highlight:
 * - Subtle cylindrical curvature: Gentle rotationX (-12f..+12f max), smooth natural scaling,
 *   subtle progressive blur and alpha falloff.
 * - Signature White Capsule Pill Highlight: As words are sung, they smoothly transition into
 *   a clean rounded white capsule pill with crisp dark text (#161616) inside, while unsung words
 *   remain smooth translucent white.
 * - Gentle floating wave motion: Subtle sinusoidal breathing drift (1.5dp) for an organic kinetic feel.
 * - Soft luminous bloom: Pure white and accent glow on singing words without harsh chromatic splits.
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

    // Continuous frame clock for gentle, calm floating wave drift on the active line
    var wallTime by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isActive) {
        if (isActive) {
            while (isActive) {
                withFrameMillis { frameTime ->
                    wallTime = frameTime
                }
            }
        }
    }

    // 1. Subtle, gentle cylindrical rotationX:
    // Gentle tilt without violent distortion (-12f..+12f max)
    val targetRotationX = when {
        !isSynced || isSelectionModeActive || isActive -> 0f
        distanceFromCurrent == -1 -> -4f
        distanceFromCurrent == -2 -> -8f
        distanceFromCurrent <= -3 -> -12f
        distanceFromCurrent == 1 -> 4f
        distanceFromCurrent == 2 -> 8f
        distanceFromCurrent >= 3 -> 12f
        else -> 0f
    }

    val animatedRotationX by animateFloatAsState(
        targetValue = targetRotationX,
        animationSpec = tween(durationMillis = 350),
        label = "fisheyeRotationX"
    )

    // 2. Smooth, natural scale progression
    val targetScaleY = when {
        !isSynced || isSelectionModeActive -> 1f
        isActive -> 1.06f
        absDistance == 1 -> 0.94f
        absDistance == 2 -> 0.85f
        absDistance == 3 -> 0.76f
        else -> 0.68f
    }

    val targetScaleX = when {
        !isSynced || isSelectionModeActive -> 1f
        isActive -> 1.06f
        absDistance == 1 -> 0.91f // subtle natural barrel squeeze
        absDistance == 2 -> 0.81f
        absDistance == 3 -> 0.71f
        else -> 0.62f
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

    // 3. Subtle progressive blur falloff (crisp focus on active line, gentle depth on distant lines)
    val targetBlur = if (!appleMusicLyricsBlur || !isAutoScrollActive || isActive || !isSynced || isSelectionModeActive) {
        0f
    } else {
        when (absDistance) {
            1 -> 0.8f
            2 -> 1.6f
            3 -> 2.5f
            else -> 3.5f
        }
    }

    val animatedBlur by animateFloatAsState(
        targetValue = targetBlur,
        animationSpec = tween(durationMillis = 350),
        label = "fisheyeBlur"
    )

    // 4. Smooth alpha falloff
    val targetAlpha = when {
        !isSynced || (isSelectionModeActive && isSelected) -> 1f
        isActive -> 1f
        absDistance == 1 -> 0.65f
        absDistance == 2 -> 0.42f
        absDistance == 3 -> 0.24f
        else -> 0.12f
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

    // Word segmentation: precise word timestamps or proportional estimation
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

    // Inactive lines tuck tightly and naturally around the central active line
    val lineVerticalPadding = when {
        isActive -> (3.5f * lineSpacing.coerceAtMost(1.15f)).dp
        absDistance == 1 -> (1.6f * lineSpacing.coerceAtMost(1.15f)).dp
        absDistance == 2 -> (1.0f * lineSpacing.coerceAtMost(1.15f)).dp
        else -> (0.6f * lineSpacing.coerceAtMost(1.15f)).dp
    }

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
        .clip(RoundedCornerShape(14.dp))
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
        .padding(horizontal = 14.dp, vertical = lineVerticalPadding)
        .blur(animatedBlur.dp)

    // Center alignment matching convex lens apex
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

    val baseFontSize = (textSize * 1.16f).sp
    val baseLineHeight = (textSize * 1.32f * lineSpacing.coerceAtMost(1.2f)).sp

    Column(
        modifier = itemModifier,
        horizontalAlignment = agentAlignment
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 5.dp,
                alignment = when (agentTextAlign) {
                    TextAlign.Center -> Alignment.CenterHorizontally
                    TextAlign.Right -> Alignment.End
                    else -> Alignment.CenterHorizontally
                }
            ),
            verticalArrangement = Arrangement.spacedBy(
                with(LocalDensity.current) { (textSize * 0.16f).sp.toDp() }
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

                // Calm floating wave drift (1.5dp height)
                val waveSpeed = 0.002f
                val waveHeight = 1.5f // subtle dp height
                val phaseOffset = index * 0.35f
                val waveY = if (isActive && wallTime > 0L) {
                    sin((wallTime * waveSpeed).toDouble() + phaseOffset).toFloat() * waveHeight
                } else 0f

                // Word is sung or currently singing
                val isSung = progress >= 0.05f
                val isSinging = progress in 0.05f..0.99f

                // Smooth animated pill transition
                val animatedPillAlpha by animateFloatAsState(
                    targetValue = if (isActive && isSynced && isSung) 1f else 0f,
                    animationSpec = tween(durationMillis = 200),
                    label = "pillAlpha"
                )

                // Subtle calm bounce (4% pop)
                val pillBounce = if (isSinging) (sin(progress * PI.toFloat()) * 0.04f) else 0f
                val wordScale = 1f + pillBounce

                // Smooth color transition from translucent white to dark text inside the white pill
                val wordTextColor = if (isActive && isSynced) {
                    lerp(
                        start = Color.White.copy(alpha = 0.70f),
                        stop = Color(0xFF161616),
                        fraction = animatedPillAlpha
                    )
                } else if (isActive) {
                    Color.White
                } else {
                    Color.White.copy(alpha = 0.65f)
                }

                // Soft luminous bloom glow during active singing
                val pillShadow = if (isSinging) {
                    Shadow(
                        color = Color.White.copy(alpha = 0.45f),
                        offset = Offset.Zero,
                        blurRadius = 12f
                    )
                } else null

                // White Capsule Pill Container for sung word
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            this.translationY = waveY * density
                            this.scaleX = wordScale
                            this.scaleY = wordScale
                            this.transformOrigin = TransformOrigin.Center
                        }
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = animatedPillAlpha))
                        .padding(
                            horizontal = if (animatedPillAlpha > 0.1f) 7.dp else 2.dp,
                            vertical = if (animatedPillAlpha > 0.1f) 2.dp else 1.dp
                        )
                ) {
                    Text(
                        text = wordText,
                        fontSize = baseFontSize,
                        style = TextStyle(
                            color = wordTextColor,
                            fontWeight = if (isSung) FontWeight.Bold else FontWeight.SemiBold,
                            letterSpacing = 0.3.sp,
                            lineHeight = baseLineHeight,
                            textAlign = agentTextAlign,
                            shadow = pillShadow
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
                    text = romanized,
                    fontSize = (baseFontSize.value * 0.58f).sp,
                    color = Color.White.copy(alpha = if (isActive) 0.85f else 0.45f),
                    textAlign = agentTextAlign,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp,
                    modifier = Modifier.padding(top = 2.dp).fillMaxWidth(),
                    lineHeight = (baseFontSize.value * 0.58f * lineSpacing.coerceAtMost(1.2f)).sp
                )
            }
        }

        // Translated text support
        if (showTranslated) {
            val translatedText by entry.translatedTextFlow.collectAsState()
            translatedText?.let { translated ->
                Text(
                    text = translated,
                    fontSize = (baseFontSize.value * 0.62f).sp,
                    color = expressiveAccent.copy(alpha = if (isActive) 0.90f else 0.50f),
                    textAlign = agentTextAlign,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp).fillMaxWidth(),
                    lineHeight = (baseFontSize.value * 0.62f * lineSpacing.coerceAtMost(1.2f)).sp
                )
            }
        }
    }
}
