package com.chimerapps.storageinspector.api.protocol.vmservice

import com.chimerapps.storageinspector.util.classLogger
import com.google.gsonpackaged.Gson
import com.google.gsonpackaged.JsonObject
import com.google.gsonpackaged.JsonParser
import com.intellij.diagnostic.ActivityImpl.listener
import kotlinx.coroutines.CompletableDeferred
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.io.Closeable
import java.io.IOException
import java.net.URI
import java.util.UUID

class VMService(websocketUri: URI) : Closeable {

    private val vmServiceSocket = object : WebSocketClient(websocketUri) {
        override fun onOpen(handshakedata: ServerHandshake?) {
            connectWaiter.complete(Unit)
        }

        override fun onMessage(message: String) {
            handleMessage(message)
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
        }

        override fun onError(ex: Exception) {
            connectWaiter.completeExceptionally(ex)
        }
    }

    private val connectWaiter = CompletableDeferred<Unit>()
    private val waitingFutures = mutableMapOf<String, Pair<CompletableDeferred<*>, (JsonObject?, JsonObject?, CompletableDeferred<*>) -> Unit>>()

    suspend fun connect() {
        vmServiceSocket.connect()
        return connectWaiter.await()
    }

    override fun close() {
        vmServiceSocket.close()
    }

    suspend fun getVM(): VM {
        return call("getVM") { data, error ->
            if (error != null) throw IOException("VM error: ${error.get("message")}")
            Gson().fromJson(data, VM::class.java)
        }
    }

    suspend fun callExtensionMethod(method: String, isolateId: String?, params: JsonObject = JsonObject()): JsonObject {
        return call(method, params.also { p -> isolateId?.let { p.addProperty("isolateId", it) } }) { data, error ->
            if (error != null) throw IOException("Extension method error: ${error.get("message")}")
            data!!
        }
    }

    private fun handleMessage(message: String) {
        val json = JsonParser.parseString(message).asJsonObject
        if (!json.has("id")) return

        val requestId = json.get("id").asString
        val result = if (json.has("result")) json.getAsJsonObject("result") else null
        val error = if (json.has("error")) json.getAsJsonObject("error") else null
        val futurePair = waitingFutures.remove(requestId) ?: return classLogger.warn("Got unsolicited response with unknown request id: $requestId. Data: $result")

        if (result != null || error != null) {
            futurePair.second(result, error, futurePair.first)
        }
    }

    private suspend fun <T> call(
        method: String,
        arguments: JsonObject = JsonObject(),
        responseHandler: (result: JsonObject?, error: JsonObject?) -> T,
    ): T {
        val requestId = UUID.randomUUID().toString()

        val future = CompletableDeferred<T>()
        waitingFutures[requestId] = Pair(future) { data, error, _ ->
            try {
                future.complete(responseHandler(data, error))
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }

        val command = JsonObject().also {
            it.addProperty("jsonrpc", "2.0")
            it.addProperty("id", requestId)
            it.addProperty("method", method)
            it.add("params", arguments)
        }.toString()

        vmServiceSocket.send(command)

        return future.await()
    }

}

data class VM(
    val isolates: List<IsolateRef>,
)

data class IsolateRef(
    val id: String,
    val name: String,
)