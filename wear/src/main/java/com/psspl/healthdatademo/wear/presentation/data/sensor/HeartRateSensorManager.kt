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

/***
 * Name : HeartRateSensorManager.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : Manages heart rate sensor data collection for the Wear OS version of the HealthDataDemo app.
 */
class HeartRateSensorManager @Inject constructor(
    /*** Context for accessing system services, injected via Hilt. **/
    private val context: Context
) {

    /*** Tag for logging messages related to this sensor manager. **/
    var TAG = "BleServerManager"

    /*** SensorManager instance for accessing hardware sensors, initialized from the context. **/
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    /*** Heart rate sensor instance, nullable if not available on the device. **/
    private val heartRateSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

    /*** Listener for sensor events, nullable to allow registration and unregistration. **/
    var listener: SensorEventListener? = null

    /***
     * Provides a flow of heart rate data from the sensor, emitting HeartRateData objects.
     */
    fun getHeartRateFlow() = callbackFlow {
        if (listener == null) {
            listener = object : SensorEventListener {
                /*** Handles changes in sensor data, emitting heart rate values. **/
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        if (it.sensor.type == Sensor.TYPE_HEART_RATE) {
                            val heartRate = it.values[0].toInt()
                            Log.e(TAG, "TYPE_HEART_RATE $heartRate")
                            trySend(HeartRateData(heartRate, System.currentTimeMillis(), false, ""))
                        }
                    }
                }

                /*** Handles changes in sensor accuracy, logging the new accuracy level. **/
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    Log.e(
                        TAG,
                        "Heart rate sensor accuracy changed: $accuracy at ${System.currentTimeMillis()}"
                    )
                    when (accuracy) {
                        SensorManager.SENSOR_STATUS_NO_CONTACT -> Log.e(TAG, "No contact detected")
                        SensorManager.SENSOR_STATUS_UNRELIABLE -> Log.e(
                            TAG,
                            "Sensor data unreliable"
                        )

                        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> Log.e(
                            TAG,
                            "Sensor accuracy low"
                        )

                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Log.e(
                            TAG,
                            "Sensor accuracy medium"
                        )

                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Log.e(
                            TAG,
                            "Sensor accuracy high"
                        )
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