package me.alfredobejarano.bluethootmanager.data

import androidx.room.Room
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Test class for the [AppDatabase] class.
 *
 * @author Alfredo Bejarano
 * @version 1.0
 * @since November 06, 2018 - 16:24
 */
@RunWith(RobolectricTestRunner::class)

class AppDatabaseTest {
    private lateinit var testDB: AppDatabase

    @Before
    fun setup() {
        // Builds the test database for this class.
        testDB = Room
            .inMemoryDatabaseBuilder(RuntimeEnvironment.systemContext, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    // Asserts that the database instance gets built correctly.
    @Test
    fun getDeviceDao() {
        AppDatabase
            .getInstance(RuntimeEnvironment.systemContext)
            .getDeviceDao()
    }
}