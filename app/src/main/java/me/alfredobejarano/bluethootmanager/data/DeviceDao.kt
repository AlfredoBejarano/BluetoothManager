package me.alfredobejarano.bluethootmanager.data

import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 *
 * Dao interface to be implemented by Room to create a class
 * that allows database operations for the [Device] entity.
 *
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 09:55
 * @version 1.0
 **/
interface DeviceDao {
    /**
     * Inserts a device into the database, if it already exists
     * the device gets updated with the device data.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(device: Device)

    /**
     * Retrieves all the bluetooth devices from the
     * local database ordered by their creation date.
     */
    @Query("SELECT * FROM device_table ORDER BY created_at ASC")
    fun read(): LiveData<List<Device>>

    @Query("SELECT * FROM device_table WHERE synchronized = 0 ORDER BY created_at ASC")
    fun readUnSync(): List<Device>

    /**
     * Nukes the device_table, deletes everything.
     */
    @Query("DELETE FROM device_table")
    fun deleteAll()
}