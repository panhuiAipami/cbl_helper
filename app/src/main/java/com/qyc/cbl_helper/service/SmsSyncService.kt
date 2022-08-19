package com.qyc.cbl_helper.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.text.format.DateFormat
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_SERVICE
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import com.qyc.cbl_helper.common.PingAnSyncHelper
import com.qyc.cbl_helper.http.request.SmsSyncReqItemInfo
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import com.qyc.cbl_helper.MyApplication
import com.qyc.cbl_helper.R
import com.qyc.cbl_helper.common.InsSmsMatcherHelper
import com.qyc.cbl_helper.common.PushMessageNotificationHelper
import com.qyc.cbl_helper.common.SamplingHelper
import com.qyc.cbl_helper.constant.AppConstant
import com.qyc.cbl_helper.constant.AppConstant.Companion.TAG_SMS_SYNC
import com.qyc.cbl_helper.model.PushMessageInfo
import com.qyc.cbl_helper.repository.CblAPiRepository
import com.qyc.cbl_helper.smsreceiver.SmsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * 短信同步 Service
 */
class SmsSyncService : Service() {
    private val sdf by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    private val delayMinute: Long by lazy { if (AppConstant.DEBUG) 1L else 5L }
    private val logFile by lazy { "${Environment.getExternalStorageDirectory()}${File.separator}cpocar_cbl.log" }
    private val smsManager by lazy { SmsManager() }
    private var isSyncStarted = false
    private var wakeLock: PowerManager.WakeLock? = null
    private val gson by lazy { Gson() }
    private var index = 1;

    override fun onCreate() {
        super.onCreate()
        index = 1;
        val hasStartSyncSms = Hawk.get<Boolean>(LS_KEY_HAS_START_SYNC_SMS, false)
//        Log.i(TAG_SMS_SYNC,"SmsSyncService -> onCreate() _ isLogin：${UserInfoHelper.isLogin()} _ isSyncStarted：$isSyncStarted _ hasStartSyncSms：$hasStartSyncSms")
        GlobalScope.launch(Dispatchers.IO) { InsSmsMatcherHelper.updateData() }
        createNotifyChannel()
        startForegroundNotify()
        if (hasStartSyncSms) {
            startSmsSyncListener()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.i(TAG_SMS_SYNC, "SmsSyncService -> onStartCommand() _ action：$action _ isSyncStarted：$isSyncStarted")
        when (action) {
            START_ACTIVE_SYNC -> handleSmsSync(intent.getStringExtra(INTENT_KEY_SYNC_MODE) ?: "未知同步方式")
            START_SMS_LISTENER -> startSmsSyncListener()
            STOP_SMS_LISTENER -> stopSmsSyncListener()
        }
        return START_STICKY
    }

    private fun startSmsSyncListener() {
        val canSmsSync =
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.READ_SMS)
        Log.i(TAG_SMS_SYNC, "SmsSyncService -> startSmsSyncListener() _ isSyncStarted：$isSyncStarted _ canSmsSync：$canSmsSync")
        if (!isSyncStarted && canSmsSync) {
            isSyncStarted = true
            Hawk.put(LS_KEY_HAS_START_SYNC_SMS, true)
            heartbeat()
            smsManager.startListener(this, Handler()) {
                handleSmsSync("新短信同步")
            }
        }
    }

    private fun stopSmsSyncListener() {
        Log.i(TAG_SMS_SYNC, "SmsSyncService -> stopSmsSyncListener()")
        Hawk.put(LS_KEY_HAS_START_SYNC_SMS, false)
        isSyncStarted = false
        try {
            wakeLock?.let { if (it.isHeld) it.release() }
        } catch (e: Exception) {
            Log.i(TAG_SMS_SYNC, "SmsSyncService -> stopSmsSyncListener() wakeLock.release fail：${e.message}")
        }
        smsManager.stopListener(this)
        stopForegroundNotify()
        stopSelf()
    }

    /**
     * 处理新打开 APP 时的短信同步（APP在被系统杀死后收到的短信，用这个逻辑来同步）
     * 需要有短信同步功能及短信读取权限才会执行这个逻辑
     * @param syncMode        同步方式
     * @param isEmptySampling 是否数据为空时也埋点
     */
    private fun handleSmsSync(syncMode: String, isEmptySampling: Boolean = true) {
        GlobalScope.launch(Dispatchers.IO) {
            finalSmsSync(syncMode, isEmptySampling)
        }
    }

    @Synchronized
    private suspend fun finalSmsSync(syncMode: String, isEmptySampling: Boolean) {
        // 首页启动获取短信列表，上报未上传的短信
        // 获取最后上传的短信时间
        try {
            var lastUploadInsSmsDate = Hawk.get<Long>(LS_KEY_LAST_UPLOAD_INS_SMS_DATE)
            if (null == lastUploadInsSmsDate) {
                // 没有最后上报的记录日期，就取当前月的（比如9月xx日就取9月1日0点）
                val nowCal = Calendar.getInstance()
                val cal = Calendar.getInstance()
                cal.set(nowCal.get(Calendar.YEAR), nowCal.get(Calendar.MONTH), 1, 0, 0, 0)
                lastUploadInsSmsDate = cal.timeInMillis
            }
            Log.i(TAG_SMS_SYNC, "SmsSyncService -> finalSmsSync() startDate：$lastUploadInsSmsDate _ isEmptySampling：$isEmptySampling")
            val smsList = smsManager.getSmsList(MyApplication.getInstance(), startDate = lastUploadInsSmsDate, maxSize = null)
            val smsEmpty = smsList.isEmpty()
            if (!isEmptySampling && smsEmpty) return

            Log.i(TAG_SMS_SYNC, "SmsSyncService -> finalSmsSync() querySize：${smsList.size}")
            val uploadSmsContents = ArrayList<SmsSyncReqItemInfo>()
            if (!smsEmpty) {
                InsSmsMatcherHelper.updateData()
                var needSyncPinAn = false
                for (info in smsList) {
                    val smsContent = info.smsContent
                    if (InsSmsMatcherHelper.matcher(smsContent)) {
                        //有平安短信需要同步线索
                        if (!needSyncPinAn && smsContent.contains("中国平安")) {
                            needSyncPinAn = true
                        }
                        uploadSmsContents.add(
                            SmsSyncReqItemInfo(
                                msgText = smsContent,
                                sendTelNo = info.address,
                                clueTimeStamp = info.date,
                                clueTime = null,
                                phoneTime = System.currentTimeMillis(),
                                smsSendTime = info.sendDate
                            )
                        )
                    }
                }
                if (needSyncPinAn) {
                    PingAnSyncHelper.handleUpload()
                }
            }
            Log.i(TAG_SMS_SYNC, "SmsSyncService -> finalSmsSync() uploadSize：${uploadSmsContents.size}")
            printLogToSdCard("sms_sync")
            var successSize = 0
            if (uploadSmsContents.isNotEmpty()) {
                val sucParseList: List<PushMessageInfo>? = try {
                    CblAPiRepository.addCooperShopClueBySms(uploadSmsContents)
                } catch (e: Exception) {
                    Log.i(TAG_SMS_SYNC, "SmsSyncService -> finalSmsSync() call addCooperShopClueBySms fail e：${e.message}")
                    null
                }
                successSize = sucParseList?.size ?: 0
                if (null != sucParseList) {
                    Hawk.put(LS_KEY_LAST_UPLOAD_INS_SMS_DATE, smsList.first().date)
                    // 入库及显示通知
                    sucParseList.forEach { pushInfo ->
                        PushMessageNotificationHelper.showNewClueNotify(pushInfo)
                    }
                }
            } else {
                // 如果都不是保司短信，也进行标记
                if (!smsEmpty) Hawk.put(LS_KEY_LAST_UPLOAD_INS_SMS_DATE, smsList.first().date)
            }
            SamplingHelper.sampling(
                "SmsSyncService", "sms_sync",
                "querySize" to "${smsList.size}",
                "uploadSize" to "${uploadSmsContents.size}",
                "successSize" to "$successSize",
                "matcherType" to InsSmsMatcherHelper.getMatcherType(),
                "syncMode" to syncMode
            )
        } catch (e: Exception) {
            Log.i(TAG_SMS_SYNC, "SmsSyncService -> finalSmsSync() fail e：${e.message}")
        }
    }

    private fun printLogToSdCard(content: String) {
        try {
            if (AppConstant.DEBUG && PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                File(logFile).appendText("${sdf.format(Date())}：${content}\n")
            }
        }catch (e:Exception){}
    }

    @SuppressLint("WakelockTimeout")
    private fun heartbeat() {
        // 保持cpu唤醒状态
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SmsSyncService::lock").apply {
                acquire()
            }
        }
        GlobalScope.launch(Dispatchers.IO) {
            while (isSyncStarted) {
//                randomDelayMinute = (1..5).random().toLong()
                Log.i(TAG_SMS_SYNC, "SmsSyncService -> heartbeat...")
                val hour: String = DateFormat.format("HH", Date().time).toString()
                val hours: Int = hour.toInt()
//                if(hours in 7..21) {
//                    Log.i(TAG_SMS_SYNC, "SmsSyncService -> heartbeat $randomDelayMinute min  sync clue")
//                    PingAnSyncHelper.handleUpload()
//                    ThbSyncHelper.handleUpload()
//                }
                handleSmsSync("心跳同步", false)
                printLogToSdCard("sms_sync_heartbeat")
                SamplingHelper.sampling("SmsSyncService", "sms_sync_heartbeat", "index" to "$index")
                index++
                delay(delayMinute * 60 * 1000)
            }
        }
    }

    private fun createNotifyChannel() {
        Log.i(TAG_SMS_SYNC, "SmsSyncService -> createNotifyChannel()")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFY_CHANNEL_ID, "短信同步状态通知", NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableVibration(false)
            channel.enableLights(false)
            channel.vibrationPattern = longArrayOf(0)
            channel.setSound(null, null)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun startForegroundNotify() {
        Log.i(TAG_SMS_SYNC, "SmsSyncService -> startForegroundNotify()")
        val builder = NotificationCompat.Builder(this, NOTIFY_CHANNEL_ID)
        builder.setContentTitle(AppConstant.APP_NAME)
            .setContentText("已开启保司线索同步")
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(PRIORITY_HIGH)
            .setCategory(CATEGORY_SERVICE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    packageManager.getLaunchIntentForPackage(packageName),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        startForeground(601, builder.build())
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.i(TAG_SMS_SYNC, "SmsSyncService -> onTaskRemoved() action：${rootIntent?.action}")
        printLogToSdCard("onTaskRemoved")
        SamplingHelper.sampling("SmsSyncService", "task_removed")
        try {
            (applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager).set(
                AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000,
                PendingIntent.getService(
                    this,
                    1,
                    Intent(applicationContext, SmsSyncService::class.java).also { it.setPackage(packageName) },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
                )
            )
        } catch (e: Exception) {
            Log.i(TAG_SMS_SYNC, "SmsSyncService -> onTaskRemoved() alarm reStart fail：${e.message}")
        }
    }

    private fun stopForegroundNotify() {
        Log.i(TAG_SMS_SYNC, "SmsSyncService -> stopForegroundNotify()")
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.i(TAG_SMS_SYNC, "SmsSyncService -> onDestroy()")
        super.onDestroy()
    }


    companion object {
        private const val LS_KEY_HAS_START_SYNC_SMS = "ls_key_has_start_sync_sms"
        private const val LS_KEY_LAST_UPLOAD_INS_SMS_DATE = "last_upload_ins_sms_date"

        private const val NOTIFY_CHANNEL_ID = "cbl_sms_sync"

        const val INTENT_KEY_SYNC_MODE = "syncMode"

        const val START_ACTIVE_SYNC = "START_ACTIVE_SYNC" // 启动主动同步
        const val START_SMS_LISTENER = "START_SMS_LISTENER" // 启动短信监听
        const val STOP_SMS_LISTENER = "STOP_SMS_LISTENER" // 停止短信监听
    }

}