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
    var createdAt: String?,
    @ColumnInfo(name = "sync_state")
    var syncState: Boolean
) : Comparable<Device> {
    /**
     * Compares a [Device] with another,
     * if their MAC addresses are the same,
     * return 0, if not, return one.
     */
    override fun compareTo(other: Device) =
        if (other.address == this.address) {
            0
        } else {
            1
        }

    /**
     * Checks if this [Device] object
     * has the same property values as
     * another [Device] object.
     */
    fun hasTheSameContentAs(other: Device) =
        this.name == other.name &&
                this.address == other.address &&
                this.strength == other.strength &&
                this.createdAt == other.createdAt &&
                this.syncState == other.syncState

    constructor() : this("", 0, "", "", true)

}