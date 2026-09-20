package com.tether.data.repository

import com.tether.data.gpx.GpxManager
import com.tether.data.model.ActivityLevel
import com.tether.data.model.Exercise
import com.tether.data.model.FoodItem
import com.tether.data.model.Gender
import com.tether.data.model.GoalType
import com.tether.data.model.GpxRoute
import com.tether.data.model.MuscleGroup
import com.tether.data.model.TrackPoint
import com.tether.data.model.UserProfile
import com.tether.data.model.WaterEntry
import com.tether.data.model.Workout
import com.tether.data.model.WorkoutExercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object UserRepository {
    private val _userProfile = MutableStateFlow(
        UserProfile(
            name = "Александр Смирнов",
            email = "alex@tether.fitness",
            heightCm = 180f,
            weightKg = 78f,
            age = 27,
            gender = Gender.MALE,
            activityLevel = ActivityLevel.MODERATE,
            goalType = GoalType.MAINTAIN
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun login(email: String, name: String = "Александр") {
        _userProfile.value = _userProfile.value.copy(
            email = email,
            name = name.ifBlank { "Пользователь" }
        )
        _isLoggedIn.value = true
    }

    fun register(
        name: String,
        email: String,
        heightCm: Float,
        weightKg: Float,
        age: Int,
        gender: Gender,
        activityLevel: ActivityLevel,
        goalType: GoalType
    ) {
        _userProfile.value = UserProfile(
            name = name,
            email = email,
            heightCm = heightCm,
            weightKg = weightKg,
            age = age,
            gender = gender,
            activityLevel = activityLevel,
            goalType = goalType
        )
        _isLoggedIn.value = true
    }

    fun updateMetrics(
        heightCm: Float,
        weightKg: Float,
        age: Int,
        gender: Gender,
        activityLevel: ActivityLevel,
        goalType: GoalType
    ) {
        _userProfile.value = _userProfile.value.copy(
            heightCm = heightCm,
            weightKg = weightKg,
            age = age,
            gender = gender,
            activityLevel = activityLevel,
            goalType = goalType
        )
    }

    fun logout() {
        _isLoggedIn.value = false
    }
}

object DiaryRepository {
    // Initial sample products for the day
    private val _foods = MutableStateFlow<List<FoodItem>>(
        listOf(
            FoodItem(
                name = "Овсяная каша на молоке с ягодами",
                description = "Овсянка 60г, молоко 150мл, горсть черники",
                caloriesPer100g = 120f,
                proteinPer100g = 4.2f,
                fatPer100g = 3.1f,
                carbsPer100g = 19.5f,
                grams = 280f
            ),
            FoodItem(
                name = "Куриная грудка гриль",
                description = "Филе куриное запеченное с розмарином и паприкой",
                caloriesPer100g = 165f,
                proteinPer100g = 31f,
                fatPer100g = 3.6f,
                carbsPer100g = 0f,
                grams = 180f
            ),
            FoodItem(
                name = "Киноа с овощами",
                description = "Киноа отварное, брокколи, болгарский перец, оливковое масло",
                caloriesPer100g = 145f,
                proteinPer100g = 4.5f,
                fatPer100g = 4.2f,
                carbsPer100g = 22.0f,
                grams = 150f
            ),
            FoodItem(
                name = "Греческий йогурт и миндаль",
                description = "Натуральный йогурт 2% и 15г миндаля",
                caloriesPer100g = 115f,
                proteinPer100g = 9.0f,
                fatPer100g = 6.0f,
                carbsPer100g = 5.5f,
                grams = 160f
            )
        )
    )
    val foods: StateFlow<List<FoodItem>> = _foods.asStateFlow()

    // Water entries
    private val _waterEntries = MutableStateFlow<List<WaterEntry>>(
        listOf(
            WaterEntry(amountMl = 350),
            WaterEntry(amountMl = 500),
            WaterEntry(amountMl = 250),
            WaterEntry(amountMl = 400)
        )
    )
    val waterEntries: StateFlow<List<WaterEntry>> = _waterEntries.asStateFlow()

    fun addFood(food: FoodItem) {
        _foods.value = listOf(food) + _foods.value
    }

    fun removeFood(id: String) {
        _foods.value = _foods.value.filter { it.id != id }
    }

    fun addWater(amountMl: Int) {
        _waterEntries.value = _waterEntries.value + WaterEntry(amountMl = amountMl)
    }

    fun resetWater() {
        _waterEntries.value = emptyList()
    }

    fun undoLastWater() {
        if (_waterEntries.value.isNotEmpty()) {
            _waterEntries.value = _waterEntries.value.dropLast(1)
        }
    }
}

object WorkoutRepository {
    val defaultExercises: List<Exercise> = listOf(
        // Грудь
        Exercise(
            name = "Жим штанги лежа",
            description = "Базовое упражнение со свободным весом для развития грудных мышц, передней дельты и трицепса.",
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
            equipment = "Штанга, скамья",
            defaultSets = 4,
            defaultReps = 8
        ),
        Exercise(
            name = "Отжимания от пола",
            description = "Классические отжимания в ровной планке для груди и кора.",
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.ABS),
            equipment = "Собственный вес",
            defaultSets = 3,
            defaultReps = 15
        ),
        Exercise(
            name = "Разведение гантелей лежа",
            description = "Изолирующее упражнение для растяжения и прокачки грудных волокон.",
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = listOf(MuscleGroup.SHOULDERS),
            equipment = "Гантели, скамья",
            defaultSets = 3,
            defaultReps = 12
        ),

        // Спина
        Exercise(
            name = "Подтягивания широким хватом",
            description = "Лучшее базовое упражнение для широчайших мышц спины и ромбовидных.",
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = listOf(MuscleGroup.BICEPS),
            equipment = "Турник",
            defaultSets = 4,
            defaultReps = 8
        ),
        Exercise(
            name = "Тяга штанги в наклоне",
            description = "Мощное движение для толщины спины и трапеций.",
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.HAMSTRINGS),
            equipment = "Штанга",
            defaultSets = 4,
            defaultReps = 10
        ),

        // Ноги
        Exercise(
            name = "Классические приседания",
            description = "Фундаментальное упражнение для квадрицепсов, ягодиц и общей силы тела.",
            primaryMuscle = MuscleGroup.QUADS,
            secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
            equipment = "Штанга или собственный вес",
            defaultSets = 4,
            defaultReps = 10
        ),
        Exercise(
            name = "Румынская тяга",
            description = "Фокусированное движение на заднюю поверхность бедра и ягодицы с ровной спиной.",
            primaryMuscle = MuscleGroup.HAMSTRINGS,
            secondaryMuscles = listOf(MuscleGroup.BACK),
            equipment = "Штанга / Гантели",
            defaultSets = 3,
            defaultReps = 10
        ),
        Exercise(
            name = "Подъемы на носки",
            description = "Упражнение для развития икроножных и камбаловидных мышц голени.",
            primaryMuscle = MuscleGroup.CALVES,
            secondaryMuscles = emptyList(),
            equipment = "Ступенька / Гантель",
            defaultSets = 4,
            defaultReps = 15
        ),

        // Плечи
        Exercise(
            name = "Армейский жим стоя",
            description = "Базовый жим штанги над головой для передней и средней дельты.",
            primaryMuscle = MuscleGroup.SHOULDERS,
            secondaryMuscles = listOf(MuscleGroup.TRICEPS),
            equipment = "Штанга",
            defaultSets = 4,
            defaultReps = 8
        ),
        Exercise(
            name = "Махи гантелями через стороны",
            description = "Изолирующая проработка средней дельты для создания округлости плеч.",
            primaryMuscle = MuscleGroup.SHOULDERS,
            secondaryMuscles = emptyList(),
            equipment = "Гантели",
            defaultSets = 3,
            defaultReps = 12
        ),

        // Руки
        Exercise(
            name = "Подъем штанги на бицепс",
            description = "Классическое сгибание рук стоя для пика двуглавой мышцы плеча.",
            primaryMuscle = MuscleGroup.BICEPS,
            secondaryMuscles = emptyList(),
            equipment = "Штанга EZ",
            defaultSets = 3,
            defaultReps = 10
        ),
        Exercise(
            name = "Отжимания на брусьях",
            description = "Отличное компаундное упражнение для трехглавой мышцы плеча (трицепса) и низа груди.",
            primaryMuscle = MuscleGroup.TRICEPS,
            secondaryMuscles = listOf(MuscleGroup.CHEST),
            equipment = "Брусья",
            defaultSets = 3,
            defaultReps = 10
        ),

        // Пресс
        Exercise(
            name = "Планка в упоре на предплечьях",
            description = "Статическое удержание для укрепления поперечной мышцы живота и глубоких стабилизаторов.",
            primaryMuscle = MuscleGroup.ABS,
            secondaryMuscles = listOf(MuscleGroup.SHOULDERS),
            equipment = "Коврик",
            defaultSets = 3,
            defaultReps = 60
        ),
        Exercise(
            name = "Подъем ног в висе",
            description = "Эффективная проработка нижнего отдела прямой мышцы живота.",
            primaryMuscle = MuscleGroup.ABS,
            secondaryMuscles = emptyList(),
            equipment = "Турник",
            defaultSets = 3,
            defaultReps = 12
        )
    )

    private val _workouts = MutableStateFlow<List<Workout>>(
        listOf(
            Workout(
                title = "Верх тела & Сила (Upper Body)",
                description = "Грудь, широчайшие мышцы спины, плечи и трицепс в динамическом темпе.",
                durationMinutes = 55,
                isCustom = false,
                exercises = listOf(
                    WorkoutExercise(
                        exerciseId = defaultExercises[0].id,
                        name = "Жим штанги лежа",
                        muscleGroup = MuscleGroup.CHEST,
                        sets = 4,
                        reps = 8,
                        weightKg = 70f
                    ),
                    WorkoutExercise(
                        exerciseId = defaultExercises[3].id,
                        name = "Подтягивания широким хватом",
                        muscleGroup = MuscleGroup.BACK,
                        sets = 4,
                        reps = 8,
                        weightKg = 0f
                    ),
                    WorkoutExercise(
                        exerciseId = defaultExercises[8].id,
                        name = "Армейский жим стоя",
                        muscleGroup = MuscleGroup.SHOULDERS,
                        sets = 3,
                        reps = 10,
                        weightKg = 40f
                    ),
                    WorkoutExercise(
                        exerciseId = defaultExercises[11].id,
                        name = "Отжимания на брусьях",
                        muscleGroup = MuscleGroup.TRICEPS,
                        sets = 3,
                        reps = 10,
                        weightKg = 0f
                    )
                )
            ),
            Workout(
                title = "День ног & Пресс (Legs & Core)",
                description = "Интенсивная тренировка квадрицепсов, бицепса бедра и мышечного корсета.",
                durationMinutes = 50,
                isCustom = false,
                exercises = listOf(
                    WorkoutExercise(
                        exerciseId = defaultExercises[5].id,
                        name = "Классические приседания",
                        muscleGroup = MuscleGroup.QUADS,
                        sets = 4,
                        reps = 10,
                        weightKg = 80f
                    ),
                    WorkoutExercise(
                        exerciseId = defaultExercises[6].id,
                        name = "Румынская тяга",
                        muscleGroup = MuscleGroup.HAMSTRINGS,
                        sets = 4,
                        reps = 10,
                        weightKg = 70f
                    ),
                    WorkoutExercise(
                        exerciseId = defaultExercises[7].id,
                        name = "Подъемы на носки",
                        muscleGroup = MuscleGroup.CALVES,
                        sets = 4,
                        reps = 15,
                        weightKg = 20f
                    ),
                    WorkoutExercise(
                        exerciseId = defaultExercises[13].id,
                        name = "Подъем ног в висе",
                        muscleGroup = MuscleGroup.ABS,
                        sets = 3,
                        reps = 12,
                        weightKg = 0f
                    )
                )
            )
        )
    )
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()

    fun addWorkout(workout: Workout) {
        _workouts.value = listOf(workout) + _workouts.value
    }

    fun removeWorkout(id: String) {
        _workouts.value = _workouts.value.filter { it.id != id }
    }
}

object RouteRepository {
    private val _communityRoutes = MutableStateFlow<List<GpxRoute>>(GpxManager.getCommunityRoutes())
    val communityRoutes: StateFlow<List<GpxRoute>> = _communityRoutes.asStateFlow()

    private val _myRoutes = MutableStateFlow<List<GpxRoute>>(
        listOf(
            GpxRoute(
                name = "Утренняя пробежка в парке",
                description = "Мой персональный записанный маршрут вокруг местного пруда.",
                distanceKm = 5.3f,
                elevationGainMeters = 35,
                durationMinutes = 32,
                isCommunity = false,
                isUserRecorded = true,
                points = GpxManager.getCommunityRoutes()[0].points.take(20)
            )
        )
    )
    val myRoutes: StateFlow<List<GpxRoute>> = _myRoutes.asStateFlow()

    // Active live GPX recording session
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordedPoints = MutableStateFlow<List<TrackPoint>>(emptyList())
    val recordedPoints: StateFlow<List<TrackPoint>> = _recordedPoints.asStateFlow()

    private val _recordingStartTime = MutableStateFlow(0L)
    val recordingStartTime: StateFlow<Long> = _recordingStartTime.asStateFlow()

    fun addCustomRoute(route: GpxRoute) {
        _myRoutes.value = listOf(route) + _myRoutes.value
    }

    fun startRecording() {
        _isRecording.value = true
        _recordedPoints.value = emptyList()
        _recordingStartTime.value = System.currentTimeMillis()
    }

    fun addTrackPoint(point: TrackPoint) {
        if (_isRecording.value) {
            _recordedPoints.value = _recordedPoints.value + point
        }
    }

    fun finishRecording(name: String, description: String): GpxRoute {
        val pts = _recordedPoints.value
        val dist = GpxManager.calculateDistanceKm(pts)
        val ele = GpxManager.calculateElevationGain(pts)
        val elapsedMins = ((System.currentTimeMillis() - _recordingStartTime.value) / 60000L).toInt().coerceAtLeast(1)

        val newRoute = GpxRoute(
            name = name.ifBlank { "Моя GPS тренировка" },
            description = description.ifBlank { "Записанный маршрут через GPS" },
            distanceKm = (Math.round(dist * 10) / 10f),
            elevationGainMeters = ele,
            durationMinutes = elapsedMins,
            points = pts,
            isCommunity = false,
            isUserRecorded = true
        )

        _myRoutes.value = listOf(newRoute) + _myRoutes.value
        _isRecording.value = false
        _recordedPoints.value = emptyList()
        return newRoute
    }

    fun cancelRecording() {
        _isRecording.value = false
        _recordedPoints.value = emptyList()
    }
}
