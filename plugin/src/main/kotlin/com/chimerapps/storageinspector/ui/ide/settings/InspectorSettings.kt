package com.chimerapps.storageinspector.ui.ide.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "StorageInspectorSettings", storages = [Storage("storageinspector.xml")])
class StorageInspectorSettings : PersistentStateComponent<StorageInspectorSettingsData> {

    companion object {
        val instance: StorageInspectorSettings
            get() = ApplicationManager.getApplication().getService(StorageInspectorSettings::class.java)
    }

    private var settings: StorageInspectorSettingsData = StorageInspectorSettingsData()

    override fun getState(): StorageInspectorSettingsData = settings

    override fun loadState(state: StorageInspectorSettingsData) {
        settings = state
    }

}

data class StorageInspectorSettingsData(
    var adbPath: String? = null,
    var iDeviceBinariesPath: String? = null
)