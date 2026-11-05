package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.health.StepSensorManager
import com.example.data.model.CloudBackupInfo
import com.example.data.model.DailyGoals
import com.example.data.model.DailyNutrientStats
import com.example.data.model.Equipment
import com.example.data.model.Exercise
import com.example.data.model.FoodProduct
import com.example.data.model.GpxTrack
import com.example.data.model.MuscleGroup
import com.example.data.model.LoggedExercise
import com.example.data.model.LoggedFood
import com.example.data.model.LoggedSet
import com.example.data.model.MealType
import com.example.data.model.PersonalRecord
import com.example.data.model.SetKind
import com.example.data.model.UserAccount
import com.example.data.model.Workout
import com.example.data.model.SmartInsight
import com.example.data.model.SmartInsightsEngine
import com.example.data.model.WeeklyAnalytics
import kotlinx.coroutines.flow.combine
import com.example.data.model.WorkoutCategory
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSession
import com.example.data.repository.AccountRepository
import com.example.data.repository.AppThemeConfig
import com.example.data.repository.AppThemePalette
import com.example.data.repository.DarkThemeMode
import com.example.data.repository.DefaultFoodDatabase
import com.example.data.repository.NutritionRepository
import com.example.data.repository.ThemeRepository
import com.example.data.repository.TrackRepository
import com.example.data.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class NutritionUiState(
    val selectedDate: String,
    val formattedDateTitle: String,
    val activeTab: Int = 0, // 0 = Diary, 1 = Products, 2 = Workouts, 3 = Account
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
    val showAccount: Boolean = false,
    val isStepTrackingActive: Boolean = true,
    val activeWorkoutSession: WorkoutSession? = null,
    val snackbarMessage: String? = null
)

class NutritionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = NutritionRepository(db)
    private val accountRepository = AccountRepository(application)
    private val trackRepository = TrackRepository(application)
    private val themeRepository = ThemeRepository(application)
    private val workoutRepository = WorkoutRepository(application)
    val stepSensorManager = StepSensorManager(application)

    val account: StateFlow<UserAccount> = accountRepository.accountState
    val healthProfile: StateFlow<com.example.data.api.tether.HealthProfileResponse> = accountRepository.healthProfile
    val themeConfig: StateFlow<AppThemeConfig> = themeRepository.themeConfig
    val communityTracks: StateFlow<List<GpxTrack>> = trackRepository.communityTracks
    val myTracks: StateFlow<List<GpxTrack>> = trackRepository.myTracks

    val routines: StateFlow<List<WorkoutRoutine>> = workoutRepository.getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val catalogExercises: StateFlow<List<Exercise>> = workoutRepository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), workoutRepository.defaultCatalogExercises)

    val workoutHistory: StateFlow<List<WorkoutSession>> = workoutRepository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personalRecords: StateFlow<List<PersonalRecord>> = workoutRepository.getPersonalRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("d MMMM, EEEE", Locale("ru"))

    private val _currentDate = MutableStateFlow(dateFormat.format(Date()))
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _uiState = MutableStateFlow(
        NutritionUiState(
            selectedDate = _currentDate.value,
            formattedDateTitle = formatDateTitle(_currentDate.value),
            searchResults = DefaultFoodDatabase.items,
            isStepTrackingActive = true
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val weeklyAnalytics: StateFlow<WeeklyAnalytics> = _currentDate
        .flatMapLatest { date -> repository.getWeeklyAnalytics(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeeklyAnalytics(
                days = emptyList(),
                averageCalories = 0f,
                averageProtein = 0f,
                averageFat = 0f,
                averageCarbs = 0f,
                averageWaterMl = 0,
                averageSteps = 0,
                totalBurnedCalories = 0f,
                targetCalories = 2000f,
                targetWaterMl = 2000,
                targetSteps = 10000
            )
        )

    val smartInsights: StateFlow<List<SmartInsight>> = combine(
        healthProfile,
        dailyStats,
        weeklyAnalytics,
        workouts
    ) { hp, stats, weekly, wkList ->
        SmartInsightsEngine.generateInsights(hp, stats, weekly, wkList.size)
    }.stateIn(
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
        // Запуск фоновой службы шагомера
        try {
            StepSensorManager.startStepService(application)
            stepSensorManager.registerListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Синхронизация шагов сенсора с Room БД при изменении
        viewModelScope.launch {
            stepSensorManager.sensorSteps.collect { steps ->
                if (steps > 0 && _currentDate.value == dateFormat.format(Date())) {
                    val burned = StepSensorManager.calculateBurnedCalories(steps)
                    repository.updateDailySteps(_currentDate.value, steps, burned, false)
                }
            }
        }

        // Загрузка избранного
        viewModelScope.launch {
            repository.getFavoriteProductIds().collect { favList ->
                val favSet = favList.toSet()
                _uiState.update { it.copy(favoriteProductIds = favSet) }
                performSearch(_uiState.value.searchQuery)
            }
        }

        refreshBackupInfo()

        // Автономная фоновая синхронизация каждые 5 минут
        viewModelScope.launch {
            while (isActive) {
                delay(5 * 60 * 1000L)
                if (account.value.isLoggedIn && account.value.autoSyncEnabled) {
                    val (foods, products, water) = repository.getTotalCounts()
                    accountRepository.syncNow(foods, products, water)
                    refreshBackupInfo()
                }
            }
        }
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

    fun canGoForward(): Boolean {
        val todayStr = dateFormat.format(Date())
        return _currentDate.value < todayStr
    }

    fun changeDate(offsetDays: Int) {
        try {
            val todayStr = dateFormat.format(Date())
            if (offsetDays > 0 && _currentDate.value >= todayStr) {
                return
            }
            val date = dateFormat.parse(_currentDate.value) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, offsetDays)
            val newDateStr = dateFormat.format(cal.time)
            if (offsetDays > 0 && newDateStr > todayStr) {
                return
            }
            _currentDate.value = newDateStr
            _uiState.update {
                it.copy(
                    selectedDate = newDateStr,
                    formattedDateTitle = formatDateTitle(newDateStr)
                )
            }
        } catch (e: Exception) {
            // Keep current date
        }
    }

    fun setDate(dateStr: String) {
        val todayStr = dateFormat.format(Date())
        val validDateStr = if (dateStr > todayStr) todayStr else dateStr
        _currentDate.value = validDateStr
        _uiState.update {
            it.copy(
                selectedDate = validDateStr,
                formattedDateTitle = formatDateTitle(validDateStr)
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

    fun openAccount() {
        _uiState.update { it.copy(showAccount = true) }
        refreshBackupInfo()
    }

    fun closeAccount() {
        _uiState.update { it.copy(showAccount = false) }
    }

    fun publishTrackToCommunity(track: GpxTrack) {
        viewModelScope.launch {
            trackRepository.publishToCommunity(track)
            _uiState.update { it.copy(snackbarMessage = "Трек «${track.title}» опубликован в комьюнити!") }
        }
    }

    fun setUseMonet(enabled: Boolean) {
        themeRepository.setUseMonet(enabled)
    }

    fun setThemePalette(palette: AppThemePalette) {
        themeRepository.setPalette(palette)
    }

    fun setDarkThemeMode(mode: DarkThemeMode) {
        themeRepository.setDarkThemeMode(mode)
    }

    fun performSearch(query: String) {
        val q = query.trim()
        _uiState.update { it.copy(searchQuery = query, isSearching = true) }

        viewModelScope.launch {
            val results = repository.searchProducts(q, _uiState.value.favoriteProductIds)
            _uiState.update {
                it.copy(
                    searchResults = results,
                    isSearching = false
                )
            }
        }
    }

    fun logFoodItem(product: FoodProduct, mealType: MealType, weightGrams: Float) {
        viewModelScope.launch {
            val ratio = weightGrams / 100f
            val item = LoggedFood(
                date = _currentDate.value,
                mealType = mealType,
                name = product.name,
                desc = product.desc,
                weightGrams = weightGrams,
                calories = product.caloriesPer100g * ratio,
                protein = product.proteinPer100g * ratio,
                fat = product.fatPer100g * ratio,
                carbs = product.carbsPer100g * ratio,
                fiber = product.fiberPer100g * ratio,
                sugar = product.sugarPer100g * ratio,
                sodium = product.sodiumPer100g * ratio
            )
            repository.logFood(item)
            closeAddFoodSheet()
            _uiState.update { it.copy(snackbarMessage = "«${product.name}» добавлено в дневник") }
            triggerAutoSync()
        }
    }

    fun deleteLoggedItem(item: LoggedFood) {
        viewModelScope.launch {
            repository.deleteLoggedFood(item.id)
            _uiState.update { it.copy(snackbarMessage = "«${item.name}» удалено") }
            triggerAutoSync()
        }
    }

    fun addWaterMl(deltaMl: Int) {
        viewModelScope.launch {
            val current = dailyStats.value.waterConsumedMl
            repository.updateWater(_currentDate.value, current + deltaMl)
            triggerAutoSync()
        }
    }

    fun updateDailyGoals(goals: DailyGoals) {
        viewModelScope.launch {
            repository.updateGoals(goals)
            _uiState.update { it.copy(snackbarMessage = "Цели успешно сохранены") }
            triggerAutoSync()
        }
    }

    fun createCustomProduct(product: FoodProduct) {
        viewModelScope.launch {
            repository.saveCustomProduct(product)
            closeCreateProductSheet()
            performSearch(_uiState.value.searchQuery)
            _uiState.update { it.copy(snackbarMessage = "Продукт «${product.name}» сохранен") }
            triggerAutoSync()
        }
    }

    fun saveCustomProduct(product: FoodProduct) = createCustomProduct(product)

    fun toggleFavoriteProduct(product: FoodProduct) {
        viewModelScope.launch {
            repository.toggleFavorite(product.id)
            performSearch(_uiState.value.searchQuery)
        }
    }

    fun setPalette(palette: AppThemePalette) = setThemePalette(palette)

    // Тренировки (переработанные по GymMane)
    fun startEmptyWorkout() {
        val emptySession = WorkoutSession(
            date = _currentDate.value,
            title = "Свободная тренировка",
            exercises = emptyList()
        )
        _uiState.update { it.copy(activeWorkoutSession = emptySession) }
    }

    fun startRoutine(routine: WorkoutRoutine) {
        val routineSession = WorkoutSession(
            date = _currentDate.value,
            title = routine.name,
            routineId = routine.id,
            exercises = routine.exercises.map { ex ->
                LoggedExercise(
                    exerciseId = ex.id,
                    exerciseName = ex.name,
                    muscleGroup = ex.primaryMuscle,
                    sets = listOf(
                        LoggedSet(setNumber = 1, kind = SetKind.NORMAL, weightKg = 30f, reps = 10, isCompleted = false),
                        LoggedSet(setNumber = 2, kind = SetKind.NORMAL, weightKg = 30f, reps = 10, isCompleted = false),
                        LoggedSet(setNumber = 3, kind = SetKind.NORMAL, weightKg = 30f, reps = 10, isCompleted = false)
                    )
                )
            }
        )
        _uiState.update { it.copy(activeWorkoutSession = routineSession) }
    }

    fun resumeActiveWorkout() {
        // activeWorkoutSession remains in state, dialog is re-opened in UI
    }

    fun cancelActiveWorkout() {
        _uiState.update { it.copy(activeWorkoutSession = null) }
    }

    fun finishWorkoutSession(session: WorkoutSession) {
        viewModelScope.launch {
            workoutRepository.saveWorkoutSession(session)
            _uiState.update {
                it.copy(
                    activeWorkoutSession = null,
                    snackbarMessage = "Тренировка «${session.title}» сохранена! Сожжено %,d ккал".format(session.caloriesBurned.toInt())
                )
            }
            triggerAutoSync()
        }
    }

    fun deleteWorkoutSession(id: Long) {
        viewModelScope.launch {
            workoutRepository.deleteWorkoutSession(id)
            _uiState.update { it.copy(snackbarMessage = "Запись тренировки удалена") }
        }
    }

    fun createCustomRoutine(name: String, targetMuscles: String, exercises: List<Exercise>) {
        viewModelScope.launch {
            val newRoutine = WorkoutRoutine(
                id = "routine_" + UUID.randomUUID().toString().take(8),
                name = name,
                targetMuscles = targetMuscles,
                defaultRestSeconds = 90,
                exercises = exercises
            )
            workoutRepository.saveCustomRoutine(newRoutine)
            _uiState.update { it.copy(snackbarMessage = "Программа «$name» создана") }
        }
    }

    fun createCustomExercise(
        name: String,
        muscle: MuscleGroup,
        equipment: Equipment,
        description: String = ""
    ) {
        viewModelScope.launch {
            workoutRepository.addCustomExercise(name, muscle, equipment, description)
            _uiState.update { it.copy(snackbarMessage = "Упражнение «$name» добавлено в каталог!") }
        }
    }

    fun addManualWorkout(
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
            closeAddWorkoutSheet()
            _uiState.update { it.copy(snackbarMessage = "Тренировка «$title» сохранена") }
            triggerAutoSync()
        }
    }

    fun saveWorkout(
        title: String,
        category: WorkoutCategory,
        durationMinutes: Int,
        burnedCalories: Float,
        distanceKm: Float? = null,
        notes: String? = null
    ) = addManualWorkout(title, category, durationMinutes, burnedCalories, distanceKm ?: 0f, notes ?: "")

    fun deleteWorkout(workoutId: Long) {
        viewModelScope.launch {
            repository.deleteWorkout(workoutId)
            _uiState.update { it.copy(snackbarMessage = "Тренировка удалена") }
            triggerAutoSync()
        }
    }

    // GPX треки
    fun addTrack(track: GpxTrack) {
        viewModelScope.launch {
            trackRepository.addTrack(track)
            _uiState.update { it.copy(snackbarMessage = "Маршрут «${track.title}» сохранен") }
        }
    }

    fun deleteTrack(trackId: String) {
        viewModelScope.launch {
            trackRepository.deleteTrack(trackId)
            _uiState.update { it.copy(snackbarMessage = "Маршрут удален") }
        }
    }

    // Шагомер
    fun updateSteps(steps: Int) {
        viewModelScope.launch {
            stepSensorManager.setManualSteps(steps)
            val burnedKcal = StepSensorManager.calculateBurnedCalories(steps)
            repository.updateDailySteps(
                date = _currentDate.value,
                steps = steps,
                burnedKcal = burnedKcal,
                isHealthConnect = false
            )
            triggerAutoSync()
        }
    }

    fun updateStepGoal(newGoal: Int) {
        viewModelScope.launch {
            repository.updateStepGoal(newGoal)
            _uiState.update { it.copy(snackbarMessage = "Цель шагов установлена: %,d".format(newGoal)) }
            triggerAutoSync()
        }
    }

    fun addManualSteps(delta: Int) {
        val current = dailyStats.value.stepsCount
        updateSteps(current + delta)
    }

    // Аккаунт и серверная синхронизация Tether backend
    fun getAuthBaseUrl(): String = accountRepository.getAuthBaseUrl()
    fun getDiaryBaseUrl(): String = accountRepository.getDiaryBaseUrl()

    fun setServerUrls(authUrl: String, diaryUrl: String) {
        accountRepository.setServerUrls(authUrl, diaryUrl)
        _uiState.update { it.copy(snackbarMessage = "Адреса серверов обновлены") }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val result = accountRepository.login(email, pass)
            result.onSuccess { user ->
                _uiState.update { it.copy(snackbarMessage = "Вход выполнен: ${user.displayName}") }
            }.onFailure { err ->
                _uiState.update { it.copy(snackbarMessage = err.message ?: "Ошибка авторизации") }
            }
        }
    }

    fun register(login: String, pass: String, email: String) {
        viewModelScope.launch {
            val result = accountRepository.register(login, pass, email)
            result.onSuccess { user ->
                _uiState.update { it.copy(snackbarMessage = "Аккаунт «${user.displayName}» успешно создан") }
            }.onFailure { err ->
                _uiState.update { it.copy(snackbarMessage = err.message ?: "Ошибка регистрации") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            accountRepository.logout()
            _uiState.update { it.copy(snackbarMessage = "Вы вышли из системы") }
        }
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
            result.onSuccess {
                refreshBackupInfo()
                _uiState.update {
                    it.copy(snackbarMessage = "Синхронизация с сервером успешно завершена")
                }
            }.onFailure { err ->
                _uiState.update { it.copy(snackbarMessage = err.message ?: "Ошибка синхронизации с сервером") }
            }
        }
    }

    fun updateHealthProfile(
        gender: String,
        age: Int,
        weight: Float,
        heightMeters: Float,
        activityLevel: Int,
        targetGoal: Int,
        targetSteps: Int,
        targetWaterMl: Int
    ) {
        viewModelScope.launch {
            val result = accountRepository.updateHealthProfile(
                gender = gender,
                age = age,
                weight = weight,
                heightMeters = heightMeters,
                activityLevel = activityLevel,
                targetGoal = targetGoal,
                targetSteps = targetSteps,
                targetWaterMl = targetWaterMl
            )
            result.onSuccess {
                _uiState.update { it.copy(snackbarMessage = "Параметры и суточные нормы обновлены") }
            }.onFailure { err ->
                _uiState.update { it.copy(snackbarMessage = err.message ?: "Ошибка сохранения параметров") }
            }
        }
    }

    fun restoreBackup() {
        syncNow()
    }

    private fun triggerAutoSync() {
        viewModelScope.launch {
            val (foods, products, water) = repository.getTotalCounts()
            accountRepository.triggerAutoSyncIfEnabled(foods, products, water)
            refreshBackupInfo()
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
