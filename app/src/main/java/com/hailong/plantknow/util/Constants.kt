package com.hailong.plantknow.util

import com.hailong.plantknow.BuildConfig

/**
 * 应用常量配置类
 * 集中管理所有API配置、网络设置和认证信息
 *
 * 设计原则：
 * 1. 统一管理：所有配置常量集中在一个位置
 * 2. 安全存储：敏感信息通过BuildConfig保护
 * 3. 易于维护：修改配置只需更新此文件
 * 4. 类型安全：使用const val确保编译时常量
 */
object Constants {

    // ==================== 百度AI平台认证信息 ====================

    /**
     * 百度AI平台的API Key
     * 从项目的BuildConfig中读取，避免硬编码在源代码中
     *
     * 需要在app模块的build.gradle中配置：
     * buildConfigField "String", "BAIDU_API_KEY", "\"your_actual_api_key\""
     */
    const val API_KEY = BuildConfig.BAIDU_API_KEY

    /**
     * 百度AI平台的Secret Key
     * 同样从BuildConfig读取，保护敏感信息
     *
     * 需要在app模块的build.gradle中配置：
     * buildConfigField "String", "BAIDU_SECRET_KEY", "\"your_actual_secret_key\""
     */
    const val SECRET_KEY = BuildConfig.BAIDU_SECRET_KEY

    // ==================== 阿里平台认证信息 ====================
    const val ALIYUN_BASE_URL = "https://dashscope.aliyuncs.com/"
    const val QWEN_FLASH_MODEL = "qwen-flash"
    // 阿里云API Key - 从BuildConfig读取
    const val DASHSCOPE_API_KEY = BuildConfig.ALI_API_KEY

    // ==================== API 基础配置 ====================

    /**
     * 百度AI API的基础URL
     * 所有API请求都基于这个URL构建
     */
    const val BAIDU_BASE_URL = "https://aip.baidubce.com/"

    // ==================== Token 认证相关 ====================

    /**
     * 获取Access Token的API路径
     */
    const val TOKEN_URL_PATH = "/oauth/2.0/token"

    /**
     * OAuth 2.0认证的授权类型
     * client_credentials表示客户端凭证模式
     */
    const val GRANT_TYPE = "client_credentials"

    // ==================== 植物识别API ====================

    /**
     * 植物识别API的具体路径
     * 完整的URL为：BASE_URL + PLANT_RECOGNITION_PATH
     */
    const val PLANT_RECOGNITION_PATH = "/rest/2.0/image-classify/v1/plant"

    // ==================== 网络超时配置 ====================

    /**
     * 连接超时时间（秒）
     * 建立TCP连接的最大等待时间
     */
    const val CONNECT_TIMEOUT_SEC = 15L

    /**
     * 读取超时时间（秒）
     * 从服务器读取数据的最大等待时间
     */
    const val READ_TIMEOUT_SEC = 30L

    /**
     * 写入超时时间（秒）
     * 向服务器发送数据的最大等待时间
     */
    const val WRITE_TIMEOUT_SEC = 30L
}