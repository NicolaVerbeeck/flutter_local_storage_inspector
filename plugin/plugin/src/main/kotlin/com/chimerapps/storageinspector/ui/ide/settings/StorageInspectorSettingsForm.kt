package com.chimerapps.storageinspector.ui.ide.settings

import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.labels.LinkLabel
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextPane

//Originally from UI designer
class StorageInspectorSettingsForm(private val testConfigurationClicked: () -> Unit) {
    private val root: JPanel = JPanel().also { it.layout = BoxLayout(it, BoxLayout.PAGE_AXIS) }
    val adbField: TextFieldWithBrowseButton = TextFieldWithBrowseButton()
    val sdbField: TextFieldWithBrowseButton = TextFieldWithBrowseButton()
    val iDeviceField: TextFieldWithBrowseButton = TextFieldWithBrowseButton()
    val resultsPane: JTextPane = JTextPane()
    val analyticsCheckbox = JCheckBox(Tr.PreferencesSendAnalytics.tr())

    init {

        root.add(JBLabel(Tr.PreferencesOptionPathToAdb.tr()).also { it.alignmentX = 0.0f })
        root.add(Box.createVerticalStrut(2))
        root.add(adbField.also {
            it.alignmentX = 0.0f
            it.maximumSize = Dimension(Int.MAX_VALUE, it.preferredSize.height)
        })
        root.add(Box.createVerticalStrut(4))

        root.add(JBLabel(Tr.PreferencesOptionPathToSdb.tr()).also { it.alignmentX = 0.0f })
        root.add(Box.createVerticalStrut(2))
        root.add(sdbField.also {
            it.alignmentX = 0.0f
            it.maximumSize = Dimension(Int.MAX_VALUE, it.preferredSize.height)
        })
        root.add(Box.createVerticalStrut(4))

        root.add(JBLabel(Tr.PreferencesOptionPathToIdevice.tr()).also { it.alignmentX = 0.0f })
        root.add(Box.createVerticalStrut(2))
        root.add(iDeviceField.also {
            it.alignmentX = 0.0f
            it.maximumSize = Dimension(Int.MAX_VALUE, it.preferredSize.height)
        })

        val analyticsPanel = JPanel().also {
            it.layout = BoxLayout(it, BoxLayout.LINE_AXIS)
            it.alignmentX = 0.0f
        }
        analyticsPanel.add(analyticsCheckbox)
        analyticsPanel.add(Box.createHorizontalStrut(8))
        analyticsPanel.add(ActionLink(Tr.PreferencesSendAnalyticsInfo.tr()) {
            BrowserUtil.browse("https://github.com/NicolaVerbeeck/flutter_local_storage_inspector/plugin/analytics/PRIVACY.md")
        })
        root.add(Box.createVerticalStrut(6))
        root.add(analyticsPanel.also {
            it.maximumSize = Dimension(Int.MAX_VALUE, analyticsCheckbox.preferredSize.height)
        })
        root.add(Box.createVerticalStrut(6))
        root.add(JButton(Tr.PreferencesButtonTestConfiguration.tr()).also {
            it.alignmentX = 0.0f
            it.addActionListener { testConfigurationClicked() }
        })

        root.add(Box.createVerticalStrut(4))
        root.add(object : JPanel(BorderLayout()) {
            override fun getPreferredSize(): Dimension {
                return Dimension(root.width - 100, super.getPreferredSize().height)
            }
        }.also { child ->
            child.alignmentX = 0.0f
            child.add(JBScrollPane(resultsPane), BorderLayout.CENTER)
        })

        root.add(Box.createVerticalGlue())
    }

    /**
     * @noinspection ALL
     */
    fun rootComponent(): JComponent {
        return root
    }
}