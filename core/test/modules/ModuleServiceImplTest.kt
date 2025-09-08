package modules

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.models.domain.Module
import scalereal.core.modules.MainModules
import scalereal.core.modules.ModuleRepository
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules

class ModuleServiceImplTest : StringSpec() {
    private val moduleRepository = mockk<ModuleRepository>()
    private val moduleService = ModuleService(moduleRepository)

    init {
        "should fetch list of modules in sorted order" {
            val modules =
                listOf(
                    Module(
                        moduleId = 2,
                        moduleName = "Settings",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 3,
                        moduleName = "Analytics",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 4,
                        moduleName = "Integrations",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 5,
                        moduleName = "Departments",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 6,
                        moduleName = "Feedback",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 7,
                        moduleName = "Teams",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 8,
                        moduleName = "Designations",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 9,
                        moduleName = "Roles & Permissions",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 10,
                        moduleName = "Employees",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 11,
                        moduleName = "KRAs",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 12,
                        moduleName = "KPIs",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 13,
                        moduleName = "Reviews for Team Members",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 14,
                        moduleName = "Check-in with Team Members",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 15,
                        moduleName = "Review Cycles",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 16,
                        moduleName = "Company Information",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 17,
                        moduleName = "User Activity Log",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 18,
                        moduleName = "Performance Review",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 19,
                        moduleName = "Received Suggestions",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 20,
                        moduleName = "Tutorial Videos",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 21,
                        moduleName = "Team Goals",
                        mainModule = "Goals",
                    ),
                )
            val expectedModules =
                listOf(
                    Module(
                        moduleId = 19,
                        moduleName = "Received",
                        mainModule = MainModules.SUGGESTION_BOX.moduleName,
                    ),
                    Module(
                        moduleId = 21,
                        moduleName = Modules.TEAM_GOALS.moduleName,
                        mainModule = MainModules.GOALS.moduleName,
                    ),
                    Module(
                        moduleId = 6,
                        moduleName = Modules.EMPLOYEE_FEEDBACK.moduleName,
                        mainModule = MainModules.REPORTS.moduleName,
                    ),
                    Module(
                        moduleId = 18,
                        moduleName = Modules.PERFORMANCE_REVIEWS.moduleName,
                        mainModule = MainModules.REPORTS.moduleName,
                    ),
                    Module(
                        moduleId = 3,
                        moduleName = Modules.ANALYTICS.moduleName,
                        mainModule = MainModules.REPORTS.moduleName,
                    ),
                    Module(
                        moduleId = 5,
                        moduleName = Modules.DEPARTMENTS.moduleName,
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 7,
                        moduleName = Modules.TEAMS.moduleName,
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 8,
                        moduleName = Modules.DESIGNATIONS.moduleName,
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 9,
                        moduleName = Modules.ROLES_AND_PERMISSIONS.moduleName,
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 10,
                        moduleName = Modules.EMPLOYEES.moduleName,
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 11,
                        moduleName = Modules.KRAs.moduleName,
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 12,
                        moduleName = Modules.KPIs.moduleName,
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 13,
                        moduleName = Modules.REVIEW_FOR_TEAM.moduleName,
                        mainModule = MainModules.REVIEW_TIMELINE.moduleName,
                    ),
                    Module(
                        moduleId = 14,
                        moduleName = Modules.CHECK_IN_WITH_TEAM.moduleName,
                        mainModule = MainModules.REVIEW_TIMELINE.moduleName,
                    ),
                    Module(
                        moduleId = 15,
                        moduleName = Modules.REVIEW_CYCLE.moduleName,
                        mainModule = MainModules.REVIEW_TIMELINE.moduleName,
                    ),
                    Module(
                        moduleId = 16,
                        moduleName = Modules.COMPANY_INFORMATION.moduleName,
                        mainModule = MainModules.CONFIGURATION.moduleName,
                    ),
                    Module(
                        moduleId = 2,
                        moduleName = Modules.SETTINGS.moduleName,
                        mainModule = MainModules.CONFIGURATION.moduleName,
                    ),
                    Module(
                        moduleId = 4,
                        moduleName = Modules.INTEGRATIONS.moduleName,
                        mainModule = MainModules.CONFIGURATION.moduleName,
                    ),
                    Module(
                        moduleId = 17,
                        moduleName = "User Activity Log",
                        mainModule = null,
                    ),
                    Module(
                        moduleId = 20,
                        moduleName = Modules.TUTORIAL_VIDEOS.moduleName,
                        mainModule = MainModules.HELP_AND_TRAINING.moduleName,
                    ),
                )
            every { moduleRepository.fetch() } returns modules
            moduleService.fetch() shouldBe expectedModules
            verify { moduleRepository.fetch() }
        }

        "should fetch module id by module name" {
            every { moduleRepository.fetchModuleId(any()) } returns 18
            moduleService.fetchModuleId(Modules.RECEIVED_SUGGESTIONS.moduleName) shouldBe 18
            verify { moduleRepository.fetchModuleId(Modules.RECEIVED_SUGGESTIONS.moduleName) }
        }
    }
}
