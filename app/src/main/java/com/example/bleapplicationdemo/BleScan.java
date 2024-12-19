package com.example.bleapplicationdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BleScan {
    private final MainActivity context;
    private final IScanCallBack callBack;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private BluetoothManager bluetoothManager;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 30000;

    public BleScan(MainActivity context, IScanCallBack callBack) {
        this.context = context;
        this.callBack = callBack;
        this.askPermission();
        mHandler = new Handler();
        bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        bluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
    }
    private void askPermission() {
        String permission = Manifest.permission.BLUETOOTH_CONNECT;

        int grant = ContextCompat.checkSelfPermission(context, permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                String bluetoothScanPermission = Manifest.permission.BLUETOOTH_SCAN;
//                String bluetoothConnect = Manifest.permission.BLUETOOTH_CONNECT;
//                permission_list[1] = bluetoothScanPermission;
//                permission_list[2] = bluetoothConnect;
//
//            }
            ActivityCompat.requestPermissions(context, permission_list, 1);
        }
    }
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            System.out.println("cuongpq: " + result.getDevice().getName());
            List<BluetoothDevice> addedDevices = callBack.getScannedList();
            if (!addedDevices.contains(result.getDevice())) {
                byte[] deviceInfo = result.getScanRecord().getBytes();
                byte[] brand = Arrays.copyOfRange(deviceInfo, 5, 6);
                System.out.println(new String(brand));
                String serialNo = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }

                if (result.getDevice() != null && result.getDevice().getName() != null && result.getDevice().getName().equals("Blue2-MFT")) {
                    byte[] bDeviceSerial = Arrays.copyOfRange(deviceInfo, 8, 20);
                    serialNo = new String(bDeviceSerial);
                }
                String finalSerialNo = serialNo;

                callBack.addNewDevice(new CustomBluetoothDevice(result.getDevice(), finalSerialNo, false));
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    public void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(context,
                                Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }
                    bluetoothLeScanner.stopScan(mLeScanCallback);
                    callBack.onScanFinished();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .build();
            List<ScanFilter> filters = new ArrayList<ScanFilter>();
            bluetoothLeScanner.startScan(filters, settings, mLeScanCallback);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(mLeScanCallback);
            callBack.onScanFinished();
        }
    }

    public interface IScanCallBack {
        void addNewDevice(CustomBluetoothDevice device);
        List<BluetoothDevice> getScannedList();
        void onScanFinished();
    }
}
