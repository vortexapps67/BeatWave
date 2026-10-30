/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.beatwave.music.LocalPlayerAwareWindowInsets
import com.beatwave.music.R
import com.beatwave.music.constants.AppFont
import com.beatwave.music.constants.BrandFontEnabledKey
import com.beatwave.music.constants.CustomFontEnabledKey
import com.beatwave.music.constants.CustomFontNameKey
import com.beatwave.music.constants.CustomFontPathKey
import com.beatwave.music.constants.SelectedFontKey
import com.beatwave.music.ui.component.IconButton
import com.beatwave.music.ui.component.Material3SettingsGroup
import com.beatwave.music.ui.component.Material3SettingsItem
import com.beatwave.music.ui.theme.GoogleSansFontFamily
import com.beatwave.music.ui.theme.OutfitFontFamily
import com.beatwave.music.ui.theme.PlusJakartaSansFontFamily
import com.beatwave.music.ui.theme.SansFlexFontFamily
import com.beatwave.music.ui.theme.ChillaxFontFamily
import com.beatwave.music.ui.theme.ChillaxBoldFontFamily
import com.beatwave.music.ui.theme.ChillaxLightFontFamily
import com.beatwave.music.ui.theme.BbhBartleFontFamily
import com.beatwave.music.ui.theme.MonospaceFontFamily
import com.beatwave.music.ui.theme.SerifFontFamily
import com.beatwave.music.ui.theme.CursiveFontFamily
import com.beatwave.music.ui.theme.SansSerifFontFamily
import com.beatwave.music.ui.theme.SanFranciscoFontFamily
import com.beatwave.music.ui.theme.InterFontFamily
import com.beatwave.music.ui.theme.EditorialSerifFontFamily
import com.beatwave.music.ui.theme.TechMonoFontFamily
import com.beatwave.music.ui.theme.CondensedGroteskFontFamily
import com.beatwave.music.ui.theme.CasualRoundedFontFamily
import com.beatwave.music.ui.theme.copyCustomFont
import com.beatwave.music.ui.utils.appTopBarWindowInsets
import com.beatwave.music.ui.utils.backToMain
import com.beatwave.music.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSelectionScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (selectedFont, onSelectedFontChange) = rememberPreference(
        SelectedFontKey,
        defaultValue = AppFont.SYSTEM.value
    )

    val (brandFont, onBrandFontChange) = rememberPreference(
        BrandFontEnabledKey,
        defaultValue = true
    )
    val (customFontEnabled, onCustomFontEnabledChange) = rememberPreference(
        CustomFontEnabledKey,
        defaultValue = false
    )
    val (customFontPath, onCustomFontPathChange) = rememberPreference(
        CustomFontPathKey,
        defaultValue = ""
    )
    val (customFontName, onCustomFontNameChange) = rememberPreference(
        CustomFontNameKey,
        defaultValue = ""
    )
    val context = LocalContext.current
    val customFontScope = rememberCoroutineScope()
    val customFontPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val displayName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
        } ?: uri.lastPathSegment.orEmpty()
        customFontScope.launch {
            val newPath = withContext(Dispatchers.IO) { copyCustomFont(context, uri) }
            if (newPath != null) {
                onCustomFontPathChange(newPath)
                onCustomFontNameChange(displayName)
                onCustomFontEnabledChange(true)
            }
        }
    }

    val activeFontFamily = remember(selectedFont) {
        runCatching {
            when (AppFont.fromValue(selectedFont)) {
                AppFont.SYSTEM -> FontFamily.Default
                AppFont.GOOGLE_SANS -> GoogleSansFontFamily
                AppFont.SANS_FLEX -> SansFlexFontFamily
                AppFont.OUTFIT -> OutfitFontFamily
                AppFont.PLUS_JAKARTA_SANS -> PlusJakartaSansFontFamily
                AppFont.CHILLAX -> ChillaxFontFamily
                AppFont.CHILLAX_BOLD -> ChillaxBoldFontFamily
                AppFont.CHILLAX_LIGHT -> ChillaxLightFontFamily
                AppFont.BBH_BARTLE -> BbhBartleFontFamily
                AppFont.MONOSPACE -> MonospaceFontFamily
                AppFont.SERIF -> SerifFontFamily
                AppFont.CURSIVE -> CursiveFontFamily
                AppFont.SANS_SERIF -> SansSerifFontFamily
                AppFont.SAN_FRANCISCO -> SanFranciscoFontFamily
                AppFont.INTER -> InterFontFamily
                AppFont.EDITORIAL_SERIF -> EditorialSerifFontFamily
                AppFont.TECH_MONO -> TechMonoFontFamily
                AppFont.CONDENSED_GROTESK -> CondensedGroteskFontFamily
                AppFont.CASUAL_ROUNDED -> CasualRoundedFontFamily
                else -> FontFamily.Default
            }
        }.getOrDefault(FontFamily.Default)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = appTopBarWindowInsets(),
                title = { Text(stringResource(R.string.app_font)) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_back),
                            contentDescription = null
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
                .padding(horizontal = 16.dp)
        ) {
            // Typography Preview Card
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.typography_preview).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = stringResource(R.string.preview_text_quote),
                        fontFamily = activeFontFamily,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "Expressive typeface is applied to display, headlines, and titles. Body copy and labels remain in the system font for maximum readability.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options settings group
            Material3SettingsGroup(
                title = stringResource(R.string.font_selection),
                items = listOf(
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.SYSTEM.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = stringResource(R.string.font_system),
                                fontFamily = FontFamily.Default
                            )
                        },
                        description = {
                            Text(
                                text = stringResource(R.string.font_system_desc),
                                fontFamily = FontFamily.Default
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.SYSTEM.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.GOOGLE_SANS.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = stringResource(R.string.font_google_sans),
                                fontFamily = GoogleSansFontFamily
                            )
                        },
                        description = {
                            Text(
                                text = stringResource(R.string.font_google_sans_desc),
                                fontFamily = GoogleSansFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.GOOGLE_SANS.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.SANS_FLEX.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = stringResource(R.string.font_sans_flex),
                                fontFamily = SansFlexFontFamily
                            )
                        },
                        description = {
                            Text(
                                text = stringResource(R.string.font_sans_flex_desc),
                                fontFamily = SansFlexFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.SANS_FLEX.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.OUTFIT.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = stringResource(R.string.font_outfit),
                                fontFamily = OutfitFontFamily
                            )
                        },
                        description = {
                            Text(
                                text = stringResource(R.string.font_outfit_desc),
                                fontFamily = OutfitFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.OUTFIT.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.PLUS_JAKARTA_SANS.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = stringResource(R.string.font_plus_jakarta_sans),
                                fontFamily = PlusJakartaSansFontFamily
                            )
                        },
                        description = {
                            Text(
                                text = stringResource(R.string.font_plus_jakarta_sans_desc),
                                fontFamily = PlusJakartaSansFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.PLUS_JAKARTA_SANS.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.CHILLAX.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = "Chillax Geometric",
                                fontFamily = ChillaxFontFamily
                            )
                        },
                        description = {
                            Text(
                                text = "Clean geometric neo-grotesque with balanced curves",
                                fontFamily = ChillaxFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.CHILLAX.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.CHILLAX_BOLD.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = "Chillax Bold Headline",
                                fontFamily = ChillaxBoldFontFamily
                            )
                        },
                        description = {
                            Text(
                                text = "Punchy heavyweight modern geometric sans-serif",
                                fontFamily = ChillaxBoldFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.CHILLAX_BOLD.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.CHILLAX_LIGHT.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = "Chillax Light Elegance",
                                fontFamily = ChillaxLightFontFamily
                            )
                        },
                        description = {
                            Text(
                                text = "Airy delicate geometric letterforms",
                                fontFamily = ChillaxLightFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.CHILLAX_LIGHT.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.BBH_BARTLE.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = "BBH Bartle Retro",
                                fontFamily = BbhBartleFontFamily
                            )
                        },
                        description = {
                            Text(
                                text = "Bold vintage vinyl & retro acoustic headline display",
                                fontFamily = BbhBartleFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.BBH_BARTLE.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.MONOSPACE.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = "Cyber Monospace",
                                fontFamily = MonospaceFontFamily
                            )
                        },
                        description = {
                            Text(
                                text = "Clean terminal code font with fixed character rhythm",
                                fontFamily = MonospaceFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.MONOSPACE.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.SERIF.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = "Classic Editorial Serif",
                                fontFamily = SerifFontFamily
                            )
                        },
                        description = {
                            Text(
                                text = "Elegant literary editorial typeface with refined contrast",
                                fontFamily = SerifFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.SERIF.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.CURSIVE.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = "Acoustic Cursive",
                                fontFamily = CursiveFontFamily
                            )
                        },
                        description = {
                            Text(
                                text = "Smooth handwritten signature script for organic music vibes",
                                fontFamily = CursiveFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.CURSIVE.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.SANS_SERIF.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = "Clean Sans-Serif",
                                fontFamily = SansSerifFontFamily
                            )
                        },
                        description = {
                            Text(
                                text = "Pure neutral neo-grotesque for maximum clarity",
                                fontFamily = SansSerifFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.SANS_SERIF.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.SAN_FRANCISCO.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = "San Francisco (Cupertino)",
                                fontFamily = SanFranciscoFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        description = {
                            Text(
                                text = "Apple-inspired balanced neo-grotesque with tight optical tracking",
                                fontFamily = SanFranciscoFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.SAN_FRANCISCO.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.INTER.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = "Inter Display",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        description = {
                            Text(
                                text = "Modern precision geometric typeface crafted for high-density screens",
                                fontFamily = InterFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.INTER.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.EDITORIAL_SERIF.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = "Editorial Luxury Serif",
                                fontFamily = EditorialSerifFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        description = {
                            Text(
                                text = "High-fashion classical luxury serif with expressive character",
                                fontFamily = EditorialSerifFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.EDITORIAL_SERIF.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.TECH_MONO.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = "Tech Code Mono",
                                fontFamily = TechMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        description = {
                            Text(
                                text = "Developer studio monospace with sharp geometric glyphs",
                                fontFamily = TechMonoFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.TECH_MONO.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.CONDENSED_GROTESK.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = "Condensed Grotesk",
                                fontFamily = CondensedGroteskFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        description = {
                            Text(
                                text = "Punchy cinematic poster display font for commanding headlines",
                                fontFamily = CondensedGroteskFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.CONDENSED_GROTESK.value) }
                    ),
                    Material3SettingsItem(
                        leadingContent = {
                            RadioButton(
                                selected = selectedFont == AppFont.CASUAL_ROUNDED.value,
                                onClick = null
                            )
                        },
                        title = {
                            Text(
                                text = "Casual Rounded",
                                fontFamily = CasualRoundedFontFamily
                            )
                        },
                        description = {
                            Text(
                                text = "Warm, relaxed organic letterforms with inviting curves",
                                fontFamily = CasualRoundedFontFamily
                            )
                        },
                        onClick = { onSelectedFontChange(AppFont.CASUAL_ROUNDED.value) }
                    )
                )
            )
            Spacer(modifier = Modifier.height(27.dp))

            Material3SettingsGroup(
                title = stringResource(R.string.font_additional),
                items = listOf(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.palette),
                        title = { Text(stringResource(R.string.brand_font)) },
                        description = { Text(stringResource(R.string.brand_font_desc)) },
                        trailingContent = {
                            Switch(
                                checked = brandFont,
                                onCheckedChange = onBrandFontChange,
                                thumbContent = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (brandFont) R.drawable.check else R.drawable.close
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize)
                                    )
                                }
                            )
                        },
                        onClick = { onBrandFontChange(!brandFont) }
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.palette),
                        title = { Text(stringResource(R.string.custom_font)) },
                        description = {
                            Text(
                                if (customFontPath.isEmpty()) stringResource(R.string.custom_font_none)
                                else customFontName.ifEmpty { stringResource(R.string.custom_font_set) }
                            )
                        },
                        trailingContent = {
                            if (customFontPath.isNotEmpty()) {
                                Switch(
                                    checked = customFontEnabled,
                                    onCheckedChange = onCustomFontEnabledChange,
                                    thumbContent = {
                                        Icon(
                                            painter = painterResource(
                                                id = if (customFontEnabled) R.drawable.check else R.drawable.close
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize)
                                        )
                                    }
                                )
                            }
                        },
                        onClick = { customFontPickerLauncher.launch(arrayOf("*/*")) }
                    )
                )
            )
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
