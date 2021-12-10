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

interface KeyValueInspectorInterface {

    val servers: List<KeyValueStorageServer>

    fun addListener(listener: KeyValueInspectorListener)

    fun removeListener(listener: KeyValueInspectorListener)

    suspend fun getData(server: StorageServer): KeyValueServerValues

    suspend fun reloadData(server: StorageServer): KeyValueServerValues

    suspend fun clear(server: StorageServer): Boolean

    suspend fun remove(server: StorageServer, key: ValueWithType): Boolean

    suspend fun set(server: StorageServer, key: ValueWithType, value: ValueWithType): Boolean
}

interface KeyValueInspectorListener {
    fun onServerAdded(server: KeyValueStorageServer)
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
) : KeyValueInspectorInterface, KeyValueProtocolListener {

    override val servers = mutableListOf<KeyValueStorageServer>()

    private val listeners = mutableListOf<KeyValueInspectorListener>()

    private var cachedData: KeyValueServerValues? = null

    init {
        keyValueProtocol.addListener(this)
    }

    override fun addListener(listener: KeyValueInspectorListener) {
        synchronized(listeners) { listeners.add(listener) }
    }

    override fun removeListener(listener: KeyValueInspectorListener) {
        synchronized(listeners) { listeners.remove(listener) }
    }

    override suspend fun getData(server: StorageServer): KeyValueServerValues {
        cachedData?.let { return it }
        return reloadData(server)
    }

    override suspend fun reloadData(server: StorageServer): KeyValueServerValues {
        val serverData = keyValueProtocol.get(server.id)
        cachedData = serverData
        return serverData
    }

    override suspend fun clear(server: StorageServer): Boolean {
        if (keyValueProtocol.clear(server.id)) {
            cachedData = KeyValueServerValues(server.id, emptyList())
            return true
        }
        return false
    }

    override suspend fun remove(server: StorageServer, key: ValueWithType): Boolean {
        if (keyValueProtocol.remove(server.id, key)) {
            cachedData = cachedData?.let { data -> data.copy(values = data.values.filterNot { it.key == key }) }
            return true
        }
        return false
    }

    override suspend fun set(server: StorageServer, key: ValueWithType, value: ValueWithType): Boolean {
        if (keyValueProtocol.set(server.id, key, value)) {
            cachedData = cachedData?.let { data ->
                val index = data.values.indexOfFirst { it.key == key }
                if (index == -1)
                    data.copy(values = data.values + KeyValueServerValue(key = key, value = value))
                else
                    data.copy(values = data.values.withReplacementAt(index, KeyValueServerValue(key = key, value = value)))
            }
            return true
        }
        return false
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
        synchronized(servers) {
            servers.add(server)
        }
        synchronized(listeners) {
            listeners.forEach { it.onServerAdded(server) }
        }
    }

}

private fun <E> List<E>.withReplacementAt(index: Int, value: E): List<E> {
    val mutable = ArrayList(this)
    mutable[index] = value
    return mutable
}
