package com.chimerapps.storageinspector.api

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
            println(socket.protocol.keyValueProtocol.get("123"))
        }

        Thread.sleep(3000);
        socket.close()
    }

}