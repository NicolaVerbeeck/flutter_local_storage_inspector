package com.chimerapps.storageinspector.util.analytics.batching

import com.chimerapps.storageinspector.ui.ide.settings.StorageInspectorSettings
import com.chimerapps.storageinspector.util.analytics.AnalyticsEvent
import com.chimerapps.storageinspector.util.analytics.EventTracker
import com.chimerapps.storageinspector.util.analytics.ga.GAEventTarget
import com.intellij.openapi.application.appSystemDir
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.RandomAccessFile
import java.nio.ByteBuffer


/**
 * @author Nicola Verbeeck
 */
class BatchingEventTracker : EventTracker {

    companion object {
        val instance: BatchingEventTracker by lazy { BatchingEventTracker() }

        private val batchFile = appSystemDir.resolve("caches").resolve("storage_plugin_analytics.dat").toFile()
    }

    private val target = GAEventTarget.instance
    private val scope = CoroutineScope(SupervisorJob())

    fun clear() {
        synchronized(batchFile) {
            if (batchFile.exists()) batchFile.delete()
        }
    }

    override fun logEvent(event: AnalyticsEvent, count: Int?) {
        val state = StorageInspectorSettings.instance.state
        if (state.analyticsStatus != true) return

        ensureFile()
        synchronized(batchFile) {
            val openFile = RandomAccessFile(batchFile, "rw")
            val numItems: Int
            val lastWrite: Long
            if (openFile.length() == 0L || openFile.length() <= 5) {
                openFile.write(1)
                lastWrite = System.currentTimeMillis()
                openFile.write(lastWrite.toByteArray())
                numItems = 1
            } else {
                numItems = openFile.read()
                openFile.seek(0L)
                openFile.write(numItems + 1)
                val buffer = ByteArray(Long.SIZE_BYTES)
                openFile.read(buffer)
                lastWrite = buffer.toLong()
            }
            openFile.seek(openFile.length())
            openFile.write("${event.name}:${count ?: ""}\n".toByteArray(charset = Charsets.UTF_8))

            if (numItems < 10 && ((System.currentTimeMillis() - lastWrite) < 86400000L)) {
                openFile.close()
                return@synchronized null
            }
            openFile.seek(5)
            val buffer = ByteArray((openFile.length() - 5).toInt())
            openFile.readFully(buffer)
            openFile.close()

            batchFile.delete()
            String(buffer).split("\n")
        }?.forEach { line ->
            val (name, countStr) = line.split(':')
            val resolvedCount = countStr.toIntOrNull()

            scope.launch {
                try {
                    target.sendEvent(name, resolvedCount)
                } catch (ignored: Throwable) {
                }
            }
        }
    }

    private fun ensureFile() {
        if (batchFile.exists()) return
        if (batchFile.parentFile.exists()) return
        batchFile.parentFile.mkdirs()
    }
}

interface EventTarget {
    suspend fun sendEvent(eventName: String, count: Int?): Boolean
}

private fun Long.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(Long.SIZE_BYTES)
    buffer.putLong(this)
    return buffer.array()
}

private fun ByteArray.toLong(): Long {
    val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
    buffer.put(this)
    buffer.flip()
    return buffer.long
}