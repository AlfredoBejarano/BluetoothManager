package me.alfredobejarano.bluethootmanager.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 *
 * API definitions to be implemented by a Retrofit client that
 * allows access to said endpoints from a remote API.
 *
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 10:09
 * @version 1.0
 **/
interface DeviceService {
    /**
     * Calls the remote API to retrieve the list of stored devices.
     */
    @GET("devices/")
    fun fetchDevices(): Call<List<Device>>

    /**
     * Save the device to the remote API.
     * **Note:** It returns the saved device itself, if it
     * already exists, it returns the same device with the
     * "created_at" property updated.
     */
    @POST("add/")
    fun addDevice(@Body device: Device): Call<Device>
}