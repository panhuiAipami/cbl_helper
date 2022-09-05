package com.qyc.cbl_helper.http

import com.qyc.cbl_helper.http.response.base.BaseResponse
import com.qyc.cbl_helper.http.response.base.CommonResponse
import com.qyc.cbl_helper.model.ChbLoginEncInfo
import cn.cpocar.qyc_cbl.model.ShopConfInfo
import com.qyc.cbl_helper.http.request.*
import com.qyc.cbl_helper.http.request.ChbLoginEnc
import com.qyc.cbl_helper.http.request.ChbSyncReq
import com.qyc.cbl_helper.http.request.HhbSyncReq
import com.qyc.cbl_helper.http.request.SmsSyncReqItemInfo
import com.qyc.cbl_helper.model.PushMessageInfo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * 车百灵 API
 * User: wanggang@cpocar.cn
 * Date: 2020/10/23 13:13
 */
interface CblApi {

    /**
     * 保司短信初筛规则
     */
    @GET("v1/sms/ins_sms_feature_matchers")
    suspend fun insSmsFeatureMatchers(): CommonResponse<List<String>>

    /**
     * 保司短信同步
     */
    @POST("v1/clue/add/cooper_shop/by_sms_v2")
    suspend fun smsSync(@Body req: List<SmsSyncReqItemInfo>): CommonResponse<List<PushMessageInfo>>

    /**
     * Chb同步
     */
    @POST("v1/clue/add/cooper_shop/by_board_chb")
    suspend fun addByChb(@Body req: ChbData): CommonResponse<List<PushMessageInfo>>

    /**
     * 平安同步
     */
    @POST("v1/clue/add/cooper_shop/by_board_hhb")
    suspend fun addByPingAn(@Body req:HhbData): CommonResponse<List<PushMessageInfo>>

    /**
     * Chb登录加密
     */
    @POST("v1/ins/chb/login_enc")
    suspend fun chbLoginEnc(@Body req: ChbLoginEnc): CommonResponse<ChbLoginEncInfo>

    /**
     * 上传通话记录
     */
    @POST("v1/shop/upload_call_journal")
    suspend fun uploadCallJournal(@Body req: List<CallLogSyncReqItemInfo>): BaseResponse

    /**
     * 埋点
     */
    @GET
    suspend fun sampling(@Url url: String): BaseResponse

    /**
     * 获取门店配置信息
     */
    @GET("v1/shop/get_shop_conf")
    suspend fun getShopConf(): CommonResponse<ShopConfInfo>

    /**
     * 上传位置信息
     */
    @POST("v1/emp/position_upload")
    suspend fun positionUpload(@Body req: PositionUploadRequest): BaseResponse

    /**
     * 更新App同步Token
     */
    @POST("v1/shop/update_app_sync_token")
    suspend fun updateAppSyncToken(@Body req: UpdateAppSyncTokenReq): BaseResponse
}