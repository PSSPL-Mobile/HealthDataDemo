package com.psspl.healthdatademo.data.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.psspl.healthdatademo.data.model.HeartRateData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/***
 * Name : BleClientManager.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 28 Jun 2025
 * Desc : Manages BLE client operations for heart rate data collection on Wear OS.
 * */
class BleClientManager @Inject constructor(
    private val context: Context
) {
    var TAG = "BleClientManager" // Tag for logging BLE-related operations

    private val serviceUuid =
        UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB") // Heart Rate Service UUID
    private val characteristicUuid =
        UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB") // Heart Rate Measurement Characteristic UUID
    private val descriptorUuid =
        UUID.fromString("00002902-0000-1000-8000-00805F9B34FB") // Client Characteristic Configuration Descriptor UUID
    private val _heartRateData =
        MutableStateFlow<HeartRateData?>(
            HeartRateData(
                heartRate = 0,
                timestamp = 0,
                isAlert = false,
                alertMsg = ""
            )
        ) // Mutable state flow for heart rate data updates
    val heartRateData: StateFlow<HeartRateData?> =
        _heartRateData // Read-only flow for observing heart rate data
    private val _connectionState = MutableStateFlow(false) // Tracks connection status
    val connectionState: StateFlow<Boolean> =
        _connectionState // Read-only flow for connection state
    private val _discoveredDevices =
        MutableStateFlow<List<BluetoothDevice>>(emptyList()) // List of discovered BLE devices
    val discoveredDevices: StateFlow<List<BluetoothDevice>> =
        _discoveredDevices // Read-only flow for discovered devices
    private val bluetoothAdapter: BluetoothAdapter? =
        BluetoothAdapter.getDefaultAdapter() // Default Bluetooth adapter
    private var bluetoothGatt: BluetoothGatt? = null // GATT client for BLE communication
    private var scanCallback: ScanCallback? = null // Callback for BLE device scanning
    private var keepAliveJob: Job? = null // Job for periodic keep-alive operations

    /**
     * Initiates scanning for BLE devices with the specified heart rate service UUID.
     * Requires BLUETOOTH_SCAN permission.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun scanForDevices() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth is not enabled")
            return
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "BLUETOOTH_SCAN permission not granted")
            return
        }

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(android.os.ParcelUuid(serviceUuid))
                .build()
        ) // Filter for devices advertising the heart rate service
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build() // High-performance scan settings

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.device?.let { device ->
                    val currentDevices = _discoveredDevices.value.toMutableList()
                    if (!currentDevices.any { it.address == device.address }) {
                        currentDevices.add(device)
                        _discoveredDevices.value = currentDevices
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted")
                            return
                        }
                        Log.e(
                            TAG,
                            "Found device: ${device.name ?: "Unknown Device"}, address: ${device.address}"
                        )
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "Scan failed with error: $errorCode") // Log scan failure
            }
        }

        bluetoothAdapter.bluetoothLeScanner?.startScan(filters, settings, scanCallback)
        Log.e(TAG, "Started scanning for devices at ${System.currentTimeMillis()}")
    }

    /**
     * Stops the ongoing BLE device scan.
     * Requires BLUETOOTH_SCAN permission.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScanning() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "BLUETOOTH_SCAN permission not granted")
            return
        }
        scanCallback?.let {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(it)
            scanCallback = null
            Log.e(TAG, "Scanning stopped at ${System.currentTimeMillis()}")
        }
    }

    /**
     * Establishes a connection to the specified BLE device.
     * Requires BLUETOOTH_SCAN and BLUETOOTH_CONNECT permissions.
     * @param device The BluetoothDevice to connect to.
     */
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    fun connectToDevice(device: BluetoothDevice?) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth is not enabled")
            _connectionState.value = false
            return
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Required permissions not granted")
            return
        }

        bluetoothGatt = null
        bluetoothGatt = device?.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(
                gatt: BluetoothGatt?,
                status: Int,
                newState: Int
            ) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    _connectionState.value = true
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.e(TAG, "BLUETOOTH_CONNECT permission not granted")
                        return
                    }
                    Log.e(
                        TAG,
                        "Connected to device: ${gatt?.device?.address} at ${System.currentTimeMillis()}"
                    )
                    // Request a higher MTU for better data transfer
                    gatt?.requestMtu(100)
                    gatt?.discoverServices()
                    // Start keep-alive to maintain connection
                    startKeepAlive(gatt)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    _connectionState.value = false
                    _heartRateData.value = null
                    stopKeepAlive()
                    Log.e(TAG, "Disconnected from device: ${gatt?.device?.address} with status: $status at ${System.currentTimeMillis()}")
                    // Attempt to reconnect on common errors
                    if (status == 133 || status == 8) {
                        Log.e(TAG, "Attempting to reconnect to ${gatt?.device?.address}")
                        gatt?.device?.connectGatt(context, false, this)
                    }
                }
            }

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.e(TAG, "MTU changed to $mtu at ${System.currentTimeMillis()}")
                    gatt?.discoverServices() // Proceed with service discovery
                } else {
                    Log.e(
                        TAG,
                        "MTU change failed with status: $status at ${System.currentTimeMillis()}"
                    )
                    gatt?.discoverServices() // Fallback to discover services
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt?.getService(serviceUuid)
                    val characteristic = service?.getCharacteristic(characteristicUuid)
                    characteristic?.let {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted")
                            return
                        }
                        // Enable notifications for real-time data
                        gatt.setCharacteristicNotification(it, true)
                        val descriptor = it.getDescriptor(descriptorUuid)
                        descriptor?.let { desc ->
                            desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            if (!gatt.writeDescriptor(desc)) {
                                Log.e(TAG, "Failed to write descriptor for notifications")
                            }
                        } ?: run {
                            Log.e(
                                TAG,
                                "Descriptor not found for characteristic ${characteristic.uuid}"
                            )
                        }
                        // Read initial characteristic value
                        if (!gatt.readCharacteristic(it)) {
                            Log.e(TAG, "Failed to read characteristic ${characteristic.uuid}")
                        }
                    } ?: run {
                        Log.e(TAG, "Heart rate characteristic not found")
                    }
                } else {
                    Log.e(
                        TAG,
                        "Service discovery failed with status: $status at ${System.currentTimeMillis()}"
                    )
                }
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt?,
                descriptor: BluetoothGattDescriptor?,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.e(TAG, "Descriptor write successful at ${System.currentTimeMillis()}")
                } else {
                    Log.e(
                        TAG,
                        "Descriptor write failed with status: $status at ${System.currentTimeMillis()}"
                    )
                    // Disconnect if notifications can't be enabled
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    gatt?.disconnect()
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                characteristic?.let {
                    if (it.uuid == characteristicUuid) {
                        val value = it.value
                        if (value == null || value.isEmpty()) {
                            Log.e(
                                TAG,
                                "Heart rate characteristic value is null or empty at ${System.currentTimeMillis()}"
                            )
                            return
                        }

                        // Validate minimum length for heart rate data format
                        if (value.size < 13) {
                            Log.e(
                                TAG,
                                "Heart rate value too short for new format: ${value.size} bytes at ${System.currentTimeMillis()}"
                            )
                            return
                        }

                        val flags = value[0].toInt()
                        val isBpmUint16 = (flags and 0x01) == 1 // Bit 0: 0 = UINT8, 1 = UINT16

                        if (!isBpmUint16) {
                            Log.e(
                                TAG,
                                "Unexpected flags: $flags, expected UINT16 format at ${System.currentTimeMillis()}"
                            )
                            return
                        }

                        // Parse BPM (2 bytes, little-endian)
                        val bpm = (value[1].toInt() and 0xFF) or ((value[2].toInt() and 0xFF) shl 8)

                        // Parse Timestamp (8 bytes, little-endian)
                        var timestamp = 0L
                        for (i in 0..7) {
                            timestamp = timestamp or ((value[3 + i].toLong() and 0xFF) shl (i * 8))
                        }

                        val isAlert = value[11] == 0x01.toByte()
                        val alertMsgLength = (value[12].toInt() and 0xFF)

                        if (value.size < 13 + alertMsgLength) {
                            Log.e(
                                TAG,
                                "Heart rate value too short for alertMsg: expected ${13 + alertMsgLength} bytes, got ${value.size} at ${System.currentTimeMillis()}"
                            )
                            return
                        }

                        val alertMsgBytes = value.copyOfRange(13, 13 + alertMsgLength)
                        val alertMsg = alertMsgBytes.toString(Charsets.UTF_8)

                        _heartRateData.value = HeartRateData(bpm, timestamp, isAlert, alertMsg)
                        Log.e(
                            TAG,
                            "Heart rate updated: BPM=$bpm, Timestamp=$timestamp, isAlert=$isAlert, AlertMsg=$alertMsg at ${System.currentTimeMillis()}"
                        )
                    }
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e(
                        TAG,
                        "Characteristic read failed with status: $status at ${System.currentTimeMillis()}"
                    )
                    return
                }
                characteristic?.let {
                    if (it.uuid == characteristicUuid) {
                        val value = it.value
                        if (value == null || value.isEmpty()) {
                            Log.e(
                                TAG,
                                "Heart rate characteristic value is null or empty at ${System.currentTimeMillis()}"
                            )
                            return
                        }

                        if (value.size < 13) {
                            Log.e(
                                TAG,
                                "Heart rate value too short for new format: ${value.size} bytes at ${System.currentTimeMillis()}"
                            )
                            return
                        }

                        val flags = value[0].toInt()
                        val isBpmUint16 = (flags and 0x01) == 1

                        if (!isBpmUint16) {
                            Log.e(
                                TAG,
                                "Unexpected flags: $flags, expected UINT16 format at ${System.currentTimeMillis()}"
                            )
                            return
                        }

                        val bpm = (value[1].toInt() and 0xFF) or ((value[2].toInt() and 0xFF) shl 8)
                        var timestamp = 0L
                        for (i in 0..7) {
                            timestamp = timestamp or ((value[3 + i].toLong() and 0xFF) shl (i * 8))
                        }

                        val isAlert = value[11] == 0x01.toByte()
                        val alertMsgLength = (value[12].toInt() and 0xFF)

                        if (value.size < 13 + alertMsgLength) {
                            Log.e(
                                TAG,
                                "Heart rate value too short for alertMsg: expected ${13 + alertMsgLength} bytes, got ${value.size} at ${System.currentTimeMillis()}"
                            )
                            return
                        }

                        val alertMsgBytes = value.copyOfRange(13, 13 + alertMsgLength)
                        val alertMsg = alertMsgBytes.toString(Charsets.UTF_8)

                        if (bpm !in 30..220) {
                            Log.e(
                                TAG,
                                "Invalid BPM value: $bpm (out of range 30–220) at ${System.currentTimeMillis()}"
                            )
                            return
                        }

                        _heartRateData.value = HeartRateData(bpm, timestamp, isAlert, alertMsg)
                        Log.e(
                            TAG,
                            "Heart rate read: BPM=$bpm, Timestamp=$timestamp, isAlert=$isAlert, AlertMsg=$alertMsg at ${System.currentTimeMillis()}"
                        )
                    }
                }
            }

        }, BluetoothDevice.TRANSPORT_LE) // Use Low Energy transport

        when (bluetoothGatt?.device?.bondState) {
            BluetoothDevice.BOND_BONDED -> {
                Log.e("Pair", "Device ${bluetoothGatt?.device?.address} is already bonded")
            }

            BluetoothDevice.BOND_NONE -> {
                Log.e(
                    "Pair",
                    "Device ${bluetoothGatt?.device?.address} is not bonded, initiating pairing"
                )
                bluetoothGatt?.device?.createBond()
            }

            BluetoothDevice.BOND_BONDING -> {
                Log.e("Pair", "Device ${bluetoothGatt?.device?.address} is currently bonding")
            }
        }

        Log.e(
            TAG,
            "Initiated connection to device: ${bluetoothGatt?.device?.address} at ${System.currentTimeMillis()}"
        )
    }

    /**
     * Disconnects from the current BLE device, with an option to reconnect.
     * Requires BLUETOOTH_CONNECT permission.
     * @param reconnect Whether to attempt reconnection after disconnecting.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect(reconnect: Boolean = false) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted")
            return
        }
        stopKeepAlive()
        bluetoothGatt?.let {
            it.disconnect()
            Log.e(TAG, "Disconnected from device at ${System.currentTimeMillis()}")
            CoroutineScope(Dispatchers.Main).launch {
                it.close()
                if (reconnect) {
                    delay(3000)
                    connectToDevice(bluetoothGatt?.device)
                } else {
                    bluetoothGatt = null
                    _connectionState.value = false
                    _heartRateData.value = null
                }
            }
        }
    }

    /**
     * Starts a keep-alive mechanism to maintain the BLE connection.
     * Reads the heart rate characteristic periodically.
     * @param gatt The BluetoothGatt instance to monitor.
     */
    private fun startKeepAlive(gatt: BluetoothGatt?) {
        stopKeepAlive() // Cancel any existing keep-alive job
        keepAliveJob = CoroutineScope(Dispatchers.Main).launch {
            while (_connectionState.value) {
                delay(30000) // Every 30 seconds
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(TAG, "BLUETOOTH_CONNECT permission not granted")
                    return@launch
                }
                val service = gatt?.getService(serviceUuid)
                val characteristic = service?.getCharacteristic(characteristicUuid)
                characteristic?.let {
                    if (!gatt.readCharacteristic(it)) {
                        Log.e(
                            TAG,
                            "Failed to read characteristic for keep-alive at ${System.currentTimeMillis()}"
                        )
                        disconnect(true)
                    }
                } ?: run {
                    Log.e(
                        TAG,
                        "Heart rate characteristic not found for keep-alive at ${System.currentTimeMillis()}"
                    )
                }
            }
        }
    }

    /**
     * Stops the keep-alive mechanism.
     */
    private fun stopKeepAlive() {
        keepAliveJob?.cancel()
        keepAliveJob = null
    }
}