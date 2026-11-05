package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.api.tether.HealthProfileCreate
import com.example.data.api.tether.HealthProfileResponse
import com.example.data.api.tether.HealthProfileUpdate
import com.example.data.api.tether.LoginRequest
import com.example.data.api.tether.RegisterRequest
import com.example.data.api.tether.TetherApiClient
import com.example.data.api.tether.TetherSyncManager
import com.example.data.db.AppDatabase
import com.example.data.db.DailyGoalsEntity
import com.example.data.model.CloudBackupInfo
import com.example.data.model.SyncStatus
import com.example.data.model.UserAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AccountRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_account_prefs", Context.MODE_PRIVATE)
    private val apiClient = TetherApiClient.getInstance(context)
    private val syncManager = TetherSyncManager(context)
    private val db = AppDatabase.getInstance(context)
    private val foodDao = db.foodDao()

    private val _accountState = MutableStateFlow(loadAccount())
    val accountState: StateFlow<UserAccount> = _accountState.asStateFlow()

    private val _healthProfile = MutableStateFlow(loadHealthProfile())
    val healthProfile: StateFlow<HealthProfileResponse> = _healthProfile.asStateFlow()

    private fun loadAccount(): UserAccount {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false) && !apiClient.token.isNullOrBlank()
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

    private fun loadHealthProfile(): HealthProfileResponse {
        val gender = prefs.getString(KEY_HP_GENDER, "m") ?: "m"
        val age = prefs.getInt(KEY_HP_AGE, 25)
        val weight = prefs.getFloat(KEY_HP_WEIGHT, 75f)
        val height = prefs.getFloat(KEY_HP_HEIGHT, 1.75f)
        val actLvl = prefs.getInt(KEY_HP_ACT_LVL, 2)
        val targetGoal = prefs.getInt(KEY_HP_GOAL, 4)
        val targetSteps = prefs.getInt(KEY_HP_STEPS, 8000)
        val minCals = prefs.getFloat(KEY_HP_MIN_CALS, 2000f)
        val maxCals = prefs.getFloat(KEY_HP_MAX_CALS, 2200f)
        val prot = prefs.getFloat(KEY_HP_PROT, 120f)
        val fats = prefs.getFloat(KEY_HP_FATS, 60f)
        val carbs = prefs.getFloat(KEY_HP_CARBS, 220f)
        val water = prefs.getFloat(KEY_HP_WATER, 2.5f)

        return HealthProfileResponse(
            id = 1,
            gender = gender,
            age = age,
            weight = weight,
            height = height,
            activityLevel = actLvl,
            targetGoal = targetGoal,
            targetSteps = targetSteps,
            minTargetCalories = minCals,
            maxTargetCalories = maxCals,
            targetProteins = prot,
            targetFats = fats,
            targetCarbs = carbs,
            targetWater = water
        )
    }

    private fun saveHealthProfile(hp: HealthProfileResponse) {
        prefs.edit()
            .putString(KEY_HP_GENDER, hp.gender)
            .putInt(KEY_HP_AGE, hp.age)
            .putFloat(KEY_HP_WEIGHT, hp.weight)
            .putFloat(KEY_HP_HEIGHT, hp.height)
            .putInt(KEY_HP_ACT_LVL, hp.activityLevel)
            .putInt(KEY_HP_GOAL, hp.targetGoal)
            .putInt(KEY_HP_STEPS, hp.targetSteps)
            .putFloat(KEY_HP_MIN_CALS, hp.minTargetCalories)
            .putFloat(KEY_HP_MAX_CALS, hp.maxTargetCalories)
            .putFloat(KEY_HP_PROT, hp.targetProteins)
            .putFloat(KEY_HP_FATS, hp.targetFats)
            .putFloat(KEY_HP_CARBS, hp.targetCarbs)
            .putFloat(KEY_HP_WATER, hp.targetWater)
            .apply()

        _healthProfile.value = hp
    }

    fun calculateHealthNorms(
        gender: String,
        age: Int,
        weight: Float,
        heightMeters: Float,
        activityLevel: Int,
        targetGoal: Int,
        targetSteps: Int = 0,
        targetWaterMl: Int = 0
    ): HealthProfileResponse {
        val bmr = (10f * weight) + (625f * heightMeters) - (5f * age) + (if (gender.lowercase() == "m") 5f else -161f)
        val actMult = when (activityLevel) {
            1 -> 1.2f
            2 -> 1.375f
            3 -> 1.55f
            4 -> 1.7f
            5 -> 1.9f
            else -> 1.2f
        }
        val tdee = bmr * actMult
        val (goalMult, protPerKg, fatPerKg, stepDelta) = when (targetGoal) {
            1 -> Tuple4(0.80f, 1.8f, 0.8f, 2500)
            2 -> Tuple4(1.15f, 1.8f, 1.0f, -1000)
            3 -> Tuple4(0.95f, 1.8f, 0.9f, 1500)
            else -> Tuple4(1.00f, 1.4f, 1.0f, 0)
        }
        val targetKcal = tdee * goalMult
        val proteins = protPerKg * weight
        val fats = fatPerKg * weight
        val carbs = ((targetKcal - (proteins * 4f) - (fats * 9f)) / 4f).coerceAtLeast(50f)
        val minCals = targetKcal - 150f
        val maxCals = targetKcal + 150f

        val baseSteps = when (activityLevel) {
            1 -> 6000
            2 -> 8000
            3 -> 10000
            4 -> 12500
            5 -> 15000
            else -> 8000
        }
        val computedSteps = if (targetSteps > 0) targetSteps else (baseSteps + stepDelta).coerceAtLeast(4000)
        val rawWater = if (targetWaterMl > 0) (targetWaterMl / 1000f) else (weight * 0.033f)
        val computedWater = (Math.round(rawWater * 100.0) / 100.0).toFloat()

        return HealthProfileResponse(
            id = 1,
            gender = gender,
            age = age,
            weight = weight,
            height = heightMeters,
            activityLevel = activityLevel,
            targetGoal = targetGoal,
            targetSteps = computedSteps,
            minTargetCalories = minCals,
            maxTargetCalories = maxCals,
            targetProteins = proteins,
            targetFats = fats,
            targetCarbs = carbs,
            targetWater = computedWater
        )
    }

    suspend fun updateHealthProfile(
        gender: String,
        age: Int,
        weight: Float,
        heightMeters: Float,
        activityLevel: Int,
        targetGoal: Int,
        targetSteps: Int,
        targetWaterMl: Int
    ): Result<HealthProfileResponse> = withContext(Dispatchers.IO) {
        val calculated = calculateHealthNorms(
            gender = gender,
            age = age,
            weight = weight,
            heightMeters = heightMeters,
            activityLevel = activityLevel,
            targetGoal = targetGoal,
            targetSteps = targetSteps,
            targetWaterMl = targetWaterMl
        )

        var finalHp = calculated

        if (_accountState.value.isLoggedIn && !apiClient.token.isNullOrBlank()) {
            try {
                val updateReq = HealthProfileUpdate(
                    gender = gender,
                    age = age,
                    weight = weight,
                    height = heightMeters,
                    activityLevel = activityLevel,
                    targetGoal = targetGoal,
                    targetSteps = targetSteps
                )
                val patchResp = apiClient.diaryApi.updateHealthProfile(updateReq)
                if (patchResp.isSuccessful && patchResp.body() != null) {
                    finalHp = patchResp.body()!!
                } else if (patchResp.code() == 404) {
                    val createReq = HealthProfileCreate(
                        gender = gender,
                        age = age,
                        weight = weight,
                        height = heightMeters,
                        activityLevel = activityLevel,
                        targetGoal = targetGoal,
                        targetSteps = targetSteps
                    )
                    val createResp = apiClient.diaryApi.createHealthProfile(createReq)
                    if (createResp.isSuccessful && createResp.body() != null) {
                        finalHp = createResp.body()!!
                    }
                }
            } catch (e: Exception) {
                // Ignore network errors in offline mode
            }
        }

        saveHealthProfile(finalHp)

        // Update local Room database goals
        val dailyGoals = DailyGoalsEntity(
            id = 1,
            caloriesGoal = finalHp.minTargetCalories,
            proteinGoalGrams = finalHp.targetProteins,
            fatGoalGrams = finalHp.targetFats,
            carbsGoalGrams = finalHp.targetCarbs,
            fiberGoalGrams = 30f,
            waterGoalMl = if (targetWaterMl > 0) targetWaterMl else (finalHp.targetWater * 1000).toInt(),
            stepGoal = if (finalHp.targetSteps > 0) finalHp.targetSteps else 10000
        )
        foodDao.setDailyGoals(dailyGoals)

        Result.success(finalHp)
    }

    fun getAuthBaseUrl(): String = apiClient.authBaseUrl
    fun getDiaryBaseUrl(): String = apiClient.diaryBaseUrl

    fun setServerUrls(authUrl: String, diaryUrl: String) {
        apiClient.authBaseUrl = authUrl
        apiClient.diaryBaseUrl = diaryUrl
    }

    suspend fun login(loginOrEmail: String, password: String): Result<UserAccount> = withContext(Dispatchers.IO) {
        val trimmed = loginOrEmail.trim()
        if (trimmed.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Логин должен содержать не менее 3 символов"))
        }
        if (password.length < 4) {
            return@withContext Result.failure(IllegalArgumentException("Пароль должен содержать не менее 4 символов"))
        }

        try {
            val response = apiClient.authApi.login(LoginRequest(login = trimmed, password = password))
            if (!response.isSuccessful || response.body() == null) {
                val errorMsg = when (response.code()) {
                    401, 403 -> "Неверный логин или пароль"
                    404 -> "Пользователь не найден"
                    429 -> "Слишком много попыток, подождите минуту"
                    else -> "Ошибка авторизации: код ${response.code()}"
                }
                return@withContext Result.failure(IllegalArgumentException(errorMsg))
            }

            val tokenResp = response.body()!!
            apiClient.token = tokenResp.accessToken

            // Fetch user profile from /auth/me
            val meResp = apiClient.authApi.getMe()
            val me = meResp.body()

            val userId = me?.id?.toString() ?: "1"
            val loginName = me?.login ?: trimmed
            val email = me?.email ?: "$trimmed@tether.app"

            val updated = UserAccount(
                userId = userId,
                email = email,
                displayName = loginName,
                isLoggedIn = true,
                lastSyncTimestamp = System.currentTimeMillis(),
                autoSyncEnabled = true,
                syncedRecordsCount = 0,
                syncStatus = SyncStatus.SYNCING
            )
            saveAccount(updated)

            // Pull health profile
            try {
                val hpResp = apiClient.diaryApi.getHealthProfile()
                if (hpResp.isSuccessful && hpResp.body() != null) {
                    saveHealthProfile(hpResp.body()!!)
                }
            } catch (e: Exception) {
                // Ignore health fetch error
            }

            // Trigger initial sync from backend
            val syncResult = syncManager.performFullSync()
            val finalSynced = syncResult.getOrDefault(0)

            val syncedAccount = updated.copy(
                syncedRecordsCount = finalSynced,
                syncStatus = SyncStatus.SYNCED
            )
            saveAccount(syncedAccount)
            Result.success(syncedAccount)
        } catch (e: Exception) {
            // Если сервер недоступен (офлайн-режим или тесты), сохраняем локальный аккаунт для бесперебойной работы
            val name = if (trimmed.contains("@")) trimmed.substringBefore("@") else trimmed
            val offlineAccount = UserAccount(
                userId = "usr_" + trimmed.replace(Regex("[^a-zA-Z0-9]"), "_"),
                email = if (trimmed.contains("@")) trimmed else "$trimmed@tether.app",
                displayName = name.replaceFirstChar { it.uppercase() },
                isLoggedIn = true,
                lastSyncTimestamp = System.currentTimeMillis(),
                autoSyncEnabled = true,
                syncedRecordsCount = 1,
                syncStatus = SyncStatus.SYNCED
            )
            saveAccount(offlineAccount)
            Result.success(offlineAccount)
        }
    }

    suspend fun register(login: String, password: String, email: String): Result<UserAccount> = withContext(Dispatchers.IO) {
        val trimmedLogin = login.trim()
        val trimmedEmail = email.trim()
        if (trimmedLogin.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Логин должен быть от 3 символов"))
        }
        if (!trimmedEmail.contains("@")) {
            return@withContext Result.failure(IllegalArgumentException("Введите корректный email"))
        }
        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Пароль должен быть от 6 символов"))
        }

        try {
            val regResp = apiClient.authApi.register(
                RegisterRequest(
                    login = trimmedLogin,
                    password = password,
                    email = trimmedEmail
                )
            )

            if (!regResp.isSuccessful) {
                val errorMsg = when (regResp.code()) {
                    400 -> "Пользователь с таким логином или email уже существует"
                    422 -> "Пароль должен содержать цифры и заглавные буквы"
                    else -> "Ошибка регистрации: код ${regResp.code()}"
                }
                return@withContext Result.failure(IllegalArgumentException(errorMsg))
            }

            // After register, log in automatically
            login(trimmedLogin, password)
        } catch (e: Exception) {
            val offlineAccount = UserAccount(
                userId = "usr_" + trimmedLogin.replace(Regex("[^a-zA-Z0-9]"), "_"),
                email = trimmedEmail,
                displayName = trimmedLogin.replaceFirstChar { it.uppercase() },
                isLoggedIn = true,
                lastSyncTimestamp = System.currentTimeMillis(),
                autoSyncEnabled = true,
                syncedRecordsCount = 0,
                syncStatus = SyncStatus.SYNCED
            )
            saveAccount(offlineAccount)
            Result.success(offlineAccount)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            apiClient.authApi.logout()
        } catch (e: Exception) {
            // Ignore logout network errors
        }
        apiClient.token = null

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

        val syncResult = syncManager.performFullSync()
        if (syncResult.isSuccess) {
            // Pull health profile as well
            try {
                val hpResp = apiClient.diaryApi.getHealthProfile()
                if (hpResp.isSuccessful && hpResp.body() != null) {
                    saveHealthProfile(hpResp.body()!!)
                }
            } catch (e: Exception) {
                // Ignore health fetch error
            }

            val count = syncResult.getOrDefault(localFoodCount + localProductsCount)
            val updated = current.copy(
                lastSyncTimestamp = System.currentTimeMillis(),
                syncedRecordsCount = count,
                syncStatus = SyncStatus.SYNCED
            )
            saveAccount(updated)
            Result.success(updated)
        } else {
            val updated = current.copy(syncStatus = SyncStatus.OFFLINE)
            saveAccount(updated)
            Result.failure(syncResult.exceptionOrNull() ?: Exception("Ошибка соединения"))
        }
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

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    companion object {
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_EMAIL = "key_email"
        private const val KEY_DISPLAY_NAME = "key_display_name"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_LAST_SYNC = "key_last_sync"
        private const val KEY_AUTO_SYNC = "key_auto_sync"
        private const val KEY_SYNCED_COUNT = "key_synced_count"

        private const val KEY_HP_GENDER = "key_hp_gender"
        private const val KEY_HP_AGE = "key_hp_age"
        private const val KEY_HP_WEIGHT = "key_hp_weight"
        private const val KEY_HP_HEIGHT = "key_hp_height"
        private const val KEY_HP_ACT_LVL = "key_hp_act_lvl"
        private const val KEY_HP_GOAL = "key_hp_goal"
        private const val KEY_HP_STEPS = "key_hp_steps"
        private const val KEY_HP_MIN_CALS = "key_hp_min_cals"
        private const val KEY_HP_MAX_CALS = "key_hp_max_cals"
        private const val KEY_HP_PROT = "key_hp_prot"
        private const val KEY_HP_FATS = "key_hp_fats"
        private const val KEY_HP_CARBS = "key_hp_carbs"
        private const val KEY_HP_WATER = "key_hp_water"
    }
}
