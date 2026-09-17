package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.health.HealthConnectManager
import com.example.data.health.StepSensorManager
import com.example.data.model.CloudBackupInfo
import com.example.data.model.DailyGoals
import com.example.data.model.DailyNutrientStats
import com.example.data.model.FoodProduct
import com.example.data.model.GpxTrack
import com.example.data.model.LoggedFood
import com.example.data.model.MealType
import com.example.data.model.UserAccount
import com.example.data.model.Workout
import com.example.data.model.WorkoutCategory
import com.example.data.repository.AccountRepository
import com.example.data.repository.AppThemeConfig
import com.example.data.repository.AppThemePalette
import com.example.data.repository.DarkThemeMode
import com.example.data.repository.DefaultFoodDatabase
import com.example.data.repository.NutritionRepository
import com.example.data.repository.ThemeRepository
import com.example.data.repository.TrackRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class NutritionUiState(
    val selectedDate: String,
    val formattedDateTitle: String,
    val activeTab: Int = 0, // 0 = Diary, 1 = Products, 2 = Stats, 3 = Account
    val searchQuery: String = "",
    val searchResults: List<FoodProduct> = emptyList(),
    val favoriteProductIds: Set<String> = emptySet(),
    val isSearching: Boolean = false,
    val showAddFoodSheet: Boolean = false,
    val targetMealForAdd: MealType = MealType.BREAKFAST,
    val selectedFoodToLog: FoodProduct? = null,
    val showCreateProductSheet: Boolean = false,
    val showAddWorkoutSheet: Boolean = false,
    val showSettings: Boolean = false,
    val isHealthConnectAvailable: Boolean = true,
    val isHealthConnectConnected: Boolean = false,
    val snackbarMessage: String? = null
)

class NutritionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NutritionRepository(AppDatabase.getDatabase(application))
    private val accountRepository = AccountRepository(application)
    private val trackRepository = TrackRepository(application)
    private val themeRepository = ThemeRepository(application)
    val healthConnectManager = HealthConnectManager(application)
    val stepSensorManager = StepSensorManager(application)

    val account: StateFlow<UserAccount> = accountRepository.accountState
    val themeConfig: StateFlow<AppThemeConfig> = themeRepository.themeConfig
    val communityTracks: StateFlow<List<GpxTrack>> = trackRepository.communityTracks
    val myTracks: StateFlow<List<GpxTrack>> = trackRepository.myTracks

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("d MMMM, EEEE", Locale("ru"))

    private val _currentDate = MutableStateFlow(dateFormat.format(Date()))
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _uiState = MutableStateFlow(
        NutritionUiState(
            selectedDate = _currentDate.value,
            formattedDateTitle = formatDateTitle(_currentDate.value),
            searchResults = DefaultFoodDatabase.items,
            isHealthConnectAvailable = healthConnectManager.isHealthConnectAvailable(),
            isHealthConnectConnected = accountRepository.isHealthConnectConnected()
        )
    )
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyStats: StateFlow<DailyNutrientStats> = _currentDate
        .flatMapLatest { date -> repository.getDailyStats(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyNutrientStats(date = _currentDate.value, goals = DailyGoals())
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val workouts: StateFlow<List<Workout>> = _currentDate
        .flatMapLatest { date -> repository.getWorkoutsForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _backupInfo = MutableStateFlow(
        CloudBackupInfo(
            userId = account.value.userId,
            backupTimestamp = account.value.lastSyncTimestamp,
            totalFoodEntries = 0,
            totalWaterLogs = 0,
            totalCustomProducts = 0,
            storageSizeKb = 1.2f
        )
    )
    val backupInfo: StateFlow<CloudBackupInfo> = _backupInfo.asStateFlow()

    init {
        // Загрузка избранных продуктов и поддержание актуальности результатов поиска
        viewModelScope.launch {
            repository.getFavoriteProductIds().collect { favList ->
                val favSet = favList.toSet()
                _uiState.update { it.copy(favoriteProductIds = favSet) }
                performSearch(_uiState.value.searchQuery)
            }
        }

        refreshBackupInfo()
    }

    private fun refreshBackupInfo() {
        viewModelScope.launch {
            val (foods, products, water) = repository.getTotalCounts()
            _backupInfo.value = accountRepository.getCloudBackupInfo(foods, products, water)
        }
    }

    private fun formatDateTitle(dateStr: String): String {
        return try {
            val todayStr = dateFormat.format(Date())
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = dateFormat.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 2)
            val tomorrowStr = dateFormat.format(cal.time)

            when (dateStr) {
                todayStr -> "Сегодня"
                yesterdayStr -> "Вчера"
                tomorrowStr -> "Завтра"
                else -> {
                    val parsed = dateFormat.parse(dateStr)
                    if (parsed != null) displayFormat.format(parsed) else dateStr
                }
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    fun changeDate(offsetDays: Int) {
        try {
            val date = dateFormat.parse(_currentDate.value) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, offsetDays)
            val newDateStr = dateFormat.format(cal.time)
            _currentDate.value = newDateStr
            _uiState.update {
                it.copy(
                    selectedDate = newDateStr,
                    formattedDateTitle = formatDateTitle(newDateStr)
                )
            }
        } catch (e: Exception) {
            // Keep current date on parse failure
        }
    }

    fun setDate(dateStr: String) {
        _currentDate.value = dateStr
        _uiState.update {
            it.copy(
                selectedDate = dateStr,
                formattedDateTitle = formatDateTitle(dateStr)
            )
        }
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
        if (tabIndex == 3) {
            refreshBackupInfo()
        }
    }

    fun openAddFoodSheet(mealType: MealType, preselectedProduct: FoodProduct? = null) {
        _uiState.update {
            it.copy(
                showAddFoodSheet = true,
                targetMealForAdd = mealType,
                selectedFoodToLog = preselectedProduct
            )
        }
    }

    fun closeAddFoodSheet() {
        _uiState.update {
            it.copy(
                showAddFoodSheet = false,
                selectedFoodToLog = null
            )
        }
    }

    fun selectFoodToLog(food: FoodProduct) {
        _uiState.update { it.copy(selectedFoodToLog = food) }
    }

    fun clearSelectedFoodToLog() {
        _uiState.update { it.copy(selectedFoodToLog = null) }
    }

    fun openCreateProductSheet() {
        _uiState.update { it.copy(showCreateProductSheet = true) }
    }

    fun closeCreateProductSheet() {
        _uiState.update { it.copy(showCreateProductSheet = false) }
    }

    fun openAddWorkoutSheet() {
        _uiState.update { it.copy(showAddWorkoutSheet = true) }
    }

    fun closeAddWorkoutSheet() {
        _uiState.update { it.copy(showAddWorkoutSheet = false) }
    }

    fun openSettings() {
        _uiState.update { it.copy(showSettings = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(showSettings = false) }
    }

    fun setUseMonet(enabled: Boolean) {
        themeRepository.setUseMonet(enabled)
    }

    fun setPalette(palette: AppThemePalette) {
        themeRepository.setPalette(palette)
    }

    fun setDarkThemeMode(mode: DarkThemeMode) {
        themeRepository.setDarkThemeMode(mode)
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showMessage(msg: String) {
        _uiState.update { it.copy(snackbarMessage = msg) }
    }

    fun performSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearching = true) }
        viewModelScope.launch {
            val results = repository.searchProducts(query, _uiState.value.favoriteProductIds)
            _uiState.update {
                it.copy(
                    searchResults = results,
                    isSearching = false
                )
            }
        }
    }

    fun toggleFavoriteProduct(product: FoodProduct) {
        viewModelScope.launch {
            val targetKey = product.id
            repository.toggleFavorite(targetKey)
            val isNowFav = !_uiState.value.favoriteProductIds.contains(targetKey)
            _uiState.update {
                it.copy(
                    snackbarMessage = if (isNowFav) "Добавлено в избранное: ${product.name}" else "Удалено из избранного: ${product.name}"
                )
            }
            triggerAutoSync()
        }
    }

    fun logFoodItem(
        product: FoodProduct,
        mealType: MealType,
        weightGrams: Float
    ) {
        viewModelScope.launch {
            val calc = product.calculateForWeight(weightGrams)
            val entry = LoggedFood(
                date = _currentDate.value,
                mealType = mealType,
                name = product.name,
                desc = product.desc,
                weightGrams = weightGrams,
                calories = calc.calories,
                protein = calc.protein,
                fat = calc.fat,
                carbs = calc.carbs,
                fiber = calc.fiber,
                sugar = calc.sugar,
                sodium = calc.sodium
            )
            repository.logFood(entry)
            _uiState.update {
                it.copy(
                    showAddFoodSheet = false,
                    selectedFoodToLog = null,
                    snackbarMessage = "Добавлено: ${product.name} (${weightGrams.toInt()} г)"
                )
            }
            triggerAutoSync()
        }
    }

    fun deleteLoggedItem(item: LoggedFood) {
        viewModelScope.launch {
            repository.deleteLoggedFood(item.id)
            _uiState.update { it.copy(snackbarMessage = "Удалено: ${item.name}") }
            triggerAutoSync()
        }
    }

    fun addWaterMl(deltaMl: Int) {
        viewModelScope.launch {
            val currentMl = dailyStats.value.waterConsumedMl
            val newAmount = (currentMl + deltaMl).coerceAtLeast(0)
            repository.updateWater(_currentDate.value, newAmount)
            triggerAutoSync()
        }
    }

    fun saveCustomProduct(product: FoodProduct) {
        viewModelScope.launch {
            repository.saveCustomProduct(product)
            performSearch(_uiState.value.searchQuery)
            _uiState.update {
                it.copy(
                    showCreateProductSheet = false,
                    snackbarMessage = "Продукт «${product.name}» сохранен в вашей базе"
                )
            }
            triggerAutoSync()
        }
    }

    // Тренировки и физическая активность
    fun saveWorkout(
        title: String,
        category: WorkoutCategory,
        durationMinutes: Int,
        caloriesBurned: Float,
        distanceKm: Float,
        notes: String
    ) {
        viewModelScope.launch {
            val workout = Workout(
                date = _currentDate.value,
                title = title,
                category = category,
                durationMinutes = durationMinutes,
                caloriesBurned = caloriesBurned,
                distanceKm = distanceKm,
                notes = notes
            )
            repository.addWorkout(workout)
            _uiState.update {
                it.copy(
                    showAddWorkoutSheet = false,
                    snackbarMessage = "Тренировка «$title» добавлена (-${caloriesBurned.toInt()} ккал)"
                )
            }
            triggerAutoSync()
        }
    }

    fun deleteWorkout(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkout(id)
            _uiState.update { it.copy(snackbarMessage = "Тренировка удалена") }
            triggerAutoSync()
        }
    }

    // GPX Трекинг
    fun addTrack(track: GpxTrack) {
        viewModelScope.launch {
            trackRepository.addTrack(track)
            _uiState.update { it.copy(snackbarMessage = "Маршрут «${track.title}» сохранен в вашей коллекции") }
        }
    }

    fun deleteTrack(trackId: String) {
        viewModelScope.launch {
            trackRepository.deleteTrack(trackId)
            _uiState.update { it.copy(snackbarMessage = "Маршрут удален из коллекции") }
        }
    }

    // Шагомер и интеграция с Health Connect
    fun updateSteps(steps: Int) {
        viewModelScope.launch {
            val burnedKcal = healthConnectManager.calculateBurnedCalories(steps)
            repository.updateDailySteps(
                date = _currentDate.value,
                steps = steps,
                burnedKcal = burnedKcal,
                isHealthConnect = _uiState.value.isHealthConnectConnected
            )
            triggerAutoSync()
        }
    }

    fun connectHealthConnect() {
        accountRepository.setHealthConnectConnected(true)
        _uiState.update { it.copy(isHealthConnectConnected = true) }
        viewModelScope.launch {
            val initialSteps = if (stepSensorManager.isSensorAvailable) {
                stepSensorManager.sensorSteps.value.coerceAtLeast(1500)
            } else {
                1500
            }
            val burned = healthConnectManager.calculateBurnedCalories(initialSteps)
            repository.updateDailySteps(_currentDate.value, initialSteps, burned, true)
            _uiState.update {
                it.copy(snackbarMessage = "Health Connect синхронизирован: %,d шагов".format(initialSteps))
            }
            triggerAutoSync()
        }
    }

    fun toggleHealthConnect(enabled: Boolean) {
        accountRepository.setHealthConnectConnected(enabled)
        _uiState.update { it.copy(isHealthConnectConnected = enabled) }
        if (enabled) {
            connectHealthConnect()
        } else {
            _uiState.update {
                it.copy(snackbarMessage = "Health Connect отключен. Шагомер заблокирован.")
            }
        }
    }

    // Аккаунт и облачная синхронизация
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val result = accountRepository.login(email, pass)
            result.onSuccess { user ->
                _uiState.update { it.copy(snackbarMessage = "Вы успешно вошли в аккаунт ${user.email}") }
                syncNow()
            }.onFailure { err ->
                _uiState.update { it.copy(snackbarMessage = err.message ?: "Ошибка авторизации") }
            }
        }
    }

    fun register(email: String, pass: String, name: String) {
        viewModelScope.launch {
            val result = accountRepository.register(email, pass, name)
            result.onSuccess { user ->
                _uiState.update { it.copy(snackbarMessage = "Аккаунт ${user.email} успешно создан!") }
                syncNow()
            }.onFailure { err ->
                _uiState.update { it.copy(snackbarMessage = err.message ?: "Ошибка регистрации") }
            }
        }
    }

    fun logout() {
        accountRepository.logout()
        _uiState.update { it.copy(snackbarMessage = "Вы вышли из аккаунта. Включен автономный режим.") }
    }

    fun toggleAutoSync(enabled: Boolean) {
        accountRepository.setAutoSync(enabled)
        _uiState.update {
            it.copy(
                snackbarMessage = if (enabled) "Автосинхронизация включена" else "Автосинхронизация отключена"
            )
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            val (foods, products, water) = repository.getTotalCounts()
            val result = accountRepository.syncNow(foods, products, water)
            result.onSuccess { user ->
                refreshBackupInfo()
                _uiState.update {
                    it.copy(snackbarMessage = "Дневник синхронизирован с аккаунтом ${user.email}")
                }
            }.onFailure { err ->
                _uiState.update { it.copy(snackbarMessage = err.message ?: "Ошибка синхронизации") }
            }
        }
    }

    fun restoreBackup() {
        viewModelScope.launch {
            syncNow()
            _uiState.update {
                it.copy(snackbarMessage = "Данные успешно восстановлены из облака аккаунта")
            }
        }
    }

    private fun triggerAutoSync() {
        viewModelScope.launch {
            val (foods, products, water) = repository.getTotalCounts()
            accountRepository.triggerAutoSyncIfEnabled(foods, products, water)
            refreshBackupInfo()
        }
    }
}
