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

class PlantRecongnitionRepository(private val context: Context) {

    suspend fun recognizePlantFromBitmap(bitmap: Bitmap): Result<PlantResult> = withContext(Dispatchers.IO) {
        return@withContext try {
            // 获取token
            val tokenWithBearer = AuthHelper.getValidAccessToken()
            // 获取的token有前缀，但是使用accesstoken验证是 不需要前缀的
            val pureToken = tokenWithBearer.replace("Bearer ", "")

            // 将bitamp格式图片转化为based64格式
            val base64Image = ImageUtils.bitmapToBase64(bitmap)

            val response = ApiClient.apiService.recognizePlant(
                accessToken = pureToken,
                image = base64Image,
                baikeNum = 1
            )
            Log.d("PlantRepository", "API响应: ${response}")

            if (response.isSuccessful && response.body() != null) {
                val recognitionResponse = response.body()!!
                Log.d("PlantRepository", "logId: ${recognitionResponse.logId}")

                // 检查 results 是否为 null 或空
                val results = recognitionResponse.results
                Log.d("PlantRepository", "results是否为null: ${results == null}")
                if (results != null) {
                    Log.d("PlantRepository", "识别结果数量: ${results.size}")
                }

                if (results != null && results.isNotEmpty()) {
                    results.firstOrNull()?.let { plantResult ->
                        Log.d("PlantRepository", "识别成功: ${plantResult.plantName}, 置信度: ${plantResult.confidence}")
                        Result.Success(plantResult)
                    } ?: run {
                        Log.e("PlantRepository", "API未返回有效的植物数据")
                        Result.Error(Exception("No valid plant data returned from the API."))
                    }
                } else {
                    Log.e("PlantRepository", "API返回的results为null或空列表")
                    Result.Error(Exception("API返回结果为空。请确保图片包含清晰的植物特征。"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PlantRepository", "API调用失败: ${response.code()}, $errorBody")
                Result.Error(Exception("API调用失败: ${response.code()}, $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "发生错误", e)
            Result.Error(e)
        }
    }

    suspend fun recognizePlantFromUri(uri: Uri): Result<PlantResult> = withContext(Dispatchers.IO) {
        return@withContext try {
            val compressedBitmap = ImageUtils.compressImage(context, uri)
            if (compressedBitmap == null) {
                return@withContext Result.Error(IllegalArgumentException("Failed to load or compress image from URI"))
            }
            recognizePlantFromBitmap(compressedBitmap)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}