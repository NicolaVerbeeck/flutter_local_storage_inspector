package com.chimerapps.storageinspector.api.protocol.specific.key_value

import com.chimerapps.storageinspector.api.RemoteError
import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocol
import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueGetResponse
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueGetResult
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueRequest
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueRequestData
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueRequestType
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueServerIdentification
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueServerStatus
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueServerValues
import com.chimerapps.storageinspector.ui.util.json.GsonCreator
import com.chimerapps.storageinspector.util.classLogger
import com.google.gsonpackaged.JsonObject
import kotlinx.coroutines.CompletableDeferred
import java.util.UUID

interface KeyValueServerInterface {
    fun addListener(listener: KeyValueProtocolListener)

    fun removeListener(listener: KeyValueProtocolListener)

    suspend fun get(serverId: String): KeyValueServerValues
    suspend fun set(serverId: String, key: ValueWithType, value: ValueWithType): Boolean
    suspend fun get(serverId: String, key: ValueWithType): ValueWithType
    suspend fun remove(serverId: String, key: ValueWithType): Boolean
    suspend fun clear(serverId: String): Boolean
}

/**
 * @author Nicola Verbeeck
 */
class KeyValueProtocol(private val protocol: StorageInspectorProtocol) : KeyValueServerInterface {

    private companion object {
        const val TYPE_IDENTIFY = "identify"
    }

    private val gson = GsonCreator.newGsonInstance()
    private val listeners = mutableListOf<KeyValueProtocolListener>()
    private var serverIds = mutableListOf<KeyValueServerIdentification>()

    private val waitingFutures = mutableMapOf<String, Pair<CompletableDeferred<*>, (JsonObject, CompletableDeferred<*>) -> Unit>>()

    override fun addListener(listener: KeyValueProtocolListener) {
        synchronized(listeners) {
            listeners.add(listener)
            serverIds.forEach(listener::onServerIdentification)
        }
    }

    override fun removeListener(listener: KeyValueProtocolListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    fun handleMessage(requestId: String?, data: JsonObject?, error: String?) {
        try {
            if (requestId == null && data != null) {
                handleUnannouncedKeyValue(data)
            } else if (requestId != null && data != null) {
                handleResponse(requestId, data)
            } else if (requestId != null && error != null) {
                handleError(requestId, error)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun handleResponse(requestId: String, data: JsonObject) {
        val futurePair = waitingFutures.remove(requestId) ?: return classLogger.warn("Got unsolicited response with unknown request id: $requestId. Data: $data")
        futurePair.second(data, futurePair.first)
    }

    private fun handleError(requestId: String, error: String) {
        val futurePair = waitingFutures.remove(requestId) ?: return classLogger.warn("Got unsolicited response with unknown request id: $requestId. Error: $error")
        futurePair.first.completeExceptionally(RemoteError(error))
    }

    override suspend fun get(serverId: String): KeyValueServerValues {
        val requestId = UUID.randomUUID().toString()

        val future = CompletableDeferred<KeyValueServerValues>()
        waitingFutures[requestId] = Pair(future) { data, _ ->
            future.complete(gson.fromJson(data, KeyValueServerValues::class.java))
        }

        protocol.sendRequest(
            serverType = StorageInspectorProtocol.SERVER_TYPE_KEY_VALUE,
            requestId = requestId,
            data = gson.toJsonTree(
                KeyValueRequest(KeyValueRequestType.GET, KeyValueRequestData(serverId))
            ).asJsonObject
        )

        return future.await()
    }

    override suspend fun get(serverId: String, key: ValueWithType): ValueWithType {
        val requestId = UUID.randomUUID().toString()
        val future = CompletableDeferred<ValueWithType>()
        waitingFutures[requestId] = Pair(future) { data, _ ->
            future.complete(gson.fromJson(data, KeyValueGetResponse::class.java).data.value)
        }

        protocol.sendRequest(
            serverType = StorageInspectorProtocol.SERVER_TYPE_KEY_VALUE,
            requestId = requestId,
            data = gson.toJsonTree(
                KeyValueRequest(KeyValueRequestType.GET_VALUE, KeyValueRequestData(serverId, key = key))
            ).asJsonObject
        )

        return future.await()
    }

    override suspend fun set(serverId: String, key: ValueWithType, value: ValueWithType): Boolean {
        val requestId = UUID.randomUUID().toString()

        val future = CompletableDeferred<Boolean>()
        waitingFutures[requestId] = Pair(future) { data, _ ->
            future.complete(gson.fromJson(data, KeyValueServerStatus::class.java).data.success)
        }

        protocol.sendRequest(
            serverType = StorageInspectorProtocol.SERVER_TYPE_KEY_VALUE,
            requestId = requestId,
            data = gson.toJsonTree(
                KeyValueRequest(KeyValueRequestType.SET, KeyValueRequestData(serverId, key = key, value = value))
            ).asJsonObject
        )

        return future.await()
    }

    override suspend fun remove(serverId: String, key: ValueWithType): Boolean {
        val requestId = UUID.randomUUID().toString()

        val future = CompletableDeferred<Boolean>()
        waitingFutures[requestId] = Pair(future) { data, _ ->
            future.complete(gson.fromJson(data, KeyValueServerStatus::class.java).data.success)
        }

        protocol.sendRequest(
            serverType = StorageInspectorProtocol.SERVER_TYPE_KEY_VALUE,
            requestId = requestId,
            data = gson.toJsonTree(
                KeyValueRequest(KeyValueRequestType.REMOVE, KeyValueRequestData(serverId, key = key))
            ).asJsonObject
        )

        return future.await()
    }

    override suspend fun clear(serverId: String): Boolean {
        val requestId = UUID.randomUUID().toString()

        val future = CompletableDeferred<Boolean>()
        waitingFutures[requestId] = Pair(future) { data, _ ->
            future.complete(gson.fromJson(data, KeyValueServerStatus::class.java).data.success)
        }

        protocol.sendRequest(
            serverType = StorageInspectorProtocol.SERVER_TYPE_KEY_VALUE,
            requestId = requestId,
            data = gson.toJsonTree(
                KeyValueRequest(KeyValueRequestType.CLEAR, KeyValueRequestData(serverId))
            ).asJsonObject
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
            serverIds += serverId
            listeners.forEach { it.onServerIdentification(serverId) }
        }
    }
}

interface KeyValueProtocolListener {
    fun onServerIdentification(identification: KeyValueServerIdentification)
}