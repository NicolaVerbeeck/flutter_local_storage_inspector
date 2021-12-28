package com.chimerapps.storageinspector.api.protocol

import com.chimerapps.storageinspector.api.RemoteError
import com.chimerapps.storageinspector.api.StorageInspectorProtocolConnection
import com.chimerapps.storageinspector.api.protocol.model.ServerId
import com.chimerapps.storageinspector.api.protocol.specific.file.FileInspectorProtocol
import com.chimerapps.storageinspector.api.protocol.specific.key_value.KeyValueProtocol
import com.chimerapps.storageinspector.api.protocol.specific.key_value.KeyValueServerInterface
import com.chimerapps.storageinspector.ui.util.json.GsonCreator
import com.chimerapps.storageinspector.util.classLogger
import com.google.gsonpackaged.JsonObject
import com.google.gsonpackaged.JsonParser
import kotlinx.coroutines.CompletableDeferred
import java.util.UUID

/**
 * @author Nicola Verbeeck
 */
@Suppress("unused")
class StorageInspectorProtocol(private val onConnection: StorageInspectorProtocolConnection) {

    companion object {
        const val SERVER_TYPE_ID = "id"
        const val SERVER_TYPE_KEY_VALUE = "key_value"
        const val SERVER_TYPE_FILE = "file"
        private const val SERVER_TYPE_INSPECTOR = "inspector"
        private const val COMMAND_UNPAUSE = "resume"
    }

    private val gson = GsonCreator.newGsonInstance()
    private val listeners = mutableListOf<StorageInspectorProtocolListener>()
    private var serverId: ServerId? = null
    private val waitingFutures = mutableMapOf<String, Pair<CompletableDeferred<*>, (JsonObject, CompletableDeferred<*>) -> Unit>>()

    private val keyValueProtocol = KeyValueProtocol(this)
    private val fileProtocol = FileInspectorProtocol(this)

    val keyValueServerInterface: KeyValueServerInterface
        get() = keyValueProtocol

    fun addListener(listener: StorageInspectorProtocolListener) {
        synchronized(listeners) {
            listeners.add(listener)
            serverId?.let { listener.onServerIdentification(it) }
        }
    }

    fun removeListener(listener: StorageInspectorProtocolListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    suspend fun sendUnpause() {
        val requestId = UUID.randomUUID().toString()

        val future = CompletableDeferred<Unit>()
        waitingFutures[requestId] = Pair(future) { _, _ ->
            future.complete(Unit)
        }

        sendRequest(
            serverType = SERVER_TYPE_INSPECTOR,
            requestId = requestId,
            data = JsonObject().also { it.addProperty("type", COMMAND_UNPAUSE) }
        )

        return future.await()
    }

    fun onMessage(message: String) {
        val root = JsonParser.parseString(message).asJsonObject
        val serverType = root.get("serverType").asString
        val requestId = if (root.has("requestId")) root.get("requestId").asString else null

        try {
            when (serverType) {
                SERVER_TYPE_ID -> handleId(root.get("data").asJsonObject)
                SERVER_TYPE_KEY_VALUE -> keyValueProtocol.handleMessage(requestId, root.get("data")?.asJsonObject, root.get("error")?.asString)
                SERVER_TYPE_FILE -> fileProtocol.handleMessage(requestId, root.get("data")?.asJsonObject, root.get("error")?.asString)
                SERVER_TYPE_INSPECTOR -> handleInspectorMessage(requestId, root.get("data")?.asJsonObject, root.get("error")?.asString)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun handleInspectorMessage(requestId: String?, data: JsonObject?, error: String?) {
        val futurePair = waitingFutures.remove(requestId) ?: return classLogger.warn("Got unsolicited response with unknown request id: $requestId. Error: $error")
        when {
            error != null -> futurePair.first.completeExceptionally(RemoteError(error))
            data != null -> futurePair.second(data, futurePair.first)
            else -> classLogger.warn("Received response message without data: $requestId")
        }
    }

    private fun handleId(data: JsonObject) {
        val serverId = gson.fromJson(data, ServerId::class.java)
        synchronized(listeners) {
            this.serverId = serverId
            listeners.forEach { it.onServerIdentification(serverId) }
        }
    }

    fun sendRequest(serverType: String, requestId: String, data: JsonObject) {
        onConnection.send(
            gson.toJson(
                mapOf(
                    "messageId" to UUID.randomUUID().toString(),
                    "requestId" to requestId,
                    "serverType" to serverType,
                    "data" to data,
                ),
            ),
        )
    }

}

interface StorageInspectorProtocolListener {
    fun onServerIdentification(serverId: ServerId)
}