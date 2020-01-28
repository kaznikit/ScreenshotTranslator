package com.example.screenshottranslator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH


class MainActivity : AppCompatActivity() {

    var bootReceiver : BootReceiver? = null
    var actionName = "createScreenshot"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bootReceiver = BootReceiver()
        registerReceiver(bootReceiver, IntentFilter(actionName))

        setNotification()
    }


    fun setNotification() {
        var pressIntent = Intent()
        pressIntent.action = actionName
        var pendingScreenshot = PendingIntent.getBroadcast(this, 111, pressIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        var notifChannel = NotificationChannel("channel", "My Notif", NotificationManager.IMPORTANCE_HIGH)

        notificationManager.createNotificationChannel(notifChannel)

        var notification = NotificationCompat.Builder(this, "channel")
            .setPriority(PRIORITY_HIGH)
            .setContentText("hui")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Screenshot")
            .addAction(R.drawable.ic_launcher_background, "Screenshot", pendingScreenshot)
            .build()

        notificationManager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bootReceiver)
    }
}
