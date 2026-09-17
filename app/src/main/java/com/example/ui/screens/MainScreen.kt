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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DailyNutrientStats
import com.example.data.model.FoodProduct
import com.example.data.model.LoggedFood
import com.example.data.model.MealType
import com.example.data.model.SyncStatus
import com.example.data.model.Workout
import com.example.ui.components.AddFoodBottomSheet
import com.example.ui.components.AddWorkoutBottomSheet
import com.example.ui.components.CalorieSummaryCard
import com.example.ui.components.CreateProductDialog
import com.example.ui.components.MealSectionCard
import com.example.ui.components.StepTrackerCard
import com.example.ui.components.WaterTrackerCard
import com.example.ui.components.WorkoutTrackerCard
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

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "tether",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.openSettings() }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    // Плашка статуса синхронизации в верхней панели
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (account.syncStatus) {
                            SyncStatus.SYNCED -> PrimaryGreen.copy(alpha = 0.12f)
                            SyncStatus.SYNCING -> Color(0xFF2196F3).copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { viewModel.setActiveTab(3) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(
                                        when (account.syncStatus) {
                                            SyncStatus.SYNCED -> PrimaryGreen
                                            SyncStatus.SYNCING -> Color(0xFF1976D2)
                                            else -> MaterialTheme.colorScheme.outline
                                        },
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (account.isLoggedIn) "синхронизировано" else "офлайн",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
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
                    icon = { Icon(Icons.Default.Map, contentDescription = "Трекинг") },
                    label = { Text("Трекинг") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = uiState.activeTab == 3,
                    onClick = { viewModel.setActiveTab(3) },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Аккаунт") },
                    label = { Text("Аккаунт") },
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
                    workouts = workouts,
                    dateTitle = uiState.formattedDateTitle,
                    isHealthConnectAvailable = uiState.isHealthConnectAvailable,
                    isHealthConnectConnected = uiState.isHealthConnectConnected,
                    onPrevDay = { viewModel.changeDate(-1) },
                    onNextDay = { viewModel.changeDate(1) },
                    onAddFood = { meal -> viewModel.openAddFoodSheet(meal) },
                    onDeleteFood = { item -> viewModel.deleteLoggedItem(item) },
                    onAddWater = { delta -> viewModel.addWaterMl(delta) },
                    onAddWorkoutClick = { viewModel.openAddWorkoutSheet() },
                    onDeleteWorkout = { id -> viewModel.deleteWorkout(id) },
                    onUpdateSteps = { steps -> viewModel.updateSteps(steps) },
                    onConnectHealthConnect = { viewModel.setActiveTab(3) }
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

                2 -> TrackingScreen(
                    communityTracks = communityTracks,
                    myTracks = myTracks,
                    onAddTrack = { track -> viewModel.addTrack(track) },
                    onDeleteTrack = { id -> viewModel.deleteTrack(id) }
                )

                3 -> AccountScreen(
                    account = account,
                    backupInfo = backupInfo,
                    isHealthConnectConnected = uiState.isHealthConnectConnected,
                    isHealthConnectAvailable = uiState.isHealthConnectAvailable,
                    onToggleHealthConnect = { viewModel.toggleHealthConnect(it) },
                    onLogin = { email, pass -> viewModel.login(email, pass) },
                    onRegister = { email, pass, name -> viewModel.register(email, pass, name) },
                    onLogout = { viewModel.logout() },
                    onSyncNow = { viewModel.syncNow() },
                    onToggleAutoSync = { viewModel.toggleAutoSync(it) },
                    onRestoreBackup = { viewModel.restoreBackup() }
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
            isHealthConnectConnected = uiState.isHealthConnectConnected,
            isHealthConnectAvailable = uiState.isHealthConnectAvailable,
            onToggleMonet = { viewModel.setUseMonet(it) },
            onSelectPalette = { viewModel.setPalette(it) },
            onSelectDarkThemeMode = { viewModel.setDarkThemeMode(it) },
            onToggleHealthConnect = { viewModel.toggleHealthConnect(it) },
            onBack = { viewModel.closeSettings() }
        )
    }
}

@Composable
private fun DiaryTabContent(
    stats: DailyNutrientStats,
    workouts: List<Workout>,
    dateTitle: String,
    isHealthConnectAvailable: Boolean,
    isHealthConnectConnected: Boolean,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onAddFood: (MealType) -> Unit,
    onDeleteFood: (LoggedFood) -> Unit,
    onAddWater: (Int) -> Unit,
    onAddWorkoutClick: () -> Unit,
    onDeleteWorkout: (Long) -> Unit,
    onUpdateSteps: (Int) -> Unit,
    onConnectHealthConnect: () -> Unit
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
                onPreviousDay = onPrevDay,
                onNextDay = onNextDay
            )
        }

        // Карточка шагомера Health Connect
        item {
            StepTrackerCard(
                steps = stats.stepsCount,
                burnedCalories = stats.stepsBurnedCalories,
                goalSteps = 10000,
                isHealthConnectAvailable = isHealthConnectAvailable,
                isHealthConnectConnected = isHealthConnectConnected,
                onManualStepsUpdate = onUpdateSteps,
                onConnectHealthConnect = onConnectHealthConnect
            )
        }

        // Карточка тренировок
        item {
            WorkoutTrackerCard(
                workouts = workouts,
                onAddWorkoutClick = onAddWorkoutClick,
                onDeleteWorkout = onDeleteWorkout
            )
        }

        item {
            WaterTrackerCard(
                consumedMl = stats.waterConsumedMl,
                goalMl = 2000,
                onAddWater = onAddWater
            )
        }

        item {
            Text(
                text = "Приёмы пищи",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        items(MealType.values()) { mealType ->
            val itemsForMeal = stats.mealBreakdown[mealType] ?: emptyList()
            MealSectionCard(
                mealType = mealType,
                items = itemsForMeal,
                onAddClick = { onAddFood(mealType) },
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
                text = if (searchQuery.isBlank()) "Каталог продуктов (избранные вверху)" else "Найдено (${searchResults.size})",
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (product.isFavorite) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "В избранном",
                                    tint = Color(0xFFE91E63),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
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
