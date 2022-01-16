package com.chimerapps.storageinspector.inspector.specific

import com.intellij.remoteServer.ServerType

/**
 * @author Nicola Verbeeck
 */
interface BaseInspectorInterface<ServerType> {

    val servers: List<ServerType>

    fun addListener(listener: InspectorInterfaceListener<ServerType>)

    fun removeListener(listener: InspectorInterfaceListener<ServerType>)

}

abstract class BaseInspectorInterfaceImpl<ServerType> : BaseInspectorInterface<ServerType> {
    private val internalServers = mutableListOf<ServerType>()
    private val listeners = mutableListOf<InspectorInterfaceListener<ServerType>>()

    override val servers: List<ServerType>
        get() = internalServers

    override fun addListener(listener: InspectorInterfaceListener<ServerType>) {
        synchronized(listeners) { listeners += listener }
        synchronized(internalServers){
            internalServers.forEach(listener::onServerAdded)
        }
    }

    override fun removeListener(listener: InspectorInterfaceListener<ServerType>) {
        synchronized(listeners) { listeners -= listener }
    }

    protected fun onNewServer(server: ServerType){
        synchronized(internalServers) {
            internalServers.add(server)
        }
        synchronized(listeners) {
            listeners.forEach { it.onServerAdded(server) }
        }
    }
}

interface InspectorInterfaceListener<ServerType> {
    fun onServerAdded(server: ServerType)
}