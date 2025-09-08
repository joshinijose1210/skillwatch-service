package teams

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.departments.DepartmentRepository
import scalereal.core.designations.DesignationRepository
import scalereal.core.models.domain.Department
import scalereal.core.models.domain.Team
import scalereal.core.models.domain.TeamData
import scalereal.core.models.domain.TeamResults
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.teams.TeamRepository
import scalereal.core.teams.TeamService
import scalereal.core.userActivity.UserActivityRepository
import java.sql.Timestamp

class TeamServiceImplTest : StringSpec() {
    private val teamRepository = mockk<TeamRepository>()
    private val userActivityRepository = mockk<UserActivityRepository>()
    private val designationRepository = mockk<DesignationRepository>()
    private val moduleService =
        mockk<ModuleService> {
            every { fetchModuleId(Modules.TEAMS.moduleName) } returns 3
            every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } returns Unit
        }
    private val departmentRepository = mockk<DepartmentRepository>()
    private val teamService =
        TeamService(teamRepository, userActivityRepository, designationRepository, moduleService, departmentRepository)

    init {
        "should add new team" {
            val organisationId: Long = 1
            val departmentId: Long = 1
            val maxTeamId: Long = 0
            val teamName = "HR Team"
            val teamStatus = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val expectedResult =
                TeamResults(
                    existingTeam = arrayOf(),
                    addedTeam = arrayOf(teamName),
                    invalidLengthTeam = arrayOf(),
                    invalidCharTeam = arrayOf(),
                )
            every { teamRepository.isTeamExists(any(), any(), any()).exists } returns false
            every { teamRepository.getMaxTeamId(any()) } returns maxTeamId
            every { teamRepository.create(any(), any(), any(), any(), any()) } returns 1
            every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } returns Unit
            val result =
                teamService.create(
                    teams =
                        listOf(
                            TeamData(
                                organisationId = organisationId,
                                departmentId = departmentId,
                                teamName = teamName,
                                teamStatus = teamStatus,
                            ),
                        ),
                    userActivityData = userActivityData,
                )
            result.addedTeam shouldBe expectedResult.addedTeam
            result.existingTeam shouldBe expectedResult.existingTeam
            verify { teamRepository.create(organisationId, maxTeamId + 1, departmentId, teamName, teamStatus) }
        }

        "should return existed team while adding team with existing name linked to specific department" {
            val organisationId: Long = 1
            val departmentId: Long = 1
            val maxTeamId: Long = 0
            val teamName = "HR Team"
            val teamStatus = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val expectedResult =
                TeamResults(
                    existingTeam = arrayOf(teamName),
                    addedTeam = arrayOf(),
                    invalidLengthTeam = arrayOf(),
                    invalidCharTeam = arrayOf(),
                )
            every { teamRepository.isTeamExists(any(), any(), any()).exists } returns true
            val result =
                teamService.create(
                    teams =
                        listOf(
                            TeamData(
                                organisationId = organisationId,
                                departmentId = departmentId,
                                teamName = teamName,
                                teamStatus = teamStatus,
                            ),
                        ),
                    userActivityData = userActivityData,
                )
            result.addedTeam shouldBe expectedResult.addedTeam
            result.existingTeam shouldBe expectedResult.existingTeam
            verify { teamRepository.create(organisationId, maxTeamId + 1, departmentId, teamName, teamStatus) }
        }

        "should fetch all existing teams" {
            val teamData =
                listOf(
                    Team(
                        organisationId = 1,
                        departmentId = 1,
                        departmentDisplayId = "1",
                        departmentName = "Engineering",
                        departmentStatus = true,
                        id = 1,
                        teamId = "1",
                        teamName = "FE Team",
                        teamStatus = true,
                        teamCreatedAt = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                        teamUpdatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                    ),
                    Team(
                        organisationId = 1,
                        departmentId = 1,
                        departmentDisplayId = "1",
                        departmentName = "Engineering",
                        departmentStatus = true,
                        id = 2,
                        teamId = "2",
                        teamName = "BE Team",
                        teamStatus = false,
                        teamCreatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                        teamUpdatedAt = Timestamp.valueOf("2022-11-12 19:6:35.142179"),
                    ),
                )

            val expectedTeamData =
                listOf(
                    Team(
                        organisationId = 1,
                        departmentId = 1,
                        departmentDisplayId = "DEP1",
                        departmentName = "Engineering",
                        departmentStatus = true,
                        id = 1,
                        teamId = "T1",
                        teamName = "FE Team",
                        teamStatus = true,
                        teamCreatedAt = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                        teamUpdatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                    ),
                    Team(
                        organisationId = 1,
                        departmentId = 1,
                        departmentDisplayId = "DEP1",
                        departmentName = "Engineering",
                        departmentStatus = true,
                        id = 2,
                        teamId = "T2",
                        teamName = "BE Team",
                        teamStatus = false,
                        teamCreatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                        teamUpdatedAt = Timestamp.valueOf("2022-11-12 19:6:35.142179"),
                    ),
                )
            every { teamRepository.fetchAll(any(), any(), any(), any(), any()) } returns teamData
            teamService.fetchAll(
                organisationId = 1,
                page = 1,
                limit = Int.MAX_VALUE,
                searchText = "",
                departmentId = listOf(1),
            ) shouldBe expectedTeamData
            verify {
                teamRepository.fetchAll(
                    organisationId = 1,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    searchText = "",
                    departmentId = listOf(1),
                )
            }
        }

        "should get count of teams" {
            val expectedCount = 2
            every { teamRepository.count(any(), any(), any()) } returns 2
            teamService.count(organisationId = 1, searchText = "", departmentId = listOf(1)) shouldBe expectedCount
            verify { teamRepository.count(any(), any(), any()) }
        }

        "should get count of teams not linked to any department" {
            val expectedCount = 1
            every { teamRepository.getUnlinkedTeamsCount(any()) } returns 1
            teamService.getUnlinkedTeamsCount(organisationId = 1) shouldBe expectedCount
            verify { teamRepository.getUnlinkedTeamsCount(any()) }
        }

        "should be able to edit team" {
            val organisationId: Long = 1
            val departmentId: Long = 1
            val id: Long = 1
            val teamId: Long = 1
            val teamName = "Frontend Team"
            val teamStatus = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val teamData =
                Team(
                    organisationId = 1,
                    departmentId = 1,
                    departmentDisplayId = "1",
                    departmentName = "Engineering",
                    departmentStatus = true,
                    id = 1,
                    teamId = "1",
                    teamName = "FE Team",
                    teamStatus = true,
                    teamCreatedAt = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                    teamUpdatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                )
            val oldDepartmentData =
                Department(
                    id = 1,
                    organisationId = 1,
                    departmentId = "1",
                    departmentName = "Engineering",
                    departmentStatus = true,
                    departmentCreatedAt = Timestamp.valueOf("2022-11-10 18:6:35.142179"),
                    departmentUpdatedAt = null,
                )
            val activity = "Team T1 Edited"
            every { teamRepository.getTeamDataById(any(), any()) } returns teamData
            every { departmentRepository.getDepartmentDataById(any(), any()) } returns oldDepartmentData
            every { teamRepository.fetchAll(any(), any(), any(), any(), any()) } returns listOf(teamData)
            every { teamRepository.update(any(), any(), any(), any()) } returns Unit
            every {
                userActivityRepository.addActivity(any(), any(), activity = activity, any(), any())
            } returns Unit
            every {
                designationRepository.fetchAll(
                    organisationId = organisationId,
                    searchText = "",
                    departmentId = listOf(departmentId.toInt()),
                    teamId = listOf(teamId.toInt()),
                    limit = Int.MAX_VALUE,
                    offset = 0,
                )
            } returns listOf()
            teamService.update(
                organisationId = organisationId,
                id = id,
                departmentId = departmentId,
                teamName = teamName,
                teamStatus = teamStatus,
                userActivityData = userActivityData,
            ) shouldBe Unit
            verify { teamRepository.update(any(), any(), any(), any()) }
        }

        "should throw exception if team name contains special characters" {
            val organisationId: Long = 1
            val departmentId: Long = 1
            val id: Long = 1
            val invalidTeamName = "Frontend@Team"
            val teamStatus = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )

            val exception =
                shouldThrow<Exception> {
                    teamService.update(
                        organisationId = organisationId,
                        id = id,
                        departmentId = departmentId,
                        teamName = invalidTeamName,
                        teamStatus = teamStatus,
                        userActivityData = userActivityData,
                    )
                }

            exception.message shouldBe "Team name must not contain special characters."
        }

        "should throw exception while updating team name that already exist in same department" {
            val organisationId: Long = 1
            val departmentId: Long = 1
            val id: Long = 1
            val teamName = "BE Team"
            val teamStatus = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val teamData =
                Team(
                    organisationId = 1,
                    departmentId = 1,
                    departmentDisplayId = "1",
                    departmentName = "Engineering",
                    departmentStatus = true,
                    id = 1,
                    teamId = "1",
                    teamName = "FE Team",
                    teamStatus = true,
                    teamCreatedAt = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                    teamUpdatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                )
            val oldDepartmentData =
                Department(
                    id = 1,
                    organisationId = 1,
                    departmentId = "1",
                    departmentName = "Engineering",
                    departmentStatus = true,
                    departmentCreatedAt = Timestamp.valueOf("2022-11-10 18:6:35.142179"),
                    departmentUpdatedAt = null,
                )
            val teams =
                listOf(
                    Team(
                        organisationId = 1,
                        departmentId = 1,
                        departmentDisplayId = "1",
                        departmentName = "Engineering",
                        departmentStatus = true,
                        id = 1,
                        teamId = "1",
                        teamName = "FE Team",
                        teamStatus = true,
                        teamCreatedAt = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                        teamUpdatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                    ),
                    Team(
                        organisationId = 1,
                        departmentId = 1,
                        departmentDisplayId = "1",
                        departmentName = "Engineering",
                        departmentStatus = true,
                        id = 2,
                        teamId = "2",
                        teamName = "BE Team",
                        teamStatus = true,
                        teamCreatedAt = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                        teamUpdatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                    ),
                )
            every { teamRepository.getTeamDataById(any(), any()) } returns teamData
            every { departmentRepository.getDepartmentDataById(any(), any()) } returns oldDepartmentData
            every { teamRepository.fetchAll(any(), any(), any(), any(), any()) } returns teams

            val exception =
                shouldThrow<Exception> {
                    teamService.update(
                        organisationId = organisationId,
                        id = id,
                        departmentId = departmentId,
                        teamName = teamName,
                        teamStatus = teamStatus,
                        userActivityData = userActivityData,
                    )
                }
            exception.message shouldBe "Team 'BE Team' already exists in the Department DEP1"
        }

        "should throw exception while updating department of team already linked with other department" {
            val organisationId: Long = 1
            val departmentId: Long = 2
            val id: Long = 1
            val teamName = "Frontend Team"
            val teamStatus = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val teamData =
                Team(
                    organisationId = 1,
                    departmentId = 1,
                    departmentDisplayId = "1",
                    departmentName = "Engineering",
                    departmentStatus = true,
                    id = 1,
                    teamId = "1",
                    teamName = "FE Team",
                    teamStatus = true,
                    teamCreatedAt = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                    teamUpdatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                )
            val oldDepartmentData =
                Department(
                    id = 1,
                    organisationId = 1,
                    departmentId = "1",
                    departmentName = "Engineering",
                    departmentStatus = true,
                    departmentCreatedAt = Timestamp.valueOf("2022-11-10 18:6:35.142179"),
                    departmentUpdatedAt = null,
                )
            every { teamRepository.getTeamDataById(any(), any()) } returns teamData
            every { departmentRepository.getDepartmentDataById(any(), any()) } returns oldDepartmentData
            every { teamRepository.fetchAll(any(), any(), any(), any(), any()) } returns listOf(teamData)
            every {
                designationRepository.fetchAll(any(), any(), any(), any(), any(), any())
            } returns listOf()
            val exception =
                shouldThrow<Exception> {
                    teamService.update(
                        organisationId = organisationId,
                        id = id,
                        departmentId = departmentId,
                        teamName = teamName,
                        teamStatus = teamStatus,
                        userActivityData = userActivityData,
                    )
                }
            exception.message shouldBe "Team is linked with the Department DEP1 and cannot be linked with another department."
        }

        // This can be uncommented when we uncomment related service method

//        "createDefaultTeams should add default teams if not exist" {
//            val maxTeamId = 0L
//            val departmentId = 1L
//            every { teamRepository.getMaxTeamId(any()) } returns maxTeamId
//            every { departmentRepository.getDepartmentId(any(), any()) } returns departmentId
//            every { teamRepository.isTeamExists(any(), any(), any()).exists } returns false
//            every { teamRepository.create(any(), any(), any(), any(), any()) } returns Unit
//
//            teamService.createDefaultTeams(1) shouldBe Unit
//
//            verify { teamRepository.create(any(), any(), any(), any(), any()) }
//        }

        "should throw exception while linking team to department and  department is inactive" {
            val organisationId: Long = 1
            val departmentId: Long = 1
            val id: Long = 1
            val teamName = "Frontend Team"
            val teamStatus = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val teamData =
                Team(
                    organisationId = 1,
                    departmentId = null,
                    departmentDisplayId = null,
                    departmentName = null,
                    departmentStatus = null,
                    id = 1,
                    teamId = "1",
                    teamName = "FE Team",
                    teamStatus = true,
                    teamCreatedAt = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                    teamUpdatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                )
            val newDepartmentData =
                Department(
                    id = 1,
                    organisationId = 1,
                    departmentId = "1",
                    departmentName = "Engineering",
                    departmentStatus = false,
                    departmentCreatedAt = Timestamp.valueOf("2022-11-10 18:6:35.142179"),
                    departmentUpdatedAt = null,
                )
            every { teamRepository.getTeamDataById(any(), any()) } returns teamData
            every {
                designationRepository.fetchAll(any(), any(), any(), any(), any(), any())
            } returns listOf()
            every { departmentRepository.getDepartmentDataById(any(), any()) } returns newDepartmentData
            val exception =
                shouldThrow<Exception> {
                    teamService.update(
                        organisationId = organisationId,
                        id = id,
                        departmentId = departmentId,
                        teamName = teamName,
                        teamStatus = teamStatus,
                        userActivityData = userActivityData,
                    )
                }
            exception.message shouldBe "Please activate the Department DEP1 to update the team."
        }

        "should throw exception while updating team linked to inactive department" {
            val organisationId: Long = 1
            val departmentId: Long = 1
            val id: Long = 1
            val teamName = "Frontend Team"
            val teamStatus = true
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val teamData =
                Team(
                    organisationId = 1,
                    departmentId = 1,
                    departmentDisplayId = "1",
                    departmentName = "Engineering",
                    departmentStatus = true,
                    id = 1,
                    teamId = "1",
                    teamName = "FE Team",
                    teamStatus = true,
                    teamCreatedAt = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                    teamUpdatedAt = Timestamp.valueOf("2022-11-11 19:6:35.142179"),
                )
            val oldDepartmentData =
                Department(
                    id = 1,
                    organisationId = 1,
                    departmentId = "1",
                    departmentName = "Engineering",
                    departmentStatus = false,
                    departmentCreatedAt = Timestamp.valueOf("2022-11-10 18:6:35.142179"),
                    departmentUpdatedAt = null,
                )
            every { teamRepository.getTeamDataById(any(), any()) } returns teamData
            every { departmentRepository.getDepartmentDataById(any(), any()) } returns oldDepartmentData
            every { teamRepository.fetchAll(any(), any(), any(), any(), any()) } returns listOf(teamData)
            every {
                designationRepository.fetchAll(any(), any(), any(), any(), any(), any())
            } returns listOf()
            val exception =
                shouldThrow<Exception> {
                    teamService.update(
                        organisationId = organisationId,
                        id = id,
                        departmentId = departmentId,
                        teamName = teamName,
                        teamStatus = teamStatus,
                        userActivityData = userActivityData,
                    )
                }
            exception.message shouldBe "Please activate the Department DEP1 to update the team."
        }
    }
}
