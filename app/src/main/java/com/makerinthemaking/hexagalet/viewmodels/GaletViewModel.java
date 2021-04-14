package com.makerinthemaking.hexagalet.viewmodels;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.makerinthemaking.hexagalet.adapter.DiscoveredBluetoothDevice;
import com.makerinthemaking.hexagalet.profile.GaletManager;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;

public class GaletViewModel extends AndroidViewModel {

    private final GaletManager galetManager;
    private BluetoothDevice device;

    public GaletViewModel(@NonNull final Application application) {
        super(application);

        // Initialize the manager.
        galetManager = new GaletManager(getApplication());
    }

    public LiveData<ConnectionState> getConnectionState() {
        return galetManager.getState();
    }


    public LiveData<Integer> getBatteryState() {
        return galetManager.getBatteryState();
    }

    /**
     * Connect to the given peripheral.
     *
     * @param target the target device.
     */
    public void connect(@NonNull final DiscoveredBluetoothDevice target) {
        // Prevent from calling again when called again (screen orientation changed).
        if (device == null) {
            device = target.getDevice();
            reconnect();
        }
    }

    /**
     * Reconnects to previously connected device.
     * If this device was not supported, its services were cleared on disconnection, so
     * reconnection may help.
     */
    public void reconnect() {
        if (device != null) {
            galetManager.connect(device)
                    .retry(3, 100)
                    .useAutoConnect(false)
                    .enqueue();
        }
    }

    /**
     * Disconnect from peripheral.
     */
    private void disconnect() {
        device = null;
        galetManager.disconnect().enqueue();
    }

    public void setPixel(final boolean on) {
        // TODO turn pixel on
        galetManager.sendStuff("4");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (galetManager.isConnected()) {
            disconnect();
        }
    }
}