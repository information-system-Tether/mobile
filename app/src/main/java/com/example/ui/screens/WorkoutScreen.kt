package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Equipment
import com.example.data.model.Exercise
import com.example.data.model.MuscleGroup
import com.example.data.model.PersonalRecord
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSession
import com.example.ui.components.CreateCustomExerciseDialog
import com.example.ui.components.ExercisePickerModal
import com.example.ui.components.GymBodyMap
import com.example.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    routines: List<WorkoutRoutine>,
    catalogExercises: List<Exercise>,
    sessionsHistory: List<WorkoutSession>,
    personalRecords: List<PersonalRecord>,
    activeSession: WorkoutSession?,
    onStartEmptyWorkout: () -> Unit,
    onStartRoutine: (WorkoutRoutine) -> Unit,
    onResumeActiveWorkout: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    onCreateRoutine: (name: String, targetMuscles: String, exercises: List<Exercise>) -> Unit,
    onCreateExercise: (name: String, muscle: MuscleGroup, equipment: Equipment, description: String) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Программы, 1: Упражнения, 2: История
    var selectedExerciseForDetails by remember { mutableStateOf<Exercise?>(null) }
    var showCreateRoutineDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Программы", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Каталог", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("История", fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedTab) {
            0 -> RoutinesTabContent(
                routines = routines,
                activeSession = activeSession,
                onStartEmptyWorkout = onStartEmptyWorkout,
                onStartRoutine = onStartRoutine,
                onResumeActiveWorkout = onResumeActiveWorkout,
                onOpenCreateRoutine = { showCreateRoutineDialog = true }
            )
            1 -> ExercisesCatalogTabContent(
                exercises = catalogExercises,
                sessionsHistory = sessionsHistory,
                onSelectExercise = { selectedExerciseForDetails = it },
                onCreateExercise = onCreateExercise
            )
            2 -> WorkoutHistoryTabContent(
                sessions = sessionsHistory,
                prs = personalRecords,
                onDeleteSession = onDeleteSession
            )
        }
    }

    // Exercise details dialog
    selectedExerciseForDetails?.let { ex ->
        ExerciseDetailsDialog(
            exercise = ex,
            onDismiss = { selectedExerciseForDetails = null }
        )
    }

    // Create routine dialog
    if (showCreateRoutineDialog) {
        CreateCustomRoutineDialog(
            allExercises = catalogExercises,
            onSave = { name, target, exList ->
                onCreateRoutine(name, target, exList)
                showCreateRoutineDialog = false
            },
            onDismiss = { showCreateRoutineDialog = false }
        )
    }
}

@Composable
private fun RoutinesTabContent(
    routines: List<WorkoutRoutine>,
    activeSession: WorkoutSession?,
    onStartEmptyWorkout: () -> Unit,
    onStartRoutine: (WorkoutRoutine) -> Unit,
    onResumeActiveWorkout: () -> Unit,
    onOpenCreateRoutine: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Active Session banner if running
        if (activeSession != null) {
            item {
                Surface(
                    onClick = onResumeActiveWorkout,
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Тренировка идёт",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${activeSession.title} • Нажмите, чтобы открыть",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Quick start empty workout card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Свободная тренировка",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Начать без готового шаблона",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onStartEmptyWorkout,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Начать тренировку", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Готовые программы",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                TextButton(onClick = onOpenCreateRoutine) {
                    Text("+ Своя программа")
                }
            }
        }

        // Routine items
        items(routines) { routine ->
            RoutineCardItem(
                routine = routine,
                onStart = { onStartRoutine(routine) }
            )
        }
    }
}

@Composable
private fun RoutineCardItem(
    routine: WorkoutRoutine,
    onStart: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = routine.targetMuscles,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Начать")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${routine.exercises.size} упражнений • Отдых: ${routine.defaultRestSeconds}с",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Скрыть" else "Упражнения")
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    routine.exercises.forEachIndexed { idx, ex ->
                        Text(
                            text = "${idx + 1}. ${ex.name} (${ex.primaryMuscle.displayNameRu})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExercisesCatalogTabContent(
    exercises: List<Exercise>,
    sessionsHistory: List<WorkoutSession> = emptyList(),
    onSelectExercise: (Exercise) -> Unit,
    onCreateExercise: (String, MuscleGroup, Equipment, String) -> Unit = { _, _, _, _ -> }
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf<MuscleGroup?>(null) }
    var selectedEquipment by remember { mutableStateOf<Equipment?>(null) }
    var showMapCard by remember { mutableStateOf(true) }
    var showCreateExerciseDialog by remember { mutableStateOf(false) }

    val filtered = exercises.filter { ex ->
        val q = searchQuery.trim().lowercase()
        val matchQ = q.isEmpty() || ex.name.lowercase().contains(q) || ex.primaryMuscle.displayNameRu.lowercase().contains(q)
        val matchM = selectedMuscle == null || ex.primaryMuscle == selectedMuscle || ex.secondaryMuscles.contains(selectedMuscle)
        val matchE = selectedEquipment == null || ex.equipment == selectedEquipment
        matchQ && matchM && matchE
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar & Add Custom Exercise Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск упражнений...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                )

                Button(
                    onClick = { showCreateExerciseDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Своё", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Body Map for Muscle Selection
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Карта мышц",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Нажмите на мышцу для фильтрации",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { showMapCard = !showMapCard }) {
                            Icon(
                                imageVector = if (showMapCard) Icons.Default.Close else Icons.Default.Info,
                                contentDescription = "Скрыть/Показать",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (showMapCard) {
                        Spacer(modifier = Modifier.height(10.dp))

                        GymBodyMap(
                            selectedMuscle = selectedMuscle,
                            onMuscleTapped = { mg ->
                                selectedMuscle = if (selectedMuscle == mg) null else mg
                            },
                            muscleIntensity = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        // Selected muscle feedback badge
                        if (selectedMuscle != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryGreen.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(PrimaryGreen, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${selectedMuscle!!.displayNameRu} (${filtered.size} упр.)",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    TextButton(onClick = { selectedMuscle = null }) {
                                        Text("Сбросить", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Equipment filter chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedEquipment == null,
                        onClick = { selectedEquipment = null },
                        label = { Text("Все снаряды") }
                    )
                }
                items(Equipment.values()) { eq ->
                    FilterChip(
                        selected = selectedEquipment == eq,
                        onClick = { selectedEquipment = if (selectedEquipment == eq) null else eq },
                        label = { Text(eq.displayNameRu) }
                    )
                }
            }
        }

        // Section label
        item {
            Text(
                text = "Найдено упражнений (${filtered.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Exercise items
        items(filtered, key = { it.id }) { exercise ->
            Surface(
                onClick = { onSelectExercise(exercise) },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
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
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${exercise.primaryMuscle.displayNameRu} • ${exercise.equipment.displayNameRu} • ${exercise.difficulty}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Подробнее",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (showCreateExerciseDialog) {
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

@Composable
private fun WorkoutHistoryTabContent(
    sessions: List<WorkoutSession>,
    prs: List<PersonalRecord>,
    onDeleteSession: (Long) -> Unit
) {
    val muscleWorkload = remember(sessions) {
        val map = mutableMapOf<MuscleGroup, Float>()
        for (session in sessions) {
            for (ex in session.exercises) {
                val completedSets = ex.sets.count { it.isCompleted }.coerceAtLeast(1)
                val cur = map[ex.muscleGroup] ?: 0f
                map[ex.muscleGroup] = (cur + completedSets * 0.15f).coerceAtMost(1f)
            }
        }
        map
    }

    // Muscle Workload Summary (volume per muscle)
    val muscleVolumeMap = remember(sessions) {
        val map = mutableMapOf<MuscleGroup, Int>()
        for (s in sessions) {
            for (ex in s.exercises) {
                val sets = ex.sets.count { it.isCompleted }.coerceAtLeast(1)
                map[ex.muscleGroup] = (map[ex.muscleGroup] ?: 0) + sets
            }
        }
        map.entries.sortedByDescending { it.value }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // PRs Section
        if (prs.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Личные рекорды",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        prs.take(5).forEach { pr ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = pr.exerciseName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "%,d кг (1RM: %,d кг)".format(pr.maxWeight.toInt(), pr.maxOneRm.toInt()),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Muscle Workload Heatmap Card with explicit Color Scale Legend
        if (sessions.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Нагрузка мышц за неделю",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Цветовая шкала показывает объем выполненных подходов",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        GymBodyMap(
                            selectedMuscle = null,
                            onMuscleTapped = { },
                            muscleIntensity = muscleWorkload,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Color Scale Legend explaining colors clearly
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(Color(0xFF3E4B41), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Отдых", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(Color(0xFFBBD9BE), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Легкая", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(Color(0xFF488C58), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Средняя", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(Color(0xFF255E32), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Высокая", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        if (muscleVolumeMap.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Нагрузка по группам мышц (подходы)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val maxSets = muscleVolumeMap.maxOf { it.value }.coerceAtLeast(1)
                        muscleVolumeMap.take(6).forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = entry.key.displayNameRu,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(130.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(10.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = (entry.value.toFloat() / maxSets).coerceIn(0.05f, 1f))
                                            .fillMaxHeight()
                                            .background(PrimaryGreen, CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${entry.value} подх.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "История тренировок",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (sessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Пока нет сохранённых тренировок",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(sessions) { session ->
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val mins = session.durationSeconds / 60
                                Text(
                                    text = "${session.date} • $mins мин • Тоннаж: %,d кг".format(session.totalVolume.toInt()),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "-${session.caloriesBurned.toInt()} ккал",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFF5722)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onDeleteSession(session.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Удалить",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (session.exercises.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val exercisesSummary = session.exercises.joinToString(", ") {
                                "${it.exerciseName} (${it.sets.size})"
                            }
                            Text(
                                text = exercisesSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseDetailsDialog(
    exercise: Exercise,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = {
            Column {
                Text(text = exercise.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${exercise.primaryMuscle.displayNameRu} • ${exercise.equipment.displayNameRu}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Техника выполнения:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = exercise.description.ifEmpty { "Правильная фиксация тела и контролируемое движение по всей амплитуде." },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Понятно")
            }
        }
    )
}

@Composable
private fun CreateCustomRoutineDialog(
    allExercises: List<Exercise>,
    onSave: (name: String, targetMuscles: String, exercises: List<Exercise>) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetMuscles by remember { mutableStateOf("") }
    var selectedExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var showPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = { Text("Новая программа тренировок") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название программы") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = targetMuscles,
                    onValueChange = { targetMuscles = it },
                    label = { Text("Целевые мышцы") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedButton(
                    onClick = { showPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Добавить упражнения (${selectedExercises.size})")
                }

                if (selectedExercises.isNotEmpty()) {
                    Text(
                        text = selectedExercises.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), targetMuscles.trim(), selectedExercises)
                    }
                },
                enabled = name.isNotBlank() && selectedExercises.isNotEmpty()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )

    if (showPicker) {
        ExercisePickerModal(
            allExercises = allExercises,
            onSelectExercise = { ex: Exercise ->
                if (!selectedExercises.any { it.id == ex.id }) {
                    selectedExercises = selectedExercises + ex
                }
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

