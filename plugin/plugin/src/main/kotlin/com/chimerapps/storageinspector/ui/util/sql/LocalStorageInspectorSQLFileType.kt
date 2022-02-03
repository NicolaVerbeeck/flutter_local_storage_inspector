@file:Suppress("DialogTitleCapitalization")

package com.chimerapps.storageinspector.ui.util.sql

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.LanguageFileType

/**
 * @author Nicola Verbeeck
 */
object LocalStorageInspectorSQLFileType : LanguageFileType(LocalStorageInspectorSQLLanguage) {
    override fun getIcon() = AllIcons.Debugger.Db_db_object
    override fun getName() = "Local Storage Inspector SQL File"
    override fun getDefaultExtension() = "lsisql"
    override fun getDescription() = "Local Storage Inspector SQL Language File"
}