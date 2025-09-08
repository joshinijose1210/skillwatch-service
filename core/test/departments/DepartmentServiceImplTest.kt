package departments

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.departments.DepartmentRepository
import scalereal.core.departments.DepartmentService
import scalereal.core.designations.DesignationRepository
import scalereal.core.models.domain.Department
import scalereal.core.models.domain.DepartmentData
import scalereal.core.models.domain.DepartmentResults
import scalereal.core.models.domain.DepartmentStatus
import scalereal.core.models.domain.Designation
import scalereal.core.models.domain.Team
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.teams.TeamRepository
import scalereal.core.userActivity.UserActivityRepository
import java.sql.Timestamp

class DepartmentServiceImplTest : StringSpec() {
    private val departmentRepository = mockk<DepartmentRepository>()
    private val userActivity = mockk<UserActivityRepository>()
    private val moduleService =
        mockk<ModuleService> {
            every { fetchModuleId(Modules.DEPARTMENTS.moduleName) } returns 18
        }
    private val teamRepository = mockk<TeamRepository>()
    private val designationRepository = mockk<DesignationRepository>()
    private val departmentService =
        DepartmentService(departmentRepository, userActivity, moduleService, teamRepository, designationRepository)

    init {
        "should add new department" {
            val organisationId: Long = 1
            val maxDepartmentId: Long = 0
            val departmentName = "Engineering"
            val departmentStatus = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val expectedResult =
                DepartmentResults(
                    existingDepartment = arrayOf(),
                    addedDepartment = arrayOf("Engineering"),
                    invalidLengthDepartment = arrayOf(),
                    invalidCharDepartment = arrayOf(),
                )
            every { departmentRepository.getMaxDepartmentId(organisationId = 1) } returns maxDepartmentId
            every {
                departmentRepository.create(
                    organisationId = organisationId,
                    id = maxDepartmentId + 1,
                    departmentName = departmentName,
                    departmentStatus = departmentStatus,
                )
            } returns 1
            every { departmentRepository.isDepartmentExists(organisationId, departmentName).exists } returns false
            every {
                userActivity.addActivity(
                    userActivityData.actionBy,
                    moduleId = 18,
                    activity = "Department DEP1 Added and Published",
                    description = "Department DEP1 Added and Published",
                    ipAddress = userActivityData.ipAddress,
                )
            } returns Unit
            val result =
                departmentService.create(
                    departments =
                        listOf(
                            DepartmentData(
                                organisationId = 1,
                                departmentName = "Engineering",
                                departmentStatus = true,
                            ),
                        ),
                    userActivityData,
                )
            result.existingDepartment shouldBe expectedResult.existingDepartment
            result.addedDepartment shouldBe expectedResult.addedDepartment
            verify(exactly = 1) { departmentRepository.create(organisationId, maxDepartmentId + 1, departmentName, departmentStatus) }
        }

        "should return existed department while adding department with existing name" {
            val organisationId: Long = 1
            val maxDepartmentId: Long = 1
            val departmentName = "Engineering"
            val departmentStatus = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val expectedResult =
                DepartmentResults(
                    existingDepartment = arrayOf("Engineering"),
                    addedDepartment = arrayOf(),
                    invalidLengthDepartment = arrayOf(),
                    invalidCharDepartment = arrayOf(),
                )
            val isDepartment =
                DepartmentStatus(
                    exists = true,
                    status = true,
                )
            every { departmentRepository.isDepartmentExists(any(), any()) } returns isDepartment
            every { departmentRepository.getMaxDepartmentId(any()) } returns maxDepartmentId

            val result =
                departmentService.create(
                    departments =
                        listOf(
                            DepartmentData(
                                organisationId = organisationId,
                                departmentName = departmentName,
                                departmentStatus = departmentStatus,
                            ),
                        ),
                    userActivityData,
                )
            result.existingDepartment shouldBe expectedResult.existingDepartment
            result.addedDepartment shouldBe expectedResult.addedDepartment
        }

        "should throw exception while updating department name with existing name" {
            val organisationId: Long = 1
            val id: Long = 1
            val departmentName = "Engineering"
            val departmentStatus = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val departmentData =
                Department(
                    id = 1,
                    departmentId = "1",
                    organisationId = 1,
                    departmentName = "engineering",
                    departmentStatus = true,
                    departmentCreatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                    departmentUpdatedAt = Timestamp.valueOf("2022-11-12 19:6:35.142179"),
                )
            val linkedTeams =
                listOf(
                    Team(
                        organisationId = 1,
                        departmentId = 1,
                        departmentName = "Engineering",
                        departmentDisplayId = "DEP1",
                        departmentStatus = true,
                        id = 1,
                        teamId = "1",
                        teamName = "sd6",
                        teamStatus = true,
                        teamCreatedAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                        teamUpdatedAt = Timestamp.valueOf("2022-12-17 1:35:21.672150"),
                    ),
                )
            every { departmentRepository.isDepartmentExists(organisationId, departmentName).exists } returns true
            every { departmentRepository.getDepartmentDataById(departmentId = id, organisationId = organisationId) } returns departmentData
            every { teamRepository.fetchAll(any(), any(), any(), any(), any()) } returns linkedTeams
            val exception =
                shouldThrow<Exception> { departmentService.update(organisationId, id, departmentName, departmentStatus, userActivityData) }
            exception.message shouldBe "Department ${departmentName.trim()} already exists"
        }

        "should be able to unpublish department and linked teams and designations" {
            val organisationId: Long = 1
            val id: Long = 1
            val departmentName = "Engineering"
            val departmentStatus = false
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val departmentData =
                Department(
                    id = 1,
                    departmentId = "1",
                    organisationId = 1,
                    departmentName = "Engineering",
                    departmentStatus = true,
                    departmentCreatedAt = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                    departmentUpdatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                )
            val linkedTeams =
                listOf(
                    Team(
                        organisationId = 1,
                        departmentId = 1,
                        departmentName = "Engineering",
                        departmentDisplayId = "DEP1",
                        departmentStatus = true,
                        id = 1,
                        teamId = "1",
                        teamName = "sd6",
                        teamStatus = true,
                        teamCreatedAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                        teamUpdatedAt = Timestamp.valueOf("2022-12-17 1:35:21.672150"),
                    ),
                )
            val linkedDesignations =
                listOf(
                    Designation(
                        organisationId = 1,
                        departmentId = 1,
                        departmentName = "Engineering",
                        departmentDisplayId = "DEP1",
                        departmentStatus = true,
                        teamId = 1,
                        teamDisplayId = "1",
                        teamName = "sd6",
                        teamStatus = true,
                        id = 1,
                        designationId = "1",
                        designationName = "SDE-1",
                        status = true,
                        createdAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                        updatedAt = Timestamp.valueOf("2022-12-17 1:35:21.672150"),
                    ),
                )
            every { departmentRepository.getDepartmentDataById(any(), any()) } returns departmentData
            every { teamRepository.fetchAll(any(), any(), any(), any(), any()) } returns linkedTeams
            every { departmentRepository.update(any(), any(), any(), any()) } returns Unit
            every { designationRepository.fetchAll(any(), any(), any(), any(), any(), any()) } returns linkedDesignations
            every { userActivity.addActivity(any(), any(), any(), any(), any()) } just Runs
            every { teamRepository.update(any(), any(), any(), any()) } returns Unit
            every { designationRepository.update(any(), any(), any(), any()) } returns Unit
            val result =
                departmentService.update(
                    organisationId = organisationId,
                    departmentId = id,
                    departmentName = departmentName,
                    departmentStatus = departmentStatus,
                    userActivityData = userActivityData,
                )
            result shouldBe Unit
        }

        "should throw exception if department name contains special characters" {
            val organisationId: Long = 1
            val departmentId: Long = 1
            val invalidDepartmentName = "Eng!neering"
            val departmentStatus = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )

            val exception =
                shouldThrow<Exception> {
                    departmentService.update(
                        organisationId = organisationId,
                        departmentId = departmentId,
                        departmentName = invalidDepartmentName,
                        departmentStatus = departmentStatus,
                        userActivityData = userActivityData,
                    )
                }

            exception.message shouldBe "Department name must not contain special characters."
        }

        "should show all existing departments" {
            val departments =
                listOf(
                    Department(
                        organisationId = 1,
                        id = 1,
                        departmentId = "1",
                        departmentName = "Engineering",
                        departmentStatus = true,
                        departmentCreatedAt = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                        departmentUpdatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                    ),
                    Department(
                        organisationId = 1,
                        id = 2,
                        departmentId = "2",
                        departmentName = "R&D",
                        departmentStatus = true,
                        departmentCreatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                        departmentUpdatedAt = Timestamp.valueOf("2022-11-12 19:6:35.142179"),
                    ),
                )

            val departmentData =
                listOf(
                    Department(
                        organisationId = 1,
                        id = 1,
                        departmentId = "DEP1",
                        departmentName = "Engineering",
                        departmentStatus = true,
                        departmentCreatedAt = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                        departmentUpdatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                    ),
                    Department(
                        organisationId = 1,
                        id = 2,
                        departmentId = "DEP2",
                        departmentName = "R&D",
                        departmentStatus = true,
                        departmentCreatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                        departmentUpdatedAt = Timestamp.valueOf("2022-11-12 19:6:35.142179"),
                    ),
                )
            every {
                departmentRepository.fetchAll(
                    organisationId = 1,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    searchText = "",
                )
            } returns departments
            every {
                departmentRepository.departmentCount(
                    organisationId = 1,
                    searchText = "",
                )
            } returns departments.size
            departmentService.fetchAll(
                organisationId = 1,
                page = 1,
                limit = Int.MAX_VALUE,
                searchText = "",
            ) shouldBe departmentData
            departmentService.departmentCount(organisationId = 1, searchText = "") shouldBe departmentData.size
            coVerify {
                departmentRepository.fetchAll(organisationId = 1, offset = 0, limit = Int.MAX_VALUE, searchText = "")
                departmentService.departmentCount(organisationId = 1, searchText = "")
            }
        }

        // This can be uncommented when we uncomment related service method

//        "createDefaultDepartments should add default departments if not exists" {
//            val organisationId: Long = 1
//            val isDepartment =
//                DepartmentStatus(
//                    exists = false,
//                    status = false,
//                )
//            val maxDepartmentId = 0L
//            every { departmentRepository.isDepartmentExists(any(), any()) } returns isDepartment
//            every { departmentRepository.getMaxDepartmentId(any()) } returns maxDepartmentId
//            every { departmentRepository.create(any(), any(), any(), any()) } just Runs
//
//            val result = departmentService.createDefaultDepartments(organisationId)
//            result shouldBe Unit
//
//            verify(exactly = 3) {
//                departmentRepository.create(
//                    organisationId = any(),
//                    id = any(),
//                    departmentName = any(),
//                    departmentStatus = any(),
//                )
//            }
//        }
    }
}
