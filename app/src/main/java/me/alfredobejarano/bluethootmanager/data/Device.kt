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
    var name: String,
    @Expose
    @SerializedName("strength")
    var strength: Int,
    @Expose
    @ColumnInfo(name = "pk")
    @SerializedName("address")
    @PrimaryKey(autoGenerate = false)
    var address: String,
    @Expose
    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    var createdAt: String,
    @ColumnInfo(name = "sync_state")
    var syncState: Boolean
) {
    constructor() : this("", 0, "", "", true)
}