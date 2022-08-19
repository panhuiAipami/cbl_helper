package com.qyc.cbl_helper.http.request

data class UpdateAppSyncTokenReq(val token: String, val type: UpdateAppSyncTokenTypeEnum)

enum class UpdateAppSyncTokenTypeEnum {
    HHB
}