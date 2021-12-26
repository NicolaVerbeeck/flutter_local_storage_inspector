package com.chimerapps.storageinspector.ui.util.file

import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileElement
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.fileChooser.FileTypeDescriptor
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

@Suppress("UNUSED_PARAMETER")
fun chooseSaveFile(title: String, extension: String): File? {
    val descriptor = FileSaverDescriptor(title, "")
    val dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, null)
    val result = dialog.save(null as VirtualFile?, null)

    return result?.file
}

fun chooseOpenFile(title: String, vararg extensions: String): VirtualFile? {
    val descriptor = if (extensions.isEmpty()) AnyFileTypeChooser(title) else FileTypeDescriptor(title, *extensions)

    val dialog = FileChooserFactory.getInstance().createFileChooser(descriptor, null, null)
    val result = dialog.choose(null)

    return result.getOrNull(0)
}

private class AnyFileTypeChooser(title: String) : FileTypeDescriptor(title, ".ignore") {

    override fun isFileVisible(file: VirtualFile, showHiddenFiles: Boolean): Boolean {
        if (!showHiddenFiles && FileElement.isFileHidden(file)) {
            return false
        }

        if (file.isDirectory) {
            return true
        }
        return true
    }

}
