package com.example.screenshottranslator

import android.graphics.Bitmap
import android.media.Image
import android.media.ImageReader
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class ImageAvailableListener(
    var STORE_DIRECTORY: String,
    var IMAGES_PRODUCED: Int
) : ImageReader.OnImageAvailableListener {

    var TAG = "ImageSaver"

    override fun onImageAvailable(reader: ImageReader?) {
        var image: Image? = null
        var fos: FileOutputStream? = null
        var bitmap: Bitmap? = null

        try {
            image = reader!!.acquireLatestImage()
            if (image != null) {
                val planes: Array<Image.Plane> = image.planes
                val buffer: ByteBuffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding: Int = rowStride - pixelStride * image.width
                // create bitmap
                bitmap = Bitmap.createBitmap(
                    image.width + rowPadding / pixelStride,
                    image.height, Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                // write bitmap to a file
                fos =
                    FileOutputStream("$STORE_DIRECTORY/myscreen_$IMAGES_PRODUCED.png")
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)

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