package com.chimerapps.storageinspector.ui.ide.view

import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocolListener
import com.chimerapps.storageinspector.api.protocol.model.ServerId
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLTableDefinition
import com.chimerapps.storageinspector.inspector.StorageInspectorInterface
import com.chimerapps.storageinspector.inspector.StorageServer
import com.chimerapps.storageinspector.inspector.StorageServerType
import com.chimerapps.storageinspector.inspector.specific.file.FileStorageServer
import com.chimerapps.storageinspector.inspector.specific.key_value.KeyValueStorageServer
import com.chimerapps.storageinspector.inspector.specific.sql.SQLStorageServer
import com.chimerapps.storageinspector.ui.util.IncludedIcons
import com.chimerapps.storageinspector.ui.util.ProjectSessionIconProvider
import com.chimerapps.storageinspector.ui.util.enumerate
import com.chimerapps.storageinspector.ui.util.list.DiffUtilComparator
import com.chimerapps.storageinspector.ui.util.list.DiffUtilDispatchModel
import com.chimerapps.storageinspector.ui.util.list.ListUpdateHelper
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.intellij.icons.AllIcons
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import java.util.Enumeration
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreeSelectionModel

/**
 * @author Nicola Verbeeck
 */
class StorageInspectorServersView(
    iconProvider: ProjectSessionIconProvider,
    onTableSelectionChanged: (server: StorageServer, child: Any?) -> Unit,
) : JPanel(BorderLayout()),
    StorageInspectorProtocolListener {

    private val model = StorageInspectorTreeModel()
    private val storageRoot = StorageRootNode()
    private val keyValueListUpdateHelper = ListUpdateHelper(
        comparator = StorageServerComparator(),
        model = TreeModelDiffUtilDispatchModel(model, storageRoot.keyValueServersRoot),
    )
    private val fileServerListUpdateHelper = ListUpdateHelper(
        comparator = StorageServerComparator(),
        model = TreeModelDiffUtilDispatchModel(model, storageRoot.fileServersRoot),
    )
    private val sqlListUpdateHelper = ListUpdateHelper(
        comparator = StorageServerComparator(),
        model = TreeModelDiffUtilDispatchModel(model, storageRoot.sqlServersRoot),
    )

    lateinit var inspectorInterface: StorageInspectorInterface

    private val tree = Tree(model).also {
        it.showsRootHandles = true
        it.isRootVisible = false
        it.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        it.cellRenderer = TreeCellRenderer(iconProvider)
        it.selectionModel.addTreeSelectionListener { _ ->
            when (val node = it.lastSelectedPathComponent) {
                is ServerNode -> onTableSelectionChanged(node.server, null)
                is SQLTableNode -> onTableSelectionChanged(node.parentNode.server, node.table)
            }
        }
    }

    init {
        add(tree, BorderLayout.CENTER)
    }

    override fun onServerIdentification(serverId: ServerId) {
        model.setRoot(storageRoot)
    }

    fun onServerAdded(server: KeyValueStorageServer) {
        keyValueListUpdateHelper.onListUpdated(inspectorInterface.keyValueInterface.servers)
    }

    fun onServerAdded(server: FileStorageServer) {
        fileServerListUpdateHelper.onListUpdated(inspectorInterface.fileInterface.servers)
    }

    fun onServerAdded(server: SQLStorageServer) {
        sqlListUpdateHelper.onListUpdated(inspectorInterface.sqlInterface.servers)
    }

}

private class StorageRootNode : TreeNode {

    val keyValueServersRoot = StorageServerRootNode<KeyValueStorageServer>(this, StorageServerType.KEY_VALUE)
    val fileServersRoot = StorageServerRootNode<FileStorageServer>(this, StorageServerType.FILE)
    val sqlServersRoot = StorageServerRootNode<SQLStorageServer>(this, StorageServerType.SQL)

    val serverTypeNodes = listOf(keyValueServersRoot, fileServersRoot, sqlServersRoot)

    override fun getChildAt(childIndex: Int): TreeNode = serverTypeNodes[childIndex]

    override fun getChildCount(): Int = serverTypeNodes.size

    override fun getParent(): TreeNode? = null

    override fun getIndex(node: TreeNode?): Int = serverTypeNodes.indexOf(node)

    override fun getAllowsChildren(): Boolean = true

    override fun isLeaf(): Boolean = serverTypeNodes.isEmpty()

    override fun children(): Enumeration<out TreeNode> = serverTypeNodes.enumerate()

}

private class StorageServerRootNode<T : StorageServer>(
    private val parent: TreeNode,
    val storageType: StorageServerType,
) : TreeNode {

    var servers: List<T> = emptyList()
        set(value) {
            field = value
            makeNodes()
        }
    private var childNodes = mutableListOf<TreeNode>()

    private fun makeNodes() {
        childNodes.clear()
        servers.mapTo(childNodes) {
            if (it is SQLStorageServer)
                SQLServerNode(it, this, it.tables)
            else
                ServerNode(it, this)
        }
    }

    override fun getChildAt(childIndex: Int): TreeNode = childNodes[childIndex]

    override fun getChildCount(): Int = childNodes.size

    override fun getParent(): TreeNode = parent

    override fun getIndex(node: TreeNode?): Int = childNodes.indexOf(node)

    override fun getAllowsChildren(): Boolean = true

    override fun isLeaf(): Boolean = childNodes.isEmpty()

    override fun children(): Enumeration<out TreeNode> = childNodes.enumerate()

}

private interface DefaultIconProvider {
    val defaultIcon: Icon
}

private class ServerNode(val server: StorageServer, private val parentNode: TreeNode) : TreeNode, DefaultIconProvider {

    override val defaultIcon: Icon
        get() = when (server.type) {
            StorageServerType.KEY_VALUE -> AllIcons.General.ActualZoom
            StorageServerType.FILE -> AllIcons.Actions.Annotate
            StorageServerType.SQL -> throw IllegalStateException("SQL server nodes should be represented as SQLServerNode")
        }

    override fun getChildAt(childIndex: Int): TreeNode = throw IllegalStateException("No children")

    override fun getChildCount(): Int = 0

    override fun getParent(): TreeNode = parentNode

    override fun getIndex(node: TreeNode?): Int = -1

    override fun getAllowsChildren(): Boolean = false

    override fun isLeaf(): Boolean = true

    override fun children(): Enumeration<out TreeNode> = throw IllegalStateException("No children")

    override fun toString(): String = server.name
}

private class SQLServerNode(
    val server: StorageServer,
    private val parentNode: TreeNode,
    tables: List<SQLTableDefinition>,
) : TreeNode, DefaultIconProvider {

    override val defaultIcon: Icon = IncludedIcons.Type.Database
    val childNodes = tables.map { SQLTableNode(this, it) }

    override fun getChildAt(childIndex: Int): TreeNode = childNodes[childIndex]

    override fun getChildCount(): Int = childNodes.size

    override fun getParent(): TreeNode = parentNode

    override fun getIndex(node: TreeNode?): Int = childNodes.indexOf(node)

    override fun getAllowsChildren(): Boolean = true

    override fun isLeaf(): Boolean = childNodes.isEmpty()

    override fun children(): Enumeration<out TreeNode> = childNodes.enumerate()

    override fun toString(): String = server.name
}

private class SQLTableNode(val parentNode: SQLServerNode, val table: SQLTableDefinition) : TreeNode {
    override fun getChildAt(childIndex: Int): TreeNode = throw IllegalStateException("No children")

    override fun getChildCount(): Int = 0

    override fun getParent(): TreeNode = parentNode

    override fun getIndex(node: TreeNode?): Int = -1

    override fun getAllowsChildren(): Boolean = false

    override fun isLeaf(): Boolean = true

    override fun children(): Enumeration<out TreeNode> = throw IllegalStateException("No children")

    override fun toString(): String = table.name
}

private class TreeCellRenderer(private val iconProvider: ProjectSessionIconProvider) : ColoredTreeCellRenderer() {
    override fun customizeCellRenderer(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ) {
        when (value) {
            is StorageServerRootNode<*> -> {
                when (value.storageType) {
                    StorageServerType.KEY_VALUE -> {
                        icon = AllIcons.General.ActualZoom
                        append(Tr.ServersKeyValue.tr())
                    }
                    StorageServerType.FILE -> {
                        icon = AllIcons.Actions.Annotate
                        append(Tr.ServersFile.tr())
                    }
                    StorageServerType.SQL -> {
                        icon = IncludedIcons.Type.Database
                        append(Tr.ServersSql.tr())
                    }
                }
            }
            is ServerNode -> {
                icon = value.server.icon?.let(iconProvider::iconForString) ?: value.defaultIcon
                append(value.server.name)
            }
            is SQLServerNode -> {
                icon = value.server.icon?.let(iconProvider::iconForString) ?: value.defaultIcon
                append(value.server.name)
            }
            is SQLTableNode -> {
                icon = value.parentNode.server.icon?.let(iconProvider::iconForString) ?: value.parentNode.defaultIcon
                append(value.table.name)
            }
        }
    }
}

private class StorageInspectorTreeModel : DefaultTreeModel(DefaultMutableTreeNode())

private class StorageServerComparator<T : StorageServer> : DiffUtilComparator<T> {
    override fun representSameItem(left: T, right: T): Boolean = left.id == right.id
}

private class TreeModelDiffUtilDispatchModel<T : StorageServer>(
    private val rootModel: DefaultTreeModel,
    private val node: StorageServerRootNode<T>,
) : DiffUtilDispatchModel<T> {
    override fun onInserted(position: Int, count: Int) {
        rootModel.nodesWereInserted(node, IntArray(count) { i -> i + position })
    }

    override fun onRemoved(position: Int, count: Int) {
        rootModel.nodesWereRemoved(node, IntArray(count) { i -> i + position }, emptyArray())
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        rootModel.nodesChanged(node, intArrayOf(fromPosition, toPosition))
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        rootModel.nodesChanged(node, IntArray(count) { i -> i + position })
    }

    override fun setItems(items: List<T>) {
        node.servers = items
    }
}