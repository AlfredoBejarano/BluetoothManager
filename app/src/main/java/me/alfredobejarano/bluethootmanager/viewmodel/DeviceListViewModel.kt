package me.alfredobejarano.bluethootmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.alfredobejarano.bluethootmanager.data.DeviceRepository
import me.alfredobejarano.bluethootmanager.utilities.runOnIOThread
import javax.inject.Inject

/**
 *
 * ViewModel class that handles UI interactions for
 * a fragment displaying the list of cloud stored devices.
 *
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 18:22
 * @version 1.0
 **/
class DeviceListViewModel
@Inject constructor(private val repo: DeviceRepository) : ViewModel() {
    /**
     * Property that provides observation to a UI
     * controller for the list of devices.
     */
    var devices = repo.devices

    /**
     * Retrieves the devices from the repository
     * and reports them to a UI controller using
     * the [devices] property.
     */
    fun fetchDevices() = runOnIOThread {
        repo.fetchDevices()
    }

    /**
     * Factory class that tells to a ViewModelProvider
     * how to build a [DeviceListViewModel] instance.
     */
    class Factory
    @Inject constructor(private val repo: DeviceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            DeviceListViewModel(repo) as T
    }
}