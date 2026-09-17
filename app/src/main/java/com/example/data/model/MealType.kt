package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.ui.graphics.vector.ImageVector

enum class MealType(val displayNameRu: String, val defaultCalorieShare: Float) {
    BREAKFAST("Завтрак", 0.25f),
    LUNCH("Обед", 0.35f),
    DINNER("Ужин", 0.25f),
    SNACK("Перекус", 0.15f);

    val icon: ImageVector
        get() = when (this) {
            BREAKFAST -> Icons.Default.BakeryDining
            LUNCH -> Icons.Default.LunchDining
            DINNER -> Icons.Default.DinnerDining
            SNACK -> Icons.Default.Fastfood
        }
}
