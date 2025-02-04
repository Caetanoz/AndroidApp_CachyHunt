package pt.ipp.estg.cachyhunt.ui.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun LightSensorComponent(): Boolean {

    val darkMode = remember {
        mutableStateOf(false)
    }

    val ctx = LocalContext.current
    val sensorStatus = remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        val sensorManager: SensorManager =
            ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)


        val lightSensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

            }

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_LIGHT) {
                    sensorStatus.value = event.values[0].toInt()
                }
            }
        }

        sensorManager.registerListener(
            lightSensorEventListener,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        onDispose {
            sensorManager.unregisterListener(lightSensorEventListener)
        }

    }
    darkMode.value = sensorStatus.value < 20000

    return darkMode.value
}