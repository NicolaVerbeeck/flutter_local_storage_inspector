package com.chimerapps.storageinspector.ui.ide.view.file

import com.chimerapps.storageinspector.api.protocol.model.file.FileInfo
import com.intellij.ide.dnd.DnDAction
import com.intellij.ide.dnd.DnDEvent
import com.intellij.ide.dnd.DnDNativeTarget
import com.intellij.ide.dnd.FileCopyPasteUtil
import com.intellij.ide.dnd.TransferableWrapper
import com.intellij.ui.awt.RelativeRectangle
import java.io.File
import javax.swing.JTree
import javax.swing.tree.TreePath

/**
 * @author Nicola Verbeeck
 */
abstract class FilesDropTarget(private val tree: JTree) : DnDNativeTarget {

    override fun update(event: DnDEvent): Boolean {
        event.setDropPossible(false, "")

        val point = event.point ?: return false
        val target = tree.getClosestPathForLocation(point.x, point.y) ?: return false

        val bounds = tree.getPathBounds(target)
        if (bounds == null || bounds.y > point.y || point.y >= bounds.y + bounds.height) return false

        if (!FileCopyPasteUtil.isFileListFlavorAvailable(event)) {
            return false
        }
        event.setHighlighting(RelativeRectangle(tree, bounds), DnDEvent.DropTargetHighlightingType.RECTANGLE)

        event.updateAction(DnDAction.COPY)
        event.isDropPossible = true
        return false
    }

    override fun drop(event: DnDEvent) {
        val point = event.point ?: return

        val target = tree.getClosestPathForLocation(point.x, point.y) ?: return

        val bounds = tree.getPathBounds(target)
        if (bounds == null || bounds.y > point.y || point.y >= bounds.y + bounds.height) return

        val attached = event.attachedObject

        if (FileCopyPasteUtil.isFileListFlavorAvailable(event)) {
            val fileList = FileCopyPasteUtil.getFileListFromAttachedObject(attached)
            if (fileList.isNotEmpty()) {
                handleDrop(fileList, target.lastPathComponent as FileSystemNode)
                //handler.doDropFiles(fileList, target)
            }
        }
    }

    abstract fun handleDrop(files: List<File>, into: FileSystemNode)
}
