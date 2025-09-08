package scalereal.core.modules

import jakarta.inject.Singleton
import scalereal.core.models.domain.Module

@Singleton
class ModuleService(
    private val repository: ModuleRepository,
) {
    fun fetch(): List<Module> {
        val modules = repository.fetch()
        val sortedModules =
            modules.sortedBy { module ->
                Modules.values().indexOfFirst {
                    it.moduleName == module.moduleName
                }
            }
        sortedModules.forEach { module ->
            when (module.moduleName) {
                Modules.TUTORIAL_VIDEOS.moduleName -> {
                    module.mainModule = MainModules.HELP_AND_TRAINING.moduleName
                }
                Modules.RECEIVED_SUGGESTIONS.moduleName -> {
                    module.mainModule = MainModules.SUGGESTION_BOX.moduleName
                    module.moduleName = "Received"
                }
                Modules.TEAM_GOALS.moduleName -> {
                    module.mainModule = MainModules.GOALS.moduleName
                }
                Modules.EMPLOYEE_FEEDBACK.moduleName -> {
                    module.mainModule = MainModules.REPORTS.moduleName
                }
                Modules.PERFORMANCE_REVIEWS.moduleName -> {
                    module.mainModule = MainModules.REPORTS.moduleName
                }
                Modules.ANALYTICS.moduleName -> {
                    module.mainModule = MainModules.REPORTS.moduleName
                }
                Modules.REVIEW_FOR_TEAM.moduleName -> {
                    module.mainModule = MainModules.REVIEW_TIMELINE.moduleName
                }
                Modules.CHECK_IN_WITH_TEAM.moduleName -> {
                    module.mainModule = MainModules.REVIEW_TIMELINE.moduleName
                }
                Modules.REVIEW_CYCLE.moduleName -> {
                    module.mainModule = MainModules.REVIEW_TIMELINE.moduleName
                }
                Modules.COMPANY_INFORMATION.moduleName -> {
                    module.mainModule = MainModules.CONFIGURATION.moduleName
                }
                Modules.SETTINGS.moduleName -> {
                    module.mainModule = MainModules.CONFIGURATION.moduleName
                }
                Modules.INTEGRATIONS.moduleName -> {
                    module.mainModule = MainModules.CONFIGURATION.moduleName
                }
                else -> {
                    module.mainModule = null
                }
            }
        }
        return sortedModules
    }

    fun fetchModuleId(moduleName: String): Int = repository.fetchModuleId(moduleName)
}
