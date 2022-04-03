package com.chimerapps.storageinspector.ui.ide

import com.chimerapps.discovery.device.DirectPreparedConnection
import com.chimerapps.discovery.device.PreparedDeviceConnection
import com.chimerapps.discovery.device.idevice.IDeviceBootstrap
import com.chimerapps.discovery.ui.ConnectDialog
import com.chimerapps.discovery.ui.DiscoveredDeviceConnection
import com.chimerapps.discovery.ui.LocalizationDelegate
import com.chimerapps.discovery.ui.ManualConnection
import com.chimerapps.discovery.utils.freePort
import com.chimerapps.storageinspector.api.ReverseStorageInspectorProtocolConnection
import com.chimerapps.storageinspector.api.StorageInspectorConnectionListener
import com.chimerapps.storageinspector.api.StorageInspectorProtocolConnection
import com.chimerapps.storageinspector.api.StorageInspectorProtocolConnectionInterface
import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocolListener
import com.chimerapps.storageinspector.api.protocol.model.ServerId
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLTableDefinition
import com.chimerapps.storageinspector.api.protocol.vmservice.VMService
import com.chimerapps.storageinspector.inspector.StorageServer
import com.chimerapps.storageinspector.inspector.StorageServerType
import com.chimerapps.storageinspector.inspector.specific.InspectorInterfaceListener
import com.chimerapps.storageinspector.inspector.specific.file.FileStorageServer
import com.chimerapps.storageinspector.inspector.specific.key_value.KeyValueStorageServer
import com.chimerapps.storageinspector.inspector.specific.sql.SQLStorageServer
import com.chimerapps.storageinspector.ui.ide.actions.ConnectAction
import com.chimerapps.storageinspector.ui.ide.actions.DisconnectAction
import com.chimerapps.storageinspector.ui.ide.actions.ResumeAction
import com.chimerapps.storageinspector.ui.ide.renderer.StorageInspectorConnectRenderDelegate
import com.chimerapps.storageinspector.ui.ide.settings.StorageInspectorSettings
import com.chimerapps.storageinspector.ui.ide.view.StorageInspectorServersView
import com.chimerapps.storageinspector.ui.ide.view.StorageInspectorStatusBar
import com.chimerapps.storageinspector.ui.ide.view.file.FileServerView
import com.chimerapps.storageinspector.ui.ide.view.key_value.KeyValueServerView
import com.chimerapps.storageinspector.ui.ide.view.sql.SQLServerView
import com.chimerapps.storageinspector.ui.util.ProjectSessionIconProvider
import com.chimerapps.storageinspector.ui.util.dispatchMain
import com.chimerapps.storageinspector.ui.util.ensureMain
import com.chimerapps.storageinspector.ui.util.preferences.AppPreferences
import com.chimerapps.storageinspector.util.analytics.AnalyticsEvent
import com.chimerapps.storageinspector.util.analytics.EventTracker
import com.chimerapps.storageinspector.util.classLogger
import com.google.gsonpackaged.JsonObject
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import com.intellij.ui.content.Content
import com.intellij.util.IconUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URI
import java.util.Enumeration
import javax.swing.JPanel
import javax.swing.SwingUtilities


class InspectorSessionWindow(
    private val project: Project,
    private val toolWindow: InspectorToolWindow
) : JPanel(BorderLayout()) {

    companion object {
        const val DEFAULT_IDEVICE_PATH = "/usr/local/bin"
        private const val APP_PREFERENCE_SPLITTER_STATE = "${AppPreferences.PREFIX}detailSplitter"
    }

    lateinit var content: Content

    private val rootContent = JPanel(BorderLayout())
    private val connectToolbar = setupConnectToolbar()
    private var lastConnection: PreparedDeviceConnection? = null
    private val statusBar = StorageInspectorStatusBar()
    var connection: StorageInspectorProtocolConnectionInterface? = null
        private set
    private val serversView = StorageInspectorServersView(
        ProjectSessionIconProvider.instance(project, requestedWidth = 16, requestedHeight = 16),
        ::onServerSelectionChanged,
    )
    private var currentDetailView: Any? = null
    private val splitter: JBSplitter
    private val scope = CoroutineScope(SupervisorJob())
    private lateinit var connectActionGroup: DefaultActionGroup

    var connectionMode: ConnectionMode = ConnectionMode.MODE_DISCONNECTED
        private set(value) {
            field = value
            ensureMain {
                updateConnectActionGroup()
            }
        }

    val selectedDatabaseSchema: String?
        get() = (currentDetailView as? SQLServerView)?.databaseScheme

    init {
        add(rootContent, BorderLayout.CENTER)
        add(statusBar, BorderLayout.SOUTH)

        splitter = JBSplitter(APP_PREFERENCE_SPLITTER_STATE, 0.2f)
        splitter.firstComponent = serversView
        splitter.secondComponent = JPanel()

        rootContent.add(splitter, BorderLayout.CENTER)

        updateConnectActionGroup()
    }

    private fun setupConnectToolbar(): ActionToolbar {
        connectActionGroup = DefaultActionGroup()

        val toolbar = ActionManager.getInstance().createActionToolbar("Storage Inspector", connectActionGroup, true)
        val toolbarContainer = JPanel(BorderLayout())
        toolbarContainer.add(toolbar.component, BorderLayout.WEST)

        rootContent.add(toolbarContainer, BorderLayout.NORTH)
        return toolbar
    }

    private fun showConnectDialog() {
        val result = ConnectDialog.show(
            parent = SwingUtilities.getWindowAncestor(this),
            adbInterface = toolWindow.adbInterface ?: return,
            iDeviceBootstrap = IDeviceBootstrap(File(StorageInspectorSettings.instance.state.iDeviceBinariesPath ?: DEFAULT_IDEVICE_PATH)),
            announcementPort = 6396,
            sessionIconProvider = ProjectSessionIconProvider.instance(project),
            configurePluginCallback = {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Storage Inspector")
                toolWindow.adbInterface!! to IDeviceBootstrap(
                    File(
                        StorageInspectorSettings.instance.state.iDeviceBinariesPath ?: DEFAULT_IDEVICE_PATH
                    )
                )
            },
            localizationDelegate = LocalizationDelegate(),
            renderDelegate = StorageInspectorConnectRenderDelegate(),
        ) ?: return

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
            StorageInspectorProtocolConnection(connection.uri).also(::setupConnection)
        this.connection?.connect()
        lastConnection = connection
        EventTracker.default.logEvent(AnalyticsEvent.CONNECT)
        connectionMode = ConnectionMode.MODE_CONNECTED
    }

    private fun setupConnection(connection: StorageInspectorProtocolConnectionInterface) {
        connection.addListener(statusBar)
        connection.addListener(object : StorageInspectorConnectionListener {
            override fun onClosed() {
                disconnect()
                ensureMain {
                    connectionMode = ConnectionMode.MODE_DISCONNECTED
                    content.icon?.let { icon ->
                        content.icon = IconUtil.desaturate(icon)
                    }
                }
            }

            override fun onConnected() {
                ensureMain {
                    connectionMode = ConnectionMode.MODE_CONNECTED
                }
            }
        })
        connection.protocol.addListener(statusBar)
        connection.protocol.addListener(object : StorageInspectorProtocolListener {
            override fun onServerIdentification(serverId: ServerId) {
                ensureMain {
                    val newIcon = serverId.icon?.let { iconString ->
                        ProjectSessionIconProvider.instance(project, requestedHeight = 16, requestedWidth = 16).iconForString(iconString)
                    }
                    content.icon = newIcon
                    dispatchMain {
                        updateConnectActionGroup()
                    }
                }
            }
        })
        connection.protocol.addListener(serversView)
        serversView.inspectorInterface = connection.storageInterface
        connection.protocol.addListener(connection.storageInterface)
        connection.storageInterface.keyValueInterface.addListener(object : InspectorInterfaceListener<KeyValueStorageServer> {
            override fun onServerAdded(server: KeyValueStorageServer) = serversView.onServerAdded(server)
        })
        connection.storageInterface.fileInterface.addListener(object : InspectorInterfaceListener<FileStorageServer> {
            override fun onServerAdded(server: FileStorageServer) = serversView.onServerAdded(server)
        })
        connection.storageInterface.sqlInterface.addListener(object : InspectorInterfaceListener<SQLStorageServer> {
            override fun onServerAdded(server: SQLStorageServer) = serversView.onServerAdded(server)
        })
    }

    fun onWindowClosed() {
        disconnect()
    }

    private fun onServerSelectionChanged(storageServer: StorageServer, child: Any?) {
        val connection = connection?.storageInterface ?: return
        when (storageServer.type) {
            StorageServerType.KEY_VALUE -> {
                val detail = currentDetailView as? KeyValueServerView ?: KeyValueServerView(project)
                detail.setServer(connection.keyValueInterface, storageServer)

                currentDetailView = detail
                splitter.secondComponent = detail
            }
            StorageServerType.FILE -> {
                val detail = currentDetailView as? FileServerView ?: FileServerView(project)
                detail.setServer(connection.fileInterface, storageServer)

                currentDetailView = detail
                splitter.secondComponent = detail
            }
            StorageServerType.SQL -> {
                val detail = currentDetailView as? SQLServerView ?: SQLServerView(project)
                detail.setServer(connection.sqlInterface, storageServer as SQLStorageServer, child as SQLTableDefinition?)

                currentDetailView = detail
                splitter.secondComponent = detail
            }
        }

        classLogger.debug("onServerSelectionChanged: $storageServer")
    }

    private fun updateConnectActionGroup() {
        val showResume = connectionMode == ConnectionMode.MODE_CONNECTED && connection?.storageInterface?.isPaused == true
        connectActionGroup.removeAll()
        connectActionGroup.add(ConnectAction(this) {
            showConnectDialog()
        })

        if (showResume) {
            lateinit var resumeAction: ResumeAction
            resumeAction = ResumeAction(this) {
                scope.launch {
                    connection?.storageInterface?.unpause()
                    ensureMain {
                        connectActionGroup.remove(resumeAction)
                        connectToolbar.updateActionsImmediately()
                    }
                }
            }
            connectActionGroup.add(resumeAction)
        }

        connectActionGroup.add(DisconnectAction(this) {
            disconnect()

            connectionMode = ConnectionMode.MODE_DISCONNECTED
        })

        connectToolbar.updateActionsImmediately()
    }

    fun connectToService(serviceUri: String) {
        lastConnection?.tearDown()
        lastConnection = null
        connection?.close()

        this.connection =
            ReverseStorageInspectorProtocolConnection { port ->
                scope.launch {
                    val ip = findLocalIP()
                    VMService(URI(serviceUri)).use { service ->
                        service.connect()
                        val vm = service.getVM()
                        val main = vm.isolates.filter { it.name == "main" || it.name == "main()" }
                        val isolatesToTry = main.ifEmpty { vm.isolates }
                        isolatesToTry.forEach { isolate ->
                            service.callExtensionMethod("ext.storage_inspector.connect", isolate.id, JsonObject().also {
                                it.addProperty("port", port)
                                it.addProperty("ip", ip)
                            })
                        }
                    }
                }
            }.also(::setupConnection)
        this.connection?.connect()
        EventTracker.default.logEvent(AnalyticsEvent.CONNECT)
    }
}

enum class ConnectionMode {
    MODE_CONNECTED,
    MODE_DISCONNECTED
}

private fun findLocalIP(): String {
    val raw = findLocalIPRaw()
    if (raw.startsWith("/"))
        return raw.substring(1)

    return raw
}

private fun findLocalIPRaw() : String {
    try {
        var candidateAddress: InetAddress? = null
        val ifaces: Enumeration<*> = NetworkInterface.getNetworkInterfaces()
        while (ifaces.hasMoreElements()) {
            val iface = ifaces.nextElement() as NetworkInterface
            val inetAddrs: Enumeration<*> = iface.inetAddresses
            while (inetAddrs.hasMoreElements()) {
                val inetAddr = inetAddrs.nextElement() as InetAddress
                if (!inetAddr.isLoopbackAddress) {
                    if (inetAddr.isSiteLocalAddress) {
                        return inetAddr.toString()
                    } else if (candidateAddress == null) {
                        candidateAddress = inetAddr
                    }
                }
            }
        }
        if (candidateAddress != null) {
            return candidateAddress.toString()
        }
    } catch (ignored: Exception) {
    }
    return InetAddress.getLocalHost().toString()
}