package cn.cpocar.qyc_cbl.http.request

/**
 * 短信同步 - 请求
 * User: wanggang@cpocar.cn
 * Date: 2020/10/23 13:20
 */
data class SmsSyncReqItemInfo(
        var msgText: String,
        val clueTime: String?,
        val clueTimeStamp: Long?,
        val sendTelNo: String?,
        val phoneTime: Long, // 手机当前时间
        val smsSendTime: Long? // 短信发送时间 
)