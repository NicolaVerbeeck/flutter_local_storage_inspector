package com.chimerapps.storageinspector.ui.ide.util

import com.chimerapps.storageinspector.ui.ide.InspectorToolWindow
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import java.util.regex.Pattern

data class StorageInspectorStartEvent(
    val port: Int,
    val tag: String,
    val extras: Map<String, ExtraData>,
    val tagStart: Int,
    val tagEnd: Int,
)

data class ExtraData(val value: String, val tagStart: Int, val tagEnd: Int)

class StorageInspectorConnectFilter(private val project: Project) : Filter, DumbAware {

    companion object {
        private const val START_REGEX = ".*Storage Inspector server running on ([0-9\\-]+)\\s+\\[([a-zA-Z0-9]+)\\](.*)\\s*"
        private const val EXTRA_REGEX = "\\[([^\\]=]+)=?([^\\]]+)?\\]"

        private val matcher = Pattern.compile(START_REGEX).matcher("")

        fun findServerStart(inLine: String): StorageInspectorStartEvent? {
            synchronized(matcher) {
                matcher.reset(inLine)
                if (!matcher.matches()) return null

                val port = matcher.group(1).toInt()
                val tag = matcher.group(2)

                val tagGroupStart = matcher.start(2)
                val tagGroupEnd = matcher.end(2)

                val extras = mutableMapOf<String, ExtraData>()
                if (matcher.groupCount() > 2) {
                    var start = tagGroupEnd
                    val extraMatcher = Pattern.compile(EXTRA_REGEX).matcher(matcher.group(3))
                    while (extraMatcher.find()) {
                        val inner = extraMatcher.group(1)
                        val outer = if (extraMatcher.groupCount() > 1) extraMatcher.group(2) else null
                        extras[inner] = ExtraData(
                            value = outer ?: "",
                            tagStart = start,
                            tagEnd = start + (extraMatcher.end(0) - extraMatcher.start(0)) - 2,
                        )
                        start += (extraMatcher.end(0) - extraMatcher.start(0)) + 2
                    }
                }

                return StorageInspectorStartEvent(
                    port = port,
                    tag = tag,
                    extras = extras,
                    tagStart = tagGroupStart,
                    tagEnd = tagGroupEnd,
                )
            }
        }
    }

    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        val serverStartEvent = findServerStart(line) ?: return null

        val port = serverStartEvent.port
        val tag = serverStartEvent.tag
        val tagGroupStart = serverStartEvent.tagStart
        val tagGroupEnd = serverStartEvent.tagEnd
        val serviceUrl = serverStartEvent.extras["service"]?.let {
            if (it.value.isEmpty() || it.value == "null") null
            else it
        }

        val textStartOffset = entireLength - line.length
        return Filter.Result(
            listOfNotNull(
                Filter.ResultItem(
                    textStartOffset + tagGroupStart,
                    textStartOffset + tagGroupEnd,
                    StorageInspectorConnectHyperlinkInfo(port, tag)
                ),
                serviceUrl?.let {
                    Filter.ResultItem(
                        textStartOffset + it.tagStart,
                        textStartOffset + it.tagEnd,
                        StorageInspectorConnectVMServiceHyperlinkInfo(it.value)
                    )
                }
            ),
        )
    }

}

class StorageInspectorConnectHyperlinkInfo(private val port: Int, private val tag: String) : HyperlinkInfo {

    override fun navigate(project: Project) {
        val (inspectorWindow, toolWindow) = InspectorToolWindow.get(project) ?: return

        if (!toolWindow.isVisible) {
            toolWindow.show {
                inspectorWindow.newSessionForTag(tag)
            }
        } else {
            inspectorWindow.newSessionForTag(tag)
        }
    }

}

class StorageInspectorConnectVMServiceHyperlinkInfo(private val serviceUri: String) : HyperlinkInfo {

    override fun navigate(project: Project) {
        val (inspectorWindow, toolWindow) = InspectorToolWindow.get(project) ?: return

        if (!toolWindow.isVisible) {
            toolWindow.show {
                inspectorWindow.newSessionWithServiceUri(serviceUri)
            }
        } else {
            inspectorWindow.newSessionWithServiceUri(serviceUri)
        }
    }

}