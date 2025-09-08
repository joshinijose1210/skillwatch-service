package roles

import io.kotest.core.spec.Spec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import norm.executeCommand
import scalereal.core.models.domain.ModulePermission
import scalereal.core.models.domain.Permission
import scalereal.core.models.domain.Role
import scalereal.core.models.domain.RoleData
import scalereal.core.modules.ModuleRepository
import scalereal.core.modules.Modules
import scalereal.core.roles.RoleService
import scalereal.core.userActivity.UserActivityRepository
import scalereal.db.roles.RoleRepositoryImpl
import util.StringSpecWithDataSource
import java.sql.Timestamp

class RoleRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var roleRepositoryImpl: RoleRepositoryImpl
    private lateinit var roleService: RoleService
    private val userActivityRepository = mockk<UserActivityRepository>()
    private val moduleRepository =
        mockk<ModuleRepository> {
            every { fetchModuleId(Modules.ROLES_AND_PERMISSIONS.moduleName) } returns 5
            every {
                userActivityRepository.addActivity(
                    1,
                    5,
                    "Role R2 Added and Published",
                    "Role R2 Added and Published",
                    "127.0.0.1",
                )
            } returns Unit
        }

    init {
        val now = Timestamp(System.currentTimeMillis())
        "should check if role exists by role name & org id" {
            val roleStatus = roleRepositoryImpl.isRoleExists(organisationId = 1, roleName = "Employee")
            roleStatus.exists shouldBe false
            roleStatus.status shouldBe false
        }

        "should create new role" {
            roleRepositoryImpl.create(
                organisationId = 1,
                roleId = 2,
                roleName = "Employee",
                modulePermission =
                    listOf(
                        ModulePermission(
                            moduleId = 1,
                            moduleName = Modules.REVIEW_CYCLE.moduleName,
                            view = true,
                            edit = false,
                        ),
                    ),
                status = true,
            ) shouldBe Unit
        }

        "should be able to fetch permission by role and module name" {
            val expectedPermission =
                Permission(
                    view = true,
                    edit = true,
                )
            val actualPermission =
                roleRepositoryImpl.hasPermission(
                    organisationId = 1,
                    roleName = mutableListOf("HR"),
                    moduleName = Modules.REVIEW_CYCLE.moduleName,
                )
            expectedPermission shouldBe actualPermission
        }

        "should return count and fetch all roles" {
            val organisationId = 1L
            val searchText = ""
            val expectedRoles =
                listOf(
                    Role(
                        organisationId = 1,
                        id = 2,
                        roleId = "2",
                        roleName = "Employee",
                        modulePermission = listOf(),
                        status = true,
                        createdAt = now,
                        updatedAt = null,
                    ),
                    Role(
                        organisationId = 1,
                        id = 1,
                        roleId = "1",
                        roleName = "HR",
                        modulePermission = listOf(),
                        status = true,
                        createdAt = now,
                        updatedAt = null,
                    ),
                )

            val count = roleRepositoryImpl.count(organisationId = organisationId, searchText = searchText)
            val actualRoles =
                roleRepositoryImpl.fetchAll(
                    organisationId = organisationId,
                    searchText = searchText,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            count shouldBe actualRoles.size
            actualRoles.mapIndexed { index, actual ->
                val expected = expectedRoles[index]
                actual.id shouldBe expected.id
                actual.roleName shouldBe expected.roleName
                actual.status shouldBe expected.status
                actual.modulePermission shouldBe expected.modulePermission
                actual.organisationId shouldBe expected.organisationId
                actual.roleId shouldBe expected.roleId
                actual.createdAt shouldBeAfter expected.createdAt
                actual.updatedAt shouldBe expected.updatedAt
            }
        }

        "should be able to fetch permissions by role id" {
            val expectedPermission =
                listOf(
                    ModulePermission(moduleId = 1, moduleName = "Request Feedback", view = true, edit = false),
                )
            val actualPermission = roleRepositoryImpl.fetchPermissions(roleId = 2)
            actualPermission.mapIndexed { index, actual ->
                actual.moduleId shouldBe expectedPermission[index].moduleId
                actual.view shouldBe expectedPermission[index].view
                actual.edit shouldBe expectedPermission[index].edit
            }
        }

        "should update role data by roleId" {
            roleRepositoryImpl.update(
                organisationId = 1,
                id = 1,
                roleName = "Human Resource",
                status = false,
            ) shouldBe Unit
        }

        "should be able to check if role exists and its status by role name" {
            val role = roleRepositoryImpl.isRoleExists(organisationId = 1, roleName = "Human Resource")
            role.exists shouldBe true
            role.status shouldBe false
        }

        "should return empty list if role is not available by given role name" {
            roleRepositoryImpl.fetchAll(
                organisationId = 1,
                searchText = "HR executive",
                offset = 0,
                limit = Int.MAX_VALUE,
            ) shouldBe emptyList()
        }

        "should be able to delete permissions by role id" {
            roleRepositoryImpl.deletePermission(roleId = 1) shouldBe Unit
        }

        "should be able to update permissions by role id" {
            roleRepositoryImpl.updatePermissions(
                roleId = 2,
                modulePermission =
                    listOf(
                        ModulePermission(
                            moduleId = 2,
                            moduleName = Modules.EMPLOYEE_FEEDBACK.moduleName,
                            view = false,
                            edit = false,
                        ),
                        ModulePermission(
                            moduleId = 3,
                            moduleName = Modules.PERFORMANCE_REVIEWS.moduleName,
                            view = true,
                            edit = false,
                        ),
                    ),
            ) shouldBe Unit
        }

        "should get updated role data by role id" {
            val expectedRole =
                RoleData(
                    id = 2,
                    roleId = 2,
                    roleName = "Employee",
                    status = true,
                )
            val actualRole = roleRepositoryImpl.getRoleDataById(id = 2, organisationId = 1)
            actualRole shouldBe expectedRole
        }

        "should be able to get maximum role id" {
            roleRepositoryImpl.getMaxRoleId(organisationId = 1) shouldBe 2
        }

        "should be able to get role id by role name" {
            roleRepositoryImpl.getRoleId(organisationId = 1, roleName = "Employee") shouldBe 2
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        dataSource.connection.use {
            it.executeCommand(
                """
                 INSERT INTO organisations (sr_no, admin_id, name, is_active, organisation_size)
                 VALUES(1, 1, 'Scalereal', true, 50);
                 
                 INSERT INTO employees (id, emp_id, first_name, last_name, email_id, contact_no, status, onboarding_flow, organisation_id)
                 VALUES(1, 'SR0049', 'grv', 'abc', 'gp.aarush@gmail.com', 7389543155, true, false, 1);
                 
                 INSERT INTO roles (role_name, status, role_id, organisation_id) values('HR', true, 1, 1);
                 
                INSERT INTO modules (name)
                  SELECT new_module
                    FROM (VALUES 
                    ('Request Feedback'),
                    ('Feedback'),  
                    ('Teams'),
                    ('Designations'),
                    ('Roles & Permissions'),
                    ('Employees'),
                    ('KRAs'),
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
                    ('Received Suggestions')) AS new_modules(new_module)
                    LEFT JOIN modules ON new_modules.new_module = modules.name
                    WHERE modules.name IS NULL;
                 
                 INSERT INTO module_permissions(role_id, module_id, view, edit) 
                 VALUES (1, 1, true, true),
                 (1, 2, true, true),
                 (1, 3, true, true),
                 (1, 4, true, true),
                 (1, 5, true, true),
                 (1, 6, true, true),
                 (1, 7, true, true),
                 (1, 8, true, true),
                 (1, 9, true, true),
                 (1, 10, true, true),
                 (1, 11, true, true),
                 (1, 12, true, true),
                 (1, 13, true, true),
                 (1, 14, true, true),
                 (1, 15, true, true),
                 (1, 16, true, true),
                 (1, 17, true, true),
                 (1, 18, true, true);                
                """.trimIndent(),
            )
        }

        roleRepositoryImpl = RoleRepositoryImpl(dataSource)
        roleService =
            RoleService(
                repository = roleRepositoryImpl,
                userActivityRepository = userActivityRepository,
                moduleRepository = moduleRepository,
            )
    }
}
