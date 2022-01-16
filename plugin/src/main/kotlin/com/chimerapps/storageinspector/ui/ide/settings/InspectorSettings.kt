package com.chimerapps.storageinspector.ui.ide.settings

import com.google.gsonpackaged.Gson
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag

@State(name = "StorageInspectorSettings", storages = [Storage("storageinspector.xml")])
class StorageInspectorSettings : PersistentStateComponent<StorageInspectorSettingsData> {

    companion object {
        val instance: StorageInspectorSettings
            get() = ApplicationManager.getApplication().getService(StorageInspectorSettings::class.java)
    }

    private var settings: StorageInspectorSettingsData = StorageInspectorSettingsData()

    override fun getState(): StorageInspectorSettingsData = synchronized(this){settings}

    override fun loadState(state: StorageInspectorSettingsData) {
        settings = state
    }

    fun updateState(updater: StorageInspectorSettingsData.() -> StorageInspectorSettingsData) {
        synchronized(this){
            settings = settings.updater()
        }
    }

}

data class StorageInspectorSettingsData(
    var adbPath: String? = null,
    var iDeviceBinariesPath: String? = null,
    var analyticsStatus: Boolean? = null,
    var analyticsUserId: String? = null,
)

@State(name = "StorageInspectorState", storages = [Storage("storageinspector.xml")], reloadable = true)
class StorageInspectorProjectSettings : PersistentStateComponent<StorageInspectorProjectState> {

    companion object {
        fun instance(project: Project): StorageInspectorProjectSettings {
            return project.getService(StorageInspectorProjectSettings::class.java)
        }
    }

    private var state = StorageInspectorProjectState()

    fun updateState(oldStateModifier: StorageInspectorProjectState.() -> StorageInspectorProjectState) {
        state = state.oldStateModifier()
    }

    override fun getState(): StorageInspectorProjectState {
        return state
    }

    override fun loadState(state: StorageInspectorProjectState) {
        this.state = StorageInspectorProjectState()
        XmlSerializerUtil.copyBean(state, this.state)
    }

}

data class StorageInspectorProjectState(
    @OptionTag(converter = ConfigurationConverter::class) val configuration: StorageInspectorConfiguration? = null
) {

    fun updateConfiguration(oldStateModifier: StorageInspectorConfiguration.() -> StorageInspectorConfiguration): StorageInspectorConfiguration {
        return (configuration ?: StorageInspectorConfiguration()).oldStateModifier()
    }

}

data class StorageInspectorConfiguration(
    val keyValueTableConfiguration: KeyValueTableConfiguration? = null,
    val databases: List<SingleDatabaseConfiguration>?=null
) {
    fun updateKeyValueTableConfiguration(
        oldStateModifier: KeyValueTableConfiguration.() -> KeyValueTableConfiguration,
    ): KeyValueTableConfiguration {
        return (keyValueTableConfiguration ?: KeyValueTableConfiguration(-1, -1)).oldStateModifier()
    }

    fun updateDatabase(
        databaseName: String,
        modifier: SingleDatabaseConfiguration.() -> SingleDatabaseConfiguration,
    ): StorageInspectorConfiguration {
        val index = databases?.indexOfFirst { it.databaseName == databaseName } ?: -1

        val mutable = databases?.toMutableList() ?: mutableListOf()
        if (index >= 0) {
            mutable[index] = mutable[index].modifier()
        } else {
            mutable += SingleDatabaseConfiguration(databaseName, emptyList()).modifier()
        }

        return copy(databases = mutable)
    }
}

data class KeyValueTableConfiguration(
    val keyWidth: Int,
    val valueWidth: Int,
)

class ConfigurationConverter : Converter<StorageInspectorConfiguration>() {
    private val gson = Gson()

    override fun toString(value: StorageInspectorConfiguration): String? {
        return gson.toJson(value)
    }

    override fun fromString(value: String): StorageInspectorConfiguration? {
        return gson.fromJson(value, StorageInspectorConfiguration::class.java)
    }

}

data class SingleDatabaseConfiguration(
    val databaseName: String,
    val configuration: List<TableConfiguration>
) {

    fun updateTable(
        tableName: String,
        modifier: TableConfiguration.() -> TableConfiguration
    ): SingleDatabaseConfiguration {
        val index = configuration.indexOfFirst { it.tableName == tableName }

        val mutable = configuration.toMutableList()
        if (index >= 0) {
            mutable[index] = mutable[index].modifier()
        } else {
            mutable += TableConfiguration(tableName, emptyList()).modifier()
        }

        return SingleDatabaseConfiguration(databaseName, mutable)
    }

}

data class TableConfiguration(
    val tableName: String,
    val columns: List<ColumnConfiguration>
) {

    fun updateColumn(name: String, modifier: ColumnConfiguration.() -> ColumnConfiguration): TableConfiguration {
        val index = columns.indexOfFirst { it.columnName == name }

        val mutable = columns.toMutableList()
        if (index >= 0) {
            mutable[index] = mutable[index].modifier()
        } else {
            mutable += ColumnConfiguration(name, visible = true, width = -1).modifier()
        }

        return TableConfiguration(tableName, mutable)
    }

}

data class ColumnConfiguration(
    val columnName: String,
    val visible: Boolean,
    val width: Int
)
