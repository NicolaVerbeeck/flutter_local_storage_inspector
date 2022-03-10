package com.chimerapps.storageinspector.ui.ide.view.sql

import com.chimerapps.storageinspector.api.protocol.model.StorageType
import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLColumnDefinition
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLDataType
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLDateTimeFormat
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLTableDefinition
import com.chimerapps.storageinspector.api.protocol.model.sql.SQLTableExtension
import com.chimerapps.storageinspector.ui.ide.settings.StorageInspectorProjectSettings
import com.chimerapps.storageinspector.ui.ide.settings.TableConfiguration
import com.chimerapps.storageinspector.ui.ide.view.key_value.BinaryCellEditor
import com.chimerapps.storageinspector.ui.ide.view.key_value.BinaryTableCellRenderer
import com.chimerapps.storageinspector.ui.ide.view.key_value.DateTimeCellEditor
import com.chimerapps.storageinspector.ui.ide.view.key_value.DateTimeTableCellRenderer
import com.chimerapps.storageinspector.ui.util.file.chooseSaveFile
import com.chimerapps.storageinspector.ui.util.list.DiffUtilComparator
import com.chimerapps.storageinspector.ui.util.list.ListUpdateHelper
import com.chimerapps.storageinspector.ui.util.list.TableModelDiffUtilDispatchModel
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.chimerapps.storageinspector.ui.util.notification.NotificationUtil
import com.intellij.openapi.project.Project
import com.intellij.ui.table.TableView
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ComboBoxCellEditor
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import java.awt.Component
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.util.Locale
import javax.swing.DefaultCellEditor
import javax.swing.ListSelectionModel
import javax.swing.event.ChangeEvent
import javax.swing.event.ListSelectionEvent
import javax.swing.event.TableColumnModelEvent
import javax.swing.event.TableColumnModelListener
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumnModel

typealias TableRow = Map<String, Any?>

class CustomDataTableView(
    private val project: Project,
    private val doRemoveSelectedRows: (queries: List<Pair<String, List<ValueWithType>>>) -> Unit,
    private val editValue: (query: Pair<String, List<ValueWithType>>) -> Unit,
    private val saveBinaryValue: (row: TableRow, column: SQLColumnDefinition, file: File) -> Unit,
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
    private var dateTimeFormat: SQLDateTimeFormat? = null
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

    fun updateModel(databaseName: String, table: SQLTableDefinition, dateTimeFormat: SQLDateTimeFormat) {
        this.databaseName = databaseName
        this.table = table
        this.dateTimeFormat = dateTimeFormat

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
                        type = if (it.startsWith("count")) SQLDataType.INTEGER else SQLDataType.TEXT,
                        autoIncrement = false,
                        nullable = true,
                        defaultValueExpression = null,
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
                TableViewColumnInfo(project, it, ::doEditValue, ::saveBinary, dateTimeFormat!!)
            }.toTypedArray(),
            listOf(emptyMap<String, Any?>()),
            0
        )
        internalModel = model

        setColumnModel(createDefaultColumnModel()) //Reset
        setModelAndUpdateColumns(model)

        if (tableConfiguration != null) {
            activeColumns.forEachIndexed { index, col ->
                tableConfiguration.columns.find { it.columnName == col.name }?.let { columnConfig ->
                    if (index < columnModel.columnCount) {
                        val column = columnModel.getColumn(index)
                        column.minWidth = 15
                        column.maxWidth = Integer.MAX_VALUE
                        column.preferredWidth = columnConfig.width
                    }
                }
            }
        }
    }

    override fun setColumnModel(columnModel: TableColumnModel) {
        this.columnModel?.removeColumnModelListener(columnObserver)
        super.setColumnModel(columnModel)
        columnModel.addColumnModelListener(columnObserver)
    }

    private fun saveBinary(column: SQLColumnDefinition) {
        val row = selectedRow
        if (row == -1) return

        val file = chooseSaveFile("Save binary data to", "") ?: return
        val item = internalModel.getItem(row)
        saveBinaryValue(item, column, file)
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

    fun doRemoveSelectedRows() {
        table?.let { currentTable ->
            val queriesToExecute = selectedRows.mapNotNull { row ->
                val data = internalModel.getRowValue(row) ?: return@mapNotNull null
                val variables = mutableListOf<ValueWithType>()
                val query = buildString {
                    append("DELETE FROM ${currentTable.name} ")
                    append(createMatch(data, currentTable, variables))
                }
                query to variables
            }
            doRemoveSelectedRows(queriesToExecute)
        }
    }

    private fun doEditValue(row: TableRow, column: SQLColumnDefinition, newValue: Any?) {
        val table = table ?: return

        val variables = mutableListOf<ValueWithType>()
        val query = buildString {
            append("UPDATE ${table.name} SET ")

            val name = column.name
            append(name)
            append("=? ")

            variables += makeVariable(column, newValue)

            append(createMatch(row, table, variables))
        }
        editValue(query to variables)
    }
}

private fun <R> IntArray.mapNotNull(transform: (Int) -> R?): List<R> {
    val list = mutableListOf<R>()
    forEach { transform(it)?.let { transformed -> list += transformed } }
    return list
}

private class TableViewColumnInfo(
    private val project: Project,
    val column: SQLColumnDefinition,
    private val editValue: (row: TableRow, column: SQLColumnDefinition, newValue: Any?) -> Unit,
    private val onSaveBinaryTapped: (SQLColumnDefinition) -> Unit,
    private val dateTimeFormat: SQLDateTimeFormat,
) : ColumnInfo<TableRow, Any>(column.name) {

    private val binaryRenderer = BinaryTableCellRenderer()
    private val datetimeRenderer = DateTimeTableCellRenderer()

    override fun valueOf(item: TableRow): Any? {
        val raw = item[column.name] ?: return null
        return when (column.type) {
            SQLDataType.TEXT -> raw.toString()
            SQLDataType.BLOB -> "<binary>"
            SQLDataType.REAL -> (raw as Number).toDouble().toString()
            SQLDataType.INTEGER -> (raw as Number).toLong().toString()
            SQLDataType.BOOLEAN -> if ((raw as Number).toInt() != 0) {
                Tr.TypeBooleanTrue.tr()
            } else {
                Tr.TypeBooleanFalse.tr()
            }
            SQLDataType.DATETIME -> applyDateTimeFix((raw as Number).toLong(), dateTimeFormat)
        }
    }

    private fun applyDateTimeFix(raw: Long, dateTimeFormat: SQLDateTimeFormat): Long {
        val inMilliseconds = (raw * dateTimeFormat.accuracyInMicroSeconds) / 1000
        return inMilliseconds - dateTimeFormat.timezoneOffsetMilliseconds
    }

    override fun isCellEditable(item: TableRow): Boolean {
        return column.name.lowercase(Locale.getDefault()) != "rowid"
    }

    override fun getEditor(item: TableRow): TableCellEditor? {
        return when (column.type) {
            SQLDataType.TEXT,
            SQLDataType.REAL,
            SQLDataType.INTEGER -> null
            SQLDataType.BLOB -> return BinaryCellEditor { onSaveBinaryTapped(column) }
            SQLDataType.BOOLEAN -> object : ComboBoxCellEditor() {
                override fun getComboBoxItems(): List<String> {
                    return listOf(Tr.TypeBooleanTrue.tr(), Tr.TypeBooleanFalse.tr())
                }
            }
            SQLDataType.DATETIME -> return DateTimeCellEditor(project) { it }
        }
    }

    override fun getRenderer(item: TableRow): TableCellRenderer? {
        return when (column.type) {
            SQLDataType.BLOB -> binaryRenderer
            SQLDataType.DATETIME -> datetimeRenderer
            else -> super.getRenderer(item)
        }
    }

    override fun setValue(item: TableRow, value: Any?) {
        try {
            if (!isSame(item[column.name], value)) {
                if (value == null && column.nullable) {
                    editValue(item, column, null)
                } else if (value != null) {
                    editValue(item, column, value)
                }
            }
        } catch (e: Throwable) {
            NotificationUtil.error("Update failed", "Failed to update: ${e.message}", project)
        }
    }

    private fun isSame(original: Any?, value: Any?): Boolean {
        if (original == null && value == null) return true
        else if (original != null && value == null) return false
        else if (original == null && (value as? String)?.isEmpty() == true) return true
        else if (original == null) return false
        if (value == null) return false

        return when (column.type) {
            SQLDataType.TEXT -> {
                val new = value.toString()
                (original == new)
            }
            SQLDataType.BLOB -> false
            SQLDataType.REAL -> {
                val new = if (value is Number) value.toDouble() else (value.toString().toDouble())
                (original == new)
            }
            SQLDataType.INTEGER -> {
                val new = if (value is Number) value.toLong() else (value.toString().toLong())
                (original == new)
            }
            SQLDataType.BOOLEAN -> {
                val new = if (value is Boolean) value else (value.toString() == Tr.TypeBooleanTrue.tr() || value.toString() == "true")
                (original == new)
            }
            SQLDataType.DATETIME -> {
                val new = if (value is Long) value else (value.toString().toLong())
                (original == new)
            }
        }
    }

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

private fun createMatch(
    data: TableRow,
    table: SQLTableDefinition,
    variables: MutableList<ValueWithType>
): String {
    val primaryKeys = table.primaryKey
    return buildString {
        if (primaryKeys.isNotEmpty()) {
            append("WHERE ")
            primaryKeys.forEachIndexed { index, column ->
                if (index > 0)
                    append(" AND ")
                val keyData = data[column]
                append(column)
                append(" ")
                append(equalsForKey(keyData, table, column, variables))
            }
        } else if (!table.extensions.contains(SQLTableExtension.WITHOUT_ROW_ID) && data.entries.any {
                it.key.equals(
                    "rowid",
                    ignoreCase = true
                )
            }) {
            append(
                "WHERE rowid = ${
                    data.entries.find {
                        it.key.equals(
                            "rowid",
                            ignoreCase = true
                        )
                    }?.value
                }"
            )
        } else {
            append("WHERE ")
            data.entries.forEachIndexed { index, (column, data) ->
                if (index > 0)
                    append(" AND ")
                append(column)
                append(equalsForKey(data, table, column, variables))
            }
        }
    }
}

private fun equalsForKey(
    keyData: Any?,
    table: SQLTableDefinition,
    column: String,
    variables: MutableList<ValueWithType>
): String {
    if (keyData == null) return " IS NULL"
    val tableColumn = table.columns.find { it.name == column }
        ?: throw IllegalStateException("Could create statement for column, column not found")

    variables += makeVariable(tableColumn, rawValue = keyData)

    return "=?"
}

@Suppress("UNCHECKED_CAST")
private fun makeVariable(
    column: SQLColumnDefinition,
    rawValue: Any? = null
): ValueWithType {
    if (rawValue == null) {
        return ValueWithType(StorageType.int, null)
    }

    return when (column.type) {
        SQLDataType.TEXT -> ValueWithType(StorageType.string, rawValue.toString())
        SQLDataType.BLOB -> ValueWithType(StorageType.binary, rawValue as? ByteArray ?: asByteArray(rawValue as List<Int>))
        SQLDataType.REAL -> ValueWithType(StorageType.double, (rawValue as? Number)?.toDouble() ?: rawValue.toString().toDouble())
        SQLDataType.INTEGER -> ValueWithType(StorageType.int, (rawValue as? Number)?.toLong() ?: rawValue.toString().toLong())
        SQLDataType.BOOLEAN -> ValueWithType(StorageType.bool, (rawValue as? Boolean) ?: (rawValue.toString() == "true" || rawValue.toString() == Tr.TypeBooleanTrue.tr()))
        SQLDataType.DATETIME -> ValueWithType(StorageType.datetime, (rawValue as? Number)?.toLong() ?: rawValue.toString().toLong())
    }
}

fun asByteArray(ints: List<Int>): ByteArray {
    val bytes = ByteArray(ints.size)
    ints.forEachIndexed { index, i -> bytes[index] = (i and 0xFF).toByte() }
    return bytes
}
