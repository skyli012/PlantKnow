package com.hailong.plantknow.network

import com.hailong.plantknow.model.RecognitionResponse
import com.hailong.plantknow.utils.Constants
import retrofit2.http.*
import retrofit2.Response

/**
 * 百度AI平台API服务接口
 */
interface BaiduApiService {

    /**
     * 获取Access Token
     * @param grantType 固定为 "client_credentials"
     * @param clientId 应用的API Key
     * @param clientSecret 应用的Secret Key
     * @return 包含access_token的响应
     */
    @FormUrlEncoded
    @POST(Constants.TOKEN_URL_PATH)
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): Response<AccessTokenResponse>

    /**
     * 植物识别
     * 注意：此接口需要在Header中添加access_token
     * @param accessToken 通过getAccessToken获取的有效token
     * @param image 图像的Base64编码字符串 (需UrlEncode)
     * @param url 图片的URL地址 (可选)
     * @param baikeNum 返回百科信息的数量 (可选, 0-2)
     * @return 植物识别结果
     */
    @FormUrlEncoded
    @POST(Constants.PLANT_RECOGNITION_PATH)
    suspend fun recognizePlant(
        @Query("access_token") accessToken: String, //  这个是通过access token 进行鉴权
//        @Header("Authorization") accessToken: String, //  这个是通过api_key 进行鉴权
        @Field("image") image: String, //
        @Field("url") url: String? = null,
        @Field("baike_num") baikeNum: Int? = null
    ): Response<RecognitionResponse>
}

/**
 * 用于解析Token响应的数据类
 */
data class AccessTokenResponse(
    val access_token: String,
    val expires_in: Int, // 有效时间，单位秒 (通常为2592000, 即30天)
    val scope: String,
    val session_key: String,
    val refresh_token: String,
    val session_secret: String
)