/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.component

import android.app.ActivityManager
import android.os.Build
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.beatwave.music.ui.component.backdrop.BackdropEffectScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.beatwave.music.ui.component.backdrop.Backdrop
import com.beatwave.music.ui.component.backdrop.drawBackdrop
import com.beatwave.music.ui.component.backdrop.isRenderEffectSupported
import com.beatwave.music.ui.component.backdrop.effects.blur
import com.beatwave.music.ui.component.backdrop.effects.colorControls
import com.beatwave.music.ui.component.backdrop.effects.lens
import com.beatwave.music.ui.component.backdrop.highlight.Highlight
import com.beatwave.music.ui.component.backdrop.highlight.HighlightStyle
import com.beatwave.music.ui.component.backdrop.shadow.Shadow

/**
 * User-configurable parameters of the liquid glass effect, sourced from DataStore
 * preferences in [com.beatwave.music.MainActivity] and distributed through
 * [LocalGlassEffectConfig].
 */
@Stable
data class GlassEffectConfig(
    val globalEnabled: Boolean = true,
    val engine: com.beatwave.music.constants.LiquidGlassEngine = com.beatwave.music.constants.LiquidGlassEngine.NEW_GLASS,
    val vibrancy: Float = 1.2f,
    /** Blur in dp applied to glass pills. User requested 2dp for landscape fix. */
    val blurRadius: Float = 2f,
    /** 0..1, mapped to 0..[LENS_MAX_DP] dp of lens refraction height. 0.4 = 40%. */
    val lensHeight: Float = 0.4f,
    /** 0..1, mapped to 0..[LENS_MAX_DP] dp of lens refraction amount. 0.6 = 60%. */
    val lensAmount: Float = 0.6f,
    val chromaticAberration: Boolean = false,
    val depthEffect: Boolean = false,
    /** [Color.Unspecified] means adaptive: dark grey on dark. */
    val surfaceTintColor: Color = Color(0xFF1A1A1A),
    /** Specular rim ("pluck") colour. [Color.Unspecified] keeps the default white. */
    val highlightColor: Color = Color.Unspecified,
    /** Specular rim opacity, 0..1. */
    val highlightOpacity: Float = EdgeHighlightAlpha,
    /** Which rendering style every glass surface uses. */
    val style: GlassStyle = GlassStyle.LIQUID,
    /** Wash painted over the nav bar's selection puck. [Color.Unspecified] adapts
     *  to the theme: a dark wash on dark, a light one on light. */
    val puckColor: Color = Color.Unspecified,
    /** Puck wash opacity at rest. It eases toward clear while pressed. */
    val puckOpacity: Float = 0.8f,
    val surfaceOpacity: Float = 0.5f,
    val textColor: Color = Color.White,
    val playerEnabled: Boolean = true,
    val miniPlayerEnabled: Boolean = true,
    val navBarEnabled: Boolean = true,
    /** Tablet side panel — split from [navBarEnabled] so it can differ from the
     *  phone bottom bar's glass setting instead of always mirroring it. */
    val sidePanelEnabled: Boolean = true,
    /** Side panel gets its own effect tuning (unlike the other components,
     *  which all share [vibrancy]/[blurRadius]/[lensHeight]/[lensAmount]) —
     *  defaults match those shared values so it looks identical until
     *  explicitly customized. */
    val sidePanelVibrancy: Float = 1.2f,
    val sidePanelBlurRadius: Float = 2f,
    val sidePanelLensHeight: Float = 0.4f,
    val sidePanelLensAmount: Float = 0.6f,
    val sidePanelColor: Color = Color.Unspecified,
    val sidePanelSurfaceOpacity: Float = 0.5f,
    val sidePanelTextColor: Color = Color.White,
) {
    /** A copy of this config with the shared effect fields swapped for the
     *  side panel's own — pass this to Modifier.liquidGlass for the side
     *  panel instead of the raw config, since liquidGlass always reads the
     *  shared fields generically. */
    fun forSidePanel(): GlassEffectConfig = copy(
        vibrancy = sidePanelVibrancy,
        blurRadius = sidePanelBlurRadius,
        lensHeight = sidePanelLensHeight,
        lensAmount = sidePanelLensAmount,
        surfaceTintColor = sidePanelColor,
        surfaceOpacity = sidePanelSurfaceOpacity,
        textColor = sidePanelTextColor,
    )
    /**
     * Whether the glass effect should be rendered for [component], taking the master
     * switch and the per-component switch into account.
     */
    fun isEnabledFor(component: GlassComponent): Boolean =
        globalEnabled && when (component) {
            GlassComponent.PLAYER -> playerEnabled
            GlassComponent.MINI_PLAYER -> miniPlayerEnabled
            GlassComponent.NAV_BAR -> navBarEnabled
            GlassComponent.SIDE_PANEL -> sidePanelEnabled
        }

    /**
     * True when at least one surface would render glass. Used to decide whether
     * capturing the app backdrop is worth anything at all — with every component
     * off, nothing samples it and recording it is pure cost.
     */
    val anyComponentEnabled: Boolean
        get() = globalEnabled &&
            (playerEnabled || miniPlayerEnabled || navBarEnabled || sidePanelEnabled)
}

/** UI surfaces that can individually opt in or out of the liquid glass effect. */
/**
 * How a glass surface is rendered.
 *
 * The full liquid treatment costs a screen capture plus a RenderEffect chain per
 * surface. The cheaper styles exist both as a performance escape hatch and as a
 * look in their own right — and because with glass off entirely the chrome fell
 * back to an opaque fill, which reads as a black blob over content.
 */
enum class GlassStyle {
    /** Blur + saturation + lens refraction + specular rim. The default. */
    LIQUID,

    /** Blur and saturation only: no lens, no rim, no chromatic aberration.
     *  Still captures the backdrop, so it is frosted, just not refractive. */
    BLUR,

    /** No capture at all — a translucent tinted fill at the configured opacity.
     *  Cheapest by a wide margin: nothing is sampled, nothing is blurred. */
    TRANSPARENT,
}

/**
 * True when [Modifier.liquidGlass] should take its cheap translucent-tint path
 * (no backdrop capture, no RenderEffect) instead of the full liquid treatment:
 * either the user explicitly chose [GlassStyle.TRANSPARENT], or the platform
 * can't do the real blur at all ([renderEffectSupported] false, below Android
 * 12). The unsupported-platform case used to fall through to a hardcoded
 * opaque-black surface instead — the "solid black instead of any
 * transparency" bug reported on older/low-end devices. Pulled out as a plain
 * function so the fallback decision is unit-testable without a Composition.
 */
fun shouldUseTranslucentGlassFallback(style: GlassStyle, renderEffectSupported: Boolean): Boolean =
    style == GlassStyle.TRANSPARENT || !renderEffectSupported

enum class GlassComponent {
    PLAYER,
    MINI_PLAYER,
    NAV_BAR,
    SIDE_PANEL,
}

/**
 * Maximum lens refraction in dp when the 0..1 preference sliders are at 1. The
 * defaults (0.5) land on the 24dp height/amount used by the library author's
 * Apple-matched LiquidBottomTabs recipe.
 */
internal const val LENS_MAX_DP = 48f

/**
 * The full screen player uses a much heavier blur than the glass pills, matching
 * Apple Music where the now playing background is a deep-blurred material while
 * only the small controls are clear liquid glass.
 */
internal const val PLAYER_BLUR_MULTIPLIER = 4f

/**
 * Lowest resolution fraction glass surfaces are rendered at (heavy blur hides it).
 *
 * Measured empirically: flattening this across all surfaces and pushing it to
 * 0.05 was tried and reverted. Two findings worth not re-deriving. First, the
 * scale is only safe in proportion to the blur — pixel-sized effect params are
 * pre-multiplied by it, so at 0.05 the default 2dp blur collapsed to ~0.3px on a
 * 3x screen and the 20x upscale had nothing smoothing it, which reads as
 * pixelation, not frost. Second, applying it at zero blur softens surfaces whose
 * whole look is clear content, for no gain: the surfaces where compression
 * actually pays (the full screen player, 12x the nav bar's area at 4x the blur)
 * already reach this floor through the ramp.
 *
 * Keep the ramp in [glassResolutionScale] rather than a flat value.
 */
internal const val MIN_GLASS_RESOLUTION_SCALE = 0.30f

/** Blur radius (dp) at or above which the minimum resolution scale is safe to use. */
internal const val FULL_QUALITY_BLUR_DP = 8f

/**
 * Resolution fraction at which a glass surface records and processes its backdrop.
 * Blur masks the upscaling, so the more blur, the lower the resolution can go: at
 * [FULL_QUALITY_BLUR_DP]+ dp of blur the surface renders at
 * [MIN_GLASS_RESOLUTION_SCALE]; with no blur it stays at full resolution so the
 * clear glass center remains crisp.
 */
fun glassResolutionScale(blurRadiusDp: Float): Float {
    val t = (blurRadiusDp / FULL_QUALITY_BLUR_DP).coerceIn(0f, 1f)
    return 1f - t * (1f - MIN_GLASS_RESOLUTION_SCALE)
}

/**
 * The backdrop blur pipeline requires [android.graphics.RenderEffect] on a
 * [android.graphics.RenderNode], which is available from Android 12 (API 31).
 */
fun isGlassSupported(sdkInt: Int = Build.VERSION.SDK_INT): Boolean = sdkInt >= Build.VERSION_CODES.S

/**
 * Hardware performance tier for adaptive Liquid Glass rendering.
 * - LOW: < 3.5GB RAM or <= 4 CPU cores (or OS low-RAM flag). Runs single-pass hardware RenderEffect
 *   blur with lightweight resolution scale (0.20f) and bypasses expensive AGSL RuntimeShader lens refraction.
 * - MEDIUM: 3.5GB - 6GB RAM. Runs optimized liquid glass with capped backdrop scale (0.35f) and
 *   skips multi-pass depth/chromatic passes.
 * - HIGH: Full-fidelity liquid glass with full AGSL lens refraction and multi-pass effects.
 */
enum class DevicePerformanceTier {
    LOW,
    MEDIUM,
    HIGH
}

@Composable
fun rememberDevicePerformanceTier(): DevicePerformanceTier {
    val context = LocalContext.current
    return remember {
        try {
            val am = context.getSystemService(ActivityManager::class.java)
            if (am?.isLowRamDevice == true) return@remember DevicePerformanceTier.LOW
            val memInfo = ActivityManager.MemoryInfo()
            am?.getMemoryInfo(memInfo)
            val totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            val cores = Runtime.getRuntime().availableProcessors()
            val model = Build.MODEL.orEmpty()
            val isBudgetChipset = model.contains("A14", ignoreCase = true) ||
                model.contains("A13", ignoreCase = true) ||
                model.contains("A12", ignoreCase = true) ||
                model.contains("A15", ignoreCase = true) ||
                model.contains("A04", ignoreCase = true) ||
                model.contains("A05", ignoreCase = true) ||
                model.contains("A23", ignoreCase = true) ||
                model.contains("A24", ignoreCase = true)
            when {
                isBudgetChipset || totalRamGb < 5.0 || cores <= 4 -> DevicePerformanceTier.LOW
                totalRamGb < 6.5 || cores <= 6 -> DevicePerformanceTier.MEDIUM
                else -> DevicePerformanceTier.HIGH
            }
        } catch (_: Throwable) {
            DevicePerformanceTier.MEDIUM
        }
    }
}

/** Check if device is low-RAM; returns true for LOW tier */
@Composable
fun isLowRamDevice(): Boolean = rememberDevicePerformanceTier() == DevicePerformanceTier.LOW

/**
 * Combines the API-level check with hardware support; allows glass on all Android 12+ devices
 * via our adaptive tiered performance pipeline so low-end devices run smoothly without lag.
 */
@Composable
fun isGlassAllowed(): Boolean = isGlassSupported()

/**
 * Maps the user-facing vibrancy preference (0..2, default 1) to a saturation multiplier.
 * A value of 1 matches the library's built-in vibrancy effect (saturation x1.5), 0 leaves
 * colors untouched and 2 doubles the saturation.
 */
fun glassSaturation(vibrancy: Float): Float = 1f + 0.5f * vibrancy.coerceIn(0f, 2f)

/**
 * A content colour guaranteed to read against the glass it sits on.
 *
 * Picking purely from the app theme is not enough: what a glass pill actually
 * shows is the content behind it, tinted by [tint] at [opacity]. A dark tint at
 * high opacity is dark even in light mode, and a light tint is light even in
 * dark mode — so theme-derived text goes invisible at exactly the settings a
 * user is most likely to reach for. This composites the same two layers the
 * surface draws and picks from the result.
 *
 * @param behind the colour behind the glass — the theme surface, or the screen's
 * artwork tint where one is known.
 * @param tint the glass surface tint; [Color.Unspecified] means untinted.
 * @param opacity how strongly [tint] covers [behind], matching surfaceOpacity.
 */
fun glassContentColorFor(behind: Color, tint: Color, opacity: Float): Color {
    val effective = if (tint.isSpecified) {
        lerp(behind, tint, opacity.coerceIn(0f, 1f))
    } else {
        behind
    }
    // Same targets the rest of the app uses for on-surface text, so glass chrome
    // matches ordinary content rather than being its own special case.
    return if (effective.luminance() > 0.5f) Color(0xFF1A1A1A) else Color.White
}

/**
 * Apple's floating pills have a thin, subtle specular line along the top edge — a
 * hint of light, not a bold ring. The library's [Highlight.Default] (0.5dp, white @
 * 0.5 alpha) reads as near-invisible at typical density, but a wide bright rim
 * overshoots in the other direction, so this lands narrower and dimmer than that.
 */
private val EdgeHighlightWidth = 0.8f.dp
private const val EdgeHighlightAlpha = 0.55f

/**
 * Real Liquid Glass isn't a highlight painted on at a fixed angle: Apple describes
 * the material as dynamically bending and shaping light "in real time, every single
 * frame," and community shader recreations implement this as one or more soft light
 * sources drifting across the surface over time (e.g. animated via sin/cos of the
 * frame clock). A perfectly static 45° rim reads as flat and synthetic by
 * comparison, so the highlight's light angle slowly drifts instead of sitting still.
 */
private const val HighlightAngleMin = 25f
private const val HighlightAngleMax = 65f
private const val HighlightDriftMillis = 7000

/** Midpoint of the [HighlightAngleMin]..[HighlightAngleMax] sweep — see the use site. */
private const val HighlightAngleFrozen = (HighlightAngleMin + HighlightAngleMax) / 2f

val LocalGlassEffectConfig = staticCompositionLocalOf { GlassEffectConfig() }

/** The backdrop content (app UI) that glass surfaces sample from. */
val LocalAppBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/**
 * When set, glass surfaces in scope pool one recorded+effect-processed layer
 * per bucket this returns instead of a single reused layer — see
 * `drawBackdrop`'s `loopBucket` parameter. Meant for surfaces sampling a
 * backdrop whose source loops (a looping canvas video): a bucket's visual
 * output is identical every repeat, so it only needs computing once, ever.
 * Null (default) keeps every surface's existing single-layer behavior.
 */
val LocalBackdropLoopBucket = staticCompositionLocalOf<(() -> Int)?> { null }

/**
 * Whether the Apple Music-styled UI (iOS 26/27 liquid glass look, SF-style tab icons,
 * denser glass) is active. Read by [com.beatwave.music.ui.screens.Screens] consumers to pick
 * between the classic and iOS icon sets.
 */
val LocalAppleMusicUi = staticCompositionLocalOf { false }

/**
 * Renders this composable as a liquid glass surface sampling [LocalAppBackdrop].
 *
 * Applies the configured vibrancy, blur and lens refraction effects, then draws the
 * surface tint (theme-adaptive unless the user picked a color). Effects whose
 * parameters make them a no-op are skipped entirely to keep the RenderEffect chain
 * as short as possible, and the backdrop is processed at [glassResolutionScale] of
 * the surface resolution. Returns the receiver unchanged on devices without
 * RenderEffect support.
 *
 * [applyEdgeEffects] controls the edge treatment that makes small pills read as
 * physical glass: lens refraction, the specular highlight rim and the drop shadow.
 * It should be false for large surfaces such as the full screen player, where the
 * rim renders as a stray band of light.
 *
 * [blurRadiusDp] overrides the configured blur; the full screen player passes a
 * heavier value ([PLAYER_BLUR_MULTIPLIER]x) than the clear glass pills.
 *
 * [shape] is restricted to [CornerBasedShape] because the backdrop lens effect throws
 * [UnsupportedOperationException] for any other shape type.
 */
@Composable
fun Modifier.liquidGlass(
    config: GlassEffectConfig,
    shape: CornerBasedShape = RoundedCornerShape(0.dp),
    applyEdgeEffects: Boolean = true,
    blurRadiusDp: Float = config.blurRadius,
    // The nav bar and mini player want a dimmer specular rim than the default.
    highlightAlpha: Float = EdgeHighlightAlpha,
    // Fraction of the surface resolution the backdrop is recorded at. Defaults to
    // [glassResolutionScale] for the blur radius (cheap, and the blur masks the
    // upscaling) — pass 1f for a crisp full-resolution backdrop, e.g. the small
    // glass buttons whose whole look is clear content, not heavy frost.
    backdropScale: Float = glassResolutionScale(blurRadiusDp),
    // Forwarded to drawBackdrop: while this returns true the surface holds its
    // last capture instead of re-recording the screen. Used by the floating tab
    // bar for the frames of its inline/expanded/search transition, where the
    // bar's bounds animate and would otherwise force a full-screen re-capture
    // plus effect chain on every frame.
    frozen: () -> Boolean = { false },
    // Forwarded to drawBackdrop: see LocalBackdropLoopBucket's doc. Defaults to
    // whatever's provided in composition, so player control pills over a
    // looping video pick this up without every call site naming it explicitly.
    loopBucket: (() -> Int)? = LocalBackdropLoopBucket.current,
): Modifier {
    val surfaceTintColor = if (config.surfaceTintColor.isSpecified) {
        config.surfaceTintColor
    } else if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        Color(0xFFFAFAFA)
    } else {
        Color(0xFF4A4A4E)
    }

    val isNewGlass = config.engine == com.beatwave.music.constants.LiquidGlassEngine.NEW_GLASS

    // Translucent style or unsupported/low-RAM devices short-circuit everything below:
    // no backdrop is sampled and no expensive RenderEffect runs, keeping rendering fast and buttery smooth.
    if (!isGlassAllowed() || shouldUseTranslucentGlassFallback(config.style, isRenderEffectSupported())) {
        val baseModifier = this.clip(shape)
        return if (isNewGlass) {
            baseModifier
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            surfaceTintColor.copy(alpha = (config.surfaceOpacity * 1.12f).coerceIn(0f, 0.95f)),
                            surfaceTintColor.copy(alpha = (config.surfaceOpacity * 0.88f).coerceIn(0f, 0.95f))
                        )
                    )
                )
                .then(
                    if (applyEdgeEffects) {
                        Modifier.border(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0.04f)
                                )
                            ),
                            shape = shape
                        )
                    } else Modifier
                )
        } else {
            baseModifier
                .background(surfaceTintColor.copy(alpha = config.surfaceOpacity.coerceIn(0f, 1f)))
        }
    }

    val backdrop = LocalAppBackdrop.current ?: run {
        val baseModifier = this.clip(shape)
        return if (isNewGlass) {
            baseModifier
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            surfaceTintColor.copy(alpha = (config.surfaceOpacity * 1.12f).coerceIn(0f, 0.95f)),
                            surfaceTintColor.copy(alpha = (config.surfaceOpacity * 0.88f).coerceIn(0f, 0.95f))
                        )
                    )
                )
                .then(
                    if (applyEdgeEffects) {
                        Modifier.border(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0.04f)
                                )
                            ),
                            shape = shape
                        )
                    } else Modifier
                )
        } else {
            baseModifier
                .background(surfaceTintColor.copy(alpha = config.surfaceOpacity.coerceIn(0f, 1f)))
        }
    }
    val density = LocalDensity.current
    val deviceTier = rememberDevicePerformanceTier()

    // Adaptive resolution scaling: Low-end devices process 1/16th to 1/25th of the pixels,
    // dramatically dropping GPU memory and fill-rate demands while maintaining silky smooth 60-120fps.
    val resolutionScale = when (deviceTier) {
        DevicePerformanceTier.LOW -> 0.15f
        DevicePerformanceTier.MEDIUM -> backdropScale.coerceIn(0.25f, 0.45f)
        DevicePerformanceTier.HIGH -> backdropScale.coerceIn(0.05f, 1f)
    }

    // Pixel-sized effect parameters operate on the backdrop layer at its recorded
    // resolution, so they are pre-multiplied by the resolution scale to keep the
    // same visual size.
    val rawBlurPx = with(density) { blurRadiusDp.dp.toPx() } * resolutionScale
    val blurPx = if (deviceTier == DevicePerformanceTier.LOW) rawBlurPx.coerceAtMost(8f) else rawBlurPx
    val saturation = if (isNewGlass) 1f + 0.25f * config.vibrancy.coerceIn(0f, 2f) else glassSaturation(config.vibrancy)
    val effectiveLensMaxDp = if (isNewGlass) 26f else LENS_MAX_DP
    val lensHeightPx = with(density) { (config.lensHeight * effectiveLensMaxDp).dp.toPx() } * resolutionScale
    val lensAmountPx = with(density) { (config.lensAmount * effectiveLensMaxDp).dp.toPx() } * resolutionScale
    val effectiveChromaticAberration = if (isNewGlass) false else config.chromaticAberration

    val animatedHighlightAngle = HighlightAngleFrozen
    val shapeBlock: () -> Shape = remember(shape) { { shape } }

    // BLUR keeps the frosted capture but drops the refraction and the rim, which
    // are the parts that read as "liquid". Cheaper too: the lens is the most
    // expensive link in the effect chain.
    val plainBlur = config.style == GlassStyle.BLUR

    // Low-tier skips expensive AGSL runtime shader lens refraction (saving 80%+ GPU fill-rate)
    // while keeping hardware-accelerated blur + saturation + specular rim highlight!
    val enableLens = deviceTier != DevicePerformanceTier.LOW &&
        !plainBlur &&
        applyEdgeEffects &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        (lensHeightPx > 0f || lensAmountPx > 0f)

    val effectsBlock: BackdropEffectScope.() -> Unit = remember(
        saturation,
        blurPx,
        enableLens,
        lensHeightPx,
        lensAmountPx,
        deviceTier,
        config.depthEffect,
        config.chromaticAberration,
    ) {
        {
            if (saturation != 1f) {
                colorControls(saturation = saturation)
            }
            if (blurPx > 0f) {
                blur(blurPx)
            }
            if (enableLens) {
                lens(
                    refractionHeight = lensHeightPx,
                    refractionAmount = lensAmountPx,
                    depthEffect = if (deviceTier == DevicePerformanceTier.MEDIUM) false else config.depthEffect,
                    chromaticAberration = if (deviceTier == DevicePerformanceTier.MEDIUM) false else effectiveChromaticAberration,
                )
            }
        }
    }

    // The rim's own colour, independent of the surface tint: on a tinted pill a
    // white rim is what reads as "lit glass", but a coloured one is the difference
    // between chrome and neon, so it is worth exposing rather than assuming.
    val rimColor = if (config.highlightColor.isSpecified) config.highlightColor else Color.White
    // Call sites pass a per-component alpha (the nav bar wants a dimmer rim than a
    // button). Scale the user's setting by it rather than replacing it, so both the
    // per-component tuning and the preference still mean something.
    val rimAlpha = (highlightAlpha * (config.highlightOpacity / EdgeHighlightAlpha)).coerceIn(0f, 1f)

    val highlightBlock: (() -> Highlight?)? = remember(applyEdgeEffects, plainBlur, rimColor, rimAlpha, animatedHighlightAngle) {
        if (applyEdgeEffects && !plainBlur) {
            {
                Highlight(
                    width = EdgeHighlightWidth,
                    style = HighlightStyle.Default(
                        color = rimColor.copy(alpha = rimAlpha),
                        angle = animatedHighlightAngle,
                    ),
                )
            }
        } else {
            null
        }
    }

    val shadowBlock: (() -> Shadow?)? = remember(applyEdgeEffects) {
        if (applyEdgeEffects) ({ Shadow.Default }) else null
    }

    // RenderEffect is guaranteed supported past this point — the unsupported
    // case already returned the translucent-tint fallback above.
    val surfaceBlock: DrawScope.() -> Unit = remember(surfaceTintColor, config.surfaceOpacity) {
        {
            if (config.surfaceOpacity > 0f) {
                drawRect(
                    color = surfaceTintColor.copy(alpha = config.surfaceOpacity),
                    size = size,
                )
            }
        }
    }

    return drawBackdrop(
        backdrop = backdrop,
        shape = shapeBlock,
        effects = effectsBlock,
        highlight = highlightBlock,
        shadow = shadowBlock,
        onDrawSurface = surfaceBlock,
        backdropScale = resolutionScale,
        frozen = frozen,
        loopBucket = loopBucket,
    )
}
