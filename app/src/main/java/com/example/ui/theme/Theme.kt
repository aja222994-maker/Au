package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = WarmGoldPrimary,
    secondary = SepiaSecondary,
    tertiary = SageGreenTertiary,
    background = ParchmentDarkBackground,
    surface = ParchmentCardSurface,
    onBackground = InkTextOnBackground,
    onSurface = InkTextOnSurface,
    outline = ParchmentBorder
)

private val LightColorScheme = lightColorScheme(
    primary = WarmGoldPrimary,
    secondary = SepiaSecondary,
    tertiary = SageGreenTertiary,
    background = InkTextOnBackground,
    surface = InkTextOnSurface,
    onBackground = ParchmentDarkBackground,
    onSurface = ParchmentDarkBackground,
    outline = ParchmentBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark mode for the vintage writer look
    dynamicColor: Boolean = false, // Set to false to preserve the luxury book vibe
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
