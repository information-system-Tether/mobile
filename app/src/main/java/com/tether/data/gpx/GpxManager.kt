package com.tether.data.gpx

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.tether.data.model.GpxRoute
import com.tether.data.model.RouteDifficulty
import com.tether.data.model.TrackPoint
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GpxManager {

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Parses GPX XML text into track points and calculates distance & elevation gain
     */
    fun parseGpx(xmlContent: String, routeName: String = "Импортированный маршрут"): GpxRoute {
        return parseGpxStream(xmlContent.byteInputStream(), routeName)
    }

    fun parseGpxStream(inputStream: InputStream, fallbackName: String = "Импортированный маршрут"): GpxRoute {
        val points = mutableListOf<TrackPoint>()
        var extractedName = fallbackName
        var extractedDesc = "Маршрут загружен из GPX"

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(inputStream, "UTF-8")

            var eventType = parser.eventType
            var currentLat: Double? = null
            var currentLon: Double? = null
            var currentEle: Double = 0.0
            var currentTime: Long = System.currentTimeMillis()
            var currentTag = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag.equals("trkpt", ignoreCase = true) || currentTag.equals("wpt", ignoreCase = true)) {
                            val latStr = parser.getAttributeValue(null, "lat")
                            val lonStr = parser.getAttributeValue(null, "lon")
                            currentLat = latStr?.toDoubleOrNull()
                            currentLon = lonStr?.toDoubleOrNull()
                            currentEle = 0.0
                            currentTime = System.currentTimeMillis()
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim().orEmpty()
                        if (text.isNotEmpty()) {
                            when {
                                currentTag.equals("name", ignoreCase = true) && currentLat == null -> {
                                    extractedName = text
                                }
                                currentTag.equals("desc", ignoreCase = true) && currentLat == null -> {
                                    extractedDesc = text
                                }
                                currentTag.equals("ele", ignoreCase = true) -> {
                                    currentEle = text.toDoubleOrNull() ?: 0.0
                                }
                                currentTag.equals("time", ignoreCase = true) -> {
                                    try {
                                        currentTime = isoDateFormat.parse(text)?.time ?: System.currentTimeMillis()
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val endTag = parser.name
                        if (endTag.equals("trkpt", ignoreCase = true) || endTag.equals("wpt", ignoreCase = true)) {
                            if (currentLat != null && currentLon != null) {
                                points.add(
                                    TrackPoint(
                                        latitude = currentLat,
                                        longitude = currentLon,
                                        elevation = currentEle,
                                        time = currentTime
                                    )
                                )
                            }
                            currentLat = null
                            currentLon = null
                        }
                        currentTag = ""
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val distanceKm = calculateDistanceKm(points)
        val elevationGain = calculateElevationGain(points)
        val durationMins = if (points.size >= 2) {
            val diffMs = points.last().time - points.first().time
            if (diffMs > 0) (diffMs / 60000L).toInt() else ((distanceKm / 5f) * 60).toInt()
        } else {
            ((distanceKm / 5f) * 60).toInt()
        }

        val difficulty = when {
            distanceKm > 15 || elevationGain > 500 -> RouteDifficulty.HARD
            distanceKm > 7 || elevationGain > 150 -> RouteDifficulty.MEDIUM
            else -> RouteDifficulty.EASY
        }

        return GpxRoute(
            name = extractedName,
            description = extractedDesc,
            distanceKm = (Math.round(distanceKm * 10) / 10f),
            elevationGainMeters = elevationGain,
            durationMinutes = durationMins.coerceAtLeast(10),
            difficulty = difficulty,
            points = points,
            isCommunity = false,
            isUserRecorded = false
        )
    }

    /**
     * Converts a GpxRoute into standard GPX 1.1 XML format
     */
    fun buildGpxXml(route: GpxRoute): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>""").append("\n")
        sb.append("""<gpx version="1.1" creator="Tether App (Material 3 Expressive)" xmlns="http://www.topografix.com/GPX/1/1">""").append("\n")
        sb.append("  <metadata>\n")
        sb.append("    <name>${escapeXml(route.name)}</name>\n")
        sb.append("    <desc>${escapeXml(route.description)}</desc>\n")
        sb.append("    <time>${isoDateFormat.format(Date(route.createdAt))}</time>\n")
        sb.append("  </metadata>\n")
        sb.append("  <trk>\n")
        sb.append("    <name>${escapeXml(route.name)}</name>\n")
        sb.append("    <desc>${escapeXml(route.description)}</desc>\n")
        sb.append("    <trkseg>\n")
        for (pt in route.points) {
            sb.append("""      <trkpt lat="${pt.latitude}" lon="${pt.longitude}">""").append("\n")
            sb.append("        <ele>${pt.elevation}</ele>\n")
            sb.append("        <time>${isoDateFormat.format(Date(pt.time))}</time>\n")
            sb.append("      </trkpt>\n")
        }
        sb.append("    </trkseg>\n")
        sb.append("  </trk>\n")
        sb.append("</gpx>\n")
        return sb.toString()
    }

    fun exportAndShareGpx(context: Context, route: GpxRoute) {
        try {
            val gpxXml = buildGpxXml(route)
            val exportDir = File(context.cacheDir, "gpx_exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val sanitizedName = route.name.replace("[^a-zA-Z0-9А-Яа-я_\\-]".toRegex(), "_")
            val gpxFile = File(exportDir, "${sanitizedName}_tether.gpx")
            gpxFile.writeText(gpxXml, Charsets.UTF_8)

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                gpxFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/gpx+xml"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "GPX маршрут: ${route.name}")
                putExtra(Intent.EXTRA_TEXT, "Маршрут '${route.name}' (${route.distanceKm} км) из приложения Tether.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Экспорт GPX маршрута").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            // Fallback plain share
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "GPX: ${route.name}")
                putExtra(Intent.EXTRA_TEXT, buildGpxXml(route))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Поделиться GPX кодом").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    fun calculateDistanceKm(points: List<TrackPoint>): Float {
        if (points.size < 2) return 0f
        var totalMeters = 0.0
        for (i in 0 until points.size - 1) {
            totalMeters += haversineMeters(
                points[i].latitude, points[i].longitude,
                points[i + 1].latitude, points[i + 1].longitude
            )
        }
        return (totalMeters / 1000.0).toFloat()
    }

    fun calculateElevationGain(points: List<TrackPoint>): Int {
        if (points.size < 2) return 0
        var gain = 0.0
        for (i in 0 until points.size - 1) {
            val diff = points[i + 1].elevation - points[i].elevation
            if (diff > 0) gain += diff
        }
        return gain.toInt()
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun escapeXml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    /**
     * Default community routes with scenic real-world coordinate loops
     */
    fun getCommunityRoutes(): List<GpxRoute> {
        return listOf(
            createScenicRoute(
                name = "Парк Горького и Набережная",
                description = "Популярный плоский маршрут вдоль реки для пробежек и вечерних прогулок с прекрасными видами на закат.",
                startLat = 55.7298,
                startLon = 37.6015,
                distanceKm = 6.4f,
                elevationGain = 25,
                durationMinutes = 45,
                difficulty = RouteDifficulty.EASY,
                shapeType = 0
            ),
            createScenicRoute(
                name = "Тропа Воробьёвы Горы",
                description = "Маршрут с приятным рельефом, смотровой площадкой и лесными тенистыми тропинками.",
                startLat = 55.7100,
                startLon = 37.5450,
                distanceKm = 9.2f,
                elevationGain = 140,
                durationMinutes = 70,
                difficulty = RouteDifficulty.MEDIUM,
                shapeType = 1
            ),
            createScenicRoute(
                name = "Битцевский лесной трейл",
                description = "Грунтовый трейловый маршрут для бега по пересеченной местности или гравийного велосипеда.",
                startLat = 55.6200,
                startLon = 37.5500,
                distanceKm = 14.8f,
                elevationGain = 210,
                durationMinutes = 110,
                difficulty = RouteDifficulty.HARD,
                shapeType = 2
            ),
            createScenicRoute(
                name = "Красная Поляна - Хребет Аибга",
                description = "Живописнейший горный маршрут с панорамными альпийскими лугами и чистым горным воздухом.",
                startLat = 43.6833,
                startLon = 40.2500,
                distanceKm = 11.5f,
                elevationGain = 680,
                durationMinutes = 180,
                difficulty = RouteDifficulty.HARD,
                shapeType = 3
            )
        )
    }

    private fun createScenicRoute(
        name: String,
        description: String,
        startLat: Double,
        startLon: Double,
        distanceKm: Float,
        elevationGain: Int,
        durationMinutes: Int,
        difficulty: RouteDifficulty,
        shapeType: Int = 0
    ): GpxRoute {
        val points = mutableListOf<TrackPoint>()
        val pointCount = 36
        val baseTime = System.currentTimeMillis() - (durationMinutes * 60 * 1000L)
        val scale = distanceKm / 10f

        for (i in 0 until pointCount) {
            val angle = (2 * Math.PI * i) / (pointCount - 1)
            val (latOffset, lonOffset, eleOffset) = when (shapeType) {
                0 -> {
                    // River promenade: elongated S-curve along water
                    val lat = (0.012 * scale) * (sin(angle) + 0.25 * sin(2 * angle))
                    val lon = (0.035 * scale) * (cos(angle * 0.5) - 1.0)
                    val ele = 120.0 + 8 * sin(angle * 2)
                    Triple(lat, lon, ele)
                }
                1 -> {
                    // Hill climb & switchbacks
                    val lat = (0.020 * scale) * sin(angle)
                    val lon = (0.018 * scale) * (1 - cos(angle)) + 0.005 * sin(3 * angle)
                    val ele = 135.0 + (sin(angle) * (elevationGain * 0.85))
                    Triple(lat, lon, ele)
                }
                2 -> {
                    // Serpentine forest loop
                    val lat = (0.022 * scale) * (sin(angle) + 0.3 * sin(3 * angle))
                    val lon = (0.024 * scale) * (cos(angle) + 0.3 * cos(2 * angle) - 1.0)
                    val ele = 160.0 + 25 * sin(angle * 3)
                    Triple(lat, lon, ele)
                }
                else -> {
                    // Alpine mountain ridge ascent
                    val progress = i.toDouble() / (pointCount - 1)
                    val ridgeLat = (0.030 * scale) * (sin(angle) + 0.15 * sin(5 * angle))
                    val ridgeLon = (0.028 * scale) * (1 - cos(angle))
                    val peakProgress = sin(Math.PI * progress)
                    val ele = 540.0 + (peakProgress * elevationGain)
                    Triple(ridgeLat, ridgeLon, ele)
                }
            }

            val time = baseTime + ((durationMinutes * 60 * 1000L * i) / pointCount)
            points.add(
                TrackPoint(
                    latitude = startLat + latOffset,
                    longitude = startLon + lonOffset,
                    elevation = Math.max(10.0, eleOffset),
                    time = time
                )
            )
        }

        return GpxRoute(
            name = name,
            description = description,
            distanceKm = distanceKm,
            elevationGainMeters = elevationGain,
            durationMinutes = durationMinutes,
            difficulty = difficulty,
            points = points,
            isCommunity = true,
            isUserRecorded = false
        )
    }
}
