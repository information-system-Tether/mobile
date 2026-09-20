package com.tether.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tether.data.model.MuscleGroup
import com.tether.ui.theme.CardShape
import com.tether.ui.theme.MuscleHighlight

@Composable
fun MuscleAnatomyCard(
    highlightedMuscle: MuscleGroup?,
    secondaryMuscles: List<MuscleGroup> = emptyList(),
    modifier: Modifier = Modifier,
    onMuscleSelected: ((MuscleGroup) -> Unit)? = null
) {
    var isFrontView by remember { mutableStateOf(highlightedMuscle?.isFront ?: true) }

    // If active muscle changes, automatically flip to the appropriate side
    LaunchedEffect(highlightedMuscle) {
        if (highlightedMuscle != null) {
            isFrontView = highlightedMuscle.isFront
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Анатомия мышц",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = highlightedMuscle?.let { "Целевая: ${it.displayName}" } ?: "Выберите упражнение",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (highlightedMuscle != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // View Toggle Pill (Front / Back)
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val frontSelected = isFrontView
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isFrontView = true }
                            .background(
                                if (frontSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Спереди",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (frontSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isFrontView = false }
                            .background(
                                if (!frontSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Сзади",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (!frontSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Anatomical Figure Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                HumanBodyCanvas(
                    isFront = isFrontView,
                    primaryMuscle = highlightedMuscle,
                    secondaryMuscles = secondaryMuscles,
                    modifier = Modifier.size(width = 180.dp, height = 230.dp),
                    onMuscleClick = onMuscleSelected
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick legend chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Основная нагрузка",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Синергисты",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HumanBodyCanvas(
    isFront: Boolean,
    primaryMuscle: MuscleGroup?,
    secondaryMuscles: List<MuscleGroup>,
    modifier: Modifier = Modifier,
    onMuscleClick: ((MuscleGroup) -> Unit)? = null
) {
    val baseBodyColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val bodyOutlineColor = MaterialTheme.colorScheme.outlineVariant
    val activeColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        // Helper function to pick color for a muscle
        fun muscleColor(group: MuscleGroup): Color {
            return when {
                primaryMuscle == group -> activeColor
                secondaryMuscles.contains(group) -> secondaryColor
                else -> baseBodyColor
            }
        }

        // 1. Head
        drawCircle(
            color = baseBodyColor,
            radius = w * 0.11f,
            center = Offset(cx, h * 0.12f)
        )
        drawCircle(
            color = bodyOutlineColor,
            radius = w * 0.11f,
            center = Offset(cx, h * 0.12f),
            style = Stroke(width = 2.dp.toPx())
        )

        // 2. Neck
        drawRoundRect(
            color = baseBodyColor,
            topLeft = Offset(cx - w * 0.045f, h * 0.20f),
            size = Size(w * 0.09f, h * 0.05f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        if (isFront) {
            // FRONT VIEW

            // Shoulders (Deltoids)
            val leftShoulderColor = muscleColor(MuscleGroup.SHOULDERS)
            drawRoundRect(
                color = leftShoulderColor,
                topLeft = Offset(cx - w * 0.35f, h * 0.23f),
                size = Size(w * 0.14f, h * 0.11f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
            drawRoundRect(
                color = leftShoulderColor,
                topLeft = Offset(cx + w * 0.21f, h * 0.23f),
                size = Size(w * 0.14f, h * 0.11f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )

            // Chest (Pecs)
            val chestColor = muscleColor(MuscleGroup.CHEST)
            drawRoundRect(
                color = chestColor,
                topLeft = Offset(cx - w * 0.21f, h * 0.24f),
                size = Size(w * 0.20f, h * 0.13f),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )
            drawRoundRect(
                color = chestColor,
                topLeft = Offset(cx + w * 0.01f, h * 0.24f),
                size = Size(w * 0.20f, h * 0.13f),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )

            // Abs / Core
            val absColor = muscleColor(MuscleGroup.ABS)
            drawRoundRect(
                color = absColor,
                topLeft = Offset(cx - w * 0.17f, h * 0.38f),
                size = Size(w * 0.34f, h * 0.13f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Biceps & Forearms
            val bicepColor = muscleColor(MuscleGroup.BICEPS)
            // Left Arm
            drawRoundRect(
                color = bicepColor,
                topLeft = Offset(cx - w * 0.36f, h * 0.34f),
                size = Size(w * 0.11f, h * 0.14f),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )
            drawRoundRect(
                color = baseBodyColor,
                topLeft = Offset(cx - w * 0.37f, h * 0.49f),
                size = Size(w * 0.10f, h * 0.14f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
            // Right Arm
            drawRoundRect(
                color = bicepColor,
                topLeft = Offset(cx + w * 0.25f, h * 0.34f),
                size = Size(w * 0.11f, h * 0.14f),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )
            drawRoundRect(
                color = baseBodyColor,
                topLeft = Offset(cx + w * 0.27f, h * 0.49f),
                size = Size(w * 0.10f, h * 0.14f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Pelvis
            drawRoundRect(
                color = baseBodyColor,
                topLeft = Offset(cx - w * 0.18f, h * 0.52f),
                size = Size(w * 0.36f, h * 0.07f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Quads (Front Thighs)
            val quadsColor = muscleColor(MuscleGroup.QUADS)
            // Left Quad
            drawRoundRect(
                color = quadsColor,
                topLeft = Offset(cx - w * 0.18f, h * 0.60f),
                size = Size(w * 0.16f, h * 0.20f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
            // Right Quad
            drawRoundRect(
                color = quadsColor,
                topLeft = Offset(cx + w * 0.02f, h * 0.60f),
                size = Size(w * 0.16f, h * 0.20f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )

            // Lower Legs / Shin
            drawRoundRect(
                color = baseBodyColor,
                topLeft = Offset(cx - w * 0.16f, h * 0.81f),
                size = Size(w * 0.13f, h * 0.16f),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )
            drawRoundRect(
                color = baseBodyColor,
                topLeft = Offset(cx + w * 0.03f, h * 0.81f),
                size = Size(w * 0.13f, h * 0.16f),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )

        } else {
            // BACK VIEW

            // Shoulders on back
            val shoulderColor = muscleColor(MuscleGroup.SHOULDERS)
            drawRoundRect(
                color = shoulderColor,
                topLeft = Offset(cx - w * 0.35f, h * 0.23f),
                size = Size(w * 0.14f, h * 0.11f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
            drawRoundRect(
                color = shoulderColor,
                topLeft = Offset(cx + w * 0.21f, h * 0.23f),
                size = Size(w * 0.14f, h * 0.11f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )

            // Upper Back & Lats
            val backColor = muscleColor(MuscleGroup.BACK)
            drawRoundRect(
                color = backColor,
                topLeft = Offset(cx - w * 0.22f, h * 0.23f),
                size = Size(w * 0.44f, h * 0.19f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )

            // Triceps & Forearms
            val tricepColor = muscleColor(MuscleGroup.TRICEPS)
            drawRoundRect(
                color = tricepColor,
                topLeft = Offset(cx - w * 0.36f, h * 0.34f),
                size = Size(w * 0.11f, h * 0.14f),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )
            drawRoundRect(
                color = baseBodyColor,
                topLeft = Offset(cx - w * 0.37f, h * 0.49f),
                size = Size(w * 0.10f, h * 0.14f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
            drawRoundRect(
                color = tricepColor,
                topLeft = Offset(cx + w * 0.25f, h * 0.34f),
                size = Size(w * 0.11f, h * 0.14f),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )
            drawRoundRect(
                color = baseBodyColor,
                topLeft = Offset(cx + w * 0.27f, h * 0.49f),
                size = Size(w * 0.10f, h * 0.14f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Lower Back
            drawRoundRect(
                color = backColor,
                topLeft = Offset(cx - w * 0.17f, h * 0.43f),
                size = Size(w * 0.34f, h * 0.09f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Glutes & Hamstrings
            val hamstringsColor = muscleColor(MuscleGroup.HAMSTRINGS)
            // Left Glute/Hamstring
            drawRoundRect(
                color = hamstringsColor,
                topLeft = Offset(cx - w * 0.18f, h * 0.53f),
                size = Size(w * 0.16f, h * 0.25f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
            // Right Glute/Hamstring
            drawRoundRect(
                color = hamstringsColor,
                topLeft = Offset(cx + w * 0.02f, h * 0.53f),
                size = Size(w * 0.16f, h * 0.25f),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )

            // Calves (Gastrocnemius)
            val calvesColor = muscleColor(MuscleGroup.CALVES)
            drawRoundRect(
                color = calvesColor,
                topLeft = Offset(cx - w * 0.16f, h * 0.79f),
                size = Size(w * 0.13f, h * 0.18f),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )
            drawRoundRect(
                color = calvesColor,
                topLeft = Offset(cx + w * 0.03f, h * 0.79f),
                size = Size(w * 0.13f, h * 0.18f),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )
        }
    }
}
