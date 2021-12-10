package com.chimerapps.storageinspector.ui.util

import com.intellij.openapi.application.ApplicationManager

fun dispatchMain(toExecute: () -> Unit) {
    ApplicationManager.getApplication().invokeLater(toExecute)
}

fun ensureMain(toExecute: () -> Unit) {
    if (ApplicationManager.getApplication().isDispatchThread)
        toExecute()
    else
        dispatchMain(toExecute)
}