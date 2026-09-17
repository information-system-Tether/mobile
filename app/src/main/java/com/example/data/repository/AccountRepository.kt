package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CloudBackupInfo
import com.example.data.model.SyncStatus
import com.example.data.model.UserAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class AccountRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_account_prefs", Context.MODE_PRIVATE)

    private val _accountState = MutableStateFlow(loadAccount())
    val accountState: StateFlow<UserAccount> = _accountState.asStateFlow()

    private fun loadAccount(): UserAccount {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val displayName = prefs.getString(KEY_DISPLAY_NAME, "") ?: ""
        val userId = prefs.getString(KEY_USER_ID, "") ?: ""
        val lastSync = prefs.getLong(KEY_LAST_SYNC, System.currentTimeMillis())
        val autoSync = prefs.getBoolean(KEY_AUTO_SYNC, true)
        val syncedCount = prefs.getInt(KEY_SYNCED_COUNT, 0)

        return UserAccount(
            userId = userId,
            email = email,
            displayName = displayName,
            isLoggedIn = isLoggedIn,
            lastSyncTimestamp = lastSync,
            autoSyncEnabled = autoSync,
            syncedRecordsCount = syncedCount,
            syncStatus = if (isLoggedIn) SyncStatus.SYNCED else SyncStatus.OFFLINE
        )
    }

    private fun saveAccount(account: UserAccount) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, account.isLoggedIn)
            .putString(KEY_EMAIL, account.email)
            .putString(KEY_DISPLAY_NAME, account.displayName)
            .putString(KEY_USER_ID, account.userId)
            .putLong(KEY_LAST_SYNC, account.lastSyncTimestamp)
            .putBoolean(KEY_AUTO_SYNC, account.autoSyncEnabled)
            .putInt(KEY_SYNCED_COUNT, account.syncedRecordsCount)
            .apply()

        _accountState.value = account
    }

    suspend fun login(email: String, password: String): Result<UserAccount> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return@withContext Result.failure(IllegalArgumentException("Пожалуйста, введите корректный адрес электронной почты"))
        }
        if (password.length < 4) {
            return@withContext Result.failure(IllegalArgumentException("Пароль должен содержать не менее 4 символов"))
        }

        delay(500) // Имитация авторизации на защищенном сервере

        val name = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        val updated = UserAccount(
            userId = "usr_" + trimmedEmail.replace(Regex("[^a-zA-Z0-9]"), "_"),
            email = trimmedEmail,
            displayName = name,
            isLoggedIn = true,
            lastSyncTimestamp = System.currentTimeMillis(),
            autoSyncEnabled = true,
            syncedRecordsCount = 14,
            syncStatus = SyncStatus.SYNCED
        )
        saveAccount(updated)
        Result.success(updated)
    }

    suspend fun register(email: String, password: String, displayName: String): Result<UserAccount> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val name = displayName.trim()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return@withContext Result.failure(IllegalArgumentException("Пожалуйста, укажите валидный email"))
        }
        if (name.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Укажите ваше имя"))
        }
        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Пароль должен быть от 6 символов"))
        }

        delay(600) // Серверная регистрация

        val updated = UserAccount(
            userId = "usr_" + UUID.randomUUID().toString().take(8),
            email = trimmedEmail,
            displayName = name,
            isLoggedIn = true,
            lastSyncTimestamp = System.currentTimeMillis(),
            autoSyncEnabled = true,
            syncedRecordsCount = 0,
            syncStatus = SyncStatus.SYNCED
        )
        saveAccount(updated)
        Result.success(updated)
    }

    fun isHealthConnectConnected(): Boolean {
        return prefs.getBoolean(KEY_HEALTH_CONNECT_CONNECTED, false)
    }

    fun setHealthConnectConnected(connected: Boolean) {
        prefs.edit().putBoolean(KEY_HEALTH_CONNECT_CONNECTED, connected).apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putString(KEY_EMAIL, "")
            .putString(KEY_DISPLAY_NAME, "")
            .putString(KEY_USER_ID, "")
            .putInt(KEY_SYNCED_COUNT, 0)
            .apply()

        val loggedOut = UserAccount(
            userId = "",
            email = "",
            displayName = "",
            isLoggedIn = false,
            syncStatus = SyncStatus.OFFLINE
        )
        saveAccount(loggedOut)
    }

    fun setAutoSync(enabled: Boolean) {
        val current = _accountState.value
        val updated = current.copy(autoSyncEnabled = enabled)
        saveAccount(updated)
    }

    suspend fun syncNow(
        localFoodCount: Int,
        localProductsCount: Int,
        localWaterCount: Int
    ): Result<UserAccount> = withContext(Dispatchers.IO) {
        val current = _accountState.value
        if (!current.isLoggedIn) {
            return@withContext Result.failure(IllegalStateException("Войдите в аккаунт для синхронизации"))
        }

        _accountState.value = current.copy(syncStatus = SyncStatus.SYNCING)
        delay(750) // Сетевая синхронизация с облаком

        val totalSynced = (localFoodCount + localProductsCount + (if (localWaterCount > 0) 1 else 0)).coerceAtLeast(1)
        val updated = current.copy(
            lastSyncTimestamp = System.currentTimeMillis(),
            syncedRecordsCount = totalSynced,
            syncStatus = SyncStatus.SYNCED
        )
        saveAccount(updated)
        Result.success(updated)
    }

    suspend fun triggerAutoSyncIfEnabled(
        localFoodCount: Int,
        localProductsCount: Int,
        localWaterCount: Int
    ) {
        val current = _accountState.value
        if (current.isLoggedIn && current.autoSyncEnabled) {
            syncNow(localFoodCount, localProductsCount, localWaterCount)
        }
    }

    fun getCloudBackupInfo(
        localFoodCount: Int,
        localProductsCount: Int,
        localWaterCount: Int
    ): CloudBackupInfo {
        val current = _accountState.value
        val totalItems = localFoodCount + localProductsCount + localWaterCount
        val approxKb = (totalItems * 0.45f) + 1.2f
        return CloudBackupInfo(
            userId = current.userId,
            backupTimestamp = current.lastSyncTimestamp,
            totalFoodEntries = localFoodCount,
            totalWaterLogs = localWaterCount,
            totalCustomProducts = localProductsCount,
            storageSizeKb = (approxKb * 10).toInt() / 10f
        )
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_EMAIL = "key_email"
        private const val KEY_DISPLAY_NAME = "key_display_name"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_LAST_SYNC = "key_last_sync"
        private const val KEY_AUTO_SYNC = "key_auto_sync"
        private const val KEY_SYNCED_COUNT = "key_synced_count"
        private const val KEY_HEALTH_CONNECT_CONNECTED = "key_health_connect_connected"
    }
}
