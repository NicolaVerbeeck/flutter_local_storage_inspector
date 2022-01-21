package com.chimerapps.storageinspector.inspector.specific.sql

import com.chimerapps.storageinspector.api.protocol.model.sql.SQLDataType
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
    val schemaVersion: Int?,
    val tables: List<SQLTableDefinition>,
    val schema: String?,
    val createdSchema: String,
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
            schemaVersion = identification.schemaVersion,
            tables = identification.tables,
            schema = identification.schema,
            createdSchema = createSchema(identification.tables)
        )
        onNewServer(server)
    }

    private fun createSchema(tables: List<SQLTableDefinition>): String {
        return buildString {
            tables.forEach { table ->
                append("CREATE TABLE ${table.name} (")
                table.columns.forEachIndexed { index, column ->
                    if (index != 0)
                        append(", ")
                    append(column.name)
                    append(" ")
                    when (column.type) {
                        SQLDataType.TEXT -> append("TEXT")
                        SQLDataType.BLOB -> append("BLOB")
                        SQLDataType.REAL -> append("REAL")
                        SQLDataType.BOOLEAN,
                        SQLDataType.DATETIME,
                        SQLDataType.INTEGER, -> append("INTEGER")
                    }
                    if (!column.nullable) append(" NOT NULL")
                }
                append(");\n")
            }
        }
    }

}

private fun <E> List<E>.withReplacementAt(index: Int, value: E): List<E> {
    val mutable = ArrayList(this)
    mutable[index] = value
    return mutable
}
