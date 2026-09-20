package com.tether.ui.screens.diary

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tether.data.health.HealthConnectManager
import com.tether.data.model.FoodItem
import com.tether.data.repository.DiaryRepository
import com.tether.data.repository.UserRepository
import com.tether.ui.components.CalorieHeroCard
import com.tether.ui.components.HealthConnectStepCard
import com.tether.ui.components.WaterTrackerCard
import com.tether.ui.theme.CalorieAccent
import com.tether.ui.theme.CarbAccent
import com.tether.ui.theme.CardShape
import com.tether.ui.theme.FatAccent
import com.tether.ui.theme.PillShape
import com.tether.ui.theme.ProteinAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    healthConnectManager: HealthConnectManager,
    modifier: Modifier = Modifier
) {
    val userProfile by UserRepository.userProfile.collectAsState()
    val foods by DiaryRepository.foods.collectAsState()
    val waterEntries by DiaryRepository.waterEntries.collectAsState()
    val steps by healthConnectManager.stepsCount.collectAsState()
    val isHealthSynced by healthConnectManager.isAuthorized.collectAsState()

    val totalConsumedWater = waterEntries.sumOf { it.amountMl }
    var showAddFoodDialog by remember { mutableStateOf(false) }

    val todayFormatted = remember {
        SimpleDateFormat("d MMMM, EEEE", Locale.forLanguageTag("ru-RU")).format(Date())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Дневник питания",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = todayFormatted.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddFoodDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Добавить продукт", fontWeight = FontWeight.Bold) },
                shape = PillShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Calorie & Macro card
            item {
                CalorieHeroCard(
                    profile = userProfile,
                    foods = foods
                )
            }

            // 2. Health Connect Step Counter Card
            item {
                HealthConnectStepCard(
                    steps = steps,
                    isHealthConnectSynced = isHealthSynced,
                    onSyncClick = {
                        // Triggers check/sync
                    },
                    onSimulateStepAdd = { amount ->
                        healthConnectManager.addManualOrSimulatedSteps(amount)
                    }
                )
            }

            // 3. Water Tracker Card
            item {
                WaterTrackerCard(
                    targetWaterMl = userProfile.targetWaterMl,
                    consumedWaterMl = totalConsumedWater,
                    onAddWater = { ml -> DiaryRepository.addWater(ml) },
                    onUndoWater = { DiaryRepository.undoLastWater() }
                )
            }

            // 4. Food List Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Съедено за день",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${foods.size} ${getProductsPlural(foods.size)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 5. Food List Items
            if (foods.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CardShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Пока ничего не добавлено",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Нажмите кнопку ниже, чтобы записать съеденный продукт",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(foods, key = { it.id }) { food ->
                    FoodCardItem(
                        food = food,
                        onDelete = { DiaryRepository.removeFood(food.id) }
                    )
                }
            }

            // Extra space for FAB and bottom navigation
            item {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }

    if (showAddFoodDialog) {
        AddFoodDialog(
            onDismiss = { showAddFoodDialog = false },
            onAddFood = { newFood ->
                DiaryRepository.addFood(newFood)
                showAddFoodDialog = false
            }
        )
    }
}

@Composable
fun FoodCardItem(
    food: FoodItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = "${food.grams.roundToInt()} г",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                if (food.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = food.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Macros Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MacroPill(label = "Б", value = "${food.totalProtein.roundToInt()}г", color = ProteinAccent)
                    MacroPill(label = "Ж", value = "${food.totalFat.roundToInt()}г", color = FatAccent)
                    MacroPill(label = "У", value = "${food.totalCarbs.roundToInt()}г", color = CarbAccent)
                }
            }

            // Calories badge & delete button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${food.totalCalories}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CalorieAccent,
                    maxLines = 1
                )
                Text(
                    text = "ккал",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun MacroPill(label: String, value: String, color: Color) {
    Surface(
        shape = PillShape,
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AddFoodDialog(
    onDismiss: () -> Unit,
    onAddFood: (FoodItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var caloriesPer100g by remember { mutableStateOf("") }
    var proteinPer100g by remember { mutableStateOf("") }
    var fatPer100g by remember { mutableStateOf("") }
    var carbsPer100g by remember { mutableStateOf("") }
    var grams by remember { mutableStateOf("100") }

    val cals = caloriesPer100g.toFloatOrNull() ?: 0f
    val p = proteinPer100g.toFloatOrNull() ?: 0f
    val f = fatPer100g.toFloatOrNull() ?: 0f
    val c = carbsPer100g.toFloatOrNull() ?: 0f
    val g = grams.toFloatOrNull() ?: 0f

    val calculatedPortionCals = ((cals * g) / 100f).roundToInt()
    val calculatedProtein = ((p * g) / 100f).roundToInt()
    val calculatedFat = ((f * g) / 100f).roundToInt()
    val calculatedCarbs = ((c * g) / 100f).roundToInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Новый продукт",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название (напр. Гречка)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание (напр. отварная)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Gram weight
                OutlinedTextField(
                    value = grams,
                    onValueChange = { grams = it },
                    label = { Text("Съеденный вес (грамм)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Text(
                    text = "Пищевая ценность на 100 грамм:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = caloriesPer100g,
                        onValueChange = { caloriesPer100g = it },
                        label = { Text("Ккал") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = proteinPer100g,
                        onValueChange = { proteinPer100g = it },
                        label = { Text("Белки") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fatPer100g,
                        onValueChange = { fatPer100g = it },
                        label = { Text("Жиры") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = carbsPer100g,
                        onValueChange = { carbsPer100g = it },
                        label = { Text("Углеводы") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Dynamic preview badge for the portion
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Итого за порцию ($g г):",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$calculatedPortionCals ккал",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CalorieAccent
                            )
                            Text(
                                text = "Б: $calculatedProtein г • Ж: $calculatedFat г • У: $calculatedCarbs г",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAddFood(
                            FoodItem(
                                name = name.trim(),
                                description = description.trim(),
                                caloriesPer100g = cals,
                                proteinPer100g = p,
                                fatPer100g = f,
                                carbsPer100g = c,
                                grams = if (g > 0) g else 100f
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                shape = PillShape
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shape = PillShape) {
                Text("Отмена")
            }
        }
    )
}

private fun getProductsPlural(count: Int): String {
    val remainder10 = count % 10
    val remainder100 = count % 100
    return when {
        remainder100 in 11..19 -> "продуктов"
        remainder10 == 1 -> "продукт"
        remainder10 in 2..4 -> "продукта"
        else -> "продуктов"
    }
}
