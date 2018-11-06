package me.alfredobejarano.bluethootmanager.viewmodel

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.alfredobejarano.bluethootmanager.data.Device
import me.alfredobejarano.bluethootmanager.data.DeviceRepository
import me.alfredobejarano.bluethootmanager.utilities.runOnIOThread
import javax.inject.Inject

/**
 *
 * ViewModel class that handles UI events from the DeviceDiscoverFragment.
 *
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 12:08
 * @version 1.0
 **/
class DeviceDiscoverViewModel
@Inject constructor(private val repo: DeviceRepository) : ViewModel() {
    var devices = MutableLiveData<List<Device>>()

    /**
     * Calls the repository to read all the bonded bluetooth devices
     * to this device and provides observation to the UI via
     * the [devices] property.
     */
    fun readBondedDevices() = runOnIOThread {
        devices = repo.findBondedDevices()
    }

    /**
     * Receives an Intent to read the device stored in
     * it if a bluetooth device was discovered, observation
     * of the findings is provided via the [devices] property.
     * @param data The intent received from the discovery action.
     */
    fun reportFoundDevice(data: Intent) = runOnIOThread {
        // Retrieve the list of devices from the LiveData object as a MutableList.
        val deviceList = devices.value as MutableList?
        // Get the found device from the data.
        val foundDevice = data.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        // If the found device is not null, proceed to add it to the list.
        foundDevice?.let {
            // Read the strength of the device from the data.
            val strength = data.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
            // Create the device object.
            val device = Device(
                name = it.name,
                address = it.address,
                synchronized = false,
                strength = strength.toInt(),
                createdAt = repo.getCurrentTimeStamp()
            )
            // Add it to the mutable list.
            deviceList?.add(device)
            // Report the value changes to the LiveData object.
            devices.postValue(deviceList)
        }
    }

    /**
     * Tells the repository to store a device, the
     * repository class will handle the storing synchronization.
     * @param device The device being stored.
     */
    fun saveDevice(device: Device) = runOnIOThread {
        repo.storeDevice(device)
    }

    /**
     * Factory class for the [DeviceDiscoverViewModel] class
     * that tells a ViewModelProvider how to create an instance
     * of said object.
     */
    class Factory @Inject constructor(
        private val repo: DeviceRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            DeviceDiscoverViewModel(repo) as T
    }
}