package com.chimerapps.storageinspector.ui.ide

import com.chimerapps.discovery.device.DirectPreparedConnection
import com.chimerapps.discovery.device.PreparedDeviceConnection
import com.chimerapps.discovery.device.idevice.IDeviceBootstrap
import com.chimerapps.discovery.ui.ConnectDialog
import com.chimerapps.discovery.ui.DiscoveredDeviceConnection
import com.chimerapps.discovery.ui.ManualConnection
import com.chimerapps.discovery.utils.freePort
import com.chimerapps.storageinspector.api.StorageInspectorConnectionListener
import com.chimerapps.storageinspector.api.StorageInspectorProtocolConnection
import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocolListener
import com.chimerapps.storageinspector.api.protocol.model.ServerId
import com.chimerapps.storageinspector.ui.ide.actions.ConnectAction
import com.chimerapps.storageinspector.ui.ide.actions.DisconnectAction
import com.chimerapps.storageinspector.ui.ide.settings.StorageInspectorSettings
import com.chimerapps.storageinspector.ui.util.ensureMain
import com.chimerapps.storageinspector.ui.ide.view.StorageInspectorStatusBar
import com.chimerapps.storageinspector.ui.util.ProjectSessionIconProvider
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.ui.content.Content
import com.intellij.util.IconUtil
import java.awt.BorderLayout
import java.io.File
import javax.swing.JPanel
import javax.swing.SwingUtilities

class InspectorSessionWindow(
    private val project: Project,
    private val toolWindow: InspectorToolWindow
) : JPanel(BorderLayout()) {

    companion object {
        const val DEFAULT_IDEVICE_PATH = "/usr/local/bin"
//        private const val APP_PREFERENCE_SPLITTER_STATE = "${AppPreferences.PREFIX}detailSplitter"
    }

    lateinit var content: Content

    private val rootContent = JPanel(BorderLayout())
    private val connectToolbar = setupConnectToolbar()
    private var lastConnection: PreparedDeviceConnection? = null
    private val statusBar = StorageInspectorStatusBar()
    private var connection: StorageInspectorProtocolConnection? = null

    var connectionMode: ConnectionMode = ConnectionMode.MODE_DISCONNECTED
        private set(value) {
            field = value
            ensureMain {
                connectToolbar.updateActionsImmediately()
            }
        }

    init {
        add(rootContent, BorderLayout.CENTER)
        add(statusBar, BorderLayout.SOUTH)

//        val splitter = JBSplitter(APP_PREFERENCE_SPLITTER_STATE, 0.2f)
//        splitter.firstComponent = tablesView
//        splitter.secondComponent = tableView
//
//        rootContent.add(splitter, BorderLayout.CENTER)
    }

    private fun setupConnectToolbar(): ActionToolbar {
        val actionGroup = DefaultActionGroup()

        actionGroup.add(ConnectAction(this) {
            showConnectDialog()
        })
        actionGroup.add(DisconnectAction(this) {
            disconnect()

            connectionMode = ConnectionMode.MODE_DISCONNECTED
        })

        val toolbar = ActionManager.getInstance().createActionToolbar("Storage Inspector", actionGroup, true)
        val toolbarContainer = JPanel(BorderLayout())
        toolbarContainer.add(toolbar.component, BorderLayout.WEST)

        rootContent.add(toolbarContainer, BorderLayout.NORTH)
        return toolbar
    }

    private fun showConnectDialog() {
        val result = ConnectDialog.show(
            SwingUtilities.getWindowAncestor(this),
            toolWindow.adbInterface ?: return,
            IDeviceBootstrap(File(StorageInspectorSettings.instance.state.iDeviceBinariesPath ?: DEFAULT_IDEVICE_PATH)),
            6396,
            sessionIconProvider = ProjectSessionIconProvider.instance(project),
            configurePluginCallback = {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Storage Inspector")
                toolWindow.adbInterface!! to IDeviceBootstrap(
                    File(
                        StorageInspectorSettings.instance.state.iDeviceBinariesPath ?: DEFAULT_IDEVICE_PATH
                    )
                )
            }) ?: return

        result.discovered?.let {
            tryConnectSession(it)
        }
        result.direct?.let {
            tryConnectDirect(it)
        }
    }

    private fun disconnect() {
        try {
            connection?.close()
        } catch (ignore: Throwable) {
        }
        connection = null
        try {
            lastConnection?.tearDown()
        } catch (ignore: Throwable) {
        }
        lastConnection = null
    }

    private fun tryConnectDirect(directConnection: ManualConnection) {
        disconnect()

        connectOnConnection(DirectPreparedConnection(directConnection.ip, directConnection.port))
    }

    private fun tryConnectSession(discovered: DiscoveredDeviceConnection) {
        disconnect()

        val connection = discovered.device.prepareConnection(freePort(), discovered.session.port)
        connectOnConnection(connection)
    }

    private fun connectOnConnection(connection: PreparedDeviceConnection) {
        this.connection =
            StorageInspectorProtocolConnection(connection.uri).also {
                it.addListener(statusBar)
                it.addListener(object : StorageInspectorConnectionListener {
                    override fun onClosed() {
                        disconnect()
                        ensureMain {
                            connectionMode = ConnectionMode.MODE_DISCONNECTED
                            content.icon?.let { icon ->
                                content.icon = IconUtil.desaturate(icon)
                            }
                        }
                    }
                })
                it.protocol.addListener(statusBar)
                it.protocol.addListener(object : StorageInspectorProtocolListener {
                    override fun onServerIdentification(serverId: ServerId) {
                        ensureMain {
                            val newIcon = serverId.icon?.let { iconString ->
                                ProjectSessionIconProvider.instance(project, requestedHeight = 16, requestedWidth = 16).iconForString(iconString)
                            }
                            content.icon = newIcon
                        }
                    }
                })
            }
        this.connection?.connect()
        lastConnection = connection
        connectionMode = ConnectionMode.MODE_CONNECTED
    }

    fun onWindowClosed() {
        disconnect()
    }
}

enum class ConnectionMode {
    MODE_CONNECTED,
    MODE_DISCONNECTED
}