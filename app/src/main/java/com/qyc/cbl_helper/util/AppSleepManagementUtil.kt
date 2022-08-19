package com.qyc.cbl_helper.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.qyc.cbl_helper.constant.AppConstant
import com.qyc.cbl_helper.util.AppUtil.Companion.showActivity


/**
 * APP睡眠模式管理工具类
 * User: wanggang@cpocar.cn
 * Date: 2020/10/20 11:13
 */
class AppSleepManagementUtil {

    companion object {
        fun toSettings(context: Context) {
            when {
                DeviceUtil.isHwPhone() -> {
                    Log.i(AppConstant.TAG_COMMON, "AppSleepManagementUtil > toSettings() to HUAWEI setting")
                    try {
                        showActivity(context, "com.huawei.systemmanager", "com.huawei.systemmanager.power.ui.PowerSettingActivity")
                    } catch (e1: Exception) {
                        Log.i(AppConstant.TAG_COMMON, "AppSleepManagementUtil > toSettings() to HUAWEI fail(1) e：${e1.message}")
                        try {
                            val intent = Intent(Settings.ACTION_SETTINGS)
                            context.startActivity(intent.apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                            AppUtil.showLongToast("电池->更多电池设置->休眠时始终保持网络连接'开启'")
                        } catch (e2: Exception) {
                            Log.i(AppConstant.TAG_COMMON, "AppSleepManagementUtil > toSettings() to HUAWEI fail(2) e：${e2.message}")
                        }
                    }
                }
                DeviceUtil.isMiPhone() -> {
                    Log.i(AppConstant.TAG_COMMON, "AppSleepManagementUtil > toSettings() to MI setting")
                    try {
                        showActivity(context, "com.miui.powerkeeper", "com.miui.powerkeeper.ui.ScenarioPowerSavingActivity")
                    } catch (e: Exception) {
                        Log.i(AppConstant.TAG_COMMON, "AppSleepManagementUtil > toSettings() to MI fail e：${e.message}")
                    }
                }
                else -> AppUtil.toSettings(context)
            }
        }
    }

}