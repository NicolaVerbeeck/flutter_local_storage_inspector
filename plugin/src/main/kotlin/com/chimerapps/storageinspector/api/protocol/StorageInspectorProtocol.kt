package com.chimerapps.storageinspector.api.protocol

import com.chimerapps.storageinspector.api.protocol.specific.key_value.KeyValueProtocol
import com.google.gsonpackaged.JsonObject
import com.google.gsonpackaged.JsonParser

/**
 * @author Nicola Verbeeck
 */
class StorageInspectorProtocol {

    companion object {
        const val SERVER_TYPE_ID = "id"
        const val SERVER_TYPE_KEY_VALUE = "key_value"
    }

    val keyValueProtocol = KeyValueProtocol()

    fun onMessage(message: String) {
        val root = JsonParser.parseString(message).asJsonObject
        val serverType = root.get("serverType").asString
        val requestId = if (root.has("requestId")) root.get("requestId").asString else null

        when (serverType) {
            SERVER_TYPE_ID -> handleId(root.get("data").asJsonObject)
            SERVER_TYPE_KEY_VALUE -> keyValueProtocol.handleMessage(requestId, root.get("data").asJsonObject)
        }

    }

    private fun handleId(data: JsonObject) {
        //TODO
        println(data)
    }


}