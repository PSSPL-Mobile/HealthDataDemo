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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import javax.inject.Inject

class BleServerManager @Inject constructor(val context: Context) {

    var TAG = "BleServerManager"

    private val serviceUuid = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")
    private val characteristicUuid = UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB")

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState

    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var gattServer: BluetoothGattServer? = null
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.e(
                TAG,
                "Advertising started successfully at ${System.currentTimeMillis()}"
            )
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(
                TAG,
                "Advertising failed with error: $errorCode at ${System.currentTimeMillis()}"
            )
        }
    }

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
        val alertMsg =
            heartRate.alertMsg.take(20) // Truncate to 20 bytes to fit in a single BLE packet

        // Serialize HeartRateData into a byte array
        // Format: [flags (1 byte), bpm (2 bytes), timestamp (8 bytes), isAlert (1 byte), alertMsgLength (1 byte), alertMsg (variable)]
        val alertMsgBytes = alertMsg.toByteArray(Charsets.UTF_8) // Convert string to UTF-8 bytes
        val alertMsgLength =
            alertMsgBytes.size.coerceAtMost(255).toByte() // Ensure length fits in 1 byte
        val data = ByteArray(13 + alertMsgLength) // 1 + 2 + 8 + 1 + 1 + alertMsgLength

        // Flags: Indicate UINT16 BPM
        data[0] = 0x01 // Bit 0 = 1 for UINT16 BPM
        // BPM (2 bytes, little-endian)
        data[1] = (bpm and 0xFF).toByte() // Lower byte
        data[2] = ((bpm shr 8) and 0xFF).toByte() // Upper byte
        // Timestamp (8 bytes, little-endian)
        for (i in 0..7) {
            data[3 + i] = ((timestamp shr (i * 8)) and 0xFF).toByte()
        }
        // isAlert (1 byte)
        data[11] = if (isAlert) 0x01 else 0x00
        // alertMsg length (1 byte)
        data[12] = alertMsgLength
        // alertMsg (variable length)
        System.arraycopy(alertMsgBytes, 0, data, 13, alertMsgLength.toInt())

        characteristic.value = data
        Log.e(
            TAG,
            "Set characteristic value: ${
                data.joinToString {
                    it.toInt().toString(16).padStart(2, '0')
                }
            } at ${System.currentTimeMillis()}"
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
                "Notifying heart rate data: BPM=$bpm, Timestamp=$timestamp, isAlert=$isAlert, AlertMsg=$alertMsg to ${device.address} at ${System.currentTimeMillis()}"
            )
        }
    }

}