package io.agora.board.forge.ui.component

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import io.agora.board.forge.ui.internal.util.Logger
import java.io.File
import java.io.FileNotFoundException

/**
 * author : fenglibin
 * date : 2024/7/9
 * description :
 */
object FcrBitmapUtils {
    const val TAG = "FcrBitmapUtils"

    const val SUCCESS = 0
    private const val ERROR_SAVE_FAILED = 1
    private const val ERROR_FILE_NOT_FOUND = 2
    private const val ERROR_INSERT_FAILED = 3
    private const val ERROR_OPEN_OUTPUT_STREAM_FAILED = 4
    private const val ERROR_COMPRESSION_FAILED = 5
    private const val ERROR_EXCEPTION = 6

    fun combineBitmapsVertically(bitmaps: List<Bitmap>): Bitmap? {
        if (bitmaps.isEmpty()) return null

        val paint = Paint().apply { color = Color.WHITE }
        val maxWidth = bitmaps.maxOf { it.width }
        val totalHeight = bitmaps.sumOf { it.height }

        return Bitmap.createBitmap(maxWidth, totalHeight, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this).apply { drawColor(Color.WHITE) }
            var currentHeight = 0

            bitmaps.forEach { bitmap ->
                canvas.drawBitmap(bitmap, 0f, currentHeight.toFloat(), paint)
                currentHeight += bitmap.height
            }
        }
    }

    fun saveToGallery(context: Context, bitmap: Bitmap, imageName: String): Int {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            saveToGalleryLegacy(context, bitmap, imageName)
        } else {
            saveToGalleryModern(context, bitmap, imageName)
        }
    }

    private fun saveToGalleryLegacy(context: Context, bitmap: Bitmap, imageName: String): Int {
        return try {
            val insertImage = MediaStore.Images.Media.insertImage(
                context.contentResolver, bitmap, imageName, null
            )
            val realPath = getRealPathFromURI(Uri.parse(insertImage), context)
            if (realPath.isEmpty()) {
                ERROR_SAVE_FAILED
            } else {
                val file = File(realPath)
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
                SUCCESS
            }
        } catch (e: FileNotFoundException) {
            Logger.e(TAG, "保存到相册失败", e)
            ERROR_FILE_NOT_FOUND
        }
    }

    private fun saveToGalleryModern(context: Context, bitmap: Bitmap, imageName: String): Int {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return ERROR_INSERT_FAILED

        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)) {
                    SUCCESS
                } else {
                    ERROR_COMPRESSION_FAILED
                }
            } ?: ERROR_OPEN_OUTPUT_STREAM_FAILED
        } catch (e: Exception) {
            Logger.e(TAG, "保存到相册失败")
            ERROR_EXCEPTION
        }
    }

    private fun getRealPathFromURI(contentUri: Uri, context: Context): String {
        val cursor: Cursor? = context.contentResolver.query(
            contentUri, arrayOf(MediaStore.Images.Media.DATA), null, null, null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return ""
    }
}
