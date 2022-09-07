package com.qyc.cbl_helper.common

import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.qyc.cbl_helper.constant.AppConstant
import com.qyc.cbl_helper.repository.CblAPiRepository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * 埋点辅助类
 * User: wanggang@cpocar.cn
 * Date: 2020/10/23 13:35
 */
object SamplingHelper {
//    private val screen by lazy {
//        val (w, h) = AppUtil.getScreenSize()
//        "${w}x$h"
//    }
    private val sysVersion by lazy { Build.VERSION.RELEASE } // 系统版本
    private val brandVersion by lazy { Build.VERSION.INCREMENTAL } // android 厂商 rom 版本号，比如  miui 11.0.5、emui 10.1
    private val brand by lazy { Build.MANUFACTURER } // 品牌
//    private val appVersion by lazy { "Android ${AppUtil.getAppVersion(AppApplication.getInstance())}" }

    /**
     * 埋点数据上报
     */
    fun sampling(url: String, event: String, vararg customParams: Pair<String, String>) {
//        if (!UserInfoHelper.isLogin()) return

        var paramsStr: String? = null
        if (customParams.isNotEmpty()) {
            val strBuild = StringBuilder()
            for ((k, v) in customParams) {
                strBuild.append("$k=$v&")
            }
            paramsStr = strBuild.toString()
            paramsStr = paramsStr.substring(0, paramsStr.length - 1)
            paramsStr = "$paramsStr&params=${paramsStr.replace("&", ";")}"
        }
        sampling(url, event, paramsStr)
    }

    /**
     * 埋点数据上报
     * @param url           页面路径，如 原生：cn.cpocar.qyc_cbl.ui.activity.MainActivity H5 为 URL，打开/关于 APP事件固定 window
     * @param event         事件，使用 SamplingConstant.x.x
     * @param customParams  自定义参数，如 a=1&b=2&params=a=1&b=2
     */
    private fun sampling(url: String, event: String?, customParams: String?) {
//        if (!UserInfoHelper.isLogin()) return

        var finalEvent = ""
        if (!TextUtils.isEmpty(event)) {
            finalEvent = "&event=$event"
        }
        var finalCustomParams = ""
        if (!TextUtils.isEmpty(customParams)) {
            finalCustomParams = "&$customParams"
        }
        val finalParams = "userId=${PingAnSyncHelper.getUserId()}&platform=DevBoard&url=$url$finalEvent&sv=$sysVersion&bv=$brandVersion&adt=${Build.MODEL}&brand=$brand$finalCustomParams"
        Log.i(AppConstant.TAG_COMMON, "sampling() params：$finalParams")
        GlobalScope.launch(Dispatchers.IO) {
            try {
                CblAPiRepository.sampling(finalParams)
            } catch (e: Exception) {
            }
        }
    }
}