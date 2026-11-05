package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Equipment
import com.example.data.model.Exercise
import com.example.data.model.LoggedExercise
import com.example.data.model.LoggedSet
import com.example.data.model.MuscleGroup
import com.example.data.model.SetKind
import com.example.data.model.WorkoutSession
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutSessionDialog(
    initialSession: WorkoutSession,
    catalogExercises: List<Exercise>,
    onFinishSession: (WorkoutSession) -> Unit,
    onCancelSession: () -> Unit,
    onCreateExercise: (String, MuscleGroup, Equipment, String) -> Unit = { _, _, _, _ -> }
) {
    var sessionTitle by remember { mutableStateOf(initialSession.title) }
    var elapsedSeconds by remember { mutableIntStateOf(initialSession.durationSeconds) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var exercises by remember { mutableStateOf(initialSession.exercises) }

    // Rest timer state
    var restSecondsLeft by remember { mutableIntStateOf(0) }
    var totalRestDuration by remember { mutableIntStateOf(90) }
    var showAddExerciseSheet by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }

    // Stopwatch elapsed ticker
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            elapsedSeconds++
        }
    }

    // Rest countdown ticker
    LaunchedEffect(restSecondsLeft) {
        if (restSecondsLeft > 0) {
            delay(1000)
            restSecondsLeft--
        }
    }

    fun startRest(duration: Int) {
        totalRestDuration = duration
        restSecondsLeft = duration
    }

    val totalVolume = exercises.sumOf { it.totalVolume.toDouble() }.toFloat()
    val totalCompletedSets = exercises.sumOf { it.completedSetsCount }

    Dialog(
        onDismissRequest = { /* Prevent accidental dismissal, require explicit finish/cancel */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = sessionTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            val minutes = elapsedSeconds / 60
                            val seconds = elapsedSeconds % 60
                            Text(
                                text = "%02d:%02d • Тоннаж: %,d кг • Подходов: %d".format(
                                    minutes,
                                    seconds,
                                    totalVolume.toInt(),
                                    totalCompletedSets
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showCancelConfirm = true }) {
                            Icon(Icons.Default.Close, contentDescription = "Отмена")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                Surface(
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddExerciseSheet = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Упражнение")
                        }

                        Button(
                            onClick = {
                                isTimerRunning = false
                                // Estimate calories burned: approx ~7 kcal/min + volume bonus
                                val minutes = (elapsedSeconds / 60).coerceAtLeast(1)
                                val burned = (minutes * 6.5f) + (totalVolume * 0.02f)
                                val finished = initialSession.copy(
                                    title = sessionTitle,
                                    durationSeconds = elapsedSeconds,
                                    exercises = exercises,
                                    caloriesBurned = burned
                                )
                                onFinishSession(finished)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Завершить")
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Rest timer banner
                if (restSecondsLeft > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Отдых: %02d:%02d".format(restSecondsLeft / 60, restSecondsLeft % 60),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { restSecondsLeft += 30 }) {
                                        Text("+30с", fontWeight = FontWeight.Bold)
                                    }
                                    TextButton(onClick = { restSecondsLeft = 0 }) {
                                        Text("Пропустить")
                                    }
                                }
                            }

                            val progress = if (totalRestDuration > 0) {
                                (totalRestDuration - restSecondsLeft).toFloat() / totalRestDuration
                            } else 0f

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                if (exercises.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Тренировка пуста",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Добавьте упражнения, чтобы начать запись подходов",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showAddExerciseSheet = true },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Добавить упражнение")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(exercises) { exIndex, exercise ->
                            ExerciseSessionCard(
                                exercise = exercise,
                                onAddSet = {
                                    val lastSet = exercise.sets.lastOrNull()
                                    val nextNumber = exercise.sets.size + 1
                                    val newSet = LoggedSet(
                                        setNumber = nextNumber,
                                        kind = lastSet?.kind ?: SetKind.NORMAL,
                                        weightKg = lastSet?.weightKg ?: 20f,
                                        reps = lastSet?.reps ?: 10,
                                        isCompleted = false
                                    )
                                    exercises = exercises.toMutableList().also {
                                        it[exIndex] = exercise.copy(sets = exercise.sets + newSet)
                                    }
                                },
                                onUpdateSet = { setIndex, updatedSet ->
                                    val oldSet = exercise.sets[setIndex]
                                    val setList = exercise.sets.toMutableList().also {
                                        it[setIndex] = updatedSet
                                    }
                                    exercises = exercises.toMutableList().also {
                                        it[exIndex] = exercise.copy(sets = setList)
                                    }

                                    // When user checks set as done, start rest timer
                                    if (!oldSet.isCompleted && updatedSet.isCompleted) {
                                        startRest(90)
                                    }
                                },
                                onDeleteSet = { setIndex ->
                                    val setList = exercise.sets.toMutableList().also {
                                        it.removeAt(setIndex)
                                    }.mapIndexed { idx, s -> s.copy(setNumber = idx + 1) }

                                    exercises = exercises.toMutableList().also {
                                        it[exIndex] = exercise.copy(sets = setList)
                                    }
                                },
                                onDeleteExercise = {
                                    exercises = exercises.toMutableList().also {
                                        it.removeAt(exIndex)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text("Отменить тренировку?") },
            text = { Text("Все внесённые подходы за текущую сессию будут удалены.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelConfirm = false
                        onCancelSession()
                    }
                ) {
                    Text("Да, отменить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) {
                    Text("Продолжить")
                }
            }
        )
    }

    if (showAddExerciseSheet) {
        ExercisePickerModal(
            allExercises = catalogExercises,
            onCreateExercise = onCreateExercise,
            onSelectExercise = { ex ->
                val newLogged = LoggedExercise(
                    exerciseId = ex.id,
                    exerciseName = ex.name,
                    muscleGroup = ex.primaryMuscle,
                    sets = listOf(
                        LoggedSet(setNumber = 1, kind = SetKind.NORMAL, weightKg = 20f, reps = 10, isCompleted = false)
                    )
                )
                exercises = exercises + newLogged
                showAddExerciseSheet = false
            },
            onDismiss = { showAddExerciseSheet = false }
        )
    }
}

@Composable
private fun ExerciseSessionCard(
    exercise: LoggedExercise,
    onAddSet: () -> Unit,
    onUpdateSet: (Int, LoggedSet) -> Unit,
    onDeleteSet: (Int) -> Unit,
    onDeleteExercise: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Exercise Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.exerciseName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${exercise.muscleGroup.displayNameRu} • Тоннаж: %,d кг".format(exercise.totalVolume.toInt()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDeleteExercise, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Удалить упражнение",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sets Table Header - perfectly matching SetRowItem widths and paddings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Сет",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Тип",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(72.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Вес (кг)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Повт.",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Готово",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp)
                )
            }

            // Sets rows
            exercise.sets.forEachIndexed { setIndex, set ->
                SetRowItem(
                    set = set,
                    onUpdate = { onUpdateSet(setIndex, it) },
                    onDelete = { onDeleteSet(setIndex) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Add set button
            OutlinedButton(
                onClick = onAddSet,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ Добавить подход", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun SetRowItem(
    set: LoggedSet,
    onUpdate: (LoggedSet) -> Unit,
    onDelete: () -> Unit
) {
    var weightText by remember(set.weightKg) { mutableStateOf(if (set.weightKg == 0f) "" else set.weightKg.toString()) }
    var repsText by remember(set.reps) { mutableStateOf(if (set.reps == 0) "" else set.reps.toString()) }
    var showKindMenu by remember { mutableStateOf(false) }

    val bgColor = if (set.isCompleted) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Set number
            Text(
                text = "${set.setNumber}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.width(32.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Set Kind Chip
            Box(modifier = Modifier.width(72.dp)) {
                Surface(
                    onClick = { showKindMenu = true },
                    shape = RoundedCornerShape(8.dp),
                    color = when (set.kind) {
                        SetKind.WARMUP -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                        SetKind.DROP_SET -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                        SetKind.FAILURE -> Color(0xFFEF4444).copy(alpha = 0.2f)
                        SetKind.NORMAL -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = set.kind.displayNameRu,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )
                }

                DropdownMenu(
                    expanded = showKindMenu,
                    onDismissRequest = { showKindMenu = false }
                ) {
                    SetKind.values().forEach { kind ->
                        DropdownMenuItem(
                            text = { Text(kind.displayNameRu) },
                            onClick = {
                                showKindMenu = false
                                onUpdate(set.copy(kind = kind))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Weight input Box - clean, vertically centered, no glyph clipping
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = weightText,
                    onValueChange = { input ->
                        weightText = input
                        val parsed = input.toFloatOrNull() ?: 0f
                        onUpdate(set.copy(weightKg = parsed))
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Reps input Box - clean, vertically centered, no glyph clipping
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = repsText,
                    onValueChange = { input ->
                        repsText = input
                        val parsed = input.toIntOrNull() ?: 0
                        onUpdate(set.copy(reps = parsed))
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Checkbox / Finish Button
            Surface(
                onClick = { onUpdate(set.copy(isCompleted = !set.isCompleted)) },
                shape = RoundedCornerShape(8.dp),
                color = if (set.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (set.isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (set.isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerModal(
    allExercises: List<Exercise>,
    onSelectExercise: (Exercise) -> Unit,
    onDismiss: () -> Unit,
    onCreateExercise: ((String, MuscleGroup, Equipment, String) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf<MuscleGroup?>(null) }
    var showCreateExerciseDialog by remember { mutableStateOf(false) }

    val filtered = allExercises.filter { ex ->
        val matchQuery = searchQuery.isBlank() ||
                ex.name.contains(searchQuery, ignoreCase = true) ||
                ex.primaryMuscle.displayNameRu.contains(searchQuery, ignoreCase = true)
        val matchMuscle = selectedMuscle == null || ex.primaryMuscle == selectedMuscle
        matchQuery && matchMuscle
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Выбор упражнения") },
                    actions = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Поиск упражнения...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )

                    if (onCreateExercise != null) {
                        Surface(
                            onClick = { showCreateExerciseDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.height(56.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Своё",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Muscle filters row (Scrollable LazyRow with maxLines=1: no wrapping!)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedMuscle == null,
                            onClick = { selectedMuscle = null },
                            label = { Text("Все", maxLines = 1) }
                        )
                    }
                    items(MuscleGroup.values()) { mg ->
                        FilterChip(
                            selected = selectedMuscle == mg,
                            onClick = { selectedMuscle = if (selectedMuscle == mg) null else mg },
                            label = { Text(mg.displayNameRu, maxLines = 1) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Упражнение не найдено",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (onCreateExercise != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showCreateExerciseDialog = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Добавить своё упражнение")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(filtered) { _, exercise ->
                            Surface(
                                onClick = { onSelectExercise(exercise) },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = exercise.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${exercise.primaryMuscle.displayNameRu} • ${exercise.equipment.displayNameRu}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateExerciseDialog && onCreateExercise != null) {
        CreateCustomExerciseDialog(
            initialMuscle = selectedMuscle,
            onSave = { name, muscle, eq, desc ->
                onCreateExercise(name, muscle, eq, desc)
                showCreateExerciseDialog = false
            },
            onDismiss = { showCreateExerciseDialog = false }
        )
    }
}

