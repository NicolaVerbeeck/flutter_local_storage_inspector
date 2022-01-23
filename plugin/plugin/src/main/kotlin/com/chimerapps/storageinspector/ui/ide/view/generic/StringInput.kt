package com.chimerapps.storageinspector.ui.ide.view.generic

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import javax.swing.JComponent

/**
 * @author Nicola Verbeeck
 */
class StringInputDialog(
    project: Project,
    private val titleLabel: String,
    initialText: String,
    private val allowEmpty: Boolean,
) : DialogWrapper(project, false, IdeModalityType.PROJECT) {

    companion object {
        fun show(project: Project, title: String, initialText: String, allowEmpty: Boolean): String? {
            val dialog = StringInputDialog(project, title, initialText, allowEmpty)
            dialog.title = title
            if (dialog.showAndGet()) return dialog.result
            return null
        }
    }

    private val textField = JBTextField().also { it.text = initialText }

    val result: String
        get() = textField.text

    init {
        init()
    }

    override fun createCenterPanel(): JComponent = textField

    override fun getPreferredFocusedComponent(): JComponent = textField

    override fun doValidate(): ValidationInfo? {
        if (!allowEmpty && textField.text.isBlank()) return ValidationInfo("Required", textField)

        return null
    }

}