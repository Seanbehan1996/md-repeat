package com.yourname.fitnesstracker.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.sqrt

/**
 * Conditional step counter that automatically chooses between real accelerometer
 * and mock implementation based on device type (emulator vs real device)
 */
class ConditionalStepCounter(private val context: Context) {

    private val stepCounter: Any
    private val isUsingMock: Boolean

    init {
        // Check if we should use mock or real step counter
        isUsingMock = shouldUseMock()

        stepCounter = if (isUsingMock) {
            Log.d("ConditionalStepCounter", "🧪 Using MockStepCounter for emulator testing")
            MockStepCounter(context)
        } else {
            Log.d("ConditionalStepCounter", "📱 Using AccelerometerStepCounter for real device")
            RealAccelerometerStepCounter(context)
        }
    }

    /**
     * Determine if we should use mock implementation
     */
    private fun shouldUseMock(): Boolean {
        val isEmulator = (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.FINGERPRINT.contains("vbox")
                || Build.FINGERPRINT.contains("test-keys")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MODEL.contains("sdk_gphone")
                || Build.MODEL.contains("sdk_gphone64")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.MANUFACTURER.equals("Google", ignoreCase = true)
                || Build.MANUFACTURER.equals("unknown", ignoreCase = true)
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.DEVICE.contains("generic")
                || Build.DEVICE.contains("emulator"))

        // Additional check: if we're running on x86/x86_64 architecture, likely emulator
        val isX86Architecture = Build.SUPPORTED_ABIS.any {
            it.contains("x86")
        }

        // Force mock for testing - you can enable this temporarily
        val forceUseMock = true // CHANGE TO FALSE when you want real device detection

        if (forceUseMock) {
            Log.d("ConditionalStepCounter", "🧪 FORCED mock mode for testing")
            return true
        }

        if (isEmulator || isX86Architecture) {
            Log.d("ConditionalStepCounter", "🖥️ Detected emulator environment")
            Log.d("ConditionalStepCounter", "Build details: Model=${Build.MODEL}, Manufacturer=${Build.MANUFACTURER}, Product=${Build.PRODUCT}")
            return true
        }

        // Check if accelerometer is actually available
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            Log.d("ConditionalStepCounter", "❌ No accelerometer detected, using mock")
            return true
        }

        Log.d("ConditionalStepCounter", "✅ Real device with accelerometer detected")
        return false
    }

    // Public API - delegates to appropriate implementation
    fun isAccelerometerAvailable(): Boolean {
        return when (stepCounter) {
            is MockStepCounter -> stepCounter.isAccelerometerAvailable()
            is RealAccelerometerStepCounter -> stepCounter.isAccelerometerAvailable()
            else -> false
        }
    }

    fun startTracking(onStepUpdate: (Int) -> Unit) {
        when (stepCounter) {
            is MockStepCounter -> stepCounter.startTracking(onStepUpdate)
            is RealAccelerometerStepCounter -> stepCounter.startTracking(onStepUpdate)
        }
    }

    fun stopTracking() {
        when (stepCounter) {
            is MockStepCounter -> stepCounter.stopTracking()
            is RealAccelerometerStepCounter -> stepCounter.stopTracking()
        }
    }

    fun getCurrentStepCount(): Int {
        return when (stepCounter) {
            is MockStepCounter -> stepCounter.getCurrentStepCount()
            is RealAccelerometerStepCounter -> stepCounter.getCurrentStepCount()
            else -> 0
        }
    }

    fun resetStepCount() {
        when (stepCounter) {
            is MockStepCounter -> stepCounter.resetStepCount()
            is RealAccelerometerStepCounter -> stepCounter.resetStepCount()
        }
    }

    fun setStepThreshold(threshold: Float) {
        when (stepCounter) {
            is MockStepCounter -> stepCounter.setStepThreshold(threshold)
            is RealAccelerometerStepCounter -> stepCounter.setStepThreshold(threshold)
        }
    }

    fun getStepThreshold(): Float {
        return when (stepCounter) {
            is MockStepCounter -> stepCounter.getStepThreshold()
            is RealAccelerometerStepCounter -> stepCounter.getStepThreshold()
            else -> 0f
        }
    }

    fun isUsingMockImplementation(): Boolean = isUsingMock

    // Mock-only methods for testing
    fun addStepsForTesting(count: Int) {
        if (stepCounter is MockStepCounter) {
            stepCounter.addSteps(count)
        }
    }

    fun simulateRunningForTesting(duration: Long = 10000L) {
        if (stepCounter is MockStepCounter) {
            stepCounter.simulateRunning(duration)
        }
    }

    fun cleanup() {
        when (stepCounter) {
            is MockStepCounter -> stepCounter.cleanup()
            // RealAccelerometerStepCounter doesn't need cleanup
        }
    }
}

/**
 * Real accelerometer step counter
 */
class RealAccelerometerStepCounter(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Step detection parameters
    private var stepThreshold = 10f
    private val stepDelayNs = 300_000_000L // 300ms in nanoseconds

    // State tracking
    private var isTracking = false
    private var stepCount = 0
    private var lastStepTimeNs = 0L
    private var onStepUpdate: ((Int) -> Unit)? = null

    // For smoothing the accelerometer data
    private var lastAcceleration = 9.8f
    private var currentAcceleration = 9.8f

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!isTracking || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val acceleration = sqrt(x * x + y * y + z * z)

            lastAcceleration = currentAcceleration
            currentAcceleration = acceleration * 0.1f + lastAcceleration * 0.9f

            val accelerationDelta = acceleration - currentAcceleration

            if (stepCount % 50 == 0 || accelerationDelta > stepThreshold * 0.8f) {
                Log.d("RealStepCounter", "Raw: $acceleration, Filtered: $currentAcceleration, Delta: $accelerationDelta, Threshold: $stepThreshold")
            }

            if (accelerationDelta > stepThreshold) {
                val currentTimeNs = event.timestamp

                if (currentTimeNs - lastStepTimeNs > stepDelayNs) {
                    stepCount++
                    lastStepTimeNs = currentTimeNs
                    Log.d("RealStepCounter", "✅ STEP DETECTED! Total: $stepCount, Delta: $accelerationDelta")
                    onStepUpdate?.invoke(stepCount)
                } else {
                    val timeDiffMs = (currentTimeNs - lastStepTimeNs) / 1_000_000
                    Log.d("RealStepCounter", "❌ Step rejected - too soon! Time diff: ${timeDiffMs}ms")
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            Log.d("RealStepCounter", "Accelerometer accuracy changed: $accuracy")
        }
    }

    fun isAccelerometerAvailable(): Boolean = accelerometer != null

    fun startTracking(onStepUpdate: (Int) -> Unit) {
        Log.d("RealStepCounter", "🚀 Starting real accelerometer tracking...")

        if (isTracking) {
            Log.w("RealStepCounter", "❌ Already tracking steps")
            return
        }

        if (accelerometer == null) {
            Log.e("RealStepCounter", "❌ Accelerometer not available")
            return
        }

        this.onStepUpdate = onStepUpdate
        stepCount = 0
        lastStepTimeNs = 0L
        lastAcceleration = 9.8f
        currentAcceleration = 9.8f
        isTracking = true

        val registered = sensorManager.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )

        Log.d("RealStepCounter", "✅ Real sensor registered: $registered")
        onStepUpdate.invoke(0)
    }

    fun stopTracking() {
        if (!isTracking) return
        sensorManager.unregisterListener(sensorListener)
        isTracking = false
        onStepUpdate = null
        Log.d("RealStepCounter", "🛑 Real tracking stopped. Final: $stepCount")
    }

    fun getCurrentStepCount(): Int = stepCount
    fun resetStepCount() {
        stepCount = 0
        onStepUpdate?.invoke(stepCount)
        Log.d("RealStepCounter", "🔄 Real step count reset")
    }

    fun setStepThreshold(threshold: Float) {
        stepThreshold = when {
            threshold < 6.0f -> 6.0f
            threshold > 20.0f -> 20.0f
            else -> threshold
        }
        Log.d("RealStepCounter", "🎚️ Real threshold set to: $stepThreshold")
    }

    fun getStepThreshold(): Float = stepThreshold
}

/**
 * Mock step counter for emulator testing
 */
class MockStepCounter(context: Context) {

    private var stepThreshold = 11.5f
    private var simulationSpeed = 1000L

    private var isTracking = false
    private var stepCount = 0
    private var onStepUpdate: ((Int) -> Unit)? = null

    private var simulationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun isAccelerometerAvailable(): Boolean = true

    fun startTracking(onStepUpdate: (Int) -> Unit) {
        Log.d("MockStepCounter", "🧪 Starting mock step simulation...")

        if (isTracking) {
            Log.w("MockStepCounter", "❌ Already simulating")
            return
        }

        this.onStepUpdate = onStepUpdate
        stepCount = 0
        isTracking = true

        simulationJob = scope.launch {
            while (isTracking) {
                delay(simulationSpeed)
                if (isTracking) {
                    stepCount++
                    onStepUpdate?.invoke(stepCount)
                    Log.d("MockStepCounter", "🎭 Simulated step: $stepCount")
                }
            }
        }

        Log.d("MockStepCounter", "✅ Mock simulation started")
        onStepUpdate.invoke(0)
    }

    fun stopTracking() {
        if (!isTracking) return

        isTracking = false
        simulationJob?.cancel()
        simulationJob = null
        onStepUpdate = null
        Log.d("MockStepCounter", "🛑 Mock simulation stopped. Final: $stepCount")
    }

    fun getCurrentStepCount(): Int = stepCount

    fun resetStepCount() {
        stepCount = 0
        onStepUpdate?.invoke(stepCount)
        Log.d("MockStepCounter", "🔄 Mock step count reset")
    }

    fun setStepThreshold(threshold: Float) {
        simulationSpeed = when {
            threshold < 9.0f -> 500L   // High sensitivity = faster simulation
            threshold < 12.0f -> 1000L // Normal
            else -> 2000L              // Low sensitivity = slower simulation
        }
        Log.d("MockStepCounter", "🎚️ Mock speed set to: ${simulationSpeed}ms")
    }

    fun getStepThreshold(): Float = stepThreshold

    fun addSteps(count: Int) {
        if (isTracking) {
            stepCount += count
            onStepUpdate?.invoke(stepCount)
            Log.d("MockStepCounter", "➕ Added $count steps manually. Total: $stepCount")
        }
    }

    fun simulateRunning(duration: Long = 10000L) {
        if (!isTracking) return

        scope.launch {
            val originalSpeed = simulationSpeed
            simulationSpeed = 300L
            Log.d("MockStepCounter", "🏃 Simulating running for ${duration}ms")

            delay(duration)

            simulationSpeed = originalSpeed
            Log.d("MockStepCounter", "🚶 Running simulation ended")
        }
    }

    fun cleanup() {
        simulationJob?.cancel()
        scope.cancel()
    }
}