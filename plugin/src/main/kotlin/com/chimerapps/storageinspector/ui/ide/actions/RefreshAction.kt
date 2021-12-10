package com.chimerapps.storageinspector.ui.ide.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.AnimatedIcon

class RefreshAction(title: String, description: String, actionListener: () -> Unit) :
    SimpleAction(title, description, AllIcons.Actions.Refresh, actionListener) {

    var refreshing: Boolean = false
        set(value) {
            if (field != value) {
                field = value

            }
        }

    override fun update(e: AnActionEvent) {
        e.presentation.icon = if (refreshing) AnimatedIcon.Default() else AllIcons.Actions.Refresh
    }
}