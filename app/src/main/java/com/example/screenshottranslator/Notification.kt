package com.example.screenshottranslator

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.notification.*

class Notification : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.notification)

        notification_button.setOnClickListener {
            //Toast.makeText(this, "Pressed!", Toast.LENGTH_LONG)
            var intent = Intent(this, Screenshot::class.java)
            startActivity(intent)
        }
    }

    fun setNotification(context : Context, isEnabled : Boolean){
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL_ID = "my_channel_id_01"

        if(isEnabled){
            val rViews = RemoteViews(context.packageName, R.layout.notification)
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)

            if(intent != null){
                val pi = PendingIntent.getActivity(context, 0, intent, 0)
                rViews.setOnClickPendingIntent(R.id.notification_button, pi)
            }

            //notification channel
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "my notif", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)
            manager.createNotificationChannel(notificationChannel)

            val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContent(rViews)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
            manager.notify(0, builder.build())
        }
        else{
            manager.cancel(1)
        }
    }
}