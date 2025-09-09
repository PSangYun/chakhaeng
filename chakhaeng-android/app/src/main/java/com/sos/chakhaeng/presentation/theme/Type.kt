package com.sos.chakhaeng.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sos.chakhaeng.R

@Composable
fun chakhaengDefaultFont(): FontFamily {
    return if (LocalInspectionMode.current) {
        FontFamily.Default
    } else {
        FontFamily(Font(R.font.pretendard_variable))
    }
}

@Composable
fun chakhaengTypography(): Typography {
    val ff = chakhaengDefaultFont()

    return Typography(
        // Display (선택 사용)
        displayLarge = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 48.sp, lineHeight = 56.sp, letterSpacing = 0.5.sp
        ),
        displayMedium = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 44.sp, lineHeight = 52.sp, letterSpacing = 0.5.sp
        ),
        displaySmall = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 42.sp, lineHeight = 50.sp, letterSpacing = 0.5.sp
        ),

        // Headline
        headlineLarge = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 40.sp, lineHeight = 48.sp, letterSpacing = 0.5.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = 0.5.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.5.sp
        ),

        // Title
        titleLarge = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = 0.5.sp
        ),
        titleMedium = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = 0.5.sp
        ),
        titleSmall = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.5.sp
        ),

        // Body
        bodyLarge = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 18.sp, lineHeight = 26.sp, letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp
        ),
        bodySmall = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp
        ),

        // Label
        labelLarge = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp
        ),
        labelMedium = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Medium,
            fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = ff, fontWeight = FontWeight.Normal,
            fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp
        )
    )
}

// Default Material 3 typography values
val baseline = Typography()

