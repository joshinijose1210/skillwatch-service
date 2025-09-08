package teams

import io.kotest.core.spec.Spec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import norm.executeCommand
import scalereal.core.departments.DepartmentRepository
import scalereal.core.designations.DesignationRepository
import scalereal.core.models.domain.Team
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.teams.TeamService
import scalereal.core.userActivity.UserActivityRepository
import scalereal.db.teams.TeamRepositoryImpl
import util.StringSpecWithDataSource
import java.io.File
import java.sql.Timestamp

class TeamRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var teamRepositoryImpl: TeamRepositoryImpl
    private val userActivity = mockk<UserActivityRepository>()
    private val designationRepository = mockk<DesignationRepository>()
    private val moduleService =
        mockk<ModuleService> {
            every { fetchModuleId(Modules.TEAMS.moduleName) } returns 3
        }
    private lateinit var teamService: TeamService
    private val departmentRepository = mockk<DepartmentRepository>()

    init {

        val now = Timestamp(System.currentTimeMillis())

        "should able to get maximum team id in an organisation" {
            val maxTeamId = teamRepositoryImpl.getMaxTeamId(organisationId = 1)
            maxTeamId shouldBe 1
        }

        "should add new team" {
            teamRepositoryImpl.create(
                organisationId = 1,
                id = 2,
                departmentId = 1,
                teamName = "HR Team",
                teamStatus = true,
            )
            teamRepositoryImpl.create(
                organisationId = 1,
                id = 3,
                departmentId = 1,
                teamName = "BE Team",
                teamStatus = false,
            )
        }

        "should count and show all existing teams linked to specific department in an organisation" {
            val organisationId = 1L
            val departmentId = listOf(1)
            val teamCount = teamRepositoryImpl.count(organisationId = organisationId, searchText = "", departmentId = departmentId)
            val actualTeam =
                teamRepositoryImpl.fetchAll(
                    organisationId = organisationId,
                    searchText = "",
                    departmentId = departmentId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )

            teamCount shouldBe actualTeam.size

            val expectedTeam =
                listOf(
                    Team(
                        departmentId = 1,
                        organisationId = 1,
                        departmentDisplayId = "1",
                        departmentName = "Engineering",
                        departmentStatus = true,
                        id = 3,
                        teamId = "3",
                        teamName = "BE Team",
                        teamStatus = false,
                        teamCreatedAt = now,
                        teamUpdatedAt = null,
                    ),
                    Team(
                        departmentId = 1,
                        organisationId = 1,
                        departmentDisplayId = "1",
                        departmentName = "Engineering",
                        departmentStatus = true,
                        id = 2,
                        teamId = "2",
                        teamName = "HR Team",
                        teamStatus = true,
                        teamCreatedAt = now,
                        teamUpdatedAt = null,
                    ),
                    Team(
                        departmentId = 1,
                        organisationId = 1,
                        departmentDisplayId = "1",
                        departmentName = "Engineering",
                        departmentStatus = true,
                        id = 1,
                        teamId = "1",
                        teamName = "Devops Team",
                        teamStatus = true,
                        teamCreatedAt = now,
                        teamUpdatedAt = null,
                    ),
                )

            actualTeam.mapIndexed { index, team ->
                team.organisationId shouldBe expectedTeam[index].organisationId
                team.teamId shouldBe expectedTeam[index].teamId
                team.teamName shouldBe expectedTeam[index].teamName
                team.teamStatus shouldBe expectedTeam[index].teamStatus
                team.departmentId shouldBe expectedTeam[index].departmentId
                team.departmentName shouldBe expectedTeam[index].departmentName
                team.departmentDisplayId shouldBe expectedTeam[index].departmentDisplayId
                team.departmentStatus shouldBe expectedTeam[index].departmentStatus
                team.teamCreatedAt shouldBeAfter expectedTeam[index].teamCreatedAt
            }
        }

        "should be able to edit teamName" {
            teamRepositoryImpl.update(organisationId = 1, id = 1, teamName = "DEVOPS Team", teamStatus = false)
        }
        "should be able to edit teamStatus" {
            teamRepositoryImpl.update(organisationId = 1, id = 3, teamName = "BE Team", teamStatus = true)
        }

        "should be able to check if team exists linked to specific department by team name and department id in an organisation" {
            val organisationId = 1L
            val teamName = "BE Team"
            val team =
                teamRepositoryImpl.isTeamExists(
                    organisationId = organisationId,
                    departmentId = 1,
                    teamName = teamName,
                )

            team.exists shouldBe true
            team.status shouldBe true
        }

        "should be able get team id by team name and department id in an organisation " {
            val teamId =
                teamRepositoryImpl.getTeamId(
                    organisationId = 1,
                    departmentId = 1,
                    teamName = "HR Team",
                )
            teamId shouldBe 2
        }

        "should search team by given team name" {
            val teamCount = teamRepositoryImpl.count(organisationId = 1, searchText = "Devops", departmentId = listOf(1))
            val actualTeam =
                teamRepositoryImpl.fetchAll(
                    organisationId = 1,
                    departmentId = listOf(1),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    searchText = "Devops",
                )

            val expectedTeam =
                listOf(
                    Team(
                        departmentId = 1,
                        organisationId = 1,
                        departmentDisplayId = "1",
                        departmentName = "Engineering",
                        departmentStatus = true,
                        id = 1,
                        teamId = "1",
                        teamName = "DEVOPS Team",
                        teamStatus = false,
                        teamCreatedAt = now,
                        teamUpdatedAt = now,
                    ),
                )

            teamCount shouldBe actualTeam.size
            actualTeam.map {
                it.teamName shouldBe expectedTeam[0].teamName
                it.teamId shouldBe expectedTeam[0].teamId
                it.teamStatus shouldBe expectedTeam[0].teamStatus
                it.teamCreatedAt shouldBeAfter expectedTeam[0].teamCreatedAt
                it.departmentId shouldBe expectedTeam[0].departmentId
                it.departmentName shouldBe expectedTeam[0].departmentName
                it.departmentDisplayId shouldBe expectedTeam[0].departmentDisplayId
                it.departmentStatus shouldBe expectedTeam[0].departmentStatus
            }
        }

        "should be able to get team data by team id" {
            val team = teamRepositoryImpl.getTeamDataById(id = 3, organisationId = 1)

            team.id shouldBe 3
            team.teamId shouldBe "3"
            team.teamName shouldBe "BE Team"
            team.teamStatus shouldBe true
            team.departmentId shouldBe 1
            team.departmentDisplayId shouldBe "1"
            team.departmentName shouldBe "Engineering"
            team.departmentStatus shouldBe true
        }

        "should be able to get count of teams that are not linked to any department" {
            val unlinkedTeamCount = teamRepositoryImpl.getUnlinkedTeamsCount(organisationId = 1)
            unlinkedTeamCount shouldBe 0
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        val testDataFile = File("./test-res/teams/team-test-data.sql").readText().trim()
        dataSource.connection.use {
            it.executeCommand(testDataFile)
        }
        teamRepositoryImpl = TeamRepositoryImpl(dataSource)
        teamService = TeamService(teamRepositoryImpl, userActivity, designationRepository, moduleService, departmentRepository)
    }
}
