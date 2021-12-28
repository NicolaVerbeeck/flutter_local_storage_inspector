package com.chimerapps.storageinspector.ui.ide.view.file

import com.chimerapps.storageinspector.api.protocol.model.file.FileServerValues
import com.chimerapps.storageinspector.ui.util.ensureMain
import com.intellij.ui.treeStructure.SimpleTree

/**
 * @author Nicola Verbeeck
 */
class FilesTree : SimpleTree() {

    init {

    }

    fun buildTree(from: FileServerValues) {
        ensureMain {

        }
    }

}