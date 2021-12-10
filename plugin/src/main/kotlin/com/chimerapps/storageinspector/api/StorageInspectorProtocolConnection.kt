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
    uri: URI,
) : WebSocketClient(uri) {

    val protocol = StorageInspectorProtocol(this)
    private val listeners = mutableListOf<StorageInspectorConnectionListener>()

    override fun onOpen(handshakedata: ServerHandshake?) {
        synchronized(listeners) {
            listeners.forEach { it.onConnected() }
        }
    }

    override fun onMessage(message: String) = protocol.onMessage(message)

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        synchronized(listeners) {
            listeners.forEach { it.onClosed() }
        }
    }

    override fun onError(ex: Exception?) {
        synchronized(listeners) {
            listeners.forEach { it.onError() }
        }
        classLogger.info("Connection error:", ex)
    }

    fun addListener(listener: StorageInspectorConnectionListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

}

interface StorageInspectorConnectionListener {
    fun onConnected() {}

    fun onClosed() {}

    fun onError() {}
}