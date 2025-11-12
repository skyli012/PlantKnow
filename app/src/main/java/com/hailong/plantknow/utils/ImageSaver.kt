package com.hailong.plantknow.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageSaver(private val context: Context) {

    // === 你原来的收藏图片保存功能（保持不变）===
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

    // === 新增的头像保存功能 ===
    suspend fun saveAvatarToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            // 创建头像目录
            val avatarDir = File(context.filesDir, "avatars")
            if (!avatarDir.exists()) {
                avatarDir.mkdirs()
            }

            // 生成唯一文件名
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "avatar_$timeStamp.jpg"
            val imageFile = File(avatarDir, fileName)

            // 从 URI 读取图片
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // 解码图片
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // 压缩并保存图片
                FileOutputStream(imageFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                }

                // 返回内部文件路径
                return@withContext imageFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
        return@withContext null
    }

    // 获取图片文件
    fun getImageFileFromPath(filePath: String): File {
        return File(filePath)
    }

    // 删除旧的头像文件
    fun deleteOldAvatar(oldAvatarPath: String?) {
        if (!oldAvatarPath.isNullOrEmpty()) {
            try {
                val oldFile = File(oldAvatarPath)
                if (oldFile.exists()) {
                    oldFile.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 检查文件是否存在
    fun isImageFileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }
}
