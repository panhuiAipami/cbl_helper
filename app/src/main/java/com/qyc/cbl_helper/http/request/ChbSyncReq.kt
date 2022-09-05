package com.qyc.cbl_helper.http.request

data class ChbData(
    val deviceCode:String?,
    val userId:String?,
    val syncParamChb:List<ChbSyncReq>
)
data class HhbData(
    val deviceCode:String?,
    val userId:String?,
    val syncParamHhb:List<HhbSyncReq>
)

data class ChbSyncReq(
    val licensePlate: String?,
    val reportUserName: String?,
    val reportPhone: String?,
    val reportNo: String?,
    val accidentAddress: String?,
    val time: String?,
    var tpId: String?
)

data class HhbSyncReq(
    val licensePlate: String?,
    val reportUserName: String?,
    val reportPhone: String?,
    val reportNo: String?,
    val accidentAddress: String?,
    val time: String?,
    var lng:Double?,
    var lat:Double?,

    val cooperInsClueTypeStr: String?,//推修类型 01 送修，02 返修
    val carTrimName: String?,//车型
    val accidentTypeStr: String?,//事故类型 01 自责， 02 三者
    val responseTypeStr: String?,//责任类型 1单方，2双方
    val accidentImages: List<String>?,//事故图片
    var tpId: String?//id
)

data class ChbLoginEnc(
    val a: String,
    val p: String,
    val uuid: String?,
    val brand: String?
)
