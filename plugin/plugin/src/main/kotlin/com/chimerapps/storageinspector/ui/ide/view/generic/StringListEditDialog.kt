package com.chimerapps.storageinspector.ui.ide.view.generic

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import javax.swing.JComponent

/**
 * @author Nicola Verbeeck
 */
class StringListEditDialog(
    initialData: List<String>,
    title: String,
    private val project: Project,
) : DialogWrapper(project, true, IdeModalityType.PROJECT) {

    private lateinit var table: TableView<String>
    val results: List<String>
        get() = model.items

    private val model = ListTableModel<String>()
    private val columnInfo = StringColumnInfo("")

    init {
        model.columnInfos = arrayOf(columnInfo)
        model.addRows(initialData)

        init()
        setTitle(title)
    }

    override fun createCenterPanel(): JComponent {
        val decorator = ToolbarDecorator.createDecorator(TableView<String>().also { table ->
            columnInfo.table = table
            this.table = table
            table.model = model
        })
        decorator.setAddAction {
            val result = StringInputDialog.show(project, "Add value", "", allowEmpty = true)
            if (result != null) {
                model.addRow(result)
            }
        }
        decorator.setRemoveAction {
            table.selectedRows.sortedDescending().forEach { index ->
                model.removeRow(index)
            }
        }

        return decorator.createPanel().also {
            table.tableHeader.isVisible = false
        }
    }

}

private class StringColumnInfo(name: String) : ColumnInfo<String, String>(name) {

    lateinit var table: TableView<String>

    override fun valueOf(obj: String): String = obj

    override fun isCellEditable(o: String?): Boolean = true

    override fun setValue(item: String, value: String) {
        table.listTableModel.replaceRow(table.editingRow, value)
    }

}

private fun <Item> ListTableModel<Item>.replaceRow(row: Int, value: Item) {
    insertRow(row, value)
    removeRow(row + 1)
}
