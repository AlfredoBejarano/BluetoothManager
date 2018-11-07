package me.alfredobejarano.bluethootmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import me.alfredobejarano.bluethootmanager.BuildConfig

/**
 *
 * Class to be implemented by Room that
 * builds the local storage database.
 *
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 16:16
 * @version 1.0
 **/
@Database(entities = [Device::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Returns an implementation of the [DeviceDao] interface.
     */
    abstract fun getDeviceDao(): DeviceDao

    /**
     * Used for Singleton usage.
     */
    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Returns this singleton database instance or
         * creates a new one, assigns it to this singleton
         * instance property and returns it.
         * @param ctx The context requesting the database instance.
         */
        fun getInstance(ctx: Context) =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(ctx).also { instance = it }
            }

        /**
         * Builds an [AppDatabase] instance using Room.
         * @param ctx The context creating the database.
         */
        private fun buildDatabase(ctx: Context) =
            Room.databaseBuilder(
                ctx,
                AppDatabase::class.java,
                "${BuildConfig.APPLICATION_ID}-db"
            ).fallbackToDestructiveMigration().build()
    }
}