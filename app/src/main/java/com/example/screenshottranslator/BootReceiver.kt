package com.example.screenshottranslator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        var action = intent?.action
        if(action == "createScreenshot"){
            val windowManager =
                context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val layoutParams =
                WindowManager.LayoutParams(WindowManager.LayoutParams.FIRST_SUB_WINDOW)
            layoutParams.width = 300
            layoutParams.height = 300

            layoutParams.format = PixelFormat.RGBA_8888
            layoutParams.flags = (WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
            //layoutParams.token = window.decorView.rootView.windowToken

            var mTestView = View(context)
            mTestView?.setBackgroundColor(Color.RED)

        /*    mTestView?.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackPressed()
                }
                true
            }*/
            windowManager.addView(mTestView, layoutParams)
        }
    }
}