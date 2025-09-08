package designations

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.departments.DepartmentRepository
import scalereal.core.designations.DesignationRepository
import scalereal.core.designations.DesignationService
import scalereal.core.models.domain.Designation
import scalereal.core.models.domain.DesignationData
import scalereal.core.models.domain.DesignationResults
import scalereal.core.models.domain.Team
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.teams.TeamRepository
import scalereal.core.userActivity.UserActivityRepository
import java.sql.Timestamp

class DesignationServiceImplTest : StringSpec() {
    private val designationRepository = mockk<DesignationRepository>()
    private val teamRepository = mockk<TeamRepository>()
    private val userActivityRepository = mockk<UserActivityRepository>()
    private val moduleService =
        mockk<ModuleService> {
            every { fetchModuleId(Modules.DESIGNATIONS.moduleName) } returns 4
        }
    private val departmentRepository = mockk<DepartmentRepository>()
    private val designationService =
        DesignationService(
            designationRepository,
            userActivityRepository,
            teamRepository,
            moduleService,
            departmentRepository,
        )

    init {
        "should count designations that are not linked to any team" {
            every { designationRepository.unlinkedDesignationsCount(any()) } returns 0
            designationService.unlinkedDesignationsCount(organisationId = 1) shouldBe 0
        }

        "should add designations that don't exist and return added and existed designations" {
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val maxDesignationId = 1L
            val organisationId = 1L
            val teamId = 1L
            val addedDesignation = arrayOf("SDE - 2")
            val existingDesignation = arrayOf("SDE - 1")
            val expectedResult = DesignationResults(existingDesignation, addedDesignation, arrayOf(), arrayOf())
            every {
                designationRepository
                    .isDesignationExists(
                        organisationId = organisationId,
                        teamId = teamId,
                        designationName = "SDE - 1",
                    ).exists
            } returns true
            every {
                designationRepository
                    .isDesignationExists(
                        organisationId = organisationId,
                        teamId = teamId,
                        designationName = "SDE - 2",
                    ).exists
            } returns false
            every { designationRepository.getMaxDesignationId(any()) } returns maxDesignationId
            every {
                designationRepository.create(
                    id = 2,
                    organisationId = organisationId,
                    teamId = teamId,
                    designationName = "SDE - 2",
                    status = true,
                )
            } returns 2
            every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } returns Unit
            val actualResult =
                designationService.create(
                    designations =
                        listOf(
                            DesignationData(
                                organisationId = organisationId,
                                designationName = "SDE - 1",
                                teamId = teamId,
                                status = true,
                            ),
                            DesignationData(
                                organisationId = organisationId,
                                designationName = "SDE - 2",
                                teamId = teamId,
                                status = true,
                            ),
                        ),
                    userActivityData,
                )
            actualResult.addedDesignation shouldBe expectedResult.addedDesignation
            actualResult.existingDesignation shouldBe expectedResult.existingDesignation
            verify(exactly = 1) {
                designationRepository.create(
                    id = maxDesignationId + 1,
                    organisationId = organisationId,
                    designationName = "SDE - 2",
                    teamId = teamId,
                    status = true,
                )
            }
        }

        "should count all designations available in an organisations" {
            every { designationRepository.count(any(), any(), any(), any()) } returns 2
            designationService.count(
                organisationId = 1,
                searchText = "",
                departmentId = listOf(-99),
                teamId = listOf(-99),
            ) shouldBe 2
        }

        "should fetch all designations" {
            val designation =
                listOf(
                    Designation(
                        organisationId = 1,
                        departmentId = 1,
                        departmentName = "Engineering",
                        departmentDisplayId = "1",
                        departmentStatus = true,
                        teamId = 1,
                        teamName = "Backend",
                        teamDisplayId = "1",
                        teamStatus = true,
                        id = 1,
                        designationId = "1",
                        designationName = "SDE - 1",
                        status = false,
                        createdAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                        updatedAt = Timestamp.valueOf("2022-12-17 1:35:21.672150"),
                    ),
                )
            val expectedDesignation =
                listOf(
                    Designation(
                        organisationId = 1,
                        departmentId = 1,
                        departmentName = "Engineering",
                        departmentDisplayId = "DEP1",
                        departmentStatus = true,
                        teamId = 1,
                        teamName = "Backend",
                        teamDisplayId = "T1",
                        teamStatus = true,
                        id = 1,
                        designationId = "D1",
                        designationName = "SDE - 1",
                        status = false,
                        createdAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                        updatedAt = Timestamp.valueOf("2022-12-17 1:35:21.672150"),
                    ),
                )
            every { designationRepository.fetchAll(any(), any(), any(), any(), any(), any()) } returns designation

            designationService.fetchAll(
                organisationId = 1,
                searchText = "",
                departmentId = listOf(-99),
                teamId = listOf(-99),
                page = 1,
                limit = Int.MAX_VALUE,
            ) shouldBe expectedDesignation

            verify(exactly = 1) {
                designationRepository.fetchAll(
                    organisationId = 1,
                    searchText = "",
                    departmentId = listOf(-99),
                    teamId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            }
        }

        "should update existing designation" {
            val organisationId = 1L
            val departmentId = 1L
            val teamId = 1L
            val id = 1L
            val designationName = "SDE - Trainee"
            val status = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val designationOldData =
                Designation(
                    organisationId = 1,
                    departmentId = 1,
                    departmentName = "Engineering",
                    departmentDisplayId = "DEP1",
                    departmentStatus = true,
                    teamId = 1,
                    teamName = "Backend",
                    teamDisplayId = "T1",
                    teamStatus = true,
                    id = 1,
                    designationId = "1",
                    designationName = "SDE - Trainee",
                    status = false,
                    createdAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                    updatedAt = null,
                )
            val teamData =
                Team(
                    id = 1,
                    organisationId = 1,
                    teamName = "Backend",
                    teamStatus = true,
                    teamId = "1",
                    departmentId = 1,
                    departmentDisplayId = "1",
                    departmentName = "Engineering",
                    departmentStatus = true,
                    teamCreatedAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                    teamUpdatedAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                )
            val linkedDesignations = listOf(designationOldData)
            every { designationRepository.getDesignationDataById(any(), any()) } returns designationOldData
            every { teamRepository.getTeamDataById(any(), any()) } returns teamData
            every { designationRepository.fetchAll(any(), any(), any(), any(), any(), any()) } returns linkedDesignations
            every { designationRepository.update(any(), any(), any(), any()) } returns Unit
            every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } returns Unit
            designationService.update(
                organisationId = organisationId,
                departmentId = departmentId,
                teamId = teamId,
                designationName = designationName,
                status = status,
                id = id,
                userActivityData = userActivityData,
            ) shouldBe Unit
            verify { designationRepository.update(any(), any(), any(), any()) }
        }

        "should throw exception if designation name contains special characters" {
            val organisationId = 1L
            val departmentId = 1L
            val teamId = 1L
            val id = 1L
            val invalidDesignationName = "SDE@Trainee"
            val status = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )

            val exception =
                shouldThrow<Exception> {
                    designationService.update(
                        organisationId = organisationId,
                        departmentId = departmentId,
                        teamId = teamId,
                        designationName = invalidDesignationName,
                        status = status,
                        id = id,
                        userActivityData = userActivityData,
                    )
                }

            exception.message shouldBe "Designation name must not contain special characters."
        }

        "should throw exception while updating designation name with existing name in the same team" {
            val organisationId = 1L
            val departmentId = 1L
            val id = 1L
            val designationName = "SDE - 1"
            val teamId = 1L
            val status = false
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
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
                        teamName = "Backend",
                        teamDisplayId = "T1",
                        teamStatus = true,
                        id = 1,
                        designationId = "1",
                        designationName = "SDE - Trainee",
                        status = false,
                        createdAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                        updatedAt = null,
                    ),
                    Designation(
                        organisationId = 1,
                        departmentId = 1,
                        departmentName = "Engineering",
                        departmentDisplayId = "DEP1",
                        departmentStatus = true,
                        teamId = 1,
                        teamName = "Backend",
                        teamDisplayId = "T1",
                        teamStatus = true,
                        id = 2,
                        designationId = "2",
                        designationName = "SDE - 1",
                        status = true,
                        createdAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                        updatedAt = null,
                    ),
                )
            val designationOldData =
                Designation(
                    organisationId = 1,
                    departmentId = 1,
                    departmentName = "Engineering",
                    departmentDisplayId = "DEP1",
                    departmentStatus = true,
                    teamId = 1,
                    teamName = "Backend",
                    teamDisplayId = "1",
                    teamStatus = true,
                    id = 1,
                    designationId = "1",
                    designationName = "SDE - Trainee",
                    status = false,
                    createdAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                    updatedAt = null,
                )
            val teamData =
                Team(
                    id = 1,
                    organisationId = 1,
                    teamName = "Backend",
                    teamStatus = true,
                    teamId = "1",
                    departmentId = 1,
                    departmentDisplayId = "1",
                    departmentName = "Engineering",
                    departmentStatus = true,
                    teamCreatedAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                    teamUpdatedAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                )
            every { designationRepository.getDesignationDataById(any(), any()) } returns designationOldData
            every { teamRepository.getTeamDataById(any(), any()) } returns teamData
            every {
                designationRepository.fetchAll(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns linkedDesignations

            val exception =
                shouldThrow<Exception> {
                    designationService.update(
                        organisationId = organisationId,
                        departmentId = departmentId,
                        teamId = teamId,
                        designationName = designationName,
                        status = status,
                        id = id,
                        userActivityData = userActivityData,
                    )
                }
            exception.message.shouldBe("Designation 'SDE - 1' already exists in the Team T1")
        }

        // This can be uncommented when we uncomment related service method

//        "createDefaultDesignations should add designations if not exist" {
//            val maxDesignationId = 0L
//            val departmentId = 1L
//            val teamId = 1L
//            every { designationRepository.getMaxDesignationId(any()) } returns maxDesignationId
//            every { departmentRepository.getDepartmentId(any(), any()) } returns departmentId
//            every { teamRepository.getTeamId(any(), any(), any()) } returns teamId
//            every { designationRepository.isDesignationExists(any(), any(), any()).exists } returns false
//            every { designationRepository.create(any(), any(), any(), any(), any()) } returns Unit
//
//            designationService.createDefaultDesignations(1) shouldBe Unit
//
//            verify(exactly = 4) { designationRepository.create(any(), any(), any(), any(), any()) }
//        }

        "should throw exception while trying to update linked team of designation" {
            val organisationId: Long = 1
            val departmentId: Long = 1
            val teamId: Long = 2
            val id: Long = 1
            val designationName = "SDE - Trainee"
            val status = false
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val designationOldData =
                Designation(
                    organisationId = 1,
                    departmentId = 1,
                    departmentName = "Engineering",
                    departmentDisplayId = "DEP1",
                    departmentStatus = true,
                    teamId = 1,
                    teamName = "Backend",
                    teamDisplayId = "1",
                    teamStatus = true,
                    id = 1,
                    designationId = "1",
                    designationName = "SDE - Trainee",
                    status = false,
                    createdAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                    updatedAt = null,
                )
            val teamData =
                Team(
                    id = 1,
                    organisationId = 1,
                    teamName = "Backend",
                    teamStatus = true,
                    teamId = "1",
                    departmentId = 1,
                    departmentDisplayId = "1",
                    departmentName = "Engineering",
                    departmentStatus = true,
                    teamCreatedAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                    teamUpdatedAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                )
            val linkedDesignations = listOf(designationOldData)
            every { designationRepository.getDesignationDataById(any(), any()) } returns designationOldData
            every { teamRepository.getTeamDataById(any(), any()) } returns teamData
            every { designationRepository.fetchAll(any(), any(), any(), any(), any(), any()) } returns linkedDesignations
            val exception =
                shouldThrow<Exception> {
                    designationService.update(
                        organisationId = organisationId,
                        departmentId = departmentId,
                        teamId = teamId,
                        designationName = designationName,
                        status = status,
                        id = id,
                        userActivityData = userActivityData,
                    )
                }
            exception.message shouldBe "Designation is linked with the Team T1 and cannot be linked with another team."
        }

        "should throw exception while trying to link designation with inactive team" {
            val organisationId = 1L
            val departmentId = 1L
            val teamId = 1L
            val id = 1L
            val designationName = "SDE - Trainee"
            val status = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val designationOldData =
                Designation(
                    organisationId = null,
                    departmentId = null,
                    departmentName = null,
                    departmentDisplayId = null,
                    departmentStatus = null,
                    teamId = null,
                    teamName = null,
                    teamDisplayId = null,
                    teamStatus = null,
                    id = 1,
                    designationId = "1",
                    designationName = "SDE - Trainee",
                    status = false,
                    createdAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                    updatedAt = null,
                )
            val teamData =
                Team(
                    id = 1,
                    organisationId = 1,
                    teamName = "Backend",
                    teamStatus = false,
                    teamId = "1",
                    departmentId = 1,
                    departmentDisplayId = "1",
                    departmentName = "Engineering",
                    departmentStatus = true,
                    teamCreatedAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                    teamUpdatedAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                )
            val linkedDesignations = listOf(designationOldData)
            every { designationRepository.getDesignationDataById(any(), any()) } returns designationOldData
            every { teamRepository.getTeamDataById(any(), any()) } returns teamData
            every {
                designationRepository.fetchAll(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns linkedDesignations

            val exception =
                shouldThrow<Exception> {
                    designationService.update(
                        organisationId = organisationId,
                        departmentId = departmentId,
                        teamId = teamId,
                        designationName = designationName,
                        status = status,
                        id = id,
                        userActivityData = userActivityData,
                    )
                }
            exception.message shouldBe "Please activate the Team T1 to update the designation."
        }

        "should throw exception while trying to update designation with inactive linked team" {
            val organisationId = 1L
            val departmentId = 1L
            val teamId = 1L
            val id = 1L
            val designationName = "SDE - 1"
            val status = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val designationOldData =
                Designation(
                    organisationId = 1,
                    departmentId = 1,
                    departmentName = "Engineering",
                    departmentDisplayId = "DEP1",
                    departmentStatus = true,
                    teamId = 1,
                    teamName = "Backend",
                    teamDisplayId = "1",
                    teamStatus = false,
                    id = 1,
                    designationId = "1",
                    designationName = "SDE - Trainee",
                    status = false,
                    createdAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                    updatedAt = null,
                )
            val teamData =
                Team(
                    id = 1,
                    organisationId = 1,
                    teamName = "Backend",
                    teamStatus = false,
                    teamId = "1",
                    departmentId = 1,
                    departmentDisplayId = "1",
                    departmentName = "Engineering",
                    departmentStatus = true,
                    teamCreatedAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                    teamUpdatedAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                )
            val linkedDesignations = listOf(designationOldData)
            every { designationRepository.getDesignationDataById(any(), any()) } returns designationOldData
            every { teamRepository.getTeamDataById(any(), any()) } returns teamData
            every {
                designationRepository.fetchAll(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns linkedDesignations

            val exception =
                shouldThrow<Exception> {
                    designationService.update(
                        organisationId = organisationId,
                        departmentId = departmentId,
                        teamId = teamId,
                        designationName = designationName,
                        status = status,
                        id = id,
                        userActivityData = userActivityData,
                    )
                }
            exception.message shouldBe "Please activate the Team T1 to update the designation."
        }

        "should throw exception while team designation mapping in database through exception" {
            val organisationId = 1L
            val departmentId = 1L
            val teamId = 1L
            val id = 1L
            val designationName = "SDE - Trainee"
            val status = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val designationOldData =
                Designation(
                    organisationId = 1,
                    departmentId = null,
                    departmentName = null,
                    departmentDisplayId = null,
                    departmentStatus = null,
                    teamId = null,
                    teamName = null,
                    teamDisplayId = null,
                    teamStatus = null,
                    id = 1,
                    designationId = "1",
                    designationName = "SDE - Trainee",
                    status = true,
                    createdAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                    updatedAt = null,
                )
            val teamData =
                Team(
                    id = 1,
                    organisationId = 1,
                    teamName = "Backend",
                    teamStatus = true,
                    teamId = "1",
                    departmentId = 1,
                    departmentDisplayId = "1",
                    departmentName = "Engineering",
                    departmentStatus = true,
                    teamCreatedAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                    teamUpdatedAt = Timestamp.valueOf("2022-11-16 1:35:21.672150"),
                )
            val linkedDesignations = listOf(designationOldData)
            every { designationRepository.getDesignationDataById(any(), any()) } returns designationOldData
            every { teamRepository.getTeamDataById(any(), any()) } returns teamData
            every { designationRepository.fetchAll(any(), any(), any(), any(), any(), any()) } returns linkedDesignations
            every { designationRepository.update(any(), any(), any(), any()) } returns Unit
            every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } returns Unit
            every {
                designationRepository.insertTeamDesignationMapping(any(), any())
            } throws Exception("Unique index violation: idx_unique_team_designation_mapping")
            val exception =
                shouldThrow<Exception> {
                    designationService.update(
                        organisationId = organisationId,
                        departmentId = departmentId,
                        teamId = teamId,
                        designationName = designationName,
                        status = status,
                        id = id,
                        userActivityData = userActivityData,
                    )
                }
            exception.message shouldBe "Invalid Input Data. Duplicate Team Designation getting inserted"
        }
    }
}
