package com.chimerapps.storageinspector.ui.ide.view.key_value

import com.chimerapps.storageinspector.api.protocol.model.StorageType
import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.ui.ide.view.generic.TypedValueEntryView
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent

/**
 * @author Nicola Verbeeck
 */
class EditKeyValueDialog(
    private val restrictKeysTo: List<ValueWithType>,
    private val restrictKeyTypesTo: List<StorageType>,
    private val keyHints: List<ValueWithType>,
    private val restrictValueTypesTo: List<StorageType>,
    private val project: Project,
) : DialogWrapper(project, false, IdeModalityType.PROJECT) {

    var freeKeyField: TypedValueEntryView? = null
    var keyField: ComboBox<String>? = null
    lateinit var keyTypeField: ComboBox<StorageType>
    lateinit var valueTypeField: ComboBox<StorageType>
    lateinit var valueField: TypedValueEntryView

    init {
        init()
    }

    override fun createCenterPanel(): JComponent {
        return DialogPanel().also { root ->
            root.layout = BoxLayout(root, BoxLayout.Y_AXIS)

            root.add(JBLabel("Key type").also { it.alignmentX = 0.0f })
            root.add(makeStorageTypeSelector(restrictKeyTypesTo).also {
                keyTypeField = it
                it.alignmentX = 0.0f
                it.maximumSize = Dimension(30000, it.preferredSize.height)
            })

            root.add(JBLabel("Key value").also { it.alignmentX = 0.0f })
            root.add(makeStorageKeySelector().also {
                it.alignmentX = 0.0f
                it.maximumSize = Dimension(30000, it.preferredSize.height)
            })

            root.add(JBLabel("Value type").also { it.alignmentX = 0.0f })
            root.add(makeStorageTypeSelector(restrictValueTypesTo).also {
                valueTypeField = it
                it.alignmentX = 0.0f
                it.maximumSize = Dimension(30000, it.preferredSize.height)
            })

            root.add(JBLabel("Value").also { it.alignmentX = 0.0f })
            root.add(TypedValueEntryView(project).also {
                valueField = it
                it.alignmentX = 0.0f
                it.maximumSize = Dimension(30000, it.preferredSize.height)
            })
            root.add(Box.createVerticalGlue())

            valueTypeField.addActionListener {
                valueField.updateType(valueTypeField.model.getElementAt(valueTypeField.selectedIndex))
            }
        }
    }

    private fun makeStorageKeySelector(): JComponent {
        if (restrictKeysTo.isEmpty() && keyHints.isEmpty()) {
            freeKeyField = TypedValueEntryView(project)
            return freeKeyField!!
        }

        val box = ComboBox(restrictKeysTo.ifEmpty { keyHints }.map { it.asString }.toTypedArray())
        keyField = box

        if (restrictKeysTo.isEmpty()) box.isEditable = true

        return box
    }

    private fun makeStorageTypeSelector(restrictTo: List<StorageType>): ComboBox<StorageType> {
        return if (restrictTo.size == 1) {
            ComboBox(arrayOf(restrictTo[0])).also {
                it.isEnabled = false
                it.selectedIndex = 0
            }
        } else if (restrictTo.isNotEmpty()) {
            ComboBox(restrictTo.toTypedArray())
        } else {
            ComboBox(StorageType.values())
        }
    }

}