package com.example.ble_esp32

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.companion.BluetoothDeviceFilter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
//import com.example.ble_esp32.DeviceProfile.Companion.CHARACTERISTCS_STATE_UUID
//import com.example.ble_esp32.DeviceProfile.Companion.SERVICE_UUID
import java.util.*
import org.jetbrains.*
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import timber.log.Timber

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2

class MainActivity : AppCompatActivity() {

    /*******************************************
     * Propiedades
     *******************************************/

     private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var isScanning = false

    val filter1 = ScanFilter.Builder().setDeviceName("simuladorRCP").build()
    //val filter2 = ScanFilter.Builder().setDeviceName("maniquiRCP").build()
    //val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString("00001810-0000-1000-8000-00805f9b34fb")).build()

    val filters:MutableList<ScanFilter> = mutableListOf()

    private val scanResults = mutableListOf<ScanResult>()
    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) {/* result ->
            if (isScanning) {
                stopBleScan()
            }
            with(result.device) {
                Timber.w("Connecting to $address")
                ConnectionManager.connect(this, this@MainActivity)
            }*/
        }
    }

    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    /*******************************************
     * Activity function overrides
     *******************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val botonEscanear:Button = findViewById(R.id.scan_button)
        val recyclerView:RecyclerView = findViewById(R.id.scan_results_recycler_view)

        botonEscanear.setOnClickListener {
            if (!isScanning) {
                botonEscanear.setText("Stop Scan")
                startBleScan()
            }else{
                botonEscanear.setText("Start Scan")
                stopBleScan()
            }
        }
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled){
            promptEnableBluetooth()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    //startBleScan()
                }
            }
        }
    }

    /*******************************************
     * Private functions
     *******************************************/

    private fun promptEnableBluetooth(){
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    private fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            filters.add(filter1)
            //filters.add(filter2)
            bleScanner.startScan(filters, scanSettings,scanCallback)
            isScanning = true
            scanResults.clear()
            scanResultAdapter.notifyDataSetChanged()

        }
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }
        runOnUiThread {
            val dialogBuilder = AlertDialog.Builder(this)

            // set message of alert dialog
            dialogBuilder.setMessage("La aplicación solicita el permiso de ubicación para buscar dispositivos Blootooth")
                // if the dialog is cancelable
                .setCancelable(false)
                // positive button text and action
                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                    requestPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION,LOCATION_PERMISSION_REQUEST_CODE)
                })
                // negative button text and action
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                })

            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("Permiso de ubicación requerido")
            // show alert dialog
            alert.show()
        }
    }

    private fun setupRecyclerView() {
        scan_results_recycler_view.apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = scan_results_recycler_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    /*******************************************
     * Callback bodies
     *******************************************/
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
        /*    with (result.device) {
                val uuidsss = result.scanRecord!!.serviceUuids[0]
                Log.i("scanCallback","Found BLE device! Name: ${name ?: "unnamed"}, address: $address, $uuidsss")
            }*/
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    Log.i("scanCallback","Found BLE device! Name: ${name ?: "unnamed"}, address: $address")
                //Timber.i("Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("scanCallback","onScanFailed: code $errorCode" )
            //Timber.e("onScanFailed: code $errorCode")
        }
    }

    /*******************************************
     * Extension functions
     *******************************************/
    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }
    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }
}