package com.lcsmilhan.songsphere.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lcsmilhan.songsphere.R

private val QuickSand = FontFamily(
    Font(R.font.quicksand_bold, FontWeight.Bold),
    Font(R.font.quicksand_semibold, FontWeight.SemiBold),
    Font(R.font.quicksand_medium, FontWeight.Medium),
    Font(R.font.quicksand_normal, FontWeight.Normal)
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = QuickSand,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = QuickSand,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = QuickSand,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = QuickSand,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = QuickSand,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = QuickSand,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)
