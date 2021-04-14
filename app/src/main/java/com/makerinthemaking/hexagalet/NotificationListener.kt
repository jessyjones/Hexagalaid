package com.makerinthemaking.hexagalet

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi

/*
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
        Log.i("Listener","got notification from "+ sbn.packageName + " at " +sbn.postTime)
    }

}*/