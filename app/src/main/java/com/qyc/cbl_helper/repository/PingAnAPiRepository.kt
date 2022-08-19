package com.qyc.cbl_helper.repository



import com.qyc.cbl_helper.http.PingAnApi
import com.qyc.cbl_helper.http.PingAnApiService
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PingAnAPiRepository {
    private val mApi by lazy { PingAnApiService.buildApi(PingAnApi::class.java) }

    suspend fun getClueTracksList(
        mToken: String, startTime: String, endTime:String
    ): JsonObject? = withContext(Dispatchers.IO) {
        mApi.getClueTracks(
            token = mToken,
            req = mapOf(
                    "pageNo" to 1,
                    "pageSize" to 15,
                    "clueStartTime" to startTime,
                    "clueEndTime" to endTime
            )
        )
    }

    suspend fun getClueTracksDetail(
        mToken: String, reportId: String, taskId:String
    ): JsonObject? = withContext(Dispatchers.IO) {
        mApi.getPushRepairDetailFromClue(
            token = mToken,
            req = mapOf(
                "reportId" to reportId,
                "taskId" to taskId,
                "vehicleFrameNo" to ""
            )
        )
    }

    suspend fun getCaseAccidentSite(mToken: String, clueId: String): JsonObject? = withContext(Dispatchers.IO) {
        mApi.getCaseAccidentSite(
            token = mToken,
            req = mapOf( "clueId" to clueId)
        )
    }

    suspend fun pingAnLogin( url: String): JsonObject? = withContext(Dispatchers.IO) {
        mApi.pingAnLogin(url = url)
    }

    suspend fun pingAnLoginSendSms( url: String): JsonObject? = withContext(Dispatchers.IO) {
        mApi.pingAnLoginSendSms(url = url)
    }
}
