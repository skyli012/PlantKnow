package com.hailong.plantknow.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import android.util.Base64

/**
 * 图片处理工具类
 * 提供从Uri加载、压缩、转换为Base64和计算大小的功能。
 * 特别注意：为满足百度AI API要求，Base64编码需无换行且不含MIME头。
 */
object ImageUtils {

    /**
     * 将Uri指向的图片转换为Bitmap，并进行压缩以满足百度植物识别API的要求。
     *
     * 百度API要求：
     * - base64编码后大小不超过4M (即原始文件压缩后 < ~3MB)
     * - 最短边 >= 15px, 最长边 <= 4096px
     * - 格式支持 jpg/png/bmp
     * - Base64编码后需 UrlEncode，且**不能包含MIME头**
     *
     * @param context 用于获取ContentResolver
     * @param uri 图片的Uri
     * @return 压缩并符合要求的Bitmap，如果失败则返回null
     */
    suspend fun compressImage(
        context: Context,
        uri: Uri
    ): Bitmap? = withContext(Dispatchers.IO) {
        val resolver: ContentResolver = context.contentResolver
        var inputStream = resolver.openInputStream(uri) ?: return@withContext null

        try {
            // --- 步骤1: 获取原始尺寸 ---
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // 检查最短边是否 >= 15px
            if (options.outWidth < 15 || options.outHeight < 15) {
                throw IllegalArgumentException("Image shortest side must be at least 15px")
            }

            // --- 步骤2: 计算采样率，确保最长边 <= 4096px ---
            val reqMaxDim = 4096
            val currentMaxDim = maxOf(options.outWidth, options.outHeight)
            var inSampleSize = 1

            if (currentMaxDim > reqMaxDim) {
                val ratio = currentMaxDim.toFloat() / reqMaxDim
                while ((inSampleSize * 2) <= ratio) {
                    inSampleSize *= 2
                }
            }

            // --- 步骤3: 加载Bitmap ---
            inputStream = resolver.openInputStream(uri) ?: return@withContext null
            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            var bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            if (bitmap == null) return@withContext null

            // --- 步骤4: 如果尺寸因采样率而改变，检查最终尺寸 ---
            if (maxOf(bitmap.width, bitmap.height) > 4096) {
                // 理论上不会发生，因为inSampleSize已按比例计算
                // 如果发生，进行额外缩放
                val scale = 4096f / maxOf(bitmap.width, bitmap.height)
                val scaledWidth = (bitmap.width * scale).toInt()
                val scaledHeight = (bitmap.height * scale).toInt()
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
                bitmap.recycle()
                bitmap = scaledBitmap
            }

            // --- 步骤5: 质量压缩，目标是让Base64编码后 < 4MB ---
            // 我们的目标是原始字节 < ~3.5MB，给Base64膨胀留出空间 (Base64约增加33%)
            val maxSizeBytesForBase64 = (4 * 1024 * 1024).toFloat() // 4MB
            val maxSizeRawBytes = (maxSizeBytesForBase64 / 1.33f).toLong() // 约 3MB

            val outputStream = ByteArrayOutputStream()
            var quality = 95 // 从高质量开始
            val minQuality = 50 // 设置最低质量下限

            while (quality >= minQuality) {
                outputStream.reset()
                // 根据原始图片格式选择压缩格式？这里统一用JPEG平衡大小和兼容性
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                val rawSize = outputStream.size()

                if (rawSize <= maxSizeRawBytes) {
                    // 达到目标，创建最终Bitmap
                    val byteArray = outputStream.toByteArray()
                    bitmap.recycle()
                    return@withContext BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                }
                quality -= 5
            }

            // 即使最低质量也无法满足，返回当前状态的Bitmap
            val finalByteArray = outputStream.toByteArray()
            bitmap.recycle()
            BitmapFactory.decodeByteArray(finalByteArray, 0, finalByteArray.size)

        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            null
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 将Bitmap转换为Base64编码的字符串（无换行，无MIME头）
     * 这是百度AI API所要求的格式。
     * @param bitmap 要转换的Bitmap
     * @return 纯Base64字符串
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // 使用JPEG格式，避免PNG产生过大文件
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        // 关键：使用NO_WRAP，避免换行；不添加任何MIME头
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * 计算Bitmap在内存中的近似大小（字节）
     */
    fun getImageSize(bitmap: Bitmap): Long {
        return when {
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT -> {
                bitmap.allocationByteCount.toLong()
            }
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1 -> {
                bitmap.byteCount.toLong()
            }
            else -> {
                (bitmap.rowBytes * bitmap.height).toLong()
            }
        }
    }
}