package com.example.data.model

import com.example.data.api.tether.HealthProfileResponse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DayStatsSummary(
    val date: String,            // YYYY-MM-DD
    val dayOfWeek: String,       // Пн, Вт, Ср, ...
    val calories: Float = 0f,
    val targetCalories: Float = 2000f,
    val protein: Float = 0f,
    val targetProtein: Float = 125f,
    val fat: Float = 0f,
    val targetFat: Float = 65f,
    val carbs: Float = 0f,
    val targetCarbs: Float = 230f,
    val waterMl: Int = 0,
    val targetWaterMl: Int = 2000,
    val steps: Int = 0,
    val targetSteps: Int = 10000,
    val burnedCalories: Float = 0f
) {
    val hydrationPercent: Float
        get() = if (targetWaterMl > 0) ((waterMl.toFloat() / targetWaterMl.toFloat()) * 100f).coerceAtLeast(0f) else 0f

    val caloriePercent: Float
        get() = if (targetCalories > 0) ((calories / targetCalories) * 100f).coerceAtLeast(0f) else 0f

    val totalMacroCalories: Float
        get() = (protein * 4f) + (fat * 9f) + (carbs * 4f)

    val proteinEnergyPercent: Int
        get() = if (totalMacroCalories > 0) ((protein * 4f / totalMacroCalories) * 100f).toInt() else 0

    val fatEnergyPercent: Int
        get() = if (totalMacroCalories > 0) ((fat * 9f / totalMacroCalories) * 100f).toInt() else 0

    val carbsEnergyPercent: Int
        get() = if (totalMacroCalories > 0) ((carbs * 4f / totalMacroCalories) * 100f).toInt() else 0
}

data class WeeklyAnalytics(
    val days: List<DayStatsSummary>,
    val averageCalories: Float,
    val averageProtein: Float,
    val averageFat: Float,
    val averageCarbs: Float,
    val averageWaterMl: Int,
    val averageSteps: Int,
    val totalBurnedCalories: Float,
    val targetCalories: Float,
    val targetWaterMl: Int,
    val targetSteps: Int
) {
    val calorieCompliancePercent: Int
        get() {
            if (days.isEmpty() || targetCalories <= 0) return 0
            val metCount = days.count { it.calories in (targetCalories * 0.85f)..(targetCalories * 1.15f) }
            return ((metCount.toFloat() / days.size) * 100f).toInt()
        }

    val hydrationCompliancePercent: Int
        get() {
            if (days.isEmpty() || targetWaterMl <= 0) return 0
            val metCount = days.count { it.waterMl >= targetWaterMl }
            return ((metCount.toFloat() / days.size) * 100f).toInt()
        }
}

enum class InsightCategory(val displayName: String) {
    ALL("Все"),
    CALORIES("Калории"),
    WATER("Водный баланс"),
    PROTEIN("Белок и мышцы"),
    RECOVERY("Сон и режим")
}

data class SmartInsight(
    val id: String,
    val category: InsightCategory,
    val title: String,
    val description: String,
    val actionableTip: String,
    val tag: String,
    val iconName: String
)

object SmartInsightsEngine {

    /**
     * Формула Миффлина — Сан Жеора:
     * BMR = 10 * weight(kg) + 6.25 * height(cm) - 5 * age(years) + s
     * s = +5 для мужчин, -161 для женщин.
     */
    fun calculateBmr(gender: String, weightKg: Float, heightCm: Float, ageYears: Int): Float {
        val s = if (gender.trim().lowercase() == "m" || gender.trim().lowercase() == "мужской" || gender.trim().lowercase() == "муж.") 5f else -161f
        return (10f * weightKg) + (6.25f * heightCm) - (5f * ageYears) + s
    }

    /**
     * TDEE = BMR * ActivityMultiplier
     */
    fun calculateTdee(bmr: Float, activityLevel: Int): Float {
        val mult = when (activityLevel) {
            1 -> 1.2f     // Сидячий
            2 -> 1.375f   // Легкая активность
            3 -> 1.55f    // Умеренная активность
            4 -> 1.725f   // Высокая активность
            5 -> 1.9f     // Экстремальная / атлет
            else -> 1.2f
        }
        return bmr * mult
    }

    /**
     * Расчет суточных норм воды: 33 мл на 1 кг веса тела + добавка на тренировки
     */
    fun calculateWaterNormMl(weightKg: Float, workoutMinutes: Int = 0): Int {
        val baseMl = (weightKg * 33f).toInt()
        val extraWorkoutMl = (workoutMinutes / 30) * 250
        return baseMl + extraWorkoutMl
    }

    /**
     * Расчет нормы белка в граммах в зависимости от цели и веса
     */
    fun calculateProteinNormGrams(weightKg: Float, targetGoal: Int): Float {
        val perKg = when (targetGoal) {
            1 -> 1.8f // Похудение (сохранение мышц)
            2 -> 2.0f // Набор массы
            3 -> 2.2f // Рельеф / сушка
            else -> 1.4f // Поддержание
        }
        return weightKg * perKg
    }

    /**
     * Генерация персонализированных советов и аналитики
     */
    fun generateInsights(
        profile: HealthProfileResponse,
        todayStats: DailyNutrientStats,
        weeklyAnalytics: WeeklyAnalytics?,
        workoutsCount: Int = 0
    ): List<SmartInsight> {
        val insights = mutableListOf<SmartInsight>()
        val weight = profile.weight.coerceAtLeast(40f)
        val targetKcal = profile.maxTargetCalories.coerceAtLeast(1200f)
        val currentKcal = todayStats.totalCalories
        val goalId = profile.targetGoal

        // 1. СОВЕТ ПО КАЛОРИЯМ И ЦЕЛИ (Дефицит / Профицит)
        val calorieDiff = currentKcal - targetKcal
        when (goalId) {
            1 -> { // Снижение веса
                val tip = if (currentKcal == 0f) {
                    "Для безопасного и устойчивого похудения держите дефицит 15-20% (цель: ${targetKcal.toInt()} ккал). Не опускайтесь ниже базового обмена веществ, чтобы не замедлять метаболизм."
                } else if (calorieDiff < -400) {
                    "Текущий дефицит составляет более 400 ккал от нормы. Рекомендуется добавить сложный углевод или белковый перекус, чтобы предотвратить потерю мышечной массы."
                } else if (calorieDiff > 100) {
                    "Сегодня потребление калорий превысило плановую норму на ${calorieDiff.toInt()} ккал. Легкая вечерняя прогулка на 30-40 минут компенсирует избыток."
                } else {
                    "Отличный баланс! Вы находитесь в идеальном диапазоне дефицита для качественного жиросжигания без стресса для организма."
                }
                insights.add(
                    SmartInsight(
                        id = "cal_weight_loss",
                        category = InsightCategory.CALORIES,
                        title = "Калорийность при снижении веса",
                        description = "Ваша цель — планомерное уменьшение жирового компонента. Оптимальный дефицит составляет 300–500 ккал в день.",
                        actionableTip = tip,
                        tag = "Дефицит калорий",
                        iconName = "local_fire_department"
                    )
                )
            }
            2 -> { // Набор массы
                val tip = if (currentKcal < targetKcal * 0.8f) {
                    "Для набора чистой мышечной массы нужен устойчивый профицит калорий (+250–350 ккал). Добавьте в рацион орехи, каши или цельнозерновой хлеб."
                } else {
                    "Хороший темп набора массы! Следите за тем, чтобы профицит не превышал 400 ккал, чтобы минимизировать набор жировой ткани."
                }
                insights.add(
                    SmartInsight(
                        id = "cal_mass_gain",
                        category = InsightCategory.CALORIES,
                        title = "Профицит для роста мышц",
                        description = "Мышечная гипертрофия требует регулярного энергетического профицита и строительного материала.",
                        actionableTip = tip,
                        tag = "Профицит +10-15%",
                        iconName = "trending_up"
                    )
                )
            }
            3 -> { // Рельеф
                insights.add(
                    SmartInsight(
                        id = "cal_relief",
                        category = InsightCategory.CALORIES,
                        title = "Работа на рельеф и детализацию",
                        description = "Умеренный дефицит 10% в сочетании с высоким потреблением белка позволяет прорисовать мышечные волокна.",
                        actionableTip = "Сохраняйте углеводы преимущественно вокруг тренировок (до и после), а вечером отдавайте предпочтение нежирному белку и зелени.",
                        tag = "Циклирование КБЖУ",
                        iconName = "fitness_center"
                    )
                )
            }
            else -> { // Поддержание
                insights.add(
                    SmartInsight(
                        id = "cal_maintenance",
                        category = InsightCategory.CALORIES,
                        title = "Баланс поддержания веса",
                        description = "Равновесие между потребляемой энергией и суточным расходом обеспечивает стабильный вес и высокий тонус.",
                        actionableTip = "Придерживайтесь целевой планки ${targetKcal.toInt()} ккал и варьируйте источники сложных углеводов и полезных жиров.",
                        tag = "Энергетический баланс",
                        iconName = "balance"
                    )
                )
            }
        }

        // 2. СОВЕТ ПО ВОДНОМУ БАЛАНСУ
        val waterTarget = if (profile.targetWater > 0) (profile.targetWater * 1000).toInt() else calculateWaterNormMl(weight)
        val waterCurrent = todayStats.waterConsumedMl
        val hydrationPercent = if (waterTarget > 0) (waterCurrent.toFloat() / waterTarget * 100f).toInt() else 0

        val waterTip = when {
            workoutsCount > 0 -> "У вас зафиксированы тренировки! Дополнительно выпейте 500-750 мл чистой воды для восстановления электролитного баланса."
            hydrationPercent < 50 -> "Текущая гидратация ниже 50%. Начните выпивать по 1 стакану (250 мл) воды каждые 1.5–2 часа."
            hydrationPercent in 50..85 -> "Вы на верном пути! До достижения суточной нормы осталось ${waterTarget - waterCurrent} мл."
            else -> "Отличная гидратация! Суточная норма выполнена, водно-солевой обмен на оптимальном уровне."
        }

        insights.add(
            SmartInsight(
                id = "water_balance",
                category = InsightCategory.WATER,
                title = "Гидратация и водно-солевой баланс",
                description = "Вода участвует во всех метаболических реакциях, транспорте питательных веществ и терморегуляции организма.",
                actionableTip = waterTip,
                tag = "$hydrationPercent% от нормы",
                iconName = "water_drop"
            )
        )

        // 3. СОВЕТ ПО БЕЛКУ И ВОССТАНОВЛЕНИЮ МЫШЦ
        val targetProtein = profile.targetProteins.coerceAtLeast(weight * 1.5f)
        val currentProtein = todayStats.totalProtein
        val proteinRatio = currentProtein / weight

        val proteinTip = when {
            workoutsCount > 0 -> "После силовой нагрузки наступает фаза активного восстановления. Примите порцию белка (25–35 г), богатую лейцином (яйца, птица, творог) в течение 1–2 часов."
            proteinRatio < 1.2f -> "Сейчас уровень белка составляет ${String.format(Locale.US, "%.1f", proteinRatio)} г/кг веса. Добавьте в следующий прием пищи творог, рыбу, бобовые или протеин, чтобы достичь целевых 1.6–2.0 г/кг."
            else -> "Потребление белка на высоком уровне (${currentProtein.toInt()} г). Мышечный анаболизм надежно защищен от катаболизма."
        }

        insights.add(
            SmartInsight(
                id = "protein_recovery",
                category = InsightCategory.PROTEIN,
                title = "Белок и мышечное восстановление",
                description = "Аминокислоты — строительный материал для регенерации миофибрилл и связок после физических нагрузок.",
                actionableTip = proteinTip,
                tag = "${currentProtein.toInt()} из ${targetProtein.toInt()} г",
                iconName = "egg_alt"
            )
        )

        // 4. СОВЕТ ПО КАЧЕСТВУ СНА И ИНТЕРВАЛАМ ПИТАНИЯ
        insights.add(
            SmartInsight(
                id = "sleep_and_timing",
                category = InsightCategory.RECOVERY,
                title = "Качество сна и интервалы питания",
                description = "Гормоны сытости (лептин) и аппетита (грелин) напрямую зависят от длительности и глубины ночного сна.",
                actionableTip = "Старайтесь спать 7.5–8.5 часов в темной прохладной комнате. Делайте последний прием пищи за 2.5–3 часа до сна, избегая быстрых сахаров, чтобы пик соматотропного гормона пришелся на глубокую фазу сна.",
                tag = "Циркадные ритмы",
                iconName = "bedtime"
            )
        )

        // 5. РЕЖИМ ПИТАНИЯ
        insights.add(
            SmartInsight(
                id = "meal_intervals",
                category = InsightCategory.RECOVERY,
                title = "Оптимальные интервалы между приемами пищи",
                description = "Равномерные интервалы 3.5–4.5 часа предотвращают резкие скачки гликемии и приступы вечернего переедания.",
                actionableTip = "Разделите дневной калораж на 3 основных приема пищи и 1-2 сбалансированных перекуса. Начинайте день с плотного белково-углеводного завтрака.",
                tag = "Инсулиновый баланс",
                iconName = "schedule"
            )
        )

        return insights
    }
}
