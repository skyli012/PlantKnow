package com.hailong.plantknow.network

import com.hailong.plantknow.model.AliyunChatRequest
import com.hailong.plantknow.model.AliyunChatResponse
import com.hailong.plantknow.utils.Constants
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AliyunApiService {

    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer ${Constants.DASHSCOPE_API_KEY}"
    )
    @POST("compatible-mode/v1/chat/completions")
    suspend fun chatCompletion(
        @Body request: AliyunChatRequest
    ): AliyunChatResponse

    // 如果需要流式响应
    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer ${Constants.DASHSCOPE_API_KEY}"
    )
    @POST("compatible-mode/v1/chat/completions")
    suspend fun chatCompletionStream(
        @Body request: AliyunChatRequest
    ): ResponseBody // 对于流式响应，直接返回ResponseBody
}