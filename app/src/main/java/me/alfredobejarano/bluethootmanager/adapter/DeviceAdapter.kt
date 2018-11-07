package me.alfredobejarano.bluethootmanager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import me.alfredobejarano.bluethootmanager.R
import me.alfredobejarano.bluethootmanager.callback.DeviceDiffCallback
import me.alfredobejarano.bluethootmanager.data.Device
import me.alfredobejarano.bluethootmanager.utilities.fromTimeStamp
import java.util.*

/**
 *
 * Adapter class that displays a list of devices in a RecyclerView.
 *
 * @param elements The list of devices to be displayed.
 * @param foundDevices If the adapter is for displaying discovered devices (true) or stored devices (false).
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 21:38
 * @version 1.0
 **/
class DeviceAdapter(private var elements: MutableList<Device>?, private val foundDevices: Boolean) :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    /**
     * Decides which layout to use for the RecyclerView depending on the [foundDevices] property.
     */
    override fun getItemViewType(position: Int) = when (foundDevices) {
        true -> R.layout.item_device
        false -> R.layout.item_device_stored
    }

    /**
     * Creates a ViewHolder to be attached to the adapter.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DeviceViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))

    /**
     * Returns how many elements are going to be displayed.
     */
    override fun getItemCount() = elements?.size ?: 0

    /**
     * When a view holder gets attached to the adapter, proceeds
     * to render the item at the given attached position.
     */
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        // Get the element at the current position.
        elements?.get(position)?.let { device ->
            with(holder) {
                // Retrieve the ViewHolder context.
                val ctx = itemView.context
                // Display the device name
                deviceName.text = device.name
                // Display the device address
                address.text = device.address
                // Display the device strength
                strength.text = String.format(Locale.getDefault(), ctx.getString(R.string.strength), device.strength)
                // Display the device creation date.
                date?.text = device.createdAt.fromTimeStamp("MMM dd, yyyy - HH:mm a")
                // Display the device sync status
                syncStatus?.setImageResource(
                    if (device.syncState)
                        R.drawable.ic_cloud_queue
                    else
                        R.drawable.ic_cloud_off
                )
                // Set the click listener to the save button.
                saveButton?.setOnClickListener {
                    // If the ViewHolder context implements the onDeviceClickListener
                    if (ctx is OnDeviceClickListener) {
                        // Report a device clicked.
                        (ctx as OnDeviceClickListener)
                            .onDeviceClicked(device)
                    }
                }
            }
        }
    }

    /**
     * Updates the list of elements.
     */
    fun updateList(vararg newElements: Device) {
        // Create a temporary list that will hold both the old and new elements.
        val tempList = mutableListOf<Device>()
        tempList.addAll(elements ?: listOf())
        tempList.addAll(newElements)
        // Get the result from the difference.
        val result = DiffUtil.calculateDiff(DeviceDiffCallback(elements, tempList))
        elements?.addAll(newElements)
        // Report the updates.
        result.dispatchUpdatesTo(this)
    }

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val date: TextView? = itemView.findViewById(R.id.device_date)
        internal val saveButton: ImageView? = itemView.findViewById(R.id.save)
        internal val deviceName: TextView = itemView.findViewById(R.id.device_name)
        internal val address: TextView = itemView.findViewById(R.id.device_address)
        internal val strength: TextView = itemView.findViewById(R.id.device_strength)
        internal val syncStatus: ImageView? = itemView.findViewById(R.id.sync_status)
    }

    /**
     * Interface that defines functions to interact with a
     * clicked device.
     */
    interface OnDeviceClickListener {
        fun onDeviceClicked(device: Device)
    }
}