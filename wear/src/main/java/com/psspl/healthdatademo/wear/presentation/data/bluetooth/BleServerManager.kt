package com.psspl.healthdatademo.wear.presentation.data.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.psspl.healthdatademo.wear.presentation.data.model.HeartRateData
import com.psspl.healthdatademo.wear.presentation.theme.StringResources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.security.Key
import java.util.Base64
import javax.inject.Inject

/***
 * Name : BleServerManager.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 30 Jun 2025
 * Desc : Manages BLE server operations for the Wear OS version of the HealthDataDemo app with encryption.
 */
class BleServerManager @Inject constructor(
    /*** Context for accessing system services, injected via Hilt. **/
    val context: Context
) {

    /*** Tag for logging messages related to this BLE server manager. **/
    var TAG = "BleServerManager"

    /*** UUID for the heart rate service, used for BLE communication. **/
    private val serviceUuid = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")

    /*** UUID for the heart rate characteristic, used for BLE communication. **/
    private val characteristicUuid = UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB")

    /*** Mutable state flow holding the current connection state, initialized to false. **/
    private val _connectionState = MutableStateFlow(false)

    /*** Read-only state flow exposing the current connection state to observers. **/
    val connectionState: StateFlow<Boolean> = _connectionState

    /*** BluetoothManager instance for managing BLE operations, initialized from the context. **/
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    /*** GATT server instance, nullable to allow creation and cleanup. **/
    private var gattServer: BluetoothGattServer? = null

    /*** BluetoothAdapter instance for BLE advertising, nullable if not available. **/
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    /*** Encryption key and utility (demo key for simplicity). **/
    private val keySpec: Key = SecretKeySpec(StringResources.secretKey, "AES")

    /*** Callback for handling BLE advertising events. **/
    private val advertiseCallback = object : AdvertiseCallback() {
        /*** Logs successful start of advertising. **/
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.e(
                TAG,
                "Advertising started successfully at ${System.currentTimeMillis()}"
            )
        }

        /*** Logs failure of advertising with error code. **/
        override fun onStartFailure(errorCode: Int) {
            Log.e(
                TAG,
                "Advertising failed with error: $errorCode at ${System.currentTimeMillis()}"
            )
        }
    }

    /***
     * Used for encrypt data.
     */
    private fun encryptData(data: String): String {
        val cipher = Cipher.getInstance(StringResources.transformationAlgo)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    /***
     * Starts the BLE server and sets up the service, requiring BLUETOOTH_CONNECT permission.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun startServer() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted")
            return
        }

        gattServer =
            bluetoothManager.openGattServer(context, object : BluetoothGattServerCallback() {
                /*** Updates connection state and logs changes. **/
                override fun onConnectionStateChange(
                    device: BluetoothDevice?,
                    status: Int,
                    newState: Int
                ) {
                    _connectionState.value = newState == BluetoothProfile.STATE_CONNECTED
                    Log.e(
                        TAG,
                        "Server connection state changed: $newState, status: $status at ${System.currentTimeMillis()}"
                    )
                }

                /*** Handles service addition and starts advertising if successful, requiring BLUETOOTH_ADVERTISE permission. **/
                @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
                override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        startAdvertising()
                        Log.e(
                            TAG,
                            "Service added successfully, starting advertising at ${System.currentTimeMillis()}"
                        )
                    } else {
                        Log.e(
                            TAG,
                            "Failed to add service, status: $status at ${System.currentTimeMillis()}"
                        )
                    }
                }
            })

        val service = BluetoothGattService(serviceUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val characteristic = BluetoothGattCharacteristic(
            characteristicUuid,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        service.addCharacteristic(characteristic)
        gattServer?.addService(service) // This triggers onServiceAdded
    }

    /***
     * Starts BLE advertising, requiring BLUETOOTH_ADVERTISE permission.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    private fun startAdvertising() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "BLUETOOTH_ADVERTISE permission not granted")
            return
        }
        bluetoothAdapter?.bluetoothLeAdvertiser?.startAdvertising(
            AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTimeout(0)
                .build(),
            AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid(serviceUuid))
                .setIncludeDeviceName(false)
                .build(),
            advertiseCallback
        )
    }

    /***
     * Stops the BLE server and advertising, requiring BLUETOOTH_CONNECT permission.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun stopServer() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "BLUETOOTH_ADVERTISE permission not granted in stopServer")
            return
        }
        bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        gattServer?.close()
        gattServer = null
        _connectionState.value = false
        Log.e(TAG, "Server stopped at ${System.currentTimeMillis()}")
    }

    /***
     * Sends heart rate data to connected devices via BLE, requiring BLUETOOTH_CONNECT permission.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendHeartRate(heartRate: HeartRateData) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted in sendHeartRate")
            return
        }

        val service = gattServer?.getService(serviceUuid)
        val characteristic = service?.getCharacteristic(characteristicUuid)
        if (characteristic == null) {
            Log.e(
                TAG,
                "Characteristic not found for sending heart rate at ${System.currentTimeMillis()}"
            )
            return
        }

        // Validate BPM
        val bpm = if (heartRate.bpm in 30..220) heartRate.bpm else 0 // Fallback to 60 if invalid
        val timestamp = heartRate.timestamp
        val isAlert = heartRate.isAlert
        val alertMsg = heartRate.alertMsg.take(20) // Truncate to 20 bytes to fit in a single BLE packet

        // Serialize HeartRateData into a string for encryption
        val dataString = "BPM:$bpm,TS:$timestamp,Alert:$isAlert,Msg:$alertMsg"
        val encryptedData = encryptData(dataString)

        // Set encrypted data to characteristic
        characteristic.value = encryptedData.toByteArray(Charsets.UTF_8)
        Log.e(
            TAG,
            "Set encrypted characteristic value: $encryptedData at ${System.currentTimeMillis()}"
        )

        // Use BluetoothManager to get connected devices
        val connectedDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
        if (connectedDevices.isEmpty()) {
            Log.e(
                TAG,
                "No connected devices to notify at ${System.currentTimeMillis()}"
            )
            return
        }

        connectedDevices.forEach { device ->
            gattServer?.notifyCharacteristicChanged(device, characteristic, false)
            Log.e(
                TAG,
                "Notifying encrypted heart rate data to ${device.address} at ${System.currentTimeMillis()}"
            )
        }
    }
}