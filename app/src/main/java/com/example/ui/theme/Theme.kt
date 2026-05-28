package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BentoLightPrimary,
    onPrimary = Color.White,
    primaryContainer = BentoLightPrimaryContainer,
    onPrimaryContainer = BentoLightOnPrimaryContainer,
    secondary = BentoLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = BentoLightSecondaryContainer,
    onSecondaryContainer = BentoLightOnSecondaryContainer,
    tertiary = BentoLightTertiary,
    onTertiary = Color.White,
    tertiaryContainer = BentoLightTertiaryContainer,
    onTertiaryContainer = BentoLightOnTertiaryContainer,
    background = BentoLightBg,
    onBackground = BentoLightText,
    surface = BentoLightSurface,
    onSurface = BentoLightText,
    surfaceVariant = BentoLightSurface,
    onSurfaceVariant = BentoLightSecondary,
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

private val DarkColorScheme = darkColorScheme(
    primary = BentoDarkPrimary,
    onPrimary = Color(0xFF381E72),
    primaryContainer = BentoDarkPrimaryContainer,
    onPrimaryContainer = BentoDarkOnPrimaryContainer,
    secondary = BentoDarkSecondary,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = BentoDarkSecondaryContainer,
    onSecondaryContainer = BentoDarkOnSecondaryContainer,
    tertiary = BentoDarkTertiary,
    onTertiary = Color(0xFF492532),
    tertiaryContainer = BentoDarkTertiaryContainer,
    onTertiaryContainer = BentoDarkOnTertiaryContainer,
    background = BentoDarkBg,
    onBackground = BentoDarkText,
    surface = BentoDarkSurface,
    onSurface = BentoDarkText,
    surfaceVariant = BentoDarkSurface,
    onSurfaceVariant = BentoDarkSecondary,
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
