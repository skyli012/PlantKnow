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
 * 专门针对百度植物识别API优化的图片处理工具
 *
 * 主要功能：
 * 1. 从Uri加载图片并进行智能压缩
 * 2. 将Bitmap转换为Base64字符串
 * 3. 计算Bitmap内存占用大小
 *
 * 特别注意：为满足百度AI API要求，Base64编码需无换行且不含MIME头。
 *
 * 百度API图片要求：
 * - base64编码后大小不超过4M (即原始文件压缩后 < ~3MB)
 * - 最短边 >= 15px, 最长边 <= 4096px
 * - 格式支持 jpg/png/bmp
 * - Base64编码后需 UrlEncode，且不能包含MIME头
 */
object ImageUtils {

    /**
     * 将Uri指向的图片转换为Bitmap，并进行压缩以满足百度植物识别API的要求。
     *
     * 压缩策略：
     * 1. 尺寸压缩：确保图片最长边不超过4096px，最短边不小于15px
     * 2. 质量压缩：通过调整JPEG质量参数，确保Base64编码后不超过4MB
     * 3. 内存优化：使用采样率加载，避免OOM
     *
     * @param context 用于获取ContentResolver
     * @param uri 图片的Uri
     * @return 压缩并符合要求的Bitmap，如果失败则返回null
     */
    suspend fun compressImage(
        context: Context,
        uri: Uri
    ): Bitmap? = withContext(Dispatchers.IO) {
        // 获取内容解析器，用于读取URI对应的图片数据
        val resolver: ContentResolver = context.contentResolver
        var inputStream = resolver.openInputStream(uri) ?: return@withContext null

        try {
            // ==================== 步骤1: 获取原始图片尺寸信息 ====================
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true // 只解码边界信息，不加载像素数据到内存
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // 检查图片尺寸是否符合API要求（最短边 >= 15px）
            if (options.outWidth < 15 || options.outHeight < 15) {
                throw IllegalArgumentException("Image shortest side must be at least 15px")
            }

            // ==================== 步骤2: 计算采样率进行尺寸压缩 ====================
            val reqMaxDim = 4096 // API要求的最长边限制
            val currentMaxDim = maxOf(options.outWidth, options.outHeight)
            var inSampleSize = 1

            // 如果当前尺寸超过限制，计算合适的采样率
            if (currentMaxDim > reqMaxDim) {
                val ratio = currentMaxDim.toFloat() / reqMaxDim
                // 采样率必须是2的幂次方（1, 2, 4, 8, 16...）
                while ((inSampleSize * 2) <= ratio) {
                    inSampleSize *= 2
                }
            }

            // ==================== 步骤3: 使用采样率加载Bitmap到内存 ====================
            inputStream = resolver.openInputStream(uri) ?: return@withContext null
            options.inJustDecodeBounds = false // 现在要实际加载图片数据
            options.inSampleSize = inSampleSize // 设置采样率
            options.inPreferredConfig = Bitmap.Config.ARGB_8888 // 使用高质量的像素格式
            var bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            if (bitmap == null) return@withContext null

            // ==================== 步骤4: 二次尺寸检查和安全缩放 ====================
            // 由于采样率计算可能存在的精度问题，进行最终尺寸验证
            if (maxOf(bitmap.width, bitmap.height) > 4096) {
                // 理论上不会发生，但如果发生则进行精确缩放
                val scale = 4096f / maxOf(bitmap.width, bitmap.height)
                val scaledWidth = (bitmap.width * scale).toInt()
                val scaledHeight = (bitmap.height * scale).toInt()
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
                bitmap.recycle() // 回收原始Bitmap释放内存
                bitmap = scaledBitmap
            }

            // ==================== 步骤5: 质量压缩循环 ====================
            // 目标：原始JPEG字节数 < ~3MB，因为Base64编码会增加约33%的大小
            val maxSizeBytesForBase64 = (4 * 1024 * 1024).toFloat() // 4MB - API限制
            val maxSizeRawBytes = (maxSizeBytesForBase64 / 1.33f).toLong() // 约 3MB - 原始文件目标

            val outputStream = ByteArrayOutputStream()
            var quality = 95 // 从高质量开始尝试
            val minQuality = 50 // 设置最低质量底线，避免图片质量过差

            // 质量压缩循环：从高质量向低质量尝试，直到满足大小要求
            while (quality >= minQuality) {
                outputStream.reset() // 清空输出流

                // 使用JPEG格式压缩，平衡文件大小和图片质量
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                val rawSize = outputStream.size()

                if (rawSize <= maxSizeRawBytes) {
                    // 达到目标大小，创建最终的Bitmap
                    val byteArray = outputStream.toByteArray()
                    bitmap.recycle() // 回收中间Bitmap
                    return@withContext BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                }
                quality -= 5 // 降低质量继续尝试
            }

            // 即使达到最低质量也无法满足大小要求，返回当前状态的最佳结果
            val finalByteArray = outputStream.toByteArray()
            bitmap.recycle()
            BitmapFactory.decodeByteArray(finalByteArray, 0, finalByteArray.size)

        } catch (e: IOException) {
            // 文件读写异常
            e.printStackTrace()
            null
        } catch (e: IllegalArgumentException) {
            // 参数错误，如图片尺寸不满足要求
            e.printStackTrace()
            null
        } catch (e: OutOfMemoryError) {
            // 内存溢出错误
            e.printStackTrace()
            null
        } finally {
            // 确保输入流被关闭，避免资源泄漏
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
     *
     * 注意：Base64.NO_WRAP 确保编码字符串中没有换行符
     * 百度API要求纯Base64字符串，不能包含 "data:image/jpeg;base64," 这样的MIME头
     *
     * @param bitmap 要转换的Bitmap
     * @return 纯Base64字符串，可直接用于API请求
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()

        // 使用JPEG格式，避免PNG产生过大文件
        // 质量85%在文件大小和图片质量之间取得良好平衡
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()

        // 关键：使用NO_WRAP，避免换行；不添加任何MIME头
        // 符合百度API的严格要求
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * 计算Bitmap在内存中的近似大小（字节）
     * 提供跨Android版本的兼容性实现
     *
     * 内存计算方式演进：
     * - API 19+ (KitKat): allocationByteCount - 最准确，包含实际分配的内存
     * - API 12+ (Honeycomb MR1): byteCount - 存储像素所需的最小内存
     * - API 11-: 手动计算 - 行字节数 × 高度
     *
     * @param bitmap 要计算大小的Bitmap
     * @return 内存占用的字节数
     */
    fun getImageSize(bitmap: Bitmap): Long {
        return when {
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT -> {
                // API 19+：返回Bitmap实际分配的内存大小（包含可能的padding）
                bitmap.allocationByteCount.toLong()
            }
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR1 -> {
                // API 12+：返回存储像素数据所需的最小内存
                bitmap.byteCount.toLong()
            }
            else -> {
                // API 11-：手动计算（宽度 × 像素格式字节数 × 高度）
                (bitmap.rowBytes * bitmap.height).toLong()
            }
        }
    }
}