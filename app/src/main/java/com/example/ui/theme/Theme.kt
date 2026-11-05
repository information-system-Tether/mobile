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
import com.example.data.repository.AppThemePalette
import com.example.data.repository.DarkThemeMode

private val M3LightGreenColorScheme = lightColorScheme(
    primary = M3LightPrimary,
    onPrimary = M3LightOnPrimary,
    primaryContainer = M3LightPrimaryContainer,
    onPrimaryContainer = M3LightOnPrimaryContainer,
    secondary = M3LightSecondary,
    onSecondary = M3LightOnSecondary,
    secondaryContainer = M3LightSecondaryContainer,
    onSecondaryContainer = M3LightOnSecondaryContainer,
    tertiary = M3LightTertiary,
    onTertiary = M3LightOnTertiary,
    tertiaryContainer = M3LightTertiaryContainer,
    onTertiaryContainer = M3LightOnTertiaryContainer,
    background = M3LightBackground,
    onBackground = M3LightOnBackground,
    surface = Color(0xFFFFFFFF),
    onSurface = M3LightOnSurface,
    surfaceVariant = Color(0xFFF1F4F1),
    onSurfaceVariant = M3LightOnSurfaceVariant,
    outline = M3LightOutline,
    outlineVariant = M3LightOutlineVariant,
    surfaceTint = Color.Transparent,
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFF6F8F6),
    surfaceContainerLow = Color(0xFFFAFCFA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFECEFEC),
    surfaceBright = Color(0xFFFFFFFF)
)

private val M3DarkColorScheme = darkColorScheme(
    primary = M3DarkPrimary,
    onPrimary = M3DarkOnPrimary,
    primaryContainer = M3DarkPrimaryContainer,
    onPrimaryContainer = M3DarkOnPrimaryContainer,
    secondary = M3DarkSecondary,
    onSecondary = M3DarkOnSecondary,
    secondaryContainer = M3DarkSecondaryContainer,
    onSecondaryContainer = M3DarkOnSecondaryContainer,
    tertiary = M3DarkTertiary,
    onTertiary = M3DarkOnTertiary,
    tertiaryContainer = M3DarkTertiaryContainer,
    onTertiaryContainer = M3DarkOnTertiaryContainer,
    background = M3DarkBackground,
    onBackground = M3DarkOnBackground,
    surface = M3DarkSurface,
    onSurface = M3DarkOnSurface,
    surfaceVariant = M3DarkSurfaceVariant,
    onSurfaceVariant = M3DarkOnSurfaceVariant,
    outline = M3DarkOutline,
    outlineVariant = M3DarkOutlineVariant,
    surfaceTint = Color.Transparent,
    surfaceContainer = Color(0xFF1E241F),
    surfaceContainerHigh = Color(0xFF252B26),
    surfaceContainerHighest = Color(0xFF2F3630),
    surfaceContainerLow = Color(0xFF1A1F1B),
    surfaceContainerLowest = Color(0xFF121613)
)

@Composable
fun MyApplicationTheme(
    darkThemeMode: DarkThemeMode = DarkThemeMode.SYSTEM,
    useMonet: Boolean = false,
    palette: AppThemePalette = AppThemePalette.EMERALD,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (darkThemeMode) {
        DarkThemeMode.SYSTEM -> systemInDark
        DarkThemeMode.LIGHT -> false
        DarkThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        useMonet && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> M3DarkColorScheme
        else -> M3LightGreenColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
