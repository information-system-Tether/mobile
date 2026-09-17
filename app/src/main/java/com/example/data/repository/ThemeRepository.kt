package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemePalette(val displayNameRu: String, val primaryHex: Long) {
    EMERALD("Изумруд", 0xFF10B981),
    OCEAN("Океан", 0xFF0284C7),
    SUNSET("Закат", 0xFFF97316),
    NEON("Неон", 0xFF8B5CF6)
}

enum class DarkThemeMode(val displayNameRu: String) {
    SYSTEM("Системная"),
    LIGHT("Светлая"),
    DARK("Тёмная")
}

data class AppThemeConfig(
    val useMonet: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    val palette: AppThemePalette = AppThemePalette.EMERALD,
    val darkThemeMode: DarkThemeMode = DarkThemeMode.SYSTEM
)

class ThemeRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)

    private val _themeConfig = MutableStateFlow(loadConfig())
    val themeConfig: StateFlow<AppThemeConfig> = _themeConfig.asStateFlow()

    private fun loadConfig(): AppThemeConfig {
        val useMonetDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val useMonet = prefs.getBoolean(KEY_USE_MONET, useMonetDefault)
        val paletteName = prefs.getString(KEY_PALETTE, AppThemePalette.EMERALD.name) ?: AppThemePalette.EMERALD.name
        val modeName = prefs.getString(KEY_DARK_MODE, DarkThemeMode.SYSTEM.name) ?: DarkThemeMode.SYSTEM.name

        val palette = try { AppThemePalette.valueOf(paletteName) } catch (e: Exception) { AppThemePalette.EMERALD }
        val mode = try { DarkThemeMode.valueOf(modeName) } catch (e: Exception) { DarkThemeMode.SYSTEM }

        return AppThemeConfig(
            useMonet = useMonet,
            palette = palette,
            darkThemeMode = mode
        )
    }

    private fun saveConfig(config: AppThemeConfig) {
        prefs.edit()
            .putBoolean(KEY_USE_MONET, config.useMonet)
            .putString(KEY_PALETTE, config.palette.name)
            .putString(KEY_DARK_MODE, config.darkThemeMode.name)
            .apply()
        _themeConfig.value = config
    }

    fun setUseMonet(useMonet: Boolean) {
        val current = _themeConfig.value
        saveConfig(current.copy(useMonet = useMonet))
    }

    fun setPalette(palette: AppThemePalette) {
        val current = _themeConfig.value
        saveConfig(current.copy(palette = palette))
    }

    fun setDarkThemeMode(mode: DarkThemeMode) {
        val current = _themeConfig.value
        saveConfig(current.copy(darkThemeMode = mode))
    }

    companion object {
        private const val KEY_USE_MONET = "key_use_monet"
        private const val KEY_PALETTE = "key_palette"
        private const val KEY_DARK_MODE = "key_dark_mode"
    }
}
