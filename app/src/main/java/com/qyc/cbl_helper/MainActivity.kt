package com.qyc.cbl_helper

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.qyc.cbl_helper.constant.AppConstant
import com.qyc.cbl_helper.databinding.ActivityMainBinding
import com.qyc.cbl_helper.http.ChbApi
import com.qyc.cbl_helper.http.ChbApiService
import com.qyc.cbl_helper.http.PingAnApi
import com.qyc.cbl_helper.http.PingAnApiService
import com.qyc.cbl_helper.repository.PingAnAPiRepository
import com.qyc.cbl_helper.service.SmsSyncService
import com.qyc.cbl_helper.util.AppUtil
import com.qyc.cbl_helper.websocket.JWebSocketClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

class MainActivity : AppCompatActivity() {
    var TAG = "MainActivity"
    var client: JWebSocketClient? = null;
    var BASE_URL = if (AppConstant.DEBUG) "//test-qyc-bxgj.cpocar.cn" else "//ins.qyccar.com"
    var wsUrl = "wss:$BASE_URL/api/v1/ws"
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        initWebSocket()
        startService()
//        openNpcAPP()


        binding.buttonFirst.setOnClickListener(View.OnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                login("18513979168", "108580");
            }
//            Log.e("a","----hasSIMCard---->"+AppUtil.hasSIMCard(this))
        })
    }


    suspend fun login(mobile: String, verifyCode: String) {
        val url = "oauth/access/login/V2/authCodeLogin?telephone=$mobile&code=$verifyCode"
        val result = PingAnAPiRepository.pingAnLogin(url)
        var url2 = "oauth/access/login/sendAuthCode?telephone=$mobile"
        val result2 = PingAnAPiRepository.pingAnLoginSendSms(url2)

        Log.e("", "-------result2----->${result2}")
//        val loginEncRes = CblAPiRepository.chbLoginEnc(
//            acc = getAcc()!!,
//            pwd = getPwd()!!,
//            uuid = AppUtil.getUUID(),
//            brand = Build.MANUFACTURER.uppercase(Locale.getDefault())
//        )
//        val loginRes = ChbAPiRepository.login(
//            deviceEnc = loginEncRes.deviceEnc,
//            content = loginEncRes.loginEnc
//        )
    }

    private fun startService() {
        startService(Intent(this, SmsSyncService::class.java).apply {
            action = SmsSyncService.START_SMS_LISTENER
        })
    }

    private fun openNpcAPP() {
        AppUtil.startLaunchAPP(this, AppConstant.npsPackName, AppConstant.npsMain)
    }

    override fun onResume() {
        super.onResume()
//        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE) //开启心跳检测
//        if (client == null) {
//            Log.e(TAG, "--------onResume---------")
//            initWebSocket()
//        } else if (!client!!.isOpen) {
//            reconnectWs() //进入页面发现断开开启重连
//        }
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "-------onStop----------")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "---------onDestroy--------onDestroy")
        closeConnect()
    }

    /**
     * 初始化websocket
     */
    fun initWebSocket() {
        val uri = URI.create(wsUrl)
        client = object : JWebSocketClient(uri) {
            override fun onMessage(message: String?) {
                super.onMessage(message)
                if (message != "Heartbeat") {
                    Log.i(TAG, "websocket收到消息：$message")
                }
            }

            override fun onOpen(handshakedata: ServerHandshake?) {
                super.onOpen(handshakedata)
                Log.i(TAG, "websocket连接成功")
            }

            override fun onError(ex: Exception?) {
                super.onError(ex)
                Log.i(TAG, "websocket连接错误：$ex")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                super.onClose(code, reason, remote)
                if (code != 1000) {
                    reconnectWs() //意外断开马上重连
                }
                Log.i(TAG, "websocket断开连接：·code:$code·reason:$reason·remote:$remote")
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
            Log.e(TAG, "--------Websocket发送的消息：------->$msg")
            if (client!!.isOpen) {
                client!!.send(msg)
            }
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
//                        Log.e(TAG, "-------开启重连------")
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
                    Log.e(TAG, "心跳包检测websocket连接状态1" + client!!.isOpen.toString() + "/")
                    reconnectWs() //心跳机制发现断开开启重连
                } else {
                    Log.e(TAG, "心跳包检测websocket连接状态2" + client!!.isOpen.toString() + "/")
                    sendMsg("Heartbeat")
                }
            } else {
                Log.e(TAG, "心跳包检测websocket连接状态重新连接")
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
}