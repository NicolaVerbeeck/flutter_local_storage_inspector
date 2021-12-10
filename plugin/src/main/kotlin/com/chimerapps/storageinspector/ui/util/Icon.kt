package com.chimerapps.storageinspector.ui.util

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

private fun Any.loadIcon(path: String): Icon {
    return IconLoader.getIcon(path, javaClass)
}

object IncludedIcons {

    object Status {
        val connected = loadIcon("/ic_connected.svg")
        val disconnected = loadIcon("/ic_disconnected.svg")
    }

}