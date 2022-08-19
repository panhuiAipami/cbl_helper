package com.qyc.cbl_helper.common

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat

import cn.cpocar.qyc_cbl.util.Tuple3
import com.google.gson.Gson
import com.qyc.cbl_helper.MyApplication
import com.qyc.cbl_helper.R
import com.qyc.cbl_helper.constant.AppConstant
import com.qyc.cbl_helper.model.PushMessageInfo
import java.util.*


/**
 * 推送通知栏消息显示辅助类
 * User: wanggang@cpocar.cn
 * Date: 2020/11/6 09:49
 */
object PushMessageNotificationHelper {
    private const val NOTIFY_DEF_CHANNEL_ID = "cbl_default_push_msg" // 默认渠道，和 notification_helper.dart 里面的保持一致
    private const val NOTIFY_ID = 101
    private const val NOTIFY_ID_CHB_LOGOUT = 102
    private const val NOTIFY_ID_HHB_LOGOUT = 103
    private const val NOTIFY_TAG_TP_APP_LOGOUT = "tp_app_logout"
    private const val CHB_LOGOUT = "TO_CHB_LOGIN_PAGE"
    private const val PINGAN_LOGOUT = "TO_PingAn_LOGIN_PAGE"
    private val gson by lazy { Gson() }
    private val mNotificationManager: NotificationManager by lazy {
        MyApplication.getInstance().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * 显示新线索通知
     */
     fun showNewClueNotify(pushMessage: PushMessageInfo) {
        Log.i(
            AppConstant.TAG_COMMON,
            "PushMessageNotificationHelper -> showNewClueNotify() _ pushMessage：${pushMessage.title}"
        )
//        val pushInfo = pushMessage.apply { receiveUid = UserInfoHelper.getUid() }
//        val pushInfoJson = gson.toJson(pushInfo)
//        val resId = PushMessageDBHelper.add(pushInfo)
//        PlatformEventPluginHelper.sendEvent(PlatformEventPluginHelper.EVENT_NEW_MSG, pushInfoJson)
//        Log.i(AppConstant.TAG_COMMON, "PushMessageNotificationHelper -> showNewClueNotify() add to DB resId：$resId")
//        if (resId != -1L) {
//            // 除小米外，其它品牌延迟一点再显示通知，防止短信声音打断通知声音
//            if (!DeviceUtil.isMiPhone()) delay(3000)
//            Log.i(
//                AppConstant.TAG_COMMON,
//                "PushMessageNotificationHelper -> showNewClueNotify() _ newClueNotifySound：${UserInfoHelper.getNewClueNotifySound()}"
//            )
//            val context = MyApplication.getInstance()
//            val warpIntent = Intent(Intent.ACTION_VIEW).apply {
//                data = Uri.parse("cbl://main?type=PUSH_MSG_JUMP&params=$pushInfoJson")
//            }
//            val pendingIntent = PendingIntent.getActivity(
//                context,
//                UUID.randomUUID().hashCode(),
//                warpIntent,
//                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//            )
//            val builder = NotificationCompat.Builder(context, UserInfoHelper.getNewClueNotifySound() ?: NOTIFY_DEF_CHANNEL_ID).apply {
//                setContentTitle(pushInfo.title)
//                setContentText(pushInfo.body)
//                setSmallIcon(R.mipmap.ic_launcher)
//                priority = NotificationCompat.PRIORITY_HIGH
//                setContentIntent(pendingIntent)
//                setAutoCancel(true)
//            }
//            mNotificationManager.notify(pushInfo.msgId, NOTIFY_ID, builder.build())
//        }
    }

    /**
     * 显示第三方APP登出通知
     */
    fun showTpAppLogoutNotify(type: TpAppTypeEnum) {
        SamplingHelper.sampling("PushMessageNotificationHelper", "tp_app_sync_re_login_notify", "tpAppType" to type.toString())
        Log.i(AppConstant.TAG_COMMON, "PushMessageNotificationHelper -> showChbLogoutNotify() _ tpAppType：$type")
        val (schemeType, notifyTitle, notifyId) = when (type) {
            TpAppTypeEnum.THB -> Tuple3(CHB_LOGOUT, "太平洋账号异常", NOTIFY_ID_CHB_LOGOUT)
            TpAppTypeEnum.HHB -> Tuple3(PINGAN_LOGOUT, "平安账号异常", NOTIFY_ID_HHB_LOGOUT)
        }
        val context = MyApplication.getInstance()
        val warpIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("cbl://main?type=$schemeType")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            UUID.randomUUID().hashCode(),
            warpIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, "app_sync_fail.wav").apply {
            setContentTitle(notifyTitle)
            setContentText("点击处理")
            setSmallIcon(R.mipmap.ic_launcher)
            priority = NotificationCompat.PRIORITY_HIGH
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }
        mNotificationManager.notify(NOTIFY_TAG_TP_APP_LOGOUT, notifyId, builder.build())
    }

    /**
     * 显示第三方APP登出通知
     */
    fun cancelTpAppLogoutNotify(type: TpAppTypeEnum) {
        val notifyId = when (type) {
            TpAppTypeEnum.THB -> NOTIFY_ID_CHB_LOGOUT
            TpAppTypeEnum.HHB -> NOTIFY_ID_HHB_LOGOUT
        }
        mNotificationManager.cancel(NOTIFY_TAG_TP_APP_LOGOUT, notifyId)
    }

    fun cancel(tag: String) {
        mNotificationManager.cancel(tag, NOTIFY_ID)
    }

    fun cancelAll() {
        mNotificationManager.cancelAll()
    }

}

enum class TpAppTypeEnum {
    /** 太保那货 */
    THB,

    /** 平安那货 */
    HHB
}