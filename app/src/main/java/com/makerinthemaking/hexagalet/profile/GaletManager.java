package com.makerinthemaking.hexagalet.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.makerinthemaking.hexagalet.profile.callback.GaletBatteryDataCallback;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import no.nordicsemi.android.ble.PhyRequest;
import no.nordicsemi.android.ble.Request;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.livedata.ObservableBleManager;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;

public class GaletManager extends ObservableBleManager {

    public final static UUID UART_UUID_SERVICE = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    /** TODO Add Neopixel characteristic UUID. */
    private final static UUID LBS_UUID_PIXEL_CHAR = UUID.fromString("00001524-1212-efde-1523-785feabcd123");
    /** TODO Add Battery characteristic UUID. */
    private final static UUID LBS_UUID_BATT_CHAR = UUID.fromString("00001525-1212-efde-1523-785feabcd123");
    /** RX characteristic UUID */
    private final static UUID UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    /** TX characteristic UUID */
    private final static UUID UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    /** The maximum packet size is 20 bytes. */
    private static final int MAX_PACKET_SIZE = 20;

    private final MutableLiveData<Integer> batteryState = new MutableLiveData<>();

    private BluetoothGattCharacteristic batteryCharacteristic, pixelCharacteristic, mRXCharacteristic, mTXCharacteristic ;
    private byte[] mOutgoingBuffer;
    private int mBufferOffset;
    private LogSession logSession;
    private boolean supported;
    private boolean ledOn;

    public GaletManager(@NonNull final Context context) {
        super(context);
    }

    public final LiveData<Integer> getBatteryState() {
        return batteryState;
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new GaletManager.GaletBleManagerGattCallback();
    }

    private	final GaletBatteryDataCallback batteryCallback = new GaletBatteryDataCallback() {
        @Override
        public void onBatteryStateChanged(@NonNull final BluetoothDevice device,
                                         final int level) {
            log(LogContract.Log.Level.APPLICATION, "Battery " + (level));
        }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device,
                                          @NonNull final Data data) {
            log(Log.WARN, "Invalid data received: " + data);
        }
    };

    private class GaletBleManagerGattCallback extends BleManagerGattCallback {
        @Override
        protected void initialize() {
            // TODO add pixels
            setNotificationCallback(batteryCharacteristic).with(batteryCallback);
            readCharacteristic(batteryCharacteristic).with(batteryCallback).enqueue();
            enableNotifications(batteryCharacteristic).enqueue();


        }

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(UART_UUID_SERVICE);
            if (service != null) {
                batteryCharacteristic = service.getCharacteristic(LBS_UUID_BATT_CHAR);
                mRXCharacteristic = service.getCharacteristic(UART_RX_CHARACTERISTIC_UUID);
                mTXCharacteristic = service.getCharacteristic(UART_TX_CHARACTERISTIC_UUID);
            }
            boolean writeRequest = false;
            boolean writeCommand = false;
            if (mRXCharacteristic != null) {
                final int rxProperties = mRXCharacteristic.getProperties();
                writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
                writeCommand = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0;

                // Set the WRITE REQUEST type when the characteristic supports it. This will allow to send long write (also if the characteristic support it).
                // In case there is no WRITE REQUEST property, this manager will divide texts longer then 20 bytes into up to 20 bytes chunks.
                if (writeRequest)
                    mRXCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            }
            // TODO add real characteristics
            //supported = batteryCharacteristic != null ;
            supported = true ;
            return supported;
        }

        @Override
        protected void onDeviceDisconnected() {
    // TODO : anything to add here ?
            mRXCharacteristic = null;
            mTXCharacteristic = null;
            batteryCharacteristic = null ;
            pixelCharacteristic = null ;
        }



    }

    public void sendStuff(String message) {

        beginAtomicRequestQueue()
                .add(enableNotifications(mRXCharacteristic))
                .done(device -> log(Log.INFO, "Target initialized"))
                .enqueue();
        writeCharacteristic(mRXCharacteristic, Data.from(message))
                .split()
                .enqueue();
    }

}