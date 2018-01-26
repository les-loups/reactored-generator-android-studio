package io.intheloup.reactored.generator.model

import com.google.common.base.CaseFormat

data class Screen(
        val packageName: String,
        val activityName: String,
        val reactorName: String,
        val stateName: String,
        val flashName: String,
        val actionName: String,
        val viewName: String,
        val layoutName: String,
        val includesLayout: Boolean
) {
    companion object {
        fun build(screenName: String, includesLayout: Boolean): Screen {
            val clsName = screenName.capitalize()
            val packageName = screenName.toLowerCase()
            val layoutName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, screenName)
            val stateName = "${clsName}State"

            return Screen(
                    packageName,
                    "${clsName}Activity",
                    "${clsName}Reactor",
                    stateName,
                    "${stateName}.Flash",
                    "${clsName}Action",
                    "${clsName}View",
                    layoutName,
                    includesLayout
            )
        }
    }
}