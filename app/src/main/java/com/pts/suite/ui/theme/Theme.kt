package com.pts.suite.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldGreen,
    secondary = Graphite200,
    tertiary = AccentBlue,
    background = PitchBlack,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceElevated,
    outline = SketchBorder,
    onPrimary = PitchBlack,
    onSecondary = PitchBlack,
    onBackground = Graphite100,
    onSurface = Graphite100,
    onSurfaceVariant = Graphite200
)

object SketchTheme {
    val colors = SketchColors
    val shapes = SketchShapes
    val typography = Typography
}

object SketchColors {
    val pitchBlack = PitchBlack
    val card = DarkSurface
    val cardHover = DarkSurfaceElevated
    val cardSelected = CardSelected
    val inputBg = InputBg
    val border = SketchBorder
    val borderActive = SketchBorderActive
    val borderWhite = SketchBorderWhite
    val green = EmeraldGreen
    val greenBright = EmeraldGreenBright
    val red = DangerRed
    val yellow = GoldenYellow
    val amber = AccentAmber
    val blue = AccentBlue
    val graphite100 = Graphite100
    val graphite200 = Graphite200
    val graphite300 = Graphite300
    val graphite400 = Graphite400
    val graphite800 = Graphite800
}

object SketchShapes {
    val primary = SketchShape
    val alternate = SketchShapeAlt
    val small = SketchShapeSm
}

@Composable
fun PTSSuiteTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = PitchBlack.toArgb()
                window.navigationBarColor = PitchBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
