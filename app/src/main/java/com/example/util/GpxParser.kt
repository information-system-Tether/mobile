package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.model.GpxTrack
import com.example.data.model.TrackPoint
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object GpxParser {

    fun parse(inputStream: InputStream, fallbackTitle: String = "Новый маршрут"): GpxTrack {
        val points = mutableListOf<TrackPoint>()
        var trackName: String? = null
        var trackDesc: String? = null

        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
        var currentTag = ""
        var currentLat: Double? = null
        var currentLon: Double? = null
        var currentEle = 0.0

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name.lowercase()
                    if (currentTag == "trkpt" || currentTag == "wpt") {
                        val latStr = parser.getAttributeValue(null, "lat")
                        val lonStr = parser.getAttributeValue(null, "lon")
                        currentLat = latStr?.toDoubleOrNull()
                        currentLon = lonStr?.toDoubleOrNull()
                        currentEle = 0.0
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim() ?: ""
                    if (text.isNotEmpty()) {
                        when (currentTag) {
                            "name" -> if (trackName == null) trackName = text
                            "desc" -> if (trackDesc == null) trackDesc = text
                            "ele" -> currentEle = text.toDoubleOrNull() ?: 0.0
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val tag = parser.name.lowercase()
                    if ((tag == "trkpt" || tag == "wpt") && currentLat != null && currentLon != null) {
                        points.add(TrackPoint(currentLat, currentLon, currentEle))
                        currentLat = null
                        currentLon = null
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }

        val distanceKm = calculateTotalDistanceKm(points)
        val elevationGainM = calculateElevationGainMeters(points)

        return GpxTrack(
            id = "gpx_${System.currentTimeMillis()}",
            title = trackName ?: fallbackTitle,
            description = trackDesc ?: "Загружено из GPX файла (${points.size} точек)",
            distanceKm = distanceKm,
            elevationGainM = elevationGainM,
            points = points,
            isCommunity = false,
            createdAt = System.currentTimeMillis()
        )
    }

    fun parseFromUri(context: Context, uri: Uri, fallbackTitle: String = "Маршрут"): GpxTrack? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                parse(stream, fallbackTitle)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun calculateTotalDistanceKm(points: List<TrackPoint>): Float {
        if (points.size < 2) return 0f
        var totalMeters = 0.0
        for (i in 0 until points.size - 1) {
            totalMeters += haversineDistanceMeters(points[i], points[i + 1])
        }
        return (totalMeters / 1000.0).toFloat()
    }

    fun calculateElevationGainMeters(points: List<TrackPoint>): Int {
        if (points.size < 2) return 0
        var gain = 0.0
        for (i in 0 until points.size - 1) {
            val diff = points[i + 1].ele - points[i].ele
            if (diff > 0.5) { // filter minor sensor noise
                gain += diff
            }
        }
        return gain.toInt()
    }

    private fun haversineDistanceMeters(p1: TrackPoint, p2: TrackPoint): Double {
        val r = 6371000.0 // Earth radius in meters
        val lat1Rad = Math.toRadians(p1.lat)
        val lat2Rad = Math.toRadians(p2.lat)
        val deltaLat = Math.toRadians(p2.lat - p1.lat)
        val deltaLon = Math.toRadians(p2.lon - p1.lon)

        val a = sin(deltaLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun exportToGpxString(track: GpxTrack): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<gpx version=\"1.1\" creator=\"tether App\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
        sb.append("  <metadata>\n")
        sb.append("    <name>${escapeXml(track.title)}</name>\n")
        sb.append("    <desc>${escapeXml(track.description)}</desc>\n")
        sb.append("  </metadata>\n")
        sb.append("  <trk>\n")
        sb.append("    <name>${escapeXml(track.title)}</name>\n")
        sb.append("    <trkseg>\n")
        for (pt in track.points) {
            sb.append("      <trkpt lat=\"${pt.lat}\" lon=\"${pt.lon}\">\n")
            sb.append("        <ele>${pt.ele}</ele>\n")
            sb.append("      </trkpt>\n")
        }
        sb.append("    </trkseg>\n")
        sb.append("  </trk>\n")
        sb.append("</gpx>")
        return sb.toString()
    }

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
