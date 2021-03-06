package com.chimerapps.storageinspector.ui.util.sql

import com.alecstrong.sql.psi.core.SqlParserDefinition
import com.intellij.psi.FileViewProvider
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.ILightStubFileElementType

/**
 * @author Nicola Verbeeck
 */
class SQLParserDefinition : SqlParserDefinition() {

    override fun createFile(viewProvider: FileViewProvider): LocalStorageInspectorSQLFile {
        return LocalStorageInspectorSQLFile(viewProvider, isRoot = true)
    }

    override fun getFileNodeType() = FILE
    override fun getLanguage() = LocalStorageInspectorSQLLanguage

    companion object {
        val FILE = ILightStubFileElementType<PsiFileStub<LocalStorageInspectorSQLFile>>(LocalStorageInspectorSQLLanguage)
    }
}