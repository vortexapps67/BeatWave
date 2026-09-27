/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.beatwave.music.R

@OptIn(ExperimentalTextApi::class)
val GoogleSansFontFamily = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.width(100f),
            FontVariation.Setting("ROND", 100f)
        )
    ),
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500),
            FontVariation.width(100f),
            FontVariation.Setting("ROND", 100f)
        )
    ),
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.width(100f),
            FontVariation.Setting("ROND", 100f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val SansFlexFontFamily = FontFamily(
    Font(
        resId = R.font.sans_flex,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.width(100f),
            FontVariation.Setting("ROND", 100f)
        )
    ),
    Font(
        resId = R.font.sans_flex,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500),
            FontVariation.width(100f),
            FontVariation.Setting("ROND", 100f)
        )
    ),
    Font(
        resId = R.font.sans_flex,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.width(100f),
            FontVariation.Setting("ROND", 100f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val OutfitFontFamily = FontFamily(
    Font(
        resId = R.font.outfit,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400)
        )
    ),
    Font(
        resId = R.font.outfit,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500)
        )
    ),
    Font(
        resId = R.font.outfit,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val PlusJakartaSansFontFamily = FontFamily(
    Font(
        resId = R.font.plus_jakarta_sans,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400)
        )
    ),
    Font(
        resId = R.font.plus_jakarta_sans,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500)
        )
    ),
    Font(
        resId = R.font.plus_jakarta_sans,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700)
        )
    )
)

/**
 * Shared text metrics for every role below.
 *
 * Two corrections to Compose's defaults, both about vertical placement:
 *
 *  - `includeFontPadding = false` drops the extra space Android reserves above
 *    the ascent and below the descent. It is a legacy TextView behaviour that
 *    makes a line box taller than the type actually needs, so text sits
 *    visibly high inside buttons, chips, list rows and anything centred. It is
 *    the single biggest reason stock Android text looks less settled than the
 *    same type on iOS, and it is why the existing hand-tuned line heights in
 *    [AppleTokens] never quite landed where the design put them.
 *  - [LineHeightStyle] then distributes the leading evenly and trims the half
 *    leading at the first and last line, so a multi-line block is optically
 *    centred rather than bottom-heavy.
 */
@OptIn(ExperimentalTextApi::class)
private val AppPlatformStyle = PlatformTextStyle(includeFontPadding = false)

@OptIn(ExperimentalTextApi::class)
private val AppLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

@OptIn(ExperimentalTextApi::class)
private fun appTextStyle(
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit,
) = TextStyle(
    fontFamily = fontFamily,
    fontWeight = fontWeight,
    fontSize = fontSize,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
    platformStyle = AppPlatformStyle,
    lineHeightStyle = AppLineHeightStyle,
)

/**
 * The app's type scale.
 *
 * Sizes and line heights stay on the Material 3 scale — they are a sound,
 * well-tested rhythm and the whole component library is built against them.
 * Tracking and weight do not, because M3's defaults are tuned for Roboto at
 * Google's density and read loose here:
 *
 *  - **Large type tightens.** Optical tracking should fall as size rises; at
 *    display sizes M3's near-zero tracking leaves headlines looking spaced out.
 *    Display and headline roles go negative, most at the top of the scale.
 *  - **Body loses its extra tracking.** M3 puts 0.5sp on a 16sp `bodyLarge`,
 *    which at this size reads as deliberately letter-spaced rather than
 *    neutral. Reduced to near zero; the smallest roles keep positive tracking,
 *    where it genuinely aids legibility.
 *  - **Titles gain weight.** `titleLarge` at Normal is too light to act as a
 *    heading next to this app's artwork-heavy surfaces; SemiBold gives the
 *    hierarchy something to sit on.
 *
 * Takes the user's custom-installed font (see rememberCustomFontFamily in
 * Font.kt) so every text role renders in it; falls back to the system default
 * when none is installed.
 */
@OptIn(ExperimentalTextApi::class)
fun AppTypography(fontFamily: FontFamily = FontFamily.Default) = Typography(
    displayLarge = appTextStyle(fontFamily, FontWeight.SemiBold, 57.sp, 64.sp, (-1.2).sp),
    displayMedium = appTextStyle(fontFamily, FontWeight.SemiBold, 45.sp, 52.sp, (-0.9).sp),
    displaySmall = appTextStyle(fontFamily, FontWeight.SemiBold, 36.sp, 44.sp, (-0.6).sp),
    headlineLarge = appTextStyle(fontFamily, FontWeight.SemiBold, 32.sp, 40.sp, (-0.5).sp),
    headlineMedium = appTextStyle(fontFamily, FontWeight.SemiBold, 28.sp, 36.sp, (-0.4).sp),
    headlineSmall = appTextStyle(fontFamily, FontWeight.SemiBold, 24.sp, 32.sp, (-0.3).sp),
    titleLarge = appTextStyle(fontFamily, FontWeight.SemiBold, 22.sp, 28.sp, (-0.25).sp),
    titleMedium = appTextStyle(fontFamily, FontWeight.SemiBold, 16.sp, 24.sp, (-0.1).sp),
    titleSmall = appTextStyle(fontFamily, FontWeight.Medium, 14.sp, 20.sp, 0.sp),
    bodyLarge = appTextStyle(fontFamily, FontWeight.Normal, 16.sp, 24.sp, 0.sp),
    bodyMedium = appTextStyle(fontFamily, FontWeight.Normal, 14.sp, 20.sp, 0.1.sp),
    bodySmall = appTextStyle(fontFamily, FontWeight.Normal, 12.sp, 16.sp, 0.2.sp),
    labelLarge = appTextStyle(fontFamily, FontWeight.Medium, 14.sp, 20.sp, 0.1.sp),
    labelMedium = appTextStyle(fontFamily, FontWeight.Medium, 12.sp, 16.sp, 0.4.sp),
    labelSmall = appTextStyle(fontFamily, FontWeight.Medium, 11.sp, 16.sp, 0.45.sp),
)
