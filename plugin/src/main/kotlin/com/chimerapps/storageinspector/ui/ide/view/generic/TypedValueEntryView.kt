package com.chimerapps.storageinspector.ui.ide.view.generic

import com.chimerapps.storageinspector.api.protocol.model.StorageType
import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.ui.util.file.chooseOpenFile
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.FixedSizeButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.Toolkit
import java.io.InputStream
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

/**
 * @author Nicola Verbeeck
 */
class TypedValueEntryView(
    private val project: Project,
    private var storageType: StorageType,
) : JPanel(BorderLayout()) {

    private val booleanSelector = ComboBox(arrayOf(Tr.TypeBooleanTrue.tr(), Tr.TypeBooleanFalse.tr()))
    private val freeEditField = JBTextField()
    private val valueButton = FixedSizeButton()
    private var stringListItems: List<String>? = null
    private var binaryFile: VirtualFile? = null
    private var dateTime: Date? = null

    init {
        add(freeEditField, BorderLayout.CENTER)
        updateType(storageType)
    }

    fun updateType(type: StorageType) {
        storageType = type
        when (type) {
            StorageType.string -> ensureFreeEditField().also {
                (it.document as AbstractDocument).documentFilter = null
                stringListItems = null
                dateTime = null
                binaryFile = null
                removeButton()
            }
            StorageType.int -> ensureFreeEditField().also {
                (it.document as AbstractDocument).documentFilter = IntegerOnlyDocumentFilter()
                stringListItems = null
                binaryFile = null
                dateTime = null
                removeButton()
            }
            StorageType.double -> ensureFreeEditField().also {
                (it.document as AbstractDocument).documentFilter = DoubleOnlyDocumentFilter()
                stringListItems = null
                binaryFile = null
                dateTime = null
                removeButton()
            }
            StorageType.datetime -> ensureFreeEditField().also { field ->
                (field.document as AbstractDocument).documentFilter = null
                stringListItems = null
                field.isEnabled = false
                addButton().also { btn ->
                    btn.icon = AllIcons.Vcs.History
                    btn.addActionListener {
                        DateTimeEditDialog(project, dateTime ?: Date()).showAndReturn()?.let {
                            dateTime = it
                            val dateTime = LocalDateTime.ofEpochSecond(it.time / 1000, (it.time % 1000 * 1000).toInt(), ZoneOffset.UTC)
                            field.text = DateTimeFormatter.RFC_1123_DATE_TIME.format(dateTime.atOffset(ZoneOffset.UTC))
                        }
                    }
                }
            }
            StorageType.binary -> ensureFreeEditField().also { textField ->
                (textField.document as AbstractDocument).documentFilter = null
                stringListItems = null
                dateTime = null
                textField.isEnabled = false
                addButton().also { btn ->
                    btn.icon = AllIcons.FileTypes.Text
                    btn.addActionListener {
                        val file = chooseOpenFile("Pick file")
                        binaryFile = file
                        if (file == null) {
                            textField.text = ""
                        } else {
                            textField.text = "<${file.name} (${file.length} bytes)>"
                        }
                    }
                }
            }
            StorageType.bool -> {
                stringListItems = null
                binaryFile = null
                dateTime = null
                ensureBooleanSelector()
                removeButton()
            }
            StorageType.stringlist -> ensureFreeEditField().also { textField ->
                (textField.document as AbstractDocument).documentFilter = null
                textField.isEnabled = false
                binaryFile = null
                dateTime = null
                addButton().also { btn ->
                    btn.icon = AllIcons.Json.Array
                    btn.addActionListener {
                        StringListEditDialog(stringListItems ?: emptyList(), "Edit string list", project).also { dialog ->
                            if (dialog.showAndGet()) {
                                stringListItems = dialog.results
                                textField.text = stringListItems?.joinToString(",")
                            }
                        }
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
            StorageType.datetime -> doValidateDateTime()
            StorageType.binary -> doValidateBinary()
            StorageType.bool -> doValidateFromString(storageType, if (booleanSelector.selectedIndex == 0) "true" else "false", freeEditField, allowEmpty)
            StorageType.stringlist -> ValueResult(ValueWithType(storageType, stringListItems ?: emptyList<String>()))
        }

    }

    private fun doValidateBinary(): ValueResult {
        val file = binaryFile ?: return ValueResult(error = ValidationInfo("No binary data provided", freeEditField))
        val bytes = ApplicationManager.getApplication().runReadAction<ByteArray> { file.inputStream.use(InputStream::readAllBytes) }
        return ValueResult(rawValue = ValueWithType(StorageType.binary, bytes))
    }

    private fun doValidateDateTime(): ValueResult {
        val date = dateTime ?: return ValueResult(error = ValidationInfo("No datetime provided", freeEditField))
        return ValueResult(rawValue = ValueWithType(StorageType.datetime, date.time))
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
                StorageType.datetime,
                StorageType.binary,
                StorageType.stringlist -> throw IllegalArgumentException("This storage type is not editable as string")
                StorageType.bool -> {
                    return ValueResult(ValueWithType(storageType, text.equals(Tr.TypeBooleanTrue.tr(), ignoreCase = true) || text.equals("true", ignoreCase = true)))
                }
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