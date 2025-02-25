// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.liveTemplates.macro

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingAdapter
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.core.overrideImplement.ImplementMembersHandler
import org.jetbrains.kotlin.idea.util.application.runWriteAction
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

internal class AnonymousTemplateEditingListener(private val psiFile: PsiFile, private val editor: Editor) : TemplateEditingAdapter() {
    private var classRef: KtReferenceExpression? = null
    private var classDescriptor: ClassDescriptor? = null

    override fun currentVariableChanged(templateState: TemplateState, template: Template?, oldIndex: Int, newIndex: Int) {
        if (templateState.template == null) return
        val variableRange = templateState.getVariableRange("SUPERTYPE") ?: return
        val name = psiFile.findElementAt(variableRange.startOffset)
        if (name != null && name.parent is KtReferenceExpression) {
            val ref = name.parent as KtReferenceExpression
            val descriptor = ref.analyze(BodyResolveMode.FULL).get(BindingContext.REFERENCE_TARGET, ref)
            if (descriptor is ClassDescriptor) {
                classRef = ref
                classDescriptor = descriptor
            }
        }
    }

    override fun templateFinished(template: Template, brokenOff: Boolean) {
        editor.putUserData(LISTENER_KEY, null)
        if (brokenOff) return

        if (classDescriptor != null) {
            if (classDescriptor!!.kind == ClassKind.CLASS) {
                val placeToInsert = classRef!!.textRange.endOffset

                runWriteAction {
                    PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile)!!.insertString(placeToInsert, "()")
                }

                var hasConstructorsParameters = false
                for (cd in classDescriptor!!.constructors) {
                    // TODO check for visibility
                    hasConstructorsParameters = hasConstructorsParameters or (cd.valueParameters.size != 0)
                }

                if (hasConstructorsParameters) {
                    editor.caretModel.moveToOffset(placeToInsert + 1)
                }
            }

            ImplementMembersHandler().invoke(psiFile.project, editor, psiFile, true)
        }
    }

    companion object {

        private val LISTENER_KEY = Key.create<AnonymousTemplateEditingListener>("kotlin.AnonymousTemplateEditingListener")

        fun registerListener(editor: Editor, project: Project) {
            if (editor.getUserData(LISTENER_KEY) != null) return

            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)!!
            val templateState = TemplateManagerImpl.getTemplateState(editor)
            if (templateState != null) {
                val listener = AnonymousTemplateEditingListener(psiFile, editor)
                editor.putUserData(LISTENER_KEY, listener)
                templateState.addTemplateStateListener(listener)
            }
        }
    }
}
