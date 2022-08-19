package cn.cpocar.qyc_cbl.http

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

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


}