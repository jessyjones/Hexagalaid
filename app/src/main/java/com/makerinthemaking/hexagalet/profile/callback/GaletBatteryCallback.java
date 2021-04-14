package com.makerinthemaking.hexagalet.profile.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface GaletBatteryCallback {

    /**
     * Called when the data has been sent to the connected device.
     *
     * @param device the target device.
     * @param on true when LED was enabled, false when disabled.
     */
    void onBatteryStateChanged(@NonNull final BluetoothDevice device, final int value);
}
