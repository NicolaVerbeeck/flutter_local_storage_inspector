package com.chimerapps.storageinspector.ui.util.sql

import com.chimerapps.storageinspector.ui.util.safeRunWriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.LocalTimeCounter
import com.intellij.util.textCompletion.TextCompletionUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.WeakHashMap
import javax.swing.JPanel

/**
 * @author Nicola Verbeeck
 */
class SQLViewer private constructor(val project: Project) : JPanel(BorderLayout()) {

    companion object {
        private val projectEditors = WeakHashMap<Project, SQLViewer>()

        operator fun invoke(project: Project): SQLViewer {
            return projectEditors.getOrPut(project) { SQLViewer(project) }
        }
    }

    private val document : Document

    init {
        val factory = PsiFileFactory.getInstance(project)

        val stamp = LocalTimeCounter.currentTime()
        val psiFile = factory.createFileFromText("Dummy.sql", SQLFileType, "", stamp, true, false)
        psiFile.putUserData(SQLFile.preventInjectSchema, true)
        document = PsiDocumentManager.getInstance(project).getDocument(psiFile)!!
    }

    private val editor = (EditorFactory.getInstance().createEditor(document, project, SQLFileType, true)).also {
        Disposer.register(project) {
            safeRunWriteAction {
                EditorFactory.getInstance().releaseEditor(it)
            }
        }
        
    }

    var text: String
        get() = document.text.trim()
        set(value) = safeRunWriteAction {
            CommandProcessor.getInstance().executeCommand(project, Runnable {
                document.replaceString(0, document.textLength, value)
                editor.caretModel.moveToOffset(0)
            }, null, null, UndoConfirmationPolicy.DEFAULT, document)
        }

    init {
        (document as? DocumentImpl)?.setAcceptSlashR(true)

        add(JBScrollPane(editor.component), BorderLayout.CENTER)
    }

}
