package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.db.CustomExerciseEntity
import com.example.data.db.CustomRoutineEntity
import com.example.data.db.WorkoutEntity
import com.example.data.db.WorkoutSessionEntity
import com.example.data.model.Equipment
import com.example.data.model.Exercise
import com.example.data.model.LoggedExercise
import com.example.data.model.LoggedSet
import com.example.data.model.MuscleGroup
import com.example.data.model.PersonalRecord
import com.example.data.model.SetKind
import com.example.data.model.WorkoutCategory
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSession
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class WorkoutRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val foodDao = db.foodDao()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val exercisesListType = Types.newParameterizedType(List::class.java, LoggedExerciseDto::class.java)
    private val exercisesAdapter = moshi.adapter<List<LoggedExerciseDto>>(exercisesListType)

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    val defaultCatalogExercises: List<Exercise> = DefaultExerciseCatalog.exercises

    fun getAllExercises(): Flow<List<Exercise>> {
        return foodDao.getAllCustomExercises().map { customList ->
            val custom = customList.map { it.toDomain() }
            DefaultExerciseCatalog.exercises + custom
        }
    }

    suspend fun addCustomExercise(
        name: String,
        muscle: MuscleGroup,
        equipment: Equipment,
        description: String = ""
    ) = withContext(Dispatchers.IO) {
        val id = "custom_ex_${System.currentTimeMillis()}"
        val ex = Exercise(
            id = id,
            name = name.trim(),
            primaryMuscle = muscle,
            secondaryMuscles = emptyList(),
            equipment = equipment,
            difficulty = "Средний",
            description = description.trim()
        )
        foodDao.insertCustomExercise(CustomExerciseEntity.fromDomain(ex))
    }

    suspend fun deleteCustomExercise(id: String) = withContext(Dispatchers.IO) {
        foodDao.deleteCustomExercise(id)
    }

    fun getAllRoutines(): Flow<List<WorkoutRoutine>> {
        return foodDao.getAllCustomRoutines().map { customEntities ->
            val customExercises = foodDao.getAllCustomExercises() // fallback check
            val customRoutines = customEntities.map { entity ->
                val exIds = try {
                    stringListAdapter.fromJson(entity.exerciseIdsJson) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
                val matched = exIds.mapNotNull { id ->
                    DefaultExerciseCatalog.getExerciseById(id)
                }
                WorkoutRoutine(
                    id = entity.id,
                    name = entity.name,
                    targetMuscles = entity.targetMuscles,
                    defaultRestSeconds = entity.defaultRestSeconds,
                    exercises = matched
                )
            }
            DefaultExerciseCatalog.defaultRoutines + customRoutines
        }
    }

    suspend fun saveCustomRoutine(routine: WorkoutRoutine) = withContext(Dispatchers.IO) {
        val jsonIds = stringListAdapter.toJson(routine.exercises.map { it.id })
        foodDao.insertCustomRoutine(
            CustomRoutineEntity(
                id = routine.id,
                name = routine.name,
                targetMuscles = routine.targetMuscles,
                defaultRestSeconds = routine.defaultRestSeconds,
                exerciseIdsJson = jsonIds
            )
        )
    }

    suspend fun deleteCustomRoutine(id: String) = withContext(Dispatchers.IO) {
        foodDao.deleteCustomRoutine(id)
    }

    fun getAllSessions(): Flow<List<WorkoutSession>> {
        return foodDao.getAllWorkoutSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getSessionsForDate(date: String): Flow<List<WorkoutSession>> {
        return foodDao.getWorkoutSessionsForDate(date).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveWorkoutSession(session: WorkoutSession): Long = withContext(Dispatchers.IO) {
        val dtoList = session.exercises.map { LoggedExerciseDto.fromDomain(it) }
        val json = exercisesAdapter.toJson(dtoList)

        val entity = WorkoutSessionEntity(
            id = session.id,
            date = session.date,
            title = session.title,
            routineId = session.routineId,
            durationSeconds = session.durationSeconds,
            caloriesBurned = session.caloriesBurned,
            volumeKg = session.totalVolume,
            notes = session.notes,
            exercisesJson = json,
            timestamp = session.timestamp
        )

        val insertedId = foodDao.insertWorkoutSession(entity)

        // Also record in legacy workouts table so daily calorie calculation immediately reflects it!
        val legacy = WorkoutEntity(
            id = 0,
            date = session.date,
            title = session.title,
            category = WorkoutCategory.GYM.name,
            durationMinutes = (session.durationSeconds / 60).coerceAtLeast(1),
            caloriesBurned = session.caloriesBurned,
            notes = "Тоннаж: %,d кг • Подходов: %d".format(session.totalVolume.toInt(), session.totalCompletedSets),
            timestamp = session.timestamp
        )
        foodDao.insertWorkout(legacy)

        insertedId
    }

    suspend fun deleteWorkoutSession(id: Long) = withContext(Dispatchers.IO) {
        foodDao.deleteWorkoutSession(id)
    }

    fun getPersonalRecords(): Flow<List<PersonalRecord>> {
        return getAllSessions().map { sessions ->
            val recordMap = mutableMapOf<String, PersonalRecord>()
            sessions.forEach { session ->
                session.exercises.forEach { ex ->
                    val completed = ex.sets.filter { it.isCompleted && it.weightKg > 0f }
                    if (completed.isNotEmpty()) {
                        val sessionMaxW = completed.maxOf { it.weightKg }
                        val sessionMax1rm = completed.maxOf { it.oneRm }
                        val existing = recordMap[ex.exerciseId]
                        if (existing == null || sessionMaxW > existing.maxWeight) {
                            recordMap[ex.exerciseId] = PersonalRecord(
                                exerciseId = ex.exerciseId,
                                exerciseName = ex.exerciseName,
                                maxWeight = maxOf(sessionMaxW, existing?.maxWeight ?: 0f),
                                maxOneRm = maxOf(sessionMax1rm, existing?.maxOneRm ?: 0f),
                                date = session.date
                            )
                        }
                    }
                }
            }
            recordMap.values.sortedByDescending { it.maxWeight }
        }
    }

    private fun WorkoutSessionEntity.toDomain(): WorkoutSession {
        val dtos = try {
            exercisesAdapter.fromJson(exercisesJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        val exercisesDomain = dtos.map { it.toDomain() }
        return WorkoutSession(
            id = id,
            date = date,
            title = title,
            routineId = routineId,
            durationSeconds = durationSeconds,
            exercises = exercisesDomain,
            caloriesBurned = caloriesBurned,
            notes = notes,
            timestamp = timestamp
        )
    }

    // JSON DTOs for storing in Room
    data class LoggedExerciseDto(
        val exerciseId: String,
        val exerciseName: String,
        val muscleGroup: String,
        val sets: List<LoggedSetDto>,
        val notes: String = ""
    ) {
        fun toDomain(): LoggedExercise {
            val mg = try { MuscleGroup.valueOf(muscleGroup) } catch (e: Exception) { MuscleGroup.CHEST }
            return LoggedExercise(
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                muscleGroup = mg,
                sets = sets.map { it.toDomain() },
                notes = notes
            )
        }

        companion object {
            fun fromDomain(d: LoggedExercise): LoggedExerciseDto {
                return LoggedExerciseDto(
                    exerciseId = d.exerciseId,
                    exerciseName = d.exerciseName,
                    muscleGroup = d.muscleGroup.name,
                    sets = d.sets.map { LoggedSetDto.fromDomain(it) },
                    notes = d.notes
                )
            }
        }
    }

    data class LoggedSetDto(
        val setNumber: Int,
        val kind: String,
        val weightKg: Float,
        val reps: Int,
        val isCompleted: Boolean
    ) {
        fun toDomain(): LoggedSet {
            val k = try { SetKind.valueOf(kind) } catch (e: Exception) { SetKind.NORMAL }
            return LoggedSet(
                setNumber = setNumber,
                kind = k,
                weightKg = weightKg,
                reps = reps,
                isCompleted = isCompleted
            )
        }

        companion object {
            fun fromDomain(d: LoggedSet): LoggedSetDto {
                return LoggedSetDto(
                    setNumber = d.setNumber,
                    kind = d.kind.name,
                    weightKg = d.weightKg,
                    reps = d.reps,
                    isCompleted = d.isCompleted
                )
            }
        }
    }
}

