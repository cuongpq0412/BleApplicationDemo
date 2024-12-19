package com.example.bleapplicationdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bleapplicationdemo.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BleAdapter.IBle, BleScan.IScanCallBack, BluetoothLeService.BluetoothServiceConnectCallBack {
    private ActivityMainBinding binding;
    private BleAdapter adapter;
    private BleScan bleScan;
    private BluetoothLeService mBluetoothLeService;
    private List<CustomBluetoothDevice> bleDevices = new ArrayList<>();
    public static String TAG = "cuongpq";
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e(TAG, "onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mBluetoothLeService.setBluetoothServiceConnectCallBack(getBluetoothServiceConnectCallBack());
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private  BluetoothLeService.BluetoothServiceConnectCallBack getBluetoothServiceConnectCallBack() {
         return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        checkPermissions();
        bleScan = new BleScan(this, this);
        event();
        initialRecycleView();
    }

    public void checkPermissions() {
        List<String> mPermissions = new ArrayList<>();
        boolean permissionCheckPassed = true;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            mPermissions.add(android.Manifest.permission.CAMERA);
            permissionCheckPassed = false;
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            mPermissions.add(android.Manifest.permission.BLUETOOTH);
            permissionCheckPassed = false;
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            mPermissions.add(android.Manifest.permission.BLUETOOTH_ADMIN);
            permissionCheckPassed = false;
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            permissionCheckPassed = false;
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mPermissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            permissionCheckPassed = false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                mPermissions.add(android.Manifest.permission.BLUETOOTH_SCAN);
                permissionCheckPassed = false;
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                mPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
                permissionCheckPassed = false;
            }
        }
        if (!permissionCheckPassed) {
            ActivityCompat.requestPermissions(this, mPermissions.toArray(new String[0]), 1002);
        }
    }

    private void event() {
        binding.btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progress.setVisibility(View.VISIBLE);
                bleScan.scanLeDevice(true);
            }
        });
    }

    private void initialRecycleView() {
        adapter = new BleAdapter(this);
        binding.rcBle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        binding.rcBle.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("cuongpq: " + grantResults.toString());
    }

    @Override
    public int count() {
        return bleDevices.size();
    }

    @Override
    public CustomBluetoothDevice data(int position) {
        return bleDevices.get(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void addNewDevice(CustomBluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                bleDevices.add(device);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public List<BluetoothDevice> getScannedList() {
        List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
        for (CustomBluetoothDevice mLeDevice : bleDevices) {
            bluetoothDevices.add(mLeDevice.getBluetoothDevice());
        }
        return bluetoothDevices;
    }

    @Override
    public void onScanFinished() {
        binding.progress.setVisibility(View.GONE);
    }

    @Override
    public void onConnected() {
        binding.tvStatus.setText("Connected");
    }

    @Override
    public void onDisconnected() {
        binding.tvStatus.setText("Disconnected");
    }

    @Override
    public void onPushTempValue(double temp) {
        binding.tvTemp.setText("" + temp);
    }

    @Override
    public void onPairDevice(CustomBluetoothDevice data) {
        binding.tvStatus.setText("Connecting");
        mBluetoothLeService.connect(data.getBluetoothDevice().getAddress());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
}