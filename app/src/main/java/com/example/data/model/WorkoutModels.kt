package com.example.data.model

data class Workout(
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val title: String,
    val category: WorkoutCategory = WorkoutCategory.OTHER,
    val durationMinutes: Int = 30,
    val caloriesBurned: Float = 0f,
    val distanceKm: Float = 0f,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class WorkoutCategory(val displayNameRu: String, val approxKcalPerMin: Float) {
    RUNNING("Бег", 11.5f),
    WALKING("Ходьба", 4.5f),
    GYM("Силовая тренировка", 6.8f),
    CYCLING("Велосипед", 8.5f),
    SWIMMING("Плавание", 9.0f),
    YOGA("Йога / Растяжка", 3.5f),
    HIIT("Круговая / HIIT", 12.0f),
    OTHER("Другое", 6.0f);

    fun estimateBurned(minutes: Int): Float = approxKcalPerMin * minutes
}

data class StepActivityStats(
    val date: String,
    val steps: Int = 0,
    val distanceMeters: Float = 0f,
    val activeMinutes: Int = 0,
    val caloriesBurned: Float = 0f,
    val isHealthConnectAvailable: Boolean = false,
    val isConnected: Boolean = false
) {
    companion object {
        fun calculateCaloriesFromSteps(steps: Int, weightKg: Float = 70f): Float {
            // ~0.04 - 0.045 kcal per step on average for 70kg person
            val factor = (weightKg / 70f) * 0.042f
            return steps * factor
        }
    }
}
