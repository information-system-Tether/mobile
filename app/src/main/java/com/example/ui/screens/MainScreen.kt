package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.app.DatePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DailyNutrientStats
import com.example.data.model.FoodProduct
import com.example.data.model.LoggedFood
import com.example.data.model.MealType
import com.example.data.model.SyncStatus
import com.example.data.model.Workout
import com.example.ui.components.ActiveWorkoutSessionDialog
import com.example.ui.components.AddFoodBottomSheet
import com.example.ui.components.AddWorkoutBottomSheet
import com.example.ui.components.CalorieSummaryCard
import com.example.ui.components.CreateProductDialog
import com.example.ui.components.MealSectionCard
import com.example.ui.components.StepTrackerCard
import com.example.ui.components.UnifiedFoodSectionCard
import com.example.ui.components.WaterTrackerCard
import com.example.ui.components.WorkoutTrackerCard
import com.example.ui.screens.WorkoutScreen
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.QueryStats
import com.example.R
import com.example.ui.screens.StatsAndAdviceScreen
import com.example.ui.theme.PrimaryGreen
import com.example.ui.viewmodel.NutritionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: NutritionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dailyStats by viewModel.dailyStats.collectAsStateWithLifecycle()
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val account by viewModel.account.collectAsStateWithLifecycle()
    val backupInfo by viewModel.backupInfo.collectAsStateWithLifecycle()
    val communityTracks by viewModel.communityTracks.collectAsStateWithLifecycle()
    val myTracks by viewModel.myTracks.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val catalogExercises by viewModel.catalogExercises.collectAsStateWithLifecycle()
    val workoutHistory by viewModel.workoutHistory.collectAsStateWithLifecycle()
    val personalRecords by viewModel.personalRecords.collectAsStateWithLifecycle()
    val weeklyAnalytics by viewModel.weeklyAnalytics.collectAsStateWithLifecycle()
    val smartInsights by viewModel.smartInsights.collectAsStateWithLifecycle()
    val healthProfile by viewModel.healthProfile.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val cal = Calendar.getInstance()
        try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(uiState.selectedDate)
            if (parsed != null) cal.time = parsed
        } catch (_: Exception) {}

        val dpd = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val sel = Calendar.getInstance()
                sel.set(year, month, dayOfMonth)
                val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(sel.time)
                viewModel.setDate(formatted)
                showDatePicker = false
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
            setOnDismissListener { showDatePicker = false }
        }
        DisposableEffect(Unit) {
            dpd.show()
            onDispose {
                dpd.dismiss()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.tether_logo_full),
                            contentDescription = "tether",
                            modifier = Modifier.height(28.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openAccount() }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Аккаунт",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.openSettings() }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    icon = { Icon(Icons.Default.EventNote, contentDescription = "Дневник") },
                    label = { Text("Дневник") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    icon = { Icon(Icons.Default.LocalGroceryStore, contentDescription = "Продукты") },
                    label = { Text("Продукты") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = uiState.activeTab == 2,
                    onClick = { viewModel.setActiveTab(2) },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Тренировки") },
                    label = { Text("Тренировки") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = uiState.activeTab == 3,
                    onClick = { viewModel.setActiveTab(3) },
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Трекинг") },
                    label = { Text("Трекинг") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = uiState.activeTab == 4,
                    onClick = { viewModel.setActiveTab(4) },
                    icon = { Icon(Icons.Default.QueryStats, contentDescription = "Советы") },
                    label = { Text("Советы") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                    )
                )
            }
        },
        floatingActionButton = {
            if (uiState.activeTab == 0) {
                FloatingActionButton(
                    onClick = { viewModel.openAddFoodSheet(MealType.LUNCH) },
                    containerColor = PrimaryGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить продукт")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.activeTab) {
                0 -> DiaryTabContent(
                    stats = dailyStats,
                    dateTitle = uiState.formattedDateTitle,
                    canGoForward = viewModel.canGoForward(),
                    onPrevDay = { viewModel.changeDate(-1) },
                    onNextDay = { viewModel.changeDate(1) },
                    onOpenDatePicker = { showDatePicker = true },
                    onAddFood = { meal -> viewModel.openAddFoodSheet(meal) },
                    onDeleteFood = { item -> viewModel.deleteLoggedItem(item) },
                    onAddWater = { delta -> viewModel.addWaterMl(delta) },
                    onUpdateStepGoal = { newGoal -> viewModel.updateStepGoal(newGoal) }
                )

                1 -> ProductsDatabaseTabContent(
                    searchQuery = uiState.searchQuery,
                    searchResults = uiState.searchResults,
                    onSearchQueryChange = { viewModel.performSearch(it) },
                    onSelectProduct = { prod ->
                        viewModel.openAddFoodSheet(MealType.LUNCH, prod)
                    },
                    onToggleFavorite = { prod -> viewModel.toggleFavoriteProduct(prod) },
                    onOpenCreateProduct = { viewModel.openCreateProductSheet() }
                )

                2 -> WorkoutScreen(
                    routines = routines,
                    catalogExercises = catalogExercises,
                    sessionsHistory = workoutHistory,
                    personalRecords = personalRecords,
                    activeSession = uiState.activeWorkoutSession,
                    onStartEmptyWorkout = { viewModel.startEmptyWorkout() },
                    onStartRoutine = { routine -> viewModel.startRoutine(routine) },
                    onResumeActiveWorkout = { viewModel.resumeActiveWorkout() },
                    onDeleteSession = { id -> viewModel.deleteWorkoutSession(id) },
                    onCreateRoutine = { name, muscles, exList ->
                        viewModel.createCustomRoutine(name, muscles, exList)
                    },
                    onCreateExercise = { name, muscle, eq, desc ->
                        viewModel.createCustomExercise(name, muscle, eq, desc)
                    }
                )

                3 -> TrackingScreen(
                    communityTracks = communityTracks,
                    myTracks = myTracks,
                    onAddTrack = { viewModel.addTrack(it) },
                    onDeleteTrack = { viewModel.deleteTrack(it) },
                    onPublishToCommunity = { viewModel.publishTrackToCommunity(it) }
                )

                4 -> StatsAndAdviceScreen(
                    dailyStats = dailyStats,
                    weeklyAnalytics = weeklyAnalytics,
                    smartInsights = smartInsights,
                    healthProfile = healthProfile
                )
            }
        }
    }

    // Modal Sheets & Dialogs
    if (uiState.showAddFoodSheet) {
        AddFoodBottomSheet(
            mealType = uiState.targetMealForAdd,
            selectedProduct = uiState.selectedFoodToLog,
            searchQuery = uiState.searchQuery,
            searchResults = uiState.searchResults,
            isSearching = uiState.isSearching,
            onSearchQueryChange = { viewModel.performSearch(it) },
            onSelectProduct = { viewModel.selectFoodToLog(it) },
            onClearSelectedProduct = { viewModel.clearSelectedFoodToLog() },
            onLogFood = { product, meal, weight ->
                viewModel.logFoodItem(product, meal, weight)
            },
            onToggleFavorite = { prod -> viewModel.toggleFavoriteProduct(prod) },
            onOpenCreateProduct = { viewModel.openCreateProductSheet() },
            onDismiss = { viewModel.closeAddFoodSheet() }
        )
    }

    if (uiState.showAddWorkoutSheet) {
        AddWorkoutBottomSheet(
            onSaveWorkout = { title, cat, duration, calories, distance, notes ->
                viewModel.saveWorkout(title, cat, duration, calories, distance, notes)
            },
            onDismiss = { viewModel.closeAddWorkoutSheet() }
        )
    }

    if (uiState.showCreateProductSheet) {
        CreateProductDialog(
            onSave = { newProd -> viewModel.saveCustomProduct(newProd) },
            onDismiss = { viewModel.closeCreateProductSheet() }
        )
    }

    if (uiState.showSettings) {
        val themeConfig by viewModel.themeConfig.collectAsStateWithLifecycle()
        SettingsScreen(
            themeConfig = themeConfig,
            authBaseUrl = viewModel.getAuthBaseUrl(),
            diaryBaseUrl = viewModel.getDiaryBaseUrl(),
            onToggleMonet = { viewModel.setUseMonet(it) },
            onSelectPalette = { viewModel.setPalette(it) },
            onSelectDarkThemeMode = { viewModel.setDarkThemeMode(it) },
            onSaveServerUrls = { authUrl, diaryUrl -> viewModel.setServerUrls(authUrl, diaryUrl) },
            onBack = { viewModel.closeSettings() }
        )
    }

    if (uiState.showAccount) {
        val healthProfile by viewModel.healthProfile.collectAsStateWithLifecycle()
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AccountScreen(
                account = account,
                healthProfile = healthProfile,
                onUpdateHealthProfile = { gender, age, weight, heightMeters, actLvl, goal, steps, waterMl ->
                    viewModel.updateHealthProfile(gender, age, weight, heightMeters, actLvl, goal, steps, waterMl)
                },
                authBaseUrl = viewModel.getAuthBaseUrl(),
                diaryBaseUrl = viewModel.getDiaryBaseUrl(),
                onLogin = { email, pass -> viewModel.login(email, pass) },
                onRegister = { login, pass, email -> viewModel.register(login, pass, email) },
                onLogout = { viewModel.logout() },
                onSyncNow = { viewModel.syncNow() },
                onToggleAutoSync = { viewModel.toggleAutoSync(it) },
                onSaveServerUrls = { authUrl, diaryUrl -> viewModel.setServerUrls(authUrl, diaryUrl) },
                onBack = { viewModel.closeAccount() }
            )
        }
    }

    uiState.activeWorkoutSession?.let { activeSession ->
        ActiveWorkoutSessionDialog(
            initialSession = activeSession,
            catalogExercises = catalogExercises,
            onCreateExercise = { name, muscle, eq, desc ->
                viewModel.createCustomExercise(name, muscle, eq, desc)
            },
            onFinishSession = { finishedSession ->
                viewModel.finishWorkoutSession(finishedSession)
            },
            onCancelSession = {
                viewModel.cancelActiveWorkout()
            }
        )
    }
}

@Composable
private fun DiaryTabContent(
    stats: DailyNutrientStats,
    dateTitle: String,
    canGoForward: Boolean,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onOpenDatePicker: () -> Unit,
    onAddFood: (MealType) -> Unit,
    onDeleteFood: (LoggedFood) -> Unit,
    onAddWater: (Int) -> Unit,
    onUpdateStepGoal: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CalorieSummaryCard(
                stats = stats,
                dateTitle = dateTitle,
                canGoForward = canGoForward,
                onPreviousDay = onPrevDay,
                onNextDay = onNextDay,
                onOpenDatePicker = onOpenDatePicker
            )
        }

        // Карточка шагомера с редактированием цели шагов
        item {
            StepTrackerCard(
                steps = stats.stepsCount,
                burnedCalories = stats.stepsBurnedCalories,
                goalSteps = stats.goals.stepGoal,
                onGoalStepsUpdate = onUpdateStepGoal
            )
        }

        item {
            WaterTrackerCard(
                consumedMl = stats.waterConsumedMl,
                goalMl = stats.goals.waterGoalMl,
                onAddWater = onAddWater
            )
        }

        // Единый список всех продуктов за день (без деления на приемы пищи)
        item {
            UnifiedFoodSectionCard(
                items = stats.allLoggedFoods,
                onAddClick = { onAddFood(MealType.LUNCH) },
                onDeleteItem = onDeleteFood
            )
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun ProductsDatabaseTabContent(
    searchQuery: String,
    searchResults: List<FoodProduct>,
    onSearchQueryChange: (String) -> Unit,
    onSelectProduct: (FoodProduct) -> Unit,
    onToggleFavorite: (FoodProduct) -> Unit,
    onOpenCreateProduct: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Поиск продукта") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        focusedLabelColor = PrimaryGreen
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onOpenCreateProduct,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Создать продукт",
                        tint = Color.White
                    )
                }
            }
        }

        item {
            Text(
                text = if (searchQuery.isBlank()) "Каталог продуктов" else "Найдено (${searchResults.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(searchResults) { product ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectProduct(product) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (product.desc.isNotBlank()) {
                            Text(
                                text = product.desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Б: ${product.proteinPer100g}г • Ж: ${product.fatPer100g}г • У: ${product.carbsPer100g}г (на 100г)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${product.caloriesPer100g.toInt()} ккал",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = PrimaryGreen
                            )
                            Text(
                                text = "на 100 г",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { onToggleFavorite(product) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Избранное",
                                tint = if (product.isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
