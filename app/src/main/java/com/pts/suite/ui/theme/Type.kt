package com.pts.suite.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// --- Custom Sketch Typography Tokens ---
val SketchFontPrimary = FontFamily.SansSerif
val SketchFontMono = FontFamily.Monospace
val SketchFontHandwritten = FontFamily.Cursive

val SketchLogoStyle = TextStyle(
    fontFamily = SketchFontHandwritten,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    color = Graphite100,
    letterSpacing = 0.5.sp
)

val SketchHeaderStyle = TextStyle(
    fontFamily = SketchFontPrimary,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    color = Graphite100
)

val SketchBadgeStyle = TextStyle(
    fontFamily = SketchFontMono,
    fontWeight = FontWeight.Bold,
    fontSize = 10.sp,
    color = EmeraldGreen,
    letterSpacing = 0.5.sp
)

val SketchMonoStyle = TextStyle(
    fontFamily = SketchFontMono,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    color = Graphite200
)

val SketchCardNumberStyle = TextStyle(
    fontFamily = SketchFontMono,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    color = Graphite100,
    letterSpacing = 2.sp
)

// --- Material 3 Design Hierarchy ---
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = SketchFontPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        color = Graphite100
    ),
    displayMedium = TextStyle(
        fontFamily = SketchFontPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        color = Graphite100
    ),
    displaySmall = TextStyle(
        fontFamily = SketchFontPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = Graphite100
    ),
    headlineLarge = TextStyle(
        fontFamily = SketchFontPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = Graphite100
    ),
    headlineMedium = TextStyle(
        fontFamily = SketchFontPrimary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = Graphite100
    ),
    headlineSmall = TextStyle(
        fontFamily = SketchFontPrimary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = Graphite100
    ),
    titleLarge = TextStyle(
        fontFamily = SketchFontPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = Graphite100
    ),
    titleMedium = TextStyle(
        fontFamily = SketchFontPrimary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = Graphite100
    ),
    titleSmall = TextStyle(
        fontFamily = SketchFontPrimary,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = Graphite100
    ),
    bodyLarge = TextStyle(
        fontFamily = SketchFontPrimary,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = Graphite200
    ),
    bodyMedium = TextStyle(
        fontFamily = SketchFontPrimary,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = Graphite200
    ),
    bodySmall = TextStyle(
        fontFamily = SketchFontPrimary,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = Graphite400
    ),
    labelLarge = TextStyle(
        fontFamily = SketchFontPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Graphite100
    ),
    labelMedium = TextStyle(
        fontFamily = SketchFontMono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = Graphite300
    ),
    labelSmall = TextStyle(
        fontFamily = SketchFontMono,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        color = Graphite300
    )
)
