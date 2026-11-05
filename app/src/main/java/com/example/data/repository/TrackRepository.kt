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

    suspend fun publishToCommunity(track: GpxTrack) = withContext(Dispatchers.IO) {
        val current = _communityTracks.value.toMutableList()
        val pubTrack = track.copy(
            id = "comm_pub_${System.currentTimeMillis()}",
            isCommunity = true
        )
        current.add(0, pubTrack)
        _communityTracks.value = current
    }

    companion object {
        private const val KEY_MY_TRACKS = "key_my_gpx_tracks"

        fun getCommunitySampleTracks(): List<GpxTrack> {
            return emptyList()
        }
    }
}
