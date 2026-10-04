package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val MidnightColorScheme = darkColorScheme(
    primary = MidnightPrimary,
    onPrimary = MidnightOnPrimary,
    primaryContainer = MidnightSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = MidnightSecondary,
    onSecondary = Color(0xFF022C1A),
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = MidnightTertiary,
    onTertiary = Color(0xFF451A03),
    background = MidnightBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = MidnightSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

private val AmoledColorScheme = darkColorScheme(
    primary = AmoledPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF142436),
    onPrimaryContainer = Color.White,
    secondary = AmoledSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0D3321),
    onSecondaryContainer = Color.White,
    tertiary = AmoledTertiary,
    onTertiary = Color.Black,
    background = AmoledBackground,
    onBackground = Color.White,
    surface = AmoledSurface,
    onSurface = Color.White,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = Color(0xFF262626),
    outlineVariant = Color(0xFF161616)
)

private val SunsetColorScheme = darkColorScheme(
    primary = SunsetPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3D1F17),
    onPrimaryContainer = Color.White,
    secondary = SunsetSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF431526),
    onSecondaryContainer = Color(0xFFFFD1DC),
    tertiary = SunsetTertiary,
    onTertiary = Color(0xFF3E2200),
    background = SunsetBackground,
    onBackground = Color(0xFFFCE7F3),
    surface = SunsetSurface,
    onSurface = Color(0xFFFCE7F3),
    surfaceVariant = SunsetSurfaceVariant,
    onSurfaceVariant = Color(0xFFD4B3DA),
    outline = Color(0xFF4A3459),
    outlineVariant = Color(0xFF2C1F3D)
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF047857),
    tertiary = LightTertiary,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun StudyCircleTheme(
    themeMode: String = "DARK", // DARK, AMOLED, SUNSET, LIGHT, SYSTEM
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val colorScheme: ColorScheme = when (themeMode) {
        "AMOLED" -> AmoledColorScheme
        "SUNSET" -> SunsetColorScheme
        "LIGHT" -> LightColorScheme
        "DARK" -> MidnightColorScheme
        else -> if (systemDark) MidnightColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
