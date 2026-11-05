package com.example.data.api.tether

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TetherDiaryApi {

    @GET("health/me")
    suspend fun getHealthProfile(): Response<HealthProfileResponse>

    @POST("health/me")
    suspend fun createHealthProfile(@Body body: HealthProfileCreate): Response<HealthProfileResponse>

    @PATCH("health/me")
    suspend fun updateHealthProfile(@Body body: HealthProfileUpdate): Response<HealthProfileResponse>

    @GET("products/base")
    suspend fun getBaseProducts(): Response<List<ProductResponse>>

    @GET("products/me")
    suspend fun getUserProducts(): Response<List<ProductResponse>>

    @POST("products/me")
    suspend fun createUserProduct(@Body body: ProductCreate): Response<ProductResponse>

    @DELETE("products/me/{id}")
    suspend fun deleteUserProduct(@Path("id") id: Int): Response<Unit>

    @GET("sheets/me")
    suspend fun getSheets(): Response<List<SheetResponse>>

    @GET("sheets/me/{date}")
    suspend fun getSheet(@Path("date") date: String): Response<SheetResponse>

    @POST("sheets/me")
    suspend fun createSheet(@Body body: SheetCreate): Response<SheetResponse>

    @POST("sheets/me/{date}/entries")
    suspend fun createEntry(
        @Path("date") date: String,
        @Body body: EntryCreate
    ): Response<EntryResponse>

    @DELETE("entries/me/{id}")
    suspend fun deleteEntry(@Path("id") id: Int): Response<Unit>

    @GET("sync/me")
    suspend fun getSync(): Response<SyncResponse>

    @POST("sync/me")
    suspend fun syncUser(@Body body: SyncDataRequest): Response<SyncStatusResponse>
}

