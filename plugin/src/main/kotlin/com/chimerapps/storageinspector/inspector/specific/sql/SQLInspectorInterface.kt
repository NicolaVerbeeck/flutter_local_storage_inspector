package com.chimerapps.storageinspector.inspector.specific.sql

import com.chimerapps.storageinspector.api.protocol.model.sql.SQLQueryResult
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLServerIdentification
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLTableDefinition
import com.chimerapps.storageinspector.api.protocol.specific.sql.SQLDatabaseProtocolListener
import com.chimerapps.storageinspector.api.protocol.specific.sql.SQLDatabaseServerInterface
import com.chimerapps.storageinspector.inspector.StorageServer
import com.chimerapps.storageinspector.inspector.StorageServerType
import com.chimerapps.storageinspector.inspector.specific.BaseInspectorInterface
import com.chimerapps.storageinspector.inspector.specific.BaseInspectorInterfaceImpl
import org.apache.commons.collections4.map.LRUMap

interface SQLInspectorInterface : BaseInspectorInterface<SQLStorageServer> {

    suspend fun query(query: String, server: StorageServer, forceReload: Boolean = false): SQLQueryResult

}

data class SQLStorageServer(
    override val name: String,
    override val icon: String?,
    override val id: String,
    override val type: StorageServerType,
    val version: Int?,
    val tables: List<SQLTableDefinition>,
) : StorageServer

class SQLInspectorInterfaceImpl(
    private val sqlProtocol: SQLDatabaseServerInterface,
) : BaseInspectorInterfaceImpl<SQLStorageServer>(), SQLInspectorInterface, SQLDatabaseProtocolListener {

    private var cachedData = mutableMapOf<String, LRUMap<String, SQLQueryResult>>()

    init {
        sqlProtocol.addListener(this)
    }

    override suspend fun query(query: String, server: StorageServer, forceReload: Boolean): SQLQueryResult {
        if (!forceReload) {
            synchronized(cachedData) {
                cachedData[server.id]?.get(query)?.let { return it }
            }
        }

        val data = sqlProtocol.query(server.id, query)
        synchronized(cachedData) {
            cachedData.getOrPut(server.id) { LRUMap(5) }[query] = data
        }

        return data
    }

    override fun onServerIdentification(identification: SQLServerIdentification) {
        val server = SQLStorageServer(
            id = identification.id,
            icon = identification.icon,
            name = identification.name,
            type = StorageServerType.SQL,
            version = identification.version,
            tables = identification.tables
        )
        onNewServer(server)
    }

}

private fun <E> List<E>.withReplacementAt(index: Int, value: E): List<E> {
    val mutable = ArrayList(this)
    mutable[index] = value
    return mutable
}
