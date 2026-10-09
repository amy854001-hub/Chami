package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GeminiSoftBlue,
    onPrimary = GeminiDarkBackground,
    primaryContainer = GeminiDarkSurfaceVariant,
    onPrimaryContainer = GeminiDarkTextPrimary,
    secondary = GeminiPurple,
    onSecondary = GeminiDarkBackground,
    secondaryContainer = GeminiDarkSurfaceVariant,
    onSecondaryContainer = GeminiDarkTextPrimary,
    tertiary = GeminiCyan,
    background = GeminiDarkBackground,
    onBackground = GeminiDarkTextPrimary,
    surface = GeminiDarkSurface,
    onSurface = GeminiDarkTextPrimary,
    surfaceVariant = GeminiDarkSurfaceVariant,
    onSurfaceVariant = GeminiDarkTextSecondary,
    outline = GeminiDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = GeminiBlue,
    onPrimary = GeminiLightBackground,
    primaryContainer = GeminiLightSurfaceVariant,
    onPrimaryContainer = GeminiLightTextPrimary,
    secondary = GeminiPurple,
    onSecondary = GeminiLightBackground,
    secondaryContainer = GeminiLightSurfaceVariant,
    onSecondaryContainer = GeminiLightTextPrimary,
    tertiary = GeminiCyan,
    background = GeminiLightBackground,
    onBackground = GeminiLightTextPrimary,
    surface = GeminiLightSurface,
    onSurface = GeminiLightTextPrimary,
    surfaceVariant = GeminiLightSurfaceVariant,
    onSurfaceVariant = GeminiLightTextSecondary,
    outline = GeminiLightOutline
)

@Composable
fun CharmiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
