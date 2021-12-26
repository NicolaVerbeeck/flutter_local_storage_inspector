package com.chimerapps.storageinspector.ui.ide.view.key_value

import com.chimerapps.storageinspector.ui.ide.view.generic.StringListEditDialog
import com.chimerapps.storageinspector.ui.util.dispatchMain
import com.chimerapps.storageinspector.ui.util.file.chooseOpenFile
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.FixedSizeButton
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.event.MouseEvent
import java.util.EventObject
import javax.swing.AbstractCellEditor
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.UIManager
import javax.swing.border.Border
import javax.swing.border.EmptyBorder
import javax.swing.plaf.UIResource
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer

/**
 * @author Nicola Verbeeck
 */
class CursiveTableCellRenderer : DefaultTableCellRenderer() {

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

class BinaryCellEditor : AbstractCellEditor(), TableCellEditor {

    private var file: VirtualFile? = null

    override fun getCellEditorValue(): Any? {
        return file
    }

    override fun getTableCellEditorComponent(table: JTable?, value: Any?, isSelected: Boolean, row: Int, column: Int): Component {
        dispatchMain {
            file = chooseOpenFile("Pick file")
            fireEditingStopped()
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

class BinaryTableCellRenderer(private val onSaveTapped: () -> Unit) : TableCellRenderer, JPanel(BorderLayout()) {

    private val safeNoFocusBorder: Border = EmptyBorder(1, 1, 1, 1)
    private val defaultNoFocusBorder: Border = EmptyBorder(1, 1, 1, 1)

    private var noFocusBorder: Border? = defaultNoFocusBorder

    private var unselectedForeground: Color? = null
    private var unselectedBackground: Color? = null

    private val label = JBLabel()
    private val actionButton = FixedSizeButton()

    init {
        isOpaque = true
        border = getInitNoFocusBorder()
        name = "Table.cellRenderer"

        add(label, BorderLayout.CENTER)
        add(actionButton, BorderLayout.EAST)

        actionButton.addActionListener { onSaveTapped() }

        actionButton.icon = AllIcons.Actions.MenuSaveall
    }

    private fun getInitNoFocusBorder(): Border? {

        val border = UIManager.get("Table.cellNoFocusBorder", locale) as? Border
        if (System.getSecurityManager() != null) {
            return border ?: safeNoFocusBorder
        } else if (border != null) {
            if (noFocusBorder == null || noFocusBorder === defaultNoFocusBorder) {
                return border
            }
        }
        return noFocusBorder
    }

    override fun setForeground(c: Color?) {
        super.setForeground(c)
        unselectedForeground = c
    }

    override fun setBackground(c: Color?) {
        super.setBackground(c)
        unselectedBackground = c
    }

    override fun updateUI() {
        super.updateUI()
        foreground = null
        background = null
    }

    override fun getTableCellRendererComponent(
        table: JTable?, value: Any?,
        isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
    ): Component {
        var overrideSelected = isSelected
        if (table == null) {
            return this
        }
        var fg: Color? = null
        var bg: Color? = null
        val dropLocation = table.dropLocation
        if (dropLocation != null && !dropLocation.isInsertRow
            && !dropLocation.isInsertColumn
            && dropLocation.row == row && dropLocation.column == column
        ) {
            fg = UIManager.get("Table.dropCellForeground", locale) as? Color
            bg = UIManager.get("Table.dropCellBackground", locale) as? Color
            overrideSelected = true
        }
        if (overrideSelected) {
            super.setForeground(fg ?: table.selectionForeground)
            super.setBackground(bg ?: table.selectionBackground)
        } else {
            var background = if (unselectedBackground != null) unselectedBackground else table.background
            if (background == null || background is UIResource) {
                val alternateColor = UIManager.get("Table.alternateRowColor", locale) as? Color
                if (alternateColor != null && row % 2 != 0) {
                    background = alternateColor
                }
            }
            super.setForeground(if (unselectedForeground != null) unselectedForeground else table.foreground)
            super.setBackground(background)
        }
        label.font = table.font
        if (hasFocus) {
            var border: Border? = null
            if (overrideSelected) {
                border = UIManager.get("Table.focusSelectedCellHighlightBorder", locale) as? Border
            }
            if (border == null) {
                border = UIManager.get("Table.focusCellHighlightBorder", locale) as? Border
            }
            setBorder(border)
            if (!overrideSelected && table.isCellEditable(row, column)) {
                var col: Color? = UIManager.get("Table.focusCellForeground", locale) as? Color
                if (col != null) {
                    super.setForeground(col)
                }
                col = UIManager.get("Table.focusCellBackground", locale) as? Color
                if (col != null) {
                    super.setBackground(col)
                }
            }
        } else {
            border = getInitNoFocusBorder()
        }
        label.text = value?.toString() ?: ""
        return this
    }

    /*
     * The following methods are overridden as a performance measure to
     * to prune code-paths are often called in the case of renders
     * but which we know are unnecessary.  Great care should be taken
     * when writing your own renderer to weigh the benefits and
     * drawbacks of overriding methods like these.
     */
    override fun isOpaque(): Boolean {
        val back: Color? = background
        var p: Component? = parent
        if (p != null) {
            p = p.parent
        }

        // p should now be the JTable.
        val colorMatch = back != null && p != null && back == p.background &&
                p.isOpaque
        return !colorMatch && super.isOpaque()
    }

}