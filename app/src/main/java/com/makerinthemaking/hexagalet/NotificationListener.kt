package com.makerinthemaking.hexagalet

import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import com.makerinthemaking.hexagalet.constants.Constants


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
            messageIntent.putExtra(GaletService.EXTRA_MSG, "2")
        }
        else
        {
            messageIntent.putExtra(GaletService.EXTRA_MSG, "1")
        }
        startService(messageIntent);
    }

}