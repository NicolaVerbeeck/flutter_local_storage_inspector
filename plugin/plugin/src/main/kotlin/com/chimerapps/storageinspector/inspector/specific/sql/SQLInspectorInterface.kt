package com.chimerapps.storageinspector.inspector.specific.sql

import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLDataType
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLDateTimeFormat
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLQueryResult
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLServerIdentification
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLTableDefinition
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLUpdateResult
import com.chimerapps.storageinspector.api.protocol.specific.sql.SQLDatabaseProtocolListener
import com.chimerapps.storageinspector.api.protocol.specific.sql.SQLDatabaseServerInterface
import com.chimerapps.storageinspector.inspector.StorageServer
import com.chimerapps.storageinspector.inspector.StorageServerType
import com.chimerapps.storageinspector.inspector.specific.BaseInspectorInterface
import com.chimerapps.storageinspector.inspector.specific.BaseInspectorInterfaceImpl

interface SQLInspectorInterface : BaseInspectorInterface<SQLStorageServer> {

    suspend fun query(query: String, server: StorageServer, forceReload: Boolean = false): SQLQueryResult

    suspend fun update(
        query: String,
        server: StorageServer,
        variables: List<ValueWithType>,
        affectedTables: List<String>,
    ): SQLUpdateResult

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
    val dateTimeFormat: SQLDateTimeFormat,
) : StorageServer

class SQLInspectorInterfaceImpl(
    private val sqlProtocol: SQLDatabaseServerInterface,
) : BaseInspectorInterfaceImpl<SQLStorageServer>(), SQLInspectorInterface, SQLDatabaseProtocolListener {

    init {
        sqlProtocol.addListener(this)
    }

    override suspend fun query(query: String, server: StorageServer, forceReload: Boolean): SQLQueryResult {
        return sqlProtocol.query(server.id, query)
    }

    override suspend fun update(
        query: String,
        server: StorageServer,
        variables: List<ValueWithType>,
        affectedTables: List<String>,
    ): SQLUpdateResult {
        return sqlProtocol.update(server.id, query, variables, affectedTables)
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
            createdSchema = createSchema(identification.tables),
            dateTimeFormat = identification.dateTimeFormat,
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
                        SQLDataType.INTEGER,
                        -> append("INTEGER")
                    }
                    if (table.primaryKey.size == 1 && table.primaryKey[0] == column.name) {
                        append(" PRIMARY KEY")
                    } else if (!column.nullable) append(" NOT NULL")

                    if (column.defaultValueExpression != null) append(" DEFAULT ${column.defaultValueExpression}")
                }
                append(");\n")
            }
        }
    }
}