package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.CustomProductEntity
import com.example.data.db.DailyGoalsEntity
import com.example.data.db.DailyStepEntity
import com.example.data.db.FavoriteProductEntity
import com.example.data.db.LoggedFoodEntity
import com.example.data.db.WaterLogEntity
import com.example.data.db.WorkoutEntity
import com.example.data.model.DailyGoals
import com.example.data.model.DailyNutrientStats
import com.example.data.model.FoodProduct
import com.example.data.model.LoggedFood
import com.example.data.model.MealType
import com.example.data.model.StepActivityStats
import com.example.data.model.Workout
import com.example.data.model.DayStatsSummary
import com.example.data.model.WeeklyAnalytics
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class NutritionRepository(private val database: AppDatabase) {
    private val dao = database.foodDao()

    fun getDailyStats(date: String): Flow<DailyNutrientStats> {
        val foodsFlow = dao.getLoggedFoodsForDate(date)
        val goalsFlow = dao.getDailyGoals()
        val waterFlow = dao.getWaterLogForDate(date)
        val workoutsFlow = dao.getWorkoutsForDate(date)
        val stepsFlow = dao.getDailySteps(date)

        return combine(
            foodsFlow,
            goalsFlow,
            waterFlow,
            workoutsFlow,
            stepsFlow
        ) { foodsEntities, goalsEntity, waterEntity, workoutEntities, stepsEntity ->
            val domainFoods = foodsEntities.map { it.toDomain() }
            val goals = goalsEntity?.toDomain() ?: DailyGoals()
            val waterMl = waterEntity?.amountMl ?: 0

            var totalKcal = 0f
            var totalProtein = 0f
            var totalFat = 0f
            var totalCarbs = 0f
            var totalFiber = 0f
            var totalSugar = 0f
            var totalSodium = 0f

            val mealMap = mutableMapOf<MealType, MutableList<LoggedFood>>()
            MealType.values().forEach { mealMap[it] = mutableListOf() }

            domainFoods.forEach { food ->
                totalKcal += food.calories
                totalProtein += food.protein
                totalFat += food.fat
                totalCarbs += food.carbs
                totalFiber += food.fiber
                totalSugar += food.sugar
                totalSodium += food.sodium
                mealMap[food.mealType]?.add(food)
            }

            val workoutKcal = workoutEntities.sumOf { it.caloriesBurned.toDouble() }.toFloat()
            val stepsCount = stepsEntity?.steps ?: 0
            val stepsKcal = stepsEntity?.caloriesBurned ?: StepActivityStats.calculateCaloriesFromSteps(stepsCount)
            val totalBurned = workoutKcal + stepsKcal

            DailyNutrientStats(
                date = date,
                goals = goals,
                totalCalories = totalKcal,
                totalProtein = totalProtein,
                totalFat = totalFat,
                totalCarbs = totalCarbs,
                totalFiber = totalFiber,
                totalSugar = totalSugar,
                totalSodium = totalSodium,
                waterConsumedMl = waterMl,
                mealBreakdown = mealMap,
                allLoggedFoods = domainFoods,
                totalBurnedCalories = totalBurned,
                stepsCount = stepsCount,
                stepsBurnedCalories = stepsKcal,
                workoutCount = workoutEntities.size
            )
        }
    }

    fun getWeeklyAnalytics(endDateStr: String): Flow<WeeklyAnalytics> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayNameFormat = SimpleDateFormat("EE", Locale("ru"))
        val cal = Calendar.getInstance()
        val parsedDate = try { sdf.parse(endDateStr) ?: Date() } catch (e: Exception) { Date() }
        cal.time = parsedDate

        val dateList = mutableListOf<String>()
        val dayNames = mutableListOf<String>()
        val tempCal = Calendar.getInstance()
        tempCal.time = parsedDate
        tempCal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0 until 7) {
            dateList.add(sdf.format(tempCal.time))
            dayNames.add(dayNameFormat.format(tempCal.time).replace(".", "").replaceFirstChar { it.uppercase() })
            tempCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        val startDateStr = dateList.first()

        return combine(
            dao.getLoggedFoodsBetweenDates(startDateStr, endDateStr),
            dao.getDailyGoals(),
            dao.getWaterLogsBetweenDates(startDateStr, endDateStr),
            dao.getDailyStepsBetweenDates(startDateStr, endDateStr)
        ) { foodsList, goalsEntity, waterList, stepsList ->
            val goals = goalsEntity?.toDomain() ?: DailyGoals()
            val foodsByDate = foodsList.groupBy { it.date }
            val waterByDate = waterList.associateBy { it.date }
            val stepsByDate = stepsList.associateBy { it.date }

            val daysSummary = dateList.mapIndexed { idx, date ->
                val dayFoods = foodsByDate[date] ?: emptyList()
                val cals = dayFoods.sumOf { it.calories.toDouble() }.toFloat()
                val prot = dayFoods.sumOf { it.protein.toDouble() }.toFloat()
                val fat = dayFoods.sumOf { it.fat.toDouble() }.toFloat()
                val carbs = dayFoods.sumOf { it.carbs.toDouble() }.toFloat()
                val waterMl = waterByDate[date]?.amountMl ?: 0
                val stepObj = stepsByDate[date]
                val steps = stepObj?.steps ?: 0
                val burned = stepObj?.caloriesBurned ?: StepActivityStats.calculateCaloriesFromSteps(steps)

                DayStatsSummary(
                    date = date,
                    dayOfWeek = dayNames[idx],
                    calories = cals,
                    targetCalories = goals.caloriesGoal,
                    protein = prot,
                    targetProtein = goals.proteinGoalGrams,
                    fat = fat,
                    targetFat = goals.fatGoalGrams,
                    carbs = carbs,
                    targetCarbs = goals.carbsGoalGrams,
                    waterMl = waterMl,
                    targetWaterMl = goals.waterGoalMl,
                    steps = steps,
                    targetSteps = goals.stepGoal,
                    burnedCalories = burned
                )
            }

            val avgCals = if (daysSummary.isNotEmpty()) daysSummary.map { it.calories }.average().toFloat() else 0f
            val avgProt = if (daysSummary.isNotEmpty()) daysSummary.map { it.protein }.average().toFloat() else 0f
            val avgFat = if (daysSummary.isNotEmpty()) daysSummary.map { it.fat }.average().toFloat() else 0f
            val avgCarbs = if (daysSummary.isNotEmpty()) daysSummary.map { it.carbs }.average().toFloat() else 0f
            val avgWater = if (daysSummary.isNotEmpty()) daysSummary.map { it.waterMl }.average().toInt() else 0
            val avgSteps = if (daysSummary.isNotEmpty()) daysSummary.map { it.steps }.average().toInt() else 0
            val totalBurn = daysSummary.sumOf { it.burnedCalories.toDouble() }.toFloat()

            WeeklyAnalytics(
                days = daysSummary,
                averageCalories = avgCals,
                averageProtein = avgProt,
                averageFat = avgFat,
                averageCarbs = avgCarbs,
                averageWaterMl = avgWater,
                averageSteps = avgSteps,
                totalBurnedCalories = totalBurn,
                targetCalories = goals.caloriesGoal,
                targetWaterMl = goals.waterGoalMl,
                targetSteps = goals.stepGoal
            )
        }
    }

    suspend fun logFood(food: LoggedFood): Long = withContext(Dispatchers.IO) {
        dao.insertLoggedFood(LoggedFoodEntity.fromDomain(food))
    }

    suspend fun deleteLoggedFood(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteLoggedFoodById(id)
    }

    suspend fun updateWater(date: String, newAmountMl: Int) = withContext(Dispatchers.IO) {
        dao.setWaterLog(WaterLogEntity(date = date, amountMl = newAmountMl.coerceAtLeast(0)))
    }

    suspend fun updateGoals(goals: DailyGoals) = withContext(Dispatchers.IO) {
        dao.setDailyGoals(DailyGoalsEntity.fromDomain(goals))
    }

    suspend fun updateStepGoal(newGoal: Int) = withContext(Dispatchers.IO) {
        val current = dao.getDailyGoals().firstOrNull()?.toDomain() ?: DailyGoals()
        dao.setDailyGoals(DailyGoalsEntity.fromDomain(current.copy(stepGoal = newGoal)))
    }

    suspend fun searchProducts(query: String, favoriteIds: Set<String> = emptySet()): List<FoodProduct> = withContext(Dispatchers.IO) {
        val customMatches = if (query.isNotBlank()) {
            dao.searchCustomProducts(query).map { it.toDomain() }
        } else {
            emptyList()
        }
        val defaultMatches = DefaultFoodDatabase.search(query)

        val seen = mutableSetOf<String>()
        val result = mutableListOf<FoodProduct>()

        (customMatches + defaultMatches).forEach { prod ->
            val key = prod.id
            val isFav = favoriteIds.contains(prod.id)
            val enrichedProd = prod.copy(isFavorite = isFav)

            if (key.isNotBlank() && seen.add(key)) {
                result.add(enrichedProd)
            } else if (key.isBlank()) {
                result.add(enrichedProd)
            }
        }

        // Sort: favorites first, then by name
        result.sortedWith(compareByDescending<FoodProduct> { it.isFavorite }.thenBy { it.name })
    }

    suspend fun saveCustomProduct(product: FoodProduct): Long = withContext(Dispatchers.IO) {
        dao.insertCustomProduct(CustomProductEntity.fromDomain(product))
    }

    fun getAllCustomProducts(): Flow<List<CustomProductEntity>> = dao.getAllCustomProducts()

    // Favorites
    fun getFavoriteProductIds(): Flow<List<String>> = dao.getAllFavoriteProductIds()

    suspend fun toggleFavorite(productId: String) = withContext(Dispatchers.IO) {
        if (dao.isProductFavorite(productId)) {
            dao.removeFavoriteProduct(productId)
        } else {
            dao.addFavoriteProduct(FavoriteProductEntity(productId))
        }
    }

    // Workouts
    fun getWorkoutsForDate(date: String): Flow<List<Workout>> {
        return combine(dao.getWorkoutsForDate(date)) { list ->
            list[0].map { it.toDomain() }
        }
    }

    suspend fun addWorkout(workout: Workout): Long = withContext(Dispatchers.IO) {
        dao.insertWorkout(WorkoutEntity.fromDomain(workout))
    }

    suspend fun deleteWorkout(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteWorkout(id)
    }

    // Steps
    fun getDailySteps(date: String): Flow<DailyStepEntity?> = dao.getDailySteps(date)

    suspend fun updateDailySteps(date: String, steps: Int, burnedKcal: Float, isHealthConnect: Boolean) = withContext(Dispatchers.IO) {
        dao.setDailySteps(
            DailyStepEntity(
                date = date,
                steps = steps,
                caloriesBurned = burnedKcal,
                distanceMeters = steps * 0.762f,
                activeMinutes = (steps / 105).coerceAtLeast(0),
                isFromHealthConnect = isHealthConnect
            )
        )
    }

    suspend fun getTotalCounts(): Triple<Int, Int, Int> = withContext(Dispatchers.IO) {
        Triple(
            dao.getTotalLoggedFoodsCount(),
            dao.getTotalCustomProductsCount(),
            dao.getTotalWaterLogsCount()
        )
    }
}
