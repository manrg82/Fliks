package com.fliks.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fliks.R

val SplineSans = FontFamily(
    Font(R.font.splinesans_regular, FontWeight.Normal),
    Font(R.font.splinesans_medium, FontWeight.Medium),
    Font(R.font.splinesans_semibold, FontWeight.SemiBold),
    Font(R.font.splinesans_bold, FontWeight.Bold),
    Font(R.font.splinesans_light, FontWeight.Light)
)
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 48.sp,
        lineHeight = 52.sp, // Aprox 1.1
        letterSpacing = (-0.02).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 32.sp,
        lineHeight = 38.sp, // Aprox 1.2
        letterSpacing = (-0.01).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 18.sp,
        lineHeight = 28.sp // Aprox 1.6
    ),
    labelMedium = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 14.sp,
        letterSpacing = 0.05.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Light, // 500
        fontSize = 12.sp,
        lineHeight = 17.sp // Aprox 1.4
    )
)