package com.chimerapps.storageinspector.ui.ide.settings

import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.Spacer
import java.awt.Dimension
import java.awt.Insets
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.border.TitledBorder

//Originally from UI designer
class StorageInspectorSettingsForm {
    private val root: JPanel = JPanel()
    val adbField: TextFieldWithBrowseButton
    val iDeviceField: TextFieldWithBrowseButton
    val testConfigurationButton: JButton
    val resultsPane: JTextPane
    val pathToAdbLabel: JBLabel
    val pathToiDeviceLabel: JBLabel

    init {
        root.layout = GridLayoutManager(6, 1, Insets(0, 0, 0, 0), -1, -1)
        pathToAdbLabel = JBLabel()
        pathToAdbLabel.text = "Path to adb:"
        root.add(
            pathToAdbLabel,
            GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false
            )
        )
        val spacer1 = Spacer()
        root.add(
            spacer1,
            GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false)
        )
        adbField = TextFieldWithBrowseButton()
        root.add(
            adbField,
            GridConstraints(
                1,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                Dimension(150, -1),
                null,
                0,
                false
            )
        )
        pathToiDeviceLabel = JBLabel()
        pathToiDeviceLabel.text = "Path to idevice binaries:"
        root.add(
            pathToiDeviceLabel,
            GridConstraints(
                2,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false
            )
        )
        iDeviceField = TextFieldWithBrowseButton()
        root.add(
            iDeviceField,
            GridConstraints(
                3,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                Dimension(150, -1),
                null,
                0,
                false
            )
        )
        val panel1 = JPanel()
        panel1.layout = GridLayoutManager(2, 1, Insets(2, 2, 2, 2), -1, -1)
        root.add(
            panel1,
            GridConstraints(
                4,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null,
                0,
                false
            )
        )
        panel1.border =
            BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null)
        val panel2 = JPanel()
        panel2.layout = GridLayoutManager(1, 2, Insets(0, 0, 0, 0), -1, -1)
        panel1.add(
            panel2,
            GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null,
                0,
                false
            )
        )
        testConfigurationButton = JButton()
        testConfigurationButton.text = "Test configuration"
        panel2.add(
            testConfigurationButton,
            GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false
            )
        )
        val spacer2 = Spacer()
        panel2.add(
            spacer2,
            GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false)
        )
        val scrollPane1 = JBScrollPane()
        panel1.add(
            scrollPane1,
            GridConstraints(
                1,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_WANT_GROW,
                null,
                null,
                null,
                0,
                false
            )
        )
        resultsPane = JTextPane()
        scrollPane1.setViewportView(resultsPane)
    }

    /**
     * @noinspection ALL
     */
    fun rootComponent(): JComponent {
        return root
    }
}