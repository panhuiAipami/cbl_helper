package com.qyc.cbl_helper.repository


import com.qyc.cbl_helper.http.ChbApi
import com.qyc.cbl_helper.http.ChbApiService
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChbAPiRepository {
    private val mChbApi by lazy { ChbApiService.buildApi(ChbApi::class.java) }

    suspend fun qryVehicleRepairList(
        deviceEnc: String,
        tokenId: String?,
        branchCode: String?,
        vehicleCode: String?,
        vehicleName: String?,
        vehicleLevel: String?
    ): JsonObject? = withContext(Dispatchers.IO) {
        mChbApi.qryVehicleRepairList(
            device = deviceEnc,
            req = mapOf(
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
    }

    suspend fun login(deviceEnc: String, content: String): JsonObject? = withContext(Dispatchers.IO) {
        mChbApi.login(
            device = deviceEnc,
            content = content
        )
    }

}
