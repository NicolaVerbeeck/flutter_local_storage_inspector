package com.chimerapps.storageinspector.inspector.specific.file

import com.chimerapps.storageinspector.api.protocol.model.file.FileInfo
import com.chimerapps.storageinspector.api.protocol.model.file.FileServerIdentification
import com.chimerapps.storageinspector.api.protocol.model.file.FileServerValues
import com.chimerapps.storageinspector.api.protocol.specific.file.FileProtocolListener
import com.chimerapps.storageinspector.api.protocol.specific.file.FileStorageInterface
import com.chimerapps.storageinspector.inspector.StorageServer
import com.chimerapps.storageinspector.inspector.StorageServerType
import com.chimerapps.storageinspector.inspector.specific.BaseInspectorInterface
import com.chimerapps.storageinspector.inspector.specific.BaseInspectorInterfaceImpl
import com.chimerapps.storageinspector.util.analytics.AnalyticsEvent
import com.chimerapps.storageinspector.util.analytics.EventTracker
import java.io.File

/**
 * @author Nicola Verbeeck
 */
interface FileInspectorInterface : BaseInspectorInterface<FileStorageServer> {

    suspend fun getData(server: StorageServer): FileServerValues

    suspend fun reloadData(server: StorageServer): FileServerValues

    suspend fun getContents(server: StorageServer, path: String): ByteArray

    suspend fun putContents(server: StorageServer, path: String, bytes: ByteArray): Boolean

    suspend fun remove(server: StorageServer, path: String): Boolean
}

data class FileStorageServer(
    override val name: String,
    override val icon: String?,
    override val id: String,
    override val type: StorageServerType,
) : StorageServer

class FileInspectorInterfaceImpl(
    private val fileProtocol: FileStorageInterface,
) : BaseInspectorInterfaceImpl<FileStorageServer>(), FileInspectorInterface, FileProtocolListener {

    private var cachedData = mutableMapOf<String, FileServerValues>()

    init {
        fileProtocol.addListener(this)
    }

    override suspend fun getData(server: StorageServer): FileServerValues {
        synchronized(cachedData) {
            cachedData[server.id]?.let { return it }
        }
        return reloadData(server)
    }

    override suspend fun reloadData(server: StorageServer): FileServerValues {
        val serverData = fileProtocol.listFiles(server.id)
        synchronized(cachedData) {
            cachedData[server.id] = serverData
        }
        EventTracker.default.logEvent(AnalyticsEvent.LIST_FILES, serverData.data.size)
        return serverData
    }

    override suspend fun getContents(server: StorageServer, path: String): ByteArray {
        val result = fileProtocol.getFile(server.id, path)
        EventTracker.default.logEvent(AnalyticsEvent.GET_FILE_CONTENT, 1)
        return result
    }

    override suspend fun putContents(server: StorageServer, path: String, bytes: ByteArray): Boolean {
        if (fileProtocol.writeFile(server.id, path, bytes)) {
            EventTracker.default.logEvent(AnalyticsEvent.WRITE_FILE_CONTENT, 1)

            synchronized(cachedData) {
                val cache = cachedData[server.id] ?: return true
                val searchPath = cleanPath(path)
                val old = cache.data.indexOfFirst { info -> info.path == searchPath }
                val newFileInfo = FileInfo(searchPath, bytes.size.toLong(), false)
                val mutableList = cache.data.toMutableList()
                if (old == -1) {
                    mutableList += newFileInfo
                    //Find the first parent that has the isDir property true and remove it

                    var parent = File(searchPath).parentFile
                    while (parent != null) {
                        val parentNodeIndex = cache.data.indexOfFirst { info -> info.path == parent.path }
                        if (parentNodeIndex == -1) break
                        val parentNode = mutableList[parentNodeIndex]
                        if (parentNode.isDir) {
                            mutableList.removeAt(parentNodeIndex)
                            break
                        }
                        parent = parent.parentFile
                    }

                } else mutableList[old] = newFileInfo
                cachedData[server.id] = cache.copy(data = mutableList)
            }
            return true
        }
        return false
    }

    override suspend fun remove(server: StorageServer, path: String): Boolean {
        val result = fileProtocol.remove(server.id, path)
        EventTracker.default.logEvent(AnalyticsEvent.REMOVE_FILE, 1)
        return result
    }

    private fun cleanPath(path: String): String {
        var newPath = path
        while (newPath.startsWith('/')) newPath = newPath.substring(1)
        return newPath
    }

    override fun onServerIdentification(identification: FileServerIdentification) {
        val server = FileStorageServer(
            id = identification.id,
            icon = identification.icon,
            name = identification.name,
            type = StorageServerType.FILE,
        )
        onNewServer(server)
    }

}