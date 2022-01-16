package com.chimerapps.storageinspector.ui.ide.view.sql

import com.chimerapps.storageinspector.api.protocol.model.sql.SQLColumnDefinition
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLDataType
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLTableDefinition
import com.chimerapps.storageinspector.ui.ide.settings.StorageInspectorProjectSettings
import com.chimerapps.storageinspector.ui.ide.settings.TableConfiguration
import com.chimerapps.storageinspector.ui.util.list.DiffUtilComparator
import com.chimerapps.storageinspector.ui.util.list.ListUpdateHelper
import com.chimerapps.storageinspector.ui.util.list.TableModelDiffUtilDispatchModel
import com.intellij.openapi.project.Project
import com.intellij.ui.table.TableView
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import java.awt.Component
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.swing.DefaultCellEditor
import javax.swing.ListSelectionModel
import javax.swing.event.ChangeEvent
import javax.swing.event.ListSelectionEvent
import javax.swing.event.TableColumnModelEvent
import javax.swing.event.TableColumnModelListener
import javax.swing.table.TableCellEditor
import javax.swing.table.TableColumnModel

typealias TableRow = Map<String, Any?>

class CustomDataTableView(
    private val project: Project,
    private val doRemoveSelectedRows: () -> Unit,
) : TableView<TableRow>() {

    init {
        tableHeader.reorderingAllowed = false

        rowHeight = PlatformIcons.CLASS_ICON.iconHeight * 2
        preferredScrollableViewportSize = JBUI.size(-1, 150)
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                super.keyPressed(e)
                if (e.keyCode == KeyEvent.VK_DELETE || e.keyCode == KeyEvent.VK_BACK_SPACE) {
                    doRemoveSelectedRows()
                }
            }
        })

        tableHeader.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent?) {
                super.mouseReleased(e)
                commitResize()
            }
        })
    }

    private var isResizingColumns: Boolean = false
    private lateinit var internalModel: ListTableModel<TableRow>
    private var databaseName: String? = null
    private var table: SQLTableDefinition? = null
    private var activeColumns: List<SQLColumnDefinition> = emptyList()
    private val columnObserver = object : TableColumnModelListener {
        override fun columnAdded(e: TableColumnModelEvent?) {}

        override fun columnRemoved(e: TableColumnModelEvent?) {
        }

        override fun columnMoved(e: TableColumnModelEvent?) {
        }

        override fun columnMarginChanged(e: ChangeEvent) {
            isResizingColumns = true
        }

        override fun columnSelectionChanged(e: ListSelectionEvent?) {
        }
    }

    fun updateData(newRows: List<TableRow>) {
        val helper = ListUpdateHelper(TableModelDiffUtilDispatchModel(internalModel), TableRowComparator(table!!))
        helper.onListUpdated(newRows)
    }

    fun updateModel(databaseName: String, table: SQLTableDefinition) {
        this.databaseName = databaseName
        this.table = table

        activeColumns = this.table?.columns.orEmpty()
        createTableColumnModel()
    }

    fun ensureColumns(columns: List<String>): Boolean {
        val newActiveColumns = if (columns.isEmpty()) {
            table?.columns.orEmpty()
        } else {
            val newColumns = table?.columns?.filter { it.name in columns }.orEmpty()
            if (newColumns.isEmpty()) {
                columns.map {
                    SQLColumnDefinition(
                        name = it,
                        optional = true,
                        type = SQLDataType.TEXT,
                        autoIncrement = false,
                        nullable = true,
                    )
                }
            } else {
                activeColumns
            }
        }
        if (newActiveColumns == activeColumns)
            return false
        activeColumns = newActiveColumns
        createTableColumnModel()
        return true
    }

    private fun createTableColumnModel() {
        val tableConfiguration = StorageInspectorProjectSettings.instance(project).state.configuration?.databases?.find {
            it.databaseName == databaseName
        }?.configuration?.find {
            it.tableName == table!!.name
        }

        val model = ListTableModel(
            activeColumns.map {
                TableViewColumnInfo(project, it, this.table!!)
            }.toTypedArray(),
            listOf(emptyMap<String, Any?>()),
            0
        )
        internalModel = model

        setColumnModel(createDefaultColumnModel()) //Reset
        setModelAndUpdateColumns(model)

        if (tableConfiguration != null) {
            table!!.columns.forEachIndexed { index, col ->
                tableConfiguration.columns.find { it.columnName == col.name }?.let { columnConfig ->
                    val column = columnModel.getColumn(index)
                    column.minWidth = 15
                    column.maxWidth = Integer.MAX_VALUE
                    column.preferredWidth = columnConfig.width
                }
            }
        }
    }

    override fun setColumnModel(columnModel: TableColumnModel?) {
        this.columnModel?.removeColumnModelListener(columnObserver)
        super.setColumnModel(columnModel)
        columnModel?.addColumnModelListener(columnObserver)
    }

    private fun commitResize() {
        if (!isResizingColumns) return
        isResizingColumns = false

        val table = table ?: return
        val databaseName = databaseName ?: return
        if (table.columns.isEmpty()) return

        StorageInspectorProjectSettings.instance(project).updateState {
            copy(configuration = updateConfiguration {
                updateDatabase(databaseName) {
                    updateTable(table.name) {
                        var configuration: TableConfiguration = this
                        table.columns.forEachIndexed { columnIndex, modelColumn ->
                            val column = columnModel.getColumn(columnIndex)
                            val name = modelColumn.name
                            val width = column.width

                            configuration = configuration.updateColumn(name) {
                                copy(width = width)
                            }
                        }
                        configuration
                    }
                }
            })
        }
    }

    override fun getDefaultEditor(columnClass: Class<*>?): TableCellEditor {
        val editor = super.getDefaultEditor(columnClass)
        (editor as? DefaultCellEditor)?.clickCountToStart = 2
        return editor
    }

    override fun prepareEditor(editor: TableCellEditor?, row: Int, column: Int): Component {
        (editor as? DefaultCellEditor)?.clickCountToStart = 2
        return super.prepareEditor(editor, row, column)
    }

}

private class TableViewColumnInfo(
    private val project: Project,
    val column: SQLColumnDefinition,
    private val table: SQLTableDefinition,
) :
    ColumnInfo<TableRow, String>(column.name) {

    override fun valueOf(item: TableRow?): String? {
        val raw = item?.get(column.name) ?: return null
        @Suppress("UNCHECKED_CAST")
        return when (column.type) {
            SQLDataType.TEXT -> raw.toString()
            SQLDataType.BLOB -> "<binary>"
            SQLDataType.REAL -> (raw as Number).toDouble().toString()
            SQLDataType.INTEGER -> (raw as Number).toLong().toString()
            SQLDataType.BOOLEAN -> if ((raw as Number).toInt() != 0) {
                "true"
            } else {
                "false"
            }
            SQLDataType.DATETIME -> DateTimeFormatter.ISO_INSTANT.format(
                Instant.ofEpochMilli(
                    (raw as Number).toLong()
                )
            )
        }
    }

    override fun isCellEditable(item: TableRow): Boolean {
        //return column.name.lowercase(Locale.getDefault()) != "rowid"
        return false
    }

    override fun setValue(item: TableRow, value: String?) {
//        try {
//            if (!isSame(item.data[column.name], value)) {
//                if (column.isBoolean)
//                    sendUpdateQuery(table, column, item, value?.let { if (it == "true") 1 else 0 }?.toString())
//                else
//                    sendUpdateQuery(table, column, item, value)
//            }
//        } catch (e: Throwable) {
//            NotificationUtil.error("Update failed", "Failed to update: ${e.message}", project)
//        }
    }

//    private fun isSame(original: Any?, value: String?): Boolean {
//        if (original == null && value == null) return true
//        else if (original != null && value == null) return false
//        else if (original == null && value != null) return false
//
//        @Suppress("UNCHECKED_CAST")
//        when (column.type.lowercase(Locale.getDefault())) {
//            "bit", "tinyint", "smallint", "int", "bigint", "integer" -> {
//                val number = original as? Number
//                if (column.isBoolean) {
//                    return number?.toInt() == value?.equals("true")?.let { if (it) 1 else 0 }
//                }
//                return number?.toLong() == value?.toLong()
//            }
//            "float", "real", "double" -> return (original as? Number)?.toDouble() == value?.toDouble()
//            "char", "varchar", "text", "nchar", "nvarchar", "ntext" -> return original == value
//            "date", "time", "datetime", "timestamp", "year" -> {
//                val newInstant = if (value != null) {
//                    value.toLongOrNull()
//                        ?: (DateTimeFormatter.ISO_INSTANT.parse(value) as? Instant)?.toEpochMilli()
//                } else null
//                val old = (original as? Number)?.toLong()
//
//                return newInstant == old
//            }
//            "blob" -> return (original as? List<Int>) == value?.split(',')?.map { it.trim().toInt() }
//        }
//        throw IllegalStateException("Could create statement for column, type not supported: ${column.type}")
//    }

    override fun getPreferredStringValue(): String {
        return column.name
    }
}

private class TableRowComparator(table: SQLTableDefinition) : DiffUtilComparator<TableRow> {

    private val primaryKeys = table.primaryKey

    override fun representSameItem(left: TableRow, right: TableRow): Boolean {
        if (primaryKeys.isEmpty()) {
            val leftRowId = left.entries.find { it.key.equals("rowid", ignoreCase = true) }?.value
            val rightRowId = right.entries.find { it.key.equals("rowid", ignoreCase = true) }?.value
            if (leftRowId != null && rightRowId != null)
                return leftRowId == rightRowId
            return false //No rowId for one or the other -> bail
        }

        return left.filter { it.key in primaryKeys } == right.filter { it.key in primaryKeys }
    }

}