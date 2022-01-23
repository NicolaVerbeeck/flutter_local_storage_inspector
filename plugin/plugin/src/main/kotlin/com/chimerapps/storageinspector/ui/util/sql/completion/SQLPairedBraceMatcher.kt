package com.chimerapps.storageinspector.ui.util.sql.completion

import com.alecstrong.sql.psi.core.psi.SqlTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class SQLPairedBraceMatcher : PairedBraceMatcher {

  private val bracePairs = arrayOf(BracePair(SqlTypes.LP, SqlTypes.RP, false))

  override fun getPairs() = bracePairs

  override fun isPairedBracesAllowedBeforeType(
    lbraceType: IElementType,
    contextType: IElementType?
  ) = true

  override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int) = openingBraceOffset
}