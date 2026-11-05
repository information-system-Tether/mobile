package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.GpxTrack
import com.example.data.model.TrackPoint
import com.example.ui.theme.PrimaryGreen
import com.example.util.GpxParser
import kotlin.math.roundToInt

@Composable
fun TrackingScreen(
    communityTracks: List<GpxTrack>,
    myTracks: List<GpxTrack>,
    onAddTrack: (GpxTrack) -> Unit,
    onDeleteTrack: (String) -> Unit,
    onPublishToCommunity: (GpxTrack) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Community, 1 = My Tracks
    var selectedTrackForDetails by remember { mutableStateOf<GpxTrack?>(null) }

    // System File Picker for .gpx files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val parsed = GpxParser.parseFromUri(context, uri, "Импортированный трек")
            if (parsed != null && parsed.points.isNotEmpty()) {
                onAddTrack(parsed)
                selectedTab = 1
                Toast.makeText(context, "Трек «${parsed.title}» успешно добавлен!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Не удалось распознать GPX файл", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header & Tabs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Трекинг",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PrimaryGreen
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Комьюнити (${communityTracks.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (selectedTab == 0) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Мои треки (${myTracks.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (selectedTab == 1) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val displayedList = if (selectedTab == 0) communityTracks else myTracks

            if (displayedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedTab == 0) "Нет доступных маршрутов комьюнити" else "У вас пока нет сохраненных треков",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedTab == 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { filePickerLauncher.launch("*/*") },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Загрузить файл .gpx")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(displayedList, key = { it.id }) { track ->
                        TrackItemCard(
                            track = track,
                            isUserTrack = selectedTab == 1,
                            onClick = { selectedTrackForDetails = track },
                            onDelete = { onDeleteTrack(track.id) },
                            onPublish = {
                                onPublishToCommunity(track)
                                Toast.makeText(context, "Трек «${track.title}» опубликован в комьюнити!", Toast.LENGTH_SHORT).show()
                            },
                            onExport = {
                                exportGpxFile(context, track)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Floating Action Button to add GPX track
        FloatingActionButton(
            onClick = { filePickerLauncher.launch("*/*") },
            containerColor = PrimaryGreen,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить трек")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Добавить трек", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Detailed Track Dialog
    selectedTrackForDetails?.let { detailTrack ->
        TrackDetailDialog(
            track = detailTrack,
            isUserTrack = selectedTab == 1,
            onDismiss = { selectedTrackForDetails = null },
            onDelete = {
                onDeleteTrack(detailTrack.id)
                selectedTrackForDetails = null
            },
            onPublish = {
                onPublishToCommunity(detailTrack)
                Toast.makeText(context, "Трек «${detailTrack.title}» опубликован в комьюнити!", Toast.LENGTH_SHORT).show()
                selectedTrackForDetails = null
            },
            onExport = {
                exportGpxFile(context, detailTrack)
            }
        )
    }
}

@Composable
private fun TrackItemCard(
    track: GpxTrack,
    isUserTrack: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onPublish: () -> Unit = {},
    onExport: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Real OSM Route Map Preview
            OsmRouteMapView(
                points = track.points,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Title and Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (isUserTrack) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Удалить трек",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (track.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Track stats chips (Distance, Elevation)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryGreen.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f км".format(track.distanceKm),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PrimaryGreen
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Landscape,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+${track.elevationGainM} м",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row (Separate row, full width, equal weight)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isUserTrack) {
                    Button(
                        onClick = onPublish,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Опубликовать",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Опубликовать", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onExport,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Скачать/Экспорт",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Экспорт GPX", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OsmRouteMapView(
    points: List<TrackPoint>,
    modifier: Modifier = Modifier,
    zoomOffset: Int = 0,
    showZoomControls: Boolean = false,
    onZoomIn: (() -> Unit)? = null,
    onZoomOut: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .background(Color(0xFFE2E8F0)) // neutral map tile background while loading
            .clipToBounds()
    ) {
        val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val heightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val tileSizeDp = with(density) { 256.toDp() }

        if (points.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Маршрут пуст",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            return@BoxWithConstraints
        }

        // Bounding box
        val minLat = points.minOf { it.lat }
        val maxLat = points.maxOf { it.lat }
        val minLon = points.minOf { it.lon }
        val maxLon = points.maxOf { it.lon }

        val normMinX = lonToNormX(minLon)
        val normMaxX = lonToNormX(maxLon)
        val normMinY = latToNormY(maxLat) // maxLat has smaller normY in Mercator
        val normMaxY = latToNormY(minLat)

        val deltaNormX = (normMaxX - normMinX).coerceAtLeast(0.00005)
        val deltaNormY = (normMaxY - normMinY).coerceAtLeast(0.00005)

        // Calculate base zoom to fit bounding box with 25% padding
        val zoomX = kotlin.math.log2((widthPx * 0.75) / (256.0 * deltaNormX))
        val zoomY = kotlin.math.log2((heightPx * 0.75) / (256.0 * deltaNormY))
        val baseZoom = kotlin.math.min(zoomX, zoomY).toInt().coerceIn(2, 17)
        val zoom = (baseZoom + zoomOffset).coerceIn(1, 18)

        val maxTiles = 1 shl zoom
        val worldSizePx = 256.0 * maxTiles

        val centerLat = (minLat + maxLat) / 2.0
        val centerLon = (minLon + maxLon) / 2.0

        val centerPx = lonToNormX(centerLon) * worldSizePx
        val centerPy = latToNormY(centerLat) * worldSizePx

        val viewMinPx = centerPx - (widthPx / 2.0)
        val viewMinPy = centerPy - (heightPx / 2.0)
        val viewMaxPx = centerPx + (widthPx / 2.0)
        val viewMaxPy = centerPy + (heightPx / 2.0)

        val startTileX = kotlin.math.floor(viewMinPx / 256.0).toInt()
        val endTileX = kotlin.math.floor(viewMaxPx / 256.0).toInt()
        val startTileY = kotlin.math.floor(viewMinPy / 256.0).toInt()
        val endTileY = kotlin.math.floor(viewMaxPy / 256.0).toInt()

        // Render OpenStreetMap tiles
        for (ty in startTileY..endTileY) {
            if (ty in 0 until maxTiles) {
                for (tx in startTileX..endTileX) {
                    val wrappedTx = ((tx % maxTiles) + maxTiles) % maxTiles
                    val tileLeftPx = (tx * 256.0 - viewMinPx).toFloat()
                    val tileTopPx = (ty * 256.0 - viewMinPy).toFloat()

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("https://tile.openstreetmap.org/$zoom/$wrappedTx/$ty.png")
                            .addHeader("User-Agent", "TetherApp/1.0 (Android)")
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .offset { IntOffset(tileLeftPx.roundToInt(), tileTopPx.roundToInt()) }
                            .size(tileSizeDp)
                    )
                }
            }
        }

        // Overlay Route Polyline
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (points.size == 1) {
                val pt = points.first()
                val px = (lonToNormX(pt.lon) * worldSizePx - viewMinPx).toFloat()
                val py = (latToNormY(pt.lat) * worldSizePx - viewMinPy).toFloat()
                drawCircle(color = Color.White, radius = 9f, center = Offset(px, py))
                drawCircle(color = Color(0xFF10B981), radius = 6f, center = Offset(px, py))
                return@Canvas
            }

            fun toOffset(pt: TrackPoint): Offset {
                val px = (lonToNormX(pt.lon) * worldSizePx - viewMinPx).toFloat()
                val py = (latToNormY(pt.lat) * worldSizePx - viewMinPy).toFloat()
                return Offset(px, py)
            }

            val path = Path()
            val firstPt = toOffset(points.first())
            path.moveTo(firstPt.x, firstPt.y)

            for (i in 1 until points.size) {
                val p = toOffset(points[i])
                path.lineTo(p.x, p.y)
            }

            // Dark slate outline for crisp contrast on any map tiles
            drawPath(
                path = path,
                color = Color(0xCC0F172A),
                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Primary emerald route polyline
            drawPath(
                path = path,
                color = Color(0xFF10B981),
                style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Start marker (Green with white rim)
            val startPt = toOffset(points.first())
            drawCircle(color = Color.White, radius = 8f, center = startPt)
            drawCircle(color = Color(0xFF10B981), radius = 5.5f, center = startPt)

            // End marker (Red with white rim)
            val endPt = toOffset(points.last())
            drawCircle(color = Color.White, radius = 8f, center = endPt)
            drawCircle(color = Color(0xFFEF4444), radius = 5.5f, center = endPt)
        }

        // Optional Zoom Controls
        if (showZoomControls && (onZoomIn != null || onZoomOut != null)) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (onZoomIn != null) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                        shadowElevation = 3.dp,
                        modifier = Modifier.size(36.dp)
                    ) {
                        IconButton(onClick = onZoomIn) {
                            Icon(Icons.Default.Add, contentDescription = "Приблизить", tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                if (onZoomOut != null) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                        shadowElevation = 3.dp,
                        modifier = Modifier.size(36.dp)
                    ) {
                        IconButton(onClick = onZoomOut) {
                            Icon(Icons.Default.Remove, contentDescription = "Отдалить", tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackDetailDialog(
    track: GpxTrack,
    isUserTrack: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onPublish: () -> Unit,
    onExport: () -> Unit
) {
    var zoomOffset by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top header with title and close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (track.isCommunity) "Маршрут сообщества" else "Мой маршрут",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Large Map with Zoom Controls
                    OsmRouteMapView(
                        points = track.points,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        zoomOffset = zoomOffset,
                        showZoomControls = true,
                        onZoomIn = { if (zoomOffset < 5) zoomOffset++ },
                        onZoomOut = { if (zoomOffset > -5) zoomOffset-- }
                    )

                    // Description if present
                    if (track.description.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = track.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // Key Stats Grid
                    Text(
                        text = "Параметры маршрута",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Straighten,
                            iconTint = PrimaryGreen,
                            title = "Дистанция",
                            value = "%.1f км".format(track.distanceKm)
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Landscape,
                            iconTint = Color(0xFFFF9800),
                            title = "Набор высоты",
                            value = "+${track.elevationGainM} м"
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Place,
                            iconTint = Color(0xFF3B82F6),
                            title = "GPS точек",
                            value = "${track.points.size}"
                        )
                    }

                    // Estimated Activity Times
                    Text(
                        text = "Расчетное время прохождения",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val walkMinutes = (track.distanceKm / 4.5f * 60f).roundToInt()
                        val runMinutes = (track.distanceKm / 9.5f * 60f).roundToInt()
                        val bikeMinutes = (track.distanceKm / 18.0f * 60f).roundToInt()

                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.DirectionsWalk,
                            iconTint = Color(0xFF10B981),
                            title = "Пешком",
                            value = formatMinutes(walkMinutes)
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.DirectionsRun,
                            iconTint = Color(0xFFF59E0B),
                            title = "Бег",
                            value = formatMinutes(runMinutes)
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.DirectionsBike,
                            iconTint = Color(0xFF8B5CF6),
                            title = "Вело",
                            value = formatMinutes(bikeMinutes)
                        )
                    }

                    // GPS Coordinates Section
                    if (track.points.isNotEmpty()) {
                        val start = track.points.first()
                        val finish = track.points.last()

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Старт: %.4f, %.4f (%d м)".format(start.lat, start.lon, start.ele.roundToInt()),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Финиш: %.4f, %.4f (%d м)".format(finish.lat, finish.lon, finish.ele.roundToInt()),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons at the bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isUserTrack) {
                        Button(
                            onClick = onPublish,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Опубликовать", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onExport,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Экспорт GPX", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (isUserTrack) {
                        IconButton(
                            onClick = {
                                onDelete()
                                onDismiss()
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Удалить трек",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatMinutes(totalMinutes: Int): String {
    return if (totalMinutes >= 60) {
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        if (m > 0) "${h} ч ${m} м" else "${h} ч"
    } else {
        "$totalMinutes мин"
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

private fun lonToNormX(lon: Double): Double = (lon + 180.0) / 360.0

private fun latToNormY(lat: Double): Double {
    val clampedLat = lat.coerceIn(-85.05112878, 85.05112878)
    val rad = Math.toRadians(clampedLat)
    val y = Math.log(Math.tan(Math.PI / 4.0 + rad / 2.0))
    return (1.0 - y / Math.PI) / 2.0
}

fun exportGpxFile(context: android.content.Context, track: GpxTrack) {
    try {
        val gpxText = GpxParser.exportToGpxString(track)
        val dir = java.io.File(context.cacheDir, "tracks").apply { mkdirs() }
        val safeName = track.title.trim().replace(Regex("[^a-zA-Z0-9а-яА-Я._-]"), "_").ifBlank { "track_${track.id}" }
        val gpxFile = java.io.File(dir, "$safeName.gpx")
        gpxFile.writeText(gpxText, Charsets.UTF_8)

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            gpxFile
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/gpx+xml"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "${track.title}.gpx")
            putExtra(Intent.EXTRA_TEXT, "Маршрут ${track.title}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Экспорт GPX маршрута")
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "Не удалось экспортировать GPX: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

