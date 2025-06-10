package com.example.finess

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// RetrofitClient.kt for api requests.
object RetrofitClient {
    private const val BASE_URL = "https://trackapi.nutritionix.com/v2/"
    private const val APP_ID = "e32decbb"
    private const val API_KEY = "9bac46bfa7f7a3c786a7cf67ad098642"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("x-app-id", APP_ID)
                .addHeader("x-app-key", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)) // Optional
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: NutritionixApiService = retrofit.create(NutritionixApiService::class.java)
}