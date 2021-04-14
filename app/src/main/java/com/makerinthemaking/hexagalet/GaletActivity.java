package com.makerinthemaking.hexagalet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.makerinthemaking.hexagalet.adapter.DiscoveredBluetoothDevice;
import com.makerinthemaking.hexagalet.viewmodels.BlinkyViewModel;
import com.makerinthemaking.hexagalet.viewmodels.GaletViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;


@SuppressWarnings("ConstantConditions")

public class GaletActivity extends AppCompatActivity {
    public static final String EXTRA_DEVICE = "no.nordicsemi.android.blinky.EXTRA_DEVICE";

    private GaletViewModel viewModel;

    @BindView(R.id.galet_led_switch)
    SwitchMaterial led;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blinky);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        final DiscoveredBluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
        final String deviceName = device.getName();
        final String deviceAddress = device.getAddress();

        final MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(deviceName != null ? deviceName : getString(R.string.unknown_device));
        toolbar.setSubtitle(deviceAddress);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configure the view model.
        viewModel = new ViewModelProvider(this).get(GaletViewModel.class);
        viewModel.connect(device);

        // Set up views.
        final TextView ledState = findViewById(R.id.led_state);
        final LinearLayout progressContainer = findViewById(R.id.progress_container);
        final TextView connectionState = findViewById(R.id.connection_state);
        final View content = findViewById(R.id.device_container);
        final View notSupported = findViewById(R.id.not_supported);
        led = findViewById(R.id.led_switch);

        led.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.setPixel(true));
        viewModel.getConnectionState().observe(this, state -> {
            switch (state.getState()) {
                case CONNECTING:
                    progressContainer.setVisibility(View.VISIBLE);
                    notSupported.setVisibility(View.GONE);
                    connectionState.setText(R.string.state_connecting);
                    break;
                case INITIALIZING:
                    connectionState.setText(R.string.state_initializing);
                    break;
                case READY:
                    progressContainer.setVisibility(View.GONE);
                    content.setVisibility(View.VISIBLE);
                    onConnectionStateChanged(true);
                    break;
                case DISCONNECTED:
                    if (state instanceof ConnectionState.Disconnected) {
                        final ConnectionState.Disconnected stateWithReason = (ConnectionState.Disconnected) state;
                        if (stateWithReason.isNotSupported()) {
                            progressContainer.setVisibility(View.GONE);
                            notSupported.setVisibility(View.VISIBLE);
                        }
                    }
                    // fallthrough
                case DISCONNECTING:
                    onConnectionStateChanged(false);
                    break;
            }
        });
    }

    @OnClick(R.id.action_clear_cache)
    public void onTryAgainClicked() {
        viewModel.reconnect();
    }

    private void onConnectionStateChanged(final boolean connected) {
  //      led.setEnabled(connected);
        if (!connected) {

            // TODO : should we do smt ?
        }
    }
}
