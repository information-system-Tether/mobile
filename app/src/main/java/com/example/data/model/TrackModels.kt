package com.example.data.model

data class TrackPoint(
    val lat: Double,
    val lon: Double,
    val ele: Double = 0.0
)

data class GpxTrack(
    val id: String,
    val title: String,
    val description: String = "",
    val distanceKm: Float = 0f,
    val elevationGainM: Int = 0,
    val points: List<TrackPoint> = emptyList(),
    val isCommunity: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
