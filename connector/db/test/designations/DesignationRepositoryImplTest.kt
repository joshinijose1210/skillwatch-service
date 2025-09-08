package designations

import io.kotest.core.spec.Spec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import norm.executeCommand
import scalereal.core.departments.DepartmentRepository
import scalereal.core.designations.DesignationService
import scalereal.core.models.domain.Designation
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.teams.TeamRepository
import scalereal.core.userActivity.UserActivityRepository
import scalereal.db.designations.DesignationRepositoryImpl
import util.StringSpecWithDataSource
import java.io.File
import java.sql.Timestamp

class DesignationRepositoryImplTest : StringSpecWithDataSource() {
    private val userActivity = mockk<UserActivityRepository>()
    private val moduleService =
        mockk<ModuleService> {
            every { fetchModuleId(Modules.DESIGNATIONS.moduleName) } returns 4
        }
    private val teamRepository = mockk<TeamRepository>()
    private lateinit var designationRepositoryImpl: DesignationRepositoryImpl
    private lateinit var designationService: DesignationService
    private val departmentRepository = mockk<DepartmentRepository>()

    init {
        val now = Timestamp(System.currentTimeMillis())

        "should able to get maximum designation id in an organisation" {
            val maxTeamId = designationRepositoryImpl.getMaxDesignationId(organisationId = 1)
            maxTeamId shouldBe 0
        }

        "should add new designation" {
            // TODO - Need to found a way to check if a record is added in db
            designationRepositoryImpl.create(
                organisationId = 1,
                id = 1,
                designationName = "SDE - Trainee",
                teamId = 1,
                status = false,
            ) shouldBe 1
        }

        "should count and fetch all designations" {
            val organisationId = 1L
            val departmentId = listOf(-99)
            val teamId = listOf(-99)
            val expectedDesignations =
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
                        designationName = "SDE - Trainee",
                        status = false,
                        createdAt = now,
                        updatedAt = null,
                    ),
                )
            val count =
                designationRepositoryImpl.count(
                    organisationId = organisationId,
                    searchText = "",
                    teamId = teamId,
                    departmentId = departmentId,
                )
            val actualDesignations =
                designationRepositoryImpl.fetchAll(
                    organisationId = organisationId,
                    searchText = "",
                    departmentId = departmentId,
                    teamId = teamId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            count shouldBe actualDesignations.size

            assertDesignationsMatch(expected = expectedDesignations, actual = actualDesignations)
        }

        "should be able to check if designation exists by designation name and team id" {
            val isDesignation = designationRepositoryImpl.isDesignationExists(1, 1, "SDE - 1")
            isDesignation.exists shouldBe false
            isDesignation.status shouldBe false
        }

        "should be able to add new designation" {
            designationRepositoryImpl.create(
                id = 2,
                organisationId = 1,
                teamId = 1,
                designationName = "SDE - 2",
                status = false,
            ) shouldBe 2
        }

        "should update existing designation name and status" {
            designationRepositoryImpl.update(
                organisationId = 1,
                id = 2,
                designationName = "SDE - 1",
                status = true,
            ) shouldBe Unit
        }

        "should be able to check if designation exists and its status by designation name and team id" {
            val isDesignation = designationRepositoryImpl.isDesignationExists(1, 1, "SDE - 1")
            isDesignation.exists shouldBe true
            isDesignation.status shouldBe true
        }

        "should return empty list if no designation is linked with given team name" {
            designationRepositoryImpl.fetchAll(
                organisationId = 1,
                searchText = "Frontend",
                departmentId = listOf(-99),
                teamId = listOf(-99),
                offset = 0,
                limit = Int.MAX_VALUE,
            ) shouldBe emptyList()
        }

        "should fetch designations by Department name" {
            val expectedDesignations =
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
                        id = 2,
                        designationId = "2",
                        designationName = "SDE - 1",
                        status = true,
                        createdAt = now,
                        updatedAt = now,
                    ),
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
                        designationName = "SDE - Trainee",
                        status = false,
                        createdAt = now,
                        updatedAt = null,
                    ),
                )
            val actualDesignations =
                designationRepositoryImpl.fetchAll(
                    organisationId = 1,
                    searchText = "Engineering",
                    departmentId = listOf(-99),
                    teamId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            assertDesignationsMatch(expected = expectedDesignations, actual = actualDesignations)
        }

        "should fetch designation while searching by designation name" {
            val expectedDesignations =
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
                        designationName = "SDE - Trainee",
                        status = false,
                        createdAt = now,
                        updatedAt = null,
                    ),
                )
            val actualDesignations =
                designationRepositoryImpl.fetchAll(
                    organisationId = 1,
                    searchText = "Trainee",
                    departmentId = listOf(-99),
                    teamId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            assertDesignationsMatch(expected = expectedDesignations, actual = actualDesignations)
        }

        "should add new designation with same name in another team" {
            designationRepositoryImpl.create(
                organisationId = 1,
                id = 3,
                designationName = "SDE - Trainee",
                teamId = 2,
                status = true,
            ) shouldBe 3
        }

        "should fetch designation by team name" {
            val expectedDesignations =
                listOf(
                    Designation(
                        organisationId = 1,
                        departmentId = 1,
                        departmentName = "Engineering",
                        departmentDisplayId = "1",
                        departmentStatus = true,
                        teamId = 2,
                        teamName = "Frontend",
                        teamDisplayId = "2",
                        teamStatus = true,
                        id = 3,
                        designationId = "3",
                        designationName = "SDE - Trainee",
                        status = true,
                        createdAt = now,
                        updatedAt = null,
                    ),
                )
            val actualDesignations =
                designationRepositoryImpl.fetchAll(
                    organisationId = 1,
                    searchText = "Frontend",
                    departmentId = listOf(-99),
                    teamId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            assertDesignationsMatch(expected = expectedDesignations, actual = actualDesignations)
        }

        "should fetch designation id by designation name and team id" {
            designationRepositoryImpl.getDesignationId(
                organisationId = 1,
                teamId = 2,
                designationName = "SDE - Trainee",
            ) shouldBe 3
        }

        "should fetch designation data by designation id" {
            val expectedDesignation =
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
                    id = 2,
                    designationId = "2",
                    designationName = "SDE - 1",
                    status = true,
                    createdAt = now,
                    updatedAt = now,
                )
            val actualDesignation = designationRepositoryImpl.getDesignationDataById(id = 2, organisationId = 1)

            actualDesignation.id shouldBe expectedDesignation.id
            actualDesignation.designationId shouldBe expectedDesignation.designationId
            actualDesignation.designationName shouldBe expectedDesignation.designationName
            actualDesignation.status shouldBe expectedDesignation.status
            actualDesignation.createdAt shouldBeAfter expectedDesignation.createdAt
            actualDesignation.teamId shouldBe expectedDesignation.teamId
            actualDesignation.teamName shouldBe expectedDesignation.teamName
            actualDesignation.teamDisplayId shouldBe expectedDesignation.teamDisplayId
            actualDesignation.teamStatus shouldBe expectedDesignation.teamStatus
            actualDesignation.departmentId shouldBe expectedDesignation.departmentId
            actualDesignation.departmentName shouldBe expectedDesignation.departmentName
            actualDesignation.departmentDisplayId shouldBe expectedDesignation.departmentDisplayId
            actualDesignation.departmentStatus shouldBe expectedDesignation.departmentStatus
            actualDesignation.organisationId shouldBe expectedDesignation.organisationId
        }

        "should be able to count designations which are not linked to any team" {
            designationRepositoryImpl.unlinkedDesignationsCount(organisationId = 1) shouldBe 0
        }

        "should verify if all designations have active KPIs for each KRA" {
            val organisationId = 1L
            val result = designationRepositoryImpl.doAllDesignationsHaveActiveKPIsForEachKRA(organisationId)
            result shouldBe false
        }
    }

    private fun assertDesignationsMatch(
        expected: List<Designation>,
        actual: List<Designation>,
    ) {
        actual.forEachIndexed { index, actualDesignation ->
            val expectedDesignation = expected[index]

            actualDesignation.id shouldBe expectedDesignation.id
            actualDesignation.designationId shouldBe expectedDesignation.designationId
            actualDesignation.designationName shouldBe expectedDesignation.designationName
            actualDesignation.status shouldBe expectedDesignation.status
            actualDesignation.createdAt shouldBeAfter expectedDesignation.createdAt
            actualDesignation.teamId shouldBe expectedDesignation.teamId
            actualDesignation.teamName shouldBe expectedDesignation.teamName
            actualDesignation.teamDisplayId shouldBe expectedDesignation.teamDisplayId
            actualDesignation.teamStatus shouldBe expectedDesignation.teamStatus
            actualDesignation.departmentId shouldBe expectedDesignation.departmentId
            actualDesignation.departmentName shouldBe expectedDesignation.departmentName
            actualDesignation.departmentDisplayId shouldBe expectedDesignation.departmentDisplayId
            actualDesignation.departmentStatus shouldBe expectedDesignation.departmentStatus
            actualDesignation.organisationId shouldBe expectedDesignation.organisationId
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        val testDataFile = File("./test-res/designations/designations-data.sql").readText().trim()
        dataSource.connection.use {
            it.executeCommand(
                testDataFile,
            )
        }
        designationRepositoryImpl = DesignationRepositoryImpl(dataSource)
        designationService =
            DesignationService(
                designationRepositoryImpl,
                userActivity,
                teamRepository,
                moduleService,
                departmentRepository,
            )
    }
}
