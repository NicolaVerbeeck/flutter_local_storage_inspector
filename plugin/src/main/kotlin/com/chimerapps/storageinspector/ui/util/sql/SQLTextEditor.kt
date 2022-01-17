package com.chimerapps.storageinspector.ui.util.sql

import com.chimerapps.storageinspector.ui.util.safeRunWriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBScrollPane
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

    private val editor = (EditorFactory.getInstance().createEditor(EditorFactory.getInstance().createDocument(""), project) as EditorImpl).also {
        Disposer.register(project) {
            EditorFactory.getInstance().releaseEditor(it)
        }
        it.highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, SQLFileType)

    }
    private val document = editor.document

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
            it.maximumSize = Dimension(Int.MAX_VALUE, editor.lineHeight * 3)
            it.preferredSize = Dimension(it.preferredSize.width, editor.lineHeight * 3)
        }, BorderLayout.CENTER)
    }

}
