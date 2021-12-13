package com.chimerapps.storageinspector.ui.ide.view.key_value

import com.chimerapps.storageinspector.api.protocol.model.StorageType
import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueServerValue
import com.chimerapps.storageinspector.ui.util.list.DiffUtilDispatchModel
import com.chimerapps.storageinspector.ui.util.list.TableModelDiffUtilDispatchModel
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.intellij.ui.table.TableView
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ComboBoxCellEditor
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.ListSelectionModel
import javax.swing.table.TableCellEditor


/**
 * @author Nicola Verbeeck
 */
class KeyValueTableView(
    private val removeKeys: (List<ValueWithType>) -> Unit,
    private val editValue: (key: ValueWithType, newValue: ValueWithType) -> Unit,
) : TableView<KeyValueServerValue>() {

    private val internalModel: ListTableModel<KeyValueServerValue>

    val dispatchModel: DiffUtilDispatchModel<KeyValueServerValue>

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

        internalModel = ListTableModel(
            arrayOf(
                TableViewColumnInfo(Tr.KeyValueKey.tr(), KeyValueServerValue::key, editable = false),
                TableViewColumnInfo(Tr.KeyValueValue.tr(), KeyValueServerValue::value, editable = true, onEdited = ::onValueEdited),
            ),
            listOf(),
            0
        )
        model = internalModel

        dispatchModel = TableModelDiffUtilDispatchModel(internalModel)
    }

    private fun onValueEdited(keyValueServerValue: KeyValueServerValue, newValue: String) {
        val converted = when (keyValueServerValue.value.type) {
            StorageType.string -> newValue
            StorageType.int -> newValue.toLong()
            StorageType.double -> newValue.toDouble()
            StorageType.bool -> (newValue.lowercase() == "true" || newValue.lowercase() == Tr.TypeBooleanTrue.tr().lowercase())
            StorageType.datetime -> TODO()
            StorageType.binary -> TODO()
            StorageType.stringlist -> TODO()
        }
        editValue(keyValueServerValue.key, keyValueServerValue.value.copy(value = converted))
    }

    fun doRemoveSelectedRows() {
        removeKeys(selectedRows.map { index -> internalModel.getItem(index).key })
    }
}

private class TableViewColumnInfo(
    name: String,
    private val selector: (KeyValueServerValue) -> ValueWithType,
    private val editable: Boolean,
    private val onEdited: (KeyValueServerValue, stringValue: String) -> Unit = { _, _ -> },
) : ColumnInfo<KeyValueServerValue, String>(name) {

    override fun valueOf(item: KeyValueServerValue): String {
        return selector(item).asString
    }

    override fun isCellEditable(item: KeyValueServerValue?): Boolean = editable

    override fun getEditor(item: KeyValueServerValue): TableCellEditor? {
        return when (selector(item).type) {
            StorageType.bool -> return object : ComboBoxCellEditor() {
                override fun getComboBoxItems(): List<String> {
                    return listOf(Tr.TypeBooleanTrue.tr(), Tr.TypeBooleanFalse.tr())
                }
            }
            else -> null
        }
    }

    override fun setValue(item: KeyValueServerValue, value: String) {
        onEdited(item, value)
    }
}