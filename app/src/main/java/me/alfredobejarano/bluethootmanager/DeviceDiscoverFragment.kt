package me.alfredobejarano.bluethootmanager


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice.ACTION_FOUND
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_device_discover.*
import me.alfredobejarano.bluethootmanager.adapter.DeviceAdapter
import me.alfredobejarano.bluethootmanager.data.Device
import me.alfredobejarano.bluethootmanager.utilities.Injector
import me.alfredobejarano.bluethootmanager.viewmodel.DeviceDiscoverViewModel
import javax.inject.Inject

/**
 * A simple [Fragment] subclass that displays a
 * list of bonded and discovered bluetooth bondedDevices.
 *
 */
class DeviceDiscoverFragment : Fragment() {
    companion object {
        private const val PERMISSION_REQUEST = 21
    }

    @Inject
    lateinit var factory: DeviceDiscoverViewModel.Factory
    private val btAdapter = BluetoothAdapter.getDefaultAdapter()
    private val deviceFoundFilter = IntentFilter(ACTION_FOUND)
    private lateinit var viewModel: DeviceDiscoverViewModel

    /**
     * Broadcast receiver that will listen for found bluetooth bondedDevices.
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        device_list?.layoutManager = LinearLayoutManager(requireContext())
        // Fetch the ViewModel from the activity.
        viewModel = ViewModelProviders.of(requireActivity(), factory)[DeviceDiscoverViewModel::class.java]
        // Provide observers for the ViewModel.
        observeViewModel()
        // fetch the bonded bondedDevices
        viewModel.readBondedDevices()
        // Check for coarse location permission.
    }

    /**
     * Provides observation for the necessary LiveData properties from the ViewModel.
     */
    private fun observeViewModel() {
        // Observe changes in the found bondedDevices.
        viewModel.bondedDevices.observe(this, Observer { devices ->
            // Create a new adapter if the list adapter is null or update the existing one.
            device_list?.adapter?.let {
                // Update the list.
                (device_list?.adapter as DeviceAdapter).updateList(*devices.toTypedArray())
            } ?: run {
                // Or create a new adapter if the list adapter is null.
                device_list?.adapter = DeviceAdapter(devices as MutableList<Device>, true)
            }
        })
        // Observe changes in the discovered device.
        viewModel.discoveredDevice.observe(this, Observer { device ->
            // Create a new adapter if the list adapter is null or update the existing one.
            device_list?.adapter?.let {
                // Update the list.
                (device_list?.adapter as DeviceAdapter).updateList(device)
            } ?: run {
                // Or create a new adapter if the list adapter is null.
                device_list?.adapter = DeviceAdapter(mutableListOf(device), true)
            }
        })
    }

    /**
     * Checks if the COARSE_LOCATION permission is granted, if not, proceeds to request it.
     */
    private fun requestLocationPermission() {
        // Check if the permission is not granted.
        val isDenied =
            checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_DENIED

        if (isDenied) {
            // If it is not granted, display a SnackBar requesting the permission.
            Snackbar.make(view ?: View(requireContext()), R.string.permission_needed, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.grant) {
                    // Set an action to grant it when the SnackBar shows.
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST)
                }.show()
        } else {
            btAdapter.startDiscovery()
        }
    }

    /**
     * Detects the result of a permission request.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST) { // Checks if the result is for the COARSE_LOCATION request.
            if (grantResults.isNotEmpty() && grantResults.first() == PERMISSION_GRANTED) {
                // Start the device discovery
                btAdapter.startDiscovery()
            } else {
                // If the permission was denied, report to the user that no nearby devices will be discovered.
                (requireActivity() as MainActivity).displayMessage(R.string.devices_will_not_be_discovered)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * Starts discovering devices when the fragment starts.
     */
    override fun onStart() {
        super.onStart()
        requireActivity().registerReceiver(mDeviceFoundReceiver, deviceFoundFilter)
        requestLocationPermission()
    }

    /**
     * Stops discovering devices and unregisters the broadcast receiver.
     */
    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(mDeviceFoundReceiver)
        btAdapter.cancelDiscovery()
    }
}
