package me.alfredobejarano.bluethootmanager

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import me.alfredobejarano.bluethootmanager.adapter.DeviceAdapter
import me.alfredobejarano.bluethootmanager.data.Device
import me.alfredobejarano.bluethootmanager.utilities.Injector
import me.alfredobejarano.bluethootmanager.viewmodel.DeviceDiscoverViewModel
import javax.inject.Inject

class MainActivity : AppCompatActivity(), DeviceAdapter.OnDeviceClickListener {
    private val btAdapter = BluetoothAdapter.getDefaultAdapter()
    @Inject
    lateinit var factory: DeviceDiscoverViewModel.Factory
    private lateinit var viewModel: DeviceDiscoverViewModel

    companion object {
        private const val BLUETOOTH_TURN_ON_CODE = 21
    }

    /**
     * Saves a device when it gets clicked from an adapter.
     */
    override fun onDeviceClicked(device: Device) {
        displayMessage(R.string.saving_device)
        viewModel.saveDevice(device).observe(this, Observer {
            it?.let {
                displayMessage(R.string.device_saved)
            } ?: run {
                displayMessage(R.string.device_not_saved)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initializes the injector object.
        Injector.initialize(application)
        // Injects this class dependencies.
        Injector.inject(this)
        // Check if the device is compatible with bluetooth.
        if (!isDeviceCompatible()) {
            // Notify to the user that the device is not compatible.
            displayMessage(R.string.device_not_compatible)
            // Finish the app
            finishAffinity()
        } else {
            // Retrieves a ViewModel for this class.
            viewModel = ViewModelProviders.of(this, factory)[DeviceDiscoverViewModel::class.java]
            // Sets the content view.
            setContentView(R.layout.activity_main)
            // Turn on the bluetooth if it hasn't been turned on.
            if (!isBluetoothTurnedOn()) {
                turnOnBluetooth()
            }
        }
    }

    /**
     * Displays a SnackBar with a message.
     */
    private fun displayMessage(text: String) =
        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show()

    /**
     * Displays a SnackBar with a message using a string resource.
     */
    private fun displayMessage(@StringRes resource: Int) =
        displayMessage(getString(resource))

    /**
     * Checks if the device is compatible with bluetooth.
     */
    private fun isDeviceCompatible() = btAdapter != null

    /**
     * Checks if the device bluetooth is turned on.
     */
    private fun isBluetoothTurnedOn() = btAdapter?.isEnabled == true

    /**
     * Prompts to turn on the bluetooth if it is not turned on.
     */
    private fun turnOnBluetooth() = startActivityForResult(
        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_TURN_ON_CODE
    )

    /**
     * Responds to an activity result depending on a request code.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            BLUETOOTH_TURN_ON_CODE -> {
                // Umm, seems like the user didn't turned its bluetooth on after prompting.
                if (!isBluetoothTurnedOn()) {
                    // Display a SnackBar for the user allowing him to turn the bluetooth on.
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        R.string.you_didnt_turned_your_bluetooth_on,
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(R.string.turn_on) {
                        turnOnBluetooth()
                    }.show()
                }
            }
        }
    }
}
