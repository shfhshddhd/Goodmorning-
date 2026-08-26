package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TgVoiceColorScheme = darkColorScheme(
    primary = TgBlue,
    onPrimary = Color.White,
    primaryContainer = TgBlueDark,
    onPrimaryContainer = Color.White,
    secondary = TgCyan,
    onSecondary = Color.Black,
    secondaryContainer = TgDarkSurfaceVariant,
    onSecondaryContainer = TgCyan,
    tertiary = TgVoiceGreen,
    onTertiary = Color.Black,
    background = TgDarkBackground,
    onBackground = TgTextPrimary,
    surface = TgDarkSurface,
    onSurface = TgTextPrimary,
    surfaceVariant = TgDarkSurfaceVariant,
    onSurfaceVariant = TgTextSecondary,
    surfaceContainer = TgDarkCard,
    outline = TgDarkBorder,
    error = TgVoiceMutedRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Always use Telegram Dark Theme for minimal battery and low-light voice chat ergonomics
    MaterialTheme(
        colorScheme = TgVoiceColorScheme,
        typography = Typography,
        content = content
    )
}
