package com.example

import com.example.data.model.SmartInsightsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MetabolicNormsCalculationTest {

    @Test
    fun `bmr calculation male using mifflin st jeor formula is accurate`() {
        // Male: 80 kg, 180 cm, 25 years old
        // BMR = 10 * 80 + 6.25 * 180 - 5 * 25 + 5 = 800 + 1125 - 125 + 5 = 1805
        val bmrMale = SmartInsightsEngine.calculateBmr(
            gender = "m",
            weightKg = 80f,
            heightCm = 180f,
            ageYears = 25
        )
        assertEquals(1805f, bmrMale, 0.1f)
    }

    @Test
    fun `bmr calculation female using mifflin st jeor formula is accurate`() {
        // Female: 60 kg, 165 cm, 30 years old
        // BMR = 10 * 60 + 6.25 * 165 - 5 * 30 - 161 = 600 + 1031.25 - 150 - 161 = 1320.25
        val bmrFemale = SmartInsightsEngine.calculateBmr(
            gender = "f",
            weightKg = 60f,
            heightCm = 165f,
            ageYears = 30
        )
        assertEquals(1320.25f, bmrFemale, 0.1f)
    }

    @Test
    fun `tdee calculation applies correct activity level multipliers`() {
        val bmr = 1500f
        // Level 1: Sedentary (1.2)
        assertEquals(1800f, SmartInsightsEngine.calculateTdee(bmr, 1), 0.1f)
        // Level 2: Light (1.375)
        assertEquals(2062.5f, SmartInsightsEngine.calculateTdee(bmr, 2), 0.1f)
        // Level 3: Moderate (1.55)
        assertEquals(2325f, SmartInsightsEngine.calculateTdee(bmr, 3), 0.1f)
        // Level 4: High (1.725)
        assertEquals(2587.5f, SmartInsightsEngine.calculateTdee(bmr, 4), 0.1f)
        // Level 5: Athlete (1.9)
        assertEquals(2850f, SmartInsightsEngine.calculateTdee(bmr, 5), 0.1f)
    }

    @Test
    fun `water norm calculation considers weight and workout duration`() {
        // Base norm: 70 kg * 33 ml = 2310 ml
        val baseWater = SmartInsightsEngine.calculateWaterNormMl(weightKg = 70f, workoutMinutes = 0)
        assertEquals(2310, baseWater)

        // With 60 min workout (+500 ml): 2310 + 500 = 2810 ml
        val workoutWater = SmartInsightsEngine.calculateWaterNormMl(weightKg = 70f, workoutMinutes = 60)
        assertEquals(2810, workoutWater)
    }

    @Test
    fun `protein norm adjusts based on user target goal`() {
        val weight = 70f
        // Goal 1: Weight loss (1.8 g/kg) -> 126 g
        assertEquals(126f, SmartInsightsEngine.calculateProteinNormGrams(weight, 1), 0.1f)
        // Goal 2: Muscle gain (2.0 g/kg) -> 140 g
        assertEquals(140f, SmartInsightsEngine.calculateProteinNormGrams(weight, 2), 0.1f)
        // Goal 3: Relief (2.2 g/kg) -> 154 g
        assertEquals(154f, SmartInsightsEngine.calculateProteinNormGrams(weight, 3), 0.1f)
        // Goal 4: Maintenance (1.4 g/kg) -> 98 g
        assertEquals(98f, SmartInsightsEngine.calculateProteinNormGrams(weight, 4), 0.1f)
    }
}
