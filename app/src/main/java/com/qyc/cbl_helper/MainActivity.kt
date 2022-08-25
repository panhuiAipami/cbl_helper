package com.qyc.cbl_helper

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.orhanobut.hawk.Hawk
import com.qyc.cbl_helper.common.PingAnSyncHelper
import com.qyc.cbl_helper.common.ThbSyncHelper
import com.qyc.cbl_helper.constant.AppConstant
import com.qyc.cbl_helper.databinding.ActivityMainBinding
import com.qyc.cbl_helper.service.SmsSyncService
import com.qyc.cbl_helper.util.AppUtil
import com.qyc.cbl_helper.websocket.JWebSocketClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URI

class MainActivity : AppCompatActivity() {
    var TAG = "MainActivity"
    var client: JWebSocketClient? = null;
    var BASE_URL =if (AppConstant.DEBUG) "192.168.9.193:8061/websocket" else "test-qyc-bxgj.cpocar.cn"
    var wsUrl = "ws://$BASE_URL"
    private lateinit var binding: ActivityMainBinding
    lateinit var info:TextView
    lateinit var log:TextView
    lateinit var clean:TextView
    var sb:StringBuilder = StringBuilder()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Hawk.put(AppConstant.TOKEN, "vwVXJ0CIatNMF9WvFXZvGcBHiiKaGbk0W7l0pMtlzIU=")//TODO

        initWebSocket()
        startService()

        info = binding.info
        log = binding.log
        clean = binding.cleanLog
        log.movementMethod = ScrollingMovementMethod.getInstance()

        info.text = "是否插sim卡：${AppUtil.hasSIMCard(this)}；是否联网：${AppUtil.isWifiConnected()}；平安是否同步：${PingAnSyncHelper.getToken()!=null}；太保是否同步：${ThbSyncHelper.getTokenId() != null}"

        binding.buttonFirst.setOnClickListener {
//            openNpcAPP()
//            startAct()
//            testSync()
            buildMessage()

        }
        clean.setOnClickListener {
            sb.clear()
            log.text = ""
        }
    }

    private fun buildMessage() {
        val deviceNo = AppUtil.getUUID()
        val status = if(AppUtil.hasSIMCard(this))1 else 0

        val orgClueSync = listOf(
            mapOf(
                "orgStatus" to "0",
                "orgName" to "太伙伴",
            ),
            mapOf(
                "orgStatus" to "0",
                "orgName" to "好伙伴",
            )
        )
        val params  = mutableMapOf("messageType" to "development_board_status")
        params["hasSIMCard"] = status.toString()
        params["deviceId"] = deviceNo
        params["orgClueSync"] = orgClueSync.toString()

        sendMsg(params.toString())
    }


    /**
     * 测试同步
     */
    fun testSync(){
        GlobalScope.launch(Dispatchers.IO) {
            PingAnSyncHelper.setInfo("eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhZG1wIiwiaWF0IjoxNjYxMjQ1OTI4LCJzdWIiOiJ7XCJjaGFubmVsQ29kZVwiOlwiUE1TXCIsXCJjaXR5Q29kZVwiOlwiMTEwMTAwXCIsXCJkZXB0Q29kZVwiOlwiMjAxNTFcIixcIm9sZENvZGVzXCI6W3tcIm9sZENvZGVcIjpcIjIwMTIxMDkxMjAwNlwiLFwib2xkQ29kZVR5cGVcIjpcIjAxXCJ9XSxcInJvbGVDb2Rlc1wiOltcImFkbXBfdGVuZW1lbnRfZ2VuZXJhbF9tYW5hZ2VyXCJdLFwidGVsZXBob25lXCI6XCIwRjUwRTA0NDA1Mzk3Q0VDRUJDNTBDREYwRjk2MEYwRFwiLFwidGVuZW1lbnRDb2RlXCI6XCIyMDEyMTA5MTIwMDZcIixcInRlbmVtZW50TmFtZVwiOlwi5YyX5Lqs5YyX5pa556aP55Ge5rG96L2m6ZSA5ZSu5pyN5Yqh5pyJ6ZmQ5YWs5Y-4XCIsXCJ0ZW5lbWVudFR5cGVcIjpcIjEwMVwiLFwidXNlckNvZGVcIjpcIllFRlNQLTY3NzIyXCIsXCJ1c2VyTmFtZVwiOlwi5YWo5LyY6L2m5rWL6K-V5py6b3Bwb1wifSIsImp0aSI6IjIyMDgyMzE3MTUwODUwNTcyMjYyNDQyIiwiZXhwIjoxNjYzODM3OTI4LCJuYmYiOjE2NjEyNDU5Mjh9.9JzwlevyGQYJGCVMh5iI-ADdzdmzXAcVPiqR131Ptqc","",true)
            PingAnSyncHelper.handleUpload()
        }
    }

    /**
     * 启动前台服务
     */
    private fun startService() {
        startService(Intent(this, SmsSyncService::class.java).apply {
            action = SmsSyncService.START_SMS_LISTENER
        })
    }


    /**
     * 启动nps
     */
    private fun openNpcAPP() {
        AppUtil.startLaunchAPP(this, AppConstant.npsPackName, AppConstant.npsMain)
    }

    private fun startAct() {
        startActivity(Intent(this, NpsActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE) //开启心跳检测
        if (client == null) {
            initWebSocket()
        } else if (!client!!.isOpen) {
            reconnectWs() //进入页面发现断开开启重连
        }
    }


    /**
     * 初始化websocket
     */
    fun initWebSocket() {
        val uri = URI.create(wsUrl)
        client = object : JWebSocketClient(uri) {
            override  fun onMessage(message: String?) {
                super.onMessage(message)
//                if (message != "Heartbeat") {
                sb.append(message+"\n")
                log.text = sb
                Log.i(TAG, "websocket收到消息：$message")
//                }

                val json = JSONObject(message)
                val action = json.getString(AppConstant.ACTION)
                when (action) {
                    AppConstant.GET_THB_ACCOUNT_INFO -> {
                        val acc = json.getString("acc")
                        val pwd = json.getString("pwd")
                        val tokenId = json.getString("tokenId")
                        val deviceEnc = json.getString("deviceEnc")
                        val branchCode = json.getString("branchCode")
                        val vehicleCode = json.getString("vehicleCode")
                        val vehicleName = json.getString("vehicleName")
                        val vehicleLevel = json.getString("vehicleLevel")

                        ThbSyncHelper.setInfo(
                            acc = acc,
                            pwd = pwd,
                            tokenId = tokenId,
                            deviceEnc = deviceEnc,
                            branchCode = branchCode,
                            vehicleCode = vehicleCode,
                            vehicleName = vehicleName,
                            vehicleLevel = vehicleLevel)
                        GlobalScope.launch(Dispatchers.IO) {
                            ThbSyncHelper.handleUpload()
                        }
                    }

                    AppConstant.GET_HHB_ACCOUNT_INFO -> {
                        val token = json.getString("token")

                        PingAnSyncHelper.setInfo(token,"",true)
                        GlobalScope.launch(Dispatchers.IO) {
                            PingAnSyncHelper.handleUpload()
                        }
                    }

                    AppConstant.DEVELOPMENT_BOARD_STATUS -> {

                    }
                    else -> {

                    }
                }
            }

            override fun onOpen(handshakedata: ServerHandshake?) {
                super.onOpen(handshakedata)
                val msg = "websocket连接成功"
                sb.append(msg+"\n")
                log.text = sb
                Log.i(TAG,msg )
            }

            override fun onError(ex: Exception?) {
                super.onError(ex)
                val msg = "websocket连接错误"
                sb.append(msg+"\n")
//                log.text = sb
                Log.e(TAG, "$msg：$ex")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                super.onClose(code, reason, remote)
                val msg = "websocket断开连接：·code:$code·reason:$reason·remote:$remote"
                sb.append(msg+"\n")
                log.text = sb
                Log.e(TAG, msg)

                if (code != 1000) {
                    reconnectWs() //意外断开马上重连
                }

            }
        }
        client?.connectionLostTimeout = 100 * 1000
        object : Thread() {
            override fun run() {
                try {
                    //connectBlocking多出一个等待操作，会先连接再发送，否则未连接发送会报错
                    client?.connectBlocking()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    /**
     * 发送消息
     *
     * @param msg
     */
    fun sendMsg(msg: String) {
        if (null != client) {
            sb.append(msg+"\n")
            if (client!!.isOpen) {
                client!!.send(msg)
            }
            log.text = sb
        }
    }

    /**
     * 开启重连
     */
    private fun reconnectWs() {
        mHandler.removeCallbacks(heartBeatRunnable)
        object : Thread() {
            override fun run() {
                try {
                    if (client != null) {
                        client?.reconnectBlocking()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    /**
     * 断开连接
     */
    private fun closeConnect() {
        try {
            //关闭websocket
            if (null != client) {
                client?.close()
            }
            //停止心跳
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
                    Log.i(TAG, "心跳包检测websocket连接状态1" + client!!.isOpen.toString() + "/")
                    reconnectWs() //心跳机制发现断开开启重连
                } else {
                    Log.i(TAG, "心跳包检测websocket连接状态2" + client!!.isOpen.toString() + "/")
                    sendMsg("Heartbeat")
                }
            } else {
                Log.i(TAG, "心跳包检测websocket连接状态重新连接")
                //如果client已为空，重新初始化连接
                client = null
                initWebSocket()
            }
            //每隔一定的时间，对长连接进行一次心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE)
        }
    }

    companion object { //每隔10秒进行一次对长连接的心跳检测
        private const val HEART_BEAT_RATE = (30 * 1000).toLong()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeConnect()
    }
}