package com.example.data.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.data.model.StepActivityStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service for interfacing with Health Connect API (Android 14+ system API, or Health Connect provider app on Android 9-13).
 * Gracefully provides Health Connect status, permissions intent, and step/calorie calculation.
 */
class HealthConnectManager(private val context: Context) {

    // Package for Health Connect provider on Android < 14
    private val healthConnectPackage = "com.google.android.apps.healthdata"

    /**
     * Checks if Health Connect is available on the device:
     * - Android 14+ (SDK 34+) has Health Connect framework built into Android system
     * - Android 9-13 requires the Google Health Connect app from Google Play Store
     */
    fun isHealthConnectAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            true
        } else {
            isPackageInstalled(healthConnectPackage)
        }
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getInstallHealthConnectIntent(): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=$healthConnectPackage")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * Calculate burned calories based on step count and optional body weight in kg.
     */
    fun calculateBurnedCalories(steps: Int, weightKg: Float = 70f): Float {
        return StepActivityStats.calculateCaloriesFromSteps(steps, weightKg)
    }

    /**
     * Estimates distance in meters based on steps (average stride ~0.76m).
     */
    fun calculateDistanceMeters(steps: Int): Float {
        return steps * 0.762f
    }

    /**
     * Estimates active walking/running minutes from steps (~100-110 steps/min).
     */
    fun calculateActiveMinutes(steps: Int): Int {
        return (steps / 105).coerceAtLeast(0)
    }
}
