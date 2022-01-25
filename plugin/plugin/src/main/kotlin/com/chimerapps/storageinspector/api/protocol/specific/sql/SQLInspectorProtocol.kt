package com.chimerapps.storageinspector.api.protocol.specific.sql

import com.chimerapps.storageinspector.api.RemoteError
import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocol
import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLQueryResult
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLRequest
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLRequestData
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLRequestType
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLServerIdentification
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLUpdateResult
import com.chimerapps.storageinspector.ui.util.json.GsonCreator
import com.chimerapps.storageinspector.util.classLogger
import com.google.gsonpackaged.JsonObject
import kotlinx.coroutines.CompletableDeferred
import java.util.UUID

/**
 * @author Nicola Verbeeck
 */
interface SQLDatabaseServerInterface {
    fun addListener(listener: SQLDatabaseProtocolListener)

    fun removeListener(listener: SQLDatabaseProtocolListener)

    suspend fun query(serverId: String, queryString: String): SQLQueryResult

    suspend fun update(
        serverId: String,
        query: String,
        variables: List<ValueWithType>,
        affectedTables: List<String>,
    ): SQLUpdateResult
}

class SQLDatabaseProtocol(private val protocol: StorageInspectorProtocol) : SQLDatabaseServerInterface {
    private companion object {
        const val TYPE_IDENTIFY = "identify"
    }

    private val gson = GsonCreator.newGsonInstance()
    private val listeners = mutableListOf<SQLDatabaseProtocolListener>()
    private var serverIds = mutableListOf<SQLServerIdentification>()

    private val waitingFutures = mutableMapOf<String, Pair<CompletableDeferred<*>, (JsonObject, CompletableDeferred<*>) -> Unit>>()

    override fun addListener(listener: SQLDatabaseProtocolListener) {
        synchronized(listeners) {
            listeners.add(listener)
            serverIds.forEach(listener::onServerIdentification)
        }
    }

    override fun removeListener(listener: SQLDatabaseProtocolListener) {
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

    override suspend fun query(serverId: String, queryString: String): SQLQueryResult {
        val requestId = UUID.randomUUID().toString()

        val future = CompletableDeferred<SQLQueryResult>()
        waitingFutures[requestId] = Pair(future) { data, _ ->
            future.complete(gson.fromJson(data, SQLQueryResult::class.java))
        }

        protocol.sendRequest(
            serverType = StorageInspectorProtocol.SERVER_TYPE_SQL,
            requestId = requestId,
            data = gson.toJsonTree(
                SQLRequest(SQLRequestType.QUERY, SQLRequestData(serverId, query = queryString))
            ).asJsonObject
        )

        return future.await()
    }

    override suspend fun update(
        serverId: String,
        query: String,
        variables: List<ValueWithType>,
        affectedTables: List<String>
    ): SQLUpdateResult {
        val requestId = UUID.randomUUID().toString()

        val future = CompletableDeferred<SQLUpdateResult>()
        waitingFutures[requestId] = Pair(future) { data, _ ->
            future.complete(gson.fromJson(data, SQLUpdateResult::class.java))
        }

        protocol.sendRequest(
            serverType = StorageInspectorProtocol.SERVER_TYPE_SQL,
            requestId = requestId,
            data = gson.toJsonTree(
                SQLRequest(SQLRequestType.UPDATE, SQLRequestData(
                    serverId,
                    query = query,
                    variables = variables,
                    affectedTables = affectedTables,
                ))
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
        val serverId = gson.fromJson(asJsonObject, SQLServerIdentification::class.java)
        synchronized(listeners) {
            serverIds += serverId
            listeners.forEach { it.onServerIdentification(serverId) }
        }
    }
}

interface SQLDatabaseProtocolListener {
    fun onServerIdentification(identification: SQLServerIdentification)
}