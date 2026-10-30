/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.beatwave.music.R
import com.beatwave.music.constants.LyricsAnimationStyle
import com.beatwave.music.constants.LyricsAnimationStyleKey
import com.beatwave.music.ui.utils.backToMain
import com.beatwave.music.utils.appTopBarWindowInsets
import com.beatwave.music.utils.rememberEnumPreference

data class LyricsStyleOption(
    val style: LyricsAnimationStyle,
    val titleRes: Int,
    val descriptionRes: Int,
    val badge: String,
    val previewGradient: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsStylesScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val (selectedStyle, onSelectedStyleChange) = rememberEnumPreference(
        LyricsAnimationStyleKey,
        defaultValue = LyricsAnimationStyle.VIVIMUSIC_1
    )

    val styles = listOf(
        LyricsStyleOption(
            style = LyricsAnimationStyle.VIVIMUSIC_1,
            titleRes = R.string.lyrics_style_apple_fluid,
            descriptionRes = R.string.lyrics_style_apple_fluid_desc,
            badge = "Apple Music",
            previewGradient = listOf(Color(0xFFFC3D39), Color(0xFFFF2D55))
        ),
        LyricsStyleOption(
            style = LyricsAnimationStyle.METRO_LYRICS,
            titleRes = R.string.lyrics_style_metro,
            descriptionRes = R.string.lyrics_style_metro_desc,
            badge = "Metro",
            previewGradient = listOf(Color(0xFF0078D7), Color(0xFF00B4FF))
        ),
        LyricsStyleOption(
            style = LyricsAnimationStyle.GLOW,
            titleRes = R.string.lyrics_style_kinetic_neon,
            descriptionRes = R.string.lyrics_style_kinetic_neon_desc,
            badge = "Neon Pulse",
            previewGradient = listOf(Color(0xFF00F0FF), Color(0xFFFF007F))
        ),
        LyricsStyleOption(
            style = LyricsAnimationStyle.KARAOKE,
            titleRes = R.string.lyrics_style_cassette_stage,
            descriptionRes = R.string.lyrics_style_cassette_stage_desc,
            badge = "Karaoke",
            previewGradient = listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
        ),
        LyricsStyleOption(
            style = LyricsAnimationStyle.SLIDE,
            titleRes = R.string.lyrics_style_cinema_fade,
            descriptionRes = R.string.lyrics_style_cinema_fade_desc,
            badge = "Cinema",
            previewGradient = listOf(Color(0xFF8E8E93), Color(0xFF2C2C2E))
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = appTopBarWindowInsets(),
                title = { Text(stringResource(R.string.lyrics_styles_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.lyrics_styles_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }

            items(styles) { option ->
                val isSelected = selectedStyle == option.style || 
                    (option.style == LyricsAnimationStyle.VIVIMUSIC_1 && 
                     (selectedStyle == LyricsAnimationStyle.APPLE || selectedStyle == LyricsAnimationStyle.APPLE_V2 || selectedStyle == LyricsAnimationStyle.LYRICS_V2))

                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = tween(250),
                    label = "border"
                )

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSelected) 0.65f else 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, borderColor, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelectedStyleChange(option.style) }
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(option.previewGradient)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.lyrics),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.size(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stringResource(option.titleRes),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = option.badge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = stringResource(option.descriptionRes),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectedStyleChange(option.style) }
                            )
                        }

                        // Live Interactive Typography Preview Pill
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "♫ The sound of the waves in motion...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
