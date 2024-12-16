package com.sushilbx.bletask;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.sushilbx.bletask.databinding.ActivityBledeviceBinding;

import java.util.UUID;

public class BLEDeviceActivity extends AppCompatActivity {
    ActivityBledeviceBinding b;
    BluetoothDevice deviceAd;
    private static final UUID Battery_Service_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    private static final UUID Battery_Level_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityBledeviceBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        deviceAd = getIntent().getParcelableExtra("blutoothdevice");
        b.tvAddress.setText("Device Address : " + deviceAd);
        Log.e("deviceAd", String.valueOf(deviceAd));
        connectToDevice(deviceAd);
    }

    private void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        device.connectGatt(BLEDeviceActivity.this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLE", "Connected to device: " + device.getAddress());
                    if (ActivityCompat.checkSelfPermission(BLEDeviceActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    gatt.discoverServices(); // Discover services after successful connection
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BLE", "Disconnected from device: " + device.getAddress());
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService batteryService = gatt.getService(Battery_Service_UUID);
                    if (batteryService != null) {
                        BluetoothGattCharacteristic batteryLevelCharacteristic = batteryService.getCharacteristic(Battery_Level_UUID);
                        if (batteryLevelCharacteristic != null) {
                            if (ActivityCompat.checkSelfPermission(BLEDeviceActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                                return;
                            }
                            gatt.readCharacteristic(batteryLevelCharacteristic); // Read the battery level
                        }
                    } else {
                        Log.d("BLE", "Battery service not found!");
                        b.tvBle.setText("Battery service not found!");

                    }
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (characteristic.getUuid().equals(Battery_Level_UUID)) {
                        int batteryLevel = characteristic.getValue()[0];
                        Log.e("BLE", "Battery Level: " + batteryLevel + "%");
                        runOnUiThread(() -> b.tvBle.setText(batteryLevel + "%"));
                    }
                }
            }
        });
    }

}