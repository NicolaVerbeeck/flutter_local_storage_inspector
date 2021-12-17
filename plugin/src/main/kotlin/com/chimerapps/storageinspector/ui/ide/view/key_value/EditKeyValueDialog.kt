package com.chimerapps.storageinspector.ui.ide.view.key_value

import com.chimerapps.storageinspector.api.protocol.model.StorageType
import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.ui.ide.view.generic.TypedValueEntryView
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
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

    private var freeKeyField: TypedValueEntryView? = null
    private var keyField: ComboBox<String>? = null
    private lateinit var keyTypeField: ComboBox<StorageType>
    private lateinit var valueTypeField: ComboBox<StorageType>
    private lateinit var valueField: TypedValueEntryView

    var results: Pair<ValueWithType, ValueWithType>? = null
        private set

    init {
        init()
    }

    override fun getDimensionServiceKey(): String = "edit_key_value"

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
            root.add(TypedValueEntryView(project, valueTypeField.model.getElementAt(valueTypeField.selectedIndex)).also {
                valueField = it
                it.alignmentX = 0.0f
                it.maximumSize = Dimension(30000, it.preferredSize.height)
            })
            root.add(Box.createVerticalGlue())

            keyTypeField.addActionListener {
                freeKeyField?.updateType(keyTypeField.model.getElementAt(keyTypeField.selectedIndex))
            }

            valueTypeField.addActionListener {
                valueField.updateType(valueTypeField.model.getElementAt(valueTypeField.selectedIndex))
            }

            root.revalidate()
            root.minimumSize = Dimension(300, root.preferredSize.height)
        }
    }

    override fun doValidate(): ValidationInfo? {
        var key = freeKeyField?.doValidate { false }

        if (key == null) {
            val keyValue = keyField!!.selectedItem as String
            key = TypedValueEntryView.doValidateFromString(keyTypeField.model.getElementAt(keyTypeField.selectedIndex), keyValue, keyField!!) { false }
        }
        key.error?.let { return it }

        val valueResult = valueField.doValidate() { storageType -> storageType == StorageType.string }
        valueResult.error?.let { return it }

        results = Pair(key.rawValue!!, valueResult.rawValue!!)

        return null
    }

    private fun makeStorageKeySelector(): JComponent {
        if (restrictKeysTo.isEmpty() && keyHints.isEmpty()) {
            freeKeyField = TypedValueEntryView(project, keyTypeField.model.getElementAt(keyTypeField.selectedIndex))
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