package com.chimerapps.storageinspector.api

import com.chimerapps.storageinspector.api.protocol.model.StorageType
import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * @author Nicola Verbeeck
 */
class StorageProtocolConnectionTest {

    @Test
    fun testCreateConnection() {
        val socket = StorageInspectorProtocolConnection(listener = object : StorageInspectorConnectionListener {
            override fun onConnected() {
                println("Connected")
            }

            override fun onClosed() {
                println("Closed")
            }

            override fun onError() {
                println("Error")
            }
        }, ip = "localhost", port = 9999)
        socket.connectBlocking()

        runBlocking {
            println(socket.protocol.keyValueServerInterface.get("123"))
            println(socket.protocol.keyValueServerInterface.set("123", ValueWithType(StorageType.string, "some"), ValueWithType(StorageType.string, "body")))
            println(socket.protocol.keyValueServerInterface.get("123"))
            println(socket.protocol.keyValueServerInterface.remove("123", ValueWithType(StorageType.string, "hello")))
            println(socket.protocol.keyValueServerInterface.get("123"))
            println(socket.protocol.keyValueServerInterface.clear("123"))
            println(socket.protocol.keyValueServerInterface.get("123"))
        }

        Thread.sleep(3000);
        socket.close()
    }

}