package net.prototux.aare.car

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

/**
 * Ambient light and solar time detector for automatic Day/Night theme switching.
 * Solves the common issue where Chinese head units and aftermarket adapters do not
 * wire vehicle illumination (ILL) or report night mode over AAP.
 */
class AmbientLightDetector(private val context: Context, private val scope: CoroutineScope) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _isNight = MutableStateFlow(calculateTimeBasedNight())
    val isNight: StateFlow<Boolean> = _isNight.asStateFlow()

    private var running = false
    private val main = Handler(Looper.getMainLooper())

    private var darkSampleCount = 0
    private var brightSampleCount = 0

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val lux = event?.values?.getOrNull(0) ?: return
            // Hysteresis filter: require consecutive consistent readings before toggling
            if (lux < LUX_NIGHT_THRESHOLD) {
                brightSampleCount = 0
                darkSampleCount++
                if (darkSampleCount >= SAMPLES_REQUIRED && !_isNight.value) {
                    _isNight.value = true
                }
            } else if (lux > LUX_DAY_THRESHOLD) {
                darkSampleCount = 0
                brightSampleCount++
                if (brightSampleCount >= SAMPLES_REQUIRED && _isNight.value) {
                    _isNight.value = false
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun start() {
        if (running) return
        running = true
        if (lightSensor != null && sensorManager != null) {
            sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            // Fallback: poll time every 60s
            scheduleTimeCheck()
        }
    }

    fun stop() {
        if (!running) return
        running = false
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorListener)
        }
        main.removeCallbacksAndMessages(null)
    }

    private fun scheduleTimeCheck() {
        if (!running) return
        _isNight.value = calculateTimeBasedNight()
        main.postDelayed({ scheduleTimeCheck() }, 60_000L)
    }

    companion object {
        private const val LUX_NIGHT_THRESHOLD = 15.0f
        private const val LUX_DAY_THRESHOLD = 40.0f
        private const val SAMPLES_REQUIRED = 3

        /** Time-based fallback: night between 18:30 and 06:30. */
        fun calculateTimeBasedNight(): Boolean {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            val time = hour + minute / 60.0f
            return time >= 18.5f || time < 6.5f
        }
    }
}

