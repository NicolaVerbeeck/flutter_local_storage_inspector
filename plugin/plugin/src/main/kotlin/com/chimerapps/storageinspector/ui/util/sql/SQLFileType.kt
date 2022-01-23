@file:Suppress("DialogTitleCapitalization")

package com.chimerapps.storageinspector.ui.util.sql

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.LanguageFileType

/**
 * @author Nicola Verbeeck
 */
object SQLFileType : LanguageFileType(SQLLanguage) {
    override fun getIcon() = AllIcons.Debugger.Db_db_object
    override fun getName() = "SQL File"
    override fun getDefaultExtension() = "sql"
    override fun getDescription() = "SQL Language File"
}