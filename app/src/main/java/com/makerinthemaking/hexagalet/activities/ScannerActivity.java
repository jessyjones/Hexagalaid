package com.makerinthemaking.hexagalet.activities;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.android.material.appbar.MaterialToolbar;
import com.makerinthemaking.hexagalet.R;
import com.makerinthemaking.hexagalet.adapter.DevicesAdapter;
import com.makerinthemaking.hexagalet.adapter.DiscoveredBluetoothDevice;
import com.makerinthemaking.hexagalet.constants.Constants;
import com.makerinthemaking.hexagalet.services.GaletService;
import com.makerinthemaking.hexagalet.utils.Utils;
import com.makerinthemaking.hexagalet.viewmodels.ScannerStateLiveData;
import com.makerinthemaking.hexagalet.viewmodels.ScannerViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScannerActivity extends AppCompatActivity implements DevicesAdapter.OnItemClickListener {
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1022; // random number

    private ScannerViewModel scannerViewModel;

    @BindView(R.id.state_scanning) View scanningView;
    @BindView(R.id.no_devices) View emptyView;
    @BindView(R.id.no_location_permission) View noLocationPermissionView;
    @BindView(R.id.action_grant_location_permission) Button grantPermissionButton;
    @BindView(R.id.action_permission_settings) Button permissionSettingsButton;
    @BindView(R.id.no_location) View noLocationView;
    @BindView(R.id.bluetooth_off) View noBluetoothView;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        ButterKnife.bind(this);

        final MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        noLocationPermissionView = findViewById(R.id.no_location_permission);
        scanningView = findViewById(R.id.state_scanning);
        emptyView = findViewById(R.id.no_devices);
        noLocationPermissionView  = findViewById(R.id.no_location_permission);
        grantPermissionButton = findViewById(R.id.action_grant_location_permission);
        permissionSettingsButton = findViewById(R.id.action_permission_settings);
        noLocationPermissionView.setOnClickListener(this::onClick);
        scanningView.setOnClickListener(this::onClick);
        emptyView.setOnClickListener(this::onClick);
        noLocationView = findViewById(R.id.no_location);
        noBluetoothView = findViewById(R.id.bluetooth_off);

        // Create view model containing utility methods for scanning
        scannerViewModel = new ViewModelProvider(this).get(ScannerViewModel.class);
        scannerViewModel.getScannerState().observe(this, this::startScan);

        // Configure the recycler view
        final RecyclerView recyclerView = findViewById(R.id.recycler_view_ble_devices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        final RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        scannerViewModel.filterByUuid(true);
        final DevicesAdapter adapter = new DevicesAdapter(this, scannerViewModel.getDevices());
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        clear();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScan();
    }

    @Override
    public void onItemClick(@NonNull final DiscoveredBluetoothDevice device) {

        Log.d("ScannerActivity", "Launching");

        final Intent i = new Intent(this, GaletService.class);
        i.putExtra(GaletActivity.EXTRA_DEVICE, device);
        i.setAction(Constants.STARTFOREGROUND_ACTION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i);
        } else {
            startService(i);
        }

        finish();
    }

    @Override
    public void onClick(View view) {
        if (view.equals(permissionSettingsButton))
        {
            onGrantLocationPermissionClicked();
        } else if (view.equals(findViewById(R.id.action_enable_bluetooth)))
        {
            onEnableBluetoothClicked();
        } else if (view.equals(findViewById(R.id.action_enable_location)))
        {
            onEnableLocationClicked();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            scannerViewModel.refresh();
        }
    }

    public void onEnableLocationClicked() {
        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    public void onEnableBluetoothClicked() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableIntent);
    }

    public void onGrantLocationPermissionClicked() {
        Utils.markLocationPermissionRequested(this);
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_ACCESS_FINE_LOCATION);
    }

    public void onPermissionSettingsClicked() {
        final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    /**
     * Start scanning for Bluetooth devices or displays a message based on the scanner state.
     */
    private void startScan(final ScannerStateLiveData state) {
        // First, check the Location permission. This is required on Marshmallow onwards in order
        // to scan for Bluetooth LE devices.
        if (Utils.isLocationPermissionsGranted(this)) {
            noLocationPermissionView.setVisibility(View.GONE);

            // Bluetooth must be enabled.
            if (state.isBluetoothEnabled()) {
                noBluetoothView.setVisibility(View.GONE);

                // We are now OK to start scanning.
                if(!state.isScanning())
                {
                    scannerViewModel.startScan();
                    scanningView.setVisibility(View.VISIBLE);
                }

                if (!state.hasRecords()) {
                    emptyView.setVisibility(View.VISIBLE);

                    if (!Utils.isLocationRequired(this) || Utils.isLocationEnabled(this)) {
                        noLocationView.setVisibility(View.INVISIBLE);
                    } else {
                        noLocationView.setVisibility(View.VISIBLE);
                    }
                } else {
                    emptyView.setVisibility(View.GONE);
                }
            } else {
                noBluetoothView.setVisibility(View.VISIBLE);
                scanningView.setVisibility(View.INVISIBLE);
                emptyView.setVisibility(View.GONE);
                clear();
            }
        } else {
            noLocationPermissionView.setVisibility(View.VISIBLE);
            noBluetoothView.setVisibility(View.GONE);
            scanningView.setVisibility(View.INVISIBLE);
            emptyView.setVisibility(View.GONE);

            final boolean deniedForever = Utils.isLocationPermissionDeniedForever(this);
            grantPermissionButton.setVisibility(deniedForever ? View.GONE : View.VISIBLE);
            permissionSettingsButton.setVisibility(deniedForever ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * stop scanning for bluetooth devices.
     */
    private void stopScan() {
        scannerViewModel.stopScan();
    }

    /**
     * Clears the list of devices, which will notify the observer.
     */
    private void clear() {
        scannerViewModel.getDevices().clear();
        scannerViewModel.getScannerState().clearRecords();
    }
}