package com.chimerapps.storageinspector.ui.ide.view.generic

import com.chimerapps.storageinspector.api.protocol.model.StorageType
import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.ui.util.file.chooseOpenFile
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.chimerapps.storageinspector.ui.util.notification.NotificationUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.FixedSizeButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.Toolkit
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

/**
 * @author Nicola Verbeeck
 */
class TypedValueEntryView(private val project: Project, private var storageType: StorageType) : JPanel(BorderLayout()) {

    private val booleanSelector = ComboBox(arrayOf(Tr.TypeBooleanTrue.tr(), Tr.TypeBooleanFalse.tr()))
    private val freeEditField = JBTextField()
    private val valueButton = FixedSizeButton()

    init {
        add(freeEditField, BorderLayout.CENTER)
        updateType(storageType)
    }

    fun updateType(type: StorageType) {
        storageType = type
        when (type) {
            StorageType.string -> ensureFreeEditField().also {
                (it.document as AbstractDocument).documentFilter = null
                removeButton()
            }
            StorageType.int -> ensureFreeEditField().also {
                (it.document as AbstractDocument).documentFilter = IntegerOnlyDocumentFilter()
                removeButton()
            }
            StorageType.double -> ensureFreeEditField().also {
                (it.document as AbstractDocument).documentFilter = DoubleOnlyDocumentFilter()
                removeButton()
            }
            StorageType.datetime -> ensureFreeEditField().also {
                (it.document as AbstractDocument).documentFilter = null
                it.isEnabled = false
                addButton().also { btn ->
                    btn.icon = AllIcons.Vcs.History
                    NotificationUtil.info("Under construction", "This feature is under construction", project)
                }
            }
            StorageType.binary -> ensureFreeEditField().also {
                //TODO implement
                (it.document as AbstractDocument).documentFilter = null
                it.isEnabled = false
                addButton().also { btn ->
                    btn.icon = AllIcons.FileTypes.Text
                    btn.addActionListener {
                        val file = chooseOpenFile("Pick file")
                        NotificationUtil.info("Under construction", "This feature is under construction", project)
                    }
                }
            }
            StorageType.bool -> {
                ensureBooleanSelector()
                removeButton()
            }
            StorageType.stringlist -> ensureFreeEditField().also {
                //TODO implement
                (it.document as AbstractDocument).documentFilter = null
                it.isEnabled = false
                addButton().also { btn ->
                    btn.icon = AllIcons.Json.Array
                    btn.addActionListener {
                        NotificationUtil.info("Under construction", "This feature is under construction", project)
                    }
                }
            }
        }
    }

    private fun ensureFreeEditField(): JBTextField {
        if (freeEditField.parent != null) return freeEditField
        remove(booleanSelector)
        add(freeEditField, BorderLayout.CENTER)
        revalidate()
        return freeEditField
    }

    private fun ensureBooleanSelector() {
        if (booleanSelector.parent != null) return
        remove(freeEditField)
        add(booleanSelector, BorderLayout.CENTER)
        revalidate()
    }

    private fun removeButton() {
        if (valueButton.parent != null) {
            remove(valueButton)
            revalidate()
        }
    }

    private fun addButton(): JButton {
        if (valueButton.parent == null) {
            add(valueButton, BorderLayout.EAST)
            revalidate()
        }
        valueButton.actionListeners.toList().forEach(valueButton::removeActionListener)
        return valueButton
    }

    fun doValidate(allowEmpty: (StorageType) -> Boolean): ValueResult {
        return when (storageType) {
            StorageType.string,
            StorageType.int,
            StorageType.double -> doValidateFromString(storageType, freeEditField.text, freeEditField, allowEmpty)
            StorageType.datetime -> TODO()
            StorageType.binary -> TODO()
            StorageType.bool -> doValidateFromString(storageType, if (booleanSelector.selectedIndex == 0) "true" else "false", freeEditField, allowEmpty)
            StorageType.stringlist -> TODO()
        }

    }

    companion object {
        fun doValidateFromString(storageType: StorageType, text: String, component: JComponent, allowEmpty: (StorageType) -> Boolean): ValueResult {
            when (storageType) {
                StorageType.string -> {
                    if (text.isEmpty() && !allowEmpty(storageType)) {
                        return ValueResult(error = ValidationInfo("Empty value is not allowed", component))
                    }
                    return ValueResult(ValueWithType(storageType, text))
                }
                StorageType.int -> {
                    if (text.isEmpty() && !allowEmpty(storageType)) {
                        return ValueResult(error = ValidationInfo("Empty value is not allowed", component))
                    }
                    val value = text.toIntOrNull() ?: return ValueResult(error = ValidationInfo("Value is not integer", component))
                    return ValueResult(ValueWithType(storageType, value))
                }
                StorageType.double -> {
                    if (text.isEmpty() && !allowEmpty(storageType)) {
                        return ValueResult(error = ValidationInfo("Empty value is not allowed", component))
                    }
                    val value = text.toDoubleOrNull() ?: return ValueResult(error = ValidationInfo("Value is not double", component))
                    return ValueResult(ValueWithType(storageType, value))
                }
                StorageType.datetime -> TODO()
                StorageType.binary -> TODO()
                StorageType.bool -> {
                    return ValueResult(ValueWithType(storageType, text.equals(Tr.TypeBooleanTrue.tr(), ignoreCase = true) || text.equals("true", ignoreCase = true)))
                }
                StorageType.stringlist -> TODO()
            }
        }
    }
}

data class ValueResult(
    val rawValue: ValueWithType? = null,
    val error: ValidationInfo? = null,
)

private class IntegerOnlyDocumentFilter : DocumentFilter() {

    override fun replace(fb: FilterBypass, offset: Int, length: Int, text: String, attrs: AttributeSet?) {
        val currentText = fb.document.getText(0, offset) + text + fb.document.getText(offset, fb.document.length - offset)
        if (currentText.toIntOrNull() == null) {
            Toolkit.getDefaultToolkit().beep()
        } else {
            super.replace(fb, offset, length, text, attrs)
        }

    }

    override fun insertString(fb: FilterBypass, offset: Int, text: String, attr: AttributeSet?) {
        val currentText = fb.document.getText(0, offset) + text + fb.document.getText(offset, fb.document.length - offset)
        if (currentText.toIntOrNull() == null) {
            Toolkit.getDefaultToolkit().beep()
        } else {
            super.insertString(fb, offset, text, attr)
        }
    }
}

private class DoubleOnlyDocumentFilter : DocumentFilter() {

    override fun replace(fb: FilterBypass, offset: Int, length: Int, text: String, attrs: AttributeSet?) {
        val currentText = fb.document.getText(0, offset) + text + fb.document.getText(offset, fb.document.length - offset)
        if (currentText.toDoubleOrNull() == null) {
            Toolkit.getDefaultToolkit().beep()
        } else {
            super.replace(fb, offset, length, text, attrs)
        }

    }

    override fun insertString(fb: FilterBypass, offset: Int, text: String, attr: AttributeSet?) {
        val currentText = fb.document.getText(0, offset) + text + fb.document.getText(offset, fb.document.length - offset)
        if (currentText.toDoubleOrNull() == null) {
            Toolkit.getDefaultToolkit().beep()
        } else {
            super.insertString(fb, offset, text, attr)
        }
    }
}