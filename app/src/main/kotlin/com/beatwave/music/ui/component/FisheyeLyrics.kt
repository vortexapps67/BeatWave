/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 * Fisheye Lyrics — clean cylindrical perspective with word-level pill highlights.
 *
 * Visual language (matches the reference video exactly):
 * ─────────────────────────────────────────────────────
 * 1. CLEAN CYLINDRICAL CURVATURE  — Gentle rotationX (max ±10°), smooth scale/alpha falloff.
 *    No glitch, no chromatic aberration, no noise.  Pure optical-lens feel.
 *
 * 2. WORD-LEVEL PILL HIGHLIGHTS   — Each word transitions through three visual states:
 *    • UPCOMING   → plain text, slightly faded, no pill
 *    • ACTIVE     → bright WHITE pill, DARK text inside (#111111), slightly scaled up
 *    • SUNG       → frosted semi-transparent pill (white @ 25%), white text
 *
 * 3. LINE PROGRESSION             — The currently-singing line is centered and largest.
 *    Already-sung lines above keep their frosted pills. Future lines below are plain text.
 *    Scale decreases symmetrically as distance from active grows.
 *
 * 4. ROUNDED SOFT FONT            — Bold weight, generous letter spacing, fully-rounded pill
 *    corners (RoundedCornerShape(50%)) for the capsule shape.
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
    val density = LocalDensity.current.density
    val isPastLine = distanceFromCurrent < 0 // already-sung line (above active)

    // ─── 1. Cylindrical rotationX ───────────────────────────────────────────
    // Gentle tilt: lines above tilt backward, lines below tilt forward.
    val targetRotationX = when {
        !isSynced || isSelectionModeActive || isActive -> 0f
        distanceFromCurrent == -1 -> -3f
        distanceFromCurrent == -2 -> -6f
        distanceFromCurrent <= -3 -> -10f
        distanceFromCurrent == 1 -> 3f
        distanceFromCurrent == 2 -> 6f
        distanceFromCurrent >= 3 -> 10f
        else -> 0f
    }

    val animatedRotationX by animateFloatAsState(
        targetValue = targetRotationX,
        animationSpec = tween(durationMillis = 400, easing = LinearEasing),
        label = "fisheyeRotX"
    )

    // ─── 2. Scale progression ───────────────────────────────────────────────
    // Active line is largest. Scale decreases with distance.
    val targetScale = when {
        !isSynced || isSelectionModeActive -> 1f
        isActive -> 1.0f
        absDistance == 1 -> 0.82f
        absDistance == 2 -> 0.70f
        absDistance == 3 -> 0.60f
        else -> 0.52f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fisheyeScale"
    )

    // ─── 3. Alpha falloff ───────────────────────────────────────────────────
    val targetAlpha = when {
        !isSynced || (isSelectionModeActive && isSelected) -> 1f
        isActive -> 1f
        absDistance == 1 -> 0.72f
        absDistance == 2 -> 0.50f
        absDistance == 3 -> 0.32f
        else -> 0.18f
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 300),
        label = "fisheyeAlpha"
    )

    // ─── 4. Subtle progressive blur ─────────────────────────────────────────
    val targetBlur = if (!appleMusicLyricsBlur || !isAutoScrollActive || isActive || !isSynced || isSelectionModeActive) {
        0f
    } else {
        when (absDistance) {
            1 -> 0.5f
            2 -> 1.2f
            3 -> 2.0f
            else -> 3.0f
        }
    }

    val animatedBlur by animateFloatAsState(
        targetValue = targetBlur,
        animationSpec = tween(durationMillis = 400),
        label = "fisheyeBlur"
    )

    // ─── Timing data ────────────────────────────────────────────────────────
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

    // ─── Vertical spacing ───────────────────────────────────────────────────
    val lineVerticalPadding = when {
        isActive -> (5f * lineSpacing.coerceAtMost(1.15f)).dp
        absDistance == 1 -> (3f * lineSpacing.coerceAtMost(1.15f)).dp
        absDistance == 2 -> (2f * lineSpacing.coerceAtMost(1.15f)).dp
        else -> (1.5f * lineSpacing.coerceAtMost(1.15f)).dp
    }

    // ─── Item modifier (cylindrical transform) ──────────────────────────────
    val itemModifier = modifier
        .fillMaxWidth()
        .graphicsLayer {
            this.cameraDistance = 14f * density
            this.rotationX = animatedRotationX
            this.scaleX = animatedScale
            this.scaleY = animatedScale
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
        .padding(horizontal = 16.dp, vertical = lineVerticalPadding)
        .blur(animatedBlur.dp)

    // ─── Text alignment ─────────────────────────────────────────────────────
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

    // ─── Font sizing ────────────────────────────────────────────────────────
    // Active line uses a bolder, larger font; inactive uses slightly smaller
    val baseFontSize = (textSize * 1.18f).sp
    val baseLineHeight = (textSize * 1.38f * lineSpacing.coerceAtMost(1.2f)).sp

    // ─── Pill color palette ─────────────────────────────────────────────────
    val activePillBg = Color.White                     // solid white pill for active word
    val activePillText = Color(0xFF111111)              // near-black text inside active pill
    val sungPillBg = Color.White.copy(alpha = 0.22f)   // frosted glass pill for already-sung words
    val sungPillText = Color.White                      // white text inside frosted pill
    val upcomingText = Color.White.copy(alpha = 0.55f)  // faded text for upcoming words

    Column(
        modifier = itemModifier,
        horizontalAlignment = agentAlignment
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 6.dp,
                alignment = when (agentTextAlign) {
                    TextAlign.Center -> Alignment.CenterHorizontally
                    TextAlign.Right -> Alignment.End
                    else -> Alignment.CenterHorizontally
                }
            ),
            verticalArrangement = Arrangement.spacedBy(
                with(LocalDensity.current) { (textSize * 0.22f).sp.toDp() }
            )
        ) {
            wordData.forEachIndexed { _, (wordText, startRelative, endRelative) ->
                val lineRelTime = (effectivePlaybackPosition - entry.time).coerceAtLeast(0L)

                // Word state: 0 = upcoming, 0→1 = currently singing, 1 = sung
                val progress by animateFloatAsState(
                    targetValue = when {
                        !isSynced || !isActive -> if (isPastLine && isSynced) 1f else 0f
                        lineRelTime >= endRelative -> 1f
                        lineRelTime < startRelative -> 0f
                        else -> ((lineRelTime - startRelative).toFloat() / (endRelative - startRelative).coerceAtLeast(1L)).coerceIn(0f, 1f)
                    },
                    animationSpec = tween(durationMillis = 120, easing = LinearEasing),
                    label = "wordProg"
                )

                val isSung = progress >= 0.95f
                val isSinging = progress in 0.05f..0.94f
                val isUpcoming = progress < 0.05f

                // ── Pill background alpha ──
                val pillBgAlpha by animateFloatAsState(
                    targetValue = when {
                        !isSynced -> 0f
                        // On the active line
                        isActive && isSinging -> 1f      // solid white during singing
                        isActive && isSung -> 0.22f      // frosted after sung
                        isActive && isUpcoming -> 0f     // no pill before singing
                        // On past lines (already sung) — keep frosted pills
                        isPastLine && isSynced -> 0.22f
                        else -> 0f
                    },
                    animationSpec = tween(durationMillis = 180),
                    label = "pillBg"
                )

                // ── Text color ──
                val wordTextColor = when {
                    !isSynced -> Color.White
                    isActive && isSinging -> {
                        // Lerp from upcoming → active pill text
                        androidx.compose.ui.graphics.lerp(
                            start = upcomingText,
                            stop = activePillText,
                            fraction = progress.coerceIn(0f, 1f)
                        )
                    }
                    isActive && isSung -> sungPillText
                    isActive && isUpcoming -> upcomingText
                    isPastLine && isSynced -> Color.White.copy(alpha = 0.85f)
                    else -> upcomingText
                }

                // ── Word scale: subtle pop when actively singing ──
                val wordScaleTarget = when {
                    isActive && isSinging -> 1.04f
                    else -> 1f
                }
                val wordScale by animateFloatAsState(
                    targetValue = wordScaleTarget,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "wordScale"
                )

                // ── Pill background color ──
                val pillColor = if (pillBgAlpha > 0.01f) {
                    Color.White.copy(alpha = pillBgAlpha)
                } else {
                    Color.Transparent
                }

                // ── Render word ──
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            this.scaleX = wordScale
                            this.scaleY = wordScale
                            this.transformOrigin = TransformOrigin.Center
                        }
                        .clip(RoundedCornerShape(50)) // fully rounded capsule
                        .background(pillColor)
                        .padding(
                            horizontal = if (pillBgAlpha > 0.05f) 10.dp else 3.dp,
                            vertical = if (pillBgAlpha > 0.05f) 3.dp else 1.dp
                        )
                ) {
                    Text(
                        text = wordText,
                        fontSize = baseFontSize,
                        style = TextStyle(
                            color = wordTextColor,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.2.sp,
                            lineHeight = baseLineHeight,
                            textAlign = agentTextAlign
                        )
                    )
                }
            }
        }

        // ─── Romanized text ─────────────────────────────────────────────────
        if (showRomanized) {
            val romanizedText by entry.romanizedTextFlow.collectAsState()
            romanizedText?.let { romanized ->
                Text(
                    text = romanized,
                    fontSize = (baseFontSize.value * 0.55f).sp,
                    color = Color.White.copy(alpha = if (isActive) 0.80f else 0.40f),
                    textAlign = agentTextAlign,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.2.sp,
                    modifier = Modifier.padding(top = 3.dp).fillMaxWidth(),
                    lineHeight = (baseFontSize.value * 0.55f * lineSpacing.coerceAtMost(1.2f)).sp
                )
            }
        }

        // ─── Translated text ────────────────────────────────────────────────
        if (showTranslated) {
            val translatedText by entry.translatedTextFlow.collectAsState()
            translatedText?.let { translated ->
                Text(
                    text = translated,
                    fontSize = (baseFontSize.value * 0.60f).sp,
                    color = expressiveAccent.copy(alpha = if (isActive) 0.85f else 0.45f),
                    textAlign = agentTextAlign,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 3.dp).fillMaxWidth(),
                    lineHeight = (baseFontSize.value * 0.60f * lineSpacing.coerceAtMost(1.2f)).sp
                )
            }
        }
    }
}
