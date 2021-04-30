package com.makerinthemaking.hexagalet.services

import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import com.makerinthemaking.hexagalet.activities.DBHelper
import com.makerinthemaking.hexagalet.constants.Constants
import com.makerinthemaking.hexagalet.services.GaletService
import java.util.*


class NotificationListener : NotificationListenerService() {
    var mydb: DBHelper? = null
    var TAG :String = "NotificationListener";

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        Log.i(TAG, "Listener onCreate")
        mydb = DBHelper(applicationContext)
        super.onCreate()
    }
    override fun onListenerConnected(): Unit
    {
        Log.i(TAG, "Listener connected")
        //getActiveNotifications();
     /*   val notifications: Array<StatusBarNotification> = getActiveNotifications()
        Log.i("Listener service", "Got " + notifications.size + " notifications")
        for (i in notifications)
        {
            Log.i("Hexagalet", i.packageName)
            Log.i("Hexagalet", i.notification.toString())
        }*/
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.i(TAG, "got notification from ${sbn.packageName} at ${Date(sbn.postTime)}")
        val messageIntent = Intent(this, GaletService::class.java)
        messageIntent.action = Constants.SENDTEXT

        var commande:String = "f:0000ff:00ff00/"
/*
        if(sbn.packageName.equals("com.Slack"))
        {
            commande = "g:ff0063:ff0063/"
        }
        else if(sbn.packageName.equals("com.zulipmobile"))
        {
            commande = "r:00ff34:00ff34/"
        }
        else
        {
            val random = Random()
            val nextInt = random.nextInt(0xffffff + 1)
            val colorCode = String.format("#%06x", nextInt)
            commande = "d:" + colorCode +":" + colorCode + "/"
        }
*/
        //val commande: String? = this.mydb?.getCommand(sbn.packageName);
        messageIntent.putExtra(GaletService.EXTRA_MSG, commande)
        startService(messageIntent);
    }

}