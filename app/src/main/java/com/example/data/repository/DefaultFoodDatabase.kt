package com.example.data.repository

import com.example.data.model.FoodProduct

object DefaultFoodDatabase {
    val items = listOf(
        FoodProduct(
            id = "def_1",
            name = "Овсяные хлопья «Геркулес»",
            desc = "Традиционный геркулес высокого качества",
            caloriesPer100g = 352f,
            proteinPer100g = 12.3f,
            fatPer100g = 6.2f,
            carbsPer100g = 61.8f,
            fiberPer100g = 9.8f,
            sugarPer100g = 1.0f,
            sodiumPer100g = 0.01f,
            defaultServingGrams = 60f,
            category = "Крупы и злаки"
        ),
        FoodProduct(
            id = "def_2",
            name = "Куриное филе (грудка)",
            desc = "Свежее охлажденное куриное филе",
            caloriesPer100g = 113f,
            proteinPer100g = 23.6f,
            fatPer100g = 1.9f,
            carbsPer100g = 0.4f,
            fiberPer100g = 0.0f,
            sugarPer100g = 0.0f,
            sodiumPer100g = 0.06f,
            defaultServingGrams = 180f,
            category = "Мясо и птица"
        ),
        FoodProduct(
            id = "def_3",
            name = "Гречневая крупа ядрица",
            desc = "Цельное отборное гречневое зерно",
            caloriesPer100g = 313f,
            proteinPer100g = 12.6f,
            fatPer100g = 3.3f,
            carbsPer100g = 62.1f,
            fiberPer100g = 11.3f,
            sugarPer100g = 0.5f,
            sodiumPer100g = 0.02f,
            defaultServingGrams = 70f,
            category = "Крупы и злаки"
        ),
        FoodProduct(
            id = "def_4",
            name = "Творог 5%",
            desc = "Натуральный классический творог",
            caloriesPer100g = 121f,
            proteinPer100g = 16.0f,
            fatPer100g = 5.0f,
            carbsPer100g = 3.0f,
            fiberPer100g = 0.0f,
            sugarPer100g = 3.0f,
            sodiumPer100g = 0.08f,
            defaultServingGrams = 200f,
            category = "Молочные продукты"
        ),
        FoodProduct(
            id = "def_5",
            name = "Банан свежий",
            desc = "Сладкие спелые бананы",
            caloriesPer100g = 89f,
            proteinPer100g = 1.1f,
            fatPer100g = 0.3f,
            carbsPer100g = 22.8f,
            fiberPer100g = 2.6f,
            sugarPer100g = 12.2f,
            sodiumPer100g = 0.01f,
            defaultServingGrams = 120f,
            category = "Фрукты и ягоды"
        ),
        FoodProduct(
            id = "def_6",
            name = "Яйцо куриное категории C0",
            desc = "Отборное диетическое яйцо C0",
            caloriesPer100g = 157f,
            proteinPer100g = 12.7f,
            fatPer100g = 11.5f,
            carbsPer100g = 0.7f,
            fiberPer100g = 0.0f,
            sugarPer100g = 0.3f,
            sodiumPer100g = 0.14f,
            defaultServingGrams = 60f,
            category = "Яйца"
        ),
        FoodProduct(
            id = "def_7",
            name = "Йогурт греческий натуральный 2%",
            desc = "Густой высокобелковый йогурт",
            caloriesPer100g = 67f,
            proteinPer100g = 8.0f,
            fatPer100g = 2.0f,
            carbsPer100g = 4.2f,
            fiberPer100g = 0.0f,
            sugarPer100g = 4.2f,
            sodiumPer100g = 0.05f,
            defaultServingGrams = 140f,
            category = "Молочные продукты"
        ),
        FoodProduct(
            id = "def_8",
            name = "Авокадо Хасс",
            desc = "Маслянистое спелое авокадо",
            caloriesPer100g = 160f,
            proteinPer100g = 2.0f,
            fatPer100g = 14.7f,
            carbsPer100g = 8.5f,
            fiberPer100g = 6.7f,
            sugarPer100g = 0.7f,
            sodiumPer100g = 0.01f,
            defaultServingGrams = 80f,
            category = "Овощи и зелень"
        ),
        FoodProduct(
            id = "def_9",
            name = "Лосось атлантический (семга)",
            desc = "Охлажденное филе атлантического лосося",
            caloriesPer100g = 208f,
            proteinPer100g = 20.4f,
            fatPer100g = 13.4f,
            carbsPer100g = 0.0f,
            fiberPer100g = 0.0f,
            sugarPer100g = 0.0f,
            sodiumPer100g = 0.06f,
            defaultServingGrams = 150f,
            category = "Рыба и морепродукты"
        ),
        FoodProduct(
            id = "def_10",
            name = "Хлеб ржаной цельнозерновой",
            desc = "Ароматный ржаной цельнозерновой хлеб",
            caloriesPer100g = 207f,
            proteinPer100g = 6.8f,
            fatPer100g = 1.3f,
            carbsPer100g = 40.7f,
            fiberPer100g = 7.1f,
            sugarPer100g = 3.5f,
            sodiumPer100g = 0.45f,
            defaultServingGrams = 40f,
            category = "Хлеб и выпечка"
        ),
        FoodProduct(
            id = "def_11",
            name = "Протеиновый батончик",
            desc = "Низкокалорийный батончик с высоким белком",
            caloriesPer100g = 320f,
            proteinPer100g = 33.3f,
            fatPer100g = 10.8f,
            carbsPer100g = 9.2f,
            fiberPer100g = 33.3f,
            sugarPer100g = 1.5f,
            sodiumPer100g = 0.12f,
            defaultServingGrams = 60f,
            category = "Спортивное питание"
        ),
        FoodProduct(
            id = "def_12",
            name = "Молоко пастеризованное 3.2%",
            desc = "Питьевое пастеризованное молоко",
            caloriesPer100g = 60f,
            proteinPer100g = 3.0f,
            fatPer100g = 3.2f,
            carbsPer100g = 4.7f,
            fiberPer100g = 0.0f,
            sugarPer100g = 4.7f,
            sodiumPer100g = 0.05f,
            defaultServingGrams = 200f,
            category = "Молочные продукты"
        ),
        FoodProduct(
            id = "def_13",
            name = "Яблоко зеленое Гренни Смит",
            desc = "Сочное кисло-сладкое зеленое яблоко",
            caloriesPer100g = 52f,
            proteinPer100g = 0.3f,
            fatPer100g = 0.2f,
            carbsPer100g = 13.8f,
            fiberPer100g = 2.4f,
            sugarPer100g = 10.4f,
            sodiumPer100g = 0.01f,
            defaultServingGrams = 160f,
            category = "Фрукты и ягоды"
        ),
        FoodProduct(
            id = "def_14",
            name = "Рис белый Басмати",
            desc = "Длиннозерный ароматный белый рис",
            caloriesPer100g = 345f,
            proteinPer100g = 7.5f,
            fatPer100g = 1.0f,
            carbsPer100g = 76.0f,
            fiberPer100g = 1.8f,
            sugarPer100g = 0.2f,
            sodiumPer100g = 0.01f,
            defaultServingGrams = 75f,
            category = "Крупы и злаки"
        ),
        FoodProduct(
            id = "def_15",
            name = "Шоколад горький 75% какао",
            desc = "Темный горький шоколад с высоким какао",
            caloriesPer100g = 545f,
            proteinPer100g = 8.5f,
            fatPer100g = 38.0f,
            carbsPer100g = 39.0f,
            fiberPer100g = 10.5f,
            sugarPer100g = 28.0f,
            sodiumPer100g = 0.02f,
            defaultServingGrams = 25f,
            category = "Сладости"
        ),
        FoodProduct(
            id = "def_16",
            name = "Миндаль жареный несоленый",
            desc = "Хрустящий жареный ядерный миндаль",
            caloriesPer100g = 609f,
            proteinPer100g = 18.6f,
            fatPer100g = 53.7f,
            carbsPer100g = 13.0f,
            fiberPer100g = 10.0f,
            sugarPer100g = 4.2f,
            sodiumPer100g = 0.01f,
            defaultServingGrams = 30f,
            category = "Орехи и семена"
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
