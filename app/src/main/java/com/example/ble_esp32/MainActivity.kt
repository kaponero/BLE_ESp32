package com.example.ble_esp32

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.companion.BluetoothDeviceFilter
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    companion object{
        private val TAG = "TWESTBTSCANNER"
        val REQUEST_ENABLE_BT = 1
    }

    //Banderas que indicaran si tenemos permisos
    private var tienePermisoUbicacion = false

    // Código de permiso, defínelo tú mismo
    private val CODIGO_PERMISOS_UBICACION = 1

    private fun verificarYPedirPermisosDeUbicacion() {
        val estadoDePermiso = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (estadoDePermiso == PackageManager.PERMISSION_GRANTED) {
            // En caso de que haya dado permisos ponemos la bandera en true
            // y llamar al método
            permisoDeUbicacionConcedido()
        } else {
            // Si no, entonces pedimos permisos. Ahora mira onRequestPermissionsResult
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                CODIGO_PERMISOS_UBICACION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CODIGO_PERMISOS_UBICACION -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permisoDeUbicacionConcedido()
            } else {
                permisoDeUbicacionDenegado()
            }
        }
    }

    private fun permisoDeUbicacionConcedido() {
        // Aquí establece las banderas o haz lo que
        // ibas a hacer cuando el permiso se concediera. Por
        // ejemplo puedes poner la bandera en true y más
        // tarde en otra función comprobar esa bandera
        Toast.makeText(this@MainActivity, "El permiso de ubicación está concedido", Toast.LENGTH_SHORT).show()
        tienePermisoUbicacion = true
    }

    private fun permisoDeUbicacionDenegado() {
        // Esto se llama cuando el usuario hace click en "Denegar" o
        // cuando lo denegó anteriormente
        Toast.makeText(this@MainActivity, "El permiso de ubicación está denegado", Toast.LENGTH_SHORT).show()
        tienePermisoUbicacion = false
    }
    var bluetoothGatt : BluetoothGatt?= null

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    private fun onBluetooth(){
        if (bluetoothAdapter.isEnabled){
            Toast.makeText(this@MainActivity, "Bluetooth encendido", Toast.LENGTH_SHORT).show()
        }else{
            Log.v(TAG,"BT is disabled")
            val btIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(btIntent, REQUEST_ENABLE_BT)
        }
    }

    lateinit var reciver :BluetoothModeChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        verificarYPedirPermisosDeUbicacion()
        onBluetooth()

        reciver = BluetoothModeChangeReceiver()
        val filter1 = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

        registerReceiver(reciver, filter1);
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(reciver)
    }

    override fun onResume() {
        super.onResume()
        if (bluetoothAdapter.isEnabled){
            StartBLEScan()
        }
    }

    override fun onStart() {
        super.onStart()
        onBluetooth()
    }

    fun StartBLEScan(){
        Log.v(TAG,"StartBLEScan")

        val scanFilter = ScanFilter.Builder().build()

        val scanFilters:MutableList<ScanFilter> = mutableListOf()
        scanFilters.add(scanFilter)

        val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        Log.v(TAG,"Start Scan")

        if (tienePermisoUbicacion) {
            bluetoothAdapter.bluetoothLeScanner.startScan(scanFilters, scanSettings, bleScanCallback)
        }else{
            verificarYPedirPermisosDeUbicacion()
        }
        //desde aca


    }
    private val bleScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result:ScanResult){
                //super.onScanResult(callbackType, result)
                Log.v(TAG,"onScanResult")

                val bluetoothDevice = result.device
                if(bluetoothDevice != null) {
                    Log.v(TAG, "Device name ${result!!.device.name} Device Adrress ${bluetoothDevice.uuids}")
                }
            }

    }
}




