package cn.cpocar.qyc_cbl.http

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ChbApi {

    /**
     * 线索列表
     */
    @POST("rn/qryVehicleRepairList.do")
    suspend fun qryVehicleRepairList(@Header("DEVICE") device: String, @Body req: @JvmSuppressWildcards Map<String, Any?>): JsonObject?

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