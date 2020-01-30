package com.example.screenshottranslator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent.*
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import kotlin.math.abs

class BootReceiver : BroadcastReceiver() {

    var dx = 0
    var dy = 0

    var prevX = 0
    var prevY = 0
    var isViewScaling = false

    override fun onReceive(context: Context?, intent: Intent?) {
        var action = intent?.action
        if (action == "createScreenshot") {
            //закрыть статус бар
            var it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context?.sendBroadcast(it)


            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.CENTER

            val wm =
                context?.getSystemService(WINDOW_SERVICE) as WindowManager?
            val inflater =
                context?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val myView: View = inflater!!.inflate(R.layout.screenshot, null)

            myView.setOnTouchListener { view, motion ->

                if (view.id != R.id.screenshot_frame) {
                    false
                }

                when (motion.action) {
                    ACTION_MOVE -> {
                        if(isViewScaling){
                            /*params.width += 20//abs(view.x + motion.x).toInt()
                            params.height += 20//abs(view.y + motion.y).toInt()
                            wm!!.updateViewLayout(myView, params)*/

                            view.scaleX = 1.5f
                            view.scaleY = 1.5f
                        }
                        else {
                            view.x = motion.x - dx
                            view.y = motion.y - dy
                        }
                    }
                    ACTION_UP -> {
                        isViewScaling = motion.x.toInt() == prevX && motion.y.toInt() == prevY
                    }
                    ACTION_DOWN -> {
                        dx = (motion.x - view.x).toInt()
                        dy = (motion.y - view.y).toInt()

                        prevX = motion.x.toInt()
                        prevY = motion.y.toInt()
                    }
                }

                true
            }

            wm!!.addView(myView, params)
        }
    }
}