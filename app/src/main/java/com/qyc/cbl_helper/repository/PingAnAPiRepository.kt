package cn.cpocar.qyc_cbl.repository



import cn.cpocar.qyc_cbl.http.PingAnApi
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
}
