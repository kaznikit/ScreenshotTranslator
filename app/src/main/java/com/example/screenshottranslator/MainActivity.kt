package com.example.screenshottranslator

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.session.MediaSession
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH


class MainActivity : AppCompatActivity() {

    var bootReceiver: BootReceiver? = null
    var actionName = "createScreenshot"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkDrawOverlayPermission()

        bootReceiver = BootReceiver()
        registerReceiver(bootReceiver, IntentFilter(actionName))

        setNotification()
    }


    fun setNotification() {

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager


        /*var notificationIntent = Intent(this, MainActivity::class.java)
        var contentIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )*/


        /**
         * action intent
         */
        var pressIntent = Intent(actionName)
        var pendingScreenshot = PendingIntent.getBroadcast(
            this, 111, pressIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )


      /*  var remoteView = RemoteViews(packageName, R.layout.notification)
        remoteView.setInt(R.id.notification_button, "takeScreen", 1)
*/

        /**
         * notification channel
         */
        var notifChannel =
            NotificationChannel("channel", "My Notif", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notifChannel)

        /**
         * notification builder
         */
        var notification = NotificationCompat.Builder(this, "channel")
            .setPriority(PRIORITY_HIGH)
            //.setCustomContentView(remoteView)
            .setSmallIcon(R.mipmap.ic_launcher)
            .addAction(R.drawable.ic_scissors, "Translate", pendingScreenshot)
            .build()

        notificationManager.notify(1, notification)
    }



    private var REQUEST_CODE = 101

    private fun checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            var intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"))
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                // continue here - permission was granted
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bootReceiver)
    }
}
