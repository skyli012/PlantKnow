package com.hailong.plantknow.network
import com.hailong.plantknow.util.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

/**
 * Retrofit客户端管理器
 * 负责创建和提供ApiService实例
 */
object ApiClient {
    private var retrofit: Retrofit? = null
    private lateinit var okHttpClient: OkHttpClient

    /**
     * 获取ApiService实例
     */
    val apiService: ApiService by lazy {
        getRetrofit().create(ApiService::class.java)
    }

    /**
     * 初始化OkHttpClient（带超时设置）
     */
    init {
        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(Constants.CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
            // 可以在这里添加Interceptor，如日志、Token自动添加等
            .build()
    }

    /**
     * 获取Retrofit实例
     */
    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    // 阿里云API客户端
    private val aliyunRetrofit = Retrofit.Builder()
        .baseUrl(Constants.ALIYUN_BASE_URL)
        .client(com.hailong.plantknow.network.ApiClient.okHttpClient) // 复用现有的okHttpClient
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val aliyunApiService: AliyunApiService by lazy {
        aliyunRetrofit.create(AliyunApiService::class.java)
    }
}