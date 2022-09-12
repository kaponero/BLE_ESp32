package com.example.ble_esp32

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BluetoothModeChangeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val isBluetoothModeChanged = intent?.action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)

        if(isBluetoothModeChanged){
            val isBluetoothModeEnable = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR)
            if( isBluetoothModeEnable == BluetoothAdapter.STATE_ON){
                Toast.makeText(context, "Bluetooth Enabled", Toast.LENGTH_SHORT).show()
            }
            if( isBluetoothModeEnable == BluetoothAdapter.STATE_OFF){
                Toast.makeText(context, "Bluetooth Disabled", Toast.LENGTH_SHORT).show()
            }
        }
/*
        val isBluetoothModeEnable = intent?.getBooleanExtra("state", false) ?: return
        //val isBluetoothModeEnable = intent?.action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)
        if(isBluetoothModeEnable){
            Toast.makeText(context, "Bluetooth Enabled", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(context, "Bluetooth Disabled", Toast.LENGTH_SHORT).show()
        }*/
    }
}