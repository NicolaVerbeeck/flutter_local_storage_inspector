package com.chimerapps.storageinspector.api

import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocol
import com.chimerapps.storageinspector.util.classLogger
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

/**
 * @author Nicola Verbeeck
 */
class StorageInspectorProtocolConnection(
    private val listener: StorageInspectorConnectionListener,
    ip: String,
    port: Int,
) : WebSocketClient(URI("ws://$ip:$port")) {

    val protocol = StorageInspectorProtocol(this)

    override fun onOpen(handshakedata: ServerHandshake?) {
        listener.onConnected()
    }

    override fun onMessage(message: String) = protocol.onMessage(message)

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        listener.onClosed()
    }

    override fun onError(ex: Exception?) {
        ex?.printStackTrace()
        listener.onError()
        classLogger.info("Connection error:", ex)
    }

}

interface StorageInspectorConnectionListener {
    fun onConnected()

    fun onClosed()

    fun onError()
}