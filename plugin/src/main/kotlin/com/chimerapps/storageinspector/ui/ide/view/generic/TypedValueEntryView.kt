package com.chimerapps.storageinspector.ui.ide.view.generic

import com.chimerapps.storageinspector.api.protocol.model.StorageType
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.FixedSizeButton
import java.awt.BorderLayout
import java.text.DecimalFormat
import java.text.NumberFormat
import javax.swing.JFormattedTextField
import javax.swing.JPanel
import javax.swing.text.DefaultFormatterFactory
import javax.swing.text.NumberFormatter


/**
 * @author Nicola Verbeeck
 */
class TypedValueEntryView : JPanel(BorderLayout()) {

    private val booleanSelector = ComboBox(arrayOf(Tr.TypeBooleanTrue.tr(), Tr.TypeBooleanFalse.tr()))
    private var freeEditField: JFormattedTextField? = null
    private val valueButton = FixedSizeButton()

    init {
        freeEditField = JFormattedTextField()
        add(freeEditField!!, BorderLayout.CENTER)
    }

    fun updateType(type: StorageType) {
        when (type) {
            StorageType.string -> ensureFreeEditField(null).also { it.formatterFactory = null }
            StorageType.int -> ensureFreeEditField(makeIntegerFactory()).also { it.value = 0 }
            StorageType.double -> ensureFreeEditField(makeDoubleFactory()).also { it.value = 0.0 }
            StorageType.datetime -> TODO()
            StorageType.binary -> TODO()
            StorageType.bool -> ensureBooleanEdit()
            StorageType.stringlist -> TODO()
        }
    }

    private fun makeDoubleFactory(): DefaultFormatterFactory {
        val longFormat = DecimalFormat.getNumberInstance()
        val numberFormatter = NumberFormatter(longFormat)
        numberFormatter.valueClass = Double::class.java
        numberFormatter.allowsInvalid = false

        return DefaultFormatterFactory(numberFormatter)
    }

    private fun makeIntegerFactory(): DefaultFormatterFactory {
        val longFormat = NumberFormat.getIntegerInstance()
        val numberFormatter = NumberFormatter(longFormat)
        numberFormatter.valueClass = Long::class.java
        numberFormatter.allowsInvalid = false

        return DefaultFormatterFactory(numberFormatter)
    }

    private fun ensureFreeEditField(factory: DefaultFormatterFactory?): JFormattedTextField {
        freeEditField?.let { remove(it) }
        if (booleanSelector.parent != null) {
            remove(booleanSelector)
        }
        freeEditField = if (factory == null) JFormattedTextField() else JFormattedTextField(factory)
        add(freeEditField!!, BorderLayout.CENTER)
        revalidate()
        return freeEditField!!
    }

    private fun ensureBooleanEdit() {
        if (booleanSelector.parent != null) return
        remove(freeEditField)
        freeEditField = null
        add(booleanSelector, BorderLayout.CENTER)
        revalidate()
    }

}