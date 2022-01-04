package com.chimerapps.storageinspector.ui.ide.view.file

import com.chimerapps.storageinspector.api.protocol.model.file.FileInfo
import com.chimerapps.storageinspector.api.protocol.model.file.FileServerValues
import com.chimerapps.storageinspector.ui.util.ensureMain
import com.chimerapps.storageinspector.ui.util.enumerate
import com.intellij.ide.dnd.DnDManager
import com.intellij.ide.dnd.aware.DnDAwareTree
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.TreeUIHelper
import com.intellij.util.ui.tree.TreeUtil
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.util.Enumeration
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreeSelectionModel


/**
 * @author Nicola Verbeeck
 */
class FilesTree(
    private val onViewFileTapped: (FileInfo) -> Unit,
    private val putFiles: (List<File>, String) -> Unit
) : DnDAwareTree() {

    init {
        val listener = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                val selRow = getRowForLocation(e.x, e.y)
                if (selRow != -1) {
                    val selPath = getPathForLocation(e.x, e.y) ?: return
                    if (e.clickCount == 2) {
                        (selPath.lastPathComponent as? FileNode)?.info?.let(onViewFileTapped)
                    }
                }
            }
        }
        addMouseListener(listener)
        TreeUIHelper.getInstance().installTreeSpeedSearch(this)
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        TreeUtil.installActions(this)
        enableDnD()
    }

    fun buildTree(from: FileServerValues) {
        val root = DirectoryNode(name = "/", parentNode = null)
        from.data.forEach {
            val components = it.path.split('/')
            var parent = root
            components.forEachIndexed { index, component ->
                //Child
                if (index == components.size - 1 && !it.isDir) {
                    parent.childNodes += FileNode(name = component, info = it, parentNode = parent)
                } else {
                    parent = parent.childNodes.getOrPut(component) { DirectoryNode(name = component, parentNode = parent) }
                }
            }
        }
        root.sort()

        ensureMain {
            //TODO optimize so that we update the tree instead of replacing the root node
            this.model = DefaultTreeModel(root)
        }
    }

    private lateinit var myDropTarget: FilesDropTarget

    val selectedFile: FileInfo?
        get() = (selectionPath?.lastPathComponent as? FileNode)?.info

    val selectedPath: String?
        get() =  (selectionPath?.lastPathComponent as? FileSystemNode)?.path

    private fun enableDnD() {
        if (!ApplicationManager.getApplication().isHeadlessEnvironment) {
            myDropTarget = object : FilesDropTarget(this) {
                override fun handleDrop(files: List<File>, into: FileSystemNode) {
                    val target = if (into is FileNode) into.parentNode else into
                    val targetPath = target.path
                    putFiles(files, targetPath)
                }
            }

            val dndManager = DnDManager.getInstance()
            dndManager.registerTarget(myDropTarget, this)
        }
    }
}

private fun <E : FileSystemNode> MutableList<FileSystemNode>.getOrPut(key: String, defaultValue: () -> E): E {
    var node = find { it.name == key }
    @Suppress("UNCHECKED_CAST")
    if (node != null) return node as E
    node = defaultValue()
    add(node)
    return node
}

sealed interface FileSystemNode : TreeNode {
    val size: Long
    val name: String
    val path: String
}

private class DirectoryNode(
    override val name: String,
    val parentNode: DirectoryNode?,
    val childNodes: MutableList<FileSystemNode> = mutableListOf(),
) : FileSystemNode {

    private var calculatedSize = -1L

    override val size: Long
        get() = if (calculatedSize >= 0) {
            calculatedSize
        } else {
            calculatedSize = 0
            childNodes.forEach { calculatedSize += it.size }
            calculatedSize
        }
    override val path: String = buildString {
        if (parentNode == null) {
            append(name)
        }else {
            append(parentNode.path)
            if (!(length == 1 && toString() == "/"))
                append('/')
            append(name)
        }
    }

    override fun getChildAt(childIndex: Int): TreeNode = childNodes[childIndex]

    override fun getChildCount(): Int = childNodes.size

    override fun getParent(): TreeNode? = parentNode

    override fun getIndex(node: TreeNode?): Int = childNodes.indexOf(node)

    override fun getAllowsChildren(): Boolean = true

    override fun isLeaf(): Boolean = false

    override fun children(): Enumeration<TreeNode> = childNodes.enumerate()

    fun sort() {
        childNodes.sortBy { it.name }
        childNodes.forEach { if (it is DirectoryNode) it.sort() }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DirectoryNode

        if (name != other.name) return false
        if (childNodes != other.childNodes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + childNodes.hashCode()
        return result
    }

    override fun toString(): String {
        return name
    }

}

private class FileNode(
    override val name: String,
    val parentNode: DirectoryNode,
    val info: FileInfo,
) : FileSystemNode {
    override val size: Long = info.size
    override val path: String = info.path

    override fun getChildAt(childIndex: Int): TreeNode {
        throw IllegalArgumentException("Children not allowed")
    }

    override fun getChildCount(): Int = 0

    override fun getParent(): TreeNode = parentNode

    override fun getIndex(node: TreeNode?): Int = -1

    override fun getAllowsChildren(): Boolean = false

    override fun isLeaf(): Boolean = true

    override fun children(): Enumeration<TreeNode>? {
        throw IllegalArgumentException("Children not allowed")
    }

    override fun toString(): String {
        return "$name (${size})"
    }
}