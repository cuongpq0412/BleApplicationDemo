package com.example.bleapplicationdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServiceConnectCallBack bluetoothServiceConnectCallBack;
    private final IBinder mBinder = new LocalBinder();
    public static String TAG = "cuongpq";
    private String mBluetoothDeviceAddress;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public static String COMARK_SERVICE = "00001809-0000-1000-8000-00805f9b34fb";
    //public static String COMARK_MEASUREMENT= "00002a1c-0000-1000-8000-00805f9b34fb";
    public static String COMARK_INTERMEDIATE_MEASUREMENT = "00002a1e-0000-1000-8000-00805f9b34fb";
    public static String BLUE2_SERVICE = "f2b32c77-ea68-464b-9cd7-a22cbffb98bd";
    public static String BLUE2_MEASUREMENT =  "fe50b198-5137-4b83-a266-6181e088d395";
    public static String KWIKSWITCH_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static String KWIKSWITCH_MEASUREMENT = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static String CHEFSMART_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static String CHEFSMART_MEASUREMENT = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static String MFT_SERVICE = "6435908b-982e-4761-9eaa-bbb072119496";
    public static String MFT_MEASUREMENT = "6435952a-982e-4761-9eaa-bbb072119496";
    public static String LUMITY_SERVICE = "0000fe60-0000-1000-8000-00805f9b34fb";
    public static String LUMITY_MEASUREMENT = "0000fe62-0000-1000-8000-00805f9b34fb";
    public static String ZENTEST_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String ZENTEST_MEASUREMENT = "0000ffe4-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static UUID UUID_COMARK_INTERMEDIATE_MEASUREMENT =
            UUID.fromString(COMARK_INTERMEDIATE_MEASUREMENT);

    public final static UUID UUID_BLUE2_MEASUREMENT =
            UUID.fromString(BLUE2_MEASUREMENT);
    public final static UUID UUID_KWIKSWITCH_MEASUREMENT =
            UUID.fromString(KWIKSWITCH_MEASUREMENT);
    public final static UUID UUID_CHEFSMART_MEASUREMENT =
            UUID.fromString(CHEFSMART_MEASUREMENT);
    public final static UUID UUID_MFT_MEASUREMENT =
            UUID.fromString(MFT_MEASUREMENT);
    public final static UUID UUID_LUMITY_MEASUREMENT =
            UUID.fromString(LUMITY_MEASUREMENT);

    public final static UUID UUID_ZENTEST_MEASUREMENT =
            UUID.fromString(ZENTEST_MEASUREMENT);
    private static final String LIST_NAME = "NAME";
    private static final String LIST_UUID = "UUID";

    private HashMap<String, String> attributes = new HashMap();
    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    public void setBluetoothServiceConnectCallBack(BluetoothServiceConnectCallBack bluetoothServiceConnectCallBack) {
        this.bluetoothServiceConnectCallBack = bluetoothServiceConnectCallBack;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(BluetoothLeService.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                mBluetoothGatt.discoverServices();
                bluetoothServiceConnectCallBack.onConnected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                bluetoothServiceConnectCallBack.onDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                displayGattServices(mBluetoothGatt.getServices());
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.w(TAG, "onCharacteristicRead : ");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, gatt.getDevice().getName());
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic, gatt.getDevice().getName());
        }
    };
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<>();

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid;
        mGattCharacteristics = new ArrayList<>();
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();

            if (lookup(uuid) != null) {
                ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                        new ArrayList<HashMap<String, String>>();
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                ArrayList<BluetoothGattCharacteristic> charas =
                        new ArrayList<BluetoothGattCharacteristic>();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    if (lookup(uuid) != null) {
                        charas.add(gattCharacteristic);
                        HashMap<String, String> currentCharaData = new HashMap<String, String>();
                        currentCharaData.put(LIST_NAME, lookup(uuid));
                        currentCharaData.put(LIST_UUID, uuid);
                        gattCharacteristicGroupData.add(currentCharaData);
                    }

                }
                mGattCharacteristics.add(charas);
            }
        }
        if (mGattCharacteristics != null && !mGattCharacteristics.isEmpty()) {
            final BluetoothGattCharacteristic characteristic =
                    mGattCharacteristics.get(0).get(0);
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) == 2) {
                if (mNotifyCharacteristic != null) {
                    setCharacteristicNotification(mNotifyCharacteristic, false);
                    mNotifyCharacteristic = null;
                }
                readCharacteristic(characteristic);
            }else {
                mNotifyCharacteristic = characteristic;
                setCharacteristicNotification(
                        characteristic, true);
            }
        }
    }
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    @SuppressLint("MissingPermission")
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.w(TAG, "setCharacteristicNotification "  + enabled);
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if (UUID_COMARK_INTERMEDIATE_MEASUREMENT.equals(characteristic.getUuid()) || UUID_CHEFSMART_MEASUREMENT.equals(characteristic.getUuid()) || UUID_KWIKSWITCH_MEASUREMENT.equals(characteristic.getUuid()) || UUID_MFT_MEASUREMENT.equals(characteristic.getUuid()) || UUID_LUMITY_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
            if (descriptor != null){
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        }
        if(UUID_BLUE2_MEASUREMENT.equals(characteristic.getUuid())){
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);


            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic, String deviceName) {
        final Intent intent = new Intent(action);
        double temperatureMeasurementValue = 0.0;
        double tpmValue = 0.0;
        double phValue = 0.0;
        boolean isUUIDMatchExistDevice = false;
        String chefsmartNameConstain = "Chefsmart-100";
        Log.d("cuongpq UUID " , characteristic.getUuid().toString());
        if (characteristic.getUuid().equals(UUID_MFT_MEASUREMENT)) {
            isUUIDMatchExistDevice = true;
            int statusValue = characteristic.getValue()[4];

            temperatureMeasurementValue = TemperatureMeasurements
                    .toDouble(characteristic.getValue(), TemperatureMeasurements.ProbeType.MFT);
            Log.i(TAG, String.format("Live temperature measurement value: %.1f",
                    temperatureMeasurementValue));

        } else if (characteristic.getUuid().equals(UUID_BLUE2_MEASUREMENT)) {
            isUUIDMatchExistDevice = true;
            temperatureMeasurementValue = TemperatureMeasurements
                    .toDouble(characteristic.getValue(), TemperatureMeasurements.ProbeType.BLUE2);
            Log.i(TAG, String.format("Live temperature measurement value: %.1f",
                    temperatureMeasurementValue));
        } else if (/*characteristic.getUuid().equals(UUID_COMARK_MEASUREMENT)
                    || */characteristic.getUuid().equals(UUID_COMARK_INTERMEDIATE_MEASUREMENT)) {
            isUUIDMatchExistDevice = true;
            temperatureMeasurementValue = TemperatureMeasurements
                    .toDouble(characteristic.getValue(), TemperatureMeasurements.ProbeType.COMARK);
            Log.i(TAG, String.format("Live temperature measurement value: %.1f",
                    temperatureMeasurementValue));
        } else if (characteristic.getUuid().equals(UUID_CHEFSMART_MEASUREMENT) && deviceName.contains(chefsmartNameConstain)) {
            isUUIDMatchExistDevice = true;
            temperatureMeasurementValue = TemperatureMeasurements
                    .toDouble(characteristic.getValue(), TemperatureMeasurements.ProbeType.CHEFSMART);
        } else if (characteristic.getUuid().equals(UUID_KWIKSWITCH_MEASUREMENT)) {
            isUUIDMatchExistDevice = true;
            boolean isKwikswitchAutoCapture = false;

            temperatureMeasurementValue = TemperatureMeasurements
                    .toDouble(characteristic.getValue(), TemperatureMeasurements.ProbeType.KWIKSWITCH);
            Log.i(TAG, String.format("Live temperature measurement value: %.1f",
                    temperatureMeasurementValue));

        } else if (characteristic.getUuid().equals(UUID_LUMITY_MEASUREMENT)) {
            isUUIDMatchExistDevice = true;
            temperatureMeasurementValue = TemperatureMeasurements
                    .toDouble(characteristic.getValue(), TemperatureMeasurements.ProbeType.LUMITY);
            Log.i(TAG, String.format("Live temperature measurement value: %.1f",
                    temperatureMeasurementValue));
            tpmValue = TemperatureMeasurements
                    .toTpmDouble(characteristic.getValue(), TemperatureMeasurements.ProbeType.LUMITY);
            Log.i(TAG, String.format("Live tpm measurement value: %.1f",
                    tpmValue));

        } else if (characteristic.getUuid().equals(UUID_ZENTEST_MEASUREMENT)) {
            isUUIDMatchExistDevice = true;
            temperatureMeasurementValue = TemperatureMeasurements.toDouble(characteristic.getValue(), TemperatureMeasurements.ProbeType.ZENTEST);
            phValue = TemperatureMeasurements.getPHValueZenTest(characteristic.getValue());
            Log.i(TAG, String.format("Live temperature measurement value: %.1f", temperatureMeasurementValue));
            Log.i(TAG, String.format("Live ph value: %.1f", phValue));
            String hexValue = TemperatureMeasurements.bytesToHex(characteristic.getValue());

        }
        if (isUUIDMatchExistDevice && temperatureMeasurementValue != TemperatureMeasurements.TEMP_NONE_VALUE) {
            Log.d(TAG, String.format("Received temp: %f", temperatureMeasurementValue));
            intent.putExtra(EXTRA_DATA, temperatureMeasurementValue);
            sendBroadcast(intent);
        }
        bluetoothServiceConnectCallBack.onPushTempValue(temperatureMeasurementValue);
    }
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        if (address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(BluetoothLeService.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return mBluetoothGatt.connect();
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        mBluetoothGatt = device.connectGatt(BluetoothLeService.this, false, mGattCallback);

        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        return true;
    }
    public String lookup(String uuid) {
        String name = attributes.get(uuid);
        return name == null ? null : name;
    }
    public boolean initialize() {
        attributes.put(COMARK_INTERMEDIATE_MEASUREMENT,"CoMark Intermediate Measurement");
        attributes.put(MFT_SERVICE,"Mft Service");
        attributes.put(MFT_MEASUREMENT,"Mft Measurement");
        attributes.put(BLUE2_SERVICE,"Blue2 Service");
        attributes.put(BLUE2_MEASUREMENT,"Blue2 Measurement");
        attributes.put(KWIKSWITCH_SERVICE,"Kwikswitch Service");
        attributes.put(KWIKSWITCH_MEASUREMENT,"Kwikswitch Measurement");
        attributes.put(CHEFSMART_SERVICE,"Chefsmart Service");
        attributes.put(CHEFSMART_MEASUREMENT,"Chefsmart Measurement");
        attributes.put(LUMITY_SERVICE,"Lumity Service");
        attributes.put(LUMITY_MEASUREMENT,"Lumity Measurement");
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    public interface BluetoothServiceConnectCallBack {
        void onConnected();
        void onDisconnected();
        void onPushTempValue(double temp);
    }
}
