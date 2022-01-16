package com.chimerapps.storageinspector.ui.ide.view.sql

import com.chimerapps.storageinspector.api.protocol.model.sql.SQLTableDefinition
import com.chimerapps.storageinspector.inspector.StorageServer
import com.chimerapps.storageinspector.inspector.specific.sql.SQLInspectorInterface
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * @author Nicola Verbeeck
 */
class SQLServerView(private val project: Project) : JPanel(BorderLayout()) {

    private val scope = CoroutineScope(SupervisorJob())
    private var server: StorageServer? = null
    private var serverInterface: SQLInspectorInterface? = null

    private val tableView = CustomDataTableView(project, ::doRemoveSelectedRows)

    init {
        add(JBScrollPane(tableView), BorderLayout.CENTER)
    }

    fun setServer(serverInterface: SQLInspectorInterface, server: StorageServer, table: SQLTableDefinition?) {
        this.serverInterface = serverInterface
        this.server = server
        if (table != null) {
            tableView.updateModel(server.name, table)
        } else {
            //TODO set empty state
        }

        scope.launch {
            val newData = if (table == null) null else serverInterface.query("SELECT * FROM ${table.name}", server)

            tableView.ensureColumns(newData?.columns ?: emptyList())
            tableView.updateData(newData?.rows ?: emptyList())
        }
    }

    private fun doRemoveSelectedRows() {
        TODO("Not yet implemented")
    }

}