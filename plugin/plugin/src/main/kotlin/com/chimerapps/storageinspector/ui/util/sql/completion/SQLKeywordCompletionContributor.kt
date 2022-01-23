package com.chimerapps.storageinspector.ui.util.sql.completion

import com.alecstrong.sql.psi.core.DialectPreset
import com.alecstrong.sql.psi.core.psi.SqlStmtList
import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.chimerapps.storageinspector.ui.util.sql.SQLLanguage
import com.intellij.codeInsight.completion.AddSpaceInsertHandler
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext.DUMMY_IDENTIFIER
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.parser.GeneratedParserUtilBase
import com.intellij.lang.parser.GeneratedParserUtilBase.COMPLETION_STATE_KEY
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.source.tree.TreeUtil
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext

class SQLKeywordCompletionContributor : CompletionContributor() {

  init {
    extend(CompletionType.BASIC, psiElement(), SqliteCompletionProvider())
  }

  class SqliteCompletionProvider : CompletionProvider<CompletionParameters>() {

    private val dialectKey = Key.create<DialectPreset>("dialect")

    override fun addCompletions(
      parameters: CompletionParameters,
      context: ProcessingContext,
      result: CompletionResultSet
    ) {
      val originalFile = parameters.originalFile
      val project = originalFile.project

      val dialect = context.get(dialectKey) ?: run {
        DialectPreset.SQLITE_3_25
          .also {
            context.put(dialectKey, it)
          }
      }
      dialect.setup()

      val position = parameters.position
      if (position.node.elementType == SqlTypes.COMMENT) {
        return
      }
      val prevSibling = position.parent.prevSibling
      if (prevSibling is ASTNode && prevSibling.elementType == SqlTypes.DOT) {
        return
      }
      val stmtList = PsiTreeUtil.getParentOfType(position, SqlStmtList::class.java)

      val positionStartOffset = position.textRange.startOffset
      val range = TextRange(stmtList?.textRange?.startOffset ?: 0, positionStartOffset)
      val text = if (range.isEmpty) DUMMY_IDENTIFIER else range.substring(originalFile.text)
      val tempFile = PsiFileFactory.getInstance(project)
        .createFileFromText("temp.sql", SQLLanguage, text, true, false)
      val completionOffset = positionStartOffset - range.startOffset
      val state = CompletionState(completionOffset)

      tempFile.putUserData(COMPLETION_STATE_KEY, state)
      TreeUtil.ensureParsed(tempFile.node)
      state.items.forEach {
        val bold = LookupElementBuilder.create(it)
          .bold()
          .withInsertHandler(AddSpaceInsertHandler.INSTANCE)
        result
          .caseInsensitive()
          .addElement(bold)
      }
    }

    class CompletionState(completionOffset: Int) : GeneratedParserUtilBase.CompletionState(completionOffset) {
      private val ignoredKeywords = setOf(
        ";", "=", "(", ")", ".", ",", "+", "-", "~", ">>", "<<", "<", ">", "<=", ">=", "==", "!=",
        "<>", "*", "/", "%", "&", "|", "||", "TRUE", "FALSE", "E", "ROWID", "id", "digit", "javadoc",
        "string", "0x"
      )

      override fun convertItem(o: Any?): String? {
        val converted = super.convertItem(o)
        return if (converted !in ignoredKeywords) converted else null
      }

      override fun addItem(builder: PsiBuilder, text: String) {
        val error = findReportedError(builder)
        if (error < 0 || error != builder.rawTokenIndex()) {
          super.addItem(builder, text)
        }
      }

      private fun findReportedError(builder: PsiBuilder): Int {
        var frame: GeneratedParserUtilBase.Frame? = GeneratedParserUtilBase.ErrorState.get(builder).currentFrame
        while (frame != null) {
          if (frame.errorReportedAt != -1) {
            return frame.errorReportedAt
          }
          frame = frame.parentFrame
        }
        return -1
      }
    }
  }
}