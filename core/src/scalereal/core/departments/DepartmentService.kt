package scalereal.core.departments

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import scalereal.core.designations.DesignationRepository
import scalereal.core.models.containsSpecialChars
import scalereal.core.models.domain.Department
import scalereal.core.models.domain.DepartmentData
import scalereal.core.models.domain.DepartmentResults
import scalereal.core.models.domain.UserActivityData
import scalereal.core.models.removeExtraSpaces
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.teams.TeamRepository
import scalereal.core.userActivity.UserActivityRepository
import java.lang.Exception

@Singleton
class DepartmentService(
    private val departmentRepository: DepartmentRepository,
    private val userActivity: UserActivityRepository,
    moduleService: ModuleService,
    private val teamsRepository: TeamRepository,
    private val designationRepository: DesignationRepository,
) {
    private val departmentsModuleId = moduleService.fetchModuleId(Modules.DEPARTMENTS.moduleName)
    private val logger = LoggerFactory.getLogger(DepartmentService::class.java)

    fun create(
        departments: List<DepartmentData>,
        userActivityData: UserActivityData,
    ): DepartmentResults {
        try {
            val validDepartments =
                departments
                    .filter {
                        it.departmentName.length in 1..50 &&
                            !it.departmentName.containsSpecialChars()
                    }

            val invalidLengthDepartments =
                departments
                    .filter { it.departmentName.length !in 1..50 }
                    .map { it.departmentName }

            val invalidCharDepartments =
                departments
                    .filter { it.departmentName.containsSpecialChars() }
                    .map { it.departmentName }

            val existingDepartments = mutableListOf<String>()
            val addedDepartments = mutableListOf<String>()

            for ((organisationId, nameRaw, status) in validDepartments) {
                val name = nameRaw.removeExtraSpaces()
                val departmentExists = departmentRepository.isDepartmentExists(organisationId, name).exists

                if (departmentExists) {
                    existingDepartments.add(name)
                    continue
                }

                val newDepartmentId = departmentRepository.getMaxDepartmentId(organisationId) + 1

                departmentRepository.create(
                    organisationId = organisationId,
                    id = newDepartmentId,
                    departmentName = name,
                    departmentStatus = status,
                )

                addedDepartments.add(name)

                val activity =
                    if (status) {
                        "Department DEP$newDepartmentId Added and Published"
                    } else {
                        "Department DEP$newDepartmentId Added and Unpublished"
                    }
                addUserActivityLog(
                    userActivityData,
                    activity = activity,
                    description = activity,
                )
            }

            if (addedDepartments.isEmpty() &&
                existingDepartments.isEmpty() &&
                invalidLengthDepartments.isEmpty() &&
                invalidCharDepartments.isEmpty()
            ) {
                throw Exception("No department added or already exists.")
            }

            return DepartmentResults(
                existingDepartment = existingDepartments.toTypedArray(),
                addedDepartment = addedDepartments.toTypedArray(),
                invalidLengthDepartment = invalidLengthDepartments.toTypedArray(),
                invalidCharDepartment = invalidCharDepartments.toTypedArray(),
            )
        } catch (e: Exception) {
            logger.error("Exception while adding departments: $e")
            throw Exception("Something went wrong while adding departments.")
        }
    }

    fun fetchAll(
        organisationId: Long,
        searchText: String,
        page: Int,
        limit: Int,
    ): List<Department> =
        departmentRepository
            .fetchAll(
                organisationId = organisationId,
                searchText = searchText,
                offset = (page - 1) * limit,
                limit = limit,
            ).map {
                Department(
                    organisationId = it.organisationId,
                    id = it.id,
                    departmentId = "DEP${it.departmentId}",
                    departmentName = it.departmentName,
                    departmentStatus = it.departmentStatus,
                    departmentCreatedAt = it.departmentCreatedAt,
                    departmentUpdatedAt = it.departmentUpdatedAt,
                )
            }

    fun departmentCount(
        organisationId: Long,
        searchText: String,
    ): Int = departmentRepository.departmentCount(organisationId = organisationId, searchText = searchText)

    fun update(
        organisationId: Long,
        departmentId: Long,
        departmentName: String,
        departmentStatus: Boolean,
        userActivityData: UserActivityData,
    ) {
        if (departmentName.length !in 1..50) {
            throw Exception("Department name must be between 1 and 50 characters.")
        }
        if (departmentName.containsSpecialChars()) {
            throw Exception("Department name must not contain special characters.")
        }
        val departmentData = departmentRepository.getDepartmentDataById(departmentId, organisationId)
        val teamsLinked =
            teamsRepository.fetchAll(
                organisationId = organisationId,
                searchText = "",
                departmentId = listOf(departmentId.toInt()),
                limit = Int.MAX_VALUE,
                offset = 0,
            )
        val name = departmentName.removeExtraSpaces()
        val activity =
            when {
                departmentData.departmentName != name && departmentData.departmentStatus && !departmentStatus ->
                    "Department DEP${departmentData.departmentId} Edited and Unpublished"
                departmentData.departmentName != name && !departmentData.departmentStatus && departmentStatus ->
                    "Department DEP${departmentData.departmentId} Edited and Published"
                !departmentData.departmentStatus && departmentStatus ->
                    "Department DEP${departmentData.departmentId} Published"
                departmentData.departmentStatus && !departmentStatus ->
                    "Department DEP${departmentData.departmentId} and all the teams linked with it are Unpublished"
                departmentData.departmentName != name ->
                    "Department DEP${departmentData.departmentId} Edited"
                else -> null
            }
        try {
            departmentRepository.update(
                organisationId = organisationId,
                departmentId = departmentId,
                departmentName = name,
                departmentStatus = departmentStatus,
            )
            if (departmentData.departmentStatus && !departmentStatus) {
                teamsLinked.forEach { team ->
                    teamsRepository.update(organisationId = organisationId, id = team.id, teamName = team.teamName, teamStatus = false)
                    designationRepository
                        .fetchAll(
                            organisationId,
                            searchText = "",
                            departmentId = listOf(departmentId.toInt()),
                            teamId = listOf(team.id.toInt()),
                            limit = Int.MAX_VALUE,
                            offset = 0,
                        ).forEach { designation ->
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
            }
            if (activity != null) addUserActivityLog(userActivityData, activity = activity, description = activity)
        } catch (_: Exception) {
            throw Exception("Department $name already exists")
        }
    }

    // This can be uncommented if need to create all default departments
//    fun createDefaultDepartments(organisationId: Long) {
//        val departments: List<String> = listOf(Departments.EXECUTIVE_LEADERSHIP, Departments.HUMAN_RESOURCE)
//        departments.map { department ->
//            if (!departmentRepository.isDepartmentExists(organisationId = organisationId, department).exists) {
//                val maxDepartmentId = departmentRepository.getMaxDepartmentId(organisationId)
//                departmentRepository.create(
//                    organisationId = organisationId,
//                    id = maxDepartmentId + 1,
//                    departmentName = department,
//                    departmentStatus = true,
//                )
//            }
//        }
//    }

    private fun addUserActivityLog(
        userActivityData: UserActivityData,
        activity: String,
        description: String,
    ) {
        userActivity.addActivity(
            actionBy = userActivityData.actionBy,
            moduleId = departmentsModuleId,
            activity = activity,
            description = description,
            ipAddress = userActivityData.ipAddress,
        )
    }
}
