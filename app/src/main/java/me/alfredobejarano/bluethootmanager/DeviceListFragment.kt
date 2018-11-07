package me.alfredobejarano.bluethootmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import me.alfredobejarano.bluethootmanager.adapter.DeviceAdapter
import me.alfredobejarano.bluethootmanager.data.Device
import me.alfredobejarano.bluethootmanager.utilities.Injector
import me.alfredobejarano.bluethootmanager.viewmodel.DeviceListViewModel
import javax.inject.Inject

/**
 *
 * Simple [Fragment] class that displays a list
 * of devices stored by the user.
 *
 * @author Alfredo Bejarano
 * @since November 07, 2018 - 11:32
 * @version 1.0
 **/
class DeviceListFragment : Fragment() {
    @Inject
    lateinit var mFactory: DeviceListViewModel.Factory
    private lateinit var mViewModel: DeviceListViewModel
    private lateinit var mDeviceRecyclerView: RecyclerView
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    /**
     * Creates a SwipeRefreshLayout and assigns it as the fragment root,
     * then proceeds to create a RecyclerView and assign it as a child to the
     * SwipeRefreshLayout
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
    // Create the SwipeRefreshLayout
        SwipeRefreshLayout(requireContext()).apply {
            (this as ViewGroup).addView( // Create the RecyclerView and add it.
                RecyclerView(requireContext()).also {
                    // Assign a layout manager.
                    it.layoutManager = LinearLayoutManager(requireContext())
                    // Set the RecyclerView as a value for the mDeviceRecyclerView property.
                    mDeviceRecyclerView = it
                })
        }.also {
            // After creating the SwipeRefreshLayout, assign it as the value for the mSwipeRefreshLayout property.
            mSwipeRefreshLayout = it
            // And assign a refresh listener.
            it.setOnRefreshListener { refresh() }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Create the ViewModel for this class.
        mViewModel = ViewModelProviders.of(this, mFactory)[DeviceListViewModel::class.java].also {
            // After creating it, observe its devices property.
            observeViewModel()
            // And fetch the devices.
            it.fetchDevices()
        }
    }

    /**
     * Observes the ViewModel devices property to update the UI correctly.
     */
    private fun observeViewModel() = mViewModel.devices.observe(this, Observer { devices ->
        // Check if the devices list is not empty.
        if (devices?.isNotEmpty() == true) {
            // If the adapter exists for the list, update the elements, if not, create a new one.
            mDeviceRecyclerView.adapter?.let {
                // Update the adapter.
                (mDeviceRecyclerView.adapter as DeviceAdapter?)?.updateList(*devices.toTypedArray())
            } ?: run {
                // Create the adapter
                mDeviceRecyclerView.adapter = DeviceAdapter(devices as MutableList<Device>?, false)
            }
        } else {
            // If it is, report that to the user.
            displayMessage(R.string.no_devices_found)
        }
    })

    /**
     * Displays a SnackBar using a String resource as the message.
     */
    private fun displayMessage(@StringRes message: Int) = (requireActivity() as MainActivity).displayMessage(message)

    /**
     * Injects this class dependencies.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.inject(this)
    }

    /**
     * Invalidates the list layout and request
     * the devices again from the ViewModel.
     */
    private fun refresh() {
        mDeviceRecyclerView.adapter = null
        mDeviceRecyclerView.invalidate()
        mViewModel.fetchDevices()
    }
}