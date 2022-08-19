package com.qyc.cbl_helper.http

import com.google.gson.JsonObject
import com.qyc.cbl_helper.http.response.base.BaseResponse
import retrofit2.http.*

interface PingAnApi {

    /**
     * 线索列表
     */
    @POST("aftersale/clue/clueTrack/getClueTracks")
    suspend fun getClueTracks(@Header("x-admp-token") token: String, @Body req: @JvmSuppressWildcards Map<String, Any?>): JsonObject?

    /**
     * 线索详情
     */
    @POST("aftersale/clue/pushDetail/getPushRepairDetailFromClue")
    suspend fun getPushRepairDetailFromClue(@Header("x-admp-token") token: String, @Body req: @JvmSuppressWildcards Map<String, Any?>): JsonObject?


    /**
     * 获取线索经纬度
     */
    @POST("aftersale/clue/pushDetail/getCaseAccidentSite")
    suspend fun getCaseAccidentSite(@Header("x-admp-token") token: String, @Body req: @JvmSuppressWildcards Map<String, Any?>): JsonObject?

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