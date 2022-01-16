package com.chimerapps.storageinspector.ui.ide.view.sql

import com.chimerapps.storageinspector.api.protocol.model.sql.SQLTableDefinition
import com.chimerapps.storageinspector.inspector.StorageServer
import com.chimerapps.storageinspector.inspector.specific.sql.SQLInspectorInterface
import com.chimerapps.storageinspector.ui.ide.actions.RefreshAction
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.SearchTextField
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.util.ui.UIUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Nicola Verbeeck
 */
class SQLServerView(private val project: Project) : JBPanelWithEmptyText(BorderLayout()) {

    private val scope = CoroutineScope(SupervisorJob())
    private var server: StorageServer? = null
    private var serverInterface: SQLInspectorInterface? = null
    private var table: SQLTableDefinition? = null
    private var currentQueryView = JBLabel().also {
        it.foreground = UIUtil.getHeaderInactiveColor()
    }

    private var query: String = ""
        set(newValue) {
            field = newValue
            currentQueryView.text = Tr.SqlCurrentQuery.tr(newValue)
            currentQueryView.toolTipText = newValue
        }

    private val queryField = object : SearchTextField(true, "sql_inspector_query") {
        override fun onFocusLost() {
            //Don't save
        }
    }

    private val tableView = CustomDataTableView(project, ::doRemoveSelectedRows)

    init {
        val actionGroup = DefaultActionGroup()

        val refreshAction = RefreshAction(Tr.GenericRefresh.tr(), Tr.GenericRefresh.tr()) {
            refresh()
        }
        actionGroup.addAction(refreshAction)
        val toolbar = ActionManager.getInstance().createActionToolbar("SQL Storage Inspector", actionGroup, false)
        val decorator = ToolbarDecorator.createDecorator(tableView)
        decorator.disableUpDownActions()
        decorator.setRemoveAction { doRemoveSelectedRows() }

        val contentPanel = JPanel(BorderLayout())
        contentPanel.add(decorator.createPanel(), BorderLayout.CENTER)

        val queryPanel = JPanel(BorderLayout())
        queryPanel.add(queryField, BorderLayout.NORTH)
        queryPanel.add(currentQueryView.spaced(top=3, bottom=5), BorderLayout.SOUTH)
        contentPanel.add(queryPanel, BorderLayout.NORTH)

        add(contentPanel, BorderLayout.CENTER)
        add(toolbar.component, BorderLayout.WEST)

        withEmptyText(Tr.SqlSelectDatabase.tr())

        queryField.textEditor.addActionListener {
            checkAndExecuteRawQuery(queryField.text.trim())
        }
    }

    private fun checkAndExecuteRawQuery(query: String) {
        val table = table ?: return

        if (query.isEmpty()) {
            this.query = "SELECT * FROM ${table.name}"
        } else {
            this.query = query
        }

        refresh()
    }

    fun setServer(serverInterface: SQLInspectorInterface, server: StorageServer, table: SQLTableDefinition?) {
        this.serverInterface = serverInterface
        this.server = server
        this.table = table
        if (table != null) {
            children.forEach { it.isVisible = true }
            tableView.updateModel(server.name, table)
        } else {
            children.forEach { it.isVisible = false }
        }
        repaint()

        if (table != null) {
            query = "SELECT * FROM ${table.name}"
            refresh()
        }
    }

    private fun doRemoveSelectedRows() {
        TODO("Not yet implemented")
    }

    private fun refresh() {
        val serverInterface = serverInterface ?: return
        val server = server ?: return
        scope.launch {
            val newData = serverInterface.query(query, server)

            tableView.ensureColumns(newData.columns)
            tableView.updateData(newData.rows)
        }
    }

}

private fun JComponent.spaced(
    left: Int = 0,
    top: Int = 0,
    right: Int = 0,
    bottom: Int = 0,
): JComponent {
    border = BorderFactory.createEmptyBorder(top,left, bottom,right)
    return this
}

val JPanel.children: Iterable<Component>
    get() = UIUtil.uiChildren(this)
