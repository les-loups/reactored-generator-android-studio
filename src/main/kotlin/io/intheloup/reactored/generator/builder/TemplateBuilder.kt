package io.intheloup.reactored.generator.builder

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiPackage
import io.intheloup.reactored.generator.model.Screen
import org.jetbrains.kotlin.idea.core.getPackage

object TemplateBuilder {

    enum class Template {
        Activity, Reactor, Action, State, View, Controller
    }

    private object Properties {
        const val ScreenPackage = "SCREEN_PACKAGE"
        const val ReactoredPackage = "REACTORED_PACKAGE"
        const val ActivityName = "ACTIVITY_NAME"
        const val ReactorName = "REACTOR_NAME"
        const val ActionName = "ACTION_NAME"
        const val StateName = "STATE_NAME"
        const val FlashName = "FLASH_NAME"
        const val FlashSubclassName = "FLASH_SUBCLASS_NAME"
        const val ViewName = "VIEW_NAME"
        const val ControllerName = "CONTROLLER_NAME"
    }

    fun build(screen: Screen, project: Project, destinationDirectory: PsiDirectory, reactoredPackaged: PsiPackage) {
        val manager = FileTemplateManager.getInstance(project)
        val properties = buildProperties(manager.defaultProperties, screen, destinationDirectory, reactoredPackaged)

        mapTemplates(screen).forEach { template ->
            val fileTemplate = manager.getInternalTemplate(template.key.name)
            FileTemplateUtil.createFromTemplate(fileTemplate, "${template.value}.kt", properties, destinationDirectory)
        }
    }

    private fun mapTemplates(screen: Screen) = Template.values().associate {
        when (it) {
            Template.Activity -> Pair(it, screen.activityName)
            Template.Reactor -> Pair(it, screen.reactorName)
            Template.Action -> Pair(it, screen.actionName)
            Template.State -> Pair(it, screen.stateName)
            Template.View -> Pair(it, screen.viewName)
            Template.Controller -> Pair(it, screen.controllerName)
        }
    }

    private fun buildProperties(properties: java.util.Properties, screen: Screen, destinationDirectory: PsiDirectory, reactoredPackaged: PsiPackage) = properties.apply {
        put(Properties.ScreenPackage, destinationDirectory.getPackage()?.qualifiedName
                ?: throw IllegalStateException("Invalid destination directory"))
        put(Properties.ReactoredPackage, reactoredPackaged.qualifiedName)
        put(Properties.ActivityName, screen.activityName)
        put(Properties.ReactorName, screen.reactorName)
        put(Properties.ActionName, screen.actionName)
        put(Properties.StateName, screen.stateName)
        put(Properties.FlashName, screen.flashName)
        put(Properties.FlashSubclassName, screen.flashName.substringAfterLast(".", screen.flashName))
        put(Properties.ViewName, screen.viewName)
        put(Properties.ControllerName, screen.controllerName)
    }

}