package me.alfredobejarano.bluethootmanager.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.room.Room
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Test class for the [DeviceDao] interface.
 *
 * @author Alfredo Bejarano
 * @version 1.0
 * @since November 06, 2018 - 16:24
 */
@RunWith(RobolectricTestRunner::class)
class DeviceDaoTest {
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var testDao: DeviceDao

    @Before
    fun setup() {
        // Builds the test database for this class.
        testDao = Room
            .inMemoryDatabaseBuilder(RuntimeEnvironment.systemContext, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build().getDeviceDao()
    }

    /**
     * Asserts that the database instance gets built correctly.
     */
    @Test
    fun insertOrUpdate() {
        val mockObserver =
            mock(Observer::class.java) as Observer<List<Device>>
        testDao.read().observeForever(mockObserver)
        testDao.insertOrUpdate(Device("", "", "1", "", true))
        verify(mockObserver, atLeastOnce())
            .onChanged(any())
    }

    // Asserts that the database instance gets built correctly.
    @Test
    fun read() {
        val mockObserver =
            mock(Observer::class.java) as Observer<List<Device>>
        testDao.read().observeForever(mockObserver)
        verify(mockObserver, atLeastOnce())
            .onChanged(any())
    }

    /**
     * Asserts that the database instance gets built correctly.
     */
    @Test
    fun readUnSync() {
        // Create a mock device
        val mockUnSyncedDevice = Device("", "", "2", "", false)
        // Insert the device.
        testDao.insertOrUpdate(mockUnSyncedDevice)
        // Retrieve the un synced devices.
        val unSyncDevices = testDao.readUnSync()
        // Assert that the list is not empty.
        assert(unSyncDevices.isNotEmpty())
    }

    /**
     * Asserts that nuking the table works properly.
     */
    @Test
    fun deleteAll() {
        // Insert a device.
        testDao.insertOrUpdate(Device())
        // Read the results.
        val results = testDao.read()
        // Observe the results.
        results.observeForever {
            // Assert that the results are not empty.
            assert(it?.isNotEmpty() == true)
            // Remove the results observer.
            results.removeObserver {
                // When removed, delete all records.
                testDao.deleteAll()
                // After clearing the db, read the results again and observe them again.
                testDao.read().observeForever { emptyResult ->
                    // Assert that the results changes are now empty.
                    assert(emptyResult?.isEmpty() == true)
                }
            }
        }
    }
}