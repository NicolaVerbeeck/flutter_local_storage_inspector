package com.chimerapps.storageinspector.ui.ide.view.file

import com.chimerapps.storageinspector.api.protocol.model.file.FileInfo
import com.chimerapps.storageinspector.api.protocol.model.file.FileServerValues
import com.chimerapps.storageinspector.ui.util.ensureMain
import com.chimerapps.storageinspector.ui.util.enumerate
import com.intellij.ui.treeStructure.SimpleTree
import com.intellij.ui.treeStructure.Tree
import org.apache.xmlbeans.impl.jam.internal.javadoc.JavadocResults.setRoot
import java.util.Enumeration
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

/**
 * @author Nicola Verbeeck
 */
class FilesTree : Tree() {

    fun buildTree(from: FileServerValues) {
        val root = DirectoryNode(name = "/", parentNode = null)
        from.data.forEach {
            val components = it.path.split('/')
            var parent = root
            components.forEachIndexed { index, component ->
                //Child
                if (index == components.size - 1) {
                    parent.childNodes += FileNode(name = component, info = it, parentNode = parent)
                } else {
                    parent = parent.childNodes.getOrPut(component) { DirectoryNode(name = component, parentNode = parent) }
                }
            }
        }
        root.sort()

        ensureMain {
            this.model = DefaultTreeModel(root)
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