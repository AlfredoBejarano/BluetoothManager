package me.alfredobejarano.bluethootmanager.data

import android.bluetooth.BluetoothAdapter
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 *
 * Repository class that serves as the single source of truth for the ViewModel classes
 * for retrieving a list of bondedDevices.
 *
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 10:29
 * @version 1.0
 **/
class DeviceRepository
@Inject constructor(
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
        internal const val CACHE_EXPIRATION_KEY = "cacheInMillis"
        /**
         * Constant that describes the format of the created_at timestamp for a device.
         */
        const val TIMESTAMP_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.S'z'"
    }

    /**
     * Stores a device in the server.
     *
     * **Note** This function **IS NOT THREAD SAFE**, it has to be executed in a
     * worker thread to prevent network in UI thread exceptions.
     */
    fun storeDevice(device: Device): LiveData<Device> {
        val result = MutableLiveData<Device>()
        // Insert the device locally.
        dao.insertOrUpdate(device)
        // Attempt to store it in the cloud.
        val call = service.addDevice(device)
        // If the call hasn't been executed yet, enqueue it
        if (!call.isExecuted) {
            call.enqueue(object : Callback<Device> {
                /**
                 * If the device couldn't be saved, return null.
                 */
                override fun onFailure(call: Call<Device>, t: Throwable?) = result.postValue(null)

                /**
                 * Update the device sync state to true and update it in the cache.
                 */
                override fun onResponse(call: Call<Device>, response: Response<Device>?) {
                    if (response?.isSuccessful == true) {
                        // Retrieve the added device from the response.
                        val addedDevice = response.body() ?: device
                        // If the device got stored remotely successfully, report it as syncState.
                        addedDevice.syncState = true
                        // Update the device locally.
                        dao.insertOrUpdate(addedDevice)
                        // Report the result
                        result.postValue(addedDevice)
                    } else {
                        onFailure(call, null)
                    }
                }
            })
        }
        return result
    }

    /**
     * Reads a list of bondedDevices from the server, if a cache is valid, it gets fetched
     * from the local storage, if not, it gets fetched from the server.
     *
     * **Note*** this function **IS NOT THREAD SAFE**, execute it in a worker thread to
     * prevent ANR errors, Database in UI thread exceptions and Network in UI exceptions.
     */
    fun fetchDevices() = if (isCacheValid()) {
        dao.read() // Read the cached bondedDevices
    } else {
        refreshCache() // Refresh the cache.
    }

    /**
     * Uses the bluetooth adapter to find the bonded bondedDevices to this device.
     *
     * **Note** This function **IS NOT THREAD SAFE**, it has to be executed in a
     * worker thread to prevent ANR errors.
     * @return [MutableLiveData] object to provide observation for the bonded bondedDevices.
     */
    fun findBondedDevices(): List<Device> {
        // Find the bonded bondedDevices with this device.
        // List of bonded bondedDevices that are found..
        val deviceList = mutableListOf<Device>()
        // Get the bounded bondedDevices from the adapter.
        val bondedDevices = adapter.bondedDevices
        // Iterate through every single bonded device.
        bondedDevices?.forEach { device ->
            device?.let {
                deviceList.add(
                    Device(
                        name = it.name,
                        strength = -1,
                        address = it.address,
                        syncState = true,
                        createdAt = getCurrentTimeStamp()
                    )
                )
            }
        }
        // Return the LiveData object for observation.
        return deviceList
    }

    /**
     * Invalidates the local cache and retrieves the list of bondedDevices.
     */
    fun refreshCache(): LiveData<List<Device>> {
        invalidateCache() // Invalidate the local cache.
        return readDevicesFromServer()
    }

    /**
     * Retrieves all the un synced bondedDevices from the
     * local storage and reports them to the cloud.
     *
     * **Note** This function **IS NOT THREAD SAFE**, so prevent using it as is,
     * execute it in a worker thread to prevent crashes and exceptions.
     */
    fun synchronizeDevices() = dao.readUnSync().forEach {
        storeDevice(it)
    }

    /**
     * Clears the database table and sets the timestamp to -1, so the next time
     * the cache gets verified it returns as invalid.
     *
     * @see isCacheValid
     */
    private fun invalidateCache() = with(sharedPreferences.edit()) {
        this?.putLong(CACHE_EXPIRATION_KEY, -1) // Set the cache timestamp as -1.
        this?.apply() // Apply the changes.
    }.also {
        dao.deleteAll() // Clear the bondedDevices table.
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
            this?.putLong(CACHE_EXPIRATION_KEY, calendar.time.time)
            // Apply the changes.
            this?.apply()
        }
    }

    /**
     * Reads all the stored bondedDevices from the server.
     *
     * **Note** This function **IS NOT THREAD SAFE**, it has to be executed in a
     * worker thread to prevent network in UI thread exceptions.
     */
    private fun readDevicesFromServer(): MutableLiveData<List<Device>> {
        var results = MutableLiveData<List<Device>>()
        // Retrieve the call to perform the web service call.
        val call = service.fetchDevices()
        // If the call hasn't been executed yet, enqueue it
        call.enqueue(object : Callback<List<Device>> {
            /**
             * If the API call for devices is not successful, use the local cached devices.
             */
            override fun onFailure(call: Call<List<Device>>, t: Throwable?) {
                results = dao.read() as MutableLiveData<List<Device>>
            }

            /**
             * Report the found results to a LiveData object.
             */
            override fun onResponse(call: Call<List<Device>>, response: Response<List<Device>>?) {
                if (response?.isSuccessful == true) {
                    response.body()?.let {
                        results.postValue(it)
                        generateCacheTimeStamp()
                    } ?: run {
                        onFailure(call, null)
                    }
                } else {
                    onFailure(call, null)
                }
            }
        })
        return results
    }

    /**
     * Checks if the stored cache has not expired.
     */
    private fun isCacheValid() = if (sharedPreferences.contains(CACHE_EXPIRATION_KEY)) {
        // Retrieve the current time in milliseconds.
        val currentTime = Date().time
        // Get the cache time.
        val cacheTime = sharedPreferences.getLong(CACHE_EXPIRATION_KEY, -1)
        // If the current time is equals or larger than the cache time, it is not valid.
        cacheTime > currentTime
    } else {
        // The cache is not valid, return false.
        false
    }

    /**
     * Retrieves the system date and formats it.
     */
    fun getCurrentTimeStamp(): String = synchronized(this) {
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