package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DuolingoGreen,
    onPrimary = Color.White,
    primaryContainer = DuolingoGreenDark,
    secondary = SpeakBlue,
    onSecondary = Color.White,
    tertiary = GoldYellow,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onBackground = Color.White,
    onSurface = Color.White,
    error = HeartRed
)

private val LightColorScheme = lightColorScheme(
    primary = DuolingoGreen,
    onPrimary = Color.White,
    primaryContainer = DuolingoGreenLight,
    secondary = SpeakBlue,
    onSecondary = Color.White,
    tertiary = GoldYellow,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = Color(0xFFF0F2F5),
    onBackground = NeutralDark,
    onSurface = NeutralDark,
    error = HeartRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded Duolingo/SpeakX colors for signature identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
