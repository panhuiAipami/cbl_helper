package com.qyc.cbl_helper.repository


import com.google.gson.JsonObject
import com.qyc.cbl_helper.http.PingAn4sApiService
import com.qyc.cbl_helper.http.PingAnApi
import com.qyc.cbl_helper.http.PingAnApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


object PingAnAPiRepository {
    private val mApi by lazy { PingAnApiService.buildApi(PingAnApi::class.java) }
    private val m4sApi by lazy { PingAn4sApiService.buildApi(PingAnApi::class.java) }

    suspend fun getClueTracksList(
        mToken: String, startTime: String, endTime: String
    ): JsonObject? = withContext(Dispatchers.IO) {

        mApi.getClueTracks(
            head = getIDTMHeads(mToken,"BQTIU-26080"),
            req = mapOf(
                "clueStatus" to "",
                "orderingRule" to "0",
                "pageNo" to 1,
                "pageSize" to 15,
                "clueStartTime" to startTime,
                "clueEndTime" to endTime
            )
        )
    }

    suspend fun getClueTracksDetail(
        mToken: String, reportId: String, taskId: String
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

    suspend fun getCaseAccidentSite(mToken: String, clueId: String): JsonObject? =
        withContext(Dispatchers.IO) {
            mApi.getCaseAccidentSite(
                head = getIDTMHeads(mToken,"BQTIU-26080"),
                req = mapOf("clueId" to clueId)
            )
        }

    suspend fun getRefreshJwtApp(mToken: String): JsonObject? =
        withContext(Dispatchers.IO) {
            m4sApi.getRefreshJwtApp(
                token = mToken,
            )
        }

    suspend fun appCheckToken(mToken: String,uid:String): JsonObject? =
        withContext(Dispatchers.IO) {

            val params = LinkedHashMap<String, Any>()
            params.put("login_user_code",uid)
            params.put("login_tenement_code","480100068584")

            getADMPHeads(mToken,uid).let {
                m4sApi.appCheckToken(
                    head = it,
                    req = params
                )
            }
        }

    suspend fun pingAnLogin(url: String): JsonObject? = withContext(Dispatchers.IO) {
        mApi.pingAnLogin(url = url)
    }

    suspend fun pingAnLoginSendSms(url: String): JsonObject? = withContext(Dispatchers.IO) {
        mApi.pingAnLoginSendSms(url = url)
    }

     fun getADMPHeads(mToken:String,uid:String): LinkedHashMap<String, Any>  {
        val linkedHashMap = LinkedHashMap<String, Any>()
         linkedHashMap["X-ADMP-PLATFORM"] = "ADMP"
         linkedHashMap["X-ADMP-USER-CODE"] = uid
         linkedHashMap["X-ADMP-APP-VERSION"] = "2.14.0"
         linkedHashMap["X-ADMP-AGENT"] = "Android"
         linkedHashMap["X-ADMP-DEVICE-ID"] = "bmE3YTdmODgwMmRhYjQ0YTZhOWI2NzQyYTY4ODJmNzg3"//TODO
         linkedHashMap["X-ADMP-TENEMENT-CODE"] = System.currentTimeMillis().toString()
         linkedHashMap["X-ADMP-TOKEN"] = mToken
         return linkedHashMap
    }

    fun getIDTMHeads(mToken: String,uid:String): LinkedHashMap<String, Any> {
        val str1 = UUID.randomUUID().toString()
        val linkedHashMap = LinkedHashMap<String, Any>()
        linkedHashMap["Host"] = "icore-idtm.pingan.com.cn"
        linkedHashMap["x-idtm-device-id"] = "0000-0000-0000-0000"
        linkedHashMap["admp-platform"] = "ADMP_APP"
        linkedHashMap["x-idtm-nonce"] = str1
        linkedHashMap["x-partner-source"] = "ADMP"
        linkedHashMap["x-idtm-platform-type"] = "ICORE_ADMP"
        linkedHashMap["x-idtm-app-type"] = "H5"
        linkedHashMap["x-idtm-timestamp"] = System.currentTimeMillis().toString() + ""
        linkedHashMap["auth-type"] = "PROXY_PARTNER"
        linkedHashMap["x-partner-token"] = mToken
        linkedHashMap["x-partner-invoker"] = "ICORE_IDTM"
        linkedHashMap["content-type"] = "application/json; charset=UTF-8"
        linkedHashMap["x-portal-token"] = "0b7307bc5b2ad0f90f5f76ea2f54b729"
        linkedHashMap["x-idtm-channel"] = ""
        linkedHashMap["x-idtm-experience-userid"] = ""
        linkedHashMap["x-partner-uid"] = uid
        linkedHashMap["x-idtm-version"] = "999.999.999"
        linkedHashMap["origin"] = "https://icore-idtm.pingan.com.cn"
        linkedHashMap["x-requested-with"] = "com.pingan.aftercarmarket"
        linkedHashMap["sec-fetch-site"] = "same-origin"
        linkedHashMap["sec-fetch-dest"] = "empty"
        linkedHashMap["referer"] = "https://icore-idtm.pingan.com.cn/idtm/mobile/recommend_repair/"
        return linkedHashMap
    }

}
