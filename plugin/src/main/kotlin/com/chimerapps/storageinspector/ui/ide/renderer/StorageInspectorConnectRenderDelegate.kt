package com.chimerapps.storageinspector.ui.ide.renderer

import com.chimerapps.discovery.model.connectdialog.ConnectDialogProcessNode
import com.chimerapps.discovery.ui.LocalizationDelegate
import com.chimerapps.discovery.ui.SessionIconProvider
import com.chimerapps.discovery.ui.renderer.ConnectDialogTreeCellDelegate
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes

/**
 * @author Nicola Verbeeck
 */
class StorageInspectorConnectRenderDelegate : ConnectDialogTreeCellDelegate() {

    override fun renderConnectProcessNode(
        node: ConnectDialogProcessNode,
        iconProvider: SessionIconProvider,
        renderer: ColoredTreeCellRenderer,
        localizationDelegate: LocalizationDelegate
    ) {
        super.renderConnectProcessNode(node, iconProvider, renderer, localizationDelegate)
        val isPaused = node.session.extensions?.find { it.name == "paused" } != null
        if (isPaused) {
            renderer.append(Tr.StatusPaused.tr(), SimpleTextAttributes.GRAYED_ITALIC_ATTRIBUTES)
        }
    }
}