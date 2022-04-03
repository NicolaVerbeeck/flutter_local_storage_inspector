package com.chimerapps.storageinspector.ui.ide

import com.chimerapps.discovery.device.adb.ADBBootstrap
import com.chimerapps.discovery.device.adb.ADBInterface
import com.chimerapps.storageinspector.ui.ide.actions.NewSessionAction
import com.chimerapps.storageinspector.ui.ide.settings.StorageInspectorSettings
import com.chimerapps.storageinspector.ui.util.dispatchMain
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.chimerapps.storageinspector.util.adb.ADBUtils
import com.intellij.execution.ui.RunnerLayoutUi
import com.intellij.execution.ui.layout.PlaceInGrid
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import com.intellij.util.ui.AsyncProcessIcon
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridBagLayout
import java.io.File
import javax.swing.BorderFactory
import javax.swing.JPanel

/**
 * @author Nicola Verbeeck
 */
class InspectorToolWindow(private val project: Project, disposable: Disposable) :
    SimpleToolWindowPanel(/* vertical */ false, /* borderless */ true) {

    companion object {
        fun get(project: Project): Pair<InspectorToolWindow, ToolWindow>? {
            val toolWindowManager = project.getService(ToolWindowManager::class.java)
            val window = toolWindowManager.getToolWindow("Storage Inspector") ?: return null
            val toolWindow = window.contentManager.getContent(0)?.component as? InspectorToolWindow ?: return null
            return toolWindow to window
        }
    }

    private val tabsContainer: RunnerLayoutUi
    private val actionToolbar: ActionToolbar
    private var c = 0

    val isReady: Boolean
        get() = adbInterface != null

    val focussedSessionWindow : InspectorSessionWindow?
        get() {
            for (i in 0 until c) {
                val content = tabsContainer.findContent("$i-contentId") ?: continue
                if (content.isSelected) return content.component as InspectorSessionWindow
            }
            return null
        }

    private var adbBootstrap: ADBBootstrap
    var adbInterface: ADBInterface? = null
        get() = synchronized(this@InspectorToolWindow) {
            val result = field ?: return null
            if (!result.isRealConnection) {
                val path = StorageInspectorSettings.instance.state.adbPath
                if (path != null && File(path).let { it.exists() && it.canExecute() }) {
                    adbBootstrap = ADBBootstrap(ADBUtils.guessPaths(project)) { StorageInspectorSettings.instance.state.adbPath }
                    field = adbBootstrap.bootStrap()
                }
            }
            field
        }
        private set(value) {
            synchronized(this@InspectorToolWindow) {
                field = value
            }
        }

    init {
        actionToolbar = setupViewActions()

        tabsContainer = RunnerLayoutUi.Factory.getInstance(project)
            .create("storage-inspector-ui", "Detail tabs", "Some session name?", disposable)
        tabsContainer.addListener(object : ContentManagerListener {
            override fun contentAdded(event: ContentManagerEvent) {
            }

            override fun contentRemoveQuery(event: ContentManagerEvent) {
            }

            override fun selectionChanged(event: ContentManagerEvent) {
            }

            override fun contentRemoved(event: ContentManagerEvent) {
                (event.content.component as InspectorSessionWindow).onWindowClosed()
            }
        }, disposable)

        adbBootstrap = ADBBootstrap(ADBUtils.guessPaths(project)) { StorageInspectorSettings.instance.state.adbPath }
        bootStrapADB()
    }

    fun redoADBBootstrapBlocking() {
        adbInterface = adbBootstrap.bootStrap()
    }

    private fun bootStrapADB() {
        val loadingContent = JPanel(GridBagLayout())

        val labelAndLoading = JPanel(BorderLayout())

        labelAndLoading.add(JBLabel(Tr.ViewStartingAdb.tr()).also {
            it.font = it.font.deriveFont(50.0f)
            it.foreground = Color.lightGray
        }, BorderLayout.NORTH)
        labelAndLoading.add(
            AsyncProcessIcon.Big("ADBLoadingIndicator")
                .also { it.border = BorderFactory.createEmptyBorder(10, 0, 0, 0) }, BorderLayout.CENTER
        )

        loadingContent.add(labelAndLoading)
        setContent(loadingContent)

        Thread({
            adbInterface = adbBootstrap.bootStrap()
            dispatchMain {
                remove(loadingContent)
                setContent(tabsContainer.component)

                if (tabsContainer.contents.isEmpty())
                    newSessionWindow() //Create first session window
                actionToolbar.updateActionsImmediately()
            }
        }, "ADB startup").start()
    }

    private fun setupViewActions(): ActionToolbar {
        val actionGroup = DefaultActionGroup()

        val newSessionAction = NewSessionAction(this) {
            newSessionWindow()
        }
        actionGroup.add(newSessionAction)

        val toolbar = ActionManager.getInstance().createActionToolbar("Storage Inspector", actionGroup, false)
        setToolbar(toolbar.component)
        return toolbar
    }

    private fun newSessionWindow(): InspectorSessionWindow {
        val sessionWindow = InspectorSessionWindow(project, this)
        val content =
            tabsContainer.createContent("${c++}-contentId", sessionWindow, "${Tr.ViewSession.tr()} $c", null, null)
        content.setPreferredFocusedComponent { sessionWindow }
        sessionWindow.content = content

        content.isCloseable = true
        tabsContainer.addContent(content, -1, PlaceInGrid.center, false)
        tabsContainer.selectAndFocus(content, true, true)
        return sessionWindow
    }

    fun newSessionForTag(tag: String) {
//        TODO("Not yet implemented")
    }

    fun newSessionWithServiceUri(serviceUri: String) {
        newSessionWindow().connectToService(serviceUri)
    }

}