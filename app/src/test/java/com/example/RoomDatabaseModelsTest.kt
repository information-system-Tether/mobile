package com.example

import com.example.data.db.CustomProductEntity
import com.example.data.db.DailyGoalsEntity
import com.example.data.db.DailyStepEntity
import com.example.data.db.LoggedFoodEntity
import com.example.data.db.WaterLogEntity
import com.example.data.model.DailyGoals
import com.example.data.model.FoodProduct
import com.example.data.model.LoggedFood
import com.example.data.model.MealType
import com.example.data.model.StepActivityStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomDatabaseModelsTest {

    @Test
    fun `logged food entity roundtrip to and from domain preserves all fields`() {
        val original = LoggedFood(
            id = 42L,
            date = "2026-11-05",
            mealType = MealType.SNACK,
            name = "Протеиновый батончик",
            desc = "Шоколад-карамель",
            weightGrams = 60f,
            calories = 210f,
            protein = 20f,
            fat = 7f,
            carbs = 18f,
            fiber = 5f,
            sugar = 1.5f,
            sodium = 0.1f,
            timestamp = 1730800000000L
        )

        val entity = LoggedFoodEntity.fromDomain(original)
        assertEquals(42L, entity.id)
        assertEquals("SNACK", entity.mealType)
        assertEquals("Протеиновый батончик", entity.foodName)
        assertEquals(210f, entity.calories, 0.01f)

        val reconstructed = entity.toDomain()
        assertEquals(original.id, reconstructed.id)
        assertEquals(original.mealType, reconstructed.mealType)
        assertEquals(original.name, reconstructed.name)
        assertEquals(original.weightGrams, reconstructed.weightGrams, 0.01f)
        assertEquals(original.calories, reconstructed.calories, 0.01f)
        assertEquals(original.protein, reconstructed.protein, 0.01f)
        assertEquals(original.fat, reconstructed.fat, 0.01f)
        assertEquals(original.carbs, reconstructed.carbs, 0.01f)
        assertEquals(original.timestamp, reconstructed.timestamp)
    }

    @Test
    fun `daily goals entity roundtrip to and from domain preserves targets`() {
        val goals = DailyGoals(
            caloriesGoal = 2250f,
            proteinGoalGrams = 145f,
            fatGoalGrams = 70f,
            carbsGoalGrams = 260f,
            fiberGoalGrams = 35f,
            waterGoalMl = 2500,
            stepGoal = 12000
        )

        val entity = DailyGoalsEntity.fromDomain(goals)
        assertEquals(2250f, entity.caloriesGoal, 0.01f)
        assertEquals(145f, entity.proteinGoalGrams, 0.01f)
        assertEquals(2500, entity.waterGoalMl)
        assertEquals(12000, entity.stepGoal)

        val domain = entity.toDomain()
        assertEquals(goals.caloriesGoal, domain.caloriesGoal, 0.01f)
        assertEquals(goals.proteinGoalGrams, domain.proteinGoalGrams, 0.01f)
        assertEquals(goals.fatGoalGrams, domain.fatGoalGrams, 0.01f)
        assertEquals(goals.carbsGoalGrams, domain.carbsGoalGrams, 0.01f)
        assertEquals(goals.waterGoalMl, domain.waterGoalMl)
        assertEquals(goals.stepGoal, domain.stepGoal)
    }

    @Test
    fun `custom product entity conversion handles nutrition attributes correctly`() {
        val product = FoodProduct(
            id = "105",
            name = "Домашний творог 5%",
            desc = "Зерненый творог",
            caloriesPer100g = 121f,
            proteinPer100g = 17f,
            fatPer100g = 5f,
            carbsPer100g = 2f,
            fiberPer100g = 0f,
            defaultServingGrams = 200f
        )

        val entity = CustomProductEntity.fromDomain(product)
        assertEquals(105L, entity.id)
        assertEquals("Домашний творог 5%", entity.name)
        assertEquals(121f, entity.caloriesPer100g, 0.01f)

        val domain = entity.toDomain()
        assertEquals("105", domain.id)
        assertEquals("Домашний творог 5%", domain.name)
        assertEquals(17f, domain.proteinPer100g, 0.01f)
    }

    @Test
    fun `step activity stats correctly computes calories burned from steps`() {
        val steps = 8000
        val weightKg = 75f

        val calories = StepActivityStats.calculateCaloriesFromSteps(steps, weightKg)
        // 8000 * 0.042 * (75 / 70) = 360.0 kcal
        assertEquals(360f, calories, 1.0f)

        // For default 70kg weight:
        val caloriesDefault = StepActivityStats.calculateCaloriesFromSteps(steps)
        assertEquals(336f, caloriesDefault, 1.0f)
    }
}
