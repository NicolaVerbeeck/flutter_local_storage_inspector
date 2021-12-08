package com.chimerapps.storageinspector.api.protocol.specific.key_value

import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueServerIdentification
import com.google.gsonpackaged.Gson
import com.google.gsonpackaged.JsonObject

/**
 * @author Nicola Verbeeck
 */
class KeyValueProtocol {

    private val gson = Gson()

    fun handleMessage(requestId: String?, data: JsonObject) {
        if (requestId == null) {
            handleUnannouncedKeyValue(data)
        }
    }

    private fun handleUnannouncedKeyValue(data: JsonObject) {
        when (data.get("type")?.asString) {
            "identify" -> handleServerIdentification(data.get("data").asJsonObject)
        }
    }

    private fun handleServerIdentification(asJsonObject: JsonObject) {
        val serverId = gson.fromJson(asJsonObject, KeyValueServerIdentification::class.java)
        println(serverId)
    }
}