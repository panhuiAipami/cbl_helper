package com.qyc.cbl_helper.http

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChbApi {

    /**
     * 线索列表
     */
    @POST("rn/qryVehicleRepairList.do")
    @Headers("Content-Type: application/json;charset=UTF-8")
    suspend fun qryVehicleRepairList(@Header("DEVICE") device: String, @Header("AUTHCODE") authCode: String, @Body rawContent: String): JsonObject?

    /**
     * 登录
     */
    @POST("web/login.do")
    suspend fun login(@Header("DEVICE") device: String, @Body content: String): JsonObject?

    /**
     * 获取账号信息
     */
    @POST("rn/qryVehMerchList.do")
    suspend fun qryVehMerchList(@Header("DEVICE") device: String, @Body req: @JvmSuppressWildcards Map<String, Any?>): JsonObject?

}