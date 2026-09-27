/**
 * BeatWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.beatwave.music.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Depth cues for content tiles — artwork in the home feed and library grids.
 *
 * Deliberately NOT [Modifier.liquidGlass]. That treatment costs a backdrop
 * capture plus a RenderEffect chain *per surface*, which is affordable for the
 * handful of chrome surfaces using it (nav bar, mini player, sheets) and
 * ruinous for a scrolling grid holding dozens of tiles at once. Everything here
 * is plain draw work against the tile's own bounds: no capture, no
 * RenderEffect, nothing sampled from behind the tile.
 *
 * What sells the depth instead:
 *  - a soft drop shadow, so the tile sits above the background rather than
 *    being a flat patch of it;
 *  - a specular rim, brightest at the top-left and gone by the bottom-right,
 *    reading as a lit glass edge;
 *  - an inner bottom scrim, darkening the lower edge so the rim has something
 *    to catch against.
 *
 * Both outlines come from the caller's own [shape], so the treatment follows
 * whatever corner radius the tile was clipped to, and both are built once in
 * [drawWithCache] rather than per frame.
 */
fun Modifier.glassTileDepth(
    shape: Shape,
    elevation: Dp = 8.dp,
    rimAlpha: Float = 0.26f,
    scrimAlpha: Float = 0.18f,
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        // Explicit alpha rather than the default: an opaque black shadow over
        // the near-black surface (#121212) is invisible, which left the tile
        // reading as flat on exactly the theme most people run.
        ambientColor = Color.Black.copy(alpha = 0.55f),
        spotColor = Color.Black.copy(alpha = 0.65f),
    )
    .drawWithCache {
        val strokeWidth = 1.dp.toPx()
        val fullOutline = shape.createOutline(size, layoutDirection, this)

        // Inset by the stroke so the rim lands inside the tile's clip instead
        // of being shaved in half by it.
        val insetOutline = shape.createOutline(
            Size(
                (size.width - strokeWidth).coerceAtLeast(0f),
                (size.height - strokeWidth).coerceAtLeast(0f),
            ),
            layoutDirection,
            this,
        )

        val rim = Brush.linearGradient(
            colorStops = arrayOf(
                0f to Color.White.copy(alpha = rimAlpha),
                0.45f to Color.White.copy(alpha = rimAlpha * 0.22f),
                1f to Color.Transparent,
            ),
            start = Offset.Zero,
            end = Offset(size.width, size.height),
        )

        val scrim = Brush.verticalGradient(
            colorStops = arrayOf(
                0f to Color.Transparent,
                0.72f to Color.Transparent,
                1f to Color.Black.copy(alpha = scrimAlpha),
            ),
        )

        onDrawWithContent {
            drawContent()
            paintOutline(fullOutline, scrim)
            translate(strokeWidth / 2f, strokeWidth / 2f) {
                paintOutline(insetOutline, rim, Stroke(strokeWidth))
            }
        }
    }

/**
 * Paints [outline] with [brush].
 *
 * Hand-rolled on top of the three [DrawScope] primitives rather than calling
 * the stdlib `drawOutline` helper, whose package has moved between Compose
 * versions; `drawRect`/`drawRoundRect`/`drawPath` are members of [DrawScope]
 * itself and need no import to resolve.
 */
private fun DrawScope.paintOutline(
    outline: Outline,
    brush: Brush,
    style: DrawStyle = Fill,
) {
    when (outline) {
        is Outline.Rectangle -> drawRect(
            brush = brush,
            topLeft = Offset(outline.rect.left, outline.rect.top),
            size = Size(outline.rect.width, outline.rect.height),
            style = style,
        )

        is Outline.Rounded -> {
            val rr = outline.roundRect
            drawRoundRect(
                brush = brush,
                topLeft = Offset(rr.left, rr.top),
                size = Size(rr.width, rr.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    rr.topLeftCornerRadius.x,
                    rr.topLeftCornerRadius.y,
                ),
                style = style,
            )
        }

        is Outline.Generic -> drawPath(path = outline.path, brush = brush, style = style)
    }
}
