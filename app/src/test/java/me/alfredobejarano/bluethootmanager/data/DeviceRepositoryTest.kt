package me.alfredobejarano.bluethootmanager.data

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import me.alfredobejarano.bluethootmanager.data.DeviceRepository.Companion.CACHE_EXPIRATION_KEY
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Test suite for the [DeviceRepositoryTest] class.
 *
 * @author Alfredo Bejarano
 * @version 1.0
 * @since November 06, 2018 - 13:39
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class DeviceRepositoryTest {
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @Mock
    private lateinit var mockDeviceDao: DeviceDao
    @Mock
    private lateinit var mockAdapter: BluetoothAdapter
    @Mock
    private lateinit var mockDeviceService: DeviceService
    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    private lateinit var testRepository: DeviceRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        testRepository = DeviceRepository(
            ctx = RuntimeEnvironment.systemContext,
            dao = mockDeviceDao,
            adapter = mockAdapter,
            service = mockDeviceService,
            sharedPreferences = mockSharedPreferences
        )
    }

    /**
     * Asserts that storing a device locally and then
     * when storing it remotely successfully and updating
     * the local reference works correctly.
     */
    @Test
    fun storeDevice_locallyAndRemotely() {
        // Create a mock device
        val mockDevice = mock(Device::class.java)
        // Create a mock response for the service
        val mockServiceResponse = mock(LiveData::class.java) as LiveData<Device>
        // When the mock service response gets its value requested, return a mock device.
        `when`(mockServiceResponse.value)
            .thenReturn(mockDevice)
        // When the mock device service gets a device added, return the mock service response.
        `when`(mockDeviceService.addDevice(mockDevice))
            .thenReturn(mockServiceResponse)
        // Store the mock device.
        testRepository.storeDevice(mockDevice)
        // Verify that the dao inserted the device and then it got updated, as it got synchronized.
        verify(mockDeviceDao, times(2)).insertOrUpdate(mockDevice)
        // Verify that the device was added.
        verify(mockDeviceService).addDevice(mockDevice)
    }

    /**
     * Asserts that storing a device locally only, (when
     * storing the device remotely fails), works correctly.
     */
    @Test
    fun storeDevice_locallyOnly() {
        // Create a mock device
        val mockDevice = mock(Device::class.java)
        // Create a mock response for the service
        val mockServiceResponse = mock(LiveData::class.java) as LiveData<Device>
        // When the mock service response gets its value requested, return null, this means the remote saving failed.
        `when`(mockServiceResponse.value)
            .thenReturn(null)
        // When the mock device service gets a device added, return the mock service response.
        `when`(mockDeviceService.addDevice(mockDevice))
            .thenReturn(mockServiceResponse)
        // Store the mock device.
        testRepository.storeDevice(mockDevice)
        // Verify that the dao inserted the device and didn't updated it, as the remote saving failed.
        verify(mockDeviceDao, times(1)).insertOrUpdate(mockDevice)
        // Verify that the device was added.
        verify(mockDeviceService).addDevice(mockDevice)
    }

    /**
     * Asserts that fetching devices when the cache
     * is valid works correctly.
     */
    @Test
    fun fetchDevices_cacheValid() {
        // Return true when asked if the mock preferences contains the cache timestamp.
        `when`(mockSharedPreferences.contains(CACHE_EXPIRATION_KEY))
            .thenReturn(true)
        // When asked for the timestamp, return the maximum long value possible.
        `when`(mockSharedPreferences.getLong(CACHE_EXPIRATION_KEY, -1))
            .thenReturn(Long.MAX_VALUE)
        // Fetch the devices.
        testRepository.fetchDevices()
        // Assert that the dao was called.
        verify(mockDeviceDao).read()
    }

    /**
     * Asserts that fetching devices when the cache
     * is not valid works correctly.
     */
    @Test
    fun fetchDevices_cacheInvalid() {
        // Return true when asked if the mock preferences contains the cache timestamp.
        `when`(mockSharedPreferences.contains(CACHE_EXPIRATION_KEY))
            .thenReturn(false)
        // Fetch the devices.
        testRepository.fetchDevices()
        // Assert that the preferences were edited, first invalidating the timestamp and then updating it.
        verify(mockSharedPreferences, times(2)).edit()
        // Assert that the dao was called.
        verify(mockDeviceDao).deleteAll()
        // verify that the remote repository was called
        verify(mockDeviceService).fetchDevices()
    }

    /**
     * Asserts that finding a set of bonded devices
     * works correctly.
     */
    @Test
    fun findBondedDevices_foundDevices() {
        val mockDevice = mock(BluetoothDevice::class.java)
        // When the devices get requested from the mock Bt adapter, return a mock set.
        `when`(mockAdapter.bondedDevices)
            .thenReturn(setOf<BluetoothDevice>(mockDevice))
        // Call the Test repository class.
        testRepository.findBondedDevices()
        // verify that a connection gathering request was made in the mock object.
        verify(mockDevice).connectGatt(any(), anyBoolean(), any())
    }

    /**
     * Asserts that the flow of finding any bonded devices
     * and the set of devices being empty, works correctly.
     */
    @Test
    fun findBondedDevices_NoDevicesFound() {
        // Create a mock observer
        val mockObserver = mock(Observer::class.java) as Observer<List<Device>>
        // When the devices get requested from the mock Bt adapter, return a mock set.
        `when`(mockAdapter.bondedDevices)
            .thenReturn(setOf<BluetoothDevice>())
        // Call the Test repository class.
        testRepository.findBondedDevices().observeForever(mockObserver)
        // verify that no changes were reported, as the observer never gets called.
        verify(mockObserver, never()).onChanged(any())
    }

    /**
     * Tests that the process of refreshing the local cache is performed correctly.
     */
    @Test
    fun refreshCache() {
        // Refresh the cache.
        testRepository.refreshCache()
        // The cache is updated two times, when invalidated and when fetched again.
        verify(mockSharedPreferences, times(2)).edit()
        // verify that the table got nuked!
        verify(mockDeviceDao).deleteAll()
    }

    /**
     * Tests that the flow of synchronizing local found devices is performed correctly.
     */
    @Test
    fun synchronizeDevices() {
        // Mock device that has not been sync.
        val mockDevice = Device("", 0, "", "", false)
        // Mock list containing the device that has not being sync.
        val mockDeviceList = mutableListOf(mockDevice)
        // Mock LiveData object containing the result of adding the object.
        val mockAddResult = mock(LiveData::class.java) as LiveData<Device>
        // When reading non sync devices, return the mocked list.
        `when`(mockDeviceDao.readUnSync())
            .thenReturn(mockDeviceList)
        // When requesting the mock LiveData value, return the mock device.
        `when`(mockAddResult.value)
            .thenReturn(mockDevice)
        // When Adding a device, return the mock result.
        `when`(mockDeviceService.addDevice(mockDevice))
            .thenReturn(mockAddResult)
        // Sync the devices.
        testRepository.synchronizeDevices()
        // Assert that the device sync status is now true.
        assert(mockDevice.synchronized)
    }

    /**
     * Retrieves the formatted timestamp and
     * asserts it is not empty (as it got formatted correctly):
     */
    @Test
    fun getCurrentTimeStamp() {
        val result = testRepository.getCurrentTimeStamp()
        assert(result.isNotEmpty())
    }
}