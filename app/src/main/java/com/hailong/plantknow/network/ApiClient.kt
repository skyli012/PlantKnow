package com.hailong.plantknow.network

import com.hailong.plantknow.utils.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit客户端管理器
 * 负责创建并提供百度AI与阿里云大模型的ApiService实例
 */
object ApiClient {

    // Retrofit 实例缓存
    private var baiduRetrofit: Retrofit? = null
    private var aliyunRetrofit: Retrofit? = null

    // OkHttp 配置
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(Constants.CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
            // 可选：添加拦截器，如日志、Token自动添加等
            .build()
    }

    /**
     * 获取 BaiduApiService 实例
     */
    val baiduApiService: BaiduApiService by lazy {
        getBaiduRetrofit().create(BaiduApiService::class.java)
    }

    /**
     * 获取 AliyunApiService 实例
     */
    val aliyunApiService: AliyunApiService by lazy {
        getAliyunRetrofit().create(AliyunApiService::class.java)
    }

    /**
     * 构建或获取百度AI Retrofit实例（懒加载）
     */
    private fun getBaiduRetrofit(): Retrofit {
        if (baiduRetrofit == null) {
            baiduRetrofit = Retrofit.Builder()
                .baseUrl(Constants.BAIDU_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return baiduRetrofit!!
    }

    /**
     * 构建或获取阿里云大模型 Retrofit实例（懒加载）
     */
    private fun getAliyunRetrofit(): Retrofit {
        if (aliyunRetrofit == null) {
            aliyunRetrofit = Retrofit.Builder()
                .baseUrl(Constants.ALIYUN_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return aliyunRetrofit!!
    }
}
