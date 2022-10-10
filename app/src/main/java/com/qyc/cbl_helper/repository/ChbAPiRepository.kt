package com.qyc.cbl_helper.repository


import com.google.gson.Gson
import com.qyc.cbl_helper.http.ChbApi
import com.qyc.cbl_helper.http.ChbApiService
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChbAPiRepository {
    private val mChbApi by lazy { ChbApiService.buildApi(ChbApi::class.java) }
    private val gson by lazy { Gson() }

    suspend fun qryVehicleRepairList(
        deviceEnc: String,
        tokenId: String?,
        branchCode: String?,
        vehicleCode: String?,
        vehicleName: String?,
        vehicleLevel: String?
    ): JsonObject? = withContext(Dispatchers.IO) {
        val reqJsonStr = gson.toJson(
            mapOf(
                "reqJson" to mapOf(
                    "pageIndex" to 1,
                    "pageCount" to 5,
                    "vehicleList" to listOf(
                        mapOf(
                            "vehicleCode" to vehicleCode,
                            "vehicleName" to vehicleName,
                            "branchCode" to branchCode,
                            "vehicleLevel" to vehicleLevel
                        )
                    ),
                    "workOrderType" to null,
                    "taskStatus" to null,
                    "callFlag" to null,
                    "branchCode" to branchCode,
                    "repairStatus" to 0
                ),
                "tokenId" to tokenId
            )
        )

        val authCode = CblAPiRepository.chbAuthCodeEncEnc(reqJsonStr)

        mChbApi.qryVehicleRepairList(
            device = deviceEnc,
            authCode = authCode,
            rawContent = reqJsonStr
        )
    }

    suspend fun login(deviceEnc: String, content: String): JsonObject? = withContext(Dispatchers.IO) {
        mChbApi.login(
            device = deviceEnc,
            content = content
        )
    }

}
