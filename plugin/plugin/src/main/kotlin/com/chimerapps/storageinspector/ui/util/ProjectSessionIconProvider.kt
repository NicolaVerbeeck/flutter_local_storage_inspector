package com.chimerapps.storageinspector.ui.util

import com.chimerapps.discovery.ui.Base64SessionIconProvider
import com.chimerapps.discovery.ui.CompoundSessionIconProvider
import com.chimerapps.discovery.ui.DefaultSessionIconProvider
import com.chimerapps.discovery.ui.SessionIconProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.util.IconUtil
import java.lang.Float.min
import javax.swing.Icon
import javax.swing.ImageIcon

class ProjectSessionIconProvider private constructor(
    private val project: Project,
    private val delegate: SessionIconProvider,
    private val requestedWidth: Int,
    private val requestedHeight: Int,
) : SessionIconProvider {

    companion object {
        private val projectInstances = mutableMapOf<Pair<Project, Pair<Int, Int>>, ProjectSessionIconProvider>()

        fun instance(
            project: Project, requestedWidth: Int = 20, requestedHeight: Int = 20, delegate: SessionIconProvider = CompoundSessionIconProvider(
                SVGIconProvider(requestedWidth, requestedHeight),
                DefaultSessionIconProvider(),
                Base64SessionIconProvider(width = requestedWidth.toFloat(), height = requestedHeight.toFloat()),
            )
        ): ProjectSessionIconProvider {
            return projectInstances.getOrPut(project to (requestedWidth to requestedHeight)) { ProjectSessionIconProvider(project, delegate, requestedWidth, requestedHeight) }
        }
    }

    private val cache = mutableMapOf<String, Icon?>()

    override fun iconForString(iconString: String): Icon? {
        if (!cache.containsKey(iconString)) {
            initFromProject(iconString)
        }
        return cache[iconString] ?: delegate.iconForString(iconString)?.let { icon ->
            if (icon.iconWidth > requestedWidth || icon.iconHeight > requestedHeight) {
                IconUtil.scale(icon, null, min(requestedWidth.toFloat() / icon.iconWidth, requestedHeight.toFloat() / icon.iconHeight))
            } else icon
        }
    }

    private fun initFromProject(iconString: String) {
        val icon = loadFromProject(iconString)?.let {
            IconUtil.scale(it, null, min(requestedWidth.toFloat() / it.iconWidth, requestedHeight.toFloat() / it.iconHeight))
        }
        cache[iconString] = icon
    }

    private fun loadFromProject(iconString: String): Icon? {
        val dir = project.guessProjectDir() ?: return null
        val matches = dir.findChild(".idea")?.findChild("storage_inspector")?.children?.filter { file ->
            !file.isDirectory && (file.nameWithoutExtension == iconString || file.nameWithoutExtension == "$iconString@2x") && file.extension != "svg"
        } ?: return null
        if (matches.isEmpty()) return null

        matches.filter { it.nameWithoutExtension.endsWith("@2x") }.forEach { return ImageIcon(it.path) }
        matches.forEach { return ImageIcon(it.path) }

        return null
    }

}