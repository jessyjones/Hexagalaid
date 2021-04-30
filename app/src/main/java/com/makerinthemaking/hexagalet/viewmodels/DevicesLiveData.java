package com.makerinthemaking.hexagalet.viewmodels;


import android.os.ParcelUuid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.makerinthemaking.hexagalet.adapter.DiscoveredBluetoothDevice;
import com.makerinthemaking.hexagalet.profile.GaletManager;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * This class keeps the current list of discovered Bluetooth LE devices matching filter.
 * Each time @{link {@link #applyFilter()} is called, the observers are notified with a new
 * list instance.
 */
@SuppressWarnings("unused")
public class DevicesLiveData extends LiveData<List<DiscoveredBluetoothDevice>> {
    private static final ParcelUuid FILTER_UUID = new ParcelUuid(GaletManager.UART_UUID_SERVICE);
    private static final int FILTER_RSSI = -50; // [dBm]

    @NonNull
    private final List<DiscoveredBluetoothDevice> devices = new ArrayList<>();
    @Nullable
    private List<DiscoveredBluetoothDevice> filteredDevices = null;
    private boolean filterUuidRequired;
    private boolean filterNearbyOnly;

    /* package */ DevicesLiveData() {
    }

    /* package */ synchronized void bluetoothDisabled() {
        devices.clear();
        filteredDevices = null;
        postValue(null);
    }


    /* package */ synchronized boolean deviceDiscovered(@NonNull final ScanResult result) {
        DiscoveredBluetoothDevice device;

        // Check if it's a new device.
        final int index = indexOf(result);
        if (index == -1) {
            device = new DiscoveredBluetoothDevice(result);
            devices.add(device);
        } else {
            device = devices.get(index);
        }

        // Update RSSI and name.
        device.update(result);

        // Return true if the device was on the filtered list or is to be added.
        return (filteredDevices != null && filteredDevices.contains(device))
                || (matchesUuidFilter(result));
    }

    /**
     * Clears the list of devices.
     */
    public synchronized void clear() {
        devices.clear();
        filteredDevices = null;
        postValue(null);
    }

    /**
     * Refreshes the filtered device list based on the filter flags.
     */
    /* package */ synchronized boolean applyFilter() {
        final List<DiscoveredBluetoothDevice> tmp = new ArrayList<>();
        for (final DiscoveredBluetoothDevice device : devices) {
            final ScanResult result = device.getScanResult();
            if (matchesUuidFilter(result)) {
                tmp.add(device);
            }
        }
        filteredDevices = tmp;
        postValue(filteredDevices);
        return !filteredDevices.isEmpty();
    }

    /* package */  boolean filterByUuid(final boolean uuidRequired) {
        filterUuidRequired = uuidRequired;
        return applyFilter();
    }

    /**
     * Finds the index of existing devices on the device list.
     *
     * @param result scan result.
     * @return Index of -1 if not found.
     */
    private int indexOf(@NonNull final ScanResult result) {
        int i = 0;
        for (final DiscoveredBluetoothDevice device : devices) {
            if (device.matches(result))
                return i;
            i++;
        }
        return -1;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    // TODO : add own filter
    private boolean matchesUuidFilter(@NonNull final ScanResult result) {
        if (!filterUuidRequired)
            return true;

        final ScanRecord record = result.getScanRecord();
        if (record == null)
            return false;

        final List<ParcelUuid> uuids = record.getServiceUuids();
        if (uuids == null)
            return false;

        return uuids.contains(FILTER_UUID);
    }


}