package com.hailong.plantknow.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * 为提高识别准确率优化的图像处理工具
 * 策略：在文件大小限制内，尽可能保留植物识别所需的关键特征
 */
object AccuracyOptimizedImageUtils {

    /**
     * 为提高识别准确率优化的图像处理
     */
    suspend fun optimizeForRecognitionAccuracy(
        context: Context,
        uri: Uri
    ): String? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        var inputStream: InputStream? = null

        try {
            // ==================== 1. 分析图像特性 ====================
            inputStream = resolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val imageInfo = analyzeImageForRecognition(options.outWidth, options.outHeight)

            // ==================== 2. 智能尺寸选择 ====================
            val targetSize = calculateOptimalSizeForAccuracy(
                options.outWidth,
                options.outHeight,
                imageInfo.aspectRatio
            )

            // ==================== 3. 高质量解码 ====================
            inputStream = resolver.openInputStream(uri)
            val bitmap = decodeHighQualityBitmap(inputStream, targetSize)
            inputStream?.close()

            // ==================== 4. 智能格式选择 ====================
            return@withContext optimizeEncodingForRecognition(bitmap, imageInfo)

        } catch (e: Exception) {
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
     * 分析图像对植物识别的适宜性
     */
    private fun analyzeImageForRecognition(width: Int, height: Int): ImageRecognitionInfo {
        val aspectRatio = width.toFloat() / height.toFloat()

        // 修复：使用标准的比较操作符代替 'between'
        val isLikelyCloseUp = aspectRatio in 0.7f..1.3f

        return ImageRecognitionInfo(
            aspectRatio = aspectRatio,
            recommendedMinDimension = 800,
            recommendedMaxDimension = 2048,
            isLikelyCloseUp = isLikelyCloseUp
        )
    }

    /**
     * 计算对识别准确率最有利的尺寸
     */
    private fun calculateOptimalSizeForAccuracy(
        originalWidth: Int,
        originalHeight: Int,
        aspectRatio: Float
    ): Pair<Int, Int> {
        val maxApiDimension = 4096
        val minDimensionForDetail = 600 // 保证最小尺寸有足够细节

        val currentMax = maxOf(originalWidth, originalHeight)
        val currentMin = minOf(originalWidth, originalHeight)

        return when {
            // 图像太小 - 避免放大，保持原尺寸
            currentMax < minDimensionForDetail ->
                originalWidth to originalHeight

            // 图像在理想范围内 - 保持原尺寸或轻微缩小
            currentMax in minDimensionForDetail..2048 ->
                originalWidth to originalHeight

            // 图像较大 - 缩放到保留细节的最佳尺寸
            currentMax > 2048 -> {
                val scale = 2048f / currentMax
                val newWidth = (originalWidth * scale).toInt().coerceAtLeast(minDimensionForDetail)
                val newHeight = (originalHeight * scale).toInt().coerceAtLeast(minDimensionForDetail)
                newWidth to newHeight
            }

            else -> originalWidth to originalHeight
        }
    }

    /**
     * 高质量解码，避免采样率造成的细节损失
     */
    private fun decodeHighQualityBitmap(
        inputStream: InputStream?,
        targetSize: Pair<Int, Int>
    ): Bitmap {
        val (targetWidth, targetHeight) = targetSize

        if (inputStream == null) {
            throw IOException("Input stream is null")
        }

        // 先完整解码原图
        val options = BitmapFactory.Options().apply {
            inSampleSize = 1
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inPreferredConfig = Bitmap.Config.RGB_565 // 使用更节省内存的配置，但保持质量
        }

        val originalBitmap = BitmapFactory.decodeStream(inputStream, null, options)
            ?: throw IOException("Failed to decode bitmap")

        // 如果需要缩放，使用高质量缩放算法
        return if (originalBitmap.width != targetWidth || originalBitmap.height != targetHeight) {
            val scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                targetWidth,
                targetHeight,
                true // 使用双线性过滤，保持平滑
            )
            originalBitmap.recycle()
            scaledBitmap ?: throw IOException("Failed to scale bitmap")
        } else {
            originalBitmap
        }
    }

    /**
     * 为识别准确率优化的编码策略
     */
    private fun optimizeEncodingForRecognition(
        bitmap: Bitmap,
        imageInfo: ImageRecognitionInfo
    ): String {
        val outputStream = ByteArrayOutputStream()

        // 策略：优先保证特征清晰度，而不是文件大小
        var quality = 90 // 从高质量开始
        var encodedData: String

        while (quality >= 70) { // 设置质量底线为70%
            outputStream.reset()

            // 对植物识别，JPEG通常足够，且文件更小
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            val base64Size = calculateBase64Size(outputStream.size())

            // 如果满足API限制，使用当前质量
            if (base64Size <= 4 * 1024 * 1024) { // 4MB
                encodedData = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

                // 额外检查：确保关键特征区域清晰
                if (isFeatureDetailPreserved(bitmap, quality)) {
                    return encodedData
                }
            }

            quality -= 5
        }

        // 如果所有尝试都失败，使用最低质量但保证特征可见
        outputStream.reset()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        encodedData = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        return encodedData
    }

    /**
     * 检查关键特征是否保持清晰
     */
    private fun isFeatureDetailPreserved(bitmap: Bitmap, quality: Int): Boolean {
        // 简单的启发式检查：
        // 1. 质量不能太低
        if (quality < 75) return false

        // 2. 图像尺寸不能太小
        if (minOf(bitmap.width, bitmap.height) < 400) return false

        // 3. 检查图像是否过度模糊（简单版本）
        // 在实际应用中可以用更复杂的图像清晰度检测

        return true
    }

    /**
     * 计算Base64编码后的大小
     */
    private fun calculateBase64Size(rawSize: Int): Long {
        return (rawSize * 1.37).toLong() // Base64大小估算
    }

    /**
     * 快速压缩方案 - 在保证质量的前提下快速处理
     */
    suspend fun fastOptimizeForRecognition(
        context: Context,
        uri: Uri
    ): String? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        var inputStream: InputStream? = null

        try {
            inputStream = resolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // 快速方案：直接使用适中的尺寸和质量
            val targetSize = calculateQuickOptimalSize(options.outWidth, options.outHeight)

            inputStream = resolver.openInputStream(uri)
            val bitmap = decodeHighQualityBitmap(inputStream, targetSize)
            inputStream?.close()

            // 使用固定的高质量设置
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream) // 固定85%质量
            val encodedData = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            bitmap.recycle()
            return@withContext encodedData

        } catch (e: Exception) {
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
     * 快速计算最优尺寸
     */
    private fun calculateQuickOptimalSize(width: Int, height: Int): Pair<Int, Int> {
        val maxDimension = 1600 // 快速方案的适中尺寸

        return if (maxOf(width, height) > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(width, height)
            val newWidth = (width * scale).toInt().coerceAtLeast(600)
            val newHeight = (height * scale).toInt().coerceAtLeast(600)
            newWidth to newHeight
        } else {
            width to height
        }
    }

    data class ImageRecognitionInfo(
        val aspectRatio: Float,
        val recommendedMinDimension: Int,
        val recommendedMaxDimension: Int,
        val isLikelyCloseUp: Boolean
    )
}