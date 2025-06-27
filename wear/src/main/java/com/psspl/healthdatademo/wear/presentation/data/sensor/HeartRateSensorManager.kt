package com.psspl.healthdatademo.wear.presentation.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.psspl.healthdatademo.wear.presentation.data.model.HeartRateData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class HeartRateSensorManager @Inject constructor(private val context: Context) {

    var TAG = "BleServerManager"

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val heartRateSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    var listener: SensorEventListener? = null

    fun getHeartRateFlow() = callbackFlow {
        if (listener == null) {
            listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        if (it.sensor.type == Sensor.TYPE_HEART_RATE) {
                            val heartRate = it.values[0].toInt()
                            Log.e(TAG, "TYPE_HEART_RATE $heartRate")
                            trySend(HeartRateData(heartRate, System.currentTimeMillis(), false, ""))
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    Log.e(TAG, "Heart rate sensor accuracy changed: $accuracy at ${System.currentTimeMillis()}")
                    when (accuracy) {
                        SensorManager.SENSOR_STATUS_NO_CONTACT -> Log.e(TAG, "No contact detected")

                        SensorManager.SENSOR_STATUS_UNRELIABLE -> Log.e(TAG, "Sensor data unreliable")

                        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> Log.e(TAG, "Sensor accuracy low")

                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Log.e(TAG, "Sensor accuracy medium")

                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Log.e(TAG, "Sensor accuracy high")
                    }
                }
            }

            heartRateSensor?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}