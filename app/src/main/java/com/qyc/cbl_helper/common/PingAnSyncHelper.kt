package com.qyc.cbl_helper.common

import android.text.format.DateFormat
import android.util.Log
import com.qyc.cbl_helper.repository.PingAnAPiRepository
import cn.cpocar.qyc_cbl.util.AESCrypt
import com.google.gson.JsonObject
import com.orhanobut.hawk.Hawk
import com.qyc.cbl_helper.callback.CallBackSyncStatus
import com.qyc.cbl_helper.constant.AppConstant
import com.qyc.cbl_helper.http.request.HhbSyncReq
import com.qyc.cbl_helper.repository.CblAPiRepository
import com.qyc.cbl_helper.util.AppUtil
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

/**
 * 平安好伙伴信息同步辅助类
 */
object PingAnSyncHelper {
    /** 本地存储Key - toekn */
    private const val LS_KEY_TOKEN = "ls_key_ping_an_token"

    /** 本地存储Key - phone */
    private const val LS_KEY_PHONE = "ls_key_ping_an_phone"
    /** 本地存储Key - userCode */
    private const val LS_KEY_USER_CODE = "ls_key_ping_an_user_code"

    /** 本地存储Key - mIsOpen */
    private const val LS_KEY_IS_OPEN = "ls_key_ping_an_is_open"

    /** 本地存储Key - mUserId */
    const val LS_KEY_USER_ID = "ls_key_user_id"

    private const val LS_KEY_PINGAN_LAST_SYNC_TIME = "ls_key_pingAn_last_sync_time"
    private val pattern: String = "yyyy-MM-dd HH:mm:ss"
    private val format: SimpleDateFormat = SimpleDateFormat(pattern);

    private var mToken: String? = null
    private var mPhone: String? = null
    private var mUserId: String = ""
    private var mUserCode: String? = null
    private var mIsOpen: Boolean = false
    private lateinit var callBack: CallBackSyncStatus



    fun init() {
        if (null == mToken) {
            mToken = Hawk.get<String>(LS_KEY_TOKEN)
        }
        if (null == mPhone) {
            mPhone = Hawk.get<String>(LS_KEY_PHONE)
        }

        mUserId = Hawk.get<String>(LS_KEY_USER_ID,"")

        if (null == mUserCode) {
            mUserCode = Hawk.get<String>(LS_KEY_USER_CODE)
        }
        mIsOpen = Hawk.get<Boolean>(LS_KEY_IS_OPEN, false)
    }

    fun getToken(): String? = mToken
    fun getUserId(): String = mUserId
    fun getPhone(): String? = mPhone
    fun getUserCode(): String? = mUserCode
    fun getIsOpen(): Boolean = mIsOpen


    fun setToken(token: String?) {
        mToken = token
        if (token.isNullOrBlank()) {
            Hawk.delete(LS_KEY_TOKEN)
        } else {
            Hawk.put(LS_KEY_TOKEN, mToken)
        }
    }

    fun setIsOpen(isOpen: Boolean) {
        mIsOpen = isOpen
        Hawk.put(LS_KEY_IS_OPEN, isOpen)
    }

    fun setInfo(
        token: String?,
        userId: String,
        userCode: String?,
        isOpen: Boolean
    ) {
        mToken = token
        mUserCode = userCode
        mUserId = userId
        mIsOpen = isOpen
        if (token.isNullOrBlank()) {
            Hawk.delete(LS_KEY_TOKEN)
        } else {
            Hawk.put(LS_KEY_TOKEN, token)
        }
        if (userId.isBlank()) {
            Hawk.delete(LS_KEY_USER_ID)
        } else {
            Hawk.put(LS_KEY_USER_ID, mUserId)
        }
        if (userCode.isNullOrBlank()) {
            Hawk.delete(LS_KEY_USER_CODE)
        } else {
            Hawk.put(LS_KEY_USER_CODE, mUserCode)
        }

        Hawk.put(LS_KEY_IS_OPEN, isOpen)
    }



    suspend fun handleUpload() {
        if (!mIsOpen) {
            Log.i(AppConstant.TAG_HHB_SYNC, "handleUpload() openSync is false")
            return
        }
        try {
            val startTime: CharSequence =
                DateFormat.format(pattern, Date().time - 7 * 24 * 60 * 60 * 1000)
            val endTimeTime: CharSequence = DateFormat.format(pattern, Date().time)
            val token = getToken()
            val userCode = getUserCode()
            if (token != null) {
                val result = PingAnAPiRepository.getClueTracksList(
                    mToken = token,
                    startTime = startTime as String,
                    endTime = endTimeTime as String
                )

                val code = result?.get("code")?.asString;
                val msg = result?.get("msg")?.asString;
                val listData = result?.getAsJsonObject("data");
                val clueList = listData?.getAsJsonArray("repairInfoList");

                SamplingHelper.sampling(
                    "PingAnSyncHelper", "ping_an_sync_clue_list_res",
                    "resultCode" to (code ?: ""),
                    "code" to (code ?: ""),
                    "message" to (msg ?: ""),
                    "querySize" to "${clueList?.size() ?: "0"}"
                )

                //获取线索数据成功
                if (result != null && code == "000000") {
                    callBack.syncStatus(TpAppTypeEnum.HHB,AppConstant.SYNC_SUCCESS)
                    if (clueList != null) {
                        var lastClueTime = Hawk.get<Long?>(LS_KEY_PINGAN_LAST_SYNC_TIME)
                        val uploadData = mutableListOf<HhbSyncReq>()
                        for (i in clueList.size() - 1 downTo 0) {
                            val item = clueList.get(i).asJsonObject;
                            val clueTime = item.get("updatedAt").asString
                            val nowClueTime = format.parse(clueTime, ParsePosition(0)).time

                            //添加需要上传的不重复数据
                            if (lastClueTime == null || nowClueTime > lastClueTime) {
                                val clueId = item.get("clueId").asString;
                                val reportNo = item.get("reportNo").asString;

                                //获取详情数据 TODO 暂时不获取详情
//                                val resultDetail = PingAnAPiRepository.getClueTracksDetail(
//                                    mToken = token,
//                                    taskId = clueId,
//                                    reportId = reportNo
//                                )
//                                if (resultDetail != null && resultDetail.has("data")) {
//                                    val data = resultDetail.getAsJsonObject("data")
//                                    hhbSyncReq = buildHhbSyncReq(clueId, item, data)
//
//                                } else {
//                                    hhbSyncReq = buildHhbSyncReq(clueId, item, null)
//                                }
                                val hhbSyncReq: HhbSyncReq = buildHhbSyncReq(clueId, item, null)


                                if (clueList.size() == 1) {
                                    try {
                                        //获取线索经纬度,只有一条的时候取经纬度，防止并发判断为异常调用
                                        val resultData = PingAnAPiRepository.getCaseAccidentSite(
                                            mToken = token,
                                            clueId = clueId
                                        )
                                        if (resultData != null) {
                                            val longitude =
                                                resultData.get("damagePlaceGpsX")?.asDouble
                                            val latitude =
                                                resultData.get("damagePlaceGpsY")?.asDouble

                                            hhbSyncReq.lat = latitude
                                            hhbSyncReq.lng = longitude
                                        }
                                    } catch (e: java.lang.Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                Log.i("AddLoadData", "---需上传的数据--->$hhbSyncReq")
                                uploadData.add(hhbSyncReq)
                                lastClueTime = nowClueTime
                            }
                        }
                        Log.i(
                            AppConstant.TAG_HHB_SYNC,
                            "handleUpload() uploadData size：${uploadData.size}"
                        )
                        //去上传
                        var upLoadSuccess = true
                        if (uploadData.isNotEmpty()) {
                            try {
                                CblAPiRepository.addByPingAn(AppUtil.getUUID(), mUserId,uploadData)
                            } catch (e: Exception) {
                                upLoadSuccess = false
                                Log.e(
                                    AppConstant.TAG_HHB_SYNC,
                                    "handleUpload() call addByHhb fail e：${e.message}"
                                )
                                null
                            }?.forEach { pushMsg ->
                                PushMessageNotificationHelper.showNewClueNotify(pushMsg)
                            }
                        }
                        if (upLoadSuccess) Hawk.put(LS_KEY_PINGAN_LAST_SYNC_TIME, lastClueTime)

                        var size = uploadData.size
                        SamplingHelper.sampling(
                            "PingAnSyncHelper", "ping_an_sync_clue",
                            "querySize" to "${clueList.size()}",
                            "uploadSize" to "$size", "clueTime" to (if(size >0) uploadData[size-1].time.toString() else "0")
                        )
                        callBack.syncStatus(TpAppTypeEnum.HHB,AppConstant.SYNC_SUCCESS)
                    }
                } else {//获取数据失败
                    clearAllInfo(401)
                }
            } else {
                //token失效
                clearAllInfo(401)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun buildHhbSyncReq(clueId: String, item: JsonObject, data: JsonObject?): HhbSyncReq {
        return HhbSyncReq(
            licensePlate = item.get("carMark")?.asString,
            reportUserName = item.get("reportName")?.asString,
            reportPhone = item.get("phoneNo")?.asString,
            reportNo = item.get("reportNo")?.asString,
            accidentAddress = item.get("accidentPlace")?.asString,
            time = item.get("clueTime")?.asString,
            responseTypeStr = item.get("channelType")?.asString,//责任类型 01 自责， 02 三者
            cooperInsClueTypeStr = (if(item.get("recommendRepairType").isJsonNull) "" else item.get("recommendRepairType")?.asString),//推修类型 01 送修，02 返修

            //详情
            accidentTypeStr = "",//事故类型 1单方，2双方
            carTrimName = "",//车型
            accidentImages = null,// 事故照片

            //经纬度
            lat = null,
            lng = null,
            tpId = AESCrypt.encrypt(clueId)
        )
    }

    fun clearAllInfo(code:Int) {
        setInfo(null, mUserId, mUserCode,mIsOpen)
        callBack.syncStatus(TpAppTypeEnum.HHB,code)

        SamplingHelper.sampling(
            "PingAnSyncHelper", "ping_an_sync_failure",
            "message" to "同步异常" ,
            ("code" to code.toString())
        )
    }


    fun setCallBack(back:CallBackSyncStatus){
        callBack = back
    }

}