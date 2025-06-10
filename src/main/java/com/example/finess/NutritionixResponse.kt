package com.example.finess

// NutritionixResponse.kt
data class NutritionixResponse(
    val foods: List<FoodItem>
)

//Class to obtain food object information.
data class FoodItem(
    val food_name: String,
    val nf_calories: Double,
    val nf_protein: Double,
    val nf_total_fat: Double,
    val nf_total_carbohydrate: Double
)
