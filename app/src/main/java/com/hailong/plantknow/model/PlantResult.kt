package com.hailong.plantknow.model

import com.google.gson.annotations.SerializedName

/**
 * 百科信息数据类
 * 包含描述和百科链接
 */
data class PlantResult(
    @SerializedName("name")
    val plantName: String, // 植物名称

    @SerializedName("score")
    val confidence: Double, // 识别的置信度

    @SerializedName("baike_info")
    val baikeInfo: BaikeInfo? = null  // 百科信息，可能为空
)

/**
 * 百科信息数据类
 * 包含描述和百科链接
 */
data class BaikeInfo(

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("baike_url")
    val baikeUrl: String? = null
)

/**
 * 安全地获取百科描述
 * 若baikeInfo或description为null，则返回空字符串，避免空指针异常。
 */
val PlantResult.description: String        // 安全地获取百科描述，避免空指针异常
    get() = baikeInfo?.description.orEmpty() // 如果你的baikeInfor或description为null，返回空字符串而不是null

/**
 * 直接获取百科链接
 * 保持可空类型，因为部分识别结果可能没有百科信息。
 */
val PlantResult.baikeUrl: String?          // 直接访问百科链接，无需通过baikeInfo中间层
    get() = baikeInfo?.baikeUrl            // 保持可为空的特性，因为链接可能不存在

/**
 * 将置信度（0-1）转换为百分比（0~100）整数。
 * 方便在UI层直接显示百分比值。
 */
val PlantResult.confidencePercent: Int            // 将API返回的0-1范围的小数转换为0-100的整数百分比
    get() = (confidence * 100).toInt()     // 便于在UI中直接显示