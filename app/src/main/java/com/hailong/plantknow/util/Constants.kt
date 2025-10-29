package com.hailong.plantknow.util

import com.hailong.plantknow.BuildConfig

object Constants {
    // --- 百度AI平台凭证 ---
    const val API_KEY = BuildConfig.BAIDU_API_KEY // 替换为您的实际API Key
    const val SECRET_KEY = BuildConfig.BAIDU_SECRET_KEY // 替换为您的实际Secret Key

    // --- API 基础URL ---
    const val BASE_URL = "https://aip.baidubce.com/"

    // --- Token 相关 ---
    const val TOKEN_URL_PATH = "/oauth/2.0/token"
    const val GRANT_TYPE = "client_credentials"

    // --- 植物识别API ---
    const val PLANT_RECOGNITION_PATH = "/rest/2.0/image-classify/v1/plant"

    // --- 网络配置 ---
    const val CONNECT_TIMEOUT_SEC = 15L
    const val READ_TIMEOUT_SEC = 30L
    const val WRITE_TIMEOUT_SEC = 30L
}