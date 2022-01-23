package com.chimerapps.storageinspector.ui.ide.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.AnimatedIcon
import javax.swing.Icon

class RefreshAction(title: String, description: String, private val icon: Icon = AllIcons.Actions.Refresh, actionListener: () -> Unit) :
    SimpleAction(title, description, icon, actionListener) {

    var refreshing: Boolean = false

    override fun update(e: AnActionEvent) {
        e.presentation.icon = if (refreshing) AnimatedIcon.Default() else icon
    }
}