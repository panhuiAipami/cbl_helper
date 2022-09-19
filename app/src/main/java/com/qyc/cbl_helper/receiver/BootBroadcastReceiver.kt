package com.qyc.cbl_helper.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.qyc.cbl_helper.MyApplication
import com.qyc.cbl_helper.common.SamplingHelper
import com.qyc.cbl_helper.constant.AppConstant
import com.qyc.cbl_helper.service.SmsSyncService
import com.qyc.cbl_helper.util.AppUtil

/**
 * 开机广播
 */
class BootBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val canSmsSync = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            MyApplication.getInstance(), Manifest.permission.READ_SMS)
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            if (canSmsSync) {
                val serIntent = Intent(context, SmsSyncService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serIntent)
                } else {
                    context.startService(serIntent)
                }
            }
            AppUtil.startLaunchAPP(context, AppConstant.cblPackName, AppConstant.cblMain)
        }
        Log.i(AppConstant.TAG_COMMON,"BootBroadcastReceiver > onReceive() _ action：$action _ canSmsSync：$canSmsSync")
        SamplingHelper.sampling("BootBroadcastReceiver","boot_broadcast", "action" to "$action","canSmsSync" to "$canSmsSync")
    }
}