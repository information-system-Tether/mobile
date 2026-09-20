package com.tether.ui.screens.routes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tether.data.gpx.GpxManager
import com.tether.data.model.GpxRoute
import com.tether.data.model.RouteDifficulty
import com.tether.data.model.TrackPoint
import com.tether.data.repository.RouteRepository
import com.tether.ui.components.RouteMapCanvas
import com.tether.ui.theme.CardShape
import com.tether.ui.theme.PillShape
import com.tether.ui.theme.RouteAccent
import kotlinx.coroutines.delay
import java.io.InputStream
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val communityRoutes by RouteRepository.communityRoutes.collectAsState()
    val myRoutes by RouteRepository.myRoutes.collectAsState()
    val isRecording by RouteRepository.isRecording.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Сообщество, 1: Мои маршруты
    var showRecordDialog by remember { mutableStateOf(false) }

    // File Picker for GPX upload
    val gpxPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val parsedRoute = GpxManager.parseGpxStream(inputStream, "Импортированный GPX")
                    RouteRepository.addCustomRoute(parsedRoute)
                    selectedTab = 1 // Switch to "Мои маршруты"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Маршруты & GPX",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { gpxPickerLauncher.launch("*/*") }) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Загрузить GPX файл",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showRecordDialog = true },
                icon = {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.FiberManualRecord else Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary
                    )
                },
                text = {
                    Text(
                        text = if (isRecording) "Идет запись GPS..." else "Записать маршрут",
                        fontWeight = FontWeight.Bold
                    )
                },
                shape = PillShape,
                containerColor = if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
                contentColor = if (isRecording) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs: Сообщество / Мои маршруты
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Сообщество (${communityRoutes.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Мои маршруты (${myRoutes.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            val currentList = if (selectedTab == 0) communityRoutes else myRoutes

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentList.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = CardShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Маршрутов пока нет",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Вы можете загрузить .gpx файл или записать свой маршрут через GPS",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(currentList, key = { it.id }) { route ->
                        RouteCard(
                            route = route,
                            onExportGpx = {
                                GpxManager.exportAndShareGpx(context, route)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(110.dp))
                }
            }
        }
    }

    if (showRecordDialog) {
        GpxRecordSessionDialog(
            onDismiss = { showRecordDialog = false }
        )
    }
}

@Composable
fun RouteCard(
    route: GpxRoute,
    onExportGpx: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Map Preview
            RouteMapCanvas(route = route, heightDp = 160)

            Spacer(modifier = Modifier.height(12.dp))

            // Title & Difficulty Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = PillShape,
                    color = when (route.difficulty) {
                        RouteDifficulty.EASY -> MaterialTheme.colorScheme.primaryContainer
                        RouteDifficulty.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                        RouteDifficulty.HARD -> MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Text(
                        text = route.difficulty.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        softWrap = false,
                        color = when (route.difficulty) {
                            RouteDifficulty.EASY -> MaterialTheme.colorScheme.onPrimaryContainer
                            RouteDifficulty.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
                            RouteDifficulty.HARD -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }

            if (route.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = route.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics row & Export GPX button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("Дистанция", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${route.distanceKm} км", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Подъем", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+${route.elevationGainMeters} м", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Время", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("~${route.durationMinutes} мин", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onExportGpx,
                    shape = PillShape,
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GPX", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GpxRecordSessionDialog(
    onDismiss: () -> Unit
) {
    val isRecording by RouteRepository.isRecording.collectAsState()
    val recordedPoints by RouteRepository.recordedPoints.collectAsState()

    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var currentSpeedKmh by remember { mutableDoubleStateOf(0.0) }
    var routeName by remember { mutableStateOf("Мой записанный трек") }
    var routeDesc by remember { mutableStateOf("Записан через Tether GPS") }

    // Simulation/Location generator loop while recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            val baseLat = 55.751244
            val baseLon = 37.618423
            while (isRecording) {
                delay(1000L)
                elapsedSeconds++
                // Add simulated progressive GPS track point
                val angle = (elapsedSeconds * 0.05)
                val lat = baseLat + 0.003 * sin(angle)
                val lon = baseLon + 0.005 * (1 - cos(angle))
                val ele = 130.0 + 15 * sin(angle * 2)

                currentSpeedKmh = 10.5 + 2.0 * sin(angle * 3)

                RouteRepository.addTrackPoint(
                    TrackPoint(
                        latitude = lat,
                        longitude = lon,
                        elevation = ele,
                        time = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    val distanceKm = GpxManager.calculateDistanceKm(recordedPoints)
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timerFormatted = String.format("%02d:%02d", minutes, seconds)

    AlertDialog(
        modifier = Modifier.imePadding(),
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRecording) "Идет запись GPX трека" else "Запись маршрута",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                if (isRecording) {
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = "REC",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live metrics HUD
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = timerFormatted,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Время записи",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${Math.round(distanceKm * 100) / 100f} км",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Дистанция", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${Math.round(currentSpeedKmh * 10) / 10.0} км/ч",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Скорость", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${recordedPoints.size}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Точек GPX", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                if (!isRecording) {
                    OutlinedTextField(
                        value = routeName,
                        onValueChange = { routeName = it },
                        label = { Text("Название маршрута") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = routeDesc,
                        onValueChange = { routeDesc = it },
                        label = { Text("Описание") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (!isRecording) {
                Button(
                    onClick = {
                        RouteRepository.startRecording()
                        elapsedSeconds = 0
                    },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Начать запись")
                }
            } else {
                Button(
                    onClick = {
                        RouteRepository.finishRecording(routeName, routeDesc)
                        onDismiss()
                    },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Стоп & Сохранить GPX")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (isRecording) {
                        RouteRepository.cancelRecording()
                    }
                    onDismiss()
                },
                shape = PillShape
            ) {
                Text(if (isRecording) "Отмена" else "Закрыть")
            }
        }
    )
}
