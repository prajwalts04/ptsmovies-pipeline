package com.pts.suite.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Chunky hand-drawn Sketch border modifier.
 */
fun Modifier.sketchBorder(
    width: Dp = 2.5.dp,
    color: Color = SketchBorder,
    shape: Shape = SketchShape
): Modifier = this.border(
    width = width,
    color = color,
    shape = shape
)

/**
 * Convenience modifier to create a styled sketch card with clip, background, and sketch border.
 */
fun Modifier.sketchCard(
    shape: Shape = SketchShape,
    backgroundColor: Color = DarkSurface,
    borderColor: Color = SketchBorder,
    borderWidth: Dp = 2.dp
): Modifier = this
    .clip(shape)
    .background(backgroundColor)
    .sketchBorder(width = borderWidth, color = borderColor, shape = shape)

/**
 * Global Pitch Black canvas background with subtle orthogonal grid lines and radial vignette shading.
 */
fun Modifier.sketchCanvasBackground(
    gridSize: Dp = 28.dp,
    gridColor: Color = Color(0x08FFFFFF),
    vignetteColor: Color = Color(0xE6000000)
): Modifier = this.drawBehind {
    // 1. Solid Pitch Black base
    drawRect(color = PitchBlack)

    // 2. Orthogonal Grid Lines
    val step = gridSize.toPx()
    if (step > 0f) {
        var x = 0f
        while (x <= size.width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += step
        }

        var y = 0f
        while (y <= size.height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += step
        }
    }

    // 3. Radial Vignette Shading
    val maxDim = max(size.width, size.height)
    if (maxDim > 0f) {
        val vignetteBrush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                vignetteColor.copy(alpha = 0.4f),
                vignetteColor
            ),
            center = center,
            radius = maxDim * 0.75f
        )
        drawRect(brush = vignetteBrush)
    }
}
