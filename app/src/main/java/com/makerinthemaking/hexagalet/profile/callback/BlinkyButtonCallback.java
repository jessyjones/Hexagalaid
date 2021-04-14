package com.makerinthemaking.hexagalet.profile.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface BlinkyButtonCallback {

    /**
     * Called when a button was pressed or released on device.
     *
     * @param device the target device.
     * @param pressed true if the button was pressed, false if released.
     */
    void onButtonStateChanged(@NonNull final BluetoothDevice device, final boolean pressed);
}
