package com.example.data.repository

import com.example.data.model.Equipment
import com.example.data.model.Exercise
import com.example.data.model.MuscleGroup
import com.example.data.model.WorkoutRoutine

object DefaultExerciseCatalog {

    val exercises: List<Exercise> = listOf(
        // Chest
        Exercise(
            id = "ex_bench_press",
            name = "Жим штанги лёжа",
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
            equipment = Equipment.BARBELL,
            difficulty = "Средний",
            description = "Лягте на горизонтальную скамью, сведите лопатки, опустите штангу к нижней части груди и мощно выжмите вверх до полного выпрямления рук."
        ),
        Exercise(
            id = "ex_incline_dumbbell_press",
            name = "Жим гантелей на наклонной скамье",
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS),
            equipment = Equipment.DUMBBELL,
            difficulty = "Средний",
            description = "Установите скамью под углом 30-45 градусов. Опускайте гантели по дуге до уровня груди и выжимайте вверх, сводя в верхней точке."
        ),
        Exercise(
            id = "ex_dips_chest",
            name = "Отжимания на брусьях",
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
            equipment = Equipment.BODYWEIGHT,
            difficulty = "Средний",
            description = "Наклоните корпус слегка вперёд, разведите локти в стороны и опускайтесь до угла 90 градусов в локтях."
        ),
        Exercise(
            id = "ex_cable_crossover",
            name = "Сведение рук в кроссовере",
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = listOf(MuscleGroup.SHOULDERS),
            equipment = Equipment.CABLE,
            difficulty = "Начальный",
            description = "Стоя между блоками, слегка согните руки в локтях и сводите рукояти перед собой с акцентом на пиковое сокращение грудных мышц."
        ),
        Exercise(
            id = "ex_pushups",
            name = "Отжимания от пола",
            primaryMuscle = MuscleGroup.CHEST,
            secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.CORE),
            equipment = Equipment.BODYWEIGHT,
            difficulty = "Начальный",
            description = "Держите тело прямой линией, напрягая мышцы кора. Опускайтесь грудью почти до пола и возвращайтесь в исходное положение."
        ),

        // Back
        Exercise(
            id = "ex_pullups",
            name = "Подтягивания широким хватом",
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = listOf(MuscleGroup.BICEPS),
            equipment = Equipment.BODYWEIGHT,
            difficulty = "Средний",
            description = "Возьмитесь за перекладину хватом шире плеч. Подтягивайтесь за счёт сведения лопаток и опускания локтей вниз, касаясь подбородком перекладины."
        ),
        Exercise(
            id = "ex_barbell_row",
            name = "Тяга штанги в наклоне",
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.BACK),
            equipment = Equipment.BARBELL,
            difficulty = "Средний",
            description = "Наклоните корпус под углом 45 градусов с прямой спиной. Тяните штангу к низу живота, прижимая локти к корпусу."
        ),
        Exercise(
            id = "ex_lat_pulldown",
            name = "Тяга верхнего блока к груди",
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = listOf(MuscleGroup.BICEPS),
            equipment = Equipment.CABLE,
            difficulty = "Начальный",
            description = "Сядьте в тренажёр, зафиксируйте бёдра валиками. Тяните рукоять к верхней части груди, сводя лопатки в нижней точке."
        ),
        Exercise(
            id = "ex_dumbbell_row",
            name = "Тяга гантели одной рукой",
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = listOf(MuscleGroup.BICEPS),
            equipment = Equipment.DUMBBELL,
            difficulty = "Начальный",
            description = "Обопритесь одним коленом и рукой о скамью. Тяните гантель к поясу, сохраняя горизонтальное положение корпуса."
        ),
        Exercise(
            id = "ex_deadlift",
            name = "Становая тяга",
            primaryMuscle = MuscleGroup.BACK,
            secondaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CORE),
            equipment = Equipment.BARBELL,
            difficulty = "Продвинутый",
            description = "Встаньте перед штангой, ноги на ширине плеч. Поднимайте вес за счёт разгибания бёдер и коленей, удерживая спину идеально прямой."
        ),

        // Shoulders
        Exercise(
            id = "ex_overhead_press",
            name = "Армейский жим стоя",
            primaryMuscle = MuscleGroup.SHOULDERS,
            secondaryMuscles = listOf(MuscleGroup.TRICEPS, MuscleGroup.CORE),
            equipment = Equipment.BARBELL,
            difficulty = "Средний",
            description = "Выжимайте штангу с груди над головой, удерживая пресс напряжённым и не прогибаясь в пояснице."
        ),
        Exercise(
            id = "ex_dumbbell_shoulder_press",
            name = "Жим гантелей сидя",
            primaryMuscle = MuscleGroup.SHOULDERS,
            secondaryMuscles = listOf(MuscleGroup.TRICEPS),
            equipment = Equipment.DUMBBELL,
            difficulty = "Средний",
            description = "Сидя на скамье с вертикальной спинкой, выжимайте гантели вверх по дуговой траектории."
        ),
        Exercise(
            id = "ex_lateral_raise",
            name = "Махи гантелями в стороны",
            primaryMuscle = MuscleGroup.SHOULDERS,
            secondaryMuscles = emptyList(),
            equipment = Equipment.DUMBBELL,
            difficulty = "Начальный",
            description = "Поднимайте гантели через стороны до уровня параллели с полом, слегка направляя мизинцы вверх."
        ),
        Exercise(
            id = "ex_rear_delt_fly",
            name = "Махи гантелями в наклоне (задняя дельта)",
            primaryMuscle = MuscleGroup.SHOULDERS,
            secondaryMuscles = listOf(MuscleGroup.BACK),
            equipment = Equipment.DUMBBELL,
            difficulty = "Начальный",
            description = "Наклонитесь вперед почти до горизонтали. Разводите руки в стороны, акцентируя внимание на задних пучках дельт."
        ),

        // Arms (Biceps & Triceps)
        Exercise(
            id = "ex_barbell_curl",
            name = "Подъем штанги на бицепс стоя",
            primaryMuscle = MuscleGroup.BICEPS,
            secondaryMuscles = emptyList(),
            equipment = Equipment.BARBELL,
            difficulty = "Начальный",
            description = "Держите локти неподвижно прижатыми по бокам корпуса. Сгибайте руки, поднимая штангу к плечам без раскачки."
        ),
        Exercise(
            id = "ex_dumbbell_hammer_curl",
            name = "Молотковые сгибания (Молотки)",
            primaryMuscle = MuscleGroup.BICEPS,
            secondaryMuscles = emptyList(),
            equipment = Equipment.DUMBBELL,
            difficulty = "Начальный",
            description = "Держите гантели нейтральным хватом (ладони смотрят друг на друга). Поочередно или синхронно сгибайте руки."
        ),
        Exercise(
            id = "ex_skull_crushers",
            name = "Французский жим лёжа",
            primaryMuscle = MuscleGroup.TRICEPS,
            secondaryMuscles = emptyList(),
            equipment = Equipment.BARBELL,
            difficulty = "Средний",
            description = "Лежа на скамье, опускайте гриф штанги ко лбу, удерживая локти неподвижными и направленными вверх."
        ),
        Exercise(
            id = "ex_cable_triceps_pushdown",
            name = "Разгибания на трицепс на блоке",
            primaryMuscle = MuscleGroup.TRICEPS,
            secondaryMuscles = emptyList(),
            equipment = Equipment.CABLE,
            difficulty = "Начальный",
            description = "Разгибайте руки вниз на блочном тренажере с канатной или прямой рукоятью, полностью фиксируя локти по бокам."
        ),

        // Legs (Quads, Hamstrings, Glutes, Calves)
        Exercise(
            id = "ex_squat",
            name = "Приседания со штангой",
            primaryMuscle = MuscleGroup.QUADS,
            secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.CORE),
            equipment = Equipment.BARBELL,
            difficulty = "Продвинутый",
            description = "Штанга на верхней части трапеций. Опускайтесь до параллели бёдер с полом, держа колени на одной линии со стопами."
        ),
        Exercise(
            id = "ex_leg_press",
            name = "Жим ногами в тренажере",
            primaryMuscle = MuscleGroup.QUADS,
            secondaryMuscles = listOf(MuscleGroup.GLUTES),
            equipment = Equipment.MACHINE,
            difficulty = "Начальный",
            description = "Плотно прижмите спину и поясницу к креслу тренажера. Выжимайте платформу, не разгибая колени до щелчка."
        ),
        Exercise(
            id = "ex_romanian_deadlift",
            name = "Румынская тяга со штангой",
            primaryMuscle = MuscleGroup.HAMSTRINGS,
            secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.BACK),
            equipment = Equipment.BARBELL,
            difficulty = "Средний",
            description = "Слегка согните колени и отводите таз назад, плавно опуская штангу вдоль голеней до ощущения растяжения задней поверхности бедра."
        ),
        Exercise(
            id = "ex_walking_lunges",
            name = "Выпады с гантелями в шаге",
            primaryMuscle = MuscleGroup.GLUTES,
            secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS),
            equipment = Equipment.DUMBBELL,
            difficulty = "Средний",
            description = "Делайте широкий шаг вперед, опуская заднее колено почти до касания пола под углом 90 градусов."
        ),
        Exercise(
            id = "ex_leg_extension",
            name = "Разгибания ног в тренажере",
            primaryMuscle = MuscleGroup.QUADS,
            secondaryMuscles = emptyList(),
            equipment = Equipment.MACHINE,
            difficulty = "Начальный",
            description = "Изолированное упражнение для квадрицепсов. Поднимайте валик до полного выпрямления ног с паузой в верхней точке."
        ),
        Exercise(
            id = "ex_leg_curl",
            name = "Сгибания ног лёжа в тренажере",
            primaryMuscle = MuscleGroup.HAMSTRINGS,
            secondaryMuscles = emptyList(),
            equipment = Equipment.MACHINE,
            difficulty = "Начальный",
            description = "Изолированное движение для бицепса бедра. Сгибайте ноги, стараясь коснуться валиком ягодиц."
        ),
        Exercise(
            id = "ex_calf_raise",
            name = "Подъемы на носки стоя",
            primaryMuscle = MuscleGroup.CALVES,
            secondaryMuscles = emptyList(),
            equipment = Equipment.MACHINE,
            difficulty = "Начальный",
            description = "Поднимайтесь на носки на максимальную высоту, фиксируйте пиковое сокращение и медленно растягивайте икры внизу."
        ),

        // Core / Abs
        Exercise(
            id = "ex_hanging_leg_raise",
            name = "Подъем ног в висе на турнике",
            primaryMuscle = MuscleGroup.CORE,
            secondaryMuscles = emptyList(),
            equipment = Equipment.BODYWEIGHT,
            difficulty = "Средний",
            description = "В висе на перекладине поднимайте прямые или согнутые ноги к груди за счет скручивания таза вверх."
        ),
        Exercise(
            id = "ex_plank",
            name = "Планка на локтях",
            primaryMuscle = MuscleGroup.CORE,
            secondaryMuscles = listOf(MuscleGroup.SHOULDERS),
            equipment = Equipment.BODYWEIGHT,
            difficulty = "Начальный",
            description = "Удерживайте тело на предплечьях и пальцах ног в идеально ровной линии, максимально напрягая пресс и ягодицы."
        ),

        // Obliques
        Exercise(
            id = "ex_russian_twist",
            name = "Русские скручивания",
            primaryMuscle = MuscleGroup.OBLIQUES,
            secondaryMuscles = listOf(MuscleGroup.CORE),
            equipment = Equipment.BODYWEIGHT,
            difficulty = "Начальный",
            description = "Сидя с приподнятыми ногами, поворачивайте корпус из стороны в сторону, активируя косые мышцы живота."
        ),
        Exercise(
            id = "ex_side_plank",
            name = "Боковая планка",
            primaryMuscle = MuscleGroup.OBLIQUES,
            secondaryMuscles = listOf(MuscleGroup.CORE),
            equipment = Equipment.BODYWEIGHT,
            difficulty = "Начальный",
            description = "Удерживайте тело на одном предплечье боком, сохраняя прямую линию от головы до стоп."
        ),

        // Trapezius
        Exercise(
            id = "ex_dumbbell_shrugs",
            name = "Шраги с гантелями",
            primaryMuscle = MuscleGroup.TRAPEZIUS,
            secondaryMuscles = emptyList(),
            equipment = Equipment.DUMBBELL,
            difficulty = "Начальный",
            description = "Стоя с гантелями по бокам, поднимайте плечи максимально вверх к ушам без сгибания рук в локтях."
        ),

        // Forearm
        Exercise(
            id = "ex_wrist_curls",
            name = "Сгибания запястий со штангой",
            primaryMuscle = MuscleGroup.FOREARM,
            secondaryMuscles = emptyList(),
            equipment = Equipment.BARBELL,
            difficulty = "Начальный",
            description = "Положите предплечья на скамью ладонями вверх и сгибайте запястья, поднимая гриф силой предплечий."
        ),
        Exercise(
            id = "ex_farmers_walk",
            name = "Прогулка фермера",
            primaryMuscle = MuscleGroup.FOREARM,
            secondaryMuscles = listOf(MuscleGroup.TRAPEZIUS, MuscleGroup.CORE),
            equipment = Equipment.DUMBBELL,
            difficulty = "Средний",
            description = "Ходьба с тяжелыми гантелями в руках с ровной осанкой для укрепления хвата и трапеций."
        ),

        // Lower Back
        Exercise(
            id = "ex_hyperextension",
            name = "Гиперэкстензия",
            primaryMuscle = MuscleGroup.LOWER_BACK,
            secondaryMuscles = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
            equipment = Equipment.BODYWEIGHT,
            difficulty = "Начальный",
            description = "Опускайте корпус вниз и плавно разгибайте спину в тренажере до прямой линии тела."
        ),

        // Cardio
        Exercise(
            id = "ex_running_treadmill",
            name = "Бег на дорожке",
            primaryMuscle = MuscleGroup.CARDIO,
            secondaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.CALVES),
            equipment = Equipment.MACHINE,
            difficulty = "Начальный",
            description = "Интервальный или равномерный кардио-бег для улучшения выносливости и сжигания калорий."
        )
    )

    fun getExerciseById(id: String): Exercise? {
        return exercises.find { it.id == id }
    }

    val defaultRoutines: List<WorkoutRoutine> = listOf(
        WorkoutRoutine(
            id = "routine_push",
            name = "Жим",
            targetMuscles = "Грудь • Плечи • Трицепс",
            defaultRestSeconds = 90,
            exercises = listOf(
                getExerciseById("ex_bench_press")!!,
                getExerciseById("ex_incline_dumbbell_press")!!,
                getExerciseById("ex_overhead_press")!!,
                getExerciseById("ex_lateral_raise")!!,
                getExerciseById("ex_cable_triceps_pushdown")!!
            )
        ),
        WorkoutRoutine(
            id = "routine_pull",
            name = "Тяга",
            targetMuscles = "Спина • Бицепс • Задняя дельта",
            defaultRestSeconds = 90,
            exercises = listOf(
                getExerciseById("ex_pullups")!!,
                getExerciseById("ex_barbell_row")!!,
                getExerciseById("ex_lat_pulldown")!!,
                getExerciseById("ex_rear_delt_fly")!!,
                getExerciseById("ex_barbell_curl")!!
            )
        ),
        WorkoutRoutine(
            id = "routine_legs",
            name = "Ноги",
            targetMuscles = "Квадрицепсы • Бицепс бедра • Пресс",
            defaultRestSeconds = 120,
            exercises = listOf(
                getExerciseById("ex_squat")!!,
                getExerciseById("ex_romanian_deadlift")!!,
                getExerciseById("ex_leg_press")!!,
                getExerciseById("ex_calf_raise")!!,
                getExerciseById("ex_hanging_leg_raise")!!
            )
        ),
        WorkoutRoutine(
            id = "routine_fullbody",
            name = "Всё тело",
            targetMuscles = "Грудь • Спина • Ноги • Плечи",
            defaultRestSeconds = 90,
            exercises = listOf(
                getExerciseById("ex_squat")!!,
                getExerciseById("ex_bench_press")!!,
                getExerciseById("ex_lat_pulldown")!!,
                getExerciseById("ex_overhead_press")!!,
                getExerciseById("ex_plank")!!
            )
        )
    )
}

