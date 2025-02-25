// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.quickfix

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.idea.base.resources.KotlinBundle
import org.jetbrains.kotlin.idea.codeinsight.api.classic.quickfixes.KotlinQuickFixAction
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtReturnExpression

class RemoveReturnLabelFix(element: KtReturnExpression, private val labelName: String) : KotlinQuickFixAction<KtReturnExpression>(element) {
    override fun invoke(project: Project, editor: Editor?, file: KtFile) {
        element?.labeledExpression?.delete()
    }

    override fun getFamilyName(): String = KotlinBundle.message("remove.return.label.fix.family")

    override fun getText(): String = KotlinBundle.message("remove.return.label.fix.text", labelName)

    companion object : KotlinSingleIntentionActionFactory() {
        override fun createAction(diagnostic: Diagnostic): KotlinQuickFixAction<KtReturnExpression>? {
            val returnExpression = diagnostic.psiElement as? KtReturnExpression ?: return null
            val labelName = returnExpression.getLabelName() ?: return null
            return RemoveReturnLabelFix(returnExpression, labelName)
        }
    }
}