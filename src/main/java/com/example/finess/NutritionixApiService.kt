package com.example.finess

import retrofit2.http.Body
import retrofit2.http.POST

// NutritionixApiService.kt
interface NutritionixApiService {
    @POST("natural/nutrients")
    suspend fun searchFood(
        @Body request: NutritionQueryRequest
    ): NutritionixResponse
}

// Request body for Nutritionix (send query as JSON)
data class NutritionQueryRequest(
    val query: String
)