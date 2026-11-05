package com.example

import com.example.data.model.DailyGoals
import com.example.data.model.DailyNutrientStats
import com.example.data.model.FoodProduct
import com.example.data.model.LoggedFood
import com.example.data.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodDiaryLogicTest {

    @Test
    fun `portion scaling accurately calculates macros and calories for various weights`() {
        val chickenBreast = FoodProduct(
            id = "chicken_1",
            name = "Куриная грудка",
            caloriesPer100g = 165f,
            proteinPer100g = 31f,
            fatPer100g = 3.6f,
            carbsPer100g = 0f
        )

        // Test 100g
        val at100g = chickenBreast.calculateForWeight(100f)
        assertEquals(165f, at100g.calories, 0.01f)
        assertEquals(31f, at100g.protein, 0.01f)
        assertEquals(3.6f, at100g.fat, 0.01f)
        assertEquals(0f, at100g.carbs, 0.01f)

        // Test 200g (2x)
        val at200g = chickenBreast.calculateForWeight(200f)
        assertEquals(330f, at200g.calories, 0.01f)
        assertEquals(62f, at200g.protein, 0.01f)
        assertEquals(7.2f, at200g.fat, 0.01f)

        // Test 50g (0.5x)
        val at50g = chickenBreast.calculateForWeight(50f)
        assertEquals(82.5f, at50g.calories, 0.01f)
        assertEquals(15.5f, at50g.protein, 0.01f)
        assertEquals(1.8f, at50g.fat, 0.01f)
    }

    @Test
    fun `food diary daily stats accurately sums up multiple meal entries`() {
        val breakfastOats = LoggedFood(
            id = 1,
            date = "2026-11-05",
            mealType = MealType.BREAKFAST,
            name = "Овсянка с ягодами",
            weightGrams = 200f,
            calories = 280f,
            protein = 10f,
            fat = 5f,
            carbs = 48f
        )

        val lunchChicken = LoggedFood(
            id = 2,
            date = "2026-11-05",
            mealType = MealType.LUNCH,
            name = "Куриное филе с рисом",
            weightGrams = 350f,
            calories = 450f,
            protein = 40f,
            fat = 8f,
            carbs = 55f
        )

        val dinnerSalad = LoggedFood(
            id = 3,
            date = "2026-11-05",
            mealType = MealType.DINNER,
            name = "Греческий салат",
            weightGrams = 250f,
            calories = 220f,
            protein = 6f,
            fat = 18f,
            carbs = 10f
        )

        val foods = listOf(breakfastOats, lunchChicken, dinnerSalad)
        val totalCalories = foods.sumOf { it.calories.toDouble() }.toFloat()
        val totalProtein = foods.sumOf { it.protein.toDouble() }.toFloat()
        val totalFat = foods.sumOf { it.fat.toDouble() }.toFloat()
        val totalCarbs = foods.sumOf { it.carbs.toDouble() }.toFloat()

        assertEquals(950f, totalCalories, 0.01f)
        assertEquals(56f, totalProtein, 0.01f)
        assertEquals(31f, totalFat, 0.01f)
        assertEquals(113f, totalCarbs, 0.01f)

        val goals = DailyGoals(caloriesGoal = 2000f, proteinGoalGrams = 120f)
        val stats = DailyNutrientStats(
            date = "2026-11-05",
            goals = goals,
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalFat = totalFat,
            totalCarbs = totalCarbs,
            totalBurnedCalories = 350f
        )

        // Net calories = calories - burned = 950 - 350 = 600
        assertEquals(600f, stats.netCalories, 0.01f)
        // Remaining calories = goals - consumed + burned = 2000 - 950 + 350 = 1400
        assertEquals(1400f, stats.remainingCalories, 0.01f)
        // Calorie progress = 950 / 2000 = 0.475
        assertEquals(0.475f, stats.calorieProgress, 0.01f)
    }

    @Test
    fun `macronutrient energy percentages sum up to approximately 100 percent`() {
        val stats = DailyNutrientStats(
            date = "2026-11-05",
            goals = DailyGoals(),
            totalCalories = 2000f,
            totalProtein = 150f, // 150 * 4 = 600 kcal
            totalFat = 60f,      // 60 * 9 = 540 kcal
            totalCarbs = 215f    // 215 * 4 = 860 kcal
        )
        // Total macro kcal = 600 + 540 + 860 = 2000 kcal
        assertEquals(2000f, stats.totalMacroCalories, 0.1f)
        assertEquals(30, stats.proteinEnergyPercent) // 600 / 2000 = 30%
        assertEquals(27, stats.fatEnergyPercent)     // 540 / 2000 = 27%
        assertEquals(43, stats.carbsEnergyPercent)   // 860 / 2000 = 43%
        val totalPct = stats.proteinEnergyPercent + stats.fatEnergyPercent + stats.carbsEnergyPercent
        assertEquals(100, totalPct)
    }
}
