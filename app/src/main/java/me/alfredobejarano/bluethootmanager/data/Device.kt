package me.alfredobejarano.bluethootmanager.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 *
 * Data class that represents a bluetooth entity for Room and a Model for retrofit.
 *
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 09:44
 * @version 1.0
 **/
@Entity(tableName = "device_table")
data class Device(
    @Expose
    @SerializedName("name")
    val name: String,
    @Expose
    @SerializedName("strength")
    val strength: Int,
    @Expose
    @ColumnInfo(name = "pk")
    @SerializedName("address")
    @PrimaryKey(autoGenerate = false)
    val address: String,
    @Expose
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: String
)