package com.example.screenshottranslator

import android.content.Context
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.view.Display
import android.view.OrientationEventListener

class OrientationChangeCallback(
    context: Context?,
    var mDisplay: Display,
    var mVirtualDisplay: VirtualDisplay,
    var mImageReader: ImageReader,
    var callback: () -> Unit
) : OrientationEventListener(context) {

    private var mRotation = 0

    override fun onOrientationChanged(orientation: Int) {
        var rotation = mDisplay.rotation
        if (rotation != mRotation) {
            if (rotation != null) {
                mRotation = rotation
            }
            try {
                // clean up
                if (mVirtualDisplay != null) mVirtualDisplay!!.release()
                if (mImageReader != null) mImageReader!!.setOnImageAvailableListener(null, null)

                // re-create virtual display depending on device width / height
                callback()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
}