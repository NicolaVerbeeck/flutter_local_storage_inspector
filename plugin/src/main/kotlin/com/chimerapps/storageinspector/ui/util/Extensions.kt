package com.chimerapps.storageinspector.ui.util

import com.intellij.openapi.application.ApplicationManager
import java.util.Enumeration

fun dispatchMain(toExecute: () -> Unit) {
    ApplicationManager.getApplication().invokeLater(toExecute)
}

fun ensureMain(toExecute: () -> Unit) {
    if (ApplicationManager.getApplication().isDispatchThread)
        toExecute()
    else
        dispatchMain(toExecute)
}

fun <T> List<T>.enumerate(): Enumeration<T> {
    val it = iterator()
    return object : Enumeration<T> {
        override fun hasMoreElements(): Boolean = it.hasNext()

        override fun nextElement(): T = it.next()
    }
}