package roles

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.models.domain.Module
import scalereal.core.models.domain.ModulePermission
import scalereal.core.models.domain.Permission
import scalereal.core.models.domain.Role
import scalereal.core.models.domain.RoleData
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.MainModules
import scalereal.core.modules.ModuleRepository
import scalereal.core.modules.Modules
import scalereal.core.roles.RoleRepository
import scalereal.core.roles.RoleService
import scalereal.core.userActivity.UserActivityRepository
import java.sql.Timestamp

class RoleServiceImplTest : StringSpec() {
    private val roleRepository = mockk<RoleRepository>()
    private val moduleRepository =
        mockk<ModuleRepository> {
            every { fetchModuleId(Modules.ROLES_AND_PERMISSIONS.moduleName) } returns 5
        }
    private val userActivityRepository = mockk<UserActivityRepository>()
    private val roleService = RoleService(roleRepository, userActivityRepository, moduleRepository)

    init {
        val modulePermission =
            ModulePermission(
                moduleId = 1,
                moduleName = Modules.USER_ACTIVITY_LOG.moduleName,
                view = true,
                edit = false,
            )

        "should be able create default roles if not exists" {
            val modules =
                listOf(
                    Module(
                        moduleId = 1,
                        moduleName = Modules.REVIEW_CYCLE.moduleName,
                        mainModule = MainModules.REVIEW_TIMELINE.moduleName,
                    ),
                )
            val maxRoleId = 0L
            every { moduleRepository.fetch() } returns modules
            every { roleRepository.isRoleExists(any(), any()).exists } returns false
            every { roleRepository.getMaxRoleId(any()) } returns maxRoleId
            every { roleRepository.create(any(), any(), any(), any(), any()) } returns Unit
            roleService.createDefaultRoles(organisationId = 1)
            verify(exactly = 4) { roleRepository.create(any(), any(), any(), any(), any()) }
        }

        "should throw exception while adding role if role with same name already exists " {
            every { roleRepository.isRoleExists(any(), any()).exists } returns true
            val exception =
                shouldThrow<Exception> {
                    roleService.create(
                        organisationId = 1,
                        roleName = "HR",
                        modulePermission = listOf(modulePermission),
                        status = true,
                        userActivityData = UserActivityData(1, "127.0.0.1"),
                    )
                }
            exception.message shouldBe "Role HR already exists"
        }

        "should add new role in published state" {
            val organisationId = 1L
            val roleName = "Manager"
            val permission = listOf(modulePermission)
            val roleStatus = true
            val maxRoleId = 0L
            every { roleRepository.isRoleExists(any(), any()).exists } returns false
            every { roleRepository.getMaxRoleId(any()) } returns maxRoleId
            every { roleRepository.create(any(), any(), any(), any(), any()) } returns Unit
            every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } returns Unit
            roleService.create(
                organisationId = organisationId,
                roleName = roleName,
                modulePermission = permission,
                status = roleStatus,
                userActivityData = UserActivityData(1, "127.0.0.1"),
            ) shouldBe Unit
            verify {
                roleRepository.create(
                    organisationId = organisationId,
                    roleId = maxRoleId + 1,
                    roleName = roleName,
                    modulePermission = permission,
                    status = roleStatus,
                )
            }
        }

        "should throw exception while adding same role name with case change" {
            val organisationId: Long = 1
            val roleName = "hr"
            val permission = listOf(modulePermission, modulePermission.copy(moduleId = 2, moduleName = "Submitted Feedback", edit = true))
            val status = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            every { roleRepository.isRoleExists(any(), any()).exists } returns true
            every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } returns Unit
            val exception = shouldThrow<Exception> { roleService.create(organisationId, roleName, permission, status, userActivityData) }
            exception.message shouldBe "Role ${roleName.trim()} already exists"
        }

        "should fetch all roles" {
            val permission =
                listOf(
                    modulePermission,
                    modulePermission.copy(moduleId = 2, moduleName = Modules.EMPLOYEE_FEEDBACK.moduleName, edit = true),
                )
            val role =
                listOf(
                    Role(
                        organisationId = 1,
                        id = 1,
                        roleId = "1",
                        roleName = "HR",
                        modulePermission = listOf(),
                        status = true,
                        createdAt = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                        updatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                    ),
                )

            val expectedRole =
                listOf(
                    Role(
                        organisationId = 1,
                        id = 1,
                        roleId = "R1",
                        roleName = "HR",
                        modulePermission = permission,
                        status = true,
                        createdAt = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                        updatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                    ),
                )
            every { roleRepository.fetchAll(any(), any(), any(), any()) } returns role
            every { roleRepository.fetchPermissions(any()) } returns permission
            roleService.fetchAll(organisationId = 1, page = 1, limit = Int.MAX_VALUE, searchText = "") shouldBe expectedRole
            verify { roleRepository.fetchAll(organisationId = 1, offset = 0, limit = Int.MAX_VALUE, searchText = "") }
        }

        "should fetch role count" {
            every { roleRepository.count(any(), any()) } returns 5
            roleService.count(organisationId = 1, searchText = "") shouldBe 5
        }

        "should update role name by roleId and publish it" {
            val organisationId: Long = 1
            val id: Long = 1
            val roleName = "Human Resource"
            val status = true
            val roleData =
                RoleData(
                    id = 1,
                    roleId = 2,
                    roleName = "HR",
                    status = false,
                )
            val modulePermissions = listOf(modulePermission)
            every { roleRepository.getRoleDataById(any(), any()) } returns roleData
            every { roleRepository.fetchPermissions(any()) } returns modulePermissions
            every { roleRepository.update(any(), any(), any(), any()) } returns Unit
            every { roleRepository.updatePermissions(any(), any()) } returns Unit
            roleService.update(
                organisationId = organisationId,
                id = id,
                roleName = roleName,
                modulePermission = modulePermissions,
                status = status,
                userActivityData =
                    UserActivityData(
                        actionBy = 1,
                        ipAddress = "127.0.0.1",
                    ),
            ) shouldBe Unit
            verify { roleRepository.update(organisationId, id, roleName, status) }
        }

        "should throw exception while updating the status of role 1 of an organisation" {
            val organisationId: Long = 1
            val id: Long = 1
            val roleName = "Org Admin"
            val status = false
            val roleData =
                RoleData(
                    id = 1,
                    roleId = 1,
                    roleName = "Org Admin",
                    status = true,
                )
            val modulePermissions = listOf(modulePermission)
            every { roleRepository.getRoleDataById(any(), any()) } returns roleData
            every { roleRepository.fetchPermissions(any()) } returns modulePermissions
            val exception =
                shouldThrow<Exception> {
                    roleService.update(
                        organisationId = organisationId,
                        id = id,
                        roleName = roleName,
                        modulePermission = modulePermissions,
                        status = status,
                        userActivityData =
                            UserActivityData(
                                actionBy = 1,
                                ipAddress = "127.0.0.1",
                            ),
                    )
                }
            exception.message shouldBe "This role cannot be updated."
        }

        "should throw exception while updating the permissions of role 1 of an organisation" {
            val organisationId: Long = 1
            val id: Long = 1
            val roleName = "Org Admin"
            val status = true
            val roleData =
                RoleData(
                    id = 1,
                    roleId = 1,
                    roleName = "Org Admin",
                    status = true,
                )
            val initialModulePermissions = listOf(modulePermission)
            val updatedModulePermissions = listOf(modulePermission.copy(view = false))
            every { roleRepository.getRoleDataById(any(), any()) } returns roleData
            every { roleRepository.fetchPermissions(any()) } returns initialModulePermissions
            val exception =
                shouldThrow<Exception> {
                    roleService.update(
                        organisationId = organisationId,
                        id = id,
                        roleName = roleName,
                        modulePermission = updatedModulePermissions,
                        status = status,
                        userActivityData =
                            UserActivityData(
                                actionBy = 1,
                                ipAddress = "127.0.0.1",
                            ),
                    )
                }
            exception.message shouldBe "This role cannot be updated."
        }

        "should throw exception while Updating role name with existing name" {
            clearMocks(roleRepository)
            val id: Long = 2
            val organisationId: Long = 1
            val roleName = "Manager"
            val permission = listOf(modulePermission, modulePermission.copy(moduleId = 2, moduleName = "Submitted Feedback"))
            val status = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val roleData =
                RoleData(
                    id = 1,
                    roleId = 3,
                    roleName = "Executive",
                    status = true,
                )
            every { roleRepository.getRoleDataById(any(), any()) } returns roleData
            every { roleRepository.fetchPermissions(any()) } returns permission
            val exception =
                shouldThrow<Exception> {
                    roleService.update(
                        organisationId = organisationId,
                        id = id,
                        roleName = roleName,
                        modulePermission = permission,
                        status = status,
                        userActivityData = userActivityData,
                    )
                }
            exception.message shouldBe "Role ${roleName.trim()} already exists"
        }

        "should able to fetch permission by role name and module name" {
            val permission =
                Permission(
                    view = true,
                    edit = true,
                )
            every { roleRepository.hasPermission(any(), any(), any()) } returns permission
            roleService.hasPermission(
                organisationId = 1,
                roleName = mutableListOf("Human Resource"),
                moduleName = Modules.REVIEW_CYCLE.moduleName,
            ) shouldBe permission
        }

        "should add new role in unpublished state" {
            val organisationId = 1L
            val roleName = "Junior HR"
            val permission = listOf(modulePermission)
            val roleStatus = false
            val maxRoleId = 0L
            every { roleRepository.isRoleExists(any(), any()).exists } returns false
            every { roleRepository.getMaxRoleId(any()) } returns maxRoleId
            every { roleRepository.create(any(), any(), any(), any(), any()) } returns Unit
            every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } returns Unit
            roleService.create(
                organisationId = organisationId,
                roleName = roleName,
                modulePermission = permission,
                status = roleStatus,
                userActivityData = UserActivityData(1, "127.0.0.1"),
            ) shouldBe Unit
            verify {
                roleRepository.create(
                    organisationId = organisationId,
                    roleId = maxRoleId + 1,
                    roleName = roleName,
                    modulePermission = permission,
                    status = roleStatus,
                )
            }
        }

        "should update role name by roleId and unpublish it" {
            clearMocks(roleRepository)
            val organisationId: Long = 1
            val id: Long = 1
            val roleName = "Junior HR"
            val status = false
            val roleData =
                RoleData(
                    id = 4,
                    roleId = 5,
                    roleName = "HR",
                    status = true,
                )
            val modulePermissions = listOf(modulePermission)
            every { roleRepository.getRoleDataById(any(), any()) } returns roleData
            every { roleRepository.fetchPermissions(any()) } returns modulePermissions
            every { roleRepository.update(any(), any(), any(), any()) } returns Unit
            every { roleRepository.updatePermissions(any(), any()) } returns Unit
            every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } returns Unit
            roleService.update(
                organisationId = organisationId,
                id = id,
                roleName = roleName,
                modulePermission = modulePermissions,
                status = status,
                userActivityData =
                    UserActivityData(
                        actionBy = 1,
                        ipAddress = "127.0.0.1",
                    ),
            ) shouldBe Unit
            verify { roleRepository.update(organisationId, id, roleName, status) }
        }

        "should update role status and publish it" {
            val organisationId: Long = 1
            val id: Long = 1
            val roleName = "HR"
            val status = true
            val roleData =
                RoleData(
                    id = 1,
                    roleId = 2,
                    roleName = "HR",
                    status = false,
                )
            val modulePermissions = listOf(modulePermission)
            every { roleRepository.getRoleDataById(any(), any()) } returns roleData
            every { roleRepository.fetchPermissions(any()) } returns modulePermissions
            every { roleRepository.update(any(), any(), any(), any()) } returns Unit
            every { roleRepository.updatePermissions(any(), any()) } returns Unit
            every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } returns Unit
            roleService.update(
                organisationId = organisationId,
                id = id,
                roleName = roleName,
                modulePermission = modulePermissions,
                status = status,
                userActivityData =
                    UserActivityData(
                        actionBy = 1,
                        ipAddress = "127.0.0.1",
                    ),
            ) shouldBe Unit
            verify { roleRepository.update(organisationId, id, roleName, status) }
        }

        "should update role status and unpublish it without changing role name" {
            val organisationId: Long = 1
            val id: Long = 1
            val roleName = "Senior HR"
            val status = false
            val roleData =
                RoleData(
                    id = 1,
                    roleId = 2,
                    roleName = "Senior HR",
                    status = true,
                )
            val modulePermissions = listOf(modulePermission)
            every { roleRepository.getRoleDataById(any(), any()) } returns roleData
            every { roleRepository.fetchPermissions(any()) } returns modulePermissions
            every { roleRepository.update(any(), any(), any(), any()) } returns Unit
            every { roleRepository.updatePermissions(any(), any()) } returns Unit
            every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } returns Unit
            roleService.update(
                organisationId = organisationId,
                id = id,
                roleName = roleName,
                modulePermission = modulePermissions,
                status = status,
                userActivityData =
                    UserActivityData(
                        actionBy = 1,
                        ipAddress = "127.0.0.1",
                    ),
            ) shouldBe Unit
            verify { roleRepository.update(organisationId, id, roleName, status) }
        }

        "should update role name and its permission without changing its status" {
            val organisationId: Long = 1
            val id: Long = 1
            val roleName = "HR Executive"
            val status = true
            val roleData =
                RoleData(
                    id = 1,
                    roleId = 2,
                    roleName = "Senior HR",
                    status = true,
                )
            val modulePermissions = listOf(modulePermission)
            val permission =
                listOf(
                    modulePermission,
                    modulePermission.copy(
                        moduleId = 2,
                        moduleName = Modules.REVIEW_CYCLE.moduleName,
                        edit = true,
                    ),
                )
            every { roleRepository.getRoleDataById(any(), any()) } returns roleData
            every { roleRepository.fetchPermissions(any()) } returns modulePermissions
            every { roleRepository.update(any(), any(), any(), any()) } returns Unit
            every { roleRepository.updatePermissions(any(), any()) } returns Unit
            every { roleRepository.deletePermission(any()) } returns Unit
            every { roleRepository.createPermissions(any(), any()) } returns Unit
            every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } returns Unit
            roleService.update(
                organisationId = organisationId,
                id = id,
                roleName = roleName,
                modulePermission = permission,
                status = status,
                userActivityData =
                    UserActivityData(
                        actionBy = 1,
                        ipAddress = "127.0.0.1",
                    ),
            ) shouldBe Unit
            verify { roleRepository.update(organisationId, id, roleName, status) }
        }
    }
}
