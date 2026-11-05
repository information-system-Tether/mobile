package com.example.data.repository

import com.example.data.model.FoodProduct

object DefaultFoodDatabase {
    val items: List<FoodProduct> = listOf(
        FoodProduct(
            id = "default_milk",
            name = "Молоко 2.5%",
            desc = "Пастеризованное молоко",
            caloriesPer100g = 60f,
            proteinPer100g = 3.0f,
            fatPer100g = 2.5f,
            carbsPer100g = 4.7f,
            category = "Молочные продукты"
        ),
        FoodProduct(
            id = "default_chicken",
            name = "Куриное филе (грудка)",
            desc = "Отварная куриная грудка",
            caloriesPer100g = 165f,
            proteinPer100g = 31.0f,
            fatPer100g = 3.6f,
            carbsPer100g = 0f,
            category = "Мясо и птица"
        ),
        FoodProduct(
            id = "default_oats",
            name = "Овсяные хлопья",
            desc = "Геркулес цельнозерновой",
            caloriesPer100g = 352f,
            proteinPer100g = 12.3f,
            fatPer100g = 6.2f,
            carbsPer100g = 61.8f,
            fiberPer100g = 10.6f,
            category = "Крупы и злаки"
        ),
        FoodProduct(
            id = "default_banana",
            name = "Банан",
            desc = "Свежий банан",
            caloriesPer100g = 89f,
            proteinPer100g = 1.1f,
            fatPer100g = 0.3f,
            carbsPer100g = 22.8f,
            fiberPer100g = 2.6f,
            category = "Фрукты"
        ),
        FoodProduct(
            id = "default_egg",
            name = "Яйцо куриное C1",
            desc = "1 яйцо ~ 55г",
            caloriesPer100g = 155f,
            proteinPer100g = 13.0f,
            fatPer100g = 11.0f,
            carbsPer100g = 1.1f,
            category = "Яйца"
        )
    )

    fun search(query: String): List<FoodProduct> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return items
        return items.filter {
            it.name.lowercase().contains(q) ||
            it.desc.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
        }
    }
}
