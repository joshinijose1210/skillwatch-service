package scalereal.core.roles

import jakarta.inject.Singleton
import scalereal.core.models.domain.ModulePermission
import scalereal.core.models.domain.Permission
import scalereal.core.models.domain.Role
import scalereal.core.models.domain.UserActivityData
import scalereal.core.models.removeExtraSpaces
import scalereal.core.modules.ModuleRepository
import scalereal.core.modules.Modules
import scalereal.core.userActivity.UserActivityRepository
import java.lang.Exception

@Singleton
class RoleService(
    private val repository: RoleRepository,
    private val userActivityRepository: UserActivityRepository,
    private val moduleRepository: ModuleRepository,
) {
    private val rolesModuleId = moduleRepository.fetchModuleId(Modules.ROLES_AND_PERMISSIONS.moduleName)

    fun create(
        organisationId: Long,
        roleName: String,
        modulePermission: List<ModulePermission>,
        status: Boolean,
        userActivityData: UserActivityData,
    ) {
        val name = roleName.removeExtraSpaces()
        val isRole = repository.isRoleExists(organisationId = organisationId, roleName = name)
        if (isRole.exists) {
            throw Exception("Role $name already exists")
        } else {
            val maxRoleId = repository.getMaxRoleId(organisationId)
            repository.create(organisationId, roleId = maxRoleId + 1, name, modulePermission, status)

            if (status) {
                addUserActivityLog(
                    userActivityData,
                    "Role R${maxRoleId + 1} Added and Published",
                    "Role R${maxRoleId + 1} Added and Published",
                )
            } else {
                addUserActivityLog(
                    userActivityData,
                    "Role R${maxRoleId + 1} Added and Unpublished",
                    "Role R${maxRoleId + 1} Added and Unpublished",
                )
            }
        }
    }

    fun fetchAll(
        organisationId: Long,
        searchText: String,
        page: Int,
        limit: Int,
    ): List<Role> =
        repository
            .fetchAll(
                organisationId = organisationId,
                searchText = searchText,
                offset = (page - 1) * limit,
                limit = limit,
            ).map {
                Role(
                    organisationId = it.organisationId,
                    id = it.id,
                    roleId = "R${it.roleId}",
                    roleName = it.roleName,
                    modulePermission = repository.fetchPermissions(it.id),
                    status = it.status,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            }

    fun count(
        organisationId: Long,
        searchText: String,
    ): Int = repository.count(organisationId = organisationId, searchText = searchText)

    fun update(
        organisationId: Long,
        id: Long,
        roleName: String,
        modulePermission: List<ModulePermission>,
        status: Boolean,
        userActivityData: UserActivityData,
    ) {
        val sortedModulePermissions = modulePermission.sortedBy { it.moduleId }
        val roleData = repository.getRoleDataById(id, organisationId)
        val modulePermissionsData = repository.fetchPermissions(roleId = id).sortedBy { it.moduleId }
        /** Permissions and status of the first default role with all permissions cannot be updated. **/
        if (roleData.roleId == 1L &&
            (sortedModulePermissions != modulePermissionsData || roleData.status != status || roleData.roleName != roleName)
        ) {
            throw Exception("This role cannot be updated.")
        }
        val name = roleName.removeExtraSpaces()
        val activity =
            when {
                (roleData.roleName != name || sortedModulePermissions != modulePermissionsData) && roleData.status && !status ->
                    "Role R${roleData.roleId} Edited and Unpublished"
                (roleData.roleName != name || sortedModulePermissions != modulePermissionsData) && !roleData.status && status ->
                    "Role R${roleData.roleId} Edited and Published"
                roleData.status && !status ->
                    "Role R${roleData.roleId} Unpublished"
                !roleData.status && status ->
                    "Role R${roleData.roleId} Published"
                roleData.roleName != name || sortedModulePermissions != modulePermissionsData ->
                    "Role R${roleData.roleId} Edited"
                else -> null
            }
        try {
            repository.update(organisationId = organisationId, id = id, roleName = name, status = status)
            if (isPermissionUpdationNeeded(id, modulePermission)) {
                repository.deletePermission(id)
                repository.createPermissions(id, modulePermission)
            }
            repository.updatePermissions(id, modulePermission)
            if (activity != null) addUserActivityLog(userActivityData, activity = activity, description = activity)
        } catch (_: Exception) {
            throw Exception("Role $name already exists")
        }
    }

    private fun isPermissionUpdationNeeded(
        id: Long,
        modulePermissions: List<ModulePermission>,
    ): Boolean =
        (
            repository.fetchPermissions(id).map { mappedPermission -> mappedPermission.moduleId } !=
                modulePermissions.map { modulePermission -> modulePermission.moduleId }
        )

    fun hasPermission(
        organisationId: Long,
        roleName: MutableCollection<String>,
        moduleName: String,
    ): Permission = repository.hasPermission(organisationId, roleName, moduleName)

    fun createDefaultRoles(organisationId: Long) {
        val modulePermission =
            ModulePermission(
                moduleId = 1,
                moduleName = "Module Name",
                view = true,
                edit = true,
            )
        val modules = moduleRepository.fetch()
        val moduleMap = modules.associateBy { it.moduleName }
        val employeeFeedbackModuleId = moduleMap[Modules.EMPLOYEE_FEEDBACK.moduleName]?.moduleId
        val teamsModuleId = moduleMap[Modules.TEAMS.moduleName]?.moduleId
        val designationsModuleId = moduleMap[Modules.DESIGNATIONS.moduleName]?.moduleId
        val rolesAndPermissionsModuleId = moduleMap[Modules.ROLES_AND_PERMISSIONS.moduleName]?.moduleId
        val employeesModuleId = moduleMap[Modules.EMPLOYEES.moduleName]?.moduleId
        val krasModuleId = moduleMap[Modules.KRAs.moduleName]?.moduleId
        val kpisModuleId = moduleMap[Modules.KPIs.moduleName]?.moduleId
        val reviewCycleModuleId = moduleMap[Modules.REVIEW_CYCLE.moduleName]?.moduleId
        val employeeReviewsModuleId = moduleMap[Modules.PERFORMANCE_REVIEWS.moduleName]?.moduleId
        val reviewForTeamModuleId = moduleMap[Modules.REVIEW_FOR_TEAM.moduleName]?.moduleId
        val checkInWithTeamModuleId = moduleMap[Modules.CHECK_IN_WITH_TEAM.moduleName]?.moduleId
        val userActivityLogModuleId = moduleMap[Modules.USER_ACTIVITY_LOG.moduleName]?.moduleId
        val companyInformationModuleId = moduleMap[Modules.COMPANY_INFORMATION.moduleName]?.moduleId
        val allowedDomainsModuleId = moduleMap[Modules.SETTINGS.moduleName]?.moduleId
        val integrationsModuleId = moduleMap[Modules.INTEGRATIONS.moduleName]?.moduleId
        val analyticsModuleId = moduleMap[Modules.ANALYTICS.moduleName]?.moduleId
        val departmentsModuleId = moduleMap[Modules.DEPARTMENTS.moduleName]?.moduleId
        val suggestionsModuleId = moduleMap[Modules.RECEIVED_SUGGESTIONS.moduleName]?.moduleId
        val tutorialVideosModuleId = moduleMap[Modules.TUTORIAL_VIDEOS.moduleName]?.moduleId
        val goalsModuleId = moduleMap[Modules.TEAM_GOALS.moduleName]?.moduleId

        val roles: List<String> = listOf(Roles.ORG_ADMIN, Roles.HUMAN_RESOURCE, Roles.MANAGER, Roles.EMPLOYEE)

        val orgAdminPermission =
            listOf(
                modulePermission.copy(moduleId = employeeFeedbackModuleId, edit = false),
                modulePermission.copy(moduleId = teamsModuleId),
                modulePermission.copy(moduleId = designationsModuleId),
                modulePermission.copy(moduleId = rolesAndPermissionsModuleId),
                modulePermission.copy(moduleId = employeesModuleId),
                modulePermission.copy(moduleId = krasModuleId),
                modulePermission.copy(moduleId = kpisModuleId),
                modulePermission.copy(moduleId = reviewCycleModuleId),
                modulePermission.copy(moduleId = employeeReviewsModuleId, edit = false),
                modulePermission.copy(moduleId = reviewForTeamModuleId),
                modulePermission.copy(moduleId = checkInWithTeamModuleId),
                modulePermission.copy(moduleId = userActivityLogModuleId, edit = false),
                modulePermission.copy(moduleId = companyInformationModuleId),
                modulePermission.copy(moduleId = allowedDomainsModuleId),
                modulePermission.copy(moduleId = integrationsModuleId),
                modulePermission.copy(moduleId = analyticsModuleId),
                modulePermission.copy(moduleId = departmentsModuleId),
                modulePermission.copy(moduleId = suggestionsModuleId),
                modulePermission.copy(moduleId = tutorialVideosModuleId, edit = false),
                modulePermission.copy(moduleId = goalsModuleId),
            )
        val humanResourcePermission =
            listOf(
                modulePermission.copy(moduleId = employeeFeedbackModuleId, edit = false),
                modulePermission.copy(moduleId = teamsModuleId),
                modulePermission.copy(moduleId = designationsModuleId),
                modulePermission.copy(moduleId = rolesAndPermissionsModuleId, edit = false),
                modulePermission.copy(moduleId = employeesModuleId),
                modulePermission.copy(moduleId = krasModuleId),
                modulePermission.copy(moduleId = kpisModuleId),
                modulePermission.copy(moduleId = reviewCycleModuleId),
                modulePermission.copy(moduleId = employeeReviewsModuleId, edit = false),
                modulePermission.copy(moduleId = reviewForTeamModuleId),
                modulePermission.copy(moduleId = checkInWithTeamModuleId),
                modulePermission.copy(moduleId = userActivityLogModuleId, edit = false),
                modulePermission.copy(moduleId = companyInformationModuleId),
                modulePermission.copy(moduleId = allowedDomainsModuleId, edit = false),
                modulePermission.copy(moduleId = integrationsModuleId, edit = false),
                modulePermission.copy(moduleId = analyticsModuleId, edit = false),
                modulePermission.copy(moduleId = departmentsModuleId),
                modulePermission.copy(moduleId = suggestionsModuleId),
                modulePermission.copy(moduleId = tutorialVideosModuleId, edit = false),
                modulePermission.copy(moduleId = goalsModuleId),
            )
        val managerPermission =
            listOf(
                modulePermission.copy(moduleId = employeeFeedbackModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = teamsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = designationsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = rolesAndPermissionsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = employeesModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = krasModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = kpisModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = reviewCycleModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = employeeReviewsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = reviewForTeamModuleId),
                modulePermission.copy(moduleId = checkInWithTeamModuleId),
                modulePermission.copy(moduleId = userActivityLogModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = companyInformationModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = allowedDomainsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = integrationsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = analyticsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = departmentsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = suggestionsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = tutorialVideosModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = goalsModuleId),
            )
        val employeePermission =
            listOf(
                modulePermission.copy(moduleId = employeeFeedbackModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = teamsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = designationsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = rolesAndPermissionsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = employeesModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = krasModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = kpisModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = reviewCycleModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = employeeReviewsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = reviewForTeamModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = checkInWithTeamModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = userActivityLogModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = companyInformationModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = allowedDomainsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = integrationsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = analyticsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = departmentsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = suggestionsModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = tutorialVideosModuleId, view = false, edit = false),
                modulePermission.copy(moduleId = goalsModuleId, view = false, edit = false),
            )

        roles.map { role ->
            if (!repository.isRoleExists(organisationId = organisationId, role).exists) {
                val maxRoleId = repository.getMaxRoleId(organisationId)
                when (role) {
                    Roles.ORG_ADMIN ->
                        repository.create(
                            organisationId = organisationId,
                            roleId = maxRoleId + 1,
                            roleName = role,
                            modulePermission = orgAdminPermission,
                            status = true,
                        )
                    Roles.HUMAN_RESOURCE ->
                        repository.create(
                            organisationId = organisationId,
                            roleId = maxRoleId + 1,
                            roleName = role,
                            modulePermission = humanResourcePermission,
                            status = true,
                        )
                    Roles.MANAGER ->
                        repository.create(
                            organisationId = organisationId,
                            roleId = maxRoleId + 1,
                            roleName = role,
                            modulePermission = managerPermission,
                            status = true,
                        )
                    Roles.EMPLOYEE ->
                        repository.create(
                            organisationId = organisationId,
                            roleId = maxRoleId + 1,
                            roleName = role,
                            modulePermission = employeePermission,
                            status = true,
                        )
                }
            }
        }
    }

    private fun addUserActivityLog(
        userActivityData: UserActivityData,
        activity: String,
        description: String,
    ) {
        userActivityRepository.addActivity(
            actionBy = userActivityData.actionBy,
            moduleId = rolesModuleId,
            activity = activity,
            description = description,
            ipAddress = userActivityData.ipAddress,
        )
    }
}
