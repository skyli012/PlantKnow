package com.hailong.plantknow.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ImageSaver(private val context: Context) {

    suspend fun saveImageForFavorite(image: Any?): String? = withContext(Dispatchers.IO) {
        try {
            when (image) {
                is Bitmap -> {
                    // 保存 Bitmap 到内部存储
                    saveBitmapToFile(image)
                }
                is Uri -> {
                    // 将相册图片复制到内部存储
                    copyUriToInternalStorage(image)
                }
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val fileName = "favorite_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        }
        return Uri.fromFile(file).toString()
    }

    private fun copyUriToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "favorite_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)

            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}