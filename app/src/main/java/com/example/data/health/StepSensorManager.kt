package com.example.data.health

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import com.example.data.db.AppDatabase
import com.example.data.db.DailyStepEntity
import com.example.data.model.StepActivityStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepSensorManager(private val context: Context) : SensorEventListener {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tether_step_prefs", Context.MODE_PRIVATE)

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    val isStepCounterAvailable: Boolean
        get() = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null

    val isStepDetectorAvailable: Boolean
        get() = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null

    val isSensorAvailable: Boolean
        get() = isStepCounterAvailable || isStepDetectorAvailable

    private fun todayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun checkDateRollover() {
        val storedDate = prefs.getString(KEY_SAVED_DATE, "") ?: ""
        val today = todayDateString()
        if (storedDate != today) {
            // New day started: reset baseline and daily count
            val lastHardware = prefs.getFloat(KEY_LAST_HARDWARE_VAL, -1f)
            prefs.edit()
                .putString(KEY_SAVED_DATE, today)
                .putFloat(KEY_BASELINE_VAL, if (lastHardware >= 0f) lastHardware else -1f)
                .putInt(KEY_TODAY_STEPS, 0)
                .apply()
            _sensorSteps.value = 0
        }
    }

    fun loadTodaySteps(): Int {
        val storedDate = prefs.getString(KEY_SAVED_DATE, "") ?: ""
        val today = todayDateString()
        if (storedDate != today) {
            val lastHardware = prefs.getFloat(KEY_LAST_HARDWARE_VAL, -1f)
            prefs.edit()
                .putString(KEY_SAVED_DATE, today)
                .putFloat(KEY_BASELINE_VAL, if (lastHardware >= 0f) lastHardware else -1f)
                .putInt(KEY_TODAY_STEPS, 0)
                .apply()
            return 0
        }
        return prefs.getInt(KEY_TODAY_STEPS, 0)
    }

    private val _sensorSteps = MutableStateFlow(loadTodaySteps())
    val sensorSteps: StateFlow<Int> = _sensorSteps.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        checkDateRollover()
    }

    fun registerListener() {
        val counterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (counterSensor != null) {
            sensorManager?.registerListener(this, counterSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else if (detectorSensor != null) {
            sensorManager?.registerListener(this, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun unregisterListener() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        checkDateRollover()
        val today = todayDateString()

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalHardware = event.values[0]
                var baseline = prefs.getFloat(KEY_BASELINE_VAL, -1f)

                // First reading or phone rebooted (totalHardware < baseline)
                if (baseline < 0f || totalHardware < baseline) {
                    baseline = totalHardware
                    prefs.edit().putFloat(KEY_BASELINE_VAL, baseline).apply()
                }

                val currentSteps = (totalHardware - baseline).toInt().coerceAtLeast(0)
                prefs.edit()
                    .putFloat(KEY_LAST_HARDWARE_VAL, totalHardware)
                    .putInt(KEY_TODAY_STEPS, currentSteps)
                    .apply()

                _sensorSteps.value = currentSteps
                persistToDatabase(today, currentSteps)
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                val current = prefs.getInt(KEY_TODAY_STEPS, 0) + 1
                prefs.edit().putInt(KEY_TODAY_STEPS, current).apply()
                _sensorSteps.value = current
                persistToDatabase(today, current)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setManualSteps(steps: Int) {
        val today = todayDateString()
        val clamped = steps.coerceAtLeast(0)
        prefs.edit()
            .putString(KEY_SAVED_DATE, today)
            .putInt(KEY_TODAY_STEPS, clamped)
            .apply()
        _sensorSteps.value = clamped
        persistToDatabase(today, clamped)
    }

    fun addManualSteps(delta: Int) {
        val current = loadTodaySteps()
        setManualSteps(current + delta)
    }

    private fun persistToDatabase(date: String, steps: Int) {
        scope.launch {
            try {
                val burned = calculateBurnedCalories(steps)
                val dist = calculateDistanceMeters(steps)
                val mins = calculateActiveMinutes(steps)
                val dao = AppDatabase.getInstance(context).foodDao()
                dao.setDailySteps(
                    DailyStepEntity(
                        date = date,
                        steps = steps,
                        caloriesBurned = burned,
                        distanceMeters = dist,
                        activeMinutes = mins,
                        isFromHealthConnect = false,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val KEY_SAVED_DATE = "key_step_saved_date"
        private const val KEY_BASELINE_VAL = "key_step_baseline_val"
        private const val KEY_LAST_HARDWARE_VAL = "key_step_last_hardware_val"
        private const val KEY_TODAY_STEPS = "key_step_today_steps"

        fun calculateBurnedCalories(steps: Int, weightKg: Float = 70f): Float {
            return StepActivityStats.calculateCaloriesFromSteps(steps, weightKg)
        }

        fun calculateDistanceMeters(steps: Int): Float {
            return steps * 0.762f
        }

        fun calculateActiveMinutes(steps: Int): Int {
            return (steps / 105).coerceAtLeast(0)
        }

        fun startStepService(context: Context) {
            val intent = Intent(context, StepTrackingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopStepService(context: Context) {
            val intent = Intent(context, StepTrackingService::class.java)
            context.stopService(intent)
        }
    }
}
