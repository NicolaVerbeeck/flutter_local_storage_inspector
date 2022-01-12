package com.chimerapps.storageinspector.api.protocol.specific.sql

import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocol
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLServerIdentification
import com.chimerapps.storageinspector.ui.util.json.GsonCreator
import com.google.gsonpackaged.JsonObject
import kotlinx.coroutines.CompletableDeferred

/**
 * @author Nicola Verbeeck
 */
interface SQLDatabaseServerInterface {
    fun addListener(listener: SQLDatabaseProtocolListener)

    fun removeListener(listener: SQLDatabaseProtocolListener)

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
                //TODO handleResponse(requestId, data)
            } else if (requestId != null && error != null) {
                //TODO handleError(requestId, error)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
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