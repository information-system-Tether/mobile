package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    // Logged meals
    @Query("SELECT * FROM logged_foods WHERE date = :date ORDER BY timestamp ASC")
    fun getLoggedFoodsForDate(date: String): Flow<List<LoggedFoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoggedFood(food: LoggedFoodEntity): Long

    @Update
    suspend fun updateLoggedFood(food: LoggedFoodEntity)

    @Delete
    suspend fun deleteLoggedFood(food: LoggedFoodEntity)

    @Query("DELETE FROM logged_foods WHERE id = :id")
    suspend fun deleteLoggedFoodById(id: Long)

    @Query("SELECT DISTINCT date FROM logged_foods ORDER BY date DESC")
    fun getAllLoggedDates(): Flow<List<String>>

    // Custom Products
    @Query("SELECT * FROM custom_products ORDER BY id DESC")
    fun getAllCustomProducts(): Flow<List<CustomProductEntity>>

    @Query("SELECT * FROM custom_products WHERE name LIKE '%' || :query || '%'")
    suspend fun searchCustomProducts(query: String): List<CustomProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomProduct(product: CustomProductEntity): Long

    // Goals
    @Query("SELECT * FROM daily_goals WHERE id = 1 LIMIT 1")
    fun getDailyGoals(): Flow<DailyGoalsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setDailyGoals(goals: DailyGoalsEntity)

    // Water
    @Query("SELECT * FROM water_logs WHERE date = :date LIMIT 1")
    fun getWaterLogForDate(date: String): Flow<WaterLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setWaterLog(waterLog: WaterLogEntity)

    // Stats and Counts for Cloud Sync
    @Query("SELECT COUNT(*) FROM logged_foods")
    suspend fun getTotalLoggedFoodsCount(): Int

    @Query("SELECT COUNT(*) FROM custom_products")
    suspend fun getTotalCustomProductsCount(): Int

    @Query("SELECT COUNT(*) FROM water_logs")
    suspend fun getTotalWaterLogsCount(): Int

    // Favorites
    @Query("SELECT productId FROM favorite_products")
    fun getAllFavoriteProductIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteProduct(favorite: FavoriteProductEntity)

    @Query("DELETE FROM favorite_products WHERE productId = :productId")
    suspend fun removeFavoriteProduct(productId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_products WHERE productId = :productId)")
    suspend fun isProductFavorite(productId: String): Boolean

    // Workouts
    @Query("SELECT * FROM workouts WHERE date = :date ORDER BY timestamp DESC")
    fun getWorkoutsForDate(date: String): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkout(id: Long)

    @Query("SELECT COUNT(*) FROM workouts")
    suspend fun getTotalWorkoutsCount(): Int

    // Steps
    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    fun getDailySteps(date: String): Flow<DailyStepEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setDailySteps(steps: DailyStepEntity)
}
