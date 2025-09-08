package scalereal.core.teams

import jakarta.inject.Singleton
import scalereal.core.departments.DepartmentRepository
import scalereal.core.designations.DesignationRepository
import scalereal.core.exception.DuplicateDataException
import scalereal.core.models.containsSpecialChars
import scalereal.core.models.domain.Department
import scalereal.core.models.domain.Team
import scalereal.core.models.domain.TeamData
import scalereal.core.models.domain.TeamResults
import scalereal.core.models.domain.UserActivityData
import scalereal.core.models.removeExtraSpaces
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.userActivity.UserActivityRepository
import java.lang.Exception

@Singleton
class TeamService(
    private val teamRepository: TeamRepository,
    private val userActivity: UserActivityRepository,
    private val designationRepository: DesignationRepository,
    moduleService: ModuleService,
    private val departmentRepository: DepartmentRepository,
) {
    private val teamsModuleId = moduleService.fetchModuleId(Modules.TEAMS.moduleName)

    fun create(
        teams: List<TeamData>,
        userActivityData: UserActivityData,
    ): TeamResults {
        try {
            val validTeams =
                teams
                    .filter {
                        it.teamName.length in 1..50 &&
                            !it.teamName.containsSpecialChars()
                    }

            val invalidLengthTeams =
                teams
                    .filter { it.teamName.length !in 1..50 }
                    .map { it.teamName }

            val invalidCharTeams =
                teams
                    .filter { it.teamName.containsSpecialChars() }
                    .map { it.teamName }

            val existingTeams = mutableListOf<String>()
            val addedTeams = mutableListOf<String>()
            for ((organisationId, departmentId, nameRaw, status) in validTeams) {
                val name = nameRaw.removeExtraSpaces()
                val teamExists = teamRepository.isTeamExists(organisationId, departmentId, name).exists
                if (teamExists) {
                    existingTeams.add(name)
                    continue
                }

                val newTeamId = teamRepository.getMaxTeamId(organisationId) + 1
                teamRepository.create(
                    organisationId = organisationId,
                    id = newTeamId,
                    departmentId = departmentId,
                    teamName = name,
                    teamStatus = status,
                )
                addedTeams.add(name)
                val activity =
                    if (status) {
                        "Team T$newTeamId Added and Published"
                    } else {
                        "Team T$newTeamId Added and Unpublished"
                    }
                addUserActivityLog(userActivityData, activity = activity, description = activity)
            }
            if (addedTeams.isEmpty() && existingTeams.isEmpty() && invalidLengthTeams.isEmpty() && invalidCharTeams.isEmpty()) {
                throw Exception("No Team added or already exists.")
            }
            return TeamResults(
                existingTeams.toTypedArray(),
                addedTeams.toTypedArray(),
                invalidLengthTeams.toTypedArray(),
                invalidCharTeams.toTypedArray(),
            )
        } catch (e: Exception) {
            when {
                e.localizedMessage.contains("idx_unique_department_team_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate Department Team getting inserted")
                else -> throw e
            }
        }
    }

    fun fetchAll(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
        page: Int,
        limit: Int,
    ): List<Team> =
        teamRepository
            .fetchAll(
                organisationId = organisationId,
                searchText = searchText,
                departmentId = departmentId,
                offset = (page - 1) * limit,
                limit = limit,
            ).map {
                Team(
                    organisationId = it.organisationId,
                    id = it.id,
                    teamId = "T${it.teamId}",
                    teamName = it.teamName,
                    teamStatus = it.teamStatus,
                    departmentId = it.departmentId,
                    departmentDisplayId = it.fetchDepartmentDisplayId(),
                    departmentName = it.departmentName,
                    departmentStatus = it.departmentStatus,
                    teamCreatedAt = it.teamCreatedAt,
                    teamUpdatedAt = it.teamUpdatedAt,
                )
            }

    fun count(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
    ): Int =
        teamRepository.count(
            organisationId = organisationId,
            searchText = searchText,
            departmentId = departmentId,
        )

    fun getUnlinkedTeamsCount(organisationId: Long): Int = teamRepository.getUnlinkedTeamsCount(organisationId)

    fun update(
        organisationId: Long,
        id: Long,
        departmentId: Long,
        teamName: String,
        teamStatus: Boolean,
        userActivityData: UserActivityData,
    ) {
        try {
            if (teamName.length !in 1..50) {
                throw Exception("Team name must be between 1 and 50 characters.")
            }
            if (teamName.containsSpecialChars()) {
                throw Exception("Team name must not contain special characters.")
            }
            val teamData = teamRepository.getTeamDataById(id, organisationId)
            val oldDepartmentData =
                if (teamData.departmentId != null) {
                    departmentRepository.getDepartmentDataById(departmentId = teamData.departmentId, organisationId = organisationId)
                } else {
                    null
                }
            val name = teamName.removeExtraSpaces()
            val activity = getActivityDescription(teamData = teamData, teamName = name, teamStatus = teamStatus)
            if (teamData.departmentId != null) {
                checkForExistingTeamInDepartment(
                    organisationId = organisationId,
                    teamName = name,
                    id = id,
                    departmentId = teamData.departmentId,
                    departmentDisplayId = teamData.departmentDisplayId,
                )
            }
            validateAndUpdateTeam(
                organisationId = organisationId,
                teamOldData = teamData,
                oldDepartmentData = oldDepartmentData,
                newDepartmentId = departmentId,
                id = id,
                teamName = name,
                teamStatus = teamStatus,
                userActivityData = userActivityData,
                activity = activity,
            )
            if (teamData.departmentId == null) {
                teamRepository.insertDepartmentTeamMapping(departmentId = departmentId, teamId = id)
            }
        } catch (e: Exception) {
            when {
                e.localizedMessage.contains("idx_unique_department_team_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate Department Team getting inserted")
                else -> throw e
            }
        }
    }

    // This can be uncommented if need to create all default teams

//    /**
//     * If there are any changes in the default teams,
//     * Please update the team names accordingly in designationService > fun createDefaultDesignation.
//     */

//    fun createDefaultTeams(organisationId: Long) {
//        val teams: List<String> = listOf(Teams.ORG_ADMIN, Teams.HUMAN_RESOURCES)
//        var departmentId: Long
//        var maxTeamId: Long
//
//        teams.map { team ->
//            maxTeamId = teamRepository.getMaxTeamId(organisationId)
//            when (team) {
//                Teams.ORG_ADMIN -> {
//                    departmentId =
//                        departmentRepository.getDepartmentId(
//                            organisationId = organisationId,
//                            departmentName = Departments.EXECUTIVE_LEADERSHIP,
//                        )
//                    if (!teamRepository.isTeamExists(organisationId, departmentId, team).exists) {
//                        teamRepository.create(
//                            id = maxTeamId + 1,
//                            organisationId = organisationId,
//                            teamName = team,
//                            teamStatus = true,
//                            departmentId = departmentId,
//                        )
//                    }
//                }
//                Teams.HUMAN_RESOURCES -> {
//                    departmentId =
//                        departmentRepository.getDepartmentId(
//                            organisationId = organisationId, departmentName = Departments.HUMAN_RESOURCE,
//                        )
//                    if (!teamRepository.isTeamExists(organisationId, departmentId, team).exists) {
//                        teamRepository.create(
//                            id = maxTeamId + 1,
//                            organisationId = organisationId,
//                            teamName = team,
//                            teamStatus = true,
//                            departmentId = departmentId,
//                        )
//                    }
//                }
//            }
//        }
//    }

    private fun getActivityDescription(
        teamData: Team,
        teamName: String,
        teamStatus: Boolean,
    ): String? =
        when {
            teamData.teamName != teamName && teamData.teamStatus && !teamStatus -> "Team T${teamData.teamId} Edited and Unpublished"
            teamData.teamName != teamName && !teamData.teamStatus && teamStatus -> "Team T${teamData.teamId} Edited and Published"
            teamData.teamStatus && !teamStatus -> "Team T${teamData.teamId} and all the Designations linked with it are Unpublished"
            !teamData.teamStatus && teamStatus -> "Team T${teamData.teamId} Published"
            teamData.teamName != teamName -> "Team T${teamData.teamId} Edited"
            else -> null
        }

    private fun checkForExistingTeamInDepartment(
        organisationId: Long,
        id: Long,
        teamName: String,
        departmentId: Long,
        departmentDisplayId: String?,
    ) {
        val existingTeamInDepartment =
            teamRepository.fetchAll(
                organisationId,
                searchText = "",
                departmentId = listOf(departmentId.toInt()),
                limit = Int.MAX_VALUE,
                offset = 0,
            )
        for (team in existingTeamInDepartment) {
            if (team.teamName.lowercase() == teamName.lowercase() && team.id != id) {
                throw Exception("Team '$teamName' already exists in the Department DEP$departmentDisplayId")
            }
        }
    }

    private fun validateAndUpdateTeam(
        teamOldData: Team,
        oldDepartmentData: Department?,
        organisationId: Long,
        id: Long,
        newDepartmentId: Long,
        teamName: String,
        teamStatus: Boolean,
        userActivityData: UserActivityData,
        activity: String?,
    ) {
        val designationsLinked =
            designationRepository.fetchAll(
                organisationId = organisationId,
                searchText = "",
                departmentId = listOf(newDepartmentId.toInt()),
                teamId = listOf(id.toInt()),
                limit = Int.MAX_VALUE,
                offset = 0,
            )
        if (teamOldData.departmentId != null && teamOldData.departmentId != newDepartmentId) {
            throw Exception(
                "Team is linked with the Department DEP${teamOldData.departmentDisplayId} and cannot be linked with another department.",
            )
        } else if (teamOldData.departmentId == null) {
            val newDepartmentData =
                departmentRepository.getDepartmentDataById(
                    organisationId = organisationId,
                    departmentId = newDepartmentId,
                )
            if (!newDepartmentData.departmentStatus) {
                throw Exception("Please activate the Department DEP${newDepartmentData.departmentId} to update the team.")
            } else {
                checkForExistingTeamInDepartment(
                    organisationId = organisationId,
                    id = id,
                    teamName = teamName,
                    departmentId = newDepartmentId,
                    departmentDisplayId = newDepartmentData.departmentId,
                )
            }
        } else if (oldDepartmentData != null && !oldDepartmentData.departmentStatus) {
            throw Exception("Please activate the Department DEP${teamOldData.departmentDisplayId} to update the team.")
        } else {
            teamRepository.update(organisationId = organisationId, id = id, teamName = teamName, teamStatus = teamStatus)
            if (teamOldData.teamStatus && !teamStatus) {
                for (designation in designationsLinked) {
                    designation.id?.let {
                        designationRepository.update(
                            organisationId = organisationId,
                            id = it,
                            designationName = designation.designationName,
                            status = false,
                        )
                    }
                }
            }
            if (activity != null) {
                addUserActivityLog(userActivityData = userActivityData, activity = activity, description = activity)
            }
        }
    }

    private fun addUserActivityLog(
        userActivityData: UserActivityData,
        activity: String,
        description: String,
    ) {
        userActivity.addActivity(
            actionBy = userActivityData.actionBy,
            moduleId = teamsModuleId,
            activity = activity,
            description = description,
            ipAddress = userActivityData.ipAddress,
        )
    }
}
