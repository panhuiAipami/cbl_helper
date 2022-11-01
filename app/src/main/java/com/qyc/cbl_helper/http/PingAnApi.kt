package com.qyc.cbl_helper.http

import com.google.gson.JsonObject
import com.qyc.cbl_helper.http.response.base.BaseResponse
import retrofit2.http.*

interface PingAnApi {

    /**
     * 线索列表
     */
    @POST("idtm/app/api/repair/app/repair/getPageRepairs")
    suspend fun getClueTracks(@HeaderMap head: @JvmSuppressWildcards LinkedHashMap<String,Any>?, @Body req: @JvmSuppressWildcards Map<String, Any?>): JsonObject?

    /**
     * 线索详情
     */
    @POST("idtm/app/api/repair/app/repair/extend/getRepairDetail")
    suspend fun getPushRepairDetailFromClue(@Header("x-partner-token") token: String, @Body req: @JvmSuppressWildcards Map<String, Any?>): JsonObject?


    /**
     * 获取线索经纬度
     */
    @POST("idtm/app/api/repair/app/repair/extend/getCaseAccidentSite")
    suspend fun getCaseAccidentSite(@HeaderMap head: @JvmSuppressWildcards LinkedHashMap<String,Any>?, @Body req: @JvmSuppressWildcards Map<String, Any?>): JsonObject?

    /**
     * 解锁刷新
     */
    @POST("admp/public/api/oauth/unAuth/login/getRefreshJwtApp")
    suspend fun getRefreshJwtApp(@Header("X-ADMP-TOKEN") token: String): JsonObject?

    /**
     * 检测token
     */
    @POST("admp/public/api/oauth/unAuth/channel/v2/appCheckToken")
    suspend fun appCheckToken(@HeaderMap head: @JvmSuppressWildcards LinkedHashMap<String,Any>,@Body req: @JvmSuppressWildcards Map<String, Any?>): JsonObject?


    /**
     * 平安验证码登录
     */
    @GET
    suspend fun pingAnLogin(@Url url: String): JsonObject?

    /**
     * 平安获取验证码
     */
    @GET
    suspend fun pingAnLoginSendSms(@Url url:String): JsonObject?

}