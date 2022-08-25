package com.qyc.cbl_helper.common

import android.os.Build
import android.util.Log
import com.qyc.cbl_helper.repository.ChbAPiRepository
import cn.cpocar.qyc_cbl.util.AESCrypt
import com.qyc.cbl_helper.util.AppUtil
import com.orhanobut.hawk.Hawk
import com.qyc.cbl_helper.constant.AppConstant
import com.qyc.cbl_helper.http.request.ChbSyncReq
import com.qyc.cbl_helper.repository.CblAPiRepository


/**
 * 太伙伴信息同步辅助类
 * User: wanggang@cpocar.cn
 * Date: 2022/02/28 15:32
 */
object ThbSyncHelper {
    /** 本地存储Key - 是否开启同步 */
    private const val LS_KEY_OPEN_SYNC = "ls_key_thb_open_sync"

    /** 本地存储Key - acc */
    private const val LS_KEY_ACC = "ls_key_thb_acc"

    /** 本地存储Key - pwd */
    private const val LS_KEY_PWD = "ls_key_thb_pwd"

    /** 本地存储Key - tokenId */
    private const val LS_KEY_TOKEN_ID = "ls_key_thb_token_id"

    /** 本地存储Key - DEVICE */
    private const val LS_KEY_DEVICE = "ls_key_thb_device"

    /** 本地存储Key - branchCode */
    private const val LS_KEY_BRANCH_CODE = "ls_key_thb_branch_code"

    /** 本地存储Key - vehicleCode */
    private const val LS_KEY_VEHICLE_CODE = "ls_key_thb_vehicle_code"

    /** 本地存储Key - vehicleName */
    private const val LS_KEY_VEHICLE_NAME = "ls_key_thb_vehicle_name"

    /** 本地存储Key - vehicleLevel */
    private const val LS_KEY_VEHICLE_LEVEL = "ls_key_thb_vehicle_level"

    /** 本地存储Key - 最后同步的ID */
    private const val LS_KEY_CHB_LAST_SYNC_ID = "ls_key_chb_last_sync_id"

    /** 本地存储Key - 需要手动登录标记 */
    private const val LS_KEY_CHB_MANUAL_LOGIN_FLAG = "ls_key_chb_manual_login_flag"

    private var isOpenSync = false
    private var isNeedManualLogin = false
    private var mTokenId: String? = null
    private var mDeviceEnc: String? = null
    private var mAcc: String? = null
    private var mPwd: String? = null
    private var mBranchCode: String? = null
    private var mVehicleCode: String? = null
    private var mVehicleName: String? = null
    private var mVehicleLevel: String? = null

    fun init() {
        isOpenSync = Hawk.get(LS_KEY_OPEN_SYNC, false)
        isNeedManualLogin = Hawk.get(LS_KEY_CHB_MANUAL_LOGIN_FLAG, false)
        if (null == mAcc) {
            mAcc = Hawk.get<String>(LS_KEY_ACC)
        }
        if (null == mPwd) {
            mPwd = Hawk.get<String>(LS_KEY_PWD)
        }
        if (null == mTokenId) {
            mTokenId = Hawk.get<String>(LS_KEY_TOKEN_ID)
        }
        if (null == mDeviceEnc) {
            mDeviceEnc = Hawk.get<String>(LS_KEY_DEVICE)
        }
        if (null == mBranchCode) {
            mBranchCode = Hawk.get<String>(LS_KEY_BRANCH_CODE)
        }
        if (null == mVehicleCode) {
            mVehicleCode = Hawk.get<String>(LS_KEY_VEHICLE_CODE)
        }
        if (null == mVehicleName) {
            mVehicleName = Hawk.get<String>(LS_KEY_VEHICLE_NAME)
        }
        if (null == mVehicleLevel) {
            mVehicleLevel = Hawk.get<String>(LS_KEY_VEHICLE_LEVEL)
        }
    }

    fun getTokenId(): String? = mTokenId
    fun getAcc(): String? = mAcc
    fun getPwd(): String? = mPwd
    fun getDeviceEnc(): String? = mDeviceEnc

    private fun getBranchCode(): String? = mBranchCode
    private fun getVehicleCode(): String? = mVehicleCode
    private fun getVehicleName(): String? = mVehicleName
    private fun getVehicleLevel(): String? = mVehicleLevel

    private fun setTokenId(tokenId: String?) {
        mTokenId = tokenId
        if (tokenId.isNullOrBlank()) {
            Hawk.delete(LS_KEY_TOKEN_ID)
        } else {
            Hawk.put(LS_KEY_TOKEN_ID, mTokenId)
        }
    }

    fun setInfo(
        acc: String?,
        pwd: String?,
        tokenId: String?,
        deviceEnc: String?,
        branchCode: String?,
        vehicleCode: String?,
        vehicleName: String?,
        vehicleLevel: String?
    ) {
        mAcc = acc
        mPwd = pwd
        mTokenId = tokenId
        mDeviceEnc = deviceEnc
        mBranchCode = branchCode
        mVehicleCode = vehicleCode
        mVehicleName = vehicleName
        mVehicleLevel = vehicleLevel
        if (acc.isNullOrBlank()) {
            Hawk.delete(LS_KEY_ACC)
        } else {
            Hawk.put(LS_KEY_ACC, mAcc)
        }
        if (pwd.isNullOrBlank()) {
            Hawk.delete(LS_KEY_PWD)
        } else {
            Hawk.put(LS_KEY_PWD, mPwd)
        }
        if (tokenId.isNullOrBlank()) {
            Hawk.delete(LS_KEY_TOKEN_ID)
        } else {
            Hawk.put(LS_KEY_TOKEN_ID, mTokenId)
        }
        if (deviceEnc.isNullOrBlank()) {
            Hawk.delete(LS_KEY_DEVICE)
        } else {
            Hawk.put(LS_KEY_DEVICE, mDeviceEnc)
        }
        if (branchCode.isNullOrBlank()) {
            Hawk.delete(LS_KEY_BRANCH_CODE)
        } else {
            Hawk.put(LS_KEY_BRANCH_CODE, mBranchCode)
        }
        if (vehicleCode.isNullOrBlank()) {
            Hawk.delete(LS_KEY_VEHICLE_CODE)
        } else {
            Hawk.put(LS_KEY_VEHICLE_CODE, mVehicleCode)
        }
        if (vehicleName.isNullOrBlank()) {
            Hawk.delete(LS_KEY_VEHICLE_NAME)
        } else {
            Hawk.put(LS_KEY_VEHICLE_NAME, mVehicleName)
        }
        if (vehicleLevel.isNullOrBlank()) {
            Hawk.delete(LS_KEY_VEHICLE_LEVEL)
        } else {
            Hawk.put(LS_KEY_VEHICLE_LEVEL, mVehicleLevel)
        }
    }

    fun isOpenSync() = isOpenSync

    fun setOpenSync(open: Boolean) {
        Log.i(AppConstant.TAG_CHB_SYNC, "setOpenSync() open：$open")
        isOpenSync = open
        Hawk.put(LS_KEY_OPEN_SYNC, open)
    }

    fun isNeedManualLogin() = isNeedManualLogin

    fun setManualLoginFlag(need: Boolean) {
        Log.i(AppConstant.TAG_CHB_SYNC, "setManualLoginFlag() need：$need")
        if (isNeedManualLogin && !need) {
            PushMessageNotificationHelper.cancelTpAppLogoutNotify(TpAppTypeEnum.THB)
        }
        Hawk.put(LS_KEY_CHB_MANUAL_LOGIN_FLAG, need)
        isNeedManualLogin = need
    }

    suspend fun handleUpload() {
        if (!isOpenSync) {
            Log.i(AppConstant.TAG_CHB_SYNC, "handleUpload() openSync is false")
            return
        }
        val tokenId = getTokenId()
        val deviceEnc = getDeviceEnc()
        Log.i(AppConstant.TAG_CHB_SYNC, "handleUpload() chb tokenId：$tokenId _ deviceEnc：$deviceEnc _ isNeedManualLogin：$isNeedManualLogin")
        if (!isNeedManualLogin && !tokenId.isNullOrBlank() && !deviceEnc.isNullOrBlank()) {
            try {
                val res = ChbAPiRepository.qryVehicleRepairList(
                    deviceEnc = deviceEnc,
                    tokenId = tokenId,
                    branchCode = getBranchCode(),
                    vehicleCode = getVehicleCode(),
                    vehicleName = getVehicleName(),
                    vehicleLevel = getVehicleLevel()
                )
                val resultCode = res?.get("resultCode")?.asString
                val code = res?.get("code")?.asString
                val message = res?.get("message")?.asString
                val repairInfoList = res?.getAsJsonObject("resultObject")?.getAsJsonArray("repairInfos")
                Log.i(AppConstant.TAG_CHB_SYNC, "handleUpload() chb resultCode：$resultCode _ code：$code _ message：$message")
                SamplingHelper.sampling(
                    "NotificationReceiver", "chb_sync_clue_list_res",
                    "resultCode" to (resultCode ?: ""),
                    "code" to (code ?: ""),
                    "message" to (message ?: ""),
                    "querySize" to "${repairInfoList?.size() ?: "0"}"
                )

                if (("2" == resultCode && true == message?.contains("未登录")) || ("3" == resultCode && true == message?.contains("用户已经登录"))) {
                    // 登出了
                    Log.i(AppConstant.TAG_CHB_SYNC, "handleUpload() chb 登出了...")
                    // 重新登录一次
                    if (!getAcc().isNullOrBlank() && !getPwd().isNullOrBlank()) {
                        Log.i(AppConstant.TAG_CHB_SYNC, "handleUpload() ReLogin")
                        try {
                            val loginEncRes = CblAPiRepository.chbLoginEnc(
                                acc = getAcc()!!,
                                pwd = getPwd()!!,
                                uuid = AppUtil.getUUID(),
                                brand = Build.MANUFACTURER?.toUpperCase()
                            )
                            val loginRes = ChbAPiRepository.login(
                                deviceEnc = loginEncRes.deviceEnc,
                                content = loginEncRes.loginEnc
                            )
                            val newTokenId = loginRes?.get("tokenId")?.asString
                            Log.i(AppConstant.TAG_CHB_SYNC, "handleUpload() ReLogin newTokenId：$newTokenId")
                            SamplingHelper.sampling(
                                "NotificationReceiver", "chb_sync_re_login",
                                "result" to if (!newTokenId.isNullOrBlank()) "SUCCESS" else "FAIL",
                                "message" to "code：${(loginRes?.get("code")?.asString ?: "")} _ resultCode：${(loginRes?.get("resultCode")?.asString ?: "")} _ message：${
                                    (loginRes?.get(
                                        "message"
                                    )?.asString ?: "")
                                }"
                            )
                            if (!newTokenId.isNullOrBlank()) {
                                setTokenId(newTokenId)
                                handleUpload()
                                return
                            }
                        } catch (e: Exception) {
                            SamplingHelper.sampling(
                                "NotificationReceiver", "chb_sync_re_login",
                                "success" to "false",
                                "message" to (e.message ?: "")
                            )
                            Log.i(AppConstant.TAG_CHB_SYNC, "handleUpload() ReLogin fail e：${e.message}")
                        }
                    }
                    // 弹个重新登录吧
                    setManualLoginFlag(true)
                    PushMessageNotificationHelper.showTpAppLogoutNotify(TpAppTypeEnum.THB)
                    clearAllInfo()
                } else {
                    setManualLoginFlag(false)
                    // 上报内容给服务器
                    Log.i(AppConstant.TAG_CHB_SYNC, "handleUpload() find clue size：${repairInfoList?.size()}")
                    var uploadSize = 0
                    if (null != repairInfoList && repairInfoList.size() > 0) {
                        var curMaxId = 0L
                        val lastId = Hawk.get<Long?>(LS_KEY_CHB_LAST_SYNC_ID)
                        Log.i(AppConstant.TAG_CHB_SYNC, "handleUpload() lastId：$lastId")
                        val uploadData = mutableListOf<ChbSyncReq>()
                        for (i in 0 until repairInfoList.size()) {
                            val repairInfo = repairInfoList.get(i).asJsonObject
                            val id = repairInfo.get("id").asLong
                            val license = repairInfo.get("license")?.asString
                            val reportPerson = repairInfo.get("reportPerson")?.asString
                            val caseNo = repairInfo.get("caseNo")?.asString
                            val reportPhone = repairInfo.get("reportPhone")?.asString
                            val accidentArea = repairInfo.get("accidentArea")?.asString
                            val vehicleRepairTime = repairInfo.get("vehicleRepairTime")?.asString
                            val claimSequence = repairInfo.get("claimSequence")?.asString
                            if (null == lastId || id > lastId) {
                                // 上报服务器
                                uploadData.add(
                                    ChbSyncReq(
                                        tpId = AESCrypt.encrypt("${id}_$claimSequence"),
                                        licensePlate = license,
                                        reportUserName = reportPerson,
                                        reportPhone = reportPhone,
                                        reportNo = caseNo,
                                        accidentAddress = accidentArea,
                                        time = vehicleRepairTime
                                    )
                                )
                                uploadSize++
                                if (id > curMaxId) {
                                    curMaxId = id
                                }
                            }
                        }

                        Log.i(AppConstant.TAG_CHB_SYNC, "handleUpload() uploadData size：${uploadData.size}")
                        var hasUploadSuc = false
                        if (uploadData.isNotEmpty()) {
                            try {
                                val uploadResPushMsgList = CblAPiRepository.addByChb(uploadData)
                                hasUploadSuc = true
                                uploadResPushMsgList
                            } catch (e: Exception) {
                                Log.i(AppConstant.TAG_CHB_SYNC, "handleUpload() call addByChb fail e：${e.message}")
                                null
                            }?.forEach { pushMsg ->
                                PushMessageNotificationHelper.showNewClueNotify(pushMsg)
                            }
                        }
                        if (curMaxId > 0 && hasUploadSuc) {
                            Log.i(AppConstant.TAG_CHB_SYNC, "handleUpload() saveMaxId：$curMaxId")
                            Hawk.put(LS_KEY_CHB_LAST_SYNC_ID, curMaxId)
                        }
                    }
                    SamplingHelper.sampling(
                        "NotificationReceiver", "chb_sync_clue",
                        "querySize" to "${repairInfoList?.size() ?: "0"}",
                        "uploadSize" to "$uploadSize"
                    )
                }
            } catch (e: Exception) {
                Log.i(AppConstant.TAG_CHB_SYNC, "handleUpload() chb fail, e：${e.message}")
            }
        } else {
            if (isNeedManualLogin) {
                // 提示需要手动登录
                PushMessageNotificationHelper.showTpAppLogoutNotify(TpAppTypeEnum.THB)
                clearAllInfo()
            }
        }
    }

    private fun clearAllInfo() {
        setInfo(mAcc, mPwd, null, null, null, null, null, null)
    }

}