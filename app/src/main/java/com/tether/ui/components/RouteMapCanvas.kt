package com.tether.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tether.data.model.GpxRoute
import com.tether.data.model.TrackPoint
import com.tether.ui.theme.RouteAccent

@Composable
fun RouteMapCanvas(
    route: GpxRoute,
    modifier: Modifier = Modifier,
    heightDp: Int = 180
) {
    val points = route.points
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainerHighest
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceContainer)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw subtle map grid lines
            val step = 32.dp.toPx()
            var x = 0f
            while (x < w) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
                x += step
            }
            var y = 0f
            while (y < h) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
                y += step
            }

            if (points.size < 2) {
                // If single point or empty, draw a beacon dot
                drawCircle(
                    color = primaryColor,
                    radius = 8.dp.toPx(),
                    center = Offset(w / 2f, h / 2f)
                )
                return@Canvas
            }

            // 2. Compute bounding box for projection
            val minLat = points.minOf { it.latitude }
            val maxLat = points.maxOf { it.latitude }
            val minLon = points.minOf { it.longitude }
            val maxLon = points.maxOf { it.longitude }

            val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
            val lonSpan = (maxLon - minLon).coerceAtLeast(0.0001)

            val padding = 28.dp.toPx()
            val usableWidth = w - padding * 2
            val usableHeight = h - padding * 2

            // Function to map GPS (lat, lon) -> Canvas (x, y)
            fun project(point: TrackPoint): Offset {
                val normX = ((point.longitude - minLon) / lonSpan).toFloat()
                val normY = (1f - ((point.latitude - minLat) / latSpan).toFloat()) // Invert Y
                return Offset(
                    x = padding + normX * usableWidth,
                    y = padding + normY * usableHeight
                )
            }

            // 3. Build polyline path
            val path = Path()
            val startOffset = project(points.first())
            path.moveTo(startOffset.x, startOffset.y)

            for (i in 1 until points.size) {
                val ptOffset = project(points[i])
                path.lineTo(ptOffset.x, ptOffset.y)
            }

            // 4. Draw outer shadow/glow
            drawPath(
                path = path,
                color = primaryColor.copy(alpha = 0.3f),
                style = Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 5. Draw main polyline
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    listOf(
                        primaryColor,
                        RouteAccent
                    )
                ),
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 6. Draw START Pin (Green circle)
            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = startOffset
            )
            drawCircle(
                color = Color(0xFF10B981),
                radius = 5.dp.toPx(),
                center = startOffset
            )

            // 7. Draw FINISH Pin (Red/Flag circle)
            val finishOffset = project(points.last())
            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = finishOffset
            )
            drawCircle(
                color = Color(0xFFEF4444),
                radius = 5.dp.toPx(),
                center = finishOffset
            )
        }

        // Overlay chips: Distance & Elevation
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        ) {
            Text(
                text = "${route.distanceKm} км • +${route.elevationGainMeters} м",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
