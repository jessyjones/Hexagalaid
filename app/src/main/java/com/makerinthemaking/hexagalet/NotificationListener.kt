package com.makerinthemaking.hexagalet

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {

    override fun onCreate() {
        Log.e("Hexagalet", "Listener onCreate")
        super.onCreate()
    }
    override fun onListenerConnected(): Unit
    {
        Log.e("Hexagalet", "Listener connected")
        //getActiveNotifications();
        val notifications: Array<StatusBarNotification> = getActiveNotifications()
        Log.e("Listener service", "Got " + notifications.size + " notifications")
        for (i in notifications)
        {
            Log.e("Hexagalet", i.packageName)
            Log.e("Hexagalet", i.notification.toString())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification): Unit{
        Log.e("Listener","got notification from "+ sbn.packageName + " at " +sbn.postTime)
    }

}