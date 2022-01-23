package com.chimerapps.storageinspector.ui.ide.view.sql

import com.chimerapps.storageinspector.api.protocol.model.sql.SQLTableDefinition
import com.chimerapps.storageinspector.inspector.specific.sql.SQLInspectorInterface
import com.chimerapps.storageinspector.inspector.specific.sql.SQLStorageServer
import com.chimerapps.storageinspector.ui.ide.actions.RefreshAction
import com.chimerapps.storageinspector.ui.ide.actions.SimpleAction
import com.chimerapps.storageinspector.ui.util.ensureMain
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.chimerapps.storageinspector.ui.util.preferences.AppPreferences
import com.chimerapps.storageinspector.ui.util.sql.SQLTextEditor
import com.google.gsonpackaged.Gson
import com.google.gsonpackaged.reflect.TypeToken
import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.JBSplitter
import com.intellij.ui.ToolbarDecorator
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

    companion object {
        private const val APP_PREFERENCE_SQL_STATE = "${AppPreferences.PREFIX}sqlSplitter"
    }

    private val scope = CoroutineScope(SupervisorJob())
    private var server: SQLStorageServer? = null
    private var serverInterface: SQLInspectorInterface? = null
    private var table: SQLTableDefinition? = null

    private val queryField = SQLTextEditor(project)

    private val tableView = CustomDataTableView(project, ::doRemoveSelectedRows)
    private val refreshAction: RefreshAction
    private lateinit var toolbar: ActionToolbar

    val databaseScheme : String?
        get() = server?.createdSchema

    init {
        val actionGroup = DefaultActionGroup()

        refreshAction = RefreshAction(Tr.GenericRefresh.tr(), Tr.GenericRefresh.tr(), icon = AllIcons.Actions.Execute) {
            QueryHistoryPopup.save(project, queryField.text)
            refresh()
        }
        val historyAction = SimpleAction(Tr.SqlHistoryText.tr(), Tr.SqlHistoryDescription.tr(), AllIcons.Vcs.History) {
            JBPopupFactory.getInstance().createListPopup(QueryHistoryPopup(project) {
                queryField.text = it
                refresh()
            }, 8).showInCenterOf(toolbar.component)
        }

        actionGroup.addAction(refreshAction)
        actionGroup.addAction(historyAction)
        toolbar = ActionManager.getInstance().createActionToolbar("SQL Storage Inspector", actionGroup, false)
        val decorator = ToolbarDecorator.createDecorator(tableView)
        decorator.disableUpDownActions()
        decorator.setRemoveAction { doRemoveSelectedRows() }

        val contentPanel = JPanel(BorderLayout())

        contentPanel.add(JBSplitter(true, APP_PREFERENCE_SQL_STATE, 0.2f).also {
            it.firstComponent = queryField
            it.secondComponent = decorator.createPanel()
        })

        add(contentPanel, BorderLayout.CENTER)
        add(toolbar.component, BorderLayout.WEST)

        withEmptyText(Tr.SqlSelectDatabase.tr())
    }

    fun setServer(serverInterface: SQLInspectorInterface, server: SQLStorageServer, table: SQLTableDefinition?) {
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
            queryField.text = "SELECT * FROM ${table.name}"
            refresh()
        }
    }

    private fun doRemoveSelectedRows() {
        TODO("Not yet implemented")
    }

    private fun refresh() {
        val serverInterface = serverInterface ?: return
        val server = server ?: return

        refreshAction.refreshing = true
        toolbar.updateActionsImmediately()

        scope.launch {
            try {
                val query = queryField.text
                val newData = serverInterface.query(query, server)

                tableView.ensureColumns(newData.columns)
                tableView.updateData(newData.rows)
            } finally {
                ensureMain {
                    refreshAction.refreshing = false
                    toolbar.updateActionsImmediately()
                }
            }
        }
    }

}

private class QueryHistoryPopup(
    project: Project,
    private val onHistoryChosen: (String) -> Unit,
) : BaseListPopupStep<String>() {

    companion object {
        private const val KEY = "sql_history"

        private fun getList(project: Project): List<String> {
            return Gson().fromJson(PropertiesComponent.getInstance(project).getValue(KEY, "[]"), object : TypeToken<List<String>>() {}.type)
        }

        fun save(project: Project, query: String) {
            val list = getList(project).toMutableList()
            val oldIndex = list.indexOf(query)
            when {
                oldIndex == 0 -> return
                oldIndex > 0 -> {
                    list.removeAt(oldIndex)
                }
            }
            list.add(0, query)
            PropertiesComponent.getInstance(project).setValue(KEY, Gson().toJson(list))
        }
    }

    init {
        init(Tr.SqlHistoryPopupTitle.tr(), getList(project), emptyList())
    }

    override fun getTextFor(value: String?): String = value ?: ""

    override fun onChosen(selectedValue: String?, finalChoice: Boolean): PopupStep<*>? {
        selectedValue?.let(onHistoryChosen)
        return FINAL_CHOICE
    }
}

private fun JComponent.spaced(
    left: Int = 0,
    top: Int = 0,
    right: Int = 0,
    bottom: Int = 0,
): JComponent {
    border = BorderFactory.createEmptyBorder(top, left, bottom, right)
    return this
}

val JPanel.children: Iterable<Component>
    get() = UIUtil.uiChildren(this)
