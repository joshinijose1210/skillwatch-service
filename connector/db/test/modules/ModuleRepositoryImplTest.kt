package modules

import io.kotest.core.spec.Spec
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import norm.executeCommand
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.db.modules.ModuleRepositoryImpl
import util.StringSpecWithDataSource

class ModuleRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var moduleRepositoryImpl: ModuleRepositoryImpl
    private lateinit var moduleService: ModuleService

    init {

        "should fetch list of modules" {
            val expectedModuleNames =
                listOf(
                    Modules.SETTINGS.moduleName,
                    Modules.ANALYTICS.moduleName,
                    Modules.INTEGRATIONS.moduleName,
                    Modules.DEPARTMENTS.moduleName,
                    Modules.EMPLOYEE_FEEDBACK.moduleName,
                    Modules.TEAMS.moduleName,
                    Modules.DESIGNATIONS.moduleName,
                    Modules.ROLES_AND_PERMISSIONS.moduleName,
                    Modules.EMPLOYEES.moduleName,
                    Modules.KRAs.moduleName,
                    Modules.KPIs.moduleName,
                    Modules.REVIEW_FOR_TEAM.moduleName,
                    Modules.CHECK_IN_WITH_TEAM.moduleName,
                    Modules.REVIEW_CYCLE.moduleName,
                    Modules.COMPANY_INFORMATION.moduleName,
                    Modules.USER_ACTIVITY_LOG.moduleName,
                    Modules.PERFORMANCE_REVIEWS.moduleName,
                    Modules.RECEIVED_SUGGESTIONS.moduleName,
                    Modules.TUTORIAL_VIDEOS.moduleName,
                    Modules.TEAM_GOALS.moduleName,
                )

            val actualModules = moduleRepositoryImpl.fetch()
            val actualModuleNames = actualModules.map { it.moduleName }

            actualModuleNames.size shouldBe expectedModuleNames.size
            actualModuleNames.containsAll(expectedModuleNames) shouldBe true
        }

        "should fetch module id by module name" {
            moduleRepositoryImpl.fetchModuleId(moduleName = Modules.RECEIVED_SUGGESTIONS.moduleName) shouldBeInRange (1..18)
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        dataSource.connection.use {
            it.executeCommand(
                """
                INSERT INTO modules (name)
                  SELECT new_module
                    FROM (VALUES 
                    ('Feedback'),  
                    ('Teams'),
                    ('Designations'),
                    ('Roles & Permissions'),
                    ('Employees'),
                    ('KRAs'),
                    ('KPIs'),
                    ('Reviews for Team Members'),
                    ('Check-in with Team Members'),
                    ('Review Cycles'),
                    ('Company Information'),
                    ('User Activity Log'),
                    ('Performance Review'),
                    ('Settings'),
                    ('Analytics'),
                    ('Integrations'),
                    ('Departments'),
                    ('Received Suggestions'),
                    ('Tutorial Videos'),
                    ('Team Goals')) AS new_modules(new_module)
                    LEFT JOIN modules ON new_modules.new_module = modules.name
                    WHERE modules.name IS NULL;
                """.trimIndent(),
            )
        }
        moduleRepositoryImpl = ModuleRepositoryImpl(dataSource)
        moduleService = ModuleService(moduleRepositoryImpl)
    }
}
