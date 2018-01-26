package io.intheloup.reactored.generator.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiNameHelper
import com.intellij.psi.search.GlobalSearchScope
import io.intheloup.reactored.generator.builder.TemplateBuilder
import io.intheloup.reactored.generator.model.Screen
import io.intheloup.reactored.generator.util.AndroidUtils
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.jvm.KotlinJavaPsiFacade

class NewScreenAction : AnAction() {

    private object Strings {
        object Dialog {
            const val Title = "New Reactor Screen"
        }
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: throw IllegalStateException("Cannot find current project")

        val dialog = Messages.showInputDialogWithCheckBox(
                "Screen name",
                "New Reactor Screen",
                "Include layout file",
                true,
                true,
                null,
                null,
                SimpleClassNameInputValidator(project)
        )

        if (dialog.first?.isBlank() != false) {
            return
        }

        val screen = Screen.build(dialog.first.capitalize(), dialog.second)

        val reactoredPackage = KotlinJavaPsiFacade.getInstance(project).findPackage("io.intheloup.reactored", GlobalSearchScope.allScope(project))
        if (reactoredPackage == null) {
            Messages.showErrorDialog("Cannot find Reactored library", Strings.Dialog.Title)
            return
        }

        val directory = event.getData(LangDataKeys.PSI_ELEMENT) as PsiDirectory
        if (directory.findSubdirectory(screen.packageName) != null) {
            Messages.showErrorDialog("Package ${screen.packageName} already exists", Strings.Dialog.Title)
            return
        }


        val facet = AndroidUtils.getFacet(project)
        val manifest = AndroidUtils.getManifest(facet)

        if (AndroidUtils.manifestHasActivity(manifest, screen.activityName)) {
            Messages.showErrorDialog("Activity ${screen.activityName} already exists in the AndroidManifest.xml", Strings.Dialog.Title)
            return
        }

        WriteCommandAction.runWriteCommandAction(event.project!!) {
            val screenDirectory = directory.createSubdirectory(screen.packageName)
            TemplateBuilder.build(screen, project, screenDirectory, reactoredPackage)

            val activityFile = screenDirectory.findFile("${screen.activityName}.kt")
                    ?: throw IllegalStateException("Cannot register ${screen.activityName}.kt in AndroidManifest.xml")
            AndroidUtils.registerActivityToManifest(manifest, activityFile as KtFile)
        }
    }

    class SimpleClassNameInputValidator(private val project: Project) : InputValidatorEx {
        override fun checkInput(input: String): Boolean {
            return getErrorText(input) == null
        }

        override fun canClose(p0: String?): Boolean {
            return true
        }

        override fun getErrorText(input: String): String? {
            if (!PsiNameHelper.getInstance(project).isQualifiedName(input) || input.contains(".")) {
                return "Name must be a valid simple class name"
            }

            return null
        }
    }
}