package com.example.downloadimage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*
import kotlin.math.min

class FileUtils {

    companion object {
        fun copyFile(source: File?, destination: File?) {
            if (source != null && destination != null) {
                try {
                    val inputStream: InputStream = FileInputStream(source)
                    val outputStream: OutputStream = FileOutputStream(destination)

                    val buffer = ByteArray(1024)

                    do {
                        val len = inputStream.read(buffer)
                        if (len == -1) break
                        outputStream.write(buffer, 0, len)
                    } while (true)

                    inputStream.close()
                    outputStream.close()
                    source.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun decodeImage(source: File): Bitmap {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(source.path, options)
            val imageW: Int = options.outWidth
            val imageH: Int = options.outHeight
            val scaleFactor: Int = min(imageW / imageH, imageW / imageH)
            options.apply {
                inJustDecodeBounds = false
                inSampleSize = scaleFactor
            }
            return BitmapFactory.decodeFile(source.path, options)
        }
    }
}