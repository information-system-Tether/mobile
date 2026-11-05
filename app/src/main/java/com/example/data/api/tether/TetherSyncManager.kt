package com.example.data.api.tether

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.db.CustomProductEntity
import com.example.data.db.DailyGoalsEntity
import com.example.data.db.LoggedFoodEntity
import com.example.data.db.WaterLogEntity
import com.example.data.model.MealType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class TetherSyncManager(private val context: Context) {

    private val apiClient = TetherApiClient.getInstance(context)
    private val db = AppDatabase.getInstance(context)
    private val foodDao = db.foodDao()

    suspend fun performFullSync(): Result<Int> = withContext(Dispatchers.IO) {
        val token = apiClient.token
        if (token.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("Требуется авторизация"))
        }

        try {
            // 1. Pull latest server data
            val syncResponse = apiClient.diaryApi.getSync()
            var syncedCount = 0

            if (syncResponse.isSuccessful && syncResponse.body() != null) {
                val data = syncResponse.body()!!

                // Update health profile & target goals
                data.health?.let { hp ->
                    val updatedGoals = DailyGoalsEntity(
                        id = 1,
                        caloriesGoal = hp.minTargetCalories,
                        proteinGoalGrams = hp.targetProteins,
                        fatGoalGrams = hp.targetFats,
                        carbsGoalGrams = hp.targetCarbs,
                        fiberGoalGrams = 30f,
                        waterGoalMl = (hp.targetWater * 1000).toInt(),
                        stepGoal = if (hp.targetSteps > 0) hp.targetSteps else 10000
                    )
                    foodDao.setDailyGoals(updatedGoals)
                    syncedCount++
                }

                // Sync custom products from server into local DB
                data.products.forEach { prod ->
                    val entity = CustomProductEntity(
                        id = prod.id.toLong(),
                        name = prod.name,
                        desc = prod.description ?: "",
                        caloriesPer100g = prod.calories,
                        proteinPer100g = prod.proteins,
                        fatPer100g = prod.fats,
                        carbsPer100g = prod.carbs,
                        defaultServingGrams = 100f
                    )
                    foodDao.insertCustomProduct(entity)
                    syncedCount++
                }

                // Sync base catalog products from server
                try {
                    val baseProdsResp = apiClient.diaryApi.getBaseProducts()
                    if (baseProdsResp.isSuccessful && baseProdsResp.body() != null) {
                        baseProdsResp.body()!!.forEach { baseProd ->
                            val entity = CustomProductEntity(
                                id = 100000L + baseProd.id.toLong(),
                                name = baseProd.name,
                                desc = baseProd.description ?: "",
                                caloriesPer100g = baseProd.calories,
                                proteinPer100g = baseProd.proteins,
                                fatPer100g = baseProd.fats,
                                carbsPer100g = baseProd.carbs,
                                defaultServingGrams = 100f
                            )
                            foodDao.insertCustomProduct(entity)
                            syncedCount++
                        }
                    }
                } catch (e: Exception) {
                    // Ignore base products sync network error
                }

                // Sync sheets and entries from server into local DB
                data.sheets.forEach { sheet ->
                    if (sheet.totalWater > 0f) {
                        foodDao.setWaterLog(
                            WaterLogEntity(
                                date = sheet.sheetDate,
                                amountMl = (sheet.totalWater * 1000).toInt()
                            )
                        )
                    }

                    sheet.entries.forEach { entry ->
                        val foodEntity = LoggedFoodEntity(
                            id = entry.id.toLong(),
                            date = sheet.sheetDate,
                            mealType = MealType.LUNCH.name,
                            foodName = entry.title,
                            desc = entry.description ?: "",
                            weightGrams = entry.quantityGrams ?: 100f,
                            calories = entry.calories ?: 0f,
                            protein = entry.proteins ?: 0f,
                            fat = entry.fats ?: 0f,
                            carbs = entry.carbs ?: 0f,
                            timestamp = System.currentTimeMillis()
                        )
                        foodDao.insertLoggedFood(foodEntity)
                        syncedCount++
                    }
                }
            }

            // 2. Push local custom products, logged foods and goals to server
            val localProducts = foodDao.getAllCustomProducts().firstOrNull() ?: emptyList()
            val uploadProducts = if (localProducts.isNotEmpty()) {
                localProducts.map { p ->
                    ProductCreate(
                        name = p.name,
                        description = p.desc.ifEmpty { null },
                        productType = "other",
                        calories = p.caloriesPer100g,
                        fats = p.fatPer100g,
                        proteins = p.proteinPer100g,
                        carbs = p.carbsPer100g
                    )
                }
            } else null

            val localFoods = foodDao.getAllLoggedFoodsList()
            val uploadEntries = if (localFoods.isNotEmpty()) {
                localFoods.map { f ->
                    EntryCreate(
                        name = f.foodName,
                        title = f.foodName,
                        description = f.desc.ifEmpty { null },
                        quantityGrams = f.weightGrams,
                        calories = f.calories,
                        fats = f.fat,
                        proteins = f.protein,
                        carbs = f.carbs,
                        productType = "other",
                        entryDate = f.date
                    )
                }
            } else null

            val currentGoals = foodDao.getDailyGoals().firstOrNull()
            val healthUpdate = currentGoals?.let {
                HealthProfileUpdate(targetSteps = it.stepGoal)
            }

            if (uploadProducts != null || uploadEntries != null || healthUpdate != null) {
                apiClient.diaryApi.syncUser(
                    SyncDataRequest(
                        health = healthUpdate,
                        products = uploadProducts,
                        entries = uploadEntries
                    )
                )
            }

            Result.success(syncedCount.coerceAtLeast(1))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun uploadHealthProfile(
        gender: String,
        age: Int,
        weight: Float,
        heightMeters: Float,
        activityLevel: Int,
        targetGoal: Int,
        targetSteps: Int
    ): Result<HealthProfileResponse> = withContext(Dispatchers.IO) {
        try {
            val req = HealthProfileCreate(
                gender = gender,
                age = age,
                weight = weight,
                height = heightMeters,
                activityLevel = activityLevel,
                targetGoal = targetGoal,
                targetSteps = targetSteps
            )
            val res = apiClient.diaryApi.createHealthProfile(req)
            if (res.isSuccessful && res.body() != null) {
                val hp = res.body()!!
                // Update local goals from mathematical server calculations
                foodDao.setDailyGoals(
                    DailyGoalsEntity(
                        id = 1,
                        caloriesGoal = hp.minTargetCalories,
                        proteinGoalGrams = hp.targetProteins,
                        fatGoalGrams = hp.targetFats,
                        carbsGoalGrams = hp.targetCarbs,
                        waterGoalMl = (hp.targetWater * 1000).toInt()
                    )
                )
                Result.success(hp)
            } else {
                Result.failure(Exception("Ошибка сохранения профиля: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

