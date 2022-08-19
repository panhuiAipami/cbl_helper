package com.qyc.cbl_helper.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Paint
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.orhanobut.hawk.Hawk
import com.qyc.cbl_helper.MyApplication
import com.qyc.cbl_helper.constant.AppConstant
import java.math.BigDecimal
import java.util.*
import kotlin.math.abs


/**
 * App 常用工具类
 * User: wanggang@cpocar.cn
 * Date: 2019/3/7 16:12
 */
class AppUtil {
    companion object {
        private const val LS_KEY_GEN_UUID = "ls_key_gen_uuid"

        /**
         * 生成 uuid （只会生成一次，再生成要清除 APP 数据）
         */
        fun getUUID(): String {
            val cacheUUID: String? = Hawk.get(LS_KEY_GEN_UUID)
            return if (cacheUUID.isNullOrBlank()) {
                val genUUID = UUID.randomUUID().toString()
                Hawk.put(LS_KEY_GEN_UUID, genUUID)
                genUUID
            } else cacheUUID
        }

        /**
         * 通过包名启动第三方应用
         */
        fun startLaunchAPP(context: Context, packageName: String,activityName:String) {
            try {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                intent.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.component = ComponentName(packageName,activityName)
                context.startActivity(intent)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        /**
         * 跳转到当前APP
         */
        fun openCurrentApp() {
            Log.i(AppConstant.TAG_COMMON, "AppUtil > openCurrentApp()")
            try {
                MyApplication.getInstance().packageManager.getLaunchIntentForPackage(MyApplication.getInstance().packageName)?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)?.let {
                    MyApplication.getInstance().startActivity(it)
                }
            } catch (e: Exception) {
                Log.i(AppConstant.TAG_COMMON, "AppUtil > openCurrentApp() fail：${e.message}")
            }
        }


        fun  hasSIMCard(context: Context): Boolean {
            val telMgr:TelephonyManager =
                context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager;
            val simState = telMgr.simState;
            return simState==TelephonyManager.SIM_STATE_READY
        }


        /**
         * 是否在充电中
         */
        fun isCharging(context: Context): Boolean? {
            return try {
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 跳转设置界面
         */
        fun toSettings(context: Context) {
            Log.i(AppConstant.TAG_COMMON, "AppUtil > toSettings()")
            try {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                Log.i(AppConstant.TAG_COMMON, "AppUtil > toSettings() fail：${e.message}")
            }
        }

        /**
         * 跳转MIUI的省电策略设置界面
         */
        fun toMiuiPowerStrategy(context: Context) {
            Log.i(AppConstant.TAG_COMMON, "AppUtil > toMiuiPowerStrategy()")
            try {
                context.startActivity(Intent().apply {
                    component = ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity")
                    putExtra("package_name", context.packageName)
                    putExtra("package_label", AppConstant.APP_NAME)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                Log.i(AppConstant.TAG_COMMON, "AppUtil > toMiuiPowerStrategy() fail：${e.message}")
            }
        }

        /**
         * 到 VIVO 的后台高耗电页面（到不了就到电池页面）
         */
        fun toVivoBackgroundPower(context: Context) {
            Log.i(AppConstant.TAG_COMMON, "AppUtil > toVivoBackgroundPower()")
            try {
                showActivity(context, "com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity")
            } catch (e1: Exception) {
                Log.i(AppConstant.TAG_COMMON, "AppUtil > toVivoBackgroundPower() fail e1：${e1.message}")
                try {
                    showActivity(context, "com.iqoo.powersaving", "com.iqoo.powersaving.PowerSavingManagerActivity")
                } catch (e2: Exception) {
                    Log.i(AppConstant.TAG_COMMON, "AppUtil > toVivoBackgroundPower() fail e2：${e2.message}")
                    toSettings(context)
                }
            }
        }

        /**
         * 跳转到指定应用的首页
         */
        fun showActivity(context: Context, packageName: String) {
            context.startActivity(MyApplication.getInstance().packageManager.getLaunchIntentForPackage(packageName))
        }

        /**
         * 跳转到指定应用的指定页面
         */
        fun showActivity(context: Context, packageName: String, activityDir: String) {
            context.startActivity(Intent().apply {
                component = ComponentName(packageName, activityDir)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }

        /**
         * 判断GPS是否开启
         *
         * @return true 开启 false 关闭
         */
        fun isGPSOpen(): Boolean {
            val locationManager = MyApplication.getInstance().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        /**
         * 判断是否是 wifi
         *
         * @return true 开启 false 关闭
         */
        @Suppress("DEPRECATION")
        @SuppressLint("MissingPermission")
        fun isWifiConnected(): Boolean {
            val connectivityManager = MyApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)?.isConnected ?: false
        }



        /**
         * 列表是否为空
         *
         * @param collection Collection
         * @return true：为空
         */
        fun listEmpty(collection: Collection<*>?) = null == collection || collection.isEmpty()

        /**
         * 判断指定APP是否有安装
         *
         * @param context     Context
         * @param packageName 需要判断的app包名
         * @return true 有安装
         */
        fun isAppInstall(context: Context, packageName: String): Boolean {
            val packageManager = context.packageManager
            val pInfo = packageManager.getInstalledPackages(0)
            if (pInfo.isNotEmpty()) {
                for (packageInfo in pInfo) {
                    if (packageInfo.packageName == packageName) {
                        return true
                    }
                }
            }
            return false
        }

        /**
         * 打开浏览器
         *
         * @param context Context
         * @param url     url
         */
        fun openBrowser(context: Context, url: String) {
            val intent = Intent()
            intent.action = "android.intent.action.VIEW"
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        }

        fun getAppVersion(context: Context): String {
            return try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                "unknown"
            }

        }

        fun getAppVersionInfo(context: Context): Pair<Int, String>? {
            return try {
                val pcg = context.packageManager.getPackageInfo(context.packageName, 0)
                return pcg.versionCode to pcg.versionName
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
         */
        fun dip2px(dpValue: Float): Int {
            val scale = MyApplication.getInstance().resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        /**
         * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
         */
        fun px2dip(pxValue: Float): Int {
            val scale = MyApplication.getInstance().resources.displayMetrics.density
            return (pxValue / scale + 0.5f).toInt()
        }

        /**
         * 根据手机的分辨率从 sp 的单位 转成为 px(像素)
         */
        fun sp2px(spValue: Float): Float {
            val scale = MyApplication.getInstance().resources.displayMetrics.scaledDensity
            return spValue * scale
        }

        /**
         * 获取进程名称
         *
         * @param cxt Context
         * @param pid pid
         * @return 进程名称
         */
        fun getProcessName(cxt: Context, pid: Int): String? {
            val am = cxt.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningApps = am.runningAppProcesses ?: return null
            for (procInfo in runningApps) {
                if (procInfo.pid == pid) {
                    return procInfo.processName
                }
            }
            return null
        }

        /**
         * 判断指定Activity是否在Activity栈中
         *
         * @param activityName 需要判断的Activity完整名称(包含包名)
         * 如 MainActivity.class.getName()
         * @return true 存在， false 不存在
         */
        @Suppress("DEPRECATION")
        fun hasActivity(activityName: String): Boolean {
            val am = MyApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val list = am.getRunningTasks(30)
            var hasMainActivity = false
            for (info in list) {
                if (activityName == info.baseActivity?.className) {
                    hasMainActivity = true
                    break
                }
            }
            return hasMainActivity
        }

        /**
         * 判断是否为数字
         *
         * @param content 内容
         * @return
         */
        fun isDigitsOnly(content: String) = !TextUtils.isEmpty(content) && TextUtils.isDigitsOnly(content)

        fun showShortToast(content: String): Toast = showToast(content, Toast.LENGTH_SHORT)

        fun showLongToast(content: String): Toast = showToast(content, Toast.LENGTH_LONG)

        private fun showToast(content: String, duration: Int = Toast.LENGTH_SHORT): Toast {
            val toast = Toast.makeText(MyApplication.getInstance(), content, duration)
            toast.show()
            return toast
        }

        /**
         * 测量 View
         *
         * @param measureSpec
         * @param defaultSize View 的默认大小
         * @return
         */
        fun measure(measureSpec: Int, defaultSize: Int): Int {
            var result = defaultSize
            val specMode = View.MeasureSpec.getMode(measureSpec)
            val specSize = View.MeasureSpec.getSize(measureSpec)

            if (specMode == View.MeasureSpec.EXACTLY) {
                result = specSize
            } else if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
            return result
        }

        /**
         * 反转数组
         *
         * @param arrays
         * @param <T>
         * @return
        </T> */
        fun <T> reverse(arrays: Array<T>?): Array<T>? {
            if (arrays == null) {
                return null
            }
            val length = arrays.size
            for (i in 0 until length / 2) {
                val t = arrays[i]
                arrays[i] = arrays[length - i - 1]
                arrays[length - i - 1] = t
            }
            return arrays
        }

        /**
         * 测量文字高度
         *
         * @param paint
         * @return
         */
        fun measureTextHeight(paint: Paint): Float {
            val fontMetrics = paint.fontMetrics
            return abs(fontMetrics.ascent) - fontMetrics.descent
        }

        fun showSoftInput(context: Context?, editText: EditText?) {
            if (context == null || editText == null) {
                return
            }
            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(editText, InputMethodManager.RESULT_SHOWN)
                    timer.cancel()
                }
            }, 200)
        }

        fun filterPrice(s: String): String {
            return if (TextUtils.isEmpty(s))
                ""
            else
                s.replace("[\u4e00-\u9fa5]".toRegex(), "").replace("/".toRegex(), "")
        }

        fun getStatusBarHeight(context: Context): Int {
            val resources = context.resources
            return resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"))
        }

        fun argbEvaluate(fraction: Float, startValue: Int, endValue: Int): Int {
            val startA = (startValue shr 24 and 0xff) / 255.0f
            var startR = (startValue shr 16 and 0xff) / 255.0f
            var startG = (startValue shr 8 and 0xff) / 255.0f
            var startB = (startValue and 0xff) / 255.0f

            val endA = (endValue shr 24 and 0xff) / 255.0f
            var endR = (endValue shr 16 and 0xff) / 255.0f
            var endG = (endValue shr 8 and 0xff) / 255.0f
            var endB = (endValue and 0xff) / 255.0f

            // convert from sRGB to linear
            startR = Math.pow(startR.toDouble(), 2.2).toFloat()
            startG = Math.pow(startG.toDouble(), 2.2).toFloat()
            startB = Math.pow(startB.toDouble(), 2.2).toFloat()

            endR = Math.pow(endR.toDouble(), 2.2).toFloat()
            endG = Math.pow(endG.toDouble(), 2.2).toFloat()
            endB = Math.pow(endB.toDouble(), 2.2).toFloat()

            // compute the interpolated color in linear space
            var a = startA + fraction * (endA - startA)
            var r = startR + fraction * (endR - startR)
            var g = startG + fraction * (endG - startG)
            var b = startB + fraction * (endB - startB)

            // convert back to sRGB in the [0..255] range
            a *= 255.0f
            r = Math.pow(r.toDouble(), 1.0 / 2.2).toFloat() * 255.0f
            g = Math.pow(g.toDouble(), 1.0 / 2.2).toFloat() * 255.0f
            b = Math.pow(b.toDouble(), 1.0 / 2.2).toFloat() * 255.0f

            return Math.round(a) shl 24 or (Math.round(r) shl 16) or (Math.round(g) shl 8) or Math.round(b)
        }

        /**
         * 是否允许通知权限
         *
         * @param context Context
         */
        fun isAllowNotification(context: Context): Boolean {
            return NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

        fun formatSize(size: Long): String {
            val kiloByte = size / 1024.0
            if (kiloByte < 1.0) {
                return size.toString() + "Byte"
            } else {
                val megaByte = kiloByte / 1024.0
                if (megaByte < 1.0) {
                    val result1 = BigDecimal(java.lang.Double.toString(kiloByte))
                    return result1.setScale(2, 4).toPlainString() + "KB"
                } else {
                    val gigaByte = megaByte / 1024.0
                    return if (gigaByte < 1.0) {
                        val result2 = BigDecimal(java.lang.Double.toString(megaByte))
                        result2.setScale(2, 4).toPlainString() + "MB"
                    } else {
                        val teraBytes = gigaByte / 1024.0
                        val result4: BigDecimal
                        if (teraBytes < 1.0) {
                            result4 = BigDecimal(java.lang.Double.toString(gigaByte))
                            result4.setScale(2, 4).toPlainString() + "GB"
                        } else {
                            result4 = BigDecimal(teraBytes)
                            result4.setScale(2, 4).toPlainString() + "TB"
                        }
                    }
                }
            }
        }

        /**
         * 获取屏幕宽高
         * @return Tuple2<Int, Int>
         *          参数1：宽
         *          参数2：高
         */
        fun getScreenSize(): Pair<Int, Int> {
            val displayMetrics = MyApplication.getInstance().resources.displayMetrics
            return displayMetrics.widthPixels to displayMetrics.heightPixels
        }

        /**
         * 获取对象完整包名
         * 如：cn.cpocar.qyc.ui.activity.MainActivity
         */
        fun getObjectFullClassName(obj: Any): String = obj.javaClass.canonicalName ?: "unknown_$obj"

        /**
         * 时间格式化（00:00:00，时：分：秒，如果小时为0则不显示）
         */
        fun timeFormat(duration: Long): String {
            val hour = (duration / (1000 * 60 * 60)).toInt()
            val minute = ((duration % (1000 * 60 * 60)) / (1000 * 60)).toInt()
            val second = ((duration % (1000 * 60)) / 1000).toInt()
            return "${if (hour <= 0) "" else if (hour <= 9) "0$hour:" else "$hour:"}${if (minute <= 9) "0$minute" else "$minute"}:${if (second <= 9) "0$second" else "$second"}"
        }
    }

}