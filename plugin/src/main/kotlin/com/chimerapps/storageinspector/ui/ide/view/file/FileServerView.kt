package com.chimerapps.storageinspector.ui.ide.view.file

import com.chimerapps.storageinspector.inspector.StorageServer
import com.chimerapps.storageinspector.inspector.specific.file.FileInspectorInterface
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * @author Nicola Verbeeck
 */
class FileServerView(private val project: Project) : JPanel(BorderLayout()) {

    private val filesTree = FilesTree()

    private val scope = CoroutineScope(SupervisorJob())
    private var server: StorageServer? = null
    private var serverInterface: FileInspectorInterface? = null

    fun setServer(serverInterface: FileInspectorInterface, server: StorageServer) {
        this.serverInterface = serverInterface

        this.server = server
        scope.launch {
            val newData = serverInterface.getData(server)

            filesTree.buildTree(newData)
        }
    }
}