package com.qyc.cbl_helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.qyc.cbl_helper.callback.CallBackSyncStatus
import com.qyc.cbl_helper.common.PingAnSyncHelper
import com.qyc.cbl_helper.common.ThbSyncHelper
import com.qyc.cbl_helper.common.TpAppTypeEnum
import com.qyc.cbl_helper.constant.AppConstant
import com.qyc.cbl_helper.databinding.ActivityMainBinding
import com.qyc.cbl_helper.repository.PingAnAPiRepository
import com.qyc.cbl_helper.util.AppUtil
import com.qyc.cbl_helper.util.AppUtil.Companion.openNpcAPP
import com.qyc.cbl_helper.util.AppUtil.Companion.setTopApp
import com.qyc.cbl_helper.websocket.JWebSocketClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI


class MainActivity : AppCompatActivity() {
    val instance by lazy { this }
    var TAG = "MainActivity"
    var timeReciver: BootBroadcastReceiver? = null
    var client: JWebSocketClient? = null
    private var BASE_URL =
        if (AppConstant.DEBUG) "test-op.cpocar.cn/api/v1/board/ws" else "cbl.qyccar.com/api/v1/board/ws"
    private var wsUrl = "wss://$BASE_URL"
    private lateinit var binding: ActivityMainBinding
    var sb: StringBuilder = StringBuilder()
    var thbStatus: Int = 0
    var hhbStatus: Int = 0
    lateinit var info: TextView
    lateinit var log: TextView
    lateinit var clean: TextView

    private var mCallBack = object : CallBackSyncStatus {
        override fun syncStatus(type: TpAppTypeEnum, code: Int) {
            Log.i(TAG, "$type------syncStatus--------???????????????${code == AppConstant.SYNC_SUCCESS}")
            when (type) {
                TpAppTypeEnum.THB -> {
                    if (code != AppConstant.SYNC_SUCCESS && ThbSyncHelper.getAcc() != null) {
                        buildMessage(AppConstant.APP_DROP_LINE)
                    } else {
                        buildMessage(AppConstant.APP_EDIT)
                    }
                }
                TpAppTypeEnum.HHB -> {
                    if (code != AppConstant.SYNC_SUCCESS && PingAnSyncHelper.getPhone() != null) {
                        buildMessage(AppConstant.APP_DROP_LINE)
                    } else {
                        buildMessage(AppConstant.APP_EDIT)
                    }
                }
            }
            GlobalScope.launch(Dispatchers.Main) {
                setText()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        timeReciver = BootBroadcastReceiver()
        initReceiver()

        ThbSyncHelper.init()
        PingAnSyncHelper.init()
        PingAnSyncHelper.setCallBack(mCallBack)
        ThbSyncHelper.setCallBack(mCallBack)
        initWebSocket()
        AppUtil.startService(this)
        openNpcAPP(this)


        info = binding.info
        log = binding.log
        clean = binding.cleanLog
        log.movementMethod = ScrollingMovementMethod.getInstance()
        setText()

        binding.buttonFirst.setOnClickListener {
//            openNpcAPP(this)
//            testSync()
//            buildMessage(AppConstant.APP_EDIT)
//            disconnect()
//            reStartDevice()
            testSync()
        }
        clean.setOnClickListener {
            sb.clear()
            log.text = ""
        }
    }

    private fun disconnect() {
        client?.closeBlocking()
    }

    private fun setText() {
        info.text =
            "?????????sim??????${AppUtil.hasSIMCard(this)}??????????????????${AppUtil.isConnected()}????????????????????????${PingAnSyncHelper.getToken() != null}????????????????????????${ThbSyncHelper.getTokenId() != null}?????????Id???${AppUtil.getUUID()}"
    }

    private fun buildMessage(action: String) {
        hhbStatus =
            if (PingAnSyncHelper.getToken() != null) AppConstant.SYNC_SUCCESS else AppConstant.SYNC_FAIL
        thbStatus =
            if (ThbSyncHelper.getTokenId() != null) AppConstant.SYNC_SUCCESS else AppConstant.SYNC_FAIL
        val status = if (AppUtil.hasSIMCard(this)) 1 else 0

        val orgClueSync = listOf(
            mapOf(
                "insStatus" to thbStatus,
                "insCode" to "000014",
                "insName" to "?????????",
            ),
            mapOf(
                "insStatus" to hhbStatus,
                "insCode" to "000017",
                "insName" to "?????????",
            )
        )

        val data = mapOf(
            "simStatus" to status,
            "insClueSync" to orgClueSync
        )

        val params = mutableMapOf("messageType" to action)
        params["data"] = Gson().toJson(data)
        params["devBoardId"] = AppUtil.getUUID()
        GlobalScope.launch(Dispatchers.IO) {
            sendMsg(Gson().toJson(params))
        }
    }


    /**
     * ????????????
     */
    fun testSync() {
        GlobalScope.launch(Dispatchers.IO) {
            try {//????????????
                GlobalScope.launch(Dispatchers.IO) {
                    PingAnSyncHelper.setInfo(
                        "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhZG1wIiwiaWF0IjoxNjY2ODM2NjcyLCJzdWIiOiJ7XCJjaGFubmVsQ29kZVwiOlwiUE1TXCIsXCJjaXR5Q29kZVwiOlwiMTEwMTAwXCIsXCJkZXB0Q29kZVwiOlwiMjAxNTFcIixcIm9sZENvZGVzXCI6W3tcIm9sZENvZGVcIjpcIjQ4MDEwMDA2ODU4NFwiLFwib2xkQ29kZVR5cGVcIjpcIjAxXCJ9XSxcInJvbGVDb2Rlc1wiOltcImFkbXBfc2VydmljZV9hZHZpc29yXCJdLFwidGVsZXBob25lXCI6XCIzQkNGMjczMDA3NDQwNzcxOTk1NzQ4MTdDNjk3MkE1NlwiLFwidGVuZW1lbnRDb2RlXCI6XCI0ODAxMDAwNjg1ODRcIixcInRlbmVtZW50TmFtZVwiOlwi5YyX5Lqs56aP55Ge576O5p6X5rG96L2m6ZSA5ZSu5pyN5Yqh5pyJ6ZmQ5YWs5Y-4XCIsXCJ0ZW5lbWVudFR5cGVcIjpcIjEwMVwiLFwidXNlckNvZGVcIjpcIkJRVElVLTI2MDgwXCIsXCJ1c2VyTmFtZVwiOlwi5p2O5aKe5bqGXCJ9IiwianRpIjoiMjIxMDI3MTAxNDEyMDMwN3k3OTEzIiwiZXhwIjoxNjY5NDI4NjcyLCJuYmYiOjE2NjY4MzY2NzJ9.zpVtfEglSualp4K6wlCiWZsob2Un1gtmeT6RRyji3hU",
                        "10025",
                        "BQTIU-26080",
                        true
                    )
                    PingAnSyncHelper.handleUpload()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }


//            ThbSyncHelper.setInfo(
//                userId = "100000129",
//                acc = "w_bffr_gl",
//                pwd = "Cpic12345!",
//                tokenId = "a9a729e9961d391fa4cfc2fab2748d31e2d3017572ad108ccdfa602302722536",
//                deviceEnc = "Xv6zlHoscDb6+5wXOreWXQYVMcs08ozvW0k1ZQ5ow0I3CBHed+L2CPZglnJ4Fkwh",
//                branchCode = "1010100",
//                vehicleCode = "4SF30339",
//                vehicleName = "????????????????????????????????????????????????",
//                vehicleLevel = "B"
//            )
//            ThbSyncHelper.setOpenSync(true)
//            ThbSyncHelper.handleUpload()
        }
    }


    override fun onResume() {
        super.onResume()
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE) //??????????????????
        if (client == null) {
            initWebSocket()
        } else if (!client!!.isOpen) {
            reconnectWs() //????????????????????????????????????
        }
    }


    /**
     * ?????????websocket
     */
    fun initWebSocket() {
        val uri = URI.create(wsUrl)
        client = object : JWebSocketClient(uri) {
            override fun onMessage(message: String?) {
                super.onMessage(message)
                if (message != AppConstant.HEARTBEAT) {
                    sb.append("websocket????????????" + "\n")
                    sb.append(message + "\n")
                    GlobalScope.launch(Dispatchers.Main) {
                        log.text = sb
                    }
                    Log.i(TAG, "websocket???????????????$message")

                    try {
                        val json = JSONObject(message)
                        val action = json.getString(AppConstant.MESSAGE_TYPE)
                        when (action) {
                            AppConstant.APP_LOGIN -> {
                                val data = json.getJSONObject("data")
                                val hasToken = data.has("token")
                                val userId = data.getString("userId")

                                if (hasToken) {//?????????
                                    val userCode = data.getString("userCode")
                                    val token = data.getString("token")
                                    PingAnSyncHelper.setInfo(token, userId, userCode,true)
                                    GlobalScope.launch(Dispatchers.IO) {
                                        PingAnSyncHelper.handleUpload()
                                    }
                                } else {//?????????
                                    val acc = data.getString("acc")
                                    val pwd = data.getString("pwd")
                                    val tokenId = data.getString("tokenId")
                                    val deviceEnc = data.getString("deviceEnc")
                                    val branchCode = data.getString("branchCode")
                                    val vehicleCode = data.getString("vehicleCode")
                                    val vehicleName = data.getString("vehicleName")
                                    val vehicleLevel = data.getString("vehicleLevel")

                                    ThbSyncHelper.setInfo(
                                        userId = userId,
                                        acc = acc,
                                        pwd = pwd,
                                        tokenId = tokenId,
                                        deviceEnc = deviceEnc,
                                        branchCode = branchCode,
                                        vehicleCode = vehicleCode,
                                        vehicleName = vehicleName,
                                        vehicleLevel = vehicleLevel
                                    )
                                    ThbSyncHelper.setOpenSync(true)
                                    ThbSyncHelper.setManualLoginFlag(false)
                                    GlobalScope.launch(Dispatchers.IO) {
                                        ThbSyncHelper.handleUpload()
                                    }
                                }
                            }

                            //??????
                            AppConstant.APP_RESTART -> {
                                reStartDevice()
                            }
                            //????????????
                            AppConstant.APP_CLOSE_SYNC -> {
                                ThbSyncHelper.setOpenSync(false)
                                PingAnSyncHelper.setIsOpen(false)
                            }
                            //????????????
                            AppConstant.APP_START_SYNC -> {
                                ThbSyncHelper.setOpenSync(true)
                                PingAnSyncHelper.setIsOpen(true)
                            }
                            else -> {

                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onOpen(handshakedata: ServerHandshake?) {
                super.onOpen(handshakedata)
                val msg = "websocket????????????-->"
                GlobalScope.launch {
                    sendMsg(AppConstant.HEARTBEAT)
                }
                sb.append(msg + "\n")
                GlobalScope.launch(Dispatchers.Main) {
                    log.text = sb
                }
                Log.i(TAG, msg)
            }

            override fun onError(ex: Exception?) {
                super.onError(ex)
                val msg = "websocket????????????"
                sb.append(msg + "\n")
                GlobalScope.launch(Dispatchers.Main) {
                    log.text = sb
                }
                Log.e(TAG, "$msg???$ex")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                super.onClose(code, reason, remote)
                val msg = "websocket?????????????????code:$code??reason:$reason??remote:$remote"
                sb.append(msg + "\n")
                GlobalScope.launch(Dispatchers.Main) {
                    log.text = sb
                }
                Log.e(TAG, msg)

                if (code != 1000) {
                    reconnectWs() //????????????????????????
                }
            }
        }
        client?.addHeader("deviceCode", AppUtil.getUUID())
        client?.connectionLostTimeout = 100 * 1000
        object : Thread() {
            override fun run() {
                try {
                    //connectBlocking?????????????????????????????????????????????????????????????????????????????????
                    client?.connectBlocking()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    /**
     * ????????????
     *
     * @param msg
     */
    suspend fun sendMsg(msg: String) {
        if (null != client) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sb.append(AppUtil.getCurrentTime() + "-->" + msg + "\n\n")
            }
            if (client!!.isOpen) {
                client!!.send(msg)
            }
            withContext(Dispatchers.Main) {
                log.text = sb
                Log.i(TAG, "$msg")
            }
        }
    }


    /**
     * ????????????
     */
    private fun reconnectWs() {
        mHandler.removeCallbacks(heartBeatRunnable)
        object : Thread() {
            override fun run() {
                try {
                    if (client != null) {
                        var reConnect = client?.reconnectBlocking()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    /**
     * ????????????
     */
    private fun closeConnect() {
        try {
            //??????websocket
            if (null != client) {
                client?.close()
            }
            //????????????
            mHandler.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            client = null
        }
    }

    private val mHandler: Handler = Handler()
    private val heartBeatRunnable: Runnable = object : Runnable {
        override fun run() {
            if (client != null) {
                if (client!!.isClosed) {
                    Log.i(TAG, "websocket????????????,????????????")
                    GlobalScope.launch {
                        reconnectWs() //????????????????????????????????????
                    }
                } else {
                    Log.i(TAG, "????????????websocket????????????-->" + client!!.isOpen.toString())
                    GlobalScope.launch {
                        sendMsg(AppConstant.HEARTBEAT)
                    }
                }
            } else {
                Log.i(TAG, "????????????websocket????????????????????????")
                //??????client?????????????????????????????????
                client = null
                GlobalScope.launch {
                    initWebSocket()
                }
            }

            //????????????????????????????????????????????????????????????
            mHandler.postDelayed(this, HEART_BEAT_RATE)
        }
    }

    companion object { //??????10??????????????????????????????????????????
        private const val HEART_BEAT_RATE = (30 * 1000).toLong()
    }

    private fun reStartDevice() {
        try {
            Runtime.getRuntime().exec("su");
            Runtime.getRuntime().exec("reboot");
        } catch (e: java.lang.Exception) {
            Toast.makeText(applicationContext, "Error! Fail to reboot.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * ??????
     * @param command
     * @return
     */
    private fun exec(command: String): String? {
        var process: Process? = null
        var reader: BufferedReader? = null
        var `is`: InputStreamReader? = null
        var os: DataOutputStream? = null
        return try {
            process = Runtime.getRuntime().exec("su")
            `is` = InputStreamReader(process.inputStream)
            reader = BufferedReader(`is`)
            os = DataOutputStream(process.outputStream)
            os.writeBytes(
                command.trimIndent()
            )
            os.writeBytes("exit\n")
            os.flush()
            var read: Int
            val buffer = CharArray(4096)
            val output = StringBuilder()
            while (reader.read(buffer).also { read = it } > 0) {
                output.append(buffer, 0, read)
            }
            process.waitFor()
            output.toString()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } finally {
            try {
                os?.close()
                `is`?.close()
                reader?.close()
                process?.destroy()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopReceiver()
        closeConnect()
    }

    private fun initReceiver() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_TIME_TICK)
        registerReceiver(timeReciver, filter)
    }

    private fun stopReceiver() {
        unregisterReceiver(timeReciver)
    }


    class BootBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == Intent.ACTION_TIME_TICK) {
//                AppUtil.startLaunchAPP(context, AppConstant.npsPackName, AppConstant.npsMain)
                setTopApp(context)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }
}
