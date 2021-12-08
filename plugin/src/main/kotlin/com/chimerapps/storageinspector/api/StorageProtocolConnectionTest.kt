package com.chimerapps.storageinspector.api

import org.junit.Test

/**
 * @author Nicola Verbeeck
 */
class StorageProtocolConnectionTest {

    @Test
    fun testCreateConnection() {
        val socket = StorageProtocolConnection("localhost", 9999)
        socket.connectBlocking()

        Thread.sleep(3000);
        socket.close()
    }


}