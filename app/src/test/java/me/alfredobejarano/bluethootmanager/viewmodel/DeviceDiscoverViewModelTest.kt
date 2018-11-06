package me.alfredobejarano.bluethootmanager.viewmodel

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import me.alfredobejarano.bluethootmanager.data.Device
import me.alfredobejarano.bluethootmanager.data.DeviceRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

/**
 * Test suite for the [DeviceDiscoverViewModel] class.
 *
 * @author Alfredo Bejarano
 * @version 1.0
 * @since November 06, 2018 - 15:31
 */
@RunWith(MockitoJUnitRunner::class)
class DeviceDiscoverViewModelTest {
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockRepository: DeviceRepository

    private lateinit var testViewModel: DeviceDiscoverViewModel

    @Before
    fun setup() {
        testViewModel = DeviceDiscoverViewModel(mockRepository)
    }

    /**
     * Tests that the flow of reading bonded devices works correctly.
     */
    @Test
    fun readBondedDevices() {
        // Mocked list of devices.
        val mockResult = listOf<Device>()
        // Mocked LiveData object to test observation.
        val mockLiveData = mock(MutableLiveData::class.java) as MutableLiveData<List<Device>>
        // Mock observer, to test changes from the repository.
        val mockObserver = mock(Observer::class.java) as Observer<List<Device>>
        // When the mocked LiveData value gets requested, return the mocked result.
        `when`(mockLiveData.value)
            .thenReturn(mockResult)
        // When the mock repository gets a task of find the bounded devices, return the mock LiveData
        `when`(mockRepository.findBondedDevices())
            .thenReturn(mockLiveData)
        // Observe the devices LiveData object.
        testViewModel.devices.observeForever(mockObserver)
        // Read the bonded devices.
        testViewModel.readBondedDevices()
        // Verify that the ViewModel detected changes.
        verify(mockObserver, timeout(2000))
            .onChanged(any())
    }

    @Test
    fun reportFoundDevice() {
        // Mock device found by the intent.
        val mockDevice = mock(BluetoothDevice::class.java)
        // Mock observer, for listening to the device result.
        val mockObserver = mock(Observer::class.java) as Observer<List<Device>>
        // mock intent, "received" when discovering the mock device.
        val mockIntent = mock(Intent::class.java)
        // When requesting the device from the mock intent, return the mock device.
        `when`(mockIntent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
            .thenReturn(mockDevice)
        // When requesting the mock device name, return a string.
        `when`(mockDevice.name)
            .thenReturn("")
        // When requesting the mock device address, return a String.
        `when`(mockDevice.address)
            .thenReturn("")
        // When requesting the mock device strength from the extras, return 0.
        `when`(mockIntent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE))
            .thenReturn(0)
        // When requesting the current system timestamp, return a string.
        `when`(mockRepository.getCurrentTimeStamp())
            .thenReturn("")
        // Observe the devices value using the mock observer.
        testViewModel.devices.observeForever(mockObserver)
        // Report a found device using the mock intent.
        testViewModel.reportFoundDevice(mockIntent)
        // Verify that the device lists changed within 2 seconds.
        verify(mockObserver, timeout(2000))
            .onChanged(any())
    }

    /**
     * Asserts that the flow of saving a device works correctly.
     */
    @Test
    fun saveDevice() {
        // Mock LiveData object as a result of a mock repository call.
        val mockResult = mock(LiveData::class.java) as LiveData<Device>
        // Mock observer that will assert any change report from it.
        val mockObserver = mock(Observer::class.java) as Observer<Device>
        // Mock device object.
        val mockDevice = mock(Device::class.java)
        // When the result gets its value requested, return the mocked device.
        `when`(mockResult.value)
            .thenReturn(mockDevice)
        // When the mock repository gets requested for storing a device, return the mock result.
        `when`(mockRepository.storeDevice(mockDevice))
            .thenReturn(mockResult)
        // Observe the saved device.
        testViewModel.savedDevice.observeForever(mockObserver)
        // Save the mock device.
        testViewModel.saveDevice(mockDevice)
        // Verify that the observer received a change within 2 seconds.
        verify(mockObserver, timeout(2000)).onChanged(any())
    }
}