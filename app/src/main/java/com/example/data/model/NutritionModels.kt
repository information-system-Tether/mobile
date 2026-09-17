package com.example.data.model

data class FoodProduct(
    val id: String = "",
    val name: String,
    val brand: String = "",
    val barcode: String? = null,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val fatPer100g: Float,
    val carbsPer100g: Float,
    val fiberPer100g: Float = 0f,
    val sugarPer100g: Float = 0f,
    val sodiumPer100g: Float = 0f,
    val imageUrl: String? = null,
    val defaultServingGrams: Float = 100f,
    val category: String = "Общее",
    val isFavorite: Boolean = false
) {
    fun calculateForWeight(weightGrams: Float): CalculatedNutrition {
        val factor = weightGrams / 100f
        return CalculatedNutrition(
            calories = caloriesPer100g * factor,
            protein = proteinPer100g * factor,
            fat = fatPer100g * factor,
            carbs = carbsPer100g * factor,
            fiber = fiberPer100g * factor,
            sugar = sugarPer100g * factor,
            sodium = sodiumPer100g * factor
        )
    }
}

data class CalculatedNutrition(
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carbs: Float,
    val fiber: Float,
    val sugar: Float,
    val sodium: Float
)

data class LoggedFood(
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val mealType: MealType,
    val name: String,
    val brand: String = "",
    val barcode: String? = null,
    val weightGrams: Float,
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carbs: Float,
    val fiber: Float = 0f,
    val sugar: Float = 0f,
    val sodium: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class DailyGoals(
    val caloriesGoal: Float = 2000f,
    val proteinGoalGrams: Float = 125f,
    val fatGoalGrams: Float = 65f,
    val carbsGoalGrams: Float = 230f,
    val fiberGoalGrams: Float = 30f,
    val waterGoalMl: Int = 2000
)

data class DailyNutrientStats(
    val date: String,
    val goals: DailyGoals,
    val totalCalories: Float = 0f,
    val totalProtein: Float = 0f,
    val totalFat: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalFiber: Float = 0f,
    val totalSugar: Float = 0f,
    val totalSodium: Float = 0f,
    val waterConsumedMl: Int = 0,
    val mealBreakdown: Map<MealType, List<LoggedFood>> = emptyMap(),
    val totalBurnedCalories: Float = 0f,
    val stepsCount: Int = 0,
    val stepsBurnedCalories: Float = 0f,
    val workoutCount: Int = 0
) {
    val netCalories: Float
        get() = totalCalories - totalBurnedCalories

    val remainingCalories: Float
        get() = (goals.caloriesGoal - totalCalories + totalBurnedCalories).coerceAtLeast(0f)

    val calorieProgress: Float
        get() = if (goals.caloriesGoal > 0) (totalCalories / goals.caloriesGoal).coerceIn(0f, 1f) else 0f

    val proteinProgress: Float
        get() = if (goals.proteinGoalGrams > 0) (totalProtein / goals.proteinGoalGrams).coerceIn(0f, 1f) else 0f

    val fatProgress: Float
        get() = if (goals.fatGoalGrams > 0) (totalFat / goals.fatGoalGrams).coerceIn(0f, 1f) else 0f

    val carbsProgress: Float
        get() = if (goals.carbsGoalGrams > 0) (totalCarbs / goals.carbsGoalGrams).coerceIn(0f, 1f) else 0f

    val fiberProgress: Float
        get() = if (goals.fiberGoalGrams > 0) (totalFiber / goals.fiberGoalGrams).coerceIn(0f, 1f) else 0f

    val waterProgress: Float
        get() = if (goals.waterGoalMl > 0) (waterConsumedMl.toFloat() / goals.waterGoalMl.toFloat()).coerceIn(0f, 1f) else 0f

    // Calorie macro energy percentages (Protein 4 kcal/g, Fat 9 kcal/g, Carbs 4 kcal/g)
    val totalMacroCalories: Float
        get() = (totalProtein * 4f) + (totalFat * 9f) + (totalCarbs * 4f)

    val proteinEnergyPercent: Int
        get() = if (totalMacroCalories > 0) ((totalProtein * 4f / totalMacroCalories) * 100f).toInt() else 0

    val fatEnergyPercent: Int
        get() = if (totalMacroCalories > 0) ((totalFat * 9f / totalMacroCalories) * 100f).toInt() else 0

    val carbsEnergyPercent: Int
        get() = if (totalMacroCalories > 0) ((totalCarbs * 4f / totalMacroCalories) * 100f).toInt() else 0
}
