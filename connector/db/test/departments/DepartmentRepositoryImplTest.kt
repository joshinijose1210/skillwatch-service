package departments

import io.kotest.core.spec.Spec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import norm.executeCommand
import scalereal.core.departments.DepartmentService
import scalereal.core.designations.DesignationRepository
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.teams.TeamRepository
import scalereal.core.userActivity.UserActivityRepository
import scalereal.db.departments.DepartmentRepositoryImpl
import util.StringSpecWithDataSource

class DepartmentRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var departmentRepositoryImpl: DepartmentRepositoryImpl
    private val userActivity = mockk<UserActivityRepository>()
    private val moduleService =
        mockk<ModuleService> {
            every { fetchModuleId(Modules.DEPARTMENTS.moduleName) } returns 18
        }
    private val teamRepository = mockk<TeamRepository>()
    private val designationRepository = mockk<DesignationRepository>()
    private lateinit var departmentService: DepartmentService

    init {

        "should able to get maximum department id 0 when there are no department in an organisation" {
            val maxDepartmentId = departmentRepositoryImpl.getMaxDepartmentId(organisationId = 1)

            maxDepartmentId shouldBe 0
        }
        "should able to add department" {
            departmentRepositoryImpl.create(organisationId = 1, id = 1, departmentName = "Engineering", departmentStatus = false)
        }

        "should able to edit the department status" {
            departmentRepositoryImpl.update(organisationId = 1, departmentId = 1, departmentName = "Engineering", departmentStatus = true)
        }

        "should able to edit the department name" {
            departmentRepositoryImpl.update(
                organisationId = 1,
                departmentId = 1,
                departmentName = "Engineering Dep.",
                departmentStatus = true,
            )
        }

        "should count and show all existing department" {
            val departmentCount = departmentRepositoryImpl.departmentCount(organisationId = 1, searchText = "")
            val department = departmentRepositoryImpl.fetchAll(organisationId = 1, offset = 0, limit = Int.MAX_VALUE, searchText = "")

            departmentCount shouldBe department.size
            department.map {
                it.organisationId shouldBe 1
                it.id shouldBe 1
                it.departmentName shouldBe "Engineering Dep."
                it.departmentStatus shouldBe true
            }
        }

        "should be able check if department exists by name" {
            val organisationId = 1L
            val departmentName = "Engineering Dep."
            val department = departmentRepositoryImpl.isDepartmentExists(organisationId, departmentName)

            department.exists shouldBe true
            department.status shouldBe true
        }

        "should be able to fetch department id by organisation id and department name" {
            val organisationId = 1L
            val departmentName = "Engineering Dep."
            val departmentId = departmentRepositoryImpl.getDepartmentId(organisationId, departmentName)

            departmentId shouldBe 1L
        }

        "should be able not to get result for non-existing department by name" {
            val organisationId = 1L
            val departmentName = "Finance Dep."
            val department = departmentRepositoryImpl.isDepartmentExists(organisationId, departmentName)

            department.exists shouldBe false
            department.status shouldBe false
        }

        "should be able to get department data by department id" {
            val department = departmentRepositoryImpl.getDepartmentDataById(organisationId = 1, departmentId = 1)

            department.id shouldBe 1
            department.departmentId shouldBe "1"
            department.departmentName shouldBe "Engineering Dep."
            department.departmentStatus shouldBe true
        }

        "should be able to get maximum department id in an organisation" {
            val maxDepartmentId = departmentRepositoryImpl.getMaxDepartmentId(organisationId = 1)

            maxDepartmentId shouldBe 1L
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        dataSource.connection.use {
            it.executeCommand(
                """
                INSERT INTO organisations(sr_no, admin_id, name, is_active, organisation_size)
                VALUES (1,1,'ScaleReal Technologies Pvt. Ltd.', true, 50);
                """.trimIndent(),
            )
        }
        departmentRepositoryImpl = DepartmentRepositoryImpl(dataSource)
        departmentService = DepartmentService(departmentRepositoryImpl, userActivity, moduleService, teamRepository, designationRepository)
    }
}
