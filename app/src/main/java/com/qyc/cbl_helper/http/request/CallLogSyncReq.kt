package com.qyc.cbl_helper.http.request

/**
 * 通话记录 - 请求
 * User: wanggang@cpocar.cn
 * Date: 2021/2/20 15:20
 */
data class CallLogSyncReqItemInfo(
        val callDurationSecs: Int, // 呼叫时长（秒）
        val callTime: String, // 呼叫时间 yyyy-MM-dd HH:mm:ss
        val callType: String, // 呼叫类型，1.呼入，2.呼出()，3.呼入未接
        val nickName: String?, // 对方昵称
        val phone: String // 对方手机号
)