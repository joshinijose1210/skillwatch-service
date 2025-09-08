package scalereal.core.designations

import jakarta.inject.Singleton
import scalereal.core.departments.DepartmentRepository
import scalereal.core.exception.DuplicateDataException
import scalereal.core.models.containsSpecialChars
import scalereal.core.models.domain.Designation
import scalereal.core.models.domain.DesignationData
import scalereal.core.models.domain.DesignationResults
import scalereal.core.models.domain.Team
import scalereal.core.models.domain.UserActivityData
import scalereal.core.models.removeExtraSpaces
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.teams.TeamRepository
import scalereal.core.userActivity.UserActivityRepository
import java.lang.Exception

@Singleton
class DesignationService(
    private val repository: DesignationRepository,
    private val userActivityRepository: UserActivityRepository,
    private val teamRepository: TeamRepository,
    moduleService: ModuleService,
    private val departmentRepository: DepartmentRepository,
) {
    private val designationModuleId = moduleService.fetchModuleId(Modules.DESIGNATIONS.moduleName)

    fun create(
        designations: List<DesignationData>,
        userActivityData: UserActivityData,
    ): DesignationResults {
        try {
            val validDesignations =
                designations
                    .filter {
                        it.designationName.length in 1..50 &&
                            !it.designationName.containsSpecialChars()
                    }
            val invalidLengthDesignations =
                designations
                    .filter { it.designationName.length !in 1..50 }
                    .map { it.designationName }

            val invalidCharDesignations =
                designations
                    .filter { it.designationName.containsSpecialChars() }
                    .map { it.designationName }

            val existingDesignations = mutableListOf<String>()
            val addedDesignations = mutableListOf<String>()
            for ((organisationId, teamId, nameRaw, status) in validDesignations) {
                val name = nameRaw.removeExtraSpaces()
                val isDesignationExists = repository.isDesignationExists(organisationId = organisationId, teamId, name).exists
                if (isDesignationExists) {
                    existingDesignations.add(name)
                    continue
                }

                val newDesignationId = repository.getMaxDesignationId(organisationId = organisationId) + 1
                repository.create(
                    id = newDesignationId,
                    organisationId = organisationId,
                    teamId = teamId,
                    designationName = name,
                    status = status,
                )
                addedDesignations.add(name)

                val activity =
                    if (status) {
                        "Designation D$newDesignationId Added and Published"
                    } else {
                        "Designation D$newDesignationId Added and Unpublished"
                    }
                userActivityRepository.addActivity(
                    actionBy = userActivityData.actionBy,
                    moduleId = designationModuleId,
                    activity = activity,
                    description = activity,
                    ipAddress = userActivityData.ipAddress,
                )
            }
            if (addedDesignations.isEmpty() &&
                existingDesignations.isEmpty() &&
                invalidLengthDesignations.isEmpty() &&
                invalidCharDesignations.isEmpty()
            ) {
                throw Exception("No Designations added or already exists.")
            }
            return DesignationResults(
                existingDesignations.toTypedArray(),
                addedDesignations.toTypedArray(),
                invalidLengthDesignations.toTypedArray(),
                invalidCharDesignations.toTypedArray(),
            )
        } catch (e: Exception) {
            when {
                e.localizedMessage.contains("idx_unique_team_designation_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate Team Designation getting inserted")
                else -> throw e
            }
        }
    }

    fun fetchAll(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
        teamId: List<Int>,
        page: Int,
        limit: Int,
    ): List<Designation> =
        repository
            .fetchAll(
                organisationId = organisationId,
                searchText = searchText,
                departmentId = departmentId,
                teamId = teamId,
                offset = (page - 1) * limit,
                limit = limit,
            ).map {
                Designation(
                    organisationId = it.organisationId,
                    departmentId = it.departmentId,
                    departmentDisplayId = it.getDepartmentsDisplayId(),
                    departmentName = it.departmentName,
                    departmentStatus = it.departmentStatus,
                    teamId = it.teamId,
                    teamName = it.teamName,
                    teamDisplayId = it.getTeamsDisplayId(),
                    teamStatus = it.teamStatus,
                    id = it.id,
                    designationId = it.getDesignationsDisplayId(),
                    designationName = it.designationName,
                    status = it.status,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            }

    fun count(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
        teamId: List<Int>,
    ): Int = repository.count(organisationId = organisationId, searchText = searchText, departmentId = departmentId, teamId = teamId)

    fun unlinkedDesignationsCount(organisationId: Long): Int = repository.unlinkedDesignationsCount(organisationId)

    fun update(
        organisationId: Long,
        departmentId: Long,
        teamId: Long,
        id: Long,
        designationName: String,
        status: Boolean,
        userActivityData: UserActivityData,
    ) {
        try {
            if (designationName.length !in 1..50) {
                throw Exception("Designation name must be between 1 and 50 characters.")
            }
            if (designationName.containsSpecialChars()) {
                throw Exception("Designation name must not contain special characters.")
            }
            val designationOldData = repository.getDesignationDataById(id = id, organisationId = organisationId)
            val oldTeamData =
                if (designationOldData.teamId != null) {
                    teamRepository.getTeamDataById(
                        organisationId = organisationId,
                        id = designationOldData.teamId,
                    )
                } else {
                    null
                }
            val name = designationName.removeExtraSpaces()
            val activity =
                getActivityDescription(designationOldData = designationOldData, designationName = name, status = status)

            if (designationOldData.teamId != null) {
                checkForExistingDesignationInTeam(
                    organisationId = organisationId,
                    designationName = name,
                    id = id,
                    departmentId = departmentId,
                    teamId = designationOldData.teamId,
                    teamDisplayId = designationOldData.teamDisplayId,
                )
            }

            validateAndUpdateDesignation(
                organisationId = organisationId,
                designationOldData = designationOldData,
                oldTeamData = oldTeamData,
                departmentId = departmentId,
                newTeamId = teamId,
                id = id,
                designationName = name,
                status = status,
                userActivityData = userActivityData,
                activity = activity,
            )
            if (designationOldData.teamId == null) {
                repository.insertTeamDesignationMapping(
                    designationId = id,
                    teamId = teamId,
                )
            }
        } catch (e: Exception) {
            when {
                e.localizedMessage.contains("idx_unique_team_designation_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate Team Designation getting inserted")
                else -> throw e
            }
        }
    }

    // This can be uncommented if need to create all default designations
//    fun createDefaultDesignations(organisationId: Long) {
//        val designations: List<String> = listOf(Designations.ORG_ADMIN, Designations.HR_MANAGER, Designations.SENIOR_HR)
//        var departmentId: Long
//        var teamId: Long
//        var maxDesignationId: Long
//
//        designations.map { designation ->
//            maxDesignationId = repository.getMaxDesignationId(organisationId = organisationId)
//            when (designation) {
//                Designations.ORG_ADMIN -> {
//                    departmentId =
//                        departmentRepository.getDepartmentId(
//                            organisationId = organisationId, departmentName = Departments.EXECUTIVE_LEADERSHIP,
//                        )
//                    teamId =
//                        teamRepository.getTeamId(
//                            organisationId = organisationId, departmentId = departmentId, teamName = Teams.ORG_ADMIN,
//                        )
//                    if (!repository.isDesignationExists(
//                            organisationId = organisationId,
//                            teamId = teamId,
//                            designationName = designation,
//                        ).exists
//                    ) {
//                        repository.create(
//                            id = maxDesignationId + 1,
//                            organisationId = organisationId,
//                            designationName = designation,
//                            status = true,
//                            teamId = teamId,
//                        )
//                    }
//                }
//                Designations.HR_MANAGER -> {
//                    departmentId =
//                        departmentRepository.getDepartmentId(
//                            organisationId = organisationId, departmentName = Departments.HUMAN_RESOURCE,
//                        )
//                    teamId =
//                        teamRepository.getTeamId(
//                            organisationId = organisationId, departmentId = departmentId, teamName = Teams.HUMAN_RESOURCES,
//                        )
//                    if (!repository.isDesignationExists(
//                            organisationId = organisationId,
//                            teamId = teamId,
//                            designationName = designation,
//                        ).exists
//                    ) {
//                        repository.create(
//                            id = maxDesignationId + 1,
//                            organisationId = organisationId,
//                            designationName = designation,
//                            status = true,
//                            teamId = teamId,
//                        )
//                    }
//                }
//                Designations.SENIOR_HR -> {
//                    departmentId =
//                        departmentRepository.getDepartmentId(
//                            organisationId = organisationId, departmentName = Departments.HUMAN_RESOURCE,
//                        )
//                    teamId =
//                        teamRepository.getTeamId(
//                            organisationId = organisationId, departmentId = departmentId, teamName = Teams.HUMAN_RESOURCES,
//                        )
//                    if (!repository.isDesignationExists(
//                            organisationId = organisationId,
//                            teamId = teamId,
//                            designationName = designation,
//                        ).exists
//                    ) {
//                        repository.create(
//                            id = maxDesignationId + 1,
//                            organisationId = organisationId,
//                            designationName = designation,
//                            status = true,
//                            teamId = teamId,
//                        )
//                    }
//                }
//            }
//        }
//    }

    private fun getActivityDescription(
        designationOldData: Designation,
        designationName: String,
        status: Boolean,
    ): String? =
        when {
            designationOldData.designationName != designationName && designationOldData.status && !status ->
                "Designation D${designationOldData.designationId} Edited and Unpublished"
            designationOldData.designationName != designationName && !designationOldData.status && status ->
                "Designation D${designationOldData.designationId} Edited and Published"
            designationOldData.status && !status ->
                "Designation D${designationOldData.designationId} Unpublished"
            !designationOldData.status && status ->
                "Designation D${designationOldData.designationId} Published"
            designationOldData.designationName != designationName ->
                "Designation D${designationOldData.designationId} Edited"
            else -> null
        }

    private fun checkForExistingDesignationInTeam(
        organisationId: Long,
        id: Long,
        departmentId: Long,
        designationName: String,
        teamId: Long,
        teamDisplayId: String?,
    ) {
        val existingDesignationsInTeam =
            repository.fetchAll(
                organisationId,
                searchText = "",
                departmentId = listOf(departmentId.toInt()),
                teamId = listOf(teamId.toInt()),
                limit = Int.MAX_VALUE,
                offset = 0,
            )
        for (designation in existingDesignationsInTeam) {
            if (designation.designationName.lowercase() == designationName.lowercase() && designation.id != id) {
                throw Exception("Designation '$designationName' already exists in the Team T$teamDisplayId")
            }
        }
    }

    private fun validateAndUpdateDesignation(
        designationOldData: Designation,
        oldTeamData: Team?,
        organisationId: Long,
        id: Long,
        departmentId: Long,
        newTeamId: Long,
        designationName: String,
        status: Boolean,
        userActivityData: UserActivityData,
        activity: String?,
    ) {
        if (designationOldData.teamId != null && designationOldData.teamId != newTeamId) {
            throw Exception(
                "Designation is linked with the Team T${designationOldData.teamDisplayId} and cannot be linked with another team.",
            )
        } else if (designationOldData.teamId == null) {
            val newTeamData = teamRepository.getTeamDataById(organisationId = organisationId, id = newTeamId)
            if (!newTeamData.teamStatus) {
                throw Exception("Please activate the Team T${newTeamData.teamId} to update the designation.")
            } else {
                checkForExistingDesignationInTeam(
                    organisationId = organisationId,
                    id = id,
                    departmentId = departmentId,
                    designationName = designationName,
                    teamId = newTeamId,
                    teamDisplayId = newTeamData.teamId,
                )
            }
        } else if (oldTeamData != null && !oldTeamData.teamStatus) {
            throw Exception("Please activate the Team T${designationOldData.teamDisplayId} to update the designation.")
        } else {
            repository.update(organisationId = organisationId, id = id, designationName = designationName, status = status)
            if (activity != null) {
                userActivityRepository.addActivity(
                    actionBy = userActivityData.actionBy,
                    moduleId = designationModuleId,
                    activity = activity,
                    description = activity,
                    ipAddress = userActivityData.ipAddress,
                )
            }
        }
    }
}
