package com.chimerapps.storageinspector.api.protocol.specific.key_value

import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocol
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueServerIdentification
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueServerValues
import com.google.gsonpackaged.Gson
import com.google.gsonpackaged.JsonObject
import kotlinx.coroutines.CompletableDeferred
import java.util.UUID

/**
 * @author Nicola Verbeeck
 */
class KeyValueProtocol(private val protocol: StorageInspectorProtocol) {

    private companion object {
        const val TYPE_IDENTIFY = "identify"
    }

    private val gson = Gson()
    private val listeners = mutableListOf<KeyValueProtocolListener>()
    var serverId: KeyValueServerIdentification? = null

    private val waitingFutures = mutableMapOf<String, Pair<CompletableDeferred<*>, (JsonObject, CompletableDeferred<*>) -> Unit>>()

    fun addListener(listener: KeyValueProtocolListener) {
        synchronized(listeners) {
            listeners.add(listener)
            serverId?.let { listener.onServerIdentification(it) }
        }
    }

    fun removeListener(listener: KeyValueProtocolListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    fun handleMessage(requestId: String?, data: JsonObject) {
        try {
            if (requestId == null) {
                handleUnannouncedKeyValue(data)
            } else {
                handleResponse(requestId, data)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun handleResponse(requestId: String, data: JsonObject) {
        val futurePair = waitingFutures.remove(requestId) ?: return
        futurePair.second(data, futurePair.first)
    }

    suspend fun get(serverId: String): KeyValueServerValues {
        val requestId = UUID.randomUUID().toString()

        val future = CompletableDeferred<KeyValueServerValues>()
        waitingFutures[requestId] = Pair(future) { data, _ ->
            future.complete(gson.fromJson(data, KeyValueServerValues::class.java))
        }

        protocol.sendRequest(
            serverType = StorageInspectorProtocol.SERVER_TYPE_KEY_VALUE,
            requestId = requestId,
            data = gson.toJsonTree(mapOf("type" to "get", "data" to mapOf("id" to serverId))).asJsonObject
        )

        return future.await()
    }

    private fun handleUnannouncedKeyValue(data: JsonObject) {
        when (data.get("type")?.asString) {
            TYPE_IDENTIFY -> handleServerIdentification(data.get("data").asJsonObject)
        }
    }

    private fun handleServerIdentification(asJsonObject: JsonObject) {
        val serverId = gson.fromJson(asJsonObject, KeyValueServerIdentification::class.java)
        synchronized(listeners) {
            this.serverId = serverId
            listeners.forEach { it.onServerIdentification(serverId) }
        }
    }
}

interface KeyValueProtocolListener {
    fun onServerIdentification(identification: KeyValueServerIdentification)
}