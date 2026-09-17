package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DailyGoals
import com.example.data.model.FoodProduct
import com.example.data.model.LoggedFood
import com.example.data.model.MealType

@Entity(tableName = "logged_foods")
data class LoggedFoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val mealType: String,
    val foodName: String,
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
) {
    fun toDomain(): LoggedFood {
        val parsedMeal = try {
            MealType.valueOf(mealType)
        } catch (e: Exception) {
            MealType.BREAKFAST
        }
        return LoggedFood(
            id = id,
            date = date,
            mealType = parsedMeal,
            name = foodName,
            brand = brand,
            barcode = barcode,
            weightGrams = weightGrams,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs,
            fiber = fiber,
            sugar = sugar,
            sodium = sodium,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(domain: LoggedFood): LoggedFoodEntity {
            return LoggedFoodEntity(
                id = domain.id,
                date = domain.date,
                mealType = domain.mealType.name,
                foodName = domain.name,
                brand = domain.brand,
                barcode = domain.barcode,
                weightGrams = domain.weightGrams,
                calories = domain.calories,
                protein = domain.protein,
                fat = domain.fat,
                carbs = domain.carbs,
                fiber = domain.fiber,
                sugar = domain.sugar,
                sodium = domain.sodium,
                timestamp = domain.timestamp
            )
        }
    }
}

@Entity(tableName = "custom_products")
data class CustomProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    val defaultServingGrams: Float = 100f
) {
    fun toDomain(): FoodProduct {
        return FoodProduct(
            id = id.toString(),
            name = name,
            brand = brand,
            barcode = barcode,
            caloriesPer100g = caloriesPer100g,
            proteinPer100g = proteinPer100g,
            fatPer100g = fatPer100g,
            carbsPer100g = carbsPer100g,
            fiberPer100g = fiberPer100g,
            sugarPer100g = sugarPer100g,
            sodiumPer100g = sodiumPer100g,
            defaultServingGrams = defaultServingGrams,
            category = "Мои продукты"
        )
    }

    companion object {
        fun fromDomain(domain: FoodProduct): CustomProductEntity {
            return CustomProductEntity(
                id = domain.id.toLongOrNull() ?: 0,
                name = domain.name,
                brand = domain.brand,
                barcode = domain.barcode,
                caloriesPer100g = domain.caloriesPer100g,
                proteinPer100g = domain.proteinPer100g,
                fatPer100g = domain.fatPer100g,
                carbsPer100g = domain.carbsPer100g,
                fiberPer100g = domain.fiberPer100g,
                sugarPer100g = domain.sugarPer100g,
                sodiumPer100g = domain.sodiumPer100g,
                defaultServingGrams = domain.defaultServingGrams
            )
        }
    }
}

@Entity(tableName = "daily_goals")
data class DailyGoalsEntity(
    @PrimaryKey val id: Int = 1,
    val caloriesGoal: Float = 2000f,
    val proteinGoalGrams: Float = 125f,
    val fatGoalGrams: Float = 65f,
    val carbsGoalGrams: Float = 230f,
    val fiberGoalGrams: Float = 30f,
    val waterGoalMl: Int = 2000
) {
    fun toDomain() = DailyGoals(
        caloriesGoal = caloriesGoal,
        proteinGoalGrams = proteinGoalGrams,
        fatGoalGrams = fatGoalGrams,
        carbsGoalGrams = carbsGoalGrams,
        fiberGoalGrams = fiberGoalGrams,
        waterGoalMl = waterGoalMl
    )

    companion object {
        fun fromDomain(domain: DailyGoals) = DailyGoalsEntity(
            id = 1,
            caloriesGoal = domain.caloriesGoal,
            proteinGoalGrams = domain.proteinGoalGrams,
            fatGoalGrams = domain.fatGoalGrams,
            carbsGoalGrams = domain.carbsGoalGrams,
            fiberGoalGrams = domain.fiberGoalGrams,
            waterGoalMl = domain.waterGoalMl
        )
    }
}

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey val date: String,
    val amountMl: Int
)

@Entity(tableName = "favorite_products")
data class FavoriteProductEntity(
    @PrimaryKey val productId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val title: String,
    val category: String,
    val durationMinutes: Int,
    val caloriesBurned: Float,
    val distanceKm: Float = 0f,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): com.example.data.model.Workout {
        val cat = try {
            com.example.data.model.WorkoutCategory.valueOf(category)
        } catch (e: Exception) {
            com.example.data.model.WorkoutCategory.OTHER
        }
        return com.example.data.model.Workout(
            id = id,
            date = date,
            title = title,
            category = cat,
            durationMinutes = durationMinutes,
            caloriesBurned = caloriesBurned,
            distanceKm = distanceKm,
            notes = notes,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromDomain(domain: com.example.data.model.Workout): WorkoutEntity {
            return WorkoutEntity(
                id = domain.id,
                date = domain.date,
                title = domain.title,
                category = domain.category.name,
                durationMinutes = domain.durationMinutes,
                caloriesBurned = domain.caloriesBurned,
                distanceKm = domain.distanceKm,
                notes = domain.notes,
                timestamp = domain.timestamp
            )
        }
    }
}

@Entity(tableName = "daily_steps")
data class DailyStepEntity(
    @PrimaryKey val date: String,
    val steps: Int,
    val caloriesBurned: Float,
    val distanceMeters: Float = 0f,
    val activeMinutes: Int = 0,
    val isFromHealthConnect: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
