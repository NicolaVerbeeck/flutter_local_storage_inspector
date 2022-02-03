package com.chimerapps.storageinspector.ui.util.sql

import com.alecstrong.sql.psi.core.SqlFileBase
import com.chimerapps.storageinspector.ui.ide.InspectorToolWindow
import com.intellij.openapi.util.Key
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.util.LocalTimeCounter

/**
 * @author Nicola Verbeeck
 */
class LocalStorageInspectorSQLFile(viewProvider: FileViewProvider, private var isRoot: Boolean) : SqlFileBase(viewProvider, LocalStorageInspectorSQLLanguage) {

    companion object {
        val preventInjectSchema = Key<Boolean>("preventInjectSchema")
    }

    private var lastSchemaFile: SqlFileBase? = null
    private var lastSchema: String? = null

    override val order = name.substringBefore(".${fileType.defaultExtension}").let { name ->
        if (name.all { it in '0'..'9' }) name.toInt()
        else null
    }
    override fun getFileType() = LocalStorageInspectorSQLFileType
    override fun toString() = "Local Storage Inspector SQL File"

    override fun baseContributorFile(): SqlFileBase? {
        if (!isRoot) return null
        if (getUserData(preventInjectSchema) == true) return null

        val window = InspectorToolWindow.get(project)?.first?.focussedSessionWindow
        val schema = window?.selectedDatabaseSchema ?: return null

        if (schema == lastSchema) return lastSchemaFile

        val factory = PsiFileFactory.getInstance(project)

        val stamp = LocalTimeCounter.currentTime()
        val psiFile = factory.createFileFromText("schema.sql", LocalStorageInspectorSQLFileType, schema, stamp, true, false)
        TreeUtil.ensureParsed(psiFile.node)

        val file = LocalStorageInspectorSQLFile(psiFile.viewProvider, isRoot = false)
        lastSchema = schema
        lastSchemaFile = file
        return file
    }
}