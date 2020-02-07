package com.example.screenshottranslator

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager

class ScreenshotTest(var context: Context, var mProjectionManager : MediaProjectionManager, var REQUEST_CODE : Int) : Activity() {
    init{
        startProjection()
    }

    fun startProjection() {
        (context as Activity).startActivityForResult(
            mProjectionManager!!.createScreenCaptureIntent(),
            REQUEST_CODE)
    }
}