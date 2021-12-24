package com.chimerapps.storageinspector.ui.util.notification

import com.intellij.ide.actions.RevealFileAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object NotificationUtil {

    private const val NOTIFICATION_GROUP_ID = "local-storage-inspector"

    fun info(title: String, message: String, project: Project?) {
        val group = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID)

        group.createNotification(title, message, NotificationType.INFORMATION)
            .setListener(RevealFileAction.FILE_SELECTING_LISTENER)
            .notify(project)
    }

    fun error(title: String, message: String, project: Project?) {
        val group = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_ID)

        group.createNotification(title, message, NotificationType.ERROR)
            .setListener(RevealFileAction.FILE_SELECTING_LISTENER)
            .notify(project)
    }
}