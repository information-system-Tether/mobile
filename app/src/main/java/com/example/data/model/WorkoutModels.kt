package com.example.data.model

enum class MuscleGroup(val displayNameRu: String, val gymManeId: String) {
    CHEST("Грудь", "chest"),
    BACK("Спина", "back"),
    SHOULDERS("Плечи", "shoulders"),
    BICEPS("Бицепс", "biceps"),
    TRICEPS("Трицепс", "triceps"),
    FOREARM("Предплечья", "forearm"),
    TRAPEZIUS("Трапеции", "trapezius"),
    LOWER_BACK("Поясница", "lowerback"),
    CORE("Пресс", "abdomen"),
    OBLIQUES("Косые мышцы", "obliques"),
    QUADS("Квадрицепсы", "quads"),
    HAMSTRINGS("Бицепс бедра", "hamstrings"),
    GLUTES("Ягодицы", "glutes"),
    CALVES("Икры", "calves"),
    CARDIO("Кардио", "cardio");

    companion object {
        fun fromGymManeId(id: String): MuscleGroup? = when (id) {
            "chest" -> CHEST
            "back" -> BACK
            "shoulders" -> SHOULDERS
            "biceps" -> BICEPS
            "triceps" -> TRICEPS
            "forearm" -> FOREARM
            "trapezius" -> TRAPEZIUS
            "lowerback" -> LOWER_BACK
            "abdomen" -> CORE
            "obliques" -> OBLIQUES
            "quads" -> QUADS
            "hamstrings" -> HAMSTRINGS
            "glutes" -> GLUTES
            "calves" -> CALVES
            else -> null
        }
    }
}

enum class Equipment(val displayNameRu: String) {
    BARBELL("Штанга"),
    DUMBBELL("Гантели"),
    MACHINE("Тренажер"),
    CABLE("Блочный тренажер"),
    BODYWEIGHT("Свой вес"),
    OTHER("Другое")
}

data class Exercise(
    val id: String,
    val name: String,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val equipment: Equipment = Equipment.BARBELL,
    val difficulty: String = "Средний",
    val description: String = ""
)

enum class SetKind(val displayNameRu: String, val shortName: String) {
    NORMAL("Обычный", "О"),
    WARMUP("Разминка", "Р"),
    DROP_SET("Дроп-сет", "Д"),
    FAILURE("До отказа", "ОТ")
}

data class LoggedSet(
    val setNumber: Int = 1,
    val kind: SetKind = SetKind.NORMAL,
    val weightKg: Float = 0f,
    val reps: Int = 0,
    val isCompleted: Boolean = false
) {
    val volume: Float
        get() = if (kind != SetKind.WARMUP && isCompleted) weightKg * reps else 0f

    // Epley formula from GymMane
    val oneRm: Float
        get() = if (reps > 0 && weightKg > 0f) weightKg * (1f + reps / 30f) else 0f
}

data class LoggedExercise(
    val exerciseId: String,
    val exerciseName: String,
    val muscleGroup: MuscleGroup,
    val sets: List<LoggedSet> = emptyList(),
    val notes: String = ""
) {
    val totalVolume: Float
        get() = sets.sumOf { it.volume.toDouble() }.toFloat()

    val completedSetsCount: Int
        get() = sets.count { it.isCompleted }

    val maxWeight: Float
        get() = sets.filter { it.isCompleted }.maxOfOrNull { it.weightKg } ?: 0f

    val bestOneRm: Float
        get() = sets.filter { it.isCompleted }.maxOfOrNull { it.oneRm } ?: 0f
}

data class WorkoutRoutine(
    val id: String,
    val name: String,
    val targetMuscles: String,
    val defaultRestSeconds: Int = 90,
    val exercises: List<Exercise> = emptyList()
)

data class WorkoutSession(
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val title: String,
    val routineId: String? = null,
    val durationSeconds: Int = 0,
    val exercises: List<LoggedExercise> = emptyList(),
    val caloriesBurned: Float = 0f,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalVolume: Float
        get() = exercises.sumOf { it.totalVolume.toDouble() }.toFloat()

    val totalCompletedSets: Int
        get() = exercises.sumOf { it.completedSetsCount }

    fun toLegacyWorkout(): Workout {
        return Workout(
            id = id,
            date = date,
            title = title,
            category = WorkoutCategory.GYM,
            durationMinutes = (durationSeconds / 60).coerceAtLeast(1),
            caloriesBurned = caloriesBurned,
            notes = notes,
            timestamp = timestamp
        )
    }
}

data class PersonalRecord(
    val exerciseId: String,
    val exerciseName: String,
    val maxWeight: Float,
    val maxOneRm: Float,
    val date: String
)

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
    YOGA("Йога", 3.5f),
    HIIT("Круговая тренировка", 12.0f),
    OTHER("Другое", 6.0f);

    fun estimateBurned(minutes: Int): Float = approxKcalPerMin * minutes
}

data class StepActivityStats(
    val date: String,
    val steps: Int = 0,
    val distanceMeters: Float = 0f,
    val activeMinutes: Int = 0,
    val caloriesBurned: Float = 0f,
    val isSensorAvailable: Boolean = true,
    val isConnected: Boolean = true,
    val isHealthConnectAvailable: Boolean = false
) {
    companion object {
        fun calculateCaloriesFromSteps(steps: Int, weightKg: Float = 70f): Float {
            val factor = (weightKg / 70f) * 0.042f
            return steps * factor
        }
    }
}
