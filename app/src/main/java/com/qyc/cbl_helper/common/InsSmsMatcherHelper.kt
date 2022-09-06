package com.qyc.cbl_helper.common

import android.util.Log
import com.orhanobut.hawk.Hawk
import com.qyc.cbl_helper.constant.AppConstant.Companion.TAG_SMS_SYNC
import com.qyc.cbl_helper.repository.CblAPiRepository

import java.util.regex.Pattern

object InsSmsMatcherHelper {
    private const val LS_KEY_MATCHER_CACHE = "ls_key_matcher_cache"

    private val defMatchers: List<String> by lazy {
        arrayListOf(
                "【([\\u4e00-\\u9fa5]+(财险|保险|产险))】",
                "\\\\[([\\u4e00-\\u9fa5]+(财险|保险|产险))\\\\]",
                "【(中国平安)】",
                "【(中国太平)】"
        )
    }
    private val matchers = arrayListOf<String>()
    private var matcherType = "未知"
    private var isLoadNetworkMatchersSuc = false
    private var lastLoadNetMillisecond: Long = 0

    suspend fun updateData() {
        matchers.clear()

        // 1. 获取最后的服务器缓存
        val cacheMatchers = Hawk.get<List<String>>(LS_KEY_MATCHER_CACHE)
        if (null != cacheMatchers && cacheMatchers.isNotEmpty()) {
            matcherType = "服务器缓存(${cacheMatchers.size})"
            matchers.addAll(cacheMatchers)
        } else {
            // 2. 上面缓存为空，就取本地写死的
            matcherType = "本地缓存(${defMatchers.size})"
            matchers.addAll(defMatchers)
        }
        Log.i(TAG_SMS_SYNC, "init sms matchers [$matcherType]")
    }

    suspend fun matcher(smsContent: String): Boolean {
        if (System.currentTimeMillis() - lastLoadNetMillisecond > 60 * 60 * 1000) {
            Log.i(TAG_SMS_SYNC, "sms matchers 过期重新加载")
            updateData()
        }

        if (matchers.isNotEmpty()) {
            for (matcher in matchers) {
                if (Pattern.compile(matcher).matcher(smsContent).find()) {
                    return true
                }
            }
        } else {
            Log.i(TAG_SMS_SYNC, "sms matchers is empty")
        }
        return false
    }

    fun getMatcherType() = matcherType

}