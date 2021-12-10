package com.chimerapps.storageinspector.ui.ide.view.key_value

import com.chimerapps.storageinspector.api.protocol.model.StorageType
import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueServerValue
import com.chimerapps.storageinspector.ui.util.list.DiffUtilDispatchModel
import com.chimerapps.storageinspector.ui.util.list.TableModelDiffUtilDispatchModel
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.intellij.ui.table.TableView
import com.intellij.util.PlatformIcons
import com.intellij.util.text.DefaultJBDateTimeFormatter
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
                TableViewColumnInfo(Tr.KeyValueValue.tr(), KeyValueServerValue::value, editable = true),
            ),
            listOf(),
            0
        )
        model = internalModel

        dispatchModel = TableModelDiffUtilDispatchModel(internalModel)
    }

    fun doRemoveSelectedRows() {
        removeKeys(selectedRows.map { index -> internalModel.getItem(index).key })
    }
}

private class TableViewColumnInfo(
    name: String,
    private val selector: (KeyValueServerValue) -> ValueWithType,
    private val editable: Boolean,
) : ColumnInfo<KeyValueServerValue, String>(name) {

    override fun valueOf(item: KeyValueServerValue): String {
        return when (selector(item).type) {
            StorageType.string,
            StorageType.double,
            StorageType.integer -> selector(item).value.toString()
            StorageType.datetime -> DefaultJBDateTimeFormatter().formatDateTime(selector(item).value as Long)
            StorageType.binary -> Tr.TypeBinary.tr()
            StorageType.boolean -> if (selector(item).value as Boolean) Tr.TypeBooleanTrue.tr() else Tr.TypeBooleanFalse.tr()
            StorageType.stringList -> (selector(item).value as List<*>).joinToString(", ")
        }
    }

    override fun isCellEditable(item: KeyValueServerValue?): Boolean = editable

    override fun getEditor(item: KeyValueServerValue): TableCellEditor? {
        return when (selector(item).type) {
            StorageType.boolean -> return object : ComboBoxCellEditor() {
                override fun getComboBoxItems(): List<String> {
                    return listOf(Tr.TypeBooleanTrue.tr(), Tr.TypeBooleanFalse.tr())
                }
            }
            else -> null
        }
    }
}