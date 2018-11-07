package me.alfredobejarano.bluethootmanager.callback

import androidx.recyclerview.widget.DiffUtil
import me.alfredobejarano.bluethootmanager.data.Device

/**
 *
 * [DiffUtil.Callback] class that will report to a RecyclerView
 * adapter updates in a list of devices.
 *
 * @author Alfredo Bejarano
 * @since November 06, 2018 - 22:45
 * @version 1.0
 **/
class DeviceDiffCallback(
    private val oldItems: List<Device>?,
    private val newItems: List<Device>?
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldItems?.size ?: 0
    override fun getNewListSize() = newItems?.size ?: 0

    /**
     * Checks if two device items are the same.
     */
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        newItems?.get(newItemPosition)?.compareTo(oldItems?.get(oldItemPosition) ?: Device()) == 0

    /**
     * Checks if two device object contents are the same.
     */
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        areItemsTheSame(oldItemPosition, newItemPosition)
}