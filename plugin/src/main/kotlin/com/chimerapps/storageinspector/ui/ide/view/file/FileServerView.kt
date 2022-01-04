package com.chimerapps.storageinspector.ui.ide.view.file

import com.chimerapps.storageinspector.api.protocol.model.file.FileInfo
import com.chimerapps.storageinspector.inspector.StorageServer
import com.chimerapps.storageinspector.inspector.specific.file.FileInspectorInterface
import com.chimerapps.storageinspector.ui.ide.actions.RefreshAction
import com.chimerapps.storageinspector.ui.util.ensureMain
import com.chimerapps.storageinspector.ui.util.file.chooseSaveFile
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.AnActionButton
import com.intellij.ui.ToolbarDecorator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.io.File
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Nicola Verbeeck
 */
class FileServerView(private val project: Project) : JPanel(BorderLayout()) {

    private val filesTree = FilesTree(::getAndOpenFile, ::putFiles)

    private val scope = CoroutineScope(SupervisorJob())
    private var server: StorageServer? = null
    private var serverInterface: FileInspectorInterface? = null

    init {
        val actionGroup = DefaultActionGroup()

        val refreshAction = RefreshAction(Tr.GenericRefresh.tr(), Tr.GenericRefresh.tr()) {
            refresh()
        }
        actionGroup.addAction(refreshAction)

        val toolbar = ActionManager.getInstance().createActionToolbar("File Inspector", actionGroup, false)

        val decorator = ToolbarDecorator.createDecorator(filesTree)
        decorator.disableUpDownActions()
        decorator.setAddAction {
            //TODO
        }
        decorator.setRemoveAction {
            //TODO
        }
        decorator.addExtraAction(SaveButton {
            filesTree.selectedFile?.let { save(it, open = false) }
        })

        val contentPanel = JPanel(BorderLayout())

        contentPanel.add(decorator.createPanel(), BorderLayout.CENTER)
        add(toolbar.component, BorderLayout.WEST)
        add(contentPanel, BorderLayout.CENTER)
    }

    fun setServer(serverInterface: FileInspectorInterface, server: StorageServer) {
        this.serverInterface = serverInterface

        this.server = server
        scope.launch {
            val newData = serverInterface.getData(server)

            filesTree.buildTree(newData)
        }
    }

    private fun refresh() {
        val serverInterface = serverInterface ?: return
        val server = server ?: return
        scope.launch {
            val newData = serverInterface.reloadData(server)
            filesTree.buildTree(newData)
        }
    }

    private fun getAndOpenFile(info: FileInfo) = save(info, open = true)

    private fun save(info: FileInfo, open: Boolean) {
        val serverInterface = serverInterface ?: return
        val server = server ?: return

        var ext = File(info.path).extension
        if (ext.isNotEmpty()) {
            ext = ".$ext"
        }
        val target = chooseSaveFile("Save file to", ext) ?: return
        val targetFile = if (target.extension.isEmpty()) File("${target.path}$ext") else target

        scope.launch {
            val bytes = serverInterface.getContents(server, info.path)
            ensureMain {
                ApplicationManager.getApplication().runWriteAction {
                    targetFile.writeBytes(bytes)
                    if (open) {
                        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(targetFile)
                        println("Got virtual file: $virtualFile")
                        virtualFile?.let { OpenFileDescriptor(project, it).navigate(true) }
                    }
                }
            }
        }
    }

    private fun putFiles(files: List<File>, toPath: String) {
        val serverInterface = serverInterface ?: return
        val server = server ?: return

        val allFiles = gatherFiles(files)

        scope.launch {
            allFiles.forEach { (subPath, file) ->
                val targetPath = "$toPath$subPath"
                if (serverInterface.putContents(server, targetPath, file.readBytes())) {
                    filesTree.buildTree(serverInterface.getData(server))
                }
            }
        }
    }

    private fun gatherFiles(files: List<File>): List<Pair<String, File>> {
        val list = mutableListOf<Pair<String, File>>()
        files.forEach { list += gatherFiles(it, "") }
        return list
    }

    private fun gatherFiles(file: File, prepend: String): List<Pair<String, File>> {
        if (!file.exists()) return emptyList()
        if (!file.isDirectory) return listOf("${prepend}/${file.name}" to file)
        val children = file.listFiles() ?: return emptyList()
        val list = mutableListOf<Pair<String, File>>()

        val newPrefix = "$prepend/${file.name}"
        children.forEach { child ->
            list += gatherFiles(child, newPrefix)
        }

        return list
    }
}

private class SaveButton(private val callback: () -> Unit) : AnActionButton("Save", "Save", AllIcons.Actions.MenuSaveall), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        callback()
    }

    override fun updateButton(e: AnActionEvent) {
        super.updateButton(e)
        if (!e.presentation.isEnabled) return
        val c: JComponent = contextComponent
        if (c is FilesTree) {
            e.presentation.isEnabled = isEnabled && c.selectedFile != null
        }
    }
}
