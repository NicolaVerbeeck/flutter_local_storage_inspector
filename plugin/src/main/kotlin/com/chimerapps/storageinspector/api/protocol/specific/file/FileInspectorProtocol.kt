package com.chimerapps.storageinspector.api.protocol.specific.file

import com.chimerapps.storageinspector.api.RemoteError
import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocol
import com.chimerapps.storageinspector.api.protocol.model.file.FileRequest
import com.chimerapps.storageinspector.api.protocol.model.file.FileRequestData
import com.chimerapps.storageinspector.api.protocol.model.file.FileRequestType
import com.chimerapps.storageinspector.api.protocol.model.file.FileServerByteData
import com.chimerapps.storageinspector.api.protocol.model.file.FileServerIdentification
import com.chimerapps.storageinspector.api.protocol.model.file.FileServerStatus
import com.chimerapps.storageinspector.api.protocol.model.file.FileServerValues
import com.chimerapps.storageinspector.ui.util.json.GsonCreator
import com.chimerapps.storageinspector.util.classLogger
import com.google.gsonpackaged.JsonObject
import com.intellij.util.Base64
import kotlinx.coroutines.CompletableDeferred
import java.util.UUID

/**
 * @author Nicola Verbeeck
 */
interface FileStorageInterface {
    fun addListener(listener: FileProtocolListener)

    fun removeListener(listener: FileProtocolListener)

    suspend fun listFiles(serverId: String): FileServerValues

    suspend fun getFile(serverId: String, path: String): ByteArray

    suspend fun writeFile(serverId: String, path: String, bytes: ByteArray): Boolean
}

class FileInspectorProtocol(private val protocol: StorageInspectorProtocol) : FileStorageInterface {

    private companion object {
        const val TYPE_IDENTIFY = "identify"
    }

    private val gson = GsonCreator.newGsonInstance()
    private val listeners = mutableListOf<FileProtocolListener>()
    private var serverId: FileServerIdentification? = null

    private val waitingFutures = mutableMapOf<String, Pair<CompletableDeferred<*>, (JsonObject, CompletableDeferred<*>) -> Unit>>()

    override fun addListener(listener: FileProtocolListener) {
        synchronized(listeners) {
            listeners += listener
            serverId?.let { listener.onServerIdentification(it) }
        }
    }

    override fun removeListener(listener: FileProtocolListener) {
        synchronized(listeners) {
            listeners -= listener
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

    override suspend fun listFiles(serverId: String): FileServerValues {
        val requestId = UUID.randomUUID().toString()

        val future = CompletableDeferred<FileServerValues>()
        waitingFutures[requestId] = Pair(future) { data, _ ->
            future.complete(gson.fromJson(data, FileServerValues::class.java))
        }

        protocol.sendRequest(
            serverType = StorageInspectorProtocol.SERVER_TYPE_FILE,
            requestId = requestId,
            data = gson.toJsonTree(
                FileRequest(FileRequestType.LIST, FileRequestData(serverId, root = "/"))
            ).asJsonObject
        )

        return future.await()
    }

    override suspend fun getFile(serverId: String, path: String): ByteArray {
        val requestId = UUID.randomUUID().toString()

        val future = CompletableDeferred<ByteArray>()
        waitingFutures[requestId] = Pair(future) { data, _ ->
            val encoded = gson.fromJson(data, FileServerByteData::class.java).data
            future.complete(Base64.decode(encoded))
        }

        protocol.sendRequest(
            serverType = StorageInspectorProtocol.SERVER_TYPE_FILE,
            requestId = requestId,
            data = gson.toJsonTree(
                FileRequest(FileRequestType.READ, FileRequestData(serverId, path = path))
            ).asJsonObject
        )

        return future.await()
    }

    override suspend fun writeFile(serverId: String, path: String, bytes: ByteArray): Boolean {
        val requestId = UUID.randomUUID().toString()

        val future = CompletableDeferred<Boolean>()
        waitingFutures[requestId] = Pair(future) { data, _ ->
            future.complete(gson.fromJson(data, FileServerStatus::class.java).data.success)
        }

        protocol.sendRequest(
            serverType = StorageInspectorProtocol.SERVER_TYPE_FILE,
            requestId = requestId,
            data = gson.toJsonTree(
                FileRequest(FileRequestType.WRITE, FileRequestData(serverId, path = path, data = Base64.encode(bytes)))
            ).asJsonObject
        )

        return future.await()
    }

    private fun handleResponse(requestId: String, data: JsonObject) {
        val futurePair = waitingFutures.remove(requestId) ?: return classLogger.warn("Got unsolicited response with unknown request id: $requestId. Data: $data")
        futurePair.second(data, futurePair.first)
    }

    private fun handleError(requestId: String, error: String) {
        val futurePair = waitingFutures.remove(requestId) ?: return classLogger.warn("Got unsolicited response with unknown request id: $requestId. Error: $error")
        futurePair.first.completeExceptionally(RemoteError(error))
    }

    private fun handleUnannouncedKeyValue(data: JsonObject) {
        when (data.get("type")?.asString) {
            TYPE_IDENTIFY -> handleServerIdentification(data.get("data").asJsonObject)
        }
    }

    private fun handleServerIdentification(asJsonObject: JsonObject) {
        val serverId = gson.fromJson(asJsonObject, FileServerIdentification::class.java)
        synchronized(listeners) {
            this.serverId = serverId
            listeners.forEach { it.onServerIdentification(serverId) }
        }
    }
}

interface FileProtocolListener {
    fun onServerIdentification(identification: FileServerIdentification)
}