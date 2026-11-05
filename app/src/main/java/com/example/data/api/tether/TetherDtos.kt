package com.example.data.api.tether

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Auth DTOs
@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "login") val login: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "login") val login: String,
    @Json(name = "password") val password: String,
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class TokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String = "bearer"
)

@JsonClass(generateAdapter = true)
data class UserResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "login") val login: String,
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class MessageResponse(
    @Json(name = "message") val message: String
)

// Health Profile DTOs
@JsonClass(generateAdapter = true)
data class HealthProfileCreate(
    @Json(name = "gender") val gender: String = "m", // "m" or "f"
    @Json(name = "age") val age: Int = 25,
    @Json(name = "weight") val weight: Float = 75f,
    @Json(name = "height") val height: Float = 1.75f, // meters
    @Json(name = "activity_level") val activityLevel: Int = 2, // 1 to 5
    @Json(name = "target_goal") val targetGoal: Int = 4, // 1: lose, 2: gain, 3: recomposition, 4: maintain
    @Json(name = "target_steps") val targetSteps: Int = 0 // 0 = auto calculate
)

@JsonClass(generateAdapter = true)
data class HealthProfileUpdate(
    @Json(name = "gender") val gender: String? = null,
    @Json(name = "age") val age: Int? = null,
    @Json(name = "weight") val weight: Float? = null,
    @Json(name = "height") val height: Float? = null,
    @Json(name = "activity_level") val activityLevel: Int? = null,
    @Json(name = "target_goal") val targetGoal: Int? = null,
    @Json(name = "target_steps") val targetSteps: Int? = null
)

@JsonClass(generateAdapter = true)
data class HealthProfileResponse(
    @Json(name = "id") val id: Int = 1,
    @Json(name = "gender") val gender: String = "m",
    @Json(name = "age") val age: Int = 25,
    @Json(name = "weight") val weight: Float = 75f,
    @Json(name = "height") val height: Float = 1.75f,
    @Json(name = "activity_level") val activityLevel: Int = 2,
    @Json(name = "target_goal") val targetGoal: Int = 4,
    @Json(name = "target_steps") val targetSteps: Int = 0,
    @Json(name = "min_target_calories") val minTargetCalories: Float = 2000f,
    @Json(name = "max_target_calories") val maxTargetCalories: Float = 2200f,
    @Json(name = "target_proteins") val targetProteins: Float = 120f,
    @Json(name = "target_fats") val targetFats: Float = 60f,
    @Json(name = "target_carbs") val targetCarbs: Float = 220f,
    @Json(name = "target_water") val targetWater: Float = 2.0f
)

// Product DTOs
@JsonClass(generateAdapter = true)
data class ProductCreate(
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "product_type") val productType: String = "other",
    @Json(name = "calories") val calories: Float,
    @Json(name = "fats") val fats: Float,
    @Json(name = "proteins") val proteins: Float,
    @Json(name = "carbs") val carbs: Float
)

@JsonClass(generateAdapter = true)
data class ProductResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "product_type") val productType: String = "other",
    @Json(name = "calories") val calories: Float,
    @Json(name = "fats") val fats: Float,
    @Json(name = "proteins") val proteins: Float,
    @Json(name = "carbs") val carbs: Float
)

// Diary Sheets and Entries
@JsonClass(generateAdapter = true)
data class SheetCreate(
    @Json(name = "sheet_date") val sheetDate: String, // YYYY-MM-DD
    @Json(name = "weight") val weight: Float? = null
)

@JsonClass(generateAdapter = true)
data class EntryCreate(
    @Json(name = "base_product_id") val baseProductId: Int? = null,
    @Json(name = "user_product_id") val userProductId: Int? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "quantity_grams") val quantityGrams: Float? = null,
    @Json(name = "calories") val calories: Float? = null,
    @Json(name = "fats") val fats: Float? = null,
    @Json(name = "proteins") val proteins: Float? = null,
    @Json(name = "carbs") val carbs: Float? = null,
    @Json(name = "product_type") val productType: String? = "other",
    @Json(name = "entry_date") val entryDate: String? = null
)

@JsonClass(generateAdapter = true)
data class EntryResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "sheet_id") val sheetId: Int,
    @Json(name = "base_product_id") val baseProductId: Int? = null,
    @Json(name = "user_product_id") val userProductId: Int? = null,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "product_type") val productType: String? = null,
    @Json(name = "quantity_grams") val quantityGrams: Float? = null,
    @Json(name = "calories") val calories: Float? = null,
    @Json(name = "fats") val fats: Float? = null,
    @Json(name = "proteins") val proteins: Float? = null,
    @Json(name = "carbs") val carbs: Float? = null,
    @Json(name = "water") val water: Float? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SheetResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "sheet_date") val sheetDate: String,
    @Json(name = "weight") val weight: Float? = null,
    @Json(name = "total_calories") val totalCalories: Float = 0f,
    @Json(name = "total_fats") val totalFats: Float = 0f,
    @Json(name = "total_proteins") val totalProteins: Float = 0f,
    @Json(name = "total_carbs") val totalCarbs: Float = 0f,
    @Json(name = "total_water") val totalWater: Float = 0f,
    @Json(name = "entries") val entries: List<EntryResponse> = emptyList()
)

// Full Sync DTOs
@JsonClass(generateAdapter = true)
data class SyncDataRequest(
    @Json(name = "health") val health: HealthProfileUpdate? = null,
    @Json(name = "products") val products: List<ProductCreate>? = null,
    @Json(name = "sheets") val sheets: List<SheetCreate>? = null,
    @Json(name = "entries") val entries: List<EntryCreate>? = null
)

@JsonClass(generateAdapter = true)
data class SyncResponse(
    @Json(name = "health") val health: HealthProfileResponse? = null,
    @Json(name = "products") val products: List<ProductResponse> = emptyList(),
    @Json(name = "sheets") val sheets: List<SheetResponse> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SyncStatusResponse(
    @Json(name = "status") val status: String
)

