package com.hailong.plantknow.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.hailong.plantknow.model.PlantResult
import com.hailong.plantknow.network.ApiClient
import com.hailong.plantknow.network.AuthHelper
import com.hailong.plantknow.utils.ImageUtils
import com.hailong.plantknow.utils.Result
import com.hailong.plantknow.model.AliyunChatRequest
import com.hailong.plantknow.model.ChatMessage
import com.hailong.plantknow.model.PlantWithDetails
import com.hailong.plantknow.utils.Constants

class PlantRecognitionRepository(
    private val context: Context,
    private val userStatsRepository: UserStatsRepository
) {

    suspend fun recognizePlantFromBitmap(bitmap: Bitmap): Result<PlantResult> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "从Bitmap开始识别")
        return@withContext try {

            val base64Image = ImageUtils.bitmapToBase64(bitmap)

            // 获取带Bearer的token
            val pureToken = AuthHelper.getValidAccessToken()
            // 去掉Bearer前缀

//            Log.d("PlantRepository", accessToken)
            val response = ApiClient.baiduApiService.recognizePlant(
                accessToken = pureToken,  //  改一下 为了防止浪费token
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
     * 从Uri识别植物。这是更常见的入口。
     */
    suspend fun recognizePlantFromUri(uri: Uri): Result<PlantResult> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "从Uri开始识别: $uri")
        return@withContext try {
            // 1. 压缩图片以满足百度API要求
            Log.d("PlantRepository", "压缩图片")
            val compressedBitmap = ImageUtils.compressImage(context, uri)
            if (compressedBitmap == null) {
                Log.e("PlantRepository", "图片加载或压缩失败")
                return@withContext Result.Error(IllegalArgumentException("Failed to load or compress image from URI"))
            }
            Log.d("PlantRepository", "图片压缩完成: ${compressedBitmap.width}x${compressedBitmap.height} ${compressedBitmap}")

            // 2. 使用压缩后的Bitmap进行识别

            recognizePlantFromBitmap(compressedBitmap)
        } catch (e: Exception) {
            Log.e("PlantRepository", "从Uri识别时发生错误", e)
            Result.Error(e)
        }
    }


    // ==================== 新增的阿里云通义千问方法 ====================

    /**
     * 获取植物的详细信息（通过阿里云通义千问）
     * @param plantName 植物名称
     * @return 包含详细信息的字符串
     */
    suspend fun getPlantDetailsFromAliyun(plantName: String): Result<String> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "开始调用阿里云API获取植物详情: $plantName")
        return@withContext try {
            // 构建提示词，让AI提供详细的植物信息
            val prompt = """
                请提供以下植物的详细信息：
                植物名称：$plantName
                
                请按照以下结构提供信息：
                1. 植物简介（50-100字）
                2. 科属分类
                3. 形态特征  
                4. 植物文化
                5. 趣味知识
                
                请确保信息准确、详细，适合植物爱好者阅读。
                要求：
                1、你返回的时候植物简介上不要加上(50-100字)  2、除了小标题以外，其他的内容不要用任何格式 3、植物简介里面加上学名别名 4、回答的格式 以“小标题:内容”的格式回答，小标题就是前面的植物简介、科属分类..
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
                Log.d("PlantRepository", "阿里云API调用成功，返回内容长度: ${content.length}")
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
     * @param bitmap 植物图片
     * @return 包含基本信息和详细描述的完整植物信息
     */
    suspend fun recognizePlantWithDetails(bitmap: Bitmap): Result<PlantWithDetails> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "开始完整植物识别流程（从Bitmap）...")
        return@withContext try {
            // 1. 使用百度识别植物
            val recognitionResult = recognizePlantFromBitmap(bitmap)

            if (recognitionResult is Result.Success) {
                val plantResult = recognitionResult.data
                Log.d("PlantRepository", "百度识别成功: ${plantResult.plantName}")


                // 👇 在调用阿里云之前，简单判断 plantName 是否等于 "非植物"
                if (plantResult.plantName == "非植物") {
                    // 如果是"非植物"，直接构建结果，跳过网络请求
                    Log.d("PlantRepository", "识别结果为'非植物'，跳过阿里云调用")
                    val plantWithDetails = PlantWithDetails(
                        basicInfo = plantResult,
                        detailedDescription = "识别结果为非植物，暂无详细描述。"
                    )
                    return@withContext Result.Success(plantWithDetails) // 直接返回成功结果
                }

                // 2. 使用阿里云获取详细信息
                Log.d("PlantRepository", "开始调用阿里云API获取详情...")
                val detailsResult = getPlantDetailsFromAliyun(plantResult.plantName)


                if (detailsResult is Result.Success) {
                    Log.d("PlantRepository", "阿里云API调用成功")
                    // 合并两个结果
                    val plantWithDetails = PlantWithDetails(
                        basicInfo = plantResult,
                        detailedDescription = detailsResult.data
                    )
                    Result.Success(plantWithDetails)
                } else {
                    Log.w("PlantRepository", "阿里云调用失败，仅返回百度结果")
                    // 阿里云调用失败，只返回百度结果
                    val plantWithDetails = PlantWithDetails(
                        basicInfo = plantResult,
                        detailedDescription = "暂时无法获取详细描述，请稍后重试"
                    )
                    Result.Success(plantWithDetails)
                }
            } else {
                Log.e("PlantRepository", "百度识别失败")
                // 百度识别失败
                Result.Error((recognitionResult as Result.Error).exception)
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "完整识别流程发生异常", e)
            Result.Error(e)
        }
    }

    /**
     * 从URI开始的完整植物识别流程
     */
    suspend fun recognizePlantWithDetailsFromUri(uri: Uri): Result<PlantWithDetails> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "开始完整植物识别流程（从URI）: $uri")
        return@withContext try {
            // 1. 压缩图片
            Log.d("PlantRepository", "压缩图片...")
            val compressedBitmap = ImageUtils.compressImage(context, uri)
            if (compressedBitmap == null) {
                Log.e("PlantRepository", "图片加载或压缩失败")
                return@withContext Result.Error(IllegalArgumentException("Failed to load or compress image from URI"))
            }
            Log.d("PlantRepository", "图片压缩完成: ${compressedBitmap.width}x${compressedBitmap.height}")

            // 2. 执行完整识别流程
            recognizePlantWithDetails(compressedBitmap)
        } catch (e: Exception) {
            Log.e("PlantRepository", "从URI识别流程发生异常", e)
            Result.Error(e)
        }
    }

    /**
     * 仅获取植物详细信息（如果已经知道植物名称）
     * 适用于重新获取详情或刷新详情内容
     */
    suspend fun refreshPlantDetails(plantName: String): Result<String> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "刷新植物详情: $plantName")
        return@withContext getPlantDetailsFromAliyun(plantName)
    }
}