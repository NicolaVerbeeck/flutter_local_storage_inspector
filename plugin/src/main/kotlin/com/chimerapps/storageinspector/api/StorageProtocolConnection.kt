package com.chimerapps.storageinspector.api

import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocol
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

/**
 * @author Nicola Verbeeck
 */
class StorageProtocolConnection(
    ip: String,
    port: Int,
) : WebSocketClient(URI("ws://$ip:$port")) {

    private val protocol = StorageInspectorProtocol()

    override fun onOpen(handshakedata: ServerHandshake?) {
    }

    override fun onMessage(message: String) = protocol.onMessage(message)

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        print("Closed")
    }

    override fun onError(ex: Exception?) {
        ex?.printStackTrace()
    }

}