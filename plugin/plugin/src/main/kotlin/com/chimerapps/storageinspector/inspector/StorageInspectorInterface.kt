package com.chimerapps.storageinspector.inspector

import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocol
import com.chimerapps.storageinspector.api.protocol.StorageInspectorProtocolListener
import com.chimerapps.storageinspector.api.protocol.model.ServerId
import com.chimerapps.storageinspector.inspector.specific.file.FileInspectorInterface
import com.chimerapps.storageinspector.inspector.specific.file.FileInspectorInterfaceImpl
import com.chimerapps.storageinspector.inspector.specific.key_value.KeyValueInspectorInterface
import com.chimerapps.storageinspector.inspector.specific.key_value.KeyValueInspectorInterfaceImpl
import com.chimerapps.storageinspector.inspector.specific.sql.SQLInspectorInterface
import com.chimerapps.storageinspector.inspector.specific.sql.SQLInspectorInterfaceImpl

/**
 * @author Nicola Verbeeck
 */
interface StorageInspectorInterface : StorageInspectorProtocolListener {

    val keyValueInterface: KeyValueInspectorInterface
    val fileInterface: FileInspectorInterface
    val sqlInterface: SQLInspectorInterface

    val isPaused: Boolean

    suspend fun unpause()
}

class StorageInspectorInterfaceImpl(private val protocol: StorageInspectorProtocol) : StorageInspectorInterface, StorageInspectorProtocolListener {

    override val keyValueInterface = KeyValueInspectorInterfaceImpl(protocol.keyValueServerInterface)
    override val fileInterface = FileInspectorInterfaceImpl(protocol.fileServerInterface)
    override val sqlInterface = SQLInspectorInterfaceImpl(protocol.sqlServerInterface)

    override var isPaused: Boolean = false

    init {
        protocol.addListener(this)
    }

    override fun onServerIdentification(serverId: ServerId) {
        isPaused = serverId.paused
    }

    override suspend fun unpause() {
        protocol.sendUnpause()
        isPaused = false
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
    FILE,
    SQL,
}