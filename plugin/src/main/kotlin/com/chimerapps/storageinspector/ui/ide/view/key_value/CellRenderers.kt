package com.chimerapps.storageinspector.ui.ide.view.key_value

import com.chimerapps.storageinspector.ui.ide.view.generic.StringListEditDialog
import com.chimerapps.storageinspector.ui.util.dispatchMain
import com.chimerapps.storageinspector.ui.util.file.chooseOpenFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Component
import java.awt.Font
import java.awt.event.MouseEvent
import java.util.EventObject
import javax.swing.AbstractCellEditor
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor

/**
 * @author Nicola Verbeeck
 */
class StringListTableCellRenderer : DefaultTableCellRenderer() {

    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
        val default = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        default.font = Font(default.font.fontName, Font.ITALIC, default.font.size)
        (default as JLabel).text = (value as? List<*>)?.joinToString() ?: ""
        return default
    }
}

class StringListCellEditor(private val project: Project) : AbstractCellEditor(), TableCellEditor {

    private var currentList: List<String>? = null

    override fun getCellEditorValue(): Any? {
        return currentList
    }

    override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean, row: Int, column: Int): Component {
        @Suppress("UNCHECKED_CAST")
        currentList = value as? List<String>
        dispatchMain {
            StringListEditDialog(currentList ?: emptyList(), "Edit string list", project).also { dialog ->
                if (dialog.showAndGet()) {
                    currentList = dialog.results
                }
                fireEditingStopped()
            }
        }
        return JLabel()
    }

    override fun isCellEditable(anEvent: EventObject?): Boolean {
        return if (anEvent is MouseEvent) {
            anEvent.clickCount >= 2
        } else {
            super.isCellEditable(anEvent)
        }
    }

}

class BinaryCellEditor(private val onSaveBinaryTapped: () -> Unit) : AbstractCellEditor(), TableCellEditor {

    private var file: VirtualFile? = null

    override fun getCellEditorValue(): Any? {
        return file
    }

    override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean, row: Int, column: Int): Component {
        val shownAt = System.currentTimeMillis()
        return JPanel().also { panel ->
            panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
            panel.add(Box.createHorizontalGlue())
            panel.add(JButton("Download").also { btn ->
                btn.addActionListener {
                    //Prevent click-through
                    if ((System.currentTimeMillis() - shownAt) < 500L) return@addActionListener

                    file = null
                    onSaveBinaryTapped()
                    fireEditingStopped()
                }
            })
            panel.add(Box.createHorizontalGlue())
            panel.add(JButton("Upload").also { btn ->
                btn.addActionListener {
                    //Prevent click-through
                    if ((System.currentTimeMillis() - shownAt) < 500L) return@addActionListener

                    file = chooseOpenFile("Pick file")
                    fireEditingStopped()
                }
            })
            panel.add(Box.createHorizontalGlue())
        }
    }

    override fun isCellEditable(anEvent: EventObject?): Boolean {
        return if (anEvent is MouseEvent) {
            anEvent.clickCount >= 2
        } else {
            super.isCellEditable(anEvent)
        }
    }

}

class BinaryTableCellRenderer : DefaultTableCellRenderer() {

    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
        val default = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        default.font = Font(default.font.fontName, Font.ITALIC, default.font.size)
        (default as JLabel).text = "<binary>"
        return default
    }
}
