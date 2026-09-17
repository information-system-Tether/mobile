package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.GpxTrack
import com.example.data.model.TrackPoint
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class TrackRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_tracks_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val trackListType = Types.newParameterizedType(List::class.java, GpxTrack::class.java)
    private val jsonAdapter = moshi.adapter<List<GpxTrack>>(trackListType)

    private val _communityTracks = MutableStateFlow(getCommunitySampleTracks())
    val communityTracks: StateFlow<List<GpxTrack>> = _communityTracks.asStateFlow()

    private val _myTracks = MutableStateFlow(loadUserTracks())
    val myTracks: StateFlow<List<GpxTrack>> = _myTracks.asStateFlow()

    private fun loadUserTracks(): List<GpxTrack> {
        val json = prefs.getString(KEY_MY_TRACKS, null) ?: return emptyList()
        return try {
            jsonAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveUserTracks(tracks: List<GpxTrack>) {
        val json = jsonAdapter.toJson(tracks)
        prefs.edit().putString(KEY_MY_TRACKS, json).apply()
        _myTracks.value = tracks
    }

    suspend fun addTrack(track: GpxTrack) = withContext(Dispatchers.IO) {
        val current = _myTracks.value.toMutableList()
        current.add(0, track)
        saveUserTracks(current)
    }

    suspend fun deleteTrack(trackId: String) = withContext(Dispatchers.IO) {
        val current = _myTracks.value.filter { it.id != trackId }
        saveUserTracks(current)
    }

    companion object {
        private const val KEY_MY_TRACKS = "key_my_gpx_tracks"

        fun getCommunitySampleTracks(): List<GpxTrack> {
            return listOf(
                GpxTrack(
                    id = "comm_1",
                    title = "Малый круг Крылатское",
                    description = "Популярный живописный велоклубный и беговой маршрут в Крылатском с динамичным рельефом.",
                    distanceKm = 4.2f,
                    elevationGainM = 95,
                    isCommunity = true,
                    points = listOf(
                        TrackPoint(55.7558, 37.4140, 140.0),
                        TrackPoint(55.7570, 37.4180, 152.0),
                        TrackPoint(55.7600, 37.4220, 168.0),
                        TrackPoint(55.7620, 37.4190, 175.0),
                        TrackPoint(55.7610, 37.4120, 158.0),
                        TrackPoint(55.7580, 37.4080, 145.0),
                        TrackPoint(55.7558, 37.4140, 140.0)
                    )
                ),
                GpxTrack(
                    id = "comm_2",
                    title = "Лесной маршрут Сокольники",
                    description = "Плоская ровная трасса через тенистые аллеи парка Сокольники, идеальна для бега и ходьбы.",
                    distanceKm = 7.8f,
                    elevationGainM = 24,
                    isCommunity = true,
                    points = listOf(
                        TrackPoint(55.7925, 37.6780, 148.0),
                        TrackPoint(55.7980, 37.6830, 150.0),
                        TrackPoint(55.8050, 37.6890, 152.0),
                        TrackPoint(55.8080, 37.6820, 151.0),
                        TrackPoint(55.8020, 37.6720, 149.0),
                        TrackPoint(55.7950, 37.6710, 148.0),
                        TrackPoint(55.7925, 37.6780, 148.0)
                    )
                ),
                GpxTrack(
                    id = "comm_3",
                    title = "Набережная Парка Горького",
                    description = "Панорамный душевный маршрут вдоль Москва-реки от Воробьевых гор до Нескучного сада.",
                    distanceKm = 10.5f,
                    elevationGainM = 40,
                    isCommunity = true,
                    points = listOf(
                        TrackPoint(55.7110, 37.5450, 120.0),
                        TrackPoint(55.7170, 37.5620, 122.0),
                        TrackPoint(55.7230, 37.5810, 121.0),
                        TrackPoint(55.7310, 37.6010, 123.0),
                        TrackPoint(55.7350, 37.6080, 125.0),
                        TrackPoint(55.7310, 37.6010, 123.0),
                        TrackPoint(55.7230, 37.5810, 121.0),
                        TrackPoint(55.7110, 37.5450, 120.0)
                    )
                )
            )
        }
    }
}
