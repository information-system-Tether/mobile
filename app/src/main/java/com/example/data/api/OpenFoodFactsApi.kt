package com.example.data.api

import com.example.data.model.FoodProduct
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ProductResponse(
    @Json(name = "status") val status: Int? = null,
    @Json(name = "status_verbose") val statusVerbose: String? = null,
    @Json(name = "code") val code: String? = null,
    @Json(name = "product") val product: ProductDto? = null
)

@JsonClass(generateAdapter = true)
data class ProductDto(
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "product_name_ru") val productNameRu: String? = null,
    @Json(name = "product_name_en") val productNameEn: String? = null,
    @Json(name = "generic_name") val genericName: String? = null,
    @Json(name = "brands") val brands: String? = null,
    @Json(name = "image_front_url") val imageFrontUrl: String? = null,
    @Json(name = "image_front_small_url") val imageFrontSmallUrl: String? = null,
    @Json(name = "serving_size") val servingSize: String? = null,
    @Json(name = "nutriments") val nutriments: NutrimentsDto? = null
) {
    fun toFoodProduct(barcode: String): FoodProduct {
        val resolvedName = (productNameRu ?: productName ?: productNameEn ?: genericName ?: "Продукт ($barcode)").trim()
        val kcal = nutriments?.energyKcal100g
            ?: ((nutriments?.energy100g ?: 0f) / 4.184f)
        val protein = nutriments?.proteins100g ?: 0f
        val fat = nutriments?.fat100g ?: 0f
        val carbs = nutriments?.carbohydrates100g ?: 0f
        val fiber = nutriments?.fiber100g ?: 0f
        val sugar = nutriments?.sugars100g ?: 0f
        val sodium = nutriments?.sodium100g ?: ((nutriments?.salt100g ?: 0f) * 0.4f)

        return FoodProduct(
            id = barcode,
            name = resolvedName,
            desc = brands ?: "",
            caloriesPer100g = kcal,
            proteinPer100g = protein,
            fatPer100g = fat,
            carbsPer100g = carbs,
            fiberPer100g = fiber,
            sugarPer100g = sugar,
            sodiumPer100g = sodium,
            imageUrl = imageFrontSmallUrl ?: imageFrontUrl,
            defaultServingGrams = 100f,
            category = "Пользовательское"
        )
    }
}

@JsonClass(generateAdapter = true)
data class NutrimentsDto(
    @Json(name = "energy-kcal_100g") val energyKcal100g: Float? = null,
    @Json(name = "energy_100g") val energy100g: Float? = null,
    @Json(name = "proteins_100g") val proteins100g: Float? = null,
    @Json(name = "fat_100g") val fat100g: Float? = null,
    @Json(name = "carbohydrates_100g") val carbohydrates100g: Float? = null,
    @Json(name = "fiber_100g") val fiber100g: Float? = null,
    @Json(name = "sugars_100g") val sugars100g: Float? = null,
    @Json(name = "sodium_100g") val sodium100g: Float? = null,
    @Json(name = "salt_100g") val salt100g: Float? = null
)

interface OpenFoodFactsApiService {
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): ProductResponse
}

object OpenFoodFactsClient {
    private const val BASE_URL = "https://world.openfoodfacts.org/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "CalorieTrackerAndroid/1.0 (Android; openfoodfacts integration)")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: OpenFoodFactsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenFoodFactsApiService::class.java)
    }

    suspend fun fetchProductByBarcode(barcode: String): Result<FoodProduct> {
        return try {
            val response = api.getProductByBarcode(barcode)
            if (response.status == 1 && response.product != null) {
                Result.success(response.product.toFoodProduct(barcode))
            } else {
                Result.failure(Exception(response.statusVerbose ?: "Товар не найден в Open Food Facts"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
