package com.hailong.plantknow.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.hailong.plantknow.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.times

/**
 * 认证帮助类
 * 负责管理Access Token的获取、缓存和刷新
 * 为什么使用object 单个实例
 * 1、认证管理应该是全局唯一的
 * 2、不需要多个认证管理器实例
 * 3、所有模块共享同一token缓存
 */
object AuthHelper {
    private const val PREF_NAME = "baidu_ai_auth"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_EXPIRES_AT = "expires_at" // 存储token过期的时间戳 (毫秒)

    private lateinit var sharedPreferences: SharedPreferences

    /**
     * 初始化AuthHelper
     * @param context Application Context
     */
    fun initialize(context: Context) {
        Log.d("AuthHelper", "初始化 SharedPreferences")
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        Log.d("AuthHelper", "SharedPreferences 初始化完成")
    }

    /**
     * 获取有效的Access Token
     * 如果缓存中没有或已过期，则重新获取
     */
    suspend fun getValidAccessToken(): String = withContext(Dispatchers.IO) {
        val currentTimestamp = System.currentTimeMillis()

        Log.d("AuthHelper", "获取 Access Token")

        // 检查缓存的token是否有效
        val cachedToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        val expiresAt = sharedPreferences.getLong(KEY_EXPIRES_AT, 0)

        if (cachedToken != null && currentTimestamp < expiresAt) {
            // Token有效，直接返回
            Log.d("AuthHelper", "使用缓存的 Token")
            return@withContext "$cachedToken"
        }

        Log.d("AuthHelper", "Token已过期或不存在，获取新Token")

        // 需要获取新Token
        try {
            val response = ApiClient.baiduApiService.getAccessToken(
                grantType = Constants.GRANT_TYPE,
                clientId = Constants.API_KEY,
                clientSecret = Constants.SECRET_KEY
            )

            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.access_token
                val expiresIn = response.body()!!.expires_in // 秒

                Log.d("AuthHelper", "成功获取 Token，过期时间: $expiresIn 秒")

                // 计算过期时间 (提前1分钟过期，避免临界问题)
                val expireTimestamp = currentTimestamp + ((expiresIn - 60) * 1000L)

                // 缓存Token
                sharedPreferences.edit()
                    .putString(KEY_ACCESS_TOKEN, token)
                    .putLong(KEY_EXPIRES_AT, expireTimestamp)
                    .apply()

                Log.d("AuthHelper", "Token已缓存")
                return@withContext "Bearer $token"
            } else {
                Log.e("AuthHelper", "获取Token失败: ${response.code()}, ${response.errorBody()?.string()}")
                throw Exception("Failed to get access token: ${response.errorBody()?.string()}")
            }
        } catch (e: IOException) {
            Log.e("AuthHelper", "网络错误", e)
            throw Exception("Network error when getting access token", e)
        } catch (e: Exception) {
            Log.e("AuthHelper", "获取Token时发生错误", e)
            throw Exception("Error getting access token", e)
        }
    }

    /**
     * 清除缓存的Token（例如用户登出时调用）
     */
    fun clearToken() {
        sharedPreferences.edit().clear().apply()
    }
}