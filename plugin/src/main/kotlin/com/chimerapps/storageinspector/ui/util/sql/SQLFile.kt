package com.chimerapps.storageinspector.ui.util.sql

import com.alecstrong.sql.psi.core.SqlFileBase
import com.intellij.psi.FileViewProvider

/**
 * @author Nicola Verbeeck
 */
class SQLFile(viewProvider: FileViewProvider) : SqlFileBase(viewProvider, SQLLanguage) {
    override val order = name.substringBefore(".${fileType.defaultExtension}").let { name ->
        if (name.all { it in '0'..'9' }) name.toInt()
        else null
    }
    override fun getFileType() = SQLFileType
    override fun toString() = "SQL File"
}