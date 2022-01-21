package com.chimerapps.storageinspector.ui.ide.actions

import com.chimerapps.storageinspector.ui.ide.ConnectionMode
import com.chimerapps.storageinspector.ui.ide.InspectorSessionWindow
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.intellij.icons.AllIcons

class ConnectAction(private val window: InspectorSessionWindow, listener: () -> Unit) :
    DisableableAction(Tr.ActionConnect.tr(), Tr.ActionConnectDescription.tr(), AllIcons.Actions.Execute, listener) {

    override val isEnabled: Boolean
        get() = window.connectionMode == ConnectionMode.MODE_DISCONNECTED

}