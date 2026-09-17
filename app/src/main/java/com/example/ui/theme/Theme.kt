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

private fun getCustomPaletteScheme(palette: AppThemePalette, isDark: Boolean): ColorScheme {
    return when (palette) {
        AppThemePalette.EMERALD -> {
            if (isDark) {
                darkColorScheme(primary = Color(0xFF10B981), secondary = Color(0xFF14B8A6), tertiary = Color(0xFFF59E0B))
            } else {
                lightColorScheme(primary = Color(0xFF059669), secondary = Color(0xFF0D9488), tertiary = Color(0xFFD97706))
            }
        }
        AppThemePalette.OCEAN -> {
            if (isDark) {
                darkColorScheme(primary = Color(0xFF38BDF8), secondary = Color(0xFF818CF8), tertiary = Color(0xFF34D399))
            } else {
                lightColorScheme(primary = Color(0xFF0284C7), secondary = Color(0xFF4F46E5), tertiary = Color(0xFF059669))
            }
        }
        AppThemePalette.SUNSET -> {
            if (isDark) {
                darkColorScheme(primary = Color(0xFFFB923C), secondary = Color(0xFFF43F5E), tertiary = Color(0xFFFACC15))
            } else {
                lightColorScheme(primary = Color(0xFFEA580C), secondary = Color(0xFFE11D48), tertiary = Color(0xFFCA8A04))
            }
        }
        AppThemePalette.NEON -> {
            if (isDark) {
                darkColorScheme(primary = Color(0xFFA78BFA), secondary = Color(0xFFF472B6), tertiary = Color(0xFF38BDF8))
            } else {
                lightColorScheme(primary = Color(0xFF7C3AED), secondary = Color(0xFFDB2777), tertiary = Color(0xFF0284C7))
            }
        }
    }
}

@Composable
fun MyApplicationTheme(
    darkThemeMode: DarkThemeMode = DarkThemeMode.SYSTEM,
    useMonet: Boolean = true,
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
        else -> getCustomPaletteScheme(palette, isDark)
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
