package com.sushilbx.bletask;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BluetoothDevicesViewModel extends AndroidViewModel {

    MutableLiveData<Set<BluetoothDevice>> devices = new MutableLiveData<>(Collections.emptySet());


    public BluetoothDevicesViewModel(@NonNull Application application) {
        super(application);
    }


    public ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Set<BluetoothDevice> currentDevices = devices.getValue();
            if (currentDevices == null) {
                currentDevices = new HashSet<>();
            }
            BluetoothDevice device = result.getDevice();
            currentDevices.add(result.getDevice());
            if (ActivityCompat.checkSelfPermission(getApplication(), android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                String deviceName = device.getName() != null ? device.getName() : "Unknown Device";
                Log.d("BLE Device", "Device: " + deviceName + " - " + device.getAddress());
            }

            devices.postValue(currentDevices);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Set<BluetoothDevice> currentDevices = devices.getValue();
            if (currentDevices == null) {
                currentDevices = new HashSet<>();
            }
            for (ScanResult result : results) {
                currentDevices.add(result.getDevice());
            }
            devices.postValue(currentDevices);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("BLE Scan", "Scan failed with error code: " + errorCode);
        }
    };


}
