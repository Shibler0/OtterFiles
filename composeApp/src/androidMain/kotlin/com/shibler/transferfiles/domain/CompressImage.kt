package com.shibler.transferfiles.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import coil.decode.DecodeUtils.calculateInSampleSize
import coil.size.Scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ThumbnailGenerator {

    private fun compressToThumbnail(path: String): ByteArray? {
        try {

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)

            options.inSampleSize = calculateInSampleSize(
                srcWidth = options.outWidth,
                srcHeight = options.outHeight,
                dstWidth = 128,
                dstHeight = 128,
                scale = Scale.FILL
            )

            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeFile(path, options) ?: return null


            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)

            val imageBytes = stream.toByteArray()

            val sizeInBytes = imageBytes.size
            val sizeInKB = sizeInBytes / 1024

            println("Taille finale de la miniature : $sizeInBytes octets ($sizeInKB Ko)")
            bitmap.recycle()
            stream.close()

            return imageBytes

        }catch(e : Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend operator fun invoke(path: String): ByteArray? =
        withContext(Dispatchers.IO) {
            compressToThumbnail(path)
        }

}

