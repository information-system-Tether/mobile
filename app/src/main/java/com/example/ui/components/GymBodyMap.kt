package com.example.ui.components

import android.content.Context
import android.graphics.RectF
import android.graphics.Region
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.PathParser
import com.example.data.model.MuscleGroup
import com.example.ui.theme.PrimaryGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GymManeBodyData private constructor(
    val viewW: Float,
    val viewH: Float,
    val baseMainPaths: List<android.graphics.Path>,
    val baseLitePaths: List<android.graphics.Path>,
    val muscleFillPaths: Map<String, List<android.graphics.Path>>,
    val muscleHitRegions: Map<String, List<Region>>
) {
    fun muscleAt(x: Float, y: Float): String? {
        val px = x.toInt()
        val py = y.toInt()
        for ((muscleId, regions) in muscleHitRegions) {
            for (reg in regions) {
                if (reg.contains(px, py)) {
                    return muscleId
                }
            }
        }
        for (delta in intArrayOf(-4, 4, -8, 8, -12, 12)) {
            for ((muscleId, regions) in muscleHitRegions) {
                for (reg in regions) {
                    if (reg.contains(px + delta, py) || reg.contains(px, py + delta)) {
                        return muscleId
                    }
                }
            }
        }
        return null
    }

    companion object {
        @Volatile
        private var instance: GymManeBodyData? = null

        suspend fun get(context: Context): GymManeBodyData {
            return instance ?: withContext(Dispatchers.IO) {
                instance ?: loadFromAssets(context.applicationContext).also { instance = it }
            }
        }

        private fun loadFromAssets(context: Context): GymManeBodyData {
            val jsonString = context.assets.open("body_svg.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            val w = root.optDouble("bodyViewW", 535.0).toFloat()
            val h = root.optDouble("bodyViewH", 462.0).toFloat()

            fun parsePathList(jsonArray: org.json.JSONArray?): List<android.graphics.Path> {
                if (jsonArray == null) return emptyList()
                val list = ArrayList<android.graphics.Path>(jsonArray.length())
                for (i in 0 until jsonArray.length()) {
                    val d = jsonArray.getString(i)
                    try {
                        list.add(PathParser.createPathFromPathData(d))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return list
            }

            val baseMain = parsePathList(root.optJSONArray("bodyBaseMain"))
            val baseLite = parsePathList(root.optJSONArray("bodyBaseLite"))

            val fillsObj = root.optJSONObject("muscleFills") ?: JSONObject()
            val muscleFills = HashMap<String, List<android.graphics.Path>>()
            val keys = fillsObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                muscleFills[k] = parsePathList(fillsObj.optJSONArray(k))
            }

            val fullClip = Region(0, 0, (w + 50).toInt(), (h + 50).toInt())
            val hitsObj = root.optJSONObject("muscleHits") ?: JSONObject()
            val muscleHitRegions = HashMap<String, List<Region>>()
            for (mId in muscleFills.keys) {
                val probeList = ArrayList<android.graphics.Path>()
                if (hitsObj.has(mId) && hitsObj.optJSONArray(mId)!!.length() > 0) {
                    probeList.addAll(parsePathList(hitsObj.optJSONArray(mId)))
                }
                muscleFills[mId]?.let { probeList.addAll(it) }

                val regionList = ArrayList<Region>(probeList.size)
                for (path in probeList) {
                    val closed = android.graphics.Path(path)
                    closed.close()
                    val region = Region()
                    region.setPath(closed, fullClip)
                    if (!region.isEmpty) {
                        regionList.add(region)
                    }
                }
                muscleHitRegions[mId] = regionList
            }

            return GymManeBodyData(
                viewW = w,
                viewH = h,
                baseMainPaths = baseMain,
                baseLitePaths = baseLite,
                muscleFillPaths = muscleFills,
                muscleHitRegions = muscleHitRegions
            )
        }
    }
}

// GymMane Heat Ramps
private val heatRampsDark = mapOf(
    "green" to listOf(Color(0xFF1B4B2C), Color(0xFF2C7A44), Color(0xFF3FA95C), Color(0xFF63D67F)),
    "ember" to listOf(Color(0xFF7A4028), Color(0xFFB4632C), Color(0xFFE38B3A), Color(0xFFFFC168))
)

private val heatRampsLight = mapOf(
    "green" to listOf(Color(0xFFBBD9BE), Color(0xFF7FB88A), Color(0xFF488C58), Color(0xFF255E32)),
    "ember" to listOf(Color(0xFFD9B48A), Color(0xFFC07A3C), Color(0xFF9E4A24), Color(0xFF6E2A16))
)

@Composable
fun GymBodyMap(
    selectedMuscle: MuscleGroup?,
    onMuscleTapped: (MuscleGroup) -> Unit,
    modifier: Modifier = Modifier,
    muscleIntensity: Map<MuscleGroup, Float>? = null,
    heatTone: String = "green"
) {
    val context = LocalContext.current
    var bodyData by remember { mutableStateOf<GymManeBodyData?>(null) }
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        bodyData = GymManeBodyData.get(context)
    }

    val data = bodyData
    if (data == null) {
        Box(modifier = modifier.aspectRatio(535f / 462f))
        return
    }

    val bodyBaseMainColor = if (isDark) Color(0xFF28302A) else Color(0xFFE0E8E1)
    val bodyBaseLiteColor = if (isDark) Color(0xFF343E36) else Color(0xFFD3DED4)
    val idleMuscleColor = if (isDark) Color(0xFF3E4B41) else Color(0xFFC3D2C5)
    val outlineColor = MaterialTheme.colorScheme.onSurface

    val ramp = (if (isDark) heatRampsDark else heatRampsLight)[heatTone] ?: heatRampsDark["green"]!!

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(data.viewW / data.viewH)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data, onMuscleTapped) {
                    detectTapGestures { tapOffset ->
                        val scale = size.width / data.viewW
                        val svgX = tapOffset.x / scale
                        val svgY = tapOffset.y / scale
                        val hitId = data.muscleAt(svgX, svgY)
                        android.util.Log.d("GymBodyMap", "Tap at tapOffset=$tapOffset scale=$scale svg=($svgX, $svgY) hitId=$hitId")
                        if (hitId != null) {
                            val mg = MuscleGroup.fromGymManeId(hitId)
                            if (mg != null) {
                                onMuscleTapped(mg)
                            }
                        }
                    }
                }
        ) {
            val scale = size.width / data.viewW

            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                native.save()
                native.scale(scale, scale)

                // 1. Draw base silhouettes
                val mainPaint = android.graphics.Paint().apply {
                    color = bodyBaseMainColor.toArgb()
                    style = android.graphics.Paint.Style.FILL
                    isAntiAlias = true
                }
                for (p in data.baseMainPaths) {
                    native.drawPath(p, mainPaint)
                }

                val litePaint = android.graphics.Paint().apply {
                    color = bodyBaseLiteColor.toArgb()
                    style = android.graphics.Paint.Style.FILL
                    isAntiAlias = true
                }
                for (p in data.baseLitePaths) {
                    native.drawPath(p, litePaint)
                }

                // 2. Draw muscle fills
                val fillPaint = android.graphics.Paint().apply {
                    style = android.graphics.Paint.Style.FILL
                    isAntiAlias = true
                }

                val strokePaint = android.graphics.Paint().apply {
                    color = outlineColor.toArgb()
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 2.4f
                    isAntiAlias = true
                }

                val selectedGymId = selectedMuscle?.gymManeId

                for ((mId, paths) in data.muscleFillPaths) {
                    val mg = MuscleGroup.fromGymManeId(mId)
                    val isSelected = (selectedGymId == mId)

                    val color = when {
                        isSelected -> PrimaryGreen
                        muscleIntensity != null && mg != null -> {
                            val intensity = muscleIntensity[mg] ?: 0f
                            if (intensity <= 0.05f) {
                                idleMuscleColor
                            } else {
                                val level = (intensity * 4f).toInt().coerceIn(1, 4)
                                ramp[level - 1]
                            }
                        }
                        else -> idleMuscleColor
                    }

                    fillPaint.color = color.toArgb()
                    for (p in paths) {
                        native.drawPath(p, fillPaint)
                    }

                    // Selected muscle outline
                    if (isSelected) {
                        for (p in paths) {
                            native.drawPath(p, strokePaint)
                        }
                    }
                }

                native.restore()
            }
        }
    }
}
