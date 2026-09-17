package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyNutrientStats
import com.example.data.model.MealType
import com.example.ui.theme.NutrientCalories
import com.example.ui.theme.NutrientCarbs
import com.example.ui.theme.NutrientFat
import com.example.ui.theme.NutrientFiber
import com.example.ui.theme.NutrientProtein
import com.example.ui.theme.NutrientSodium
import com.example.ui.theme.NutrientSugar
import com.example.ui.theme.NutrientWater
import com.example.ui.theme.PrimaryGreen

@Composable
fun DetailedNutrientStats(
    stats: DailyNutrientStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Energy & Macro Ratio (Соотношение БЖУ в калориях)
        EnergyRatioCard(stats = stats)

        // Section 2: Core Macronutrients (Основные макронутриенты)
        MacronutrientsCard(stats = stats)

        // Section 3: Micronutrients & Secondary Nutrients (Клетчатка, Сахар, Натрий)
        MicronutrientsCard(stats = stats)

        // Section 4: Calories by Meals (Калории по приёмам пищи)
        MealsDistributionCard(stats = stats)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun EnergyRatioCard(stats: DailyNutrientStats) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Энергетический баланс (БЖУ)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${stats.totalCalories.toInt()} ккал",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryGreen
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stacked Bar for Macro %
            val pPercent = stats.proteinEnergyPercent
            val fPercent = stats.fatEnergyPercent
            val cPercent = stats.carbsEnergyPercent
            val total = pPercent + fPercent + cPercent

            if (total > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                ) {
                    if (pPercent > 0) {
                        Box(
                            modifier = Modifier
                                .weight(pPercent.toFloat())
                                .fillMaxWidth()
                                .background(NutrientProtein)
                        )
                    }
                    if (fPercent > 0) {
                        Box(
                            modifier = Modifier
                                .weight(fPercent.toFloat())
                                .fillMaxWidth()
                                .background(NutrientFat)
                        )
                    }
                    if (cPercent > 0) {
                        Box(
                            modifier = Modifier
                                .weight(cPercent.toFloat())
                                .fillMaxWidth()
                                .background(NutrientCarbs)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MacroLegendItem(
                    label = "Белки",
                    percent = pPercent,
                    grams = stats.totalProtein,
                    color = NutrientProtein
                )
                MacroLegendItem(
                    label = "Жиры",
                    percent = fPercent,
                    grams = stats.totalFat,
                    color = NutrientFat
                )
                MacroLegendItem(
                    label = "Углеводы",
                    percent = cPercent,
                    grams = stats.totalCarbs,
                    color = NutrientCarbs
                )
            }
        }
    }
}

@Composable
private fun MacroLegendItem(label: String, percent: Int, grams: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${grams.toInt()} г",
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
private fun MacronutrientsCard(stats: DailyNutrientStats) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Детализация макронутриентов",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            NutrientProgressRow(
                name = "Белки",
                consumed = stats.totalProtein,
                goal = stats.goals.proteinGoalGrams,
                unit = "г",
                color = NutrientProtein,
                description = "4 ккал/г • Строительный материал мышц"
            )

            Spacer(modifier = Modifier.height(14.dp))

            NutrientProgressRow(
                name = "Жиры",
                consumed = stats.totalFat,
                goal = stats.goals.fatGoalGrams,
                unit = "г",
                color = NutrientFat,
                description = "9 ккал/г • Гормональный баланс и энергия"
            )

            Spacer(modifier = Modifier.height(14.dp))

            NutrientProgressRow(
                name = "Углеводы",
                consumed = stats.totalCarbs,
                goal = stats.goals.carbsGoalGrams,
                unit = "г",
                color = NutrientCarbs,
                description = "4 ккал/г • Основной источник энергии"
            )
        }
    }
}

@Composable
private fun NutrientProgressRow(
    name: String,
    consumed: Float,
    goal: Float,
    unit: String,
    color: Color,
    description: String
) {
    val progress = if (goal > 0) (consumed / goal).coerceIn(0f, 1f) else 0f
    val percent = if (goal > 0) ((consumed / goal) * 100).toInt() else 0

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${consumed.toInt()} / ${goal.toInt()} $unit",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "($percent%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun MicronutrientsCard(stats: DailyNutrientStats) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Клетчатка и микроэлементы",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fiber
            NutrientProgressRow(
                name = "Клетчатка (пищевые волокна)",
                consumed = stats.totalFiber,
                goal = stats.goals.fiberGoalGrams,
                unit = "г",
                color = NutrientFiber,
                description = "Норма 25–35г • Здоровье ЖКТ и насыщение"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sugar
            val sugarLimit = 50f
            val sugarProgress = (stats.totalSugar / sugarLimit).coerceIn(0f, 1f)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Сахар (простые сахара)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Рекомендуемый лимит ВОЗ: до 50г в день",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${stats.totalSugar.toInt()} г",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (stats.totalSugar > sugarLimit) MaterialTheme.colorScheme.error else NutrientSugar
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { sugarProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (stats.totalSugar > sugarLimit) MaterialTheme.colorScheme.error else NutrientSugar,
                    trackColor = NutrientSugar.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sodium / Salt
            val sodiumLimit = 2.3f // grams of sodium (~5g salt)
            val sodiumProgress = (stats.totalSodium / sodiumLimit).coerceIn(0f, 1f)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Натрий (соль)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Соответствует ${(stats.totalSodium * 2.5f * 10).toInt() / 10f}г поваренной соли",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${(stats.totalSodium * 1000).toInt()} мг",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = NutrientSodium
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { sodiumProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = NutrientSodium,
                    trackColor = NutrientSodium.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun MealsDistributionCard(stats: DailyNutrientStats) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Распределение калорий по приёмам пищи",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            val totalKcal = stats.totalCalories

            MealType.values().forEach { meal ->
                val items = stats.mealBreakdown[meal] ?: emptyList()
                val mealKcal = items.sumOf { it.calories.toDouble() }.toFloat()
                val percent = if (totalKcal > 0) ((mealKcal / totalKcal) * 100).toInt() else 0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = meal.icon,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = meal.displayNameRu,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${items.size} блюд(а)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${mealKcal.toInt()} ккал",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$percent% от дня",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryGreen
                        )
                    }
                }
            }
        }
    }
}
