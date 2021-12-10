package com.chimerapps.storageinspector.ui.ide.view

import com.chimerapps.storageinspector.api.StorageInspectorConnectionListener
import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocolListener
import com.chimerapps.storageinspector.api.protocol.model.ServerId
import com.chimerapps.storageinspector.ui.ide.util.IncludedIcons
import com.chimerapps.storageinspector.ui.ide.util.ensureMain
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class StorageInspectorStatusBar : JPanel(BorderLayout()), StorageInspectorConnectionListener, StorageInspectorProtocolListener {

    private val statusText = JBLabel().apply {
        isFocusable = false
        text = ""
        verifyInputWhenFocusTarget = false
    }

    private var status: Status = Status.DISCONNECTED
    private var serverInfo: ServerId? = null

    init {
        add(statusText, BorderLayout.CENTER)
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLoweredBevelBorder(),
                null
            ), EmptyBorder(1, 6, 1, 6)
        )
        updateStatusText()
        updateStatusIcon()
    }

    override fun onServerIdentification(serverId: ServerId) {
        this.serverInfo = serverId
        updateStatusText()
    }

    override fun onConnected() {
        status = Status.CONNECTED
        updateStatusIcon()
    }

    override fun onClosed() {
        status = Status.DISCONNECTED
        serverInfo = null
        updateStatusIcon()
        updateStatusText()
    }

    override fun onError() = onClosed()

    private fun updateStatusText() {
        val text = when (status) {
            Status.CONNECTED -> buildText(Tr.StatusConnected.tr(), Tr.StatusConnectedTo.tr())
            Status.DISCONNECTED -> Tr.StatusDisconnected.tr()
        }
        ensureMain {
            statusText.text = text
        }
    }

    private fun updateStatusIcon() {
        ensureMain {
            statusText.icon = when (status) {
                Status.CONNECTED -> IncludedIcons.Status.connected
                Status.DISCONNECTED -> IncludedIcons.Status.disconnected
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun buildText(prefix: String, glue: String): String {
        val builder = StringBuilder()
        builder.append(prefix)
        serverInfo?.let {
            builder.append(' ').append(glue).append(' ')
                .append(it.bundleId)
                .append(" - V")
                .append(it.version)
        }
        return builder.toString()
    }

    private enum class Status {
        CONNECTED, DISCONNECTED
    }

}