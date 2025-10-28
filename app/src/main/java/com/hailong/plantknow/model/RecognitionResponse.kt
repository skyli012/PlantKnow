package com.hailong.plantknow.model

import com.google.gson.annotations.SerializedName

/**
 * 植物识别API整体响应数据类
 * 对应百度植物识别API的完整JSON响应
 */
data class RecognitionResponse (
    @SerializedName("log_id")
    val logId: Long,

    @SerializedName("result")
    val results: List<PlantResult>
)