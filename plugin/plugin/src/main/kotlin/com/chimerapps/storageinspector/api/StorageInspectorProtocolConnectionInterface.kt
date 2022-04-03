package com.chimerapps.storageinspector.api

import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocol
import com.chimerapps.storageinspector.inspector.StorageInspectorInterface

/**
 * @author Nicola Verbeeck
 */
interface StorageInspectorProtocolConnectionInterface {
    val storageInterface: StorageInspectorInterface

    val protocol: StorageInspectorProtocol

    fun connect()

    fun send(data: String)

    fun close()

    fun addListener(listener: StorageInspectorConnectionListener)
}

interface StorageInspectorConnectionListener {
    fun onConnected() {}

    fun onClosed() {}

    fun onError() {}
}
