package com.chimerapps.storageinspector.inspector.specific.key_value

import com.chimerapps.storageinspector.api.protocol.model.StorageType
import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyIcon
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyTypeHint
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueServerIdentification
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueServerValue
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueServerValues
import com.chimerapps.storageinspector.api.protocol.specific.key_value.KeyValueProtocolListener
import com.chimerapps.storageinspector.api.protocol.specific.key_value.KeyValueServerInterface
import com.chimerapps.storageinspector.inspector.StorageServer
import com.chimerapps.storageinspector.inspector.StorageServerType
import com.chimerapps.storageinspector.inspector.specific.BaseInspectorInterface
import com.chimerapps.storageinspector.inspector.specific.BaseInspectorInterfaceImpl
import com.chimerapps.storageinspector.util.analytics.AnalyticsEvent
import com.chimerapps.storageinspector.util.analytics.EventTracker

interface KeyValueInspectorInterface : BaseInspectorInterface<KeyValueStorageServer> {

    suspend fun getData(server: StorageServer): KeyValueServerValues

    suspend fun reloadData(server: StorageServer): KeyValueServerValues

    suspend fun clear(server: StorageServer): Boolean

    suspend fun remove(server: StorageServer, key: ValueWithType): Boolean

    suspend fun set(server: StorageServer, key: ValueWithType, value: ValueWithType): Boolean

    suspend fun get(server: StorageServer, key: ValueWithType): ValueWithType
}

data class KeyValueStorageServer(
    override val name: String,
    override val icon: String?,
    override val id: String,
    override val type: StorageServerType,
    val keySuggestions: List<ValueWithType>,
    val keyOptions: List<ValueWithType>,
    val supportedKeyTypes: List<StorageType>,
    val supportedValueTypes: List<StorageType>,
    val keyTypeHints: List<KeyTypeHint>,
    val keyIcons: List<KeyIcon>,
) : StorageServer

class KeyValueInspectorInterfaceImpl(
    private val keyValueProtocol: KeyValueServerInterface,
) : BaseInspectorInterfaceImpl<KeyValueStorageServer>(), KeyValueInspectorInterface, KeyValueProtocolListener {

    override val servers = mutableListOf<KeyValueStorageServer>()

    private var cachedData = mutableMapOf<String, KeyValueServerValues>()

    init {
        keyValueProtocol.addListener(this)
    }

    override suspend fun getData(server: StorageServer): KeyValueServerValues {
        synchronized(cachedData) {
            cachedData[server.id]?.let { return it }
        }
        return reloadData(server)
    }

    override suspend fun reloadData(server: StorageServer): KeyValueServerValues {
        val serverData = keyValueProtocol.get(server.id)
        synchronized(cachedData) {
            cachedData[server.id] = serverData
        }
        EventTracker.default.logEvent(AnalyticsEvent.LIST_KEY_VALUES, serverData.values.size)
        return serverData
    }

    override suspend fun clear(server: StorageServer): Boolean {
        if (keyValueProtocol.clear(server.id)) {
            EventTracker.default.logEvent(AnalyticsEvent.CLEAR_KEY_VALUES, 1)
            synchronized(cachedData) {
                cachedData.remove(server.id)
            }
            return true
        }
        return false
    }

    override suspend fun remove(server: StorageServer, key: ValueWithType): Boolean {
        if (keyValueProtocol.remove(server.id, key)) {
            EventTracker.default.logEvent(AnalyticsEvent.REMOVE_KEY_VALUES, 1)
            synchronized(cachedData) {
                val newData = cachedData[server.id]?.let { data -> data.copy(values = data.values.filterNot { it.key == key }) }
                newData?.let { cachedData[server.id] = it }
            }
            return true
        }
        return false
    }

    override suspend fun set(server: StorageServer, key: ValueWithType, value: ValueWithType): Boolean {
        if (keyValueProtocol.set(server.id, key, value)) {
            EventTracker.default.logEvent(AnalyticsEvent.WRITE_KEY_VALUES, 1)

            synchronized(cachedData) {
                val cleanedData = if (value.type == StorageType.binary) value.copy(value = null) else value
                val newData = cachedData[server.id]?.let { data ->
                    val index = data.values.indexOfFirst { it.key == key }
                    if (index == -1)
                        data.copy(values = data.values + KeyValueServerValue(key = key, value = cleanedData))
                    else
                        data.copy(values = data.values.withReplacementAt(index, KeyValueServerValue(key = key, value = cleanedData)))
                }
                newData?.let { cachedData[server.id] = it }
            }
            return true
        }
        return false
    }

    override suspend fun get(server: StorageServer, key: ValueWithType): ValueWithType {
        val data = keyValueProtocol.get(server.id, key)
        synchronized(cachedData) {
            val newData = cachedData[server.id]?.let { cachedValues ->
                EventTracker.default.logEvent(AnalyticsEvent.READ_KEY_VALUES, 1)
                val index = cachedValues.values.indexOfFirst { it.key == key }
                val cleanedData = if (data.type == StorageType.binary) data.copy(value = null) else data
                if (index == -1)
                    cachedValues.copy(values = cachedValues.values + KeyValueServerValue(key = key, value = cleanedData))
                else
                    cachedValues.copy(values = cachedValues.values.withReplacementAt(index, KeyValueServerValue(key = key, value = cleanedData)))
            }
            newData?.let { cachedData[server.id] = it }
        }

        return data
    }

    override fun onServerIdentification(identification: KeyValueServerIdentification) {
        val server = KeyValueStorageServer(
            id = identification.id,
            icon = identification.icon,
            name = identification.name,
            type = StorageServerType.KEY_VALUE,
            keySuggestions = identification.keySuggestions,
            keyOptions = identification.keyOptions,
            supportedKeyTypes = identification.supportedKeyTypes,
            supportedValueTypes = identification.supportedValueTypes,
            keyTypeHints = identification.keyTypeHints,
            keyIcons = identification.keyIcons,
        )
        onNewServer(server)
    }

}

private fun <E> List<E>.withReplacementAt(index: Int, value: E): List<E> {
    val mutable = ArrayList(this)
    mutable[index] = value
    return mutable
}
