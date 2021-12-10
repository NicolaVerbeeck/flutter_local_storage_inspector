package com.chimerapps.storageinspector.inspector

import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocol
import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocolListener
import com.chimerapps.storageinspector.api.protocol.model.ServerId
import com.chimerapps.storageinspector.inspector.specific.key_value.KeyValueInspectorInterface
import com.chimerapps.storageinspector.inspector.specific.key_value.KeyValueInspectorInterfaceImpl

/**
 * @author Nicola Verbeeck
 */
interface StorageInspectorInterface {

    val keyValueInterface: KeyValueInspectorInterface

}

class StorageInspectorInterfaceImpl(protocol: StorageInspectorProtocol) : StorageInspectorInterface, StorageInspectorProtocolListener {

    override val keyValueInterface = KeyValueInspectorInterfaceImpl(protocol.keyValueServerInterface)

    init {
        protocol.addListener(this)
    }

    override fun onServerIdentification(serverId: ServerId) {

    }
}

interface StorageServer {
    val name: String
    val icon: String?
    val id: String
    val type: StorageServerType
}

enum class StorageServerType {
    KEY_VALUE,
}