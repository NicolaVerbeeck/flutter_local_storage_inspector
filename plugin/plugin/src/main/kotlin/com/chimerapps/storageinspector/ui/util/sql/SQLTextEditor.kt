package com.chimerapps.storageinspector.ui.util.sql

import com.chimerapps.storageinspector.ui.util.safeRunWriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.DocumentImpl
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
class SQLTextEditor private constructor(val project: Project) : JPanel(BorderLayout()) {

    companion object {
        private val projectEditors = WeakHashMap<Project, SQLTextEditor>()

        operator fun invoke(project: Project): SQLTextEditor {
            return projectEditors.getOrPut(project) { SQLTextEditor(project) }
        }
    }

    private val document : Document

    init {
        val factory = PsiFileFactory.getInstance(project)

        val stamp = LocalTimeCounter.currentTime()
        val psiFile = factory.createFileFromText("Dummy.sql", LocalStorageInspectorSQLFileType, "", stamp, true, false)
        document = PsiDocumentManager.getInstance(project).getDocument(psiFile)!!
    }

    private val editor = (EditorFactory.getInstance().createEditor(document, project, LocalStorageInspectorSQLFileType, false) as EditorEx).also {
        Disposer.register(project) {
            safeRunWriteAction {
                EditorFactory.getInstance().releaseEditor(it)
            }
        }
        TextCompletionUtil.installCompletionHint(it)
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

        add(JBScrollPane(editor.component).also {
            it.preferredSize = Dimension(it.preferredSize.width, editor.lineHeight * 3)
        }, BorderLayout.CENTER)
    }

}
