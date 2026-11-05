package com.example.data.api.tether

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class TetherApiClient(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tether_network_prefs", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_AUTH_TOKEN, value).apply()
            rebuildClients()
        }

    var authBaseUrl: String
        get() = prefs.getString(KEY_AUTH_URL, DEFAULT_AUTH_URL) ?: DEFAULT_AUTH_URL
        set(value) {
            val normalized = if (value.endsWith("/")) value else "$value/"
            prefs.edit().putString(KEY_AUTH_URL, normalized).apply()
            rebuildClients()
        }

    var diaryBaseUrl: String
        get() = prefs.getString(KEY_DIARY_URL, DEFAULT_DIARY_URL) ?: DEFAULT_DIARY_URL
        set(value) {
            val normalized = if (value.endsWith("/")) value else "$value/"
            prefs.edit().putString(KEY_DIARY_URL, normalized).apply()
            rebuildClients()
        }

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val currentToken = token
        val builder = original.newBuilder()
        if (!currentToken.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $currentToken")
        }
        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private var okHttpClient = createOkHttpClient()

    private var _authApi: TetherAuthApi = buildAuthApi()
    val authApi: TetherAuthApi get() = _authApi

    private var _diaryApi: TetherDiaryApi = buildDiaryApi()
    val diaryApi: TetherDiaryApi get() = _diaryApi

    private fun buildAuthApi(): TetherAuthApi {
        return Retrofit.Builder()
            .baseUrl(authBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TetherAuthApi::class.java)
    }

    private fun buildDiaryApi(): TetherDiaryApi {
        return Retrofit.Builder()
            .baseUrl(diaryBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TetherDiaryApi::class.java)
    }

    private fun rebuildClients() {
        okHttpClient = createOkHttpClient()
        _authApi = buildAuthApi()
        _diaryApi = buildDiaryApi()
    }

    companion object {
        const val DEFAULT_AUTH_URL = "http://10.0.2.2:8001/"
        const val DEFAULT_DIARY_URL = "http://10.0.2.2:8002/"

        private const val KEY_AUTH_TOKEN = "key_auth_jwt_token"
        private const val KEY_AUTH_URL = "key_auth_base_url"
        private const val KEY_DIARY_URL = "key_diary_base_url"

        @Volatile
        private var INSTANCE: TetherApiClient? = null

        fun getInstance(context: Context): TetherApiClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TetherApiClient(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

