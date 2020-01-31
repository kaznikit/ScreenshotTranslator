package com.example.screenshottranslator

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.MotionEvent.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.screenshot.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class BootReceiver : BroadcastReceiver() {

    var dx = 0
    var dy = 0

    var frameDx = 0
    var frameDy = 0

    var prevX = 0
    var prevY = 0
    var isViewScaling = false

    var frameLayout: FrameLayout? = null

    var packageName: String? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        var action = intent?.action
        if (action == "createScreenshot") {
            packageName = context!!.packageName

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

            myView.screenshot_imageview.setOnTouchListener { view, motion ->

                if (view.id != myView.screenshot_imageview.id || myView.screenshot_imageview.visibility == GONE) {
                    false
                }

                when (motion.action) {
                    ACTION_MOVE -> {
                        view.x = motion.rawX - dx
                        view.y = motion.rawY - dy
                    }
                    ACTION_UP -> {
                        myView.screenshot_frame.visibility = VISIBLE
                        myView.screenshot_imageview.visibility = GONE

                        myView.screenshot_frame.left = motion.rawX.toInt() - dx
                        myView.screenshot_frame.top = motion.rawY.toInt() - dy

                        prevY = myView.screenshot_frame.top
                        prevX = myView.screenshot_frame.left
                    }
                    ACTION_DOWN -> {
                        dx = (motion.rawX - view.x).toInt()
                        dy = (motion.rawY - view.y).toInt()
                    }
                }
                true
            }

            myView.screenshot_frame.setOnTouchListener { v, event ->
                if (v.id != myView.screenshot_frame.id || myView.screenshot_frame.visibility == GONE) {
                    false
                }

                when (event.action) {
                    ACTION_MOVE -> {
                        v.left = prevX
                        v.top = prevY

                        v.layoutParams.width = (event.rawX - v.left).toInt()
                        v.layoutParams.height = (event.rawY - v.top).toInt()
                        v.requestLayout()
                    }
                    ACTION_DOWN -> {
                        frameDx = (event.rawX - v.left).toInt()
                        frameDy = (event.rawY - v.top).toInt()
                    }
                    ACTION_UP -> {

                        var image =
                            getBitmapFromView(myView.screenshot_frame, context as Activity) {

                                var textRecognizer = TextRecognizer.Builder(context).build()
                                var imageFrame = Frame.Builder().setBitmap(it).build()

                                var textBlocks = textRecognizer.detect(imageFrame)
                                var str = ""
                                for(i in 0 until textBlocks.size()){
                                    var textBlock = textBlocks.get(textBlocks.keyAt(i))
                                    str = textBlock.value
                                }

                                Log.d("gg", str)
                                /*try {
                                    var directory = context!!.filesDir
                                    var file = getOutputMediaFile()//File(directory, "testBitmap")
                                    var fos = FileOutputStream(file)
                                    it.compress(Bitmap.CompressFormat.PNG, 100, fos)
                                    fos.flush()
                                    fos.close()
                                    *//*.use { out ->
                                    it.compress(
                                        Bitmap.CompressFormat.PNG,
                                        100,
                                        out
                                    ) // bmp is your Bitmap instance
                                }*//*
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }*/
                            }//Bitmap.createBitmap(myView.screenshot_frame.width, myView.screenshot_frame.height, Bitmap.Config.ARGB_8888)

                        myView.screenshot_frame.visibility = GONE
                        myView.screenshot_imageview.visibility = VISIBLE

                        myView.screenshot_imageview.left = event.rawX.toInt() //- frameDx
                        myView.screenshot_imageview.top = event.rawY.toInt() //- frameDy
                    }
                }
                true
            }

            wm!!.addView(myView, params)
        }
    }

    /** Create a File for saving an image or video  */
    private fun getOutputMediaFile(): File? { // To be safe, you should check that the SDCard is mounted
// using Environment.getExternalStorageState() before doing this.
        val mediaStorageDir = File(
            Environment.getExternalStorageDirectory()
                .toString() + "/Android/data/"
                    + packageName
                    + "/Files"
        )
        // This location works best if you want the created images to be shared
// between applications and persist after your app has been uninstalled.
// Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        // Create a media file name
        val timeStamp: String = SimpleDateFormat("ddMMyyyy_HHmm").format(Date())
        val mediaFile: File
        val mImageName = "MI_$timeStamp.jpg"
        mediaFile = File(mediaStorageDir.path + File.separator + mImageName)
        return mediaFile
    }

    fun getBitmapFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
        activity.window?.let { window ->
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val locationOfViewInWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewInWindow)
            try {
                PixelCopy.request(
                    window,
                    Rect(
                        locationOfViewInWindow[0],
                        locationOfViewInWindow[1],
                        locationOfViewInWindow[0] + view.width,
                        locationOfViewInWindow[1] + view.height
                    ),
                    bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            callback(bitmap)
                        }
                        // possible to handle other result codes ...
                    },
                    Handler()
                )
            } catch (e: IllegalArgumentException) {
                // PixelCopy may throw IllegalArgumentException, make sure to handle it
                e.printStackTrace()
            }
        }
    }
}