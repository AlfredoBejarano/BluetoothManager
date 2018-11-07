package me.alfredobejarano.bluethootmanager


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice.ACTION_FOUND
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_device_discover.*
import me.alfredobejarano.bluethootmanager.adapter.DeviceAdapter
import me.alfredobejarano.bluethootmanager.utilities.Injector
import me.alfredobejarano.bluethootmanager.viewmodel.DeviceDiscoverViewModel
import javax.inject.Inject

/**
 * A simple [Fragment] subclass that displays a
 * list of bonded and discovered bluetooth devices.
 *
 */
class DeviceDiscoverFragment : Fragment() {
    @Inject
    lateinit var factory: DeviceDiscoverViewModel.Factory
    private val btAdapter = BluetoothAdapter.getDefaultAdapter()
    private val deviceFoundFilter = IntentFilter(ACTION_FOUND)
    private lateinit var viewModel: DeviceDiscoverViewModel

    /**
     * Broadcast receiver that will listen for found bluetooth devices.
     */
    private val mDeviceFoundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Get the intent action.
            val action = intent?.action
            // Is the action for a found device?
            if (ACTION_FOUND == action) {
                // If it is, fetch the device from the intent.
                viewModel.reportFoundDevice(intent)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inject this fragment dependencies.
        Injector.inject(this)
        // Fetch the ViewModel from the activity.
        viewModel = ViewModelProviders.of(requireActivity(), factory)[DeviceDiscoverViewModel::class.java]
        // Observe changes in the found devices.
        viewModel.devices.observe(this, Observer { devices ->
            // Create a new adapter if the list adapter is null or update the existing one.
            device_list?.adapter?.let {
                // Update the list.
                (device_list?.adapter as DeviceAdapter).updateList(devices)
            } ?: run {
                // Or create a new adapter if the list adapter is null.
                device_list?.adapter = DeviceAdapter(devices, true)
            }
        })
        // fetch the bonded devices
        viewModel.readBondedDevices()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device_discover, container, false)
    }

    /**
     * Starts discovering devices when the fragment starts.
     */
    override fun onStart() {
        super.onStart()
        requireContext().registerReceiver(mDeviceFoundReceiver, deviceFoundFilter)
        btAdapter.startDiscovery()
    }

    /**
     * Stops discovering devices if the fragment gets paused.
     */
    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(mDeviceFoundReceiver)
        btAdapter.cancelDiscovery()
    }
}
