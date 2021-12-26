package com.chimerapps.storageinspector.ui.ide.view.key_value

import com.chimerapps.storageinspector.api.protocol.model.StorageType
import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueServerValue
import com.chimerapps.storageinspector.ui.ide.settings.KeyValueTableConfiguration
import com.chimerapps.storageinspector.ui.ide.settings.StorageInspectorProjectSettings
import com.chimerapps.storageinspector.ui.util.file.chooseSaveFile
import com.chimerapps.storageinspector.ui.util.list.DiffUtilDispatchModel
import com.chimerapps.storageinspector.ui.util.list.TableModelDiffUtilDispatchModel
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.table.TableView
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ComboBoxCellEditor
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.InputStream
import javax.swing.ListSelectionModel
import javax.swing.event.ChangeEvent
import javax.swing.event.ListSelectionEvent
import javax.swing.event.TableColumnModelEvent
import javax.swing.event.TableColumnModelListener
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumnModel


/**
 * @author Nicola Verbeeck
 */
class KeyValueTableView(
    private val project: Project,
    private val removeKeys: (List<ValueWithType>) -> Unit,
    private val editValue: (key: ValueWithType, newValue: ValueWithType) -> Unit,
    private val saveBinaryValue: (key: ValueWithType, toFile: File) -> Unit,
) : TableView<KeyValueServerValue>() {

    private val internalModel: ListTableModel<KeyValueServerValue>

    val dispatchModel: DiffUtilDispatchModel<KeyValueServerValue>
    private var isResizingColumns: Boolean = false
    private val columnObserver = object : TableColumnModelListener {
        override fun columnAdded(e: TableColumnModelEvent?) {}

        override fun columnRemoved(e: TableColumnModelEvent?) {}

        override fun columnMoved(e: TableColumnModelEvent?) {}

        override fun columnMarginChanged(e: ChangeEvent) {
            isResizingColumns = true
        }

        override fun columnSelectionChanged(e: ListSelectionEvent?) {}
    }

    init {
        tableHeader.reorderingAllowed = false
        rowHeight = PlatformIcons.CLASS_ICON.iconHeight * 2
        preferredScrollableViewportSize = JBUI.size(-1, 150)

        tableHeader.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent?) {
                super.mouseReleased(e)
                commitResize()
            }
        })

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                super.keyPressed(e)
                if (e.keyCode == KeyEvent.VK_DELETE || e.keyCode == KeyEvent.VK_BACK_SPACE) {
                    doRemoveSelectedRows()
                }
            }
        })

        internalModel = ListTableModel(
            arrayOf(
                TableViewColumnInfo(project, Tr.KeyValueKey.tr(), KeyValueServerValue::key, editable = false, onSaveBinaryTapped = ::saveBinary),
                TableViewColumnInfo(project, Tr.KeyValueValue.tr(), KeyValueServerValue::value, editable = true, onEdited = ::onValueEdited, onSaveBinaryTapped = ::saveBinary),
            ),
            listOf(),
            0
        )
        model = internalModel
        columnModel.addColumnModelListener(columnObserver)

        StorageInspectorProjectSettings.instance(project).state.configuration?.keyValueTableConfiguration?.let { configuration ->
            if (configuration.keyWidth >= 0) {
                setColumnPreferredSize(0, configuration.keyWidth)
            }
            if (configuration.valueWidth >= 0) {
                setColumnPreferredSize(1, configuration.valueWidth)
            }
        }

        dispatchModel = TableModelDiffUtilDispatchModel(internalModel)
    }


    private fun setColumnPreferredSize(index: Int, width: Int) {
        val column = columnModel.getColumn(index)
        column.minWidth = 15
        column.maxWidth = Integer.MAX_VALUE
        column.preferredWidth = width
    }

    private fun onValueEdited(keyValueServerValue: KeyValueServerValue, newValue: Any?) {
        if (newValue == null) return

        val converted = when (keyValueServerValue.value.type) {
            StorageType.string -> newValue as String
            StorageType.int -> if (newValue is String) newValue.toLong() else newValue as Long
            StorageType.double -> if (newValue is String) newValue.toDouble() else newValue as Double
            StorageType.bool -> if (newValue is String) (newValue.lowercase() == "true" || newValue.lowercase() == Tr.TypeBooleanTrue.tr().lowercase()) else newValue as Boolean
            StorageType.datetime -> TODO()
            StorageType.binary -> {
                newValue as VirtualFile
                lateinit var bytes: ByteArray
                ApplicationManager.getApplication().runReadAction {
                    bytes = newValue.inputStream.use(InputStream::readAllBytes)
                }
                bytes
            }
            StorageType.stringlist -> newValue
        }
        editValue(keyValueServerValue.key, keyValueServerValue.value.copy(value = converted))
    }

    fun doRemoveSelectedRows() {
        removeKeys(selectedRows.map { index -> internalModel.getItem(index).key })
    }

    private fun commitResize() {
        if (!isResizingColumns) return
        isResizingColumns = false

        StorageInspectorProjectSettings.instance(project).updateState {
            copy(
                configuration = updateConfiguration {
                    copy(
                        keyValueTableConfiguration = KeyValueTableConfiguration(
                            keyWidth = columnModel.getColumn(0).width,
                            valueWidth = columnModel.getColumn(1).width,
                        ),
                    )
                },
            )
        }
    }

    override fun setColumnModel(columnModel: TableColumnModel) {
        this.columnModel?.removeColumnModelListener(columnObserver)
        super.setColumnModel(columnModel)
        columnModel.addColumnModelListener(columnObserver)
    }

    private fun saveBinary() {
        val row = selectedRow
        if (row == -1) return

        val file = chooseSaveFile("Save binary data to","") ?: return
        //TODO Support binary keys
        val item = internalModel.getItem(row)
        saveBinaryValue(item.key, file)
    }
}

private class TableViewColumnInfo(
    private val project: Project,
    name: String,
    private val selector: (KeyValueServerValue) -> ValueWithType,
    private val editable: Boolean,
    private val onEdited: (KeyValueServerValue, stringValue: Any?) -> Unit = { _, _ -> },
    private val onSaveBinaryTapped: () -> Unit,
) : ColumnInfo<KeyValueServerValue, Any>(name) {

    private val stringListRenderer = StringListTableCellRenderer()
    private val binaryRenderer = BinaryTableCellRenderer()

    override fun valueOf(item: KeyValueServerValue): Any {
        val selected = selector(item)
        return when (selected.type) {
            StorageType.binary -> "binary"
            else -> selected.value!!
        }
    }

    override fun isCellEditable(item: KeyValueServerValue?): Boolean = editable

    override fun getEditor(item: KeyValueServerValue): TableCellEditor? {
        return when (selector(item).type) {
            StorageType.bool -> return object : ComboBoxCellEditor() {
                override fun getComboBoxItems(): List<String> {
                    return listOf(Tr.TypeBooleanTrue.tr(), Tr.TypeBooleanFalse.tr())
                }
            }
            StorageType.stringlist -> return StringListCellEditor(project)
            StorageType.binary -> return BinaryCellEditor(onSaveBinaryTapped)
            else -> null
        }
    }

    override fun setValue(item: KeyValueServerValue, value: Any?) {
        onEdited(item, value)
    }

    override fun getRenderer(item: KeyValueServerValue): TableCellRenderer? {
        return when (selector(item).type) {
            StorageType.stringlist -> stringListRenderer
            StorageType.binary -> binaryRenderer
            else -> super.getRenderer(item)
        }
    }
}

