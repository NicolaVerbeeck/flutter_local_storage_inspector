package com.chimerapps.storageinspector.ui.util.sql

import com.alecstrong.sql.psi.core.SqlFileBase
import com.chimerapps.storageinspector.ui.ide.InspectorToolWindow
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.SingleRootFileViewProvider
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.util.LocalTimeCounter

/**
 * @author Nicola Verbeeck
 */
class SQLFile(viewProvider: FileViewProvider, private var isRoot: Boolean) : SqlFileBase(viewProvider, SQLLanguage) {

    private var lastSchemaFile: SqlFileBase? = null
    private var lastSchema: String? = null

    override val order = name.substringBefore(".${fileType.defaultExtension}").let { name ->
        if (name.all { it in '0'..'9' }) name.toInt()
        else null
    }
    override fun getFileType() = SQLFileType
    override fun toString() = "SQL File"

    override fun baseContributorFile(): SqlFileBase? {
        if (!isRoot) return null

        val window = InspectorToolWindow.get(project)?.first?.focussedSessionWindow
        val schema = window?.selectedDatabaseSchema ?: return null

        if (schema == lastSchema) return lastSchemaFile

        val factory = PsiFileFactory.getInstance(project)

        val stamp = LocalTimeCounter.currentTime()
        val psiFile = factory.createFileFromText("schema.sql", SQLFileType, schema, stamp, true, false)
        TreeUtil.ensureParsed(psiFile.node)

        val file = SQLFile(psiFile.viewProvider, isRoot = false)
        lastSchema = schema
        lastSchemaFile = file
        return file
    }
}