/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.component

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.beatwave.music.R
import com.beatwave.music.constants.HomeCardCornerRadiusOverrideKey
import com.beatwave.music.constants.HomeHeroCardHeightOverrideKey
import com.beatwave.music.ui.component.shapes.ContinuousRoundedRectangle
import com.beatwave.music.ui.theme.AppleTokens
import com.beatwave.music.ui.theme.rememberArtworkTint
import com.beatwave.music.ui.utils.bounceClick
import com.beatwave.music.utils.rememberPreference

/**
 * The "star of the day" card that opens Home.
 *
 * Elevated with BeatWave's Liquid Glass aesthetics:
 * - Ambient gradient accent glow sampling dominant artwork colors
 * - Dynamic specular border gradient reflecting album tones
 * - Fluid glass badge pill with refined typography
 * - High-contrast clean typography hierarchy
 * - Refined Play and Shuffle buttons with frosted liquid glass backdrops
 */
@Composable
fun HomeHeroCard(
    title: String,
    subtitle: String,
    thumbnailUrl: String?,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String = "QUICK PICK",
) {
    val (heightOverride) = rememberPreference(HomeHeroCardHeightOverrideKey, 0)
    val (cornerOverride) = rememberPreference(HomeCardCornerRadiusOverrideKey, 0)
    val cornerRadius = if (cornerOverride > 0) cornerOverride.dp else AppleTokens.CardCornerLarge
    val cardShape = ContinuousRoundedRectangle(cornerRadius)

    val artworkColors = rememberArtworkTint(thumbnailUrl)
    val dominantColor = artworkColors.firstOrNull() ?: MaterialTheme.colorScheme.primary
    val secondaryColor = artworkColors.getOrNull(1) ?: MaterialTheme.colorScheme.tertiary

    val borderBrush = remember(dominantColor, secondaryColor) {
        Brush.sweepGradient(
            listOf(
                dominantColor.copy(alpha = 0.60f),
                Color.White.copy(alpha = 0.50f),
                secondaryColor.copy(alpha = 0.40f),
                Color.White.copy(alpha = 0.15f),
                dominantColor.copy(alpha = 0.60f),
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (heightOverride > 0) Modifier.height(heightOverride.dp) else Modifier.aspectRatio(4f / 3f)
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Subtle ambient radiant glow behind the card sampling artwork colors
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .graphicsLayer { alpha = 0.45f }
                    .blur(32.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                dominantColor.copy(alpha = 0.70f),
                                secondaryColor.copy(alpha = 0.35f),
                                Color.Transparent,
                            )
                        ),
                        shape = cardShape,
                    )
            )
        }

        // Main Hero Card surface
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(cardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .bounceClick(onClick = onClick),
        ) {
            // Bound the decode to the card's real on-screen pixels and skip the crossfade
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val context = LocalContext.current
                val density = LocalDensity.current
                val request = remember(thumbnailUrl, maxWidth, maxHeight, context, density) {
                    with(density) {
                        ImageRequest.Builder(context)
                            .data(thumbnailUrl)
                            .size(maxWidth.roundToPx(), maxHeight.roundToPx())
                            .crossfade(false)
                            .build()
                    }
                }
                AsyncImage(
                    model = request,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                // Frosted band over the lower portion: blurred cover with alpha ramp
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        0.3f to Color.Transparent,
                                        0.55f to Color.White.copy(alpha = 0.65f),
                                        0.75f to Color.White,
                                    ),
                                    blendMode = BlendMode.DstIn,
                                )
                            },
                    ) {
                        AsyncImage(
                            model = request,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(HeroFrostBlurRadius),
                        )
                    }
                }
            }

            // Darkening gradient pass ensuring legibility on bright covers
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.30f to Color.Transparent,
                            0.70f to Color.Black.copy(alpha = 0.40f),
                            1.0f to Color.Black.copy(alpha = 0.72f),
                        )
                    ),
            )

            // Artwork color wash over lower frosted band to infuse track's own vibrancy
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.45f to Color.Transparent,
                            0.75f to dominantColor.copy(alpha = 0.20f),
                            1.0f to Color.Transparent,
                        )
                    ),
            )

            // Specular top rim sheen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.White.copy(alpha = 0.14f),
                            0.12f to Color.Transparent,
                        )
                    ),
            )

            // Liquid glass specular border outline sampling artwork colors
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.2.dp,
                        brush = borderBrush,
                        shape = cardShape,
                    )
            )

            // Top Fluid Badge Pill with glass effect
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(AppleTokens.Gutter)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.42f))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.12f),
                            )
                        ),
                        shape = CircleShape,
                    )
                    .padding(horizontal = 11.dp, vertical = 5.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.sparks),
                        contentDescription = null,
                        tint = dominantColor,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = badgeText.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.09.em,
                        color = Color.White.copy(alpha = 0.95f),
                    )
                }
            }

            // Bottom Content Column with enhanced typography hierarchy
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(AppleTokens.Gutter),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        letterSpacing = (-0.02).em,
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                    ),
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Refined Play and Shuffle buttons with Liquid Glass backdrop
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HeroPill(
                        iconRes = R.drawable.play,
                        label = stringResource(R.string.play),
                        isPrimary = true,
                        accentColor = dominantColor,
                        onClick = onPlay,
                    )
                    HeroPill(
                        iconRes = R.drawable.shuffle,
                        label = stringResource(R.string.shuffle),
                        isPrimary = false,
                        accentColor = dominantColor,
                        onClick = onShuffle,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroPill(
    iconRes: Int,
    label: String,
    isPrimary: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
) {
    val shape = CircleShape
    val contentColor = Color.White

    val bgModifier = if (isPrimary) {
        Modifier
            .background(
                Brush.horizontalGradient(
                    listOf(
                        accentColor,
                        accentColor.copy(alpha = 0.88f),
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.60f),
                        Color.White.copy(alpha = 0.20f),
                    )
                ),
                shape = shape,
            )
    } else {
        Modifier
            .background(Color.White.copy(alpha = 0.16f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.12f),
                    )
                ),
                shape = shape,
            )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .clip(shape)
            .then(bgModifier)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            maxLines = 1,
        )
    }
}

/** Radius of the hero's frosted lower band. Matches [SpotlightCard]'s reflection. */
private val HeroFrostBlurRadius = 24.dp
