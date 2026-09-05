package com.pts.suite.ui.theme

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Custom Compose Shape replicating CSS 8-value asymmetric border-radius:
 * `rx_tl rx_tr rx_br rx_bl / ry_tl ry_tr ry_br ry_bl`
 *
 * Provides organic, hand-drawn sketch borders matching the web PTS design system.
 */
class AsymmetricSketchShape(
    val tlX: Dp,
    val trX: Dp,
    val brX: Dp,
    val blX: Dp,
    val tlY: Dp,
    val trY: Dp,
    val brY: Dp,
    val blY: Dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) {
            return Outline.Rectangle(Rect(0f, 0f, 0f, 0f))
        }

        with(density) {
            var rawTlX = tlX.toPx()
            var rawTrX = trX.toPx()
            var rawBrX = brX.toPx()
            var rawBlX = blX.toPx()

            var rawTlY = tlY.toPx()
            var rawTrY = trY.toPx()
            var rawBrY = brY.toPx()
            var rawBlY = blY.toPx()

            // Scale radii proportionally if they exceed container dimensions
            val topSum = rawTlX + rawTrX
            val bottomSum = rawBlX + rawBrX
            val leftSum = rawTlY + rawBlY
            val rightSum = rawTrY + rawBrY

            val scaleTop = if (topSum > w && topSum > 0f) w / topSum else 1f
            val scaleBottom = if (bottomSum > w && bottomSum > 0f) w / bottomSum else 1f
            val scaleLeft = if (leftSum > h && leftSum > 0f) h / leftSum else 1f
            val scaleRight = if (rightSum > h && rightSum > 0f) h / rightSum else 1f

            val rTlX = min(rawTlX * scaleTop, w / 2f)
            val rTrX = min(rawTrX * scaleTop, w / 2f)
            val rBrX = min(rawBrX * scaleBottom, w / 2f)
            val rBlX = min(rawBlX * scaleBottom, w / 2f)

            val rTlY = min(rawTlY * scaleLeft, h / 2f)
            val rTrY = min(rawTrY * scaleRight, h / 2f)
            val rBrY = min(rawBrY * scaleRight, h / 2f)
            val rBlY = min(rawBlY * scaleLeft, h / 2f)

            val path = Path().apply {
                moveTo(0f, rTlY)
                // Top-Left corner
                if (rTlX > 0f && rTlY > 0f) {
                    arcTo(Rect(0f, 0f, 2f * rTlX, 2f * rTlY), 180f, 90f, false)
                } else {
                    lineTo(0f, 0f)
                    lineTo(rTlX, 0f)
                }

                // Top Edge
                lineTo(w - rTrX, 0f)

                // Top-Right corner
                if (rTrX > 0f && rTrY > 0f) {
                    arcTo(Rect(w - 2f * rTrX, 0f, w, 2f * rTrY), 270f, 90f, false)
                } else {
                    lineTo(w, 0f)
                    lineTo(w, rTrY)
                }

                // Right Edge
                lineTo(w, h - rBrY)

                // Bottom-Right corner
                if (rBrX > 0f && rBrY > 0f) {
                    arcTo(Rect(w - 2f * rBrX, h - 2f * rBrY, w, h), 0f, 90f, false)
                } else {
                    lineTo(w, h)
                    lineTo(w - rBrX, h)
                }

                // Bottom Edge
                lineTo(rBlX, h)

                // Bottom-Left corner
                if (rBlX > 0f && rBlY > 0f) {
                    arcTo(Rect(0f, h - 2f * rBlY, 2f * rBlX, h), 90f, 90f, false)
                } else {
                    lineTo(0f, h)
                    lineTo(0f, h - rBlY)
                }

                close()
            }

            return Outline.Generic(path)
        }
    }
}

/**
 * Standard Primary Sketch Shape:
 * Replicates CSS: `255px 15px 225px 15px / 15px 225px 15px 255px`
 * Scaled to typical Android DP design tokens (25.5dp 2.5dp 22.5dp 2.5dp / 2.5dp 22.5dp 2.5dp 25.5dp)
 */
val SketchShape: Shape = AsymmetricSketchShape(
    tlX = 25.5.dp, trX = 3.5.dp, brX = 22.5.dp, blX = 3.5.dp,
    tlY = 3.5.dp, trY = 22.5.dp, brY = 3.5.dp, blY = 25.5.dp
)

/**
 * Alternate Sketch Shape (Dashed Contour Overlay / Inset):
 * Replicates CSS: `15px 225px 15px 255px / 255px 15px 225px 15px`
 */
val SketchShapeAlt: Shape = AsymmetricSketchShape(
    tlX = 3.5.dp, trX = 22.5.dp, brX = 3.5.dp, blX = 25.5.dp,
    tlY = 25.5.dp, trY = 3.5.dp, brY = 22.5.dp, blY = 3.5.dp
)

/**
 * Small Sketch Shape (Badges, Buttons, Chips, Input fields):
 * Replicates CSS: `120px 8px 110px 8px / 8px 110px 8px 120px`
 */
val SketchShapeSm: Shape = AsymmetricSketchShape(
    tlX = 14.dp, trX = 3.dp, brX = 12.dp, blX = 3.dp,
    tlY = 3.dp, trY = 12.dp, brY = 3.dp, blY = 14.dp
)

/**
 * Parametric Sketch Shape builder.
 */
fun SketchCornerShape(radius: Dp = 16.dp, asymmetry: Float = 0.85f): Shape {
    val major = radius * (1f + (1f - asymmetry))
    val minor = radius * asymmetry
    return AsymmetricSketchShape(
        tlX = major, trX = minor, brX = major, blX = minor,
        tlY = minor, trY = major, brY = minor, blY = major
    )
}
