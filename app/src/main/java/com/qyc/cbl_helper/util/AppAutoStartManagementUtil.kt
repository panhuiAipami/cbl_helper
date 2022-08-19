package com.qyc.cbl_helper.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.qyc.cbl_helper.constant.AppConstant
import com.qyc.cbl_helper.util.AppUtil.Companion.showActivity


/**
 * APP自动启动管理工具类
 * User: wanggang@cpocar.cn
 * Date: 2020/10/26 14:14
 */
class AppAutoStartManagementUtil {

    companion object {
        fun toSettings(context: Context) {
            when {
                DeviceUtil.isHwPhone() -> {
                    Log.i(AppConstant.TAG_COMMON, "AppAutoStartManagementUtil > toSettings() to HUAWEI setting")
                    try {//跳自启动管理
                        showActivity(context, "com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
                    } catch (e: Exception) {
                        Log.i(AppConstant.TAG_COMMON, "AppAutoStartManagementUtil > toSettings() to HUAWEI fail(1) e：${e.message}")
                        try {
                            val intent = Intent(Settings.ACTION_SETTINGS)
                            context.startActivity(intent.apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                            AppUtil.showLongToast("应用和服务->应用启动管理->车百灵'开启'")
                        } catch (e: Exception) {
                            Log.i(AppConstant.TAG_COMMON, "AppAutoStartManagementUtil > toSettings() to HUAWEI fail(2) e：${e.message}")
                        }
                    }
                }
                DeviceUtil.isMiPhone() -> {
                    Log.i(AppConstant.TAG_COMMON, "AppAutoStartManagementUtil > toSettings() to MI setting")
                    try {
                        showActivity(context, "com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                    } catch (e: Exception) {
                        Log.i(AppConstant.TAG_COMMON, "AppAutoStartManagementUtil > toSettings() to MI fail e：${e.message}")
                    }
                }
                DeviceUtil.isOppo() -> {
                    Log.i(AppConstant.TAG_COMMON, "AppAutoStartManagementUtil > toSettings() to OPPO setting")
                    try {
//                        PermissionsUtils.toPermissionSetting(context)
                    } catch (e: Exception) {
                        Log.i(AppConstant.TAG_COMMON, "AppAutoStartManagementUtil > toSettings() to OPPO fail e：${e.message}")
                    }
                }
                DeviceUtil.isVivo() -> {
                    Log.i(AppConstant.TAG_COMMON, "AppAutoStartManagementUtil > toSettings() to VIVO setting")
                    try {
                        showActivity(context, "com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
                    } catch (e: Exception) {
                        Log.i(AppConstant.TAG_COMMON, "AppAutoStartManagementUtil > toSettings() to VIVO fail e：${e.message}")
                    }
                }
            }
        }
    }

}