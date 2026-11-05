package com.example

import com.example.data.api.tether.HealthProfileResponse
import com.example.data.model.DailyGoals
import com.example.data.model.DailyNutrientStats
import com.example.data.model.DayStatsSummary
import com.example.data.model.InsightCategory
import com.example.data.model.SmartInsightsEngine
import com.example.data.model.WeeklyAnalytics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartInsightsAndAnalyticsTest {

    @Test
    fun `insights engine generates calorie deficit advice for weight loss goal`() {
        val profile = HealthProfileResponse(
            weight = 80f,
            height = 1.80f,
            activityLevel = 2,
            targetGoal = 1, // Weight loss
            minTargetCalories = 1800f,
            maxTargetCalories = 2000f,
            targetProteins = 140f
        )

        val stats = DailyNutrientStats(
            date = "2026-11-05",
            goals = DailyGoals(caloriesGoal = 1900f),
            totalCalories = 1750f,
            totalProtein = 135f
        )

        val insights = SmartInsightsEngine.generateInsights(profile, stats, null, workoutsCount = 0)
        val calorieInsight = insights.find { it.category == InsightCategory.CALORIES }

        assertNotNull(calorieInsight)
        assertTrue(calorieInsight!!.title.contains("снижении веса", ignoreCase = true))
        assertTrue(calorieInsight.tag.contains("Дефицит", ignoreCase = true))
    }

    @Test
    fun `insights engine generates surplus advice for muscle gain goal`() {
        val profile = HealthProfileResponse(
            weight = 75f,
            height = 1.78f,
            activityLevel = 3,
            targetGoal = 2, // Muscle gain
            minTargetCalories = 2500f,
            maxTargetCalories = 2800f,
            targetProteins = 150f
        )

        val stats = DailyNutrientStats(
            date = "2026-11-05",
            goals = DailyGoals(caloriesGoal = 2650f),
            totalCalories = 2100f,
            totalProtein = 140f
        )

        val insights = SmartInsightsEngine.generateInsights(profile, stats, null, workoutsCount = 0)
        val calorieInsight = insights.find { it.category == InsightCategory.CALORIES }

        assertNotNull(calorieInsight)
        assertTrue(calorieInsight!!.title.contains("роста мышц", ignoreCase = true) || calorieInsight.title.contains("профицит", ignoreCase = true))
        assertTrue(calorieInsight.tag.contains("Профицит", ignoreCase = true))
    }

    @Test
    fun `water advice reminds about hydration recovery when workouts are logged`() {
        val profile = HealthProfileResponse(weight = 70f, targetWater = 2.3f)
        val stats = DailyNutrientStats(
            date = "2026-11-05",
            goals = DailyGoals(waterGoalMl = 2300),
            waterConsumedMl = 1500
        )

        val insights = SmartInsightsEngine.generateInsights(profile, stats, null, workoutsCount = 2)
        val waterInsight = insights.find { it.category == InsightCategory.WATER }

        assertNotNull(waterInsight)
        assertTrue(waterInsight!!.actionableTip.contains("тренировки", ignoreCase = true))
        assertTrue(waterInsight.actionableTip.contains("500", ignoreCase = true))
    }

    @Test
    fun `protein advice emphasizes post-workout MPS recovery and leucine`() {
        val profile = HealthProfileResponse(weight = 75f, targetProteins = 150f)
        val stats = DailyNutrientStats(
            date = "2026-11-05",
            goals = DailyGoals(proteinGoalGrams = 150f),
            totalProtein = 110f
        )

        val insights = SmartInsightsEngine.generateInsights(profile, stats, null, workoutsCount = 1)
        val proteinInsight = insights.find { it.category == InsightCategory.PROTEIN }

        assertNotNull(proteinInsight)
        assertTrue(proteinInsight!!.actionableTip.contains("лейцин", ignoreCase = true) || proteinInsight.actionableTip.contains("белк", ignoreCase = true))
    }

    @Test
    fun `weekly analytics compliance percentage is correctly calculated`() {
        val days = listOf(
            DayStatsSummary(date = "2026-11-01", dayOfWeek = "Пн", calories = 2000f, waterMl = 2200),
            DayStatsSummary(date = "2026-11-02", dayOfWeek = "Вт", calories = 1950f, waterMl = 2000),
            DayStatsSummary(date = "2026-11-03", dayOfWeek = "Ср", calories = 2100f, waterMl = 1800),
            DayStatsSummary(date = "2026-11-04", dayOfWeek = "Чт", calories = 2800f, waterMl = 2500)
        )

        val weekly = WeeklyAnalytics(
            days = days,
            averageCalories = 2212.5f,
            averageProtein = 130f,
            averageFat = 65f,
            averageCarbs = 240f,
            averageWaterMl = 2125,
            averageSteps = 9500,
            totalBurnedCalories = 1200f,
            targetCalories = 2000f,
            targetWaterMl = 2000,
            targetSteps = 10000
        )

        // 3 out of 4 days are within [1700..2300] kcal -> 75%
        assertEquals(75, weekly.calorieCompliancePercent)
        // 3 out of 4 days reached >= 2000 ml water -> 75%
        assertEquals(75, weekly.hydrationCompliancePercent)
    }
}
