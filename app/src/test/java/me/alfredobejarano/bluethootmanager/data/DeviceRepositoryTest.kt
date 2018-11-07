package me.alfredobejarano.bluethootmanager.data

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.gson.Gson
import me.alfredobejarano.bluethootmanager.BuildConfig
import me.alfredobejarano.bluethootmanager.data.DeviceRepository.Companion.CACHE_EXPIRATION_KEY
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockWebServer: MockWebServer
    private lateinit var mockDeviceService: DeviceService
    private lateinit var testRepository: DeviceRepository

    @Before
    fun setup() {
        // Start the Mock web Server
        mockWebServer = MockWebServer()
        mockWebServer.start()
        // Create the mock device service implementation
        mockDeviceService = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build().create(DeviceService::class.java)
        // Init the mockito mocks
        MockitoAnnotations.initMocks(this)
        // Create the test repository
        testRepository = DeviceRepository(
            dao = mockDeviceDao,
            adapter = mockAdapter,
            service = mockDeviceService,
            sharedPreferences = mockSharedPreferences
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    /**
     * Asserts that storing a device locally and then
     * when storing it remotely successfully and updating
     * the local reference works correctly.
     */
    @Test
    fun storeDevice_locallyAndRemotely() {
        val mockObserver = Observer<Device> { assert(it?.syncState == true) }
        // Create a mock device object
        val mockDevice = Device()
        // Create a mock response for the web server.
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(Gson().toJson(mockDevice))
        // Enqueue the request
        mockWebServer.enqueue(mockResponse)
        // Store the device.
        testRepository.storeDevice(mockDevice).observeForever(mockObserver)
        // Wait 2 seconds for the request to perform.
        Thread.sleep(500)
    }

    /**
     * Asserts that storing a device locally only, (when
     * storing the device remotely fails), works correctly.
     */
    @Test
    fun storeDevice_locallyOnly() {
        // Create a mock device object
        val mockDevice = Device()
        // Create a mock response for the web server.
        val mockResponse = MockResponse()
            .setResponseCode(500)
            .setBody(Gson().toJson(Device()))
        // Enqueue the request
        mockWebServer.enqueue(mockResponse)
        // Store the device.
        testRepository.storeDevice(mockDevice)
        // Verify that the dao just stored one item in the lapse of 2 full seconds.
        verify(mockDeviceDao, timeout(2000)).insertOrUpdate(mockDevice)
    }

    /**
     * Asserts that fetching bondedDevices when the cache
     * is not valid works correctly.
     */
    @Test
    fun fetchDevices() {
        // Return true when asked if the mock preferences contains the cache timestamp.
        `when`(mockSharedPreferences.contains(CACHE_EXPIRATION_KEY))
            .thenReturn(false)
        // Fetch the bondedDevices.
        testRepository.fetchDevices()
    }

    /**
     * Asserts that finding a set of bonded bondedDevices
     * works correctly.
     */
    @Test
    fun findBondedDevices_foundDevices() {
        val mockDevice = mock(BluetoothDevice::class.java)
        // Return mock values for the mock devices.
        `when`(mockDevice.name).thenReturn("name")
        `when`(mockDevice.address).thenReturn("address")
        // When the bondedDevices get requested from the mock Bt adapter, return a mock set.
        `when`(mockAdapter.bondedDevices)
            .thenReturn(setOf<BluetoothDevice>(mockDevice))
        // Call the Test repository class.
        assert(testRepository.findBondedDevices().isNotEmpty())
    }

    /**
     * Asserts that the flow of finding any bonded bondedDevices
     * and the set of bondedDevices being empty, works correctly.
     */
    @Test
    fun findBondedDevices_NoDevicesFound() {
        // When the bondedDevices get requested from the mock Bt adapter, return a mock set.
        `when`(mockAdapter.bondedDevices)
            .thenReturn(setOf<BluetoothDevice>())
        // Call the Test repository class.
        assert(testRepository.findBondedDevices().isEmpty())
    }

    /**
     * Tests that the process of refreshing the local cache is performed correctly.
     */
    @Test
    fun refreshCache() {
        // Refresh the cache.
        testRepository.refreshCache()
        // Verify that the cache timestamp changed.
        verify(mockSharedPreferences, timeout(2000)).edit()
    }

    /**
     * Tests that the flow of synchronizing local found bondedDevices is performed correctly.
     */
    @Test
    fun synchronizeDevices() {
        val mockDeviceList = mutableListOf(Device().apply { syncState = false })
        // Return the mock device list when requested.
        `when`(mockDeviceDao.readUnSync())
            .thenReturn(mockDeviceList)
        // Create a mock response for the web server.
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(Gson().toJson(mockDeviceList.first()))
        // Enqueue the response.
        mockWebServer.enqueue(mockResponse)
        // Sync the devices.
        testRepository.synchronizeDevices()
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