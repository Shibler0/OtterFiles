package com.shibler.transferfiles.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import coil.decode.DecodeUtils.calculateInSampleSize
import coil.size.Scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale

data class Picture (val path : String, val thumbnail : ByteArray?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Picture

        if (path != other.path) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + thumbnail.contentHashCode()
        return result
    }
}

class ThumbnailGenerator {

    private fun getVideoThumbnail(path: String): ByteArray? {
        val retriever = MediaMetadataRetriever()

        return try {
            retriever.setDataSource(path)

            val fullBitmap = retriever.getFrameAtTime(2000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: return null

            val width = 128
            val height = (fullBitmap.height.toFloat() / fullBitmap.width.toFloat() * width).toInt()
            val scaledBitmap = fullBitmap.scale(width, height)


            val stream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)

            fullBitmap.recycle()
            scaledBitmap.recycle()

            stream.toByteArray()

        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try { retriever.release() } catch (e: Exception) {}
        }
    }

    private fun getPictureThumbnail(path: String): ByteArray? {
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

    suspend fun invoke(path : String) : ByteArray? =
        withContext(Dispatchers.IO) {
            val extension = path.substringAfterLast(".").lowercase()

            when(extension) {
                in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg") -> getPictureThumbnail(path)
                in listOf("mp4", "avi", "mkv", "mov", "wmv", "webm") -> getVideoThumbnail(path)
                else -> null
            }
        }

}

