package com.makerinthemaking.hexagalet.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.makerinthemaking.hexagalet.R;
import com.makerinthemaking.hexagalet.activities.MainActivity;
import com.makerinthemaking.hexagalet.adapter.DiscoveredBluetoothDevice;
import com.makerinthemaking.hexagalet.constants.Constants;
import com.makerinthemaking.hexagalet.profile.GaletManager;

import no.nordicsemi.android.ble.livedata.state.ConnectionState;

import static android.app.Notification.CATEGORY_SERVICE;

public class GaletService extends LifecycleService {
    private String TAG = "GaletService";
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static final String EXTRA_DEVICE = "no.nordicsemi.android.blinky.EXTRA_DEVICE";
    public static final String EXTRA_MSG = "com.makerinthemaking.hexagalet.MSG";

    private GaletManager galetManager;
    private BluetoothDevice device;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }

    public Boolean isConnected(){
        return galetManager.isConnected();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        if(galetManager == null){
            galetManager = new GaletManager(getApplication());
        }
        super.onCreate();
    }

    private void updateNotification(String state){

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent stopIntent = new Intent(this, GaletService.class);
        stopIntent.setAction(Constants.STOPFOREGROUND_ACTION);
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, 0);
        NotificationCompat.Action stopAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Disconnect", pendingStopIntent);

        Intent playIntent = new Intent(this, GaletService.class);
        playIntent.setAction(Constants.SENDHELLO);
        PendingIntent pendingPlayIntent = PendingIntent.getService(this, 0, playIntent, 0);
        NotificationCompat.Action playAction = new NotificationCompat.Action(android.R.drawable.ic_media_play, "Hi", pendingPlayIntent);

        Intent messageIntent = new Intent(this, GaletService.class);
        messageIntent.setAction(Constants.SENDTEXT);
        messageIntent.putExtra(GaletService.EXTRA_MSG, "f:0000ff:00ff00/");
        PendingIntent pendingMsgIntent = PendingIntent.getService(this, 0, messageIntent, 0);
        NotificationCompat.Action msgAction = new NotificationCompat.Action(android.R.drawable.ic_media_play, "Message", pendingMsgIntent);


        // TODO : add real icons
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(state)
                .setSmallIcon(android.R.drawable.button_onoff_indicator_on)
                .setContentIntent(pendingIntent)
                .addAction(playAction)
                .addAction(stopAction)
                .addAction(msgAction)
                .setCategory(CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent.getAction().equals(Constants.STARTFOREGROUND_ACTION)) {
            Log.d(TAG, "Service start requested");
            if(this.isConnected())
            {
                Log.d(TAG, "Already connected");
                return START_NOT_STICKY;
            }
            else {
                Log.d(TAG, "Starting service");
                super.onStartCommand(intent, flags, startId);
                createNotificationChannel();
                launchForegroundService();
                final DiscoveredBluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
                final String deviceName = device.getName() != null ? device.getName() : getString(R.string.unknown_device);
                final String deviceAddress = device.getAddress();
                this.connect(device);
                getConnectionState().observe(this, connectionStateObserver);
                return START_NOT_STICKY;
            }
        }
        else if (intent.getAction().equals(Constants.STOPFOREGROUND_ACTION)) {
            Log.d(TAG, "Stopping service");
            stopForeground(true);
            stopSelfResult(startId);
            return START_NOT_STICKY ;
        }
        else if (intent.getAction().equals(Constants.SENDHELLO))
        {
            Log.d(TAG, "Send hello");
            setPixel("w:ffffff:00ff00/");
            return START_NOT_STICKY ;
        }
        else if (intent.getAction().equals(Constants.SENDTEXT))
        {
            Log.d(TAG, "Send command");
            final String message = intent.getStringExtra(GaletService.EXTRA_MSG);
            setPixel(message);
            return START_NOT_STICKY ;
        }

        return START_NOT_STICKY;
    }

    final Observer<ConnectionState> connectionStateObserver = state -> {
        switch (state.getState()) {
            case CONNECTING:
                updateNotification("CONNECTING");
                Log.d(TAG, "CONNECTING");
                break;
            case INITIALIZING:
                updateNotification("INITIALIZING");
                Log.d(TAG, "INITIALIZING");
                break;
            case READY:
                updateNotification("READY");
                Log.d(TAG, "READY");
                setPixel("f:0000ff:00ff00/");
                break;
            case DISCONNECTED:
                updateNotification("DISCONNECTED");
                Log.d(TAG, "DISCONNECTED");
                if (state instanceof ConnectionState.Disconnected) {
                    final ConnectionState.Disconnected stateWithReason = (ConnectionState.Disconnected) state;
                    if (stateWithReason.isNotSupported()) {
                        Log.d(TAG, "DISCONNECTED");
                    }
                }
                // fallthrough
            case DISCONNECTING:
                onConnectionStateChanged(false);
                break;
        }
    };

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (galetManager.isConnected()) {
            disconnect();
        }
        super.onDestroy();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    private void launchForegroundService()
    {

    }

    public LiveData<ConnectionState> getConnectionState() {
        return galetManager.getState();
    }

    public LiveData<Integer> getBatteryState() {
        return galetManager.getBatteryState();
    }

    public void connect(@NonNull final DiscoveredBluetoothDevice target) {
        // Prevent from calling again when called again (screen orientation changed).
        if (device == null) {
            device = target.getDevice();
            reconnect();
        }
    }

    public void reconnect() {
        if (device != null) {
            galetManager.connect(device)
                    .retry(3, 100)
                    .useAutoConnect(false)
                    .enqueue();
        }
    }

    private void disconnect() {
        device = null;
        galetManager.disconnect().enqueue();
    }

    public void setPixel(final String command) {
        // TODO turn pixel on
        galetManager.sendStuff(command);
    }

    private void onConnectionStateChanged(final boolean connected) {
        //      led.setEnabled(connected);
        if (!connected) {

            // TODO : should we do smt ?
        }
    }

}
