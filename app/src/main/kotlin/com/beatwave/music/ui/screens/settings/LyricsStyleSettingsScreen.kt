/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.beatwave.music.LocalPlayerAwareWindowInsets
import com.beatwave.music.R
import com.beatwave.music.constants.LyricsAnimationStyle
import com.beatwave.music.constants.LyricsAnimationStyleKey
import com.beatwave.music.constants.LyricsCustomAccentColorKey
import com.beatwave.music.constants.LyricsFontStyle
import com.beatwave.music.constants.LyricsFontStyleKey
import com.beatwave.music.ui.component.ColorPickerDialog
import com.beatwave.music.ui.component.DefaultDialog
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import com.beatwave.music.constants.LyricsFisheyeEffectKey
import com.beatwave.music.constants.LyricsGlowEffectKey
import com.beatwave.music.constants.LyricsLineSpacingKey
import com.beatwave.music.constants.LyricsStandardBlurKey
import com.beatwave.music.constants.LyricsTextPositionKey
import com.beatwave.music.constants.LyricsTextSizeKey
import com.beatwave.music.constants.OneTapFullscreenLyricsKey
import com.beatwave.music.lyrics.LyricsEntry
import com.beatwave.music.lyrics.WordTimestamp
import com.beatwave.music.ui.component.CinematicMinimalLyricsLine
import com.beatwave.music.ui.component.FisheyeStickerLyricsLine
import com.beatwave.music.ui.component.GradientSweepLyricsLine
import com.beatwave.music.ui.component.IconButton
import com.beatwave.music.ui.component.Material3SettingsGroup
import com.beatwave.music.ui.component.Material3SettingsItem
import com.beatwave.music.ui.component.NeonPulseLyricsLine
import com.beatwave.music.ui.component.RetroCassetteLyricsLine
import com.beatwave.music.ui.component.SpotifyBounceLyricsLine
import com.beatwave.music.ui.theme.AppleTokens
import com.beatwave.music.ui.utils.appTopBarWindowInsets
import com.beatwave.music.ui.utils.backToMain
import com.beatwave.music.utils.rememberEnumPreference
import com.beatwave.music.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsStyleSettingsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (lyricsAnimationStyle, onLyricsAnimationStyleChange) = rememberEnumPreference(
        LyricsAnimationStyleKey,
        defaultValue = LyricsAnimationStyle.APPLE
    )
    val (lyricsFisheyeEffect, onLyricsFisheyeEffectChange) = rememberPreference(
        LyricsFisheyeEffectKey,
        defaultValue = true
    )
    val (lyricsTextSize, onLyricsTextSizeChange) = rememberPreference(
        LyricsTextSizeKey,
        defaultValue = 30f
    )
    val (lyricsLineSpacing, onLyricsLineSpacingChange) = rememberPreference(
        LyricsLineSpacingKey,
        defaultValue = 1.3f
    )
    val (lyricsGlowEffect, onLyricsGlowEffectChange) = rememberPreference(
        LyricsGlowEffectKey,
        defaultValue = true
    )
    val (lyricsStandardBlur, onLyricsStandardBlurChange) = rememberPreference(
        LyricsStandardBlurKey,
        defaultValue = false
    )
    val (oneTapFullscreenLyrics, onOneTapFullscreenLyricsChange) = rememberPreference(
        OneTapFullscreenLyricsKey,
        defaultValue = false
    )
    val (lyricsCustomAccentColorInt, onLyricsCustomAccentColorChange) = rememberPreference(
        LyricsCustomAccentColorKey,
        defaultValue = 0
    )
    val (lyricsFontStyle, onLyricsFontStyleChange) = rememberEnumPreference(
        LyricsFontStyleKey,
        defaultValue = LyricsFontStyle.SYSTEM_DEFAULT
    )
    var showColorPaletteDialog by rememberSaveable { mutableStateOf(false) }
    var showCustomColorPicker by rememberSaveable { mutableStateOf(false) }
    var showFontDialog by rememberSaveable { mutableStateOf(false) }

    val resolvedLyricsFontFamily = remember(lyricsFontStyle) {
        when (lyricsFontStyle) {
            LyricsFontStyle.SYSTEM_DEFAULT -> FontFamily.Default
            LyricsFontStyle.GOOGLE_SANS -> com.beatwave.music.ui.theme.GoogleSansFontFamily
            LyricsFontStyle.CHILLAX -> com.beatwave.music.ui.theme.ChillaxFontFamily
            LyricsFontStyle.OUTFIT -> com.beatwave.music.ui.theme.OutfitFontFamily
            LyricsFontStyle.PLUS_JAKARTA -> com.beatwave.music.ui.theme.PlusJakartaSansFontFamily
            LyricsFontStyle.LUXURY_SERIF -> FontFamily.Serif
            LyricsFontStyle.RETRO_MONO -> FontFamily.Monospace
            LyricsFontStyle.HANDWRITTEN -> FontFamily.Cursive
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = appTopBarWindowInsets(),
                title = {
                    Text(
                        text = "Lyrics Styles & Effects",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .windowInsetsPadding(
                    LocalPlayerAwareWindowInsets.current.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Live Interactive Preview Card
            LiveLyricsInteractivePreview(
                selectedStyle = lyricsAnimationStyle,
                fisheyeEnabled = lyricsFisheyeEffect,
                lyricsTextSize = lyricsTextSize,
                lyricsLineSpacing = lyricsLineSpacing,
                customAccentColorInt = lyricsCustomAccentColorInt,
                lyricsFontFamily = resolvedLyricsFontFamily
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Fisheye Effect Hero Toggle Card
            FisheyeEffectMasterCard(
                enabled = lyricsFisheyeEffect,
                onToggle = { onLyricsFisheyeEffectChange(!lyricsFisheyeEffect) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5 New Featured Styles Header
            Text(
                text = "FEATURED DYNAMIC STYLES",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            val newStyles = listOf(
                LyricsStyleInfo(
                    style = LyricsAnimationStyle.FISHEYE,
                    title = "Fisheye 3D Barrel",
                    tagline = "Cylindrical rotating drum with sticker word badges & contextual emojis",
                    badge = "HOT",
                    accentColor = Color(0xFFFF2D55)
                ),
                LyricsStyleInfo(
                    style = LyricsAnimationStyle.NEON_PULSE,
                    title = "Neon Pulse (Cyberpunk)",
                    tagline = "Electric cyan-magenta glowing neon tubes with rhythmic heartbeat glow",
                    badge = "NEW 6.2",
                    accentColor = Color(0xFF00F5FF)
                ),
                LyricsStyleInfo(
                    style = LyricsAnimationStyle.SPOTIFY_BOUNCE,
                    title = "Spotify Bounce",
                    tagline = "Kinetic pop typography with dynamic spring bounce and frosted pill backdrop",
                    badge = "NEW 6.2",
                    accentColor = Color(0xFF1DB954)
                ),
                LyricsStyleInfo(
                    style = LyricsAnimationStyle.CINEMATIC_MINIMAL,
                    title = "Cinematic Minimal",
                    tagline = "High-fashion luxury serif, ultra-wide tracking and warm champagne glow",
                    badge = "NEW 6.2",
                    accentColor = Color(0xFFFFDF9E)
                ),
                LyricsStyleInfo(
                    style = LyricsAnimationStyle.GRADIENT_SWEEP,
                    title = "Gradient Sweep",
                    tagline = "Continuous flowing chromatic aurora wave sweeping across singing lines",
                    badge = "NEW 6.2",
                    accentColor = Color(0xFFFF007F)
                ),
                LyricsStyleInfo(
                    style = LyricsAnimationStyle.RETRO_CASSETTE,
                    title = "Retro Cassette 80s",
                    tagline = "Lo-Fi synthwave typewriter with authentic RGB split chromatic aberration",
                    badge = "NEW 6.2",
                    accentColor = Color(0xFFFFB000)
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppleTokens.CardCorner))
                    .background(AppleTokens.Card)
            ) {
                newStyles.forEachIndexed { index, styleInfo ->
                    val activeColor = if (lyricsCustomAccentColorInt != 0 && lyricsAnimationStyle == styleInfo.style) {
                        Color(lyricsCustomAccentColorInt)
                    } else styleInfo.accentColor
                    LyricsStyleSelectableRow(
                        info = styleInfo,
                        isSelected = lyricsAnimationStyle == styleInfo.style,
                        onSelect = { onLyricsAnimationStyleChange(styleInfo.style) },
                        onColorClick = {
                            if (lyricsAnimationStyle != styleInfo.style) {
                                onLyricsAnimationStyleChange(styleInfo.style)
                            }
                            showColorPaletteDialog = true
                        },
                        activeColor = activeColor
                    )
                    if (index < newStyles.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Classic & Extended Styles Header
            Text(
                text = "CLASSIC & FLUID STYLES",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            val classicStyles = listOf(
                LyricsStyleInfo(
                    style = LyricsAnimationStyle.APPLE,
                    title = "Apple Music Classic",
                    tagline = "Smooth word-by-word opacity and gentle focus falloff",
                    accentColor = Color(0xFFFA2D48)
                ),
                LyricsStyleInfo(
                    style = LyricsAnimationStyle.APPLE_V2,
                    title = "Apple Music Dynamic",
                    tagline = "Letter-by-letter fluid highlight with karaoke flow",
                    accentColor = Color(0xFFFA2D48)
                ),
                LyricsStyleInfo(
                    style = LyricsAnimationStyle.LYRICS_V2,
                    title = "Fluid Dynamics (V2)",
                    tagline = "Liquid smooth word highlight and dynamic lead-in",
                    accentColor = Color(0xFF007AFF)
                ),
                LyricsStyleInfo(
                    style = LyricsAnimationStyle.METRO_LYRICS,
                    title = "Metro Lyrics",
                    tagline = "Bold typography inspired by Windows Phone flat design",
                    accentColor = Color(0xFFAF52DE)
                ),
                LyricsStyleInfo(
                    style = LyricsAnimationStyle.VIVIMUSIC_1,
                    title = "ViMusic Classic",
                    tagline = "Standard smooth word transition",
                    accentColor = Color(0xFF34C759)
                ),
                LyricsStyleInfo(
                    style = LyricsAnimationStyle.GLOW,
                    title = "Glowing Accent",
                    tagline = "Pulsing radiant glow on the active line",
                    accentColor = Color(0xFFFF9500)
                ),
                LyricsStyleInfo(
                    style = LyricsAnimationStyle.KARAOKE,
                    title = "Karaoke Fill",
                    tagline = "High-energy word fill animation",
                    accentColor = Color(0xFFFF2D55)
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppleTokens.CardCorner))
                    .background(AppleTokens.Card)
            ) {
                classicStyles.forEachIndexed { index, styleInfo ->
                    val activeColor = if (lyricsCustomAccentColorInt != 0 && lyricsAnimationStyle == styleInfo.style) {
                        Color(lyricsCustomAccentColorInt)
                    } else styleInfo.accentColor
                    LyricsStyleSelectableRow(
                        info = styleInfo,
                        isSelected = lyricsAnimationStyle == styleInfo.style,
                        onSelect = { onLyricsAnimationStyleChange(styleInfo.style) },
                        onColorClick = {
                            if (lyricsAnimationStyle != styleInfo.style) {
                                onLyricsAnimationStyleChange(styleInfo.style)
                            }
                            showColorPaletteDialog = true
                        },
                        activeColor = activeColor
                    )
                    if (index < classicStyles.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Typography & Visual Tuning Group
            Material3SettingsGroup(
                title = "TYPOGRAPHY & TUNING",
                items = listOf(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.edit),
                        title = { Text("Lyrics Font Size (${lyricsTextSize.toInt()} sp)") },
                        description = {
                            Slider(
                                value = lyricsTextSize,
                                onValueChange = onLyricsTextSizeChange,
                                valueRange = 20f..46f,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            )
                        }
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.sliders),
                        title = { Text("Line Spacing (${String.format("%.1f", lyricsLineSpacing)}x)") },
                        description = {
                            Slider(
                                value = lyricsLineSpacing,
                                onValueChange = onLyricsLineSpacingChange,
                                valueRange = 1.0f..1.8f,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            )
                        }
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.gradient),
                        title = { Text("Depth-of-field Progressive Blur") },
                        description = { Text("Applies cinematic focus blur to distant inactive lines") },
                        trailingContent = {
                            Switch(
                                checked = lyricsStandardBlur,
                                onCheckedChange = onLyricsStandardBlurChange
                            )
                        },
                        onClick = { onLyricsStandardBlurChange(!lyricsStandardBlur) }
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.fullscreen),
                        title = { Text("One-Tap Fullscreen Lyrics") },
                        description = { Text("Open fullscreen lyrics directly with a single tap on the player") },
                        trailingContent = {
                            Switch(
                                checked = oneTapFullscreenLyrics,
                                onCheckedChange = onOneTapFullscreenLyricsChange
                            )
                        },
                        onClick = { onOneTapFullscreenLyricsChange(!oneTapFullscreenLyrics) }
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.edit),
                        title = { Text("Lyrics Font Style") },
                        description = {
                            Text(
                                when (lyricsFontStyle) {
                                    LyricsFontStyle.SYSTEM_DEFAULT -> "System Default"
                                    LyricsFontStyle.GOOGLE_SANS -> "Google Sans Flex"
                                    LyricsFontStyle.CHILLAX -> "Chillax Modern"
                                    LyricsFontStyle.OUTFIT -> "Outfit Geometric"
                                    LyricsFontStyle.PLUS_JAKARTA -> "Plus Jakarta Sans"
                                    LyricsFontStyle.LUXURY_SERIF -> "Luxury Serif"
                                    LyricsFontStyle.RETRO_MONO -> "Retro Monospace"
                                    LyricsFontStyle.HANDWRITTEN -> "Cursive Script"
                                }
                            )
                        },
                        onClick = { showFontDialog = true }
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.palette),
                        title = { Text("Lyrics Accent Color") },
                        description = {
                            Text(
                                if (lyricsCustomAccentColorInt == 0) "Default (Style Accent)"
                                else "#%06X (Custom Color)".format(0xFFFFFF and lyricsCustomAccentColorInt)
                            )
                        },
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (lyricsCustomAccentColorInt == 0) Color.White.copy(alpha = 0.5f) else Color(lyricsCustomAccentColorInt))
                                    .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                            )
                        },
                        onClick = { showColorPaletteDialog = true }
                    )
                )
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showFontDialog) {
            DefaultDialog(
                onDismiss = { showFontDialog = false },
                buttons = {
                    androidx.compose.material3.TextButton(onClick = { showFontDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Choose Lyrics Font Style",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LyricsFontStyle.entries.forEach { style ->
                        val isSelected = lyricsFontStyle == style
                        val fontName = when (style) {
                            LyricsFontStyle.SYSTEM_DEFAULT -> "System Default"
                            LyricsFontStyle.GOOGLE_SANS -> "Google Sans Flex"
                            LyricsFontStyle.CHILLAX -> "Chillax Modern"
                            LyricsFontStyle.OUTFIT -> "Outfit Geometric"
                            LyricsFontStyle.PLUS_JAKARTA -> "Plus Jakarta Sans"
                            LyricsFontStyle.LUXURY_SERIF -> "Luxury Serif"
                            LyricsFontStyle.RETRO_MONO -> "Retro Monospace"
                            LyricsFontStyle.HANDWRITTEN -> "Cursive Script"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onLyricsFontStyleChange(style)
                                    showFontDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = fontName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        if (showColorPaletteDialog) {
            val paletteColors = listOf(
                Pair("Style Default", Color.Transparent),
                Pair("Cyber Cyan", Color(0xFF00F5FF)),
                Pair("Neon Magenta", Color(0xFFFF007F)),
                Pair("Electric Emerald", Color(0xFF00FF88)),
                Pair("Cyber Gold", Color(0xFFFFD600)),
                Pair("Sunset Amber", Color(0xFFFF6D00)),
                Pair("Vivid Crimson", Color(0xFFFF1744)),
                Pair("Royal Amethyst", Color(0xFFA855F7)),
                Pair("Glacial Blue", Color(0xFF38BDF8)),
                Pair("Tokyo Monolith", Color(0xFFFFFFFF))
            )

            DefaultDialog(
                onDismiss = { showColorPaletteDialog = false },
                buttons = {
                    androidx.compose.material3.TextButton(onClick = { showColorPaletteDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    androidx.compose.material3.TextButton(onClick = {
                        showColorPaletteDialog = false
                        showCustomColorPicker = true
                    }) {
                        Text("Custom Color...", fontWeight = FontWeight.Bold)
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Customize Lyrics Color",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Choose an accent tone or pick your own custom shade",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    paletteColors.forEach { (name, col) ->
                        val isDefault = col == Color.Transparent
                        val isSelected = if (isDefault) lyricsCustomAccentColorInt == 0 else lyricsCustomAccentColorInt == col.toArgb()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (isDefault) {
                                        onLyricsCustomAccentColorChange(0)
                                    } else {
                                        onLyricsCustomAccentColorChange(col.toArgb())
                                    }
                                    showColorPaletteDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isDefault) Color.White.copy(alpha = 0.4f) else col)
                                    .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    painter = painterResource(R.drawable.check),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showCustomColorPicker) {
            val initial = if (lyricsCustomAccentColorInt != 0) Color(lyricsCustomAccentColorInt) else Color(0xFF00F5FF)
            ColorPickerDialog(
                initialColor = initial,
                onDismiss = { showCustomColorPicker = false },
                title = "Custom Lyrics Color",
                onConfirm = { col ->
                    onLyricsCustomAccentColorChange(col.toArgb())
                    showCustomColorPicker = false
                }
            )
        }
    }
}

private data class LyricsStyleInfo(
    val style: LyricsAnimationStyle,
    val title: String,
    val tagline: String,
    val badge: String? = null,
    val accentColor: Color
)

@Composable
private fun LyricsStyleSelectableRow(
    info: LyricsStyleInfo,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onColorClick: (() -> Unit)? = null,
    activeColor: Color = info.accentColor
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (info.badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(info.accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = info.badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = info.accentColor
                        )
                    }
                }
            }
            Text(
                text = info.tagline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (onColorClick != null) {
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(activeColor)
                    .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                    .clickable(onClick = onColorClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.palette),
                    contentDescription = "Change Color",
                    tint = if (activeColor.luminance() > 0.5f) Color.Black else Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun FisheyeEffectMasterCard(
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(AppleTokens.CardCorner),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else AppleTokens.Card
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = if (enabled) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(AppleTokens.CardCorner)
            )
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.spatial_tracking_apple),
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Fisheye 3D Lens Effect",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Dynamic spherical magnification that scales and curves lyrics as they flow past the center focal point.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = enabled,
                onCheckedChange = { onToggle() },
                thumbContent = {
                    Icon(
                        painter = painterResource(if (enabled) R.drawable.check else R.drawable.close),
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                }
            )
        }
    }
}

/**
 * Real-time simulated lyrics playback preview container
 */
@Composable
private fun LiveLyricsInteractivePreview(
    selectedStyle: LyricsAnimationStyle,
    fisheyeEnabled: Boolean,
    lyricsTextSize: Float,
    lyricsLineSpacing: Float,
    customAccentColorInt: Int = 0,
    lyricsFontFamily: FontFamily = FontFamily.Default
) {
    val density = LocalDensity.current.density
    val infiniteTransition = rememberInfiniteTransition(label = "previewClock")
    val playbackSec by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "playbackSeconds"
    )

    val currentPlaybackMs = (playbackSec * 1000).toLong()

    val sampleLines = remember {
        listOf(
            LyricsEntry(
                time = 0L,
                text = "Neon lights across the midnight sky",
                words = listOf(
                    WordTimestamp(text = "Neon", startTime = 0.0, endTime = 0.7),
                    WordTimestamp(text = "lights", startTime = 0.7, endTime = 1.4),
                    WordTimestamp(text = "across", startTime = 1.4, endTime = 2.0),
                    WordTimestamp(text = "the", startTime = 2.0, endTime = 2.5),
                    WordTimestamp(text = "midnight", startTime = 2.5, endTime = 3.2),
                    WordTimestamp(text = "sky", startTime = 3.2, endTime = 4.0)
                )
            ),
            LyricsEntry(
                time = 4000L,
                text = "Rhythm pulsing in the heart of sound",
                words = listOf(
                    WordTimestamp(text = "Rhythm", startTime = 4.0, endTime = 4.8),
                    WordTimestamp(text = "pulsing", startTime = 4.8, endTime = 5.8),
                    WordTimestamp(text = "in", startTime = 5.8, endTime = 6.4),
                    WordTimestamp(text = "the", startTime = 6.4, endTime = 6.8),
                    WordTimestamp(text = "heart", startTime = 6.8, endTime = 7.5),
                    WordTimestamp(text = "of", startTime = 7.5, endTime = 8.0),
                    WordTimestamp(text = "sound", startTime = 8.0, endTime = 9.0)
                )
            ),
            LyricsEntry(
                time = 9000L,
                text = "Every beat echoes into tomorrow",
                words = listOf(
                    WordTimestamp(text = "Every", startTime = 9.0, endTime = 9.7),
                    WordTimestamp(text = "beat", startTime = 9.7, endTime = 10.4),
                    WordTimestamp(text = "echoes", startTime = 10.4, endTime = 11.2),
                    WordTimestamp(text = "into", startTime = 11.2, endTime = 11.6),
                    WordTimestamp(text = "tomorrow", startTime = 11.6, endTime = 12.0)
                )
            )
        )
    }

    val isFisheyeActivePreview = fisheyeEnabled || selectedStyle == LyricsAnimationStyle.FISHEYE
    val previewLines = remember(selectedStyle, fisheyeEnabled) {
        if (isFisheyeActivePreview) {
            com.beatwave.music.lyrics.LyricsUtils.chunkLinesForFisheye(sampleLines)
        } else {
            sampleLines
        }
    }

    val activeIndex = remember(previewLines, currentPlaybackMs) {
        com.beatwave.music.lyrics.LyricsUtils.findCurrentLineIndex(previewLines, currentPlaybackMs).coerceAtLeast(0)
    }

    Card(
        shape = RoundedCornerShape(AppleTokens.CardCornerLarge),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D0D12)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(AppleTokens.CardCornerLarge))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34C759))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE INTERACTIVE PREVIEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "${playbackSec.toInt()}s / 12s",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                previewLines.forEachIndexed { index, entry ->
                    val isActive = index == activeIndex
                    val lineDistance = kotlin.math.abs(index - activeIndex).toFloat()

                    val delta = (index - activeIndex).toFloat()
                    val absDistance = kotlin.math.abs(delta)

                    val isFisheyeActive = fisheyeEnabled || selectedStyle == LyricsAnimationStyle.FISHEYE

                    val fisheyeScaleX = if (isFisheyeActive) {
                        (0.68f + 0.68f / (1f + 0.40f * absDistance * absDistance)).coerceIn(0.65f, 1.45f)
                    } else 1f

                    val fisheyeScaleY = if (isFisheyeActive) {
                        (0.70f + 0.65f / (1f + 0.40f * absDistance * absDistance)).coerceIn(0.68f, 1.40f)
                    } else 1f

                    val fisheyeRotationX = if (isFisheyeActive) {
                        (-delta * 32f).coerceIn(-68f, 68f)
                    } else 0f

                    val fisheyeTranslationY = if (isFisheyeActive) {
                        val sign = if (delta < 0) 1f else -1f
                        (sign * (absDistance * absDistance * 6.0f)).coerceIn(-48f, 48f)
                    } else 0f

                    val fisheyeAlpha = if (isFisheyeActive) {
                        (1f - (absDistance * 0.16f)).coerceIn(0.25f, 1f)
                    } else 1f

                    val fisheyePivotY = if (isFisheyeActive) {
                        when {
                            delta < -0.1f -> 1.0f
                            delta > 0.1f -> 0.0f
                            else -> 0.5f
                        }
                    } else 0.5f

                    val previewAccent = if (customAccentColorInt != 0) Color(customAccentColorInt) else when (selectedStyle) {
                        LyricsAnimationStyle.NEON_PULSE -> Color(0xFF00F5FF)
                        LyricsAnimationStyle.SPOTIFY_BOUNCE -> Color(0xFF1DB954)
                        LyricsAnimationStyle.CINEMATIC_MINIMAL -> Color(0xFFFFDF9E)
                        LyricsAnimationStyle.GRADIENT_SWEEP -> Color(0xFFFF007F)
                        LyricsAnimationStyle.RETRO_CASSETTE -> Color(0xFFFFB000)
                        LyricsAnimationStyle.FISHEYE -> Color.White
                        else -> Color(0xFF00F5FF)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                this.cameraDistance = if (isFisheyeActive) 3.8f * density else 16f * density
                                this.transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, fisheyePivotY)
                                this.rotationX = fisheyeRotationX
                                this.translationY = fisheyeTranslationY
                                this.alpha = if (isActive) 1f else 0.45f * fisheyeAlpha
                                this.scaleX = (if (isActive) 1.05f else 1f) * fisheyeScaleX
                                this.scaleY = (if (isActive) 1.05f else 1f) * fisheyeScaleY
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (selectedStyle) {
                            LyricsAnimationStyle.FISHEYE -> {
                                FisheyeStickerLyricsLine(
                                    entry = entry,
                                    isActive = isActive,
                                    effectivePlaybackPosition = currentPlaybackMs,
                                    expressiveAccent = previewAccent,
                                    baseFontSize = 18f,
                                    lineHeight = 24f,
                                    agentTextAlign = TextAlign.Center,
                                    agentAlignment = Alignment.CenterHorizontally,
                                    distanceFromCenter = delta,
                                    lyricsFontFamily = lyricsFontFamily
                                )
                            }
                            LyricsAnimationStyle.NEON_PULSE -> {
                                NeonPulseLyricsLine(
                                    entry = entry,
                                    isActive = isActive,
                                    effectivePlaybackPosition = currentPlaybackMs,
                                    expressiveAccent = previewAccent,
                                    baseFontSize = 20f,
                                    lineHeight = 26f,
                                    agentTextAlign = TextAlign.Center,
                                    lyricsFontFamily = lyricsFontFamily
                                )
                            }
                            LyricsAnimationStyle.SPOTIFY_BOUNCE -> {
                                SpotifyBounceLyricsLine(
                                    entry = entry,
                                    isActive = isActive,
                                    effectivePlaybackPosition = currentPlaybackMs,
                                    expressiveAccent = previewAccent,
                                    baseFontSize = 20f,
                                    lineHeight = 26f,
                                    agentTextAlign = TextAlign.Center,
                                    lyricsFontFamily = lyricsFontFamily
                                )
                            }
                            LyricsAnimationStyle.CINEMATIC_MINIMAL -> {
                                CinematicMinimalLyricsLine(
                                    entry = entry,
                                    isActive = isActive,
                                    effectivePlaybackPosition = currentPlaybackMs,
                                    baseFontSize = 20f,
                                    lineHeight = 26f,
                                    agentTextAlign = TextAlign.Center,
                                    expressiveAccent = previewAccent,
                                    lyricsFontFamily = lyricsFontFamily
                                )
                            }
                            LyricsAnimationStyle.GRADIENT_SWEEP -> {
                                GradientSweepLyricsLine(
                                    entry = entry,
                                    isActive = isActive,
                                    effectivePlaybackPosition = currentPlaybackMs,
                                    baseFontSize = 20f,
                                    lineHeight = 26f,
                                    agentTextAlign = TextAlign.Center,
                                    expressiveAccent = previewAccent,
                                    lyricsFontFamily = lyricsFontFamily
                                )
                            }
                            LyricsAnimationStyle.RETRO_CASSETTE -> {
                                RetroCassetteLyricsLine(
                                    entry = entry,
                                    isActive = isActive,
                                    effectivePlaybackPosition = currentPlaybackMs,
                                    baseFontSize = 20f,
                                    lineHeight = 26f,
                                    agentTextAlign = TextAlign.Center,
                                    expressiveAccent = previewAccent,
                                    lyricsFontFamily = lyricsFontFamily
                                )
                            }
                            else -> {
                                // Default / Apple / Classic fallback preview
                                Text(
                                    text = entry.text,
                                    fontSize = 20.sp,
                                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isActive) Color.White else Color.White.copy(alpha = 0.4f),
                                    lineHeight = 26.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
