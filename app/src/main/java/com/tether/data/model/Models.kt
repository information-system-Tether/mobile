package com.tether.data.model

import java.util.UUID
import kotlin.math.roundToInt

enum class Gender(val displayName: String) {
    MALE("Мужской"),
    FEMALE("Женский")
}

enum class ActivityLevel(val displayName: String, val multiplier: Float, val description: String) {
    SEDENTARY("Сидячий", 1.2f, "Малоподвижный образ жизни, сидячая работа"),
    LIGHT("Легкая активность", 1.375f, "Тренировки 1-3 раза в неделю или 6-8 тыс. шагов"),
    MODERATE("Умеренная активность", 1.55f, "Тренировки 3-5 раз в неделю или 10 тыс. шагов"),
    VERY_ACTIVE("Высокая активность", 1.725f, "Интенсивные тренировки 6-7 раз в неделю"),
    ATHLETE("Экстремальная активность", 1.9f, "Тяжелый физический труд или спорт 2 раза в день")
}

enum class GoalType(val displayName: String, val multiplier: Float) {
    LOSE_WEIGHT("Снижение веса (-15%)", 0.85f),
    MAINTAIN("Поддержание веса", 1.0f),
    GAIN_MUSCLE("Набор мышечной массы (+10%)", 1.1f)
}

data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Александр",
    val email: String = "alex@tether.app",
    val heightCm: Float = 178f,
    val weightKg: Float = 75f,
    val age: Int = 26,
    val gender: Gender = Gender.MALE,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val goalType: GoalType = GoalType.MAINTAIN,
    val customCalorieTarget: Int? = null,
    val customWaterTargetMl: Int? = null
) {
    // Mifflin-St Jeor formula
    val bmr: Int
        get() {
            val base = (10f * weightKg) + (6.25f * heightCm) - (5f * age)
            return if (gender == Gender.MALE) (base + 5).roundToInt() else (base - 161).roundToInt()
        }

    val tdee: Int
        get() = (bmr * activityLevel.multiplier).roundToInt()

    val targetCalories: Int
        get() = customCalorieTarget ?: (tdee * goalType.multiplier).roundToInt()

    // 35 ml per kg + activity compensation
    val targetWaterMl: Int
        get() {
            if (customWaterTargetMl != null) return customWaterTargetMl
            val base = (weightKg * 35).roundToInt()
            val extra = when (activityLevel) {
                ActivityLevel.SEDENTARY -> 0
                ActivityLevel.LIGHT -> 250
                ActivityLevel.MODERATE -> 500
                ActivityLevel.VERY_ACTIVE -> 750
                ActivityLevel.ATHLETE -> 1000
            }
            return base + extra
        }

    // Recommended macros
    val targetProteinGrams: Int
        get() = when (goalType) {
            GoalType.LOSE_WEIGHT -> (weightKg * 2.0f).roundToInt()
            GoalType.MAINTAIN -> (weightKg * 1.6f).roundToInt()
            GoalType.GAIN_MUSCLE -> (weightKg * 2.2f).roundToInt()
        }

    val targetFatGrams: Int
        get() = (weightKg * 0.9f).roundToInt()

    val targetCarbsGrams: Int
        get() {
            val proteinCals = targetProteinGrams * 4
            val fatCals = targetFatGrams * 9
            val remainingCals = (targetCalories - proteinCals - fatCals).coerceAtLeast(0)
            return (remainingCals / 4f).roundToInt()
        }
}

data class FoodItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val fatPer100g: Float,
    val carbsPer100g: Float,
    val grams: Float = 100f,
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalCalories: Int
        get() = ((caloriesPer100g * grams) / 100f).roundToInt()

    val totalProtein: Float
        get() = (proteinPer100g * grams) / 100f

    val totalFat: Float
        get() = (fatPer100g * grams) / 100f

    val totalCarbs: Float
        get() = (carbsPer100g * grams) / 100f
}

data class WaterEntry(
    val id: String = UUID.randomUUID().toString(),
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MuscleGroup(val displayName: String, val isFront: Boolean) {
    CHEST("Грудь", true),
    BACK("Спина", false),
    SHOULDERS("Плечи", true),
    BICEPS("Бицепс", true),
    TRICEPS("Трицепс", false),
    ABS("Пресс / Кор", true),
    QUADS("Квадрицепс", true),
    HAMSTRINGS("Бицепс бедра", false),
    CALVES("Икры", false)
}

data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val equipment: String = "Собственный вес",
    val defaultSets: Int = 3,
    val defaultReps: Int = 12
)

data class WorkoutExercise(
    val id: String = UUID.randomUUID().toString(),
    val exerciseId: String,
    val name: String,
    val muscleGroup: MuscleGroup,
    val sets: Int = 3,
    val reps: Int = 10,
    val weightKg: Float = 0f,
    val isCompleted: Boolean = false
)

data class Workout(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val durationMinutes: Int = 45,
    val exercises: List<WorkoutExercise> = emptyList(),
    val isCustom: Boolean = true,
    val lastCompletedDate: Long? = null
)

data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double = 0.0,
    val time: Long = System.currentTimeMillis()
)

enum class RouteDifficulty(val displayName: String) {
    EASY("Легкий"),
    MEDIUM("Средний"),
    HARD("Сложный")
}

data class GpxRoute(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val distanceKm: Float,
    val elevationGainMeters: Int,
    val durationMinutes: Int,
    val difficulty: RouteDifficulty = RouteDifficulty.MEDIUM,
    val points: List<TrackPoint> = emptyList(),
    val isCommunity: Boolean = false,
    val isUserRecorded: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RecommendationCategory(val displayName: String) {
    NUTRITION("Питание"),
    WORKOUT("Тренировки"),
    HYDRATION("Водный баланс"),
    RECOVERY("Восстановление")
}

data class Recommendation(
    val id: String = UUID.randomUUID().toString(),
    val category: RecommendationCategory,
    val title: String,
    val shortSummary: String,
    val detailedExplanation: String,
    val tag: String
)
