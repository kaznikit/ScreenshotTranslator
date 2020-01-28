package com.example.screenshottranslator

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.notification.*


class Notification : AppCompatActivity() {

    var mTestView: View? = null

    var actionName = "createScreenshot"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification)

        notification_button.setOnClickListener {
            val windowManager =
                baseContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val layoutParams =
                WindowManager.LayoutParams(WindowManager.LayoutParams.FIRST_SUB_WINDOW)
            layoutParams.width = 300
            layoutParams.height = 300

            layoutParams.format = PixelFormat.RGBA_8888
            layoutParams.flags = (WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
            layoutParams.token = window.decorView.rootView.windowToken

            mTestView = View(this)
            mTestView?.setBackgroundColor(Color.RED)

            mTestView?.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackPressed()
                }
                true
            }
            windowManager.addView(mTestView, layoutParams)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mTestView != null) {
            val windowManager =
                baseContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (mTestView!!.isShown) {
                windowManager.removeViewImmediate(mTestView)
            }
        }
    }

    fun setNotification() {
        var pressIntent = Intent()
        pressIntent.action = actionName
        var pendingScreenshot = PendingIntent.getBroadcast(applicationContext, 111, pressIntent, FLAG_UPDATE_CURRENT)

        //var screenshotIntent = PendingIntent.getActivity(context, 0, pressIntent, 0)

        var notification = NotificationCompat.Builder(applicationContext, "s")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Screenshot")
            .addAction(R.layout.notification, "Screenshot", pendingScreenshot)
            .build()

        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }
}