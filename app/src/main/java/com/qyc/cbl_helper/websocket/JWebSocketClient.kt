package com.qyc.cbl_helper.websocket

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

open class JWebSocketClient(serverUri: URI?) : WebSocketClient(serverUri) {
    override fun onOpen(handshakedata: ServerHandshake?) {
        print("Not yet implemented")
    }

    override fun onMessage(message: String?) {
        print("Not yet implemented")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        print("Not yet implemented")
    }

    override fun onError(ex: Exception?) {
        print("Not yet implemented")
    }
}