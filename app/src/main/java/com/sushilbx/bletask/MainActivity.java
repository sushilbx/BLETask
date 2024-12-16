package com.sushilbx.bletask;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.sushilbx.bletask.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding b;
    private BluetoothLeScanner bluetoothLeScanner;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private BluetoothDevicesViewModel viewModel;
    BLEAdapter bleAdapter = new BLEAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        viewModel = new ViewModelProvider(MainActivity.this).get(BluetoothDevicesViewModel.class);
        b.rvBluetoothDevice.setAdapter(bleAdapter);
        initObservers();
        listener();
    }

    private void listener() {
        b.btScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initObservers();
                checkBluetoothPermissions();
                initializeBluetooth();
            }
        });
    }

    private void initObservers() {
        viewModel.devices.observe(this, devices -> {
            if (devices != null) {
                bleAdapter.submitList(new ArrayList<>(devices));
            }
        });
    }

    private void checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String[] permissions = {
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };

            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, REQUEST_BLUETOOTH_PERMISSIONS);
                    return;
                }
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_BLUETOOTH_PERMISSIONS);
            }
        }
    }

    private void initializeBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_PERMISSIONS);
            }
        } else {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

            startBleScanning();
        }
    }

    private void startBleScanning() {
        if (bluetoothLeScanner == null) {
            Log.e("BLE", "BluetoothLeScanner is not initialized.");
            return;
        }

        viewModel.devices.postValue(new HashSet<>());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            bluetoothLeScanner.startScan(viewModel.scanCallback);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                bluetoothLeScanner.stopScan(viewModel.scanCallback);
                Log.d("BLE Scan", "Scanning stopped. Total devices found: " + viewModel.devices.getValue().size());
            }, 10000);
        }
    }

    private void stopBleScanning(ScanCallback scanCallback) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(scanCallback);
                Log.d("BLE Scan", "Scanning stopped manually.");
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBleScanning(viewModel.scanCallback);
    }
}