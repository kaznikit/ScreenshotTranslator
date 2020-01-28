package com.example.screenshottranslator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    val notification : Notification = Notification()
    override fun onReceive(context: Context?, intent: Intent?) {
        notification.setNotification(context!!, true)
    }
}