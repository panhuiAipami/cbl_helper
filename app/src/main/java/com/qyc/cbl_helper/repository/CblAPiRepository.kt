package com.qyc.cbl_helper.repository


import com.qyc.cbl_helper.http.CblApi
import com.qyc.cbl_helper.http.CblApiService
import com.qyc.cbl_helper.model.ChbLoginEncInfo
import cn.cpocar.qyc_cbl.model.ShopConfInfo
import com.qyc.cbl_helper.common.PingAnSyncHelper
import com.qyc.cbl_helper.http.coroutineApiCall
import com.qyc.cbl_helper.http.coroutineApiCallBase
import com.qyc.cbl_helper.http.request.*
import com.qyc.cbl_helper.http.request.ChbLoginEnc
import com.qyc.cbl_helper.http.request.ChbSyncReq
import com.qyc.cbl_helper.http.request.HhbSyncReq
import com.qyc.cbl_helper.http.request.SmsSyncReqItemInfo
import com.qyc.cbl_helper.model.PushMessageInfo
import com.qyc.cbl_helper.util.AppUtil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 车百灵 API 相关
 * User: wanggang@cpocar.cn
 * Date: 2020/10/23 13:27
 */
object CblAPiRepository {
    private val mCblApi by lazy { CblApiService.buildApi(CblApi::class.java) }

    /**
     * 获取保司短信特征匹配规则
     * @return List<String>
     */
    suspend fun insSmsFeatureMatchers(): List<String> = withContext(Dispatchers.IO) {
        coroutineApiCall { mCblApi.insSmsFeatureMatchers() }
    }

    /**
     * 保司短信同步
     * @param contents
     * @return List<PushMessageInfo>
     */
    suspend fun addCooperShopClueBySms(contents: List<SmsSyncReqItemInfo>): List<PushMessageInfo> = withContext(Dispatchers.IO) {
        coroutineApiCall { mCblApi.smsSync(SmsData(deviceCode = AppUtil.getUUID(),userId = PingAnSyncHelper.getUserId(),list = contents)) }
    }

    /**
     * Chb同步
     * @param contents
     */
    suspend fun addByChb(deviceCode:String,userId:String,contents: List<ChbSyncReq>): List<PushMessageInfo> = withContext(Dispatchers.IO) {
        coroutineApiCall { mCblApi.addByChb(ChbData(deviceCode = deviceCode,userId = userId,list = contents)) }
    }

    /**
     * 平安同步
     * @param contents
     */
    suspend fun addByPingAn(deviceCode:String,userId:String,contents: List<HhbSyncReq>): List<PushMessageInfo> = withContext(Dispatchers.IO) {
        coroutineApiCall { mCblApi.addByPingAn(HhbData(deviceCode = deviceCode,userId = userId,list = contents)) }
    }

    /**
     * Chb登录加密
     */
    suspend fun chbLoginEnc(acc: String, pwd: String, uuid: String?, brand: String?): ChbLoginEncInfo = withContext(Dispatchers.IO) {
        coroutineApiCall { mCblApi.chbLoginEnc(ChbLoginEnc(a = acc, p = pwd, uuid = uuid, brand = brand)) }
    }

    /**
     * 通话记录同步
     * @param contents
     */
    suspend fun uploadCallLogs(contents: List<CallLogSyncReqItemInfo>): Boolean {
        return coroutineApiCallBase { mCblApi.uploadCallJournal(contents) }
    }

    /**
     * 埋点
     * @param params   埋点参数
     * @return BaseResponse
     */
    suspend fun sampling(params: String): Boolean = withContext(Dispatchers.IO) {
        coroutineApiCallBase { mCblApi.sampling("v1/sampling/board/put?${params}") }
    }

    /**
     * 获取门店配置信息
     * @return ShopConfInfo
     */
    suspend fun getShopConf(): ShopConfInfo = withContext(Dispatchers.IO) {
        coroutineApiCall { mCblApi.getShopConf() }
    }

    /**
     * 上传位置信息
     * @param latitude  维度
     * @param longitude 经度
     * @return BaseResponse
     */
    suspend fun positionUpload(latitude: Double, longitude: Double): Boolean = withContext(Dispatchers.IO) {
        coroutineApiCallBase { mCblApi.positionUpload(PositionUploadRequest(latitude, longitude)) }
    }

    /**
     * 更新App同步Token（目前只有 hhb 在用，就写死了，后面如果有其它平台要用，就把 type 传进来）
     * @param token Token
     * @return BaseResponse
     */
    suspend fun updateAppSyncToken(token: String): Boolean = withContext(Dispatchers.IO) {
        coroutineApiCallBase { mCblApi.updateAppSyncToken(UpdateAppSyncTokenReq(token = token, type = UpdateAppSyncTokenTypeEnum.HHB)) }
    }

}
