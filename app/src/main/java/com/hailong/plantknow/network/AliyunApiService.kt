package com.hailong.plantknow.network

import com.hailong.plantknow.model.AliyunChatRequest
import com.hailong.plantknow.model.AliyunChatResponse
import okhttp3.ResponseBody

interface AliyunApiService {

    @retrofit2.http.POST("compatible-mode/v1/chat/completions")
    suspend fun chatCompletion(
        @retrofit2.http.Header("Authorization") auth: String,
        @retrofit2.http.Body request: AliyunChatRequest
    ): AliyunChatResponse

    // 如果需要流式响应
    @retrofit2.http.POST("compatible-mode/v1/chat/completions")
    suspend fun chatCompletionStream(
        @retrofit2.http.Header("Authorization") auth: String,
        @retrofit2.http.Body request: AliyunChatRequest
    ): ResponseBody // 对于流式响应，直接返回ResponseBody
}