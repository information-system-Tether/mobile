package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.tether.HealthProfileResponse
import com.example.data.model.SyncStatus
import com.example.data.model.UserAccount
import com.example.ui.theme.NutrientCarbs
import com.example.ui.theme.NutrientFat
import com.example.ui.theme.NutrientProtein
import com.example.ui.theme.NutrientWater
import com.example.ui.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    account: UserAccount,
    healthProfile: HealthProfileResponse = HealthProfileResponse(),
    onUpdateHealthProfile: (
        gender: String,
        age: Int,
        weight: Float,
        heightMeters: Float,
        activityLevel: Int,
        targetGoal: Int,
        targetSteps: Int,
        targetWaterMl: Int
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    authBaseUrl: String = "http://10.0.2.2:8001/",
    diaryBaseUrl: String = "http://10.0.2.2:8002/",
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onLogout: () -> Unit,
    onSyncNow: () -> Unit,
    onToggleAutoSync: (Boolean) -> Unit,
    onSaveServerUrls: (String, String) -> Unit = { _, _ -> },
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Аккаунт",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Назад",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("account_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (account.isLoggedIn) {
                // 1. Logged-in Profile Card (without redundant badges)
                item {
                    UserProfileCard(
                        account = account,
                        onLogout = onLogout
                    )
                }

                // 2. Anthropometrics & Health Goals Card
                item {
                    AnthropometricsCard(
                        healthProfile = healthProfile,
                        onUpdateHealthProfile = onUpdateHealthProfile
                    )
                }

                // 3. Cloud Sync Status Card (clean, without fake stats)
                item {
                    CloudSyncCard(
                        account = account,
                        onSyncNow = onSyncNow,
                        onToggleAutoSync = onToggleAutoSync
                    )
                }
            } else {
                // Not Logged In -> Auth Form (Login / Register)
                item {
                    AuthCard(
                        onLogin = onLogin,
                        onRegister = onRegister
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.tether_logo_full),
                        contentDescription = "tether",
                        modifier = Modifier.height(26.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Версия 1.0 • Tether Health & Fitness",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserProfileCard(
    account: UserAccount,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initials Circle
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryGreen, PrimaryGreen.copy(alpha = 0.7f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = account.initials,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = account.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.testTag("logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Выйти из аккаунта",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AnthropometricsCard(
    healthProfile: HealthProfileResponse,
    onUpdateHealthProfile: (
        gender: String,
        age: Int,
        weight: Float,
        heightMeters: Float,
        activityLevel: Int,
        targetGoal: Int,
        targetSteps: Int,
        targetWaterMl: Int
    ) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Антропометрия и цели",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать параметры",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body parameters grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ParamChip(
                    title = "Вес",
                    value = "%.1f кг".format(healthProfile.weight),
                    modifier = Modifier.weight(1f)
                )
                ParamChip(
                    title = "Рост",
                    value = "${(healthProfile.height * 100).toInt()} см",
                    modifier = Modifier.weight(1f)
                )
                ParamChip(
                    title = "Возраст",
                    value = "${healthProfile.age} лет",
                    modifier = Modifier.weight(1f)
                )
                ParamChip(
                    title = "Пол",
                    value = if (healthProfile.gender == "m") "Муж." else "Жен.",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Activity level and Goal summary
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Активность:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = getActivityLevelName(healthProfile.activityLevel),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Цель:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = getGoalName(healthProfile.targetGoal),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            // Calculated Norms from mathematical backend model
            Text(
                text = "Суточные нормы (КБЖУ и цели)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Calories range
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PrimaryGreen.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Норма калорий:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${healthProfile.minTargetCalories.toInt()} — ${healthProfile.maxTargetCalories.toInt()} ккал",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = PrimaryGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Macros row (Proteins, Fats, Carbs)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroCard(
                    title = "Белки",
                    grams = healthProfile.targetProteins.toInt(),
                    color = NutrientProtein,
                    modifier = Modifier.weight(1f)
                )
                MacroCard(
                    title = "Жиры",
                    grams = healthProfile.targetFats.toInt(),
                    color = NutrientFat,
                    modifier = Modifier.weight(1f)
                )
                MacroCard(
                    title = "Углеводы",
                    grams = healthProfile.targetCarbs.toInt(),
                    color = NutrientCarbs,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Water and Steps Goals row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = NutrientWater.copy(alpha = 0.08f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalDrink,
                            contentDescription = null,
                            tint = NutrientWater,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Вода",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(healthProfile.targetWater * 1000).toInt()} мл",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = NutrientWater
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.08f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Шаги",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${healthProfile.targetSteps}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            OutlinedButton(
                onClick = { showEditDialog = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = PrimaryGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Редактировать параметры и цели",
                    color = PrimaryGreen,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    if (showEditDialog) {
        EditAnthropometricsDialog(
            current = healthProfile,
            onDismiss = { showEditDialog = false },
            onSave = { gender, age, weight, heightMeters, actLvl, goal, steps, waterMl ->
                onUpdateHealthProfile(gender, age, weight, heightMeters, actLvl, goal, steps, waterMl)
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditAnthropometricsDialog(
    current: HealthProfileResponse,
    onDismiss: () -> Unit,
    onSave: (
        gender: String,
        age: Int,
        weight: Float,
        heightMeters: Float,
        activityLevel: Int,
        targetGoal: Int,
        targetSteps: Int,
        targetWaterMl: Int
    ) -> Unit
) {
    var gender by remember { mutableStateOf(current.gender) }
    var ageText by remember { mutableStateOf(current.age.toString()) }
    var weightText by remember { mutableStateOf(current.weight.toString()) }
    var heightCmText by remember { mutableStateOf((current.height * 100).toInt().toString()) }
    var activityLevel by remember { mutableIntStateOf(current.activityLevel) }
    var targetGoal by remember { mutableIntStateOf(current.targetGoal) }
    var stepsText by remember { mutableStateOf(current.targetSteps.toString()) }
    var waterMlText by remember { mutableStateOf((current.targetWater * 1000).toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = {
            Text(
                text = "Параметры и цели",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Пол",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = gender == "m",
                        onClick = { gender = "m" },
                        label = { Text("Мужской") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryGreen.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryGreen
                        )
                    )
                    FilterChip(
                        selected = gender == "f",
                        onClick = { gender = "f" },
                        label = { Text("Женский") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryGreen.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Weight and Height
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Вес (кг)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = heightCmText,
                        onValueChange = { heightCmText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Рост (см)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Age
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Возраст (полных лет)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Activity level (1..5)
                Text(
                    text = "Уровень активности",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val activities = listOf(
                    1 to "1: Сидячий образ",
                    2 to "2: Лёгкая (1-3 трен.)",
                    3 to "3: Умеренная (3-5 трен.)",
                    4 to "4: Высокая (6-7 трен.)",
                    5 to "5: Экстремальная"
                )
                activities.forEach { (lvl, title) ->
                    Surface(
                        onClick = { activityLevel = lvl },
                        shape = RoundedCornerShape(8.dp),
                        color = if (activityLevel == lvl) PrimaryGreen.copy(alpha = 0.15f) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (activityLevel == lvl) FontWeight.Bold else FontWeight.Normal,
                                    color = if (activityLevel == lvl) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target Goal (1..4)
                Text(
                    text = "Цель",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val goals = listOf(
                    1 to "1: Похудение (дефицит)",
                    2 to "2: Набор массы (профицит)",
                    3 to "3: Рекомпозиция",
                    4 to "4: Удержание веса"
                )
                goals.forEach { (g, title) ->
                    Surface(
                        onClick = { targetGoal = g },
                        shape = RoundedCornerShape(8.dp),
                        color = if (targetGoal == g) PrimaryGreen.copy(alpha = 0.15f) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (targetGoal == g) FontWeight.Bold else FontWeight.Normal,
                                    color = if (targetGoal == g) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Steps & Water Goals
                Text(
                    text = "Цели по шагам и воде",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = stepsText,
                    onValueChange = { stepsText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Цель шагов в день (0 — авто)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = waterMlText,
                    onValueChange = { waterMlText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Цель по воде (мл)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val age = ageText.toIntOrNull()?.coerceIn(12, 125) ?: 25
                    val weight = weightText.replace(",", ".").toFloatOrNull()?.coerceIn(20f, 350f) ?: 75f
                    val heightCm = heightCmText.toIntOrNull()?.coerceIn(100, 250) ?: 175
                    val heightM = heightCm / 100f
                    val steps = stepsText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    val waterMl = waterMlText.toIntOrNull()?.coerceAtLeast(500) ?: 2000

                    onSave(
                        gender,
                        age,
                        weight,
                        heightM,
                        activityLevel,
                        targetGoal,
                        steps,
                        waterMl
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Сохранить", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun ParamChip(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MacroCard(
    title: String,
    grams: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$grams г",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

private fun getActivityLevelName(lvl: Int): String {
    return when (lvl) {
        1 -> "Сидячий (1)"
        2 -> "Лёгкая (2)"
        3 -> "Умеренная (3)"
        4 -> "Высокая (4)"
        5 -> "Экстремальная (5)"
        else -> "Лёгкая (2)"
    }
}

private fun getGoalName(goal: Int): String {
    return when (goal) {
        1 -> "Похудение (1)"
        2 -> "Набор массы (2)"
        3 -> "Рекомпозиция (3)"
        4 -> "Удержание (4)"
        else -> "Удержание (4)"
    }
}

@Composable
private fun CloudSyncCard(
    account: UserAccount,
    onSyncNow: () -> Unit,
    onToggleAutoSync: (Boolean) -> Unit
) {
    val lastSyncFormatted = remember(account.lastSyncTimestamp) {
        if (account.lastSyncTimestamp <= 0L) {
            "Никогда"
        } else {
            val df = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            df.format(Date(account.lastSyncTimestamp))
        }
    }

    val isSyncing = account.syncStatus == SyncStatus.SYNCING

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Синхронизация",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sync Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing)
                    ),
                    label = "sync_rotation_anim"
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = when (account.syncStatus) {
                                SyncStatus.SYNCED -> PrimaryGreen.copy(alpha = 0.12f)
                                SyncStatus.SYNCING -> PrimaryGreen.copy(alpha = 0.12f)
                                SyncStatus.OFFLINE -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (account.syncStatus) {
                            SyncStatus.SYNCED -> Icons.Default.Sync
                            SyncStatus.SYNCING -> Icons.Default.Sync
                            SyncStatus.OFFLINE -> Icons.Default.Sync
                            else -> Icons.Default.Sync
                        },
                        contentDescription = null,
                        tint = when (account.syncStatus) {
                            SyncStatus.SYNCED -> PrimaryGreen
                            SyncStatus.SYNCING -> PrimaryGreen
                            SyncStatus.OFFLINE -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .then(if (isSyncing) Modifier.rotate(rotation) else Modifier)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = when (account.syncStatus) {
                            SyncStatus.SYNCED -> "Синхронизировано"
                            SyncStatus.SYNCING -> "Идёт синхронизация..."
                            SyncStatus.OFFLINE -> "Автономный режим"
                            else -> "Требуется синхронизация"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Последняя синхронизация: $lastSyncFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sync Now Button
            Button(
                onClick = onSyncNow,
                enabled = !isSyncing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("sync_now_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                )
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Синхронизация...",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Синхронизировать сейчас",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Auto-sync switch (without subtitle as requested by user)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Автосинхронизация",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = account.autoSyncEnabled,
                    onCheckedChange = onToggleAutoSync,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryGreen
                    )
                )
            }
        }
    }
}

@Composable
private fun AuthCard(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Login, 1 = Register

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Tab row for Login / Register
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryGreen
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        localError = null
                    },
                    text = {
                        Text(
                            text = "Вход",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTab == 0) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        localError = null
                    },
                    text = {
                        Text(
                            text = "Регистрация",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedTab == 1) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedTab == 1) {
                // Registration Login field
                OutlinedTextField(
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        localError = null
                    },
                    label = { Text("Логин (от 3 букв) *") },
                    placeholder = { Text("Например: ivan") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryGreen)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("display_name_input")
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Login or Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    localError = null
                },
                label = { Text(if (selectedTab == 0) "Логин или эл. почта *" else "Электронная почта *") },
                placeholder = { Text(if (selectedTab == 0) "Например: tester" else "user@example.com") },
                leadingIcon = {
                    Icon(
                        imageVector = if (selectedTab == 0) Icons.Default.Person else Icons.Default.Email,
                        contentDescription = null,
                        tint = PrimaryGreen
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = if (selectedTab == 0) KeyboardType.Text else KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    localError = null
                },
                label = { Text("Пароль *") },
                placeholder = { Text(if (selectedTab == 1) "Минимум 6 симв., заглавная и цифра" else "Ваш пароль") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryGreen)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input")
            )

            AnimatedVisibility(visible = localError != null) {
                localError?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (selectedTab == 0) {
                        if (email.isBlank() || password.isBlank()) {
                            localError = "Заполните логин и пароль"
                        } else {
                            onLogin(email.trim(), password)
                        }
                    } else {
                        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
                            localError = "Заполните все поля"
                        } else {
                            onRegister(displayName.trim(), password, email.trim())
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("auth_submit_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (selectedTab == 0) "Войти в аккаунт" else "Зарегистрироваться",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
