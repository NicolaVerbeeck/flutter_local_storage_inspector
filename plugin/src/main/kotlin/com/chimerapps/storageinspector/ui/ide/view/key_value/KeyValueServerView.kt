package com.chimerapps.storageinspector.ui.ide.view.key_value

import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueServerValue
import com.chimerapps.storageinspector.inspector.StorageServer
import com.chimerapps.storageinspector.inspector.specific.key_value.KeyValueInspectorInterface
import com.chimerapps.storageinspector.inspector.specific.key_value.KeyValueStorageServer
import com.chimerapps.storageinspector.ui.ide.actions.RefreshAction
import com.chimerapps.storageinspector.ui.util.list.DiffUtilComparator
import com.chimerapps.storageinspector.ui.util.list.ListUpdateHelper
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.chimerapps.storageinspector.ui.util.notification.NotificationUtil
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.ToolbarDecorator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.io.File
import javax.swing.JPanel
import javax.swing.JTable

/**
 * @author Nicola Verbeeck
 */
class KeyValueServerView(private val project: Project) : JPanel(BorderLayout()) {

    private val table = KeyValueTableView(project, ::removeKeys, ::editValue, ::saveBinary)

    private val refreshAction: RefreshAction
    private val toolbar: ActionToolbar
    private var server: StorageServer? = null
    private var serverInterface: KeyValueInspectorInterface? = null
    private val dispatchHelper = ListUpdateHelper(table.dispatchModel, object : DiffUtilComparator<KeyValueServerValue> {
        override fun representSameItem(left: KeyValueServerValue, right: KeyValueServerValue): Boolean = left.key == right.key
    })
    private val scope = CoroutineScope(SupervisorJob())

    init {
        val actionGroup = DefaultActionGroup()

        refreshAction = RefreshAction(Tr.GenericRefresh.tr(), Tr.GenericRefresh.tr()) {
            refresh()
        }
        actionGroup.addAction(refreshAction)

        toolbar = ActionManager.getInstance().createActionToolbar("Key Value Inspector", actionGroup, false)

        val decorator = ToolbarDecorator.createDecorator(table)
        decorator.disableUpDownActions()
        decorator.setAddAction {
            val server = server as? KeyValueStorageServer
            val serverInterface = serverInterface
            if (server != null && serverInterface != null) {
                EditKeyValueDialog(
                    server.keyOptions,
                    server.supportedKeyTypes,
                    server.keySuggestions,
                    server.supportedValueTypes,
                    project,
                ).also {
                    if (it.showAndGet()) {
                        it.results?.let { results ->
                            scope.launch {
                                serverInterface.set(server, results.first, results.second)
                                dispatchHelper.onListUpdated(serverInterface.getData(server).values)
                            }
                        }
                    }
                }
            }
        }
        decorator.setRemoveAction { table.doRemoveSelectedRows() }

        table.autoResizeMode = JTable.AUTO_RESIZE_OFF

        val contentPanel = JPanel(BorderLayout())

        contentPanel.add(decorator.createPanel(), BorderLayout.CENTER)
        add(toolbar.component, BorderLayout.WEST)
        add(contentPanel, BorderLayout.CENTER)
    }

    fun setServer(serverInterface: KeyValueInspectorInterface, server: StorageServer) {
        this.serverInterface = serverInterface

        dispatchHelper.onListUpdated(emptyList())
        this.server = server
        scope.launch {
            val newData = serverInterface.getData(server)
            dispatchHelper.onListUpdated(newData.values)
        }
    }

    private fun refresh() {
        val serverInterface = serverInterface ?: return
        val server = server ?: return
        scope.launch {
            val newData = serverInterface.reloadData(server)
            dispatchHelper.onListUpdated(newData.values)
        }
    }

    private fun removeKeys(keys: List<ValueWithType>) {
        val serverInterface = serverInterface ?: return
        val server = server ?: return
        scope.launch {
            keys.forEach { key ->
                serverInterface.remove(server, key)
            }
            dispatchHelper.onListUpdated(serverInterface.getData(server).values)
        }
    }

    private fun editValue(key: ValueWithType, newValue: ValueWithType) {
        val serverInterface = serverInterface ?: return
        val server = server ?: return
        scope.launch {
            if (serverInterface.set(server, key, newValue)) {
                dispatchHelper.onListUpdated(serverInterface.getData(server).values)
            }
        }
    }

    private fun saveBinary(key: ValueWithType, toFile: File) {
        val serverInterface = serverInterface ?: return
        val server = server ?: return
        scope.launch {
            try {
                val binaryData = serverInterface.get(server, key)
                ApplicationManager.getApplication().runWriteAction {
                    toFile.writeBytes(binaryData.value as ByteArray)
                }
            } catch (e: Throwable) {
                NotificationUtil.error("Error", "Failed to get data from server: ${e.message}", project)
            }
        }
    }

}