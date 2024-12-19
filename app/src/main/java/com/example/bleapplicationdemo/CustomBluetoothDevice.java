package com.example.bleapplicationdemo;

import android.bluetooth.BluetoothDevice;

public class CustomBluetoothDevice {
    private BluetoothDevice bluetoothDevice;
    private String serialNo;
    private boolean isConnected;

    public CustomBluetoothDevice(BluetoothDevice bluetoothDevice, String serialNo,
                                 boolean isConnected) {
        this.bluetoothDevice = bluetoothDevice;
        this.serialNo = serialNo;
        this.isConnected = isConnected;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean equals(Object obj) {
        return (this == obj);
    }
}
