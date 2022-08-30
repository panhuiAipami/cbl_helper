package com.qyc.cbl_helper.constant

class AppConstant private constructor() {

    companion object {
        const val DEBUG = true
        const val APP_NAME = "车百灵助手"

        const val SYNC_SUCCESS = 1
        const val SYNC_FAIL = 0

        const val npsMain = "org.golang.app.GoNativeActivity"
        const val npsPackName = "org.nps.client"
        const val cblMain = "cn.cpocar.qyc_cbl.MainActivity"
        const val cblPackName = "cn.cpocar.qyc_cbl"

        const val TOKEN = "app_token"
        const val MESSAGE_TYPE = "messageType"
        const val APP_RESTART = "APP_RESTART"
        const val APP_LOGIN = "APP_LOGIN"
        const val APP_EDIT = "APP_EDIT"
        const val APP_DROP_LINE  = "APP_DROP_LINE"
        const val APP_CLOSE_SYNC  = "APP_CLOSE_SYNC"
        const val APP_START_SYNC  = "APP_START_SYNC"
        const val HEARTBEAT  = "Heartbeat"
        const val REBOOT  = "reboot"


        const val TAG_LOG_CALL = "cbl_tag_lc"
        const val TAG_LOCATION_REPORT = "cbl_location_report"
        const val TAG_SMS_RECEIVER = "cbl_tag_sr"
        const val TAG_HW_PUSH = "cbl_tag_hw_push"
        const val TAG_MI_PUSH = "cbl_tag_mi_push"
        const val TAG_SMS_SYNC = "cbl_tag_sms_sync"
        const val TAG_CHB_SYNC = "cbl_tag_chb_sync"
        const val TAG_HHB_SYNC = "cbl_tag_hhb_sync"
        const val TAG_COMMON = "flutter"
    }

}