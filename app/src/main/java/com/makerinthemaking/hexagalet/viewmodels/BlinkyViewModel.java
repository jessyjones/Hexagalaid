package com.makerinthemaking.hexagalet.viewmodels;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import com.makerinthemaking.hexagalet.adapter.DiscoveredBluetoothDevice;
import com.makerinthemaking.hexagalet.profile.BlinkyManager;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;

public class BlinkyViewModel extends AndroidViewModel {
    private final BlinkyManager blinkyManager;
    private BluetoothDevice device;

    public BlinkyViewModel(@NonNull final Application application) {
        super(application);

        // Initialize the manager.
        blinkyManager = new BlinkyManager(getApplication());
    }

    public LiveData<ConnectionState> getConnectionState() {
        return blinkyManager.getState();
    }

    public LiveData<Boolean> getButtonState() {
        return blinkyManager.getButtonState();
    }

    public LiveData<Boolean> getLedState() {
        return blinkyManager.getLedState();
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
            final LogSession logSession = Logger
                    .newSession(getApplication(), null, target.getAddress(), target.getName());
            blinkyManager.setLogger(logSession);
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
            blinkyManager.connect(device)
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
        blinkyManager.disconnect().enqueue();
    }

    /**
     * Sends a command to turn ON or OFF the LED on the nRF5 DK.
     *
     * @param on true to turn the LED on, false to turn it OFF.
     */
    public void setLedState(final boolean on) {
        blinkyManager.turnLed(on);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (blinkyManager.isConnected()) {
            disconnect();
        }
    }
}
