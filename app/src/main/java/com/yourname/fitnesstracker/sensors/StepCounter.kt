package com.yourname.fitnesstracker.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepCounter(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var stepCountStart: Float = 0f
    private var isTracking = false

    private var onStepUpdate: ((Int) -> Unit)? = null

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!isTracking) return
            if (stepCountStart == 0f) {
                stepCountStart = event.values[0]
            }
            val currentSteps = event.values[0] - stepCountStart
            onStepUpdate?.invoke(currentSteps.toInt())
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun startTracking(onStepUpdate: (Int) -> Unit) {
        if (isTracking || stepSensor == null) return
        this.onStepUpdate = onStepUpdate
        stepCountStart = 0f
        isTracking = true
        sensorManager.registerListener(sensorListener, stepSensor, SensorManager.SENSOR_DELAY_UI)
    }

    fun stopTracking() {
        if (!isTracking) return
        sensorManager.unregisterListener(sensorListener)
        isTracking = false
        onStepUpdate = null
        stepCountStart = 0f
    }
}
