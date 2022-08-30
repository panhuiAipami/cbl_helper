package com.qyc.cbl_helper.websocket

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

open class JWebSocketClient(serverUri: URI?) : WebSocketClient(serverUri) {


    override fun onOpen(handshakedata: ServerHandshake?) {
    }

    override fun onMessage(message: String?) {
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
    }

    override fun onError(ex: Exception?) {
    }
}