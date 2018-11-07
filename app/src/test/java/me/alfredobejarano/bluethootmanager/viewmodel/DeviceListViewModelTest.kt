package me.alfredobejarano.bluethootmanager.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
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
 * Test suite for the [DeviceListViewModel] class.
 *
 * @author Alfredo Bejarano
 * @version 1.0
 * @since November 06, 2018 - 18:36
 */
@RunWith(MockitoJUnitRunner::class)
class DeviceListViewModelTest {
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockRepository: DeviceRepository

    private lateinit var testViewModel: DeviceListViewModel

    @Before
    fun setup() {
        testViewModel = DeviceListViewModel(mockRepository)
    }

    /**
     * Tests that the flow of fetching devices
     * from a [DeviceRepository] executes correctly.
     */
    @Test
    fun fetchDevices() {
        // Mocked objects for the mocked objects to respond.
        val mockDeviceList = listOf<Device>()
        val mockResult = mock(LiveData::class.java) as LiveData<List<Device>>
        // Mocked Observer to assert the flow executed correctly.
        val mockObserver = mock(Observer::class.java) as Observer<List<Device>>
        // Define what the mock objects will return
        `when`(mockResult.value)
            .thenReturn(mockDeviceList)
        `when`(mockRepository.fetchDevices())
            .thenReturn(mockResult)
        // Observe the view model property
        testViewModel.devices.observeForever(mockObserver)
        // Fetch the devices.
        testViewModel.fetchDevices()
        // Verify that the ViewModel detected changes.
        verify(mockObserver, timeout(2000))
            .onChanged(any())
    }
}