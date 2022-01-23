package com.chimerapps.storageinspector.ui.ide.actions

import com.chimerapps.storageinspector.ui.ide.InspectorToolWindow
import com.chimerapps.storageinspector.ui.util.localization.Tr
import com.intellij.icons.AllIcons

class NewSessionAction(private val window: InspectorToolWindow, actionListener: () -> Unit) : DisableableAction(
    text = Tr.ActionNewSession.tr(), description = Tr.ActionNewSessionDescription.tr(),
    icon = AllIcons.General.Add, actionListener = actionListener
) {

    override val isEnabled: Boolean
        get() = window.isReady

}