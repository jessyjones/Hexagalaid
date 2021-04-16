package com.makerinthemaking.hexagalet.services

import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import com.makerinthemaking.hexagalet.constants.Constants
import com.makerinthemaking.hexagalet.services.GaletService


class NotificationListener : NotificationListenerService() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        Log.i("Hexagalet", "Listener onCreate")
        super.onCreate()
    }
    override fun onListenerConnected(): Unit
    {
        Log.i("Hexagalet", "Listener connected")
        //getActiveNotifications();
        val notifications: Array<StatusBarNotification> = getActiveNotifications()
        Log.i("Listener service", "Got " + notifications.size + " notifications")
        for (i in notifications)
        {
            Log.i("Hexagalet", i.packageName)
            Log.i("Hexagalet", i.notification.toString())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification): Unit{
        Log.i("Listener", "got notification from " + sbn.packageName + " at " + sbn.postTime)
        val messageIntent = Intent(this, GaletService::class.java)
        messageIntent.action = Constants.SENDTEXT
        if(sbn.packageName.equals("com.Slack"))
        {
            messageIntent.putExtra(GaletService.EXTRA_MSG, "f:0000ff:00ff00/")
        }
        else if(sbn.packageName.equals("com.zulipmobile"))
        {
            messageIntent.putExtra(GaletService.EXTRA_MSG, "f:0000ff:00ff00/")
        }
        else
        {
            messageIntent.putExtra(GaletService.EXTRA_MSG, "f:ff00ff:00ff00/")
        }
        startService(messageIntent);
    }

}