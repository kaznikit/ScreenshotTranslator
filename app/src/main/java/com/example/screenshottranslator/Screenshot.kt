package com.example.screenshottranslator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.Image.Plane
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import android.view.Display
import android.view.OrientationEventListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class Screenshot : Activity() {

    companion object {
        private val TAG: String = "screen"//ScreenCaptureImageActivity::class.java.getName()
        private val REQUEST_CODE = 100
        private var STORE_DIRECTORY: String? = null
        private var IMAGES_PRODUCED = 0
        private val SCREENCAP_NAME = "screencap"
        private val VIRTUAL_DISPLAY_FLAGS =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
        private var sMediaProjection: MediaProjection? = null

        private var mProjectionManager: MediaProjectionManager? = null
        private var mImageReader: ImageReader? = null
        var mHandler: Handler? = null
        private var mDisplay: Display? = null
        var mVirtualDisplay: VirtualDisplay? = null
        var mDensity = 0
        var mWidth = 0
        var mHeight = 0
        var mRotation = 0
        private var mOrientationChangeCallback: OrientationChangeCallback? = null



        fun createVirtualDisplay() { // get width and height
            val size = Point()
            mDisplay!!.getSize(size)
            mWidth = size.x
            mHeight = size.y
            // start capture reader
            mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2)
            mVirtualDisplay = sMediaProjection!!.createVirtualDisplay(
                SCREENCAP_NAME,
                mWidth,
                mHeight,
                mDensity,
                VIRTUAL_DISPLAY_FLAGS,
                mImageReader!!.surface,
                null,
                mHandler
            )
            mImageReader!!.setOnImageAvailableListener(ImageAvailableListener(), mHandler)
        }
    }

    class ImageAvailableListener : ImageReader.OnImageAvailableListener{
        override fun onImageAvailable(reader: ImageReader?) {
            var image: Image? = null
            var fos: FileOutputStream? = null
            var bitmap: Bitmap? = null

            try {
                image = reader!!.acquireLatestImage()
                if (image != null) {
                    val planes: Array<Plane> = image.planes
                    val buffer: ByteBuffer = planes[0].buffer
                    val pixelStride = planes[0].pixelStride
                    val rowStride = planes[0].rowStride
                    val rowPadding: Int = rowStride - pixelStride * mWidth
                    // create bitmap
                    bitmap = Bitmap.createBitmap(
                        mWidth + rowPadding / pixelStride,
                        mHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(buffer)
                    // write bitmap to a file
                    fos =
                        FileOutputStream("$STORE_DIRECTORY/myscreen_$IMAGES_PRODUCED.png")
                    bitmap.compress(CompressFormat.JPEG, 100, fos)
                    IMAGES_PRODUCED++
                    Log.e(TAG, "captured image: $IMAGES_PRODUCED")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } catch (ioe: IOException) {
                        ioe.printStackTrace()
                    }
                }
                bitmap?.recycle()
                image?.close()
            }
        }
    }

    private class OrientationChangeCallback(context: Context?) : OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            var rotation = mDisplay?.rotation
            if (rotation != mRotation) {
                if (rotation != null) {
                    mRotation = rotation
                }
                try {
                    // clean up
                    if (mVirtualDisplay != null) mVirtualDisplay!!.release()
                    if (mImageReader != null) mImageReader!!.setOnImageAvailableListener(null, null)

                    // re-create virtual display depending on device width / height
                    createVirtualDisplay()
                } catch (e : java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private class MediaProjectionStopCallback : MediaProjection.Callback() {
        override fun onStop() {
            Log.e("ScreenCapture", "stopping projection.");

        }
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        //setContentView(R.layout.screenshot_proceed)
        // call for the projection manager
        mProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startProjection()

/*
        // start projection
        val startButton: Button = findViewById(R.id.startButton) as Button
        startButton.setOnClickListener(object : OnClickListener() {
            fun onClick(v: View?) {
                startProjection()
            }
        })*/

        // stop projection
        /*val stopButton: Button = findViewById(R.id.stopButton) as Button
        stopButton.setOnClickListener(object : OnClickListener() {
            fun onClick(v: View?) {
                stopProjection()
            }
        })*/

        /*// start capture handling thread
        object : Thread() {
            override fun run() {
                Looper.prepare()
                mHandler = Handler()
                Looper.loop()
            }
        }.start()*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode === REQUEST_CODE) {
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
                // create virtual display depending on device width / height
                createVirtualDisplay()
                // register orientation change callback
                mOrientationChangeCallback = OrientationChangeCallback(this)
                if (mOrientationChangeCallback!!.canDetectOrientation()) {
                    mOrientationChangeCallback!!.enable()
                }
                // register media projection stop callback
                sMediaProjection!!.registerCallback(MediaProjectionStopCallback(), mHandler)
            }
        }
    }

    fun startProjection() {
        startActivityForResult(mProjectionManager!!.createScreenCaptureIntent(), REQUEST_CODE)
    }

    private fun stopProjection() {
        mHandler!!.post {
            if (sMediaProjection != null) {
                sMediaProjection!!.stop()
            }
        }
    }
}