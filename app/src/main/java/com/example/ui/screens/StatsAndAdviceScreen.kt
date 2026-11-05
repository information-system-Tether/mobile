package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EggAlt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.tether.HealthProfileResponse
import com.example.data.model.DailyNutrientStats
import com.example.data.model.DayStatsSummary
import com.example.data.model.InsightCategory
import com.example.data.model.SmartInsight
import com.example.data.model.WeeklyAnalytics
import com.example.ui.theme.NutrientCalories
import com.example.ui.theme.NutrientCarbs
import com.example.ui.theme.NutrientFat
import com.example.ui.theme.NutrientProtein
import com.example.ui.theme.NutrientWater
import com.example.ui.theme.PrimaryGreen

@Composable
fun StatsAndAdviceScreen(
    dailyStats: DailyNutrientStats,
    weeklyAnalytics: WeeklyAnalytics,
    smartInsights: List<SmartInsight>,
    healthProfile: HealthProfileResponse,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(InsightCategory.ALL) }
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    val filteredInsights = remember(selectedCategory, smartInsights) {
        if (selectedCategory == InsightCategory.ALL) smartInsights
        else smartInsights.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Верхний заголовок и сводка
        item {
            ScreenHeader(
                todayCalories = dailyStats.totalCalories,
                targetCalories = dailyStats.goals.caloriesGoal,
                weeklyAvgCalories = weeklyAnalytics.averageCalories,
                activeBurn = dailyStats.totalBurnedCalories
            )
        }

        // 2. Недельная динамика потребления калорий
        item {
            WeeklyCaloriesCard(
                weeklyAnalytics = weeklyAnalytics,
                selectedIndex = selectedDayIndex,
                onSelectDay = { selectedDayIndex = if (selectedDayIndex == it) null else it }
            )
        }

        // 3. Баланс БЖУ (Суточный и процентный баланс)
        item {
            MacroBalanceCard(
                stats = dailyStats,
                healthProfile = healthProfile
            )
        }

        // 4. Водный баланс и динамика гидратации
        item {
            HydrationCard(
                dailyStats = dailyStats,
                weeklyAnalytics = weeklyAnalytics
            )
        }

        // 5. Шаги и сожженные калории
        item {
            ActivityAndStepsCard(
                dailyStats = dailyStats,
                weeklyAvgSteps = weeklyAnalytics.averageSteps
            )
        }

        // 6. Раздел персонализированных советов
        item {
            InsightsHeader(
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it }
            )
        }

        // Список карточек с советами
        items(filteredInsights, key = { it.id }) { insight ->
            InsightItemCard(insight = insight)
        }
    }
}

@Composable
private fun ScreenHeader(
    todayCalories: Float,
    targetCalories: Float,
    weeklyAvgCalories: Float,
    activeBurn: Float
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PrimaryGreen.copy(alpha = 0.14f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Статистика и советы",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Комплексная аналитика рациона",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Индикатор статуса калорий
                val diff = todayCalories - targetCalories
                val statusText = when {
                    todayCalories == 0f -> "Нет данных"
                    diff in -150f..150f -> "В норме"
                    diff < -150f -> "Дефицит"
                    else -> "Профицит"
                }
                val statusColor = when {
                    todayCalories == 0f -> MaterialTheme.colorScheme.onSurfaceVariant
                    diff in -150f..150f -> PrimaryGreen
                    diff < -150f -> NutrientCalories
                    else -> Color(0xFFE11D48)
                }

                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3 ключевые метрики в ряд
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricMiniCard(
                    title = "Сегодня",
                    value = "${todayCalories.toInt()}",
                    unit = "ккал",
                    subtitle = "из ${targetCalories.toInt()}",
                    color = PrimaryGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "В среднем",
                    value = "${weeklyAvgCalories.toInt()}",
                    unit = "ккал/день",
                    subtitle = "за 7 дней",
                    color = NutrientCalories,
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "Сожжено",
                    value = "${activeBurn.toInt()}",
                    unit = "ккал",
                    subtitle = "активность",
                    color = Color(0xFFE11D48),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricMiniCard(
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = color
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun WeeklyCaloriesCard(
    weeklyAnalytics: WeeklyAnalytics,
    selectedIndex: Int?,
    onSelectDay: (Int) -> Unit
) {
    val days = weeklyAnalytics.days
    val targetCalories = weeklyAnalytics.targetCalories.coerceAtLeast(1000f)
    val maxCalories = remember(days, targetCalories) {
        val maxInDays = days.maxOfOrNull { it.calories } ?: 0f
        maxOf(maxInDays, targetCalories * 1.25f).coerceAtLeast(2000f)
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Динамика калорий за неделю",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Соблюдение плана: ${weeklyAnalytics.calorieCompliancePercent}% дней",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryGreen.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Цель: ${targetCalories.toInt()} ккал",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Столбчатая диаграмма 7 дней
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                val targetLineColor = PrimaryGreen.copy(alpha = 0.7f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height - 24.dp.toPx() // leave room for labels
                    val targetY = h - (targetCalories / maxCalories) * h

                    // Линия суточной цели
                    drawLine(
                        color = targetLineColor,
                        start = Offset(0f, targetY),
                        end = Offset(w, targetY),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    days.forEachIndexed { index, day ->
                        val isSelected = selectedIndex == index
                        val barHeightFraction = (day.calories / maxCalories).coerceIn(0.04f, 1f)
                        val isMet = day.calories in (targetCalories * 0.85f)..(targetCalories * 1.15f)
                        val barColor = when {
                            day.calories == 0f -> MaterialTheme.colorScheme.surfaceVariant
                            isMet -> PrimaryGreen
                            day.calories > targetCalories -> Color(0xFFE11D48)
                            else -> NutrientCalories
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectDay(index) }
                        ) {
                            if (isSelected) {
                                Text(
                                    text = "${day.calories.toInt()}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = barColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(if (isSelected) 22.dp else 16.dp)
                                    .height(115.dp * barHeightFraction)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(barColor)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = day.dayOfWeek,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Раскрывающаяся подсказка по выбранному дню
            selectedIndex?.let { idx ->
                if (idx in days.indices) {
                    val day = days[idx]
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${day.dayOfWeek} (${day.date}):",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${day.calories.toInt()} ккал  •  Б: ${day.protein.toInt()}г  •  Вода: ${day.waterMl}мл",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroBalanceCard(
    stats: DailyNutrientStats,
    healthProfile: HealthProfileResponse
) {
    val totalMacroKcal = stats.totalMacroCalories.coerceAtLeast(1f)
    val proteinPercent = stats.proteinEnergyPercent
    val fatPercent = stats.fatEnergyPercent
    val carbsPercent = stats.carbsEnergyPercent

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Баланс макронутриентов (БЖУ)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Суточный срез",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Составная полоса энергии БЖУ
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (stats.totalCalories > 0f) {
                    val pWeight = (stats.totalProtein * 4f / totalMacroKcal).coerceIn(0f, 1f)
                    val fWeight = (stats.totalFat * 9f / totalMacroKcal).coerceIn(0f, 1f)
                    val cWeight = (stats.totalCarbs * 4f / totalMacroKcal).coerceIn(0f, 1f)

                    if (pWeight > 0f) Box(modifier = Modifier.fillMaxHeight().weight(pWeight).background(NutrientProtein))
                    if (fWeight > 0f) Box(modifier = Modifier.fillMaxHeight().weight(fWeight).background(NutrientFat))
                    if (cWeight > 0f) Box(modifier = Modifier.fillMaxHeight().weight(cWeight).background(NutrientCarbs))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 карточки нутриентов
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroDetailPill(
                    name = "Белки",
                    currentGrams = stats.totalProtein,
                    targetGrams = healthProfile.targetProteins,
                    energyPercent = proteinPercent,
                    color = NutrientProtein,
                    modifier = Modifier.weight(1f)
                )
                MacroDetailPill(
                    name = "Жиры",
                    currentGrams = stats.totalFat,
                    targetGrams = healthProfile.targetFats,
                    energyPercent = fatPercent,
                    color = NutrientFat,
                    modifier = Modifier.weight(1f)
                )
                MacroDetailPill(
                    name = "Углеводы",
                    currentGrams = stats.totalCarbs,
                    targetGrams = healthProfile.targetCarbs,
                    energyPercent = carbsPercent,
                    color = NutrientCarbs,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MacroDetailPill(
    name: String,
    currentGrams: Float,
    targetGrams: Float,
    energyPercent: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
                Text(
                    text = "$energyPercent%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${currentGrams.toInt()} г",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "из ${targetGrams.toInt()} г",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HydrationCard(
    dailyStats: DailyNutrientStats,
    weeklyAnalytics: WeeklyAnalytics
) {
    val currentWater = dailyStats.waterConsumedMl
    val targetWater = dailyStats.goals.waterGoalMl.coerceAtLeast(1000)
    val hydrationPercent = ((currentWater.toFloat() / targetWater.toFloat()) * 100f).toInt()

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(NutrientWater.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = NutrientWater,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Водный баланс",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Процент гидратации: $hydrationPercent%",
                            style = MaterialTheme.typography.bodySmall,
                            color = NutrientWater
                        )
                    }
                }

                Text(
                    text = "$currentWater / $targetWater мл",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Прогресс гидратации
            LinearProgressIndicator(
                progress = { (currentWater.toFloat() / targetWater.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = NutrientWater,
                trackColor = NutrientWater.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Мини-индикаторы воды за 7 дней
            Text(
                text = "Недельная динамика гидратации:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyAnalytics.days.forEach { day ->
                    val dayPercent = day.hydrationPercent.toInt()
                    val isDone = dayPercent >= 100
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDone) NutrientWater else NutrientWater.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = "$dayPercent%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.dayOfWeek,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityAndStepsCard(
    dailyStats: DailyNutrientStats,
    weeklyAvgSteps: Int
) {
    val steps = dailyStats.stepsCount
    val stepGoal = dailyStats.goals.stepGoal.coerceAtLeast(1000)
    val stepPercent = ((steps.toFloat() / stepGoal.toFloat()) * 100f).toInt()
    val burnedSteps = dailyStats.stepsBurnedCalories
    val totalBurned = dailyStats.totalBurnedCalories

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFFE11D48).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = null,
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Шаги и активность",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Цель: $stepGoal шагов  ($stepPercent%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "$steps",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { (steps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFE11D48),
                trackColor = Color(0xFFE11D48).copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Сожжено за шаги: ${burnedSteps.toInt()} ккал",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "В среднем: $weeklyAvgSteps шаг/день",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryGreen
                )
            }
        }
    }
}

@Composable
private fun InsightsHeader(
    selectedCategory: InsightCategory,
    onCategorySelect: (InsightCategory) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Персональные советы",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Smart Insights на основе ваших данных",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Фильтры категорий
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(InsightCategory.values()) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(category) },
                    label = {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryGreen,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun InsightItemCard(insight: SmartInsight) {
    val (icon, accentColor) = when (insight.category) {
        InsightCategory.CALORIES -> Pair(Icons.Default.LocalFireDepartment, NutrientCalories)
        InsightCategory.WATER -> Pair(Icons.Default.WaterDrop, NutrientWater)
        InsightCategory.PROTEIN -> Pair(Icons.Default.EggAlt, NutrientProtein)
        InsightCategory.RECOVERY -> Pair(Icons.Default.Bedtime, Color(0xFF8B5CF6))
        else -> Pair(Icons.Default.Info, PrimaryGreen)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(accentColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = insight.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = insight.category.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = accentColor
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = insight.tag,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Actionable tip box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = insight.actionableTip,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
