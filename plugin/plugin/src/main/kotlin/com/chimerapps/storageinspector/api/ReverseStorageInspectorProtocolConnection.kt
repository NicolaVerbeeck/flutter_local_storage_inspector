package com.chimerapps.storageinspector.api

import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocol
import com.chimerapps.storageinspector.inspector.StorageInspectorInterface
import com.chimerapps.storageinspector.inspector.StorageInspectorInterfaceImpl
import com.chimerapps.storageinspector.util.classLogger
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.nio.ByteBuffer

/**
 * @author Nicola Verbeeck
 */
class ReverseStorageInspectorProtocolConnection(
    private val waitForStartListener: (port: Int) -> Unit,
) : WebSocketServer(InetSocketAddress(0), 1),
    StorageInspectorProtocolConnectionInterface {

    override val protocol = StorageInspectorProtocol(this)
    override val storageInterface: StorageInspectorInterface = StorageInspectorInterfaceImpl(protocol)

    private var socket: WebSocket? = null

    private val listeners = mutableListOf<StorageInspectorConnectionListener>()

    override fun connect() {
        start()
    }

    override fun addListener(listener: StorageInspectorConnectionListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake?) {
        synchronized(listeners) {
            if (socket != null) {
                conn.close()
                return
            }
            socket = conn
            listeners.forEach { it.onConnected() }
        }
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
        synchronized(listeners) {
            if (conn != socket) conn.close()
            if (socket != null) {
                socket?.close()
                socket = null
                listeners.forEach { it.onClosed() }
            }
        }
    }

    override fun onMessage(conn: WebSocket, message: ByteBuffer) {
        onMessage(conn, Charsets.UTF_8.decode(message).toString())
    }

    override fun onMessage(conn: WebSocket, message: String) {
        synchronized(listeners) {
            if (conn != socket) return conn.close()
            protocol.onMessage(message)
        }
    }

    override fun onError(conn: WebSocket, ex: java.lang.Exception?) {
        synchronized(listeners) {
            if (conn != socket) return conn.close()
            socket?.close()
            socket = null
            listeners.forEach { it.onError() }
        }
        classLogger.info("Connection error:", ex)
    }

    override fun onStart() {
        waitForStartListener(port)
    }

    override fun send(data: String) {
        socket?.send(data)
    }

    override fun close() {
        close()
    }

}
