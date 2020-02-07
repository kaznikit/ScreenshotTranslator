package com.example.screenshottranslator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Display
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(), AreaReceivedListener {

    var bootReceiver: BootReceiver? = null
    var actionName = "createScreenshot"

    private var mProjectionManager: MediaProjectionManager? = null

    private val TAG: String = "screen"

    private var STORE_DIRECTORY: String? = null

    private var mImageReader: ImageReader? = null
    var mHandler: Handler? = null
    private var mDisplay: Display? = null
    var mVirtualDisplay: VirtualDisplay? = null
    var mDensity = 0
    var mWidth = 0
    var mHeight = 0
    private val SCREENCAP_NAME = "screencap"
    private val VIRTUAL_DISPLAY_FLAGS =
        DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
    private var mOrientationChangeCallback: OrientationChangeCallback? = null
    private var IMAGES_PRODUCED = 0
    private var sMediaProjection: MediaProjection? = null

    private var PERMISSION_REQUEST_CODE = 101
    private val SCREEN_REQUEST = 100
    var screenIntent : Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkDrawOverlayPermission()

        bootReceiver = BootReceiver(this)
        registerReceiver(bootReceiver, IntentFilter(actionName))

        /**
         * Button for starting obtaining projection
         */
        turn_on_record_button.setOnClickListener {
            mProjectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            //screenIntent = mProjectionManager!!.createScreenCaptureIntent()
            ScreenshotTest(this, mProjectionManager!!, SCREEN_REQUEST).startProjection()
        }
        setNotification()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                // continue here - permission was granted
            }
        } else if (requestCode == SCREEN_REQUEST) {
            sMediaProjection = mProjectionManager!!.getMediaProjection(resultCode, data!!)
            if (sMediaProjection != null) {
                val externalFilesDir: File? = getExternalFilesDir(null)
                if (externalFilesDir != null) {
                    STORE_DIRECTORY =
                        externalFilesDir.absolutePath.toString() + "/screenshots/"
                    val storeDirectory = File(STORE_DIRECTORY)
                    if (!storeDirectory.exists()) {
                        val success: Boolean = storeDirectory.mkdirs()
                        if (!success) {
                            Log.e(TAG, "failed to create file storage directory.")
                            return
                        }
                    }
                } else {
                    Log.e(
                        TAG,
                        "failed to create file storage directory, getExternalFilesDir is null."
                    )
                    return
                }

                // display metrics
                val metrics = resources.displayMetrics
                mDensity = metrics.densityDpi
                mDisplay = windowManager.defaultDisplay

                mWidth = mDisplay!!.width
                mHeight = mDisplay!!.height

                createVirtualDisplay()

                // register orientation change callback
                mOrientationChangeCallback =
                    OrientationChangeCallback(this, mDisplay!!, mVirtualDisplay!!, mImageReader!!) {
                        createVirtualDisplay()
                    }
                if (mOrientationChangeCallback!!.canDetectOrientation()) {
                    mOrientationChangeCallback!!.enable()
                }
                // register media projection stop callback
                sMediaProjection!!.registerCallback(MediaProjectionStopCallback(), mHandler)
            }
        }
    }

    fun createVirtualDisplay() {
        // start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 1)
        mVirtualDisplay = sMediaProjection!!.createVirtualDisplay(
            SCREENCAP_NAME,
            mWidth,
            mHeight,
            mDensity,
            VIRTUAL_DISPLAY_FLAGS,
            mImageReader!!.surface,
            null,
            mHandler)

        mImageReader!!.setOnImageAvailableListener(
            ImageAvailableListener(
                STORE_DIRECTORY!!,
                IMAGES_PRODUCED), mHandler)
        IMAGES_PRODUCED++
    }

    override fun areaReceived(width: Int, height: Int) {
        mWidth = width
        mHeight = height
        //ScreenshotTest(this, mProjectionManager!!, SCREEN_REQUEST).startProjection()
        //startActivityForResult(screenIntent, SCREEN_REQUEST)
    }


    fun setNotification() {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        /**
         * action intent
         */
        var pressIntent = Intent(actionName)
        var pendingScreenshot = PendingIntent.getBroadcast(
            this, 111, pressIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)

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

    private fun checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            var intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, PERMISSION_REQUEST_CODE)
        }
    }

    private class MediaProjectionStopCallback : MediaProjection.Callback() {
        override fun onStop() {
            Log.e("ScreenCapture", "stopping projection.")

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bootReceiver)

        if (sMediaProjection != null) {
            sMediaProjection!!.stop()
        }
    }
}
