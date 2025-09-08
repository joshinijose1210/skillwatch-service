package scalereal.db.employees

import employees.AddEmployeeDesignationMappingParams
import employees.AddEmployeeDesignationMappingQuery
import employees.AddEmployeeHistoryCommand
import employees.AddEmployeeHistoryParams
import employees.AddEmployeeManagerMappingCommand
import employees.AddEmployeeManagerMappingParams
import employees.AddEmployeeRoleMappingParams
import employees.AddEmployeeRoleMappingQuery
import employees.AddEmployeesDepartmentMappingParams
import employees.AddEmployeesDepartmentMappingQuery
import employees.AddEmployeesTeamMappingParams
import employees.AddEmployeesTeamMappingQuery
import employees.DeleteEmployeeManagerMappingCommand
import employees.DeleteEmployeeManagerMappingParams
import employees.GetActiveEmployeesCountDuringReviewCycleParams
import employees.GetActiveEmployeesCountDuringReviewCycleQuery
import employees.GetActiveEmployeesDuringReviewCycleParams
import employees.GetActiveEmployeesDuringReviewCycleQuery
import employees.GetActiveEmployeesParams
import employees.GetActiveEmployeesQuery
import employees.GetAllEmployeesCountParams
import employees.GetAllEmployeesCountQuery
import employees.GetAllEmployeesParams
import employees.GetAllEmployeesQuery
import employees.GetAllEmployeesResult
import employees.GetAllManagersCountParams
import employees.GetAllManagersCountQuery
import employees.GetAllManagersParams
import employees.GetAllManagersQuery
import employees.GetAllManagersResult
import employees.GetCurrentManagerDetailsParams
import employees.GetCurrentManagerDetailsQuery
import employees.GetEmployeeByIdParams
import employees.GetEmployeeByIdQuery
import employees.GetEmployeeDataByUniqueIdParams
import employees.GetEmployeeDataByUniqueIdQuery
import employees.GetEmployeeDataByUniqueIdResult
import employees.GetEmployeeDataParams
import employees.GetEmployeeDataQuery
import employees.GetEmployeeHistoryParams
import employees.GetEmployeeHistoryQuery
import employees.GetEmployeeHistoryResult
import employees.GetEmployeeManagerCountParams
import employees.GetEmployeeManagerCountQuery
import employees.GetEmployeeManagerParams
import employees.GetEmployeeManagerQuery
import employees.GetEmployeeManagerResult
import employees.GetEmployeeTeamDuringReviewCycleParams
import employees.GetEmployeeTeamDuringReviewCycleQuery
import employees.GetEmployeesUniqueIdParams
import employees.GetEmployeesUniqueIdQuery
import employees.GetEmployeesWithReviewCyclePermissionParams
import employees.GetEmployeesWithReviewCyclePermissionQuery
import employees.GetManagerMappingParams
import employees.GetManagerMappingQuery
import employees.GetManagersParams
import employees.GetManagersQuery
import employees.GetReporteesOfManagerCountParams
import employees.GetReporteesOfManagerCountQuery
import employees.GetReporteesOfManagerParams
import employees.GetReporteesOfManagerQuery
import employees.GetSecretParams
import employees.GetSecretQuery
import employees.GetUniqueIdByEmailIdParams
import employees.GetUniqueIdByEmailIdQuery
import employees.IsContactNumberExistsParams
import employees.IsContactNumberExistsQuery
import employees.IsEmailIdExistsParams
import employees.IsEmailIdExistsQuery
import employees.IsEmployeeExistsParams
import employees.IsEmployeeExistsQuery
import employees.IsEmployeeIdExistsParams
import employees.IsEmployeeIdExistsQuery
import employees.SaveEmployeeDataParams
import employees.SaveEmployeeDataQuery
import employees.UpdateEmployeeDataParams
import employees.UpdateEmployeeDataQuery
import employees.UpdateEmployeeDesignationMappingCommand
import employees.UpdateEmployeeDesignationMappingParams
import employees.UpdateEmployeeExperienceCommand
import employees.UpdateEmployeeExperienceParams
import employees.UpdateEmployeeHistoryCommand
import employees.UpdateEmployeeHistoryParams
import employees.UpdateEmployeeManagerMappingCommand
import employees.UpdateEmployeeManagerMappingParams
import employees.UpdateEmployeeRoleMappingCommand
import employees.UpdateEmployeeRoleMappingParams
import employees.UpdateEmployeeTeamMappingCommand
import employees.UpdateEmployeeTeamMappingParams
import employees.UpdateEmployeesCommand
import employees.UpdateEmployeesDepartmentMappingCommand
import employees.UpdateEmployeesDepartmentMappingParams
import employees.UpdateEmployeesParams
import employees.UpdateManagerOfAllReporteesCommand
import employees.UpdateManagerOfAllReporteesParams
import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import scalereal.core.employees.EmployeeRepository
import scalereal.core.models.domain.EmpData
import scalereal.core.models.domain.Employee
import scalereal.core.models.domain.EmployeeData
import scalereal.core.models.domain.EmployeeDetails
import scalereal.core.models.domain.EmployeeHistory
import scalereal.core.models.domain.EmployeeManagerData
import scalereal.core.models.domain.EmployeeStatus
import scalereal.core.models.domain.Employees
import scalereal.core.models.domain.EmployeesData
import scalereal.core.models.domain.ManagerData
import scalereal.core.models.domain.ManagerDetails
import scalereal.core.models.domain.ManagerMapping
import scalereal.core.models.domain.ManagerUpdateDataList
import scalereal.core.models.domain.ReporteesData
import scalereal.db.roles.RoleRepositoryImpl
import scalereal.db.util.getWildCardedString
import java.sql.Timestamp
import javax.sql.DataSource

@Singleton
class EmployeeRepositoryImpl(
    @Inject private val dataSource: DataSource,
    @Inject private val roleRepositoryImpl: RoleRepositoryImpl,
) : EmployeeRepository {
    override fun countAllEmployees(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
        teamId: List<Int>,
        designationId: List<Int>,
        roleId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetAllEmployeesCountQuery()
                .query(
                    connection,
                    GetAllEmployeesCountParams(
                        organisationId = organisationId,
                        search = getWildCardedString(searchText),
                        departmentId = departmentId.toTypedArray(),
                        teamId = teamId.toTypedArray(),
                        designationId = designationId.toTypedArray(),
                        roleId = roleId.toTypedArray(),
                    ),
                )[0]
                .employeeCount
                ?.toInt() ?: 0
        }

    override fun fetchAllEmployees(
        organisationId: Long,
        sortOrder: String,
        searchText: String,
        departmentId: List<Int>,
        teamId: List<Int>,
        designationId: List<Int>,
        roleId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<EmployeeData> =
        dataSource.connection.use { connection ->
            GetAllEmployeesQuery()
                .query(
                    connection,
                    GetAllEmployeesParams(
                        organisationId = organisationId,
                        sortOrder = sortOrder,
                        search = getWildCardedString(searchText),
                        departmentId = departmentId.toTypedArray(),
                        teamId = teamId.toTypedArray(),
                        designationId = designationId.toTypedArray(),
                        roleId = roleId.toTypedArray(),
                        offset = offset,
                        limit = limit,
                    ),
                ).map { it.toEmployees() }
        }

    override fun isEmailIdExists(emailId: String): Boolean =
        dataSource.connection.use { connection ->
            IsEmailIdExistsQuery()
                .query(connection, IsEmailIdExistsParams(emailId))
                .map { it.exists ?: false }
                .first()
        }

    override fun isContactNumberExists(
        organisationId: Long,
        contactNo: String,
    ): Boolean =
        dataSource.connection.use { connection ->
            IsContactNumberExistsQuery()
                .query(connection, IsContactNumberExistsParams(organisationId = organisationId, contactNo = contactNo))
                .map { it.exists }
                .firstOrNull() ?: false
        }

    override fun isEmployeeIdExists(
        organisationId: Long,
        employeeId: String,
    ): EmployeeStatus =
        dataSource.connection.use { connection ->
            IsEmployeeIdExistsQuery()
                .query(connection, IsEmployeeIdExistsParams(employeeId, organisationId))
                .map {
                    EmployeeStatus(
                        exists = requireNotNull(it.exists ?: false),
                        status = requireNotNull(it.status ?: false),
                    )
                }.first()
        }

    override fun create(employeesData: EmployeesData): Unit =
        dataSource.connection.use { connection ->
            SaveEmployeeDataQuery()
                .query(
                    connection,
                    SaveEmployeeDataParams(
                        organisationId = employeesData.organisationId,
                        empId = employeesData.employeeId,
                        firstName = employeesData.firstName,
                        lastName = employeesData.lastName,
                        emailId = employeesData.emailId,
                        contactNo = employeesData.contactNo,
                        genderId = employeesData.genderId,
                        dateOfJoining = employeesData.dateOfJoining,
                        dateOfBirth = employeesData.dateOfBirth,
                        experience = employeesData.experienceInMonths,
                        status = employeesData.status,
                        isConsultant = employeesData.isConsultant,
                    ),
                )
        }

    override fun createEmployeesDepartments(
        id: Long,
        departmentId: Long,
    ): Unit =
        dataSource.connection.use { connection ->
            AddEmployeesDepartmentMappingQuery()
                .query(connection, AddEmployeesDepartmentMappingParams(id, departmentId))
        }

    override fun createEmployeesTeams(
        id: Long,
        teamId: Long,
        joinedAt: Timestamp,
    ) {
        dataSource.connection.use { connection ->
            AddEmployeesTeamMappingQuery()
                .query(connection, AddEmployeesTeamMappingParams(id = id, team_id = teamId, joinedAt = joinedAt))
        }
    }

    override fun createEmployeesRoles(
        id: Long,
        roleId: Long,
    ) {
        dataSource.connection.use { connection ->
            AddEmployeeRoleMappingQuery()
                .query(connection, AddEmployeeRoleMappingParams(id, roleId))
        }
    }

    override fun createEmployeesDesignations(
        id: Long,
        designationId: Long,
    ) {
        dataSource.connection.use { connection ->
            AddEmployeeDesignationMappingQuery()
                .query(connection, AddEmployeeDesignationMappingParams(id, designationId))
        }
    }

    override fun insertEmployeeManagerMapping(
        id: Long,
        managerId: Long,
        managerType: Int,
    ) {
        dataSource.connection.use { connection ->
            AddEmployeeManagerMappingCommand()
                .command(
                    connection,
                    AddEmployeeManagerMappingParams(id, managerId, managerType),
                )
        }
    }

    override fun update(employees: Employees): Unit =
        dataSource.connection.use { connection ->
            UpdateEmployeeDataQuery()
                .query(
                    connection,
                    UpdateEmployeeDataParams(
                        organisationId = employees.organisationId,
                        employeeId = employees.employeeId,
                        firstName = employees.firstName,
                        lastName = employees.lastName,
                        emailId = employees.emailId,
                        contactNo = employees.contactNo,
                        status = employees.status,
                        genderId = employees.genderId,
                        dateOfJoining = employees.dateOfJoining,
                        dateOfBirth = employees.dateOfBirth,
                        experience = employees.experienceInMonths,
                        isConsultant = employees.isConsultant,
                        id = employees.id,
                    ),
                )
        }

    override fun updateEmployeesDepartments(
        id: Long,
        departmentId: Long,
    ): Unit =
        dataSource.connection.use { connection ->
            UpdateEmployeesDepartmentMappingCommand()
                .command(connection, UpdateEmployeesDepartmentMappingParams(id, departmentId))
        }

    override fun updateEmployeesTeams(
        id: Long,
        leftAt: Timestamp,
    ) {
        dataSource.connection.use { connection ->
            UpdateEmployeeTeamMappingCommand()
                .command(connection, UpdateEmployeeTeamMappingParams(id = id, leftAt = leftAt))
        }
    }

    override fun updateEmployeesDesignations(
        id: Long,
        designationId: Long,
    ) {
        dataSource.connection.use { connection ->
            UpdateEmployeeDesignationMappingCommand()
                .command(connection, UpdateEmployeeDesignationMappingParams(id, designationId))
        }
    }

    override fun updateEmployeesRoles(
        id: Long,
        roleId: Long,
    ) {
        dataSource.connection.use { connection ->
            UpdateEmployeeRoleMappingCommand()
                .command(connection, UpdateEmployeeRoleMappingParams(id, roleId))
        }
    }

    override fun updateEmployeeManagerMapping(
        id: Long,
        managerType: Int,
    ) {
        dataSource.connection.use { connection ->
            UpdateEmployeeManagerMappingCommand()
                .command(
                    connection,
                    UpdateEmployeeManagerMappingParams(id, managerType),
                )
        }
    }

    override fun deleteEmployeeManagerMapping(id: Long): Unit =
        dataSource.connection.use { connection ->
            DeleteEmployeeManagerMappingCommand()
                .command(connection, DeleteEmployeeManagerMappingParams(id))
        }

    override fun updateOnBoardingFlowStatus(
        organisationId: Long,
        onboardingFlow: Boolean,
        id: Long,
    ): Unit =
        dataSource.connection.use { connection ->
            UpdateEmployeesCommand()
                .command(
                    connection,
                    UpdateEmployeesParams(
                        organisationId = organisationId,
                        onboarding_flow = onboardingFlow,
                        id = id,
                    ),
                )
        }

    override fun getEmployeesUniqueId(
        organisationId: Long,
        employeeId: String?,
    ): Long =
        dataSource.connection.use { connection ->
            GetEmployeesUniqueIdQuery()
                .query(connection, GetEmployeesUniqueIdParams(organisationId = organisationId, employeeId = employeeId))
                .map { it.id }
                .first()
        }

    override fun getUniqueIdByEmailId(emailId: String): Long =
        dataSource.connection.use { connection ->
            GetUniqueIdByEmailIdQuery()
                .query(connection, GetUniqueIdByEmailIdParams(emailId))
                .map { it.id }
                .first()
        }

    override fun getEmployeeDataByUniqueId(id: Long): EmployeeDetails =
        dataSource.connection.use { connection ->
            GetEmployeeDataByUniqueIdQuery()
                .query(connection, GetEmployeeDataByUniqueIdParams(id))
                .map { it.toEmployeeData() }
        }[0]

    override fun fetchAllManagers(
        organisationId: Long,
        moduleId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<ManagerData> =
        dataSource.connection.use { connection ->
            GetAllManagersQuery()
                .query(
                    connection,
                    GetAllManagersParams(
                        organisationId = organisationId,
                        moduleId = moduleId.toTypedArray(),
                        offset = offset,
                        limit = limit,
                    ),
                ).map { it.toManagers() }
        }

    override fun countAllManagers(
        organisationId: Long,
        moduleId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetAllManagersCountQuery()
                .query(
                    connection,
                    GetAllManagersCountParams(organisationId, moduleId.toTypedArray()),
                )[0]
                .employeeCount
                ?.toInt() ?: 0
        }

    override fun fetchByEmailId(emailId: String): Employee? =
        dataSource.connection.use { connection ->
            GetEmployeeDataQuery()
                .query(
                    connection,
                    GetEmployeeDataParams(emailId = emailId),
                ).map {
                    Employee(
                        organisationId = it.organisationId,
                        id = it.id,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        emailId = it.emailId,
                        employeeId = it.empId,
                        contactNo = it.contactNo,
                        departmentName = it.departmentName,
                        teamName = it.teamName,
                        designationName = it.designationName,
                        roleName = it.roleName,
                        modulePermission = roleRepositoryImpl.fetchPermissions(it.roleId),
                        onboardingFlow = it.onboardingFlow,
                        firstManagerId = it.firstManagerId,
                        secondManagerId = it.secondManagerId,
                        isOrWasManager = it.isOrWasManager,
                    )
                }.firstOrNull()
        }

    override fun isEmployeeExists(emailId: Any): Boolean =
        dataSource.connection.use { connection ->
            IsEmployeeExistsQuery()
                .query(connection, IsEmployeeExistsParams(emailId.toString()))
                .map { it.exists }
                .first()
        }

    override fun getEmployeeManagerList(
        organisationId: Long,
        id: List<Int>,
        firstManagerId: List<Int>,
        secondManagerId: List<Int>,
        limit: Int,
        offset: Int,
    ): List<EmployeeManagerData> =
        dataSource.connection.use { connection ->
            GetEmployeeManagerQuery()
                .query(
                    connection,
                    GetEmployeeManagerParams(
                        organisationId = organisationId,
                        id = id.toTypedArray(),
                        firstManagerId = firstManagerId.toTypedArray(),
                        secondManagerId = secondManagerId.toTypedArray(),
                        limit = limit,
                        offset = offset,
                    ),
                ).map { it.toEmployeeManagerData() }
        }

    override fun getEmployeeManagerCount(
        organisationId: Long,
        id: List<Int>,
        firstManagerId: List<Int>,
        secondManagerId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetEmployeeManagerCountQuery()
                .query(
                    connection,
                    GetEmployeeManagerCountParams(
                        organisationId = organisationId,
                        id = id.toTypedArray(),
                        firstManagerId = firstManagerId.toTypedArray(),
                        secondManagerId = secondManagerId.toTypedArray(),
                    ),
                )[0]
                .employeeManagerCount
                ?.toInt() ?: 0
        }

    override fun fetchEmployeesByManager(
        organisationId: Long,
        managerId: Long,
        offset: Int,
        limit: Int,
    ): List<ReporteesData> =
        dataSource.connection.use { connection ->
            GetReporteesOfManagerQuery()
                .query(connection, GetReporteesOfManagerParams(organisationId, managerId, offset, limit))
                .map {
                    ReporteesData(
                        organisationId = it.organisationId,
                        id = it.id,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        employeeId = it.empId,
                        emailId = it.emailId,
                        firstManagerId = it.firstManagerId,
                        secondManagerId = it.secondManagerId,
                    )
                }
        }

    override fun fetchEmployeesByManagerCount(
        organisationId: Long,
        managerId: Long,
    ): Int =
        dataSource.connection.use { connection ->
            GetReporteesOfManagerCountQuery()
                .query(
                    connection,
                    GetReporteesOfManagerCountParams(organisationId, managerId),
                )[0]
                .reporteesCount
                ?.toInt() ?: 0
        }

    override fun updateEmployeesManager(managerUpdateDataList: List<ManagerUpdateDataList>): Unit =
        dataSource.connection.use { connection ->
            for (managerUpdateData in managerUpdateDataList) {
                UpdateManagerOfAllReporteesCommand()
                    .command(
                        connection,
                        UpdateManagerOfAllReporteesParams(
                            currentManagerId = managerUpdateData.currentManagerId,
                            employeeId = managerUpdateData.employeeId,
                            newManagerId = managerUpdateData.newManagerId,
                        ),
                    )
            }
        }

    private fun GetEmployeeManagerResult.toEmployeeManagerData() =
        EmployeeManagerData(
            organisationId = organisationId,
            id = id,
            employeeId = employeeId,
            firstName = firstName,
            lastName = lastName,
            emailId = emailId,
            firstManagerId = firstManagerId,
            firstManagerEmployeeId = firstManagerEmployeeId,
            firstManagerFirstName = firstManagerFirstName,
            firstManagerLastName = firstManagerLastName,
            secondManagerId = secondManagerId,
            secondManagerEmployeeId = secondManagerEmployeeId,
            secondManagerFirstName = secondManagerFirstName,
            secondManagerLastName = secondManagerLastName,
        )

    override fun getSecret(emailId: Any): String =
        dataSource.connection.use { connection ->
            GetSecretQuery()
                .query(
                    connection,
                    GetSecretParams(
                        emailId = emailId.toString(),
                    ),
                ).map { it.password.toString() }
                .first()
        }

    override fun getActiveEmployees(organisationId: Long): List<Employee> =
        dataSource.connection.use { connection ->
            GetActiveEmployeesQuery()
                .query(connection, GetActiveEmployeesParams(organisationId))
                .map {
                    Employee(
                        organisationId = it.organisationId,
                        id = it.id,
                        employeeId = it.empId,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        emailId = it.emailId,
                        contactNo = it.contactNo,
                        departmentName = null,
                        teamName = null,
                        designationName = null,
                        roleName = null,
                        modulePermission = listOf(),
                        onboardingFlow = null,
                    )
                }
        }

    override fun getEmployeeById(
        id: Long?,
        reviewCycleId: Long?,
    ): EmpData =
        dataSource.connection.use { connection ->
            GetEmployeeByIdQuery()
                .query(connection, GetEmployeeByIdParams(id = id, review_cycle_id = reviewCycleId))
                .map {
                    EmpData(
                        organisationId = it.organisationId,
                        id = it.id,
                        employeeId = it.empId,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        emailId = it.emailId,
                        contactNo = it.contactNo,
                        genderId = it.genderId,
                        dateOfBirth = it.dateOfBirth,
                        dateOfJoining = it.dateOfJoining,
                        experienceInMonths = it.experience,
                        status = it.status,
                        isConsultant = it.isConsultant,
                        departmentId = it.departmentId,
                        departmentName = it.departmentName,
                        teamId = it.teamId,
                        teamName = it.teamName,
                        designationId = it.designationId,
                        designationName = it.designationName,
                        roleId = it.roleId,
                        roleName = it.roleName,
                        firstManagerId = it.firstManagerId,
                        firstManagerEmployeeId = it.firstManagerEmployeeId,
                        firstManagerFirstName = it.firstManagerFirstName,
                        firstManagerLastName = it.firstManagerLastName,
                        secondManagerId = it.secondManagerId,
                        secondManagerEmployeeId = it.secondManagerEmployeeId,
                        secondManagerFirstName = it.secondManagerFirstName,
                        secondManagerLastName = it.secondManagerLastName,
                        employeeNameWithEmployeeId = it.firstName + " " + it.lastName + " (" + it.empId + ")",
                    )
                }.first()
        }

    override fun getManagers(organisationId: Long): List<Employee> =
        dataSource.connection.use { connection ->
            GetManagersQuery()
                .query(connection, GetManagersParams(organisationId))
                .map {
                    Employee(
                        organisationId = it.organisationId,
                        id = it.id,
                        employeeId = it.empId,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        emailId = it.emailId,
                        contactNo = it.contactNo,
                        departmentName = null,
                        teamName = null,
                        designationName = null,
                        roleName = null,
                        modulePermission = listOf(),
                        onboardingFlow = null,
                    )
                }
        }

    override fun getMapping(id: Long): ManagerMapping =
        dataSource.connection.use { connection ->
            GetManagerMappingQuery()
                .query(connection, GetManagerMappingParams(id))
                .map {
                    ManagerMapping(
                        firstManagerId = it.firstManagerId,
                        secondManagerId = it.secondManagerId,
                    )
                }.first()
        }

    override fun getEmployeesWithModulePermission(
        organisationId: Long,
        reviewCycleModuleId: Int,
    ): List<Employee> =
        dataSource.connection.use { connection ->
            GetEmployeesWithReviewCyclePermissionQuery()
                .query(
                    connection,
                    GetEmployeesWithReviewCyclePermissionParams(
                        module_id = reviewCycleModuleId,
                        organisation_id = organisationId,
                    ),
                ).map {
                    Employee(
                        organisationId = it.organisationId,
                        id = it.id,
                        employeeId = it.empId,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        emailId = it.emailId,
                        contactNo = it.contactNo,
                        departmentName = null,
                        teamName = null,
                        designationName = null,
                        roleName = null,
                        modulePermission = listOf(),
                        onboardingFlow = null,
                    )
                }
        }

    override fun getCurrentManagerDetails(
        organisationId: Long,
        id: Long,
    ): ManagerDetails =
        dataSource.connection.use { connection ->
            GetCurrentManagerDetailsQuery()
                .query(connection, GetCurrentManagerDetailsParams(organisationId, id))
                .map {
                    ManagerDetails(
                        firstManagerId = it.firstManagerId,
                        firstManagerEmployeeId = it.firstManagerEmployeeId,
                        firstManagerFirstName = it.firstManagerFirstName,
                        firstManagerLastName = it.firstManagerLastName,
                        secondManagerId = it.secondManagerId,
                        secondManagerEmployeeId = it.secondManagerEmployeeId,
                        secondManagerFirstName = it.secondManagerFirstName,
                        secondManagerLastName = it.secondManagerLastName,
                    )
                }.first()
        }

    override fun createEmployeeHistory(
        id: Long,
        activatedAt: Timestamp,
    ): Unit =
        dataSource.connection.use { connection ->
            AddEmployeeHistoryCommand()
                .command(
                    connection,
                    AddEmployeeHistoryParams(employeeId = id, activatedAt = activatedAt),
                )
        }

    override fun updateEmployeeHistory(
        id: Long,
        deactivatedAt: Timestamp,
    ): Unit =
        dataSource.connection.use { connection ->
            UpdateEmployeeHistoryCommand()
                .command(
                    connection,
                    UpdateEmployeeHistoryParams(employeeId = id, deactivatedAt = deactivatedAt),
                )
        }

    override fun getEmployeesHistory(id: List<Int>): List<EmployeeHistory> =
        dataSource.connection.use { connection ->
            GetEmployeeHistoryQuery()
                .query(connection, GetEmployeeHistoryParams(employeeId = id.toTypedArray()))
                .map { it.toEmployeeHistory() }
        }

    override fun fetchActiveEmployeesDuringReviewCycle(
        organisationId: Long,
        reviewCycleId: Long,
    ): List<Int> =
        dataSource.connection.use { connection ->
            GetActiveEmployeesDuringReviewCycleQuery()
                .query(
                    connection,
                    GetActiveEmployeesDuringReviewCycleParams(organisationId, reviewCycleId),
                ).map { it.activeEmployees.toInt() }
        }

    override fun fetchActiveEmployeesCountDuringReviewCycle(
        organisationId: Long,
        reviewCycleId: Long,
    ): Long =
        dataSource.connection.use { connection ->
            val result =
                GetActiveEmployeesCountDuringReviewCycleQuery()
                    .query(
                        connection,
                        GetActiveEmployeesCountDuringReviewCycleParams(organisationId, reviewCycleId),
                    ).firstOrNull()

            result?.let { return it.activeEmployees ?: 0L } ?: 0L
        }

    override fun increaseEmployeesExperience() {
        dataSource.connection.use { connection ->
            UpdateEmployeeExperienceCommand()
                .command(connection, UpdateEmployeeExperienceParams())
        }
    }

    override fun fetchEmployeeTeamDuringReviewCycle(
        organisationId: Long,
        reviewCycleId: Long,
        employeeId: Long,
    ): Map<Long, String> {
        dataSource.connection.use { connection ->
            return GetEmployeeTeamDuringReviewCycleQuery()
                .query(
                    connection,
                    GetEmployeeTeamDuringReviewCycleParams(
                        organisationId = organisationId,
                        reviewCycleId = reviewCycleId,
                        employeeId = employeeId,
                    ),
                ).associate { it.teamId to it.teamName }
        }
    }

    private fun GetAllEmployeesResult.toEmployees() =
        EmployeeData(
            organisationId = organisationId,
            id = id,
            employeeId = empId,
            firstName = firstName,
            lastName = lastName,
            emailId = emailId,
            contactNo = contactNo,
            genderId = genderId,
            dateOfBirth = dateOfBirth,
            dateOfJoining = dateOfJoining,
            experienceInMonths = experience,
            status = status,
            isConsultant = isConsultant,
            departmentName = departmentName,
            teamName = teamName,
            designationName = designationName,
            roleName = roleName,
            firstManagerId = firstManagerId,
            firstManagerEmployeeId = firstManagerEmployeeId,
            firstManagerFirstName = firstManagerFirstName,
            firstManagerLastName = firstManagerLastName,
            secondManagerId = secondManagerId,
            secondManagerEmployeeId = secondManagerEmployeeId,
            secondManagerFirstName = secondManagerFirstName,
            secondManagerLastName = secondManagerLastName,
        )

    private fun GetEmployeeHistoryResult.toEmployeeHistory() =
        EmployeeHistory(
            historyId = id,
            employeeId = employeeId,
            activatedAt = activatedAt,
            deactivatedAt = deactivatedAt,
        )

    private fun GetEmployeeDataByUniqueIdResult.toEmployeeData() =
        EmployeeDetails(
            organisationId = organisationId,
            id = id,
            employeeId = empId,
            firstName = firstName,
            lastName = lastName,
            emailId = emailId,
            contactNo = contactNo,
            genderId = genderId,
            dateOfBirth = dateOfBirth,
            dateOfJoining = dateOfJoining,
            experienceInMonths = experience,
            status = status,
            isConsultant = isConsultant,
            departmentId = departmentId,
            teamId = teamId,
            designationId = designationId,
            roleId = roleId,
            firstManagerId = firstManagerId,
            secondManagerId = secondManagerId,
        )

    private fun GetAllManagersResult.toManagers() =
        ManagerData(
            organisationId = organisationId,
            id = id,
            employeeId = empId,
            firstName = firstName,
            lastName = lastName,
            emailId = emailId,
            contactNo = contactNo,
            status = status,
            departmentName = departmentName,
            teamName = teamName,
            designationName = designationName,
            roleName = roleName,
            firstManagerId = firstManagerId,
            firstManagerEmployeeId = firstManagerEmployeeId,
            secondManagerId = secondManagerId,
            secondManagerEmployeeId = secondManagerEmployeeId,
        )
}
