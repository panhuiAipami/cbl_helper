package com.qyc.cbl_helper.callback

import com.qyc.cbl_helper.common.TpAppTypeEnum

interface CallBackSyncStatus {
    fun syncStatus(type: TpAppTypeEnum,code:Int)

}