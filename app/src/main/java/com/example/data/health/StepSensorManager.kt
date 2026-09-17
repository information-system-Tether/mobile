package com.example.data.health

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StepSensorManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _sensorSteps = MutableStateFlow(0)
    val sensorSteps: StateFlow<Int> = _sensorSteps.asStateFlow()

    private var initialSensorValue = -1f

    val isSensorAvailable: Boolean
        get() = stepSensor != null

    fun registerListener() {
        if (stepSensor != null && sensorManager != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun unregisterListener() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSensorSteps = event.values[0]
            if (initialSensorValue < 0) {
                initialSensorValue = totalSensorSteps
            }
            val deltaSteps = (totalSensorSteps - initialSensorValue).toInt().coerceAtLeast(0)
            _sensorSteps.value = deltaSteps
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
