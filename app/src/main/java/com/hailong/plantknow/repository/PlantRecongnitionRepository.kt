package com.hailong.plantknow.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.hailong.plantknow.model.AliyunChatRequest
import com.hailong.plantknow.model.ChatMessage
import com.hailong.plantknow.model.PlantResult
import com.hailong.plantknow.model.PlantWithDetails
import com.hailong.plantknow.network.ApiClient
import com.hailong.plantknow.network.AuthHelper
import com.hailong.plantknow.utils.AccuracyOptimizedImageUtils
import com.hailong.plantknow.utils.Constants
import com.hailong.plantknow.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class PlantRecognitionRepository(
    private val context: Context,
    private val userStatsRepository: UserStatsRepository
) {

    /**
     * 从Bitmap识别植物 - 使用新的Base64转换方法
     */
    suspend fun recognizePlantFromBitmap(bitmap: Bitmap): Result<PlantResult> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "从Bitmap开始识别")
        return@withContext try {
            // 使用新的优化方法将Bitmap转换为Base64
            val base64Image = bitmapToOptimizedBase64(bitmap)
            if (base64Image.isBlank()) {
                Log.e("PlantRepository", "Bitmap转换为Base64失败")
                return@withContext Result.Error(Exception("Failed to convert bitmap to Base64"))
            }

            // 获取token
            val pureToken = AuthHelper.getValidAccessToken()

            val response = ApiClient.baiduApiService.recognizePlant(
                accessToken = pureToken,
                image = base64Image,
                baikeNum = 1
            )
            Log.d("PlantRepository", "API响应: ${response}")

            if (response.isSuccessful && response.body() != null) {
                val recognitionResponse = response.body()!!
                Log.d("PlantRepository", "logId: ${recognitionResponse.logId}")

                val results = recognitionResponse.results
                Log.d("PlantRepository", "results是否为null: ${results == null}")
                if (results != null) {
                    Log.d("PlantRepository", "识别结果数量: ${results.size}")
                }

                if (results != null && results.isNotEmpty()) {
                    results.firstOrNull()?.let { plantResult ->
                        Log.d("PlantRepository", "识别成功: ${plantResult.plantName}, 置信度: ${plantResult.confidence}")
                        // ✅ 增加识别次数（只在识别成功时调用）
                        if (plantResult.plantName != "非植物") {
                            userStatsRepository.incrementRecognitionCount()
                            // ✅ 新增：记录学习活动（只在识别植物时记录）
                            userStatsRepository.recordLearningActivity()
                        }
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

    /**
     * 从Uri识别植物 - 使用新的优化压缩方法
     */
    suspend fun recognizePlantFromUri(uri: Uri): Result<PlantResult> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "从Uri开始识别: $uri")
        return@withContext try {
            // 使用新的优化图片处理方法
            Log.d("PlantRepository", "开始优化图片处理...")
            val optimizedImageData = AccuracyOptimizedImageUtils.optimizeForRecognitionAccuracy(context, uri)

            if (optimizedImageData == null) {
                Log.e("PlantRepository", "图片优化处理失败")
                // 尝试使用快速方案作为备选
                Log.d("PlantRepository", "尝试快速优化方案...")
                val fastImageData = AccuracyOptimizedImageUtils.fastOptimizeForRecognition(context, uri)
                if (fastImageData == null) {
                    return@withContext Result.Error(IllegalArgumentException("Failed to optimize image from URI"))
                }
                return@withContext callRecognitionApi(fastImageData)
            }

            Log.d("PlantRepository", "图片优化完成，Base64数据长度: ${optimizedImageData.length}")
            return@withContext callRecognitionApi(optimizedImageData)

        } catch (e: Exception) {
            Log.e("PlantRepository", "从Uri识别时发生错误", e)
            Result.Error(e)
        }
    }

    /**
     * 统一的API调用方法
     */
    private suspend fun callRecognitionApi(base64Image: String): Result<PlantResult> = withContext(Dispatchers.IO) {
        try {
            val pureToken = AuthHelper.getValidAccessToken()

            val response = ApiClient.baiduApiService.recognizePlant(
                accessToken = pureToken,
                image = base64Image,
                baikeNum = 1
            )

            if (response.isSuccessful && response.body() != null) {
                val recognitionResponse = response.body()!!
                val results = recognitionResponse.results

                if (results != null && results.isNotEmpty()) {
                    results.firstOrNull()?.let { plantResult ->
                        Log.d("PlantRepository", "识别成功: ${plantResult.plantName}, 置信度: ${plantResult.confidence}")
                        if (plantResult.plantName != "非植物") {
                            userStatsRepository.incrementRecognitionCount()
                            userStatsRepository.recordLearningActivity()
                        }
                        Result.Success(plantResult)
                    } ?: Result.Error(Exception("No valid plant data returned from the API."))
                } else {
                    Result.Error(Exception("API返回结果为空。请确保图片包含清晰的植物特征。"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.Error(Exception("API调用失败: ${response.code()}, $errorBody"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * 将Bitmap转换为优化的Base64字符串
     */
    private suspend fun bitmapToOptimizedBase64(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        try {
            // 使用新的优化编码方法
            val outputStream = ByteArrayOutputStream()

            // 为植物识别优化的编码策略
            var quality = 85 // 从高质量开始
            var encodedData: String

            while (quality >= 70) {
                outputStream.reset()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

                val base64Size = calculateBase64Size(outputStream.size())

                // 如果满足API限制，使用当前质量
                if (base64Size <= 4 * 1024 * 1024) {
                    encodedData = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
                    return@withContext encodedData
                }

                quality -= 5
            }

            // 如果所有尝试都失败，使用最低质量
            outputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)

        } catch (e: Exception) {
            Log.e("PlantRepository", "Bitmap转Base64失败", e)
            ""
        }
    }

    /**
     * 计算Base64编码后的大小
     */
    private fun calculateBase64Size(rawSize: Int): Long {
        return (rawSize * 1.37).toLong()
    }

    // ==================== 阿里云通义千问方法（保持不变） ====================

    /**
     * 获取植物的详细信息（通过阿里云通义千问）
     */
    suspend fun getPlantDetailsFromAliyun(plantName: String): Result<String> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "开始调用阿里云API获取植物详情: $plantName")
        return@withContext try {
            val prompt = """
            请提供以下植物的详细信息，所有内容必须为中文，且严格**必须**按照指定格式输出：
            植物名称：$plantName
            
            **重要格式要求**：
            1. 每一行必须是 **"小标题: 内容"** 格式，冒号使用英文冒号
            2. 每个小标题必须完全使用以下列表中的名称：
               植物简介、科属分类、形态特征、植物文化、趣味知识、水、阳光、土壤、温度、肥料
            3. 每一行单独一行，不要合并多个项目
            4. 这个介绍需要稍微详细一点，可以让用户能够学到知识和能对这个植物有所了解
            
            **输出示例**：
            植物简介: 紫丁香是一种落叶灌木，学名Syringa vulgaris...
            科属分类: 木犀科 丁香属
            形态特征: 植株高度可达4-6米...
            植物文化: 在中国文化中象征春天与浪漫...
            趣味知识: 紫丁香的花香有助缓解压力...
            水: 喜欢持续湿润的土壤...
            阳光: 需要充足阳光...
            土壤: 喜欢排水良好的肥沃土壤...
            温度: 耐寒植物...
            肥料: 春季施一次通用肥料...
            
            **现在请为【$plantName】按照上述格式提供信息：**
            """.trimIndent()

            val messages = listOf(
                ChatMessage(role = "user", content = prompt)
            )

            val request = AliyunChatRequest(
                model = Constants.QWEN_FLASH_MODEL,
                messages = messages,
                stream = false,
                extra_body = mapOf("enable_thinking" to true)
            )

            Log.d("PlantRepository", "发送阿里云API请求...")
            val response = ApiClient.aliyunApiService.chatCompletion(request)

            if (response.choices.isNotEmpty()) {
                val content = response.choices[0].message.content

                // ✅ 重要：在这里立即打印原始返回内容
                Log.d("PlantRepository", "✅ AI原始返回内容（开始）==========")
                Log.d("PlantRepository", "返回内容长度: ${content.length} 字符")
                Log.d("PlantRepository", "完整内容:")
                // 按行打印，方便查看格式
                content.split("\n").forEachIndexed { index, line ->
                    Log.d("PlantRepository", "行 ${index + 1}: '$line'")
                }
                Log.d("PlantRepository", "✅ AI原始返回内容（结束）==========")

                Result.Success(content)
            } else {
                Log.e("PlantRepository", "阿里云API返回空结果")
                Result.Error(Exception("阿里云API返回空结果"))
            }

        } catch (e: Exception) {
            Log.e("PlantRepository", "获取植物详情失败", e)
            Result.Error(e)
        }
    }

    /**
     * 完整的植物识别流程：百度识别 + 阿里云详情补充
     */
    suspend fun recognizePlantWithDetails(bitmap: Bitmap): Result<PlantWithDetails> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "开始完整植物识别流程（从Bitmap）...")
        return@withContext try {
            // 1. 使用新的Base64转换方法
            val base64Image = bitmapToOptimizedBase64(bitmap)
            if (base64Image.isBlank()) {
                return@withContext Result.Error(Exception("Failed to convert bitmap to Base64"))
            }

            val recognitionResult = callRecognitionApi(base64Image)

            if (recognitionResult is Result.Success) {
                val plantResult = recognitionResult.data
                Log.d("PlantRepository", "百度识别成功: ${plantResult.plantName}")

                // 如果是"非植物"，直接返回
                if (plantResult.plantName == "非植物") {
                    Log.d("PlantRepository", "识别结果为'非植物'，跳过阿里云调用")
                    val plantWithDetails = PlantWithDetails(
                        basicInfo = plantResult,
                        detailedDescription = "识别结果为非植物，暂无详细描述。"
                    )
                    return@withContext Result.Success(plantWithDetails)
                }

                // 2. 使用阿里云获取详细信息
                Log.d("PlantRepository", "开始调用阿里云API获取详情...")
                val detailsResult = getPlantDetailsFromAliyun(plantResult.plantName)

                if (detailsResult is Result.Success) {
                    Log.d("PlantRepository", "阿里云API调用成功")
                    val plantWithDetails = PlantWithDetails(
                        basicInfo = plantResult,
                        detailedDescription = detailsResult.data
                    )
                    Result.Success(plantWithDetails)
                } else {
                    Log.w("PlantRepository", "阿里云调用失败，仅返回百度结果")
                    val plantWithDetails = PlantWithDetails(
                        basicInfo = plantResult,
                        detailedDescription = "暂时无法获取详细描述，请稍后重试"
                    )
                    Result.Success(plantWithDetails)
                }
            } else {
                Log.e("PlantRepository", "百度识别失败")
                Result.Error((recognitionResult as Result.Error).exception)
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "完整识别流程发生异常", e)
            Result.Error(e)
        }
    }

    /**
     * 从URI开始的完整植物识别流程 - 使用新的优化方法
     */
    suspend fun recognizePlantWithDetailsFromUri(uri: Uri): Result<PlantWithDetails> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "开始完整植物识别流程（从URI）: $uri")
        return@withContext try {
            // 使用新的优化图片处理方法
            Log.d("PlantRepository", "开始优化图片处理...")
            val optimizedImageData = AccuracyOptimizedImageUtils.optimizeForRecognitionAccuracy(context, uri)

            if (optimizedImageData == null) {
                Log.e("PlantRepository", "图片优化处理失败")
                // 尝试使用快速方案
                Log.d("PlantRepository", "尝试快速优化方案...")
                val fastImageData = AccuracyOptimizedImageUtils.fastOptimizeForRecognition(context, uri)
                if (fastImageData == null) {
                    return@withContext Result.Error(IllegalArgumentException("Failed to optimize image from URI"))
                }
                return@withContext callRecognitionWithDetailsApi(fastImageData)
            }

            Log.d("PlantRepository", "图片优化完成，Base64数据长度: ${optimizedImageData.length}")
            return@withContext callRecognitionWithDetailsApi(optimizedImageData)

        } catch (e: Exception) {
            Log.e("PlantRepository", "从URI识别流程发生异常", e)
            Result.Error(e)
        }
    }

    /**
     * 统一的完整识别API调用方法
     */
    private suspend fun callRecognitionWithDetailsApi(base64Image: String): Result<PlantWithDetails> = withContext(Dispatchers.IO) {
        try {
            val recognitionResult = callRecognitionApi(base64Image)

            if (recognitionResult is Result.Success) {
                val plantResult = recognitionResult.data

                if (plantResult.plantName == "非植物") {
                    val plantWithDetails = PlantWithDetails(
                        basicInfo = plantResult,
                        detailedDescription = "识别结果为非植物，暂无详细描述。"
                    )
                    return@withContext Result.Success(plantWithDetails)
                }

                val detailsResult = getPlantDetailsFromAliyun(plantResult.plantName)

                val plantWithDetails = if (detailsResult is Result.Success) {
                    PlantWithDetails(
                        basicInfo = plantResult,
                        detailedDescription = detailsResult.data
                    )
                } else {
                    PlantWithDetails(
                        basicInfo = plantResult,
                        detailedDescription = "暂时无法获取详细描述，请稍后重试"
                    )
                }
                Result.Success(plantWithDetails)
            } else {
                Result.Error((recognitionResult as Result.Error).exception)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * 仅获取植物详细信息（如果已经知道植物名称）
     */
    suspend fun refreshPlantDetails(plantName: String): Result<String> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "刷新植物详情: $plantName")
        return@withContext getPlantDetailsFromAliyun(plantName)
    }
}