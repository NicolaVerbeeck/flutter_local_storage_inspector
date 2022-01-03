package com.chimerapps.storageinspector.inspector.specific.file

import com.chimerapps.storageinspector.api.protocol.model.file.FileServerIdentification
import com.chimerapps.storageinspector.api.protocol.model.file.FileServerValues
import com.chimerapps.storageinspector.api.protocol.specific.file.FileProtocolListener
import com.chimerapps.storageinspector.api.protocol.specific.file.FileStorageInterface
import com.chimerapps.storageinspector.inspector.StorageServer
import com.chimerapps.storageinspector.inspector.StorageServerType

/**
 * @author Nicola Verbeeck
 */
interface FileInspectorInterface {

    val servers: List<FileStorageServer>

    fun addListener(listener: FileInspectorListener)

    fun removeListener(listener: FileInspectorListener)

    suspend fun getData(server: StorageServer): FileServerValues

    suspend fun reloadData(server: StorageServer): FileServerValues

    suspend fun getContents(server: StorageServer, path: String): ByteArray
}

interface FileInspectorListener {
    fun onServerAdded(server: FileStorageServer)
}

data class FileStorageServer(
    override val name: String,
    override val icon: String?,
    override val id: String,
    override val type: StorageServerType,
) : StorageServer

class FileInspectorInterfaceImpl(
    private val fileProtocol: FileStorageInterface,
) : FileInspectorInterface, FileProtocolListener {

    override val servers = mutableListOf<FileStorageServer>()
    private val listeners = mutableListOf<FileInspectorListener>()

    private var cachedData = mutableMapOf<String, FileServerValues>()

    init {
        fileProtocol.addListener(this)
    }

    override fun addListener(listener: FileInspectorListener) {
        synchronized(listeners) { listeners += listener }
    }

    override fun removeListener(listener: FileInspectorListener) {
        synchronized(listeners) { listeners -= listener }
    }

    override suspend fun getData(server: StorageServer): FileServerValues {
        cachedData[server.id]?.let { return it }
        return reloadData(server)
    }

    override suspend fun reloadData(server: StorageServer): FileServerValues {
        val serverData = fileProtocol.listFiles(server.id)
        cachedData[server.id] = serverData
        return serverData
    }

    override suspend fun getContents(server: StorageServer, path: String): ByteArray {
        return fileProtocol.getFile(server.id, path)
    }

    override fun onServerIdentification(identification: FileServerIdentification) {
        val server = FileStorageServer(
            id = identification.id,
            icon = identification.icon,
            name = identification.name,
            type = StorageServerType.FILE,
        )
        synchronized(servers) {
            servers.add(server)
        }
        synchronized(listeners) {
            listeners.forEach { it.onServerAdded(server) }
        }
    }

}