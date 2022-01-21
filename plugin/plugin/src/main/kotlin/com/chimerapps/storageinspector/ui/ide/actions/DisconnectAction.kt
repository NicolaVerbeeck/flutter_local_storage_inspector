package com.chimerapps.storageinspector.ui.ide.actions

import com.chimerapps.storageinspector.ui.ide.ConnectionMode
import com.chimerapps.storageinspector.ui.ide.InspectorSessionWindow
import com.chimerapps.storageinspector.ui.ide.actions.DisableableAction
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.intellij.icons.AllIcons

class DisconnectAction(private val window: InspectorSessionWindow, listener: () -> Unit) : DisableableAction(
    Tr.ActionDisconnect.tr(),
    Tr.ActionDisconnectDescription.tr(),
    AllIcons.Actions.Suspend,
    listener
) {

    override val isEnabled: Boolean
        get() = window.connectionMode == ConnectionMode.MODE_CONNECTED

}