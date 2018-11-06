package me.alfredobejarano.bluethootmanager.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 *
 * Repository class that serves as the single source of truth for the ViewModel classes
 * for retrieving a list of devices.
 *
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 10:29
 * @version 1.0
 **/
class DeviceRepository
@Inject constructor(
    private val ctx: Context,
    private val dao: DeviceDao,
    private val service: DeviceService,
    private val adapter: BluetoothAdapter,
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        /**
         * Constant that describes in how many minute in the future a cloud cache will expire.
         */
        private const val CACHE_MINUTE_DURATION = 60
        /**
         * Constant that defines the key of a cache stored in the SharedPreferences.
         */
        private const val CACHE_EXPIRATION_KEY = "cacheInMillis"
        /**
         * Constant that describes the format of the created_at timestamp for a device.
         */
        private const val TIMESTAMP_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.S'z'"
    }

    /**
     * Stores a device in the server.
     *
     * **Note** This function **IS NOT THREAD SAFE**, it has to be executed in a
     * worker thread to prevent network in UI thread exceptions.
     */
    fun storeDeviceInCloud(device: Device) = service.addDevice(device)

    /**
     * Reads a list of devices from the server, if a cache is valid, it gets fetched
     * from the local storage, if not, it gets fetched from the server.
     *
     * **Note*** this function **IS NOT THREAD SAFE**, execute it in a worker thread to
     * prevent ANR errors, Database in UI thread exceptions and Network in UI exceptions.
     */
    fun fetchDevices() = if (isCacheValid()) {
        dao.read()
    } else {
        readDevicesFromServer().also {
            generateCacheTimeStamp()
        }
    }

    /**
     * Uses the bluetooth adapter to find the bonded devices to this device.
     *
     * **Note** This function **IS NOT THREAD SAFE**, it has to be executed in a
     * worker thread to prevent ANR errors.
     * @return [MutableLiveData] object to provide observation for the bonded devices.
     */
    fun findBondedDevices(): MutableLiveData<List<Device>> {
        // MutableLiveData that provides observation for the devices that are found.
        val devices = MutableLiveData<List<Device>>()
        // Find the bonded devices with this device.
        // List of bonded devices that are found..
        val deviceList = mutableListOf<Device>()
        // Get the bounded devices from the adapter.
        val bondedDevices = adapter.bondedDevices
        // Iterate through every single bonded device.
        bondedDevices?.forEach {
            // Create a callback object for retrieving every single device RSSI to read its strength.
            val gattCallback = object : BluetoothGattCallback() {
                /**
                 * This function gets triggered when a Remote RSSI gets read.
                 */
                override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
                    // If the gathering of the RSSI succeeds, read the strength, if not, set it as 0.
                    val strength = if (status == BluetoothGatt.GATT_SUCCESS) {
                        rssi
                    } else {
                        0
                    }
                    // Add the device to the list.
                    deviceList.add(
                        Device(
                            name = it.name,
                            strength = strength,
                            address = it.address,
                            createdAt = getCurrentTimeStamp()
                        )
                    )
                    // Report the new list value to the LiveData object.
                    devices.postValue(deviceList)
                }
            }
            // Read the device RSSI.
            it.connectGatt(ctx, true, gattCallback)
        }
        // Return the LiveData object for observation.
        return devices
    }

    /**
     * Generates a new Cache timestamp and stores it in the app shared preferences.
     */
    private fun generateCacheTimeStamp() {
        // Retrieve the calendar instance.
        val calendar = Calendar.getInstance()
        // Add the cache expiration time to the calendar instance.
        calendar.add(Calendar.MINUTE, CACHE_MINUTE_DURATION)
        // Edit the SharedPreferences.
        with(sharedPreferences.edit()) {
            // Put the calendar timestamp as a Date class milliseconds.
            this.putLong(CACHE_EXPIRATION_KEY, calendar.time.time)
            // Apply the changes.
            this.apply()
        }
    }

    /**
     * Reads all the stored devices from the server.
     *
     * **Note** This function **IS NOT THREAD SAFE**, it has to be executed in a
     * worker thread to prevent network in UI thread exceptions.
     */
    private fun readDevicesFromServer() = service.fetchDevices()

    /**
     * Checks if the stored cache has not expired.
     */
    private fun isCacheValid() = if (sharedPreferences.contains(CACHE_EXPIRATION_KEY)) {
        // Retrieve the current time in milliseconds.
        val currentTime = Date().time
        // Get the cache time.
        val cacheTime = sharedPreferences.getLong(CACHE_EXPIRATION_KEY, currentTime)
        // If the current time is equals or larger than the cache time, it is not valid.
        currentTime >= cacheTime
    } else {
        // The cache is not valid, return false.
        false
    }

    /**
     * Retrieves the system date and formats it.
     */
    private fun getCurrentTimeStamp(): String = synchronized(this) {
        // Synchronize the execution of this function.
        // Create a SimpleDateFormat object.
        val formatter = SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault())
        try {
            // Format the current system date.
            formatter.format(Date())
        } catch (t: Throwable) {
            // If the system date fails, return an empty string.
            ""
        }
    }
}