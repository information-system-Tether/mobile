package com.example.data.api.tether

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TetherAuthApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<UserResponse>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<TokenResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<UserResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<MessageResponse>
}

