package com.hailong.plantknow.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.hailong.plantknow.model.PlantResult
import com.hailong.plantknow.network.ApiClient
import com.hailong.plantknow.network.AuthHelper
import com.hailong.plantknow.util.ImageUtils
import com.hailong.plantknow.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 植物识别数据仓库类
 * 负责处理植物识别的数据获取和业务逻辑，包括从Bitmap和URI两种方式的识别
 *
 * @param context Android上下文，用于访问系统服务和资源
 */
class PlantRecongnitionRepository(private val context: Context) {

    /**
     * 从Bitmap图片识别植物
     *
     * @param bitmap 要识别的植物图片Bitmap对象
     * @return 识别结果包装在Result中，包含成功或错误信息
     */
    suspend fun recognizePlantFromBitmap(bitmap: Bitmap): Result<PlantResult> = withContext(Dispatchers.IO) {
        return@withContext try {
            // ==================== 1. 认证令牌处理 ====================
            // 获取有效的访问令牌（包含Bearer前缀）
            val tokenWithBearer = AuthHelper.getValidAccessToken()
            // 移除Bearer前缀，因为API验证只需要纯token
            val pureToken = tokenWithBearer.replace("Bearer ", "")
            Log.d("PlantRepository", "获取到token，长度: ${pureToken.length}")

            // ==================== 2. 图片数据预处理 ====================
            // 将Bitmap格式图片转换为Base64字符串格式，便于网络传输
            val base64Image = ImageUtils.bitmapToBase64(bitmap)
            Log.d("PlantRepository", "图片转换为Base64完成，长度: ${base64Image.length}")

            // ==================== 3. 调用识别API ====================
            // 发送网络请求到植物识别API
            val response = ApiClient.apiService.recognizePlant(
                accessToken = pureToken,      // 认证令牌
                image = base64Image,          // Base64编码的图片数据
                baikeNum = 1                  // 请求返回的百科结果数量
            )
            Log.d("PlantRepository", "API响应状态: ${response.isSuccessful}")

            // ==================== 4. 响应结果处理 ====================
            if (response.isSuccessful && response.body() != null) {
                val recognitionResponse = response.body()!!
                Log.d("PlantRepository", "请求logId: ${recognitionResponse.logId}")

                // 检查识别结果是否存在
                val results = recognitionResponse.results
                Log.d("PlantRepository", "results是否为null: ${results == null}")
                if (results != null) {
                    Log.d("PlantRepository", "识别结果数量: ${results.size}")
                }

                // ==================== 5. 业务逻辑处理 ====================
                if (results != null && results.isNotEmpty()) {
                    // 取第一个识别结果（通常置信度最高的）
                    results.firstOrNull()?.let { plantResult ->
                        Log.d("PlantRepository", "识别成功: ${plantResult.plantName}, 置信度: ${plantResult.confidence}")
                        // 返回成功结果
                        Result.Success(plantResult)
                    } ?: run {
                        // 结果列表不为空但第一个元素为null的异常情况
                        Log.e("PlantRepository", "API未返回有效的植物数据")
                        Result.Error(Exception("No valid plant data returned from the API."))
                    }
                } else {
                    // 识别结果为空的情况（可能是图片不清晰或非植物图片）
                    Log.e("PlantRepository", "API返回的results为null或空列表")
                    Result.Error(Exception("API返回结果为空。请确保图片包含清晰的植物特征。"))
                }
            } else {
                // API调用失败（网络错误、服务器错误等）
                val errorBody = response.errorBody()?.string()
                Log.e("PlantRepository", "API调用失败: ${response.code()}, $errorBody")
                Result.Error(Exception("API调用失败: ${response.code()}, $errorBody"))
            }
        } catch (e: Exception) {
            // 捕获所有异常，防止应用崩溃
            Log.e("PlantRepository", "发生未知错误", e)
            Result.Error(e)
        }
    }

    /**
     * 从图片URI识别植物
     * 先压缩图片，然后调用Bitmap识别方法
     *
     * @param uri 图片的URI地址
     * @return 识别结果包装在Result中
     */
    suspend fun recognizePlantFromUri(uri: Uri): Result<PlantResult> = withContext(Dispatchers.IO) {
        return@withContext try {
            // ==================== 1. 图片压缩处理 ====================
            // 从URI加载图片并进行压缩，优化内存使用和网络传输
            val compressedBitmap = ImageUtils.compressImage(context, uri)

            if (compressedBitmap == null) {
                // 图片加载或压缩失败
                Log.e("PlantRepository", "无法从URI加载或压缩图片: $uri")
                return@withContext Result.Error(IllegalArgumentException("Failed to load or compress image from URI"))
            }

            Log.d("PlantRepository", "图片压缩完成，尺寸: ${compressedBitmap.width}x${compressedBitmap.height}")

            // ==================== 2. 调用Bitmap识别方法 ====================
            // 使用压缩后的Bitmap进行植物识别
            recognizePlantFromBitmap(compressedBitmap)

        } catch (e: Exception) {
            // 捕获图片处理过程中的异常
            Log.e("PlantRepository", "从URI识别植物时发生错误", e)
            Result.Error(e)
        }
    }
}