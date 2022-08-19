package com.qyc.cbl_helper.model

data class SmsInfo(
        var id: Int,
        val smsContent: String,
        val address: String?,
        val date: Long?, // 接收时间
        val sendDate: Long?, // 发送时间
        val read: Int //  是否阅读 0未读， 1已读
)