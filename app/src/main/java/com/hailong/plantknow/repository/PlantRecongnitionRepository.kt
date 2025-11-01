package com.hailong.lenspoety.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.hailong.plantknow.model.PlantResult
import com.hailong.plantknow.network.ApiClient
import com.hailong.plantknow.network.AuthHelper
import com.hailong.plantknow.util.ImageUtils
import com.hailong.plantknow.util.Result
import com.hailong.plantknow.model.AliyunChatRequest
import com.hailong.plantknow.model.ChatMessage
import com.hailong.plantknow.model.PlantWithDetails
import com.hailong.plantknow.util.Constants

/**
 * 植物识别数据仓库
 * 负责协调百度AI植物识别和阿里云大语言模型的协同工作
 * 实现从图片输入到完整植物知识输出的端到端业务流程
 */
class PlantRecognitionRepository(private val context: Context) {

    /**
     * 从Bitmap图像识别植物种类
     * @param bitmap 待识别的植物图片
     * @return 包含识别结果的Result封装
     */
    suspend fun recognizePlantFromBitmap(bitmap: Bitmap): Result<PlantResult> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "从Bitmap开始识别，图片尺寸: ${bitmap.width}x${bitmap.height}")
        return@withContext try {
            // 步骤1: 将Bitmap转换为Base64编码字符串，满足百度API输入要求
            val base64Image = ImageUtils.bitmapToBase64(bitmap)

            // 步骤2: 获取并处理访问令牌
            val pureToken = AuthHelper.getValidAccessToken()
            // 移除Bearer前缀，适配百度API的认证格式要求

            // 步骤3: 调用百度植物识别API
            Log.d("PlantRepository", "调用百度植物识别API...")
            val response = ApiClient.apiService.recognizePlant(
                accessToken = pureToken,
                image = base64Image,
                baikeNum = 1  // 请求百科信息数量
            )
            Log.d("PlantRepository", "API响应状态: ${response.isSuccessful}")

            // 步骤4: 处理API响应
            if (response.isSuccessful && response.body() != null) {
                val recognitionResponse = response.body()!!
                Log.d("PlantRepository", "百度API日志ID: ${recognitionResponse.logId}")

                // 验证识别结果的有效性
                val results = recognitionResponse.results
                Log.d("PlantRepository", "识别结果数量: ${results?.size ?: 0}")

                if (results != null && results.isNotEmpty()) {
                    // 提取置信度最高的识别结果
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
                // 处理API错误响应
                val errorBody = response.errorBody()?.string()
                Log.e("PlantRepository", "API调用失败: ${response.code()}, $errorBody")
                Result.Error(Exception("API调用失败: ${response.code()}, $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "植物识别过程中发生异常", e)
            Result.Error(e)
        }
    }

    /**
     * 从图片URI识别植物种类
     * 这是更常用的入口点，处理从相册或相机获取的图片
     * @param uri 图片的Uri地址
     * @return 包含识别结果的Result封装
     */
    suspend fun recognizePlantFromUri(uri: Uri): Result<PlantResult> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "从Uri开始识别: $uri")
        return@withContext try {
            // 步骤1: 压缩和优化图片，满足百度API的大小和格式要求
            Log.d("PlantRepository", "开始图片压缩处理...")
            val compressedBitmap = ImageUtils.compressImage(context, uri)
            if (compressedBitmap == null) {
                Log.e("PlantRepository", "图片加载或压缩失败")
                return@withContext Result.Error(IllegalArgumentException("Failed to load or compress image from URI"))
            }
            Log.d("PlantRepository", "图片压缩完成: ${compressedBitmap.width}x${compressedBitmap.height}")

            // 步骤2: 使用压缩后的图片进行识别
            recognizePlantFromBitmap(compressedBitmap)
        } catch (e: Exception) {
            Log.e("PlantRepository", "从Uri识别植物时发生异常", e)
            Result.Error(e)
        }
    }

    // ==================== 阿里云通义千问集成方法 ====================

    /**
     * 调用阿里云通义千问API获取植物的详细知识描述
     * 利用大语言模型的优势提供丰富、生动的植物相关信息
     * @param plantName 需要查询的植物名称
     * @return 包含详细描述的Result封装
     */
    suspend fun getPlantDetailsFromAliyun(plantName: String): Result<String> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "开始调用阿里云API获取植物详情: $plantName")
        return@withContext try {
            // 构建结构化的提示词，引导AI生成标准化的植物介绍
            val prompt = """
                请提供以下植物的详细信息：
                植物名称：$plantName
                
                请按照以下结构提供信息：
                1. 植物简介（50-100字）
                2. 形态特征
                3. 生长环境  
                4. 分布范围
                
                请确保信息准确、详细，适合植物爱好者阅读。
            """.trimIndent()

            // 构建对话消息列表
            val messages = listOf(
                ChatMessage(role = "user", content = prompt)
            )

            // 配置阿里云API请求参数
            val request = AliyunChatRequest(
                model = Constants.QWEN_FLASH_MODEL,  // 使用Flash模型平衡速度和效果
                messages = messages,
                stream = false,  // Android端关闭流式传输以获得更好的稳定性
                extra_body = mapOf("enable_thinking" to true)  // 启用思维链展示推理过程
            )

            Log.d("PlantRepository", "发送阿里云API请求...")
            val response = ApiClient.aliyunApiService.chatCompletion(request)

            // 处理阿里云API响应
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
     * 完整的双AI引擎植物识别流程
     * 结合百度AI的精准识别和阿里云AI的知识扩展
     * @param bitmap 植物图片
     * @return 包含基本识别信息和详细描述的完整植物数据
     */
    suspend fun recognizePlantWithDetails(bitmap: Bitmap): Result<PlantWithDetails> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "开始完整植物识别流程（从Bitmap）...")
        return@withContext try {
            // 阶段1: 百度AI精准识别 - 回答"这是什么植物"
            val recognitionResult = recognizePlantFromBitmap(bitmap)

            if (recognitionResult is Result.Success) {
                val plantResult = recognitionResult.data
                Log.d("PlantRepository", "百度识别成功: ${plantResult.plantName}")

                // 阶段2: 阿里云AI知识扩展 - 回答"这种植物有什么特点"
                Log.d("PlantRepository", "开始调用阿里云API获取详情...")
                val detailsResult = getPlantDetailsFromAliyun(plantResult.plantName)

                if (detailsResult is Result.Success) {
                    Log.d("PlantRepository", "阿里云API调用成功，构建完整植物信息")
                    // 融合两个AI引擎的结果
                    val plantWithDetails = PlantWithDetails(
                        basicInfo = plantResult,
                        detailedDescription = detailsResult.data
                    )
                    Result.Success(plantWithDetails)
                } else {
                    // 降级处理: 阿里云服务不可用时仍返回基础识别结果
                    Log.w("PlantRepository", "阿里云调用失败，启用降级方案：仅返回百度结果")
                    val plantWithDetails = PlantWithDetails(
                        basicInfo = plantResult,
                        detailedDescription = "暂时无法获取详细描述，请稍后重试"
                    )
                    Result.Success(plantWithDetails)
                }
            } else {
                Log.e("PlantRepository", "百度识别失败，流程终止")
                // 百度识别失败，整个流程终止
                Result.Error((recognitionResult as Result.Error).exception)
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "完整识别流程发生异常", e)
            Result.Error(e)
        }
    }

    /**
     * 从图片URI开始的完整植物识别流程
     * 用户友好的入口点，支持相册和相机两种图片来源
     * @param uri 图片Uri地址
     * @return 完整的植物识别结果
     */
    suspend fun recognizePlantWithDetailsFromUri(uri: Uri): Result<PlantWithDetails> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "开始完整植物识别流程（从URI）: $uri")
        return@withContext try {
            // 预处理: 图片压缩和格式转换
            Log.d("PlantRepository", "开始图片压缩预处理...")
            val compressedBitmap = ImageUtils.compressImage(context, uri)
            if (compressedBitmap == null) {
                Log.e("PlantRepository", "图片加载或压缩失败")
                return@withContext Result.Error(IllegalArgumentException("Failed to load or compress image from URI"))
            }
            Log.d("PlantRepository", "图片预处理完成: ${compressedBitmap.width}x${compressedBitmap.height}")

            // 执行完整的双AI识别流程
            recognizePlantWithDetails(compressedBitmap)
        } catch (e: Exception) {
            Log.e("PlantRepository", "从URI识别流程发生异常", e)
            Result.Error(e)
        }
    }

    /**
     * 重新获取或刷新植物详细信息
     * 适用于以下场景：
     * 1. 用户主动刷新植物详情
     * 2. 网络异常后的重试机制
     * 3. 获取更新后的植物知识
     * @param plantName 已知的植物名称
     * @return 更新后的植物详细描述
     */
    suspend fun refreshPlantDetails(plantName: String): Result<String> = withContext(Dispatchers.IO) {
        Log.d("PlantRepository", "刷新植物详情: $plantName")
        return@withContext getPlantDetailsFromAliyun(plantName)
    }
}