package scalereal.core.employees

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
import java.sql.Timestamp

interface EmployeeRepository {
    fun countAllEmployees(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
        teamId: List<Int>,
        designationId: List<Int>,
        roleId: List<Int>,
    ): Int

    fun fetchAllEmployees(
        organisationId: Long,
        sortOrder: String,
        searchText: String,
        departmentId: List<Int>,
        teamId: List<Int>,
        designationId: List<Int>,
        roleId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<EmployeeData>

    fun isEmailIdExists(emailId: String): Boolean

    fun isContactNumberExists(
        organisationId: Long,
        contactNo: String,
    ): Boolean

    fun isEmployeeIdExists(
        organisationId: Long,
        employeeId: String,
    ): EmployeeStatus

    fun create(employeesData: EmployeesData)

    fun createEmployeesDepartments(
        id: Long,
        departmentId: Long,
    )

    fun createEmployeesTeams(
        id: Long,
        teamId: Long,
        joinedAt: Timestamp,
    )

    fun createEmployeesRoles(
        id: Long,
        roleId: Long,
    )

    fun createEmployeesDesignations(
        id: Long,
        designationId: Long,
    )

    fun insertEmployeeManagerMapping(
        id: Long,
        managerId: Long,
        managerType: Int,
    )

    fun update(employees: Employees)

    fun updateEmployeesDepartments(
        id: Long,
        departmentId: Long,
    )

    fun updateEmployeesTeams(
        id: Long,
        leftAt: Timestamp,
    )

    fun updateEmployeesRoles(
        id: Long,
        roleId: Long,
    )

    fun updateEmployeesDesignations(
        id: Long,
        designationId: Long,
    )

    fun updateEmployeeManagerMapping(
        id: Long,
        managerType: Int,
    )

    fun deleteEmployeeManagerMapping(id: Long)

    fun updateOnBoardingFlowStatus(
        organisationId: Long,
        onboardingFlow: Boolean,
        id: Long,
    )

    fun getEmployeesUniqueId(
        organisationId: Long,
        employeeId: String?,
    ): Long

    fun getUniqueIdByEmailId(emailId: String): Long

    fun getEmployeeDataByUniqueId(id: Long): EmployeeDetails

    fun countAllManagers(
        organisationId: Long,
        moduleId: List<Int>,
    ): Int

    fun fetchAllManagers(
        organisationId: Long,
        moduleId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<ManagerData>

    fun fetchByEmailId(emailId: String): Employee?

    fun isEmployeeExists(emailId: Any): Boolean

    fun getEmployeeManagerList(
        organisationId: Long,
        id: List<Int>,
        firstManagerId: List<Int>,
        secondManagerId: List<Int>,
        limit: Int,
        offset: Int,
    ): List<EmployeeManagerData>

    fun getEmployeeManagerCount(
        organisationId: Long,
        id: List<Int>,
        firstManagerId: List<Int>,
        secondManagerId: List<Int>,
    ): Int

    fun fetchEmployeesByManager(
        organisationId: Long,
        managerId: Long,
        offset: Int,
        limit: Int,
    ): List<ReporteesData>

    fun fetchEmployeesByManagerCount(
        organisationId: Long,
        managerId: Long,
    ): Int

    fun updateEmployeesManager(managerUpdateDataList: List<ManagerUpdateDataList>)

    fun getSecret(emailId: Any): String

    fun getActiveEmployees(organisationId: Long): List<Employee>

    fun getEmployeeById(
        id: Long?,
        reviewCycleId: Long? = null,
    ): EmpData

    fun getManagers(organisationId: Long): List<Employee>

    fun getMapping(id: Long): ManagerMapping

    fun getEmployeesWithModulePermission(
        organisationId: Long,
        reviewCycleModuleId: Int,
    ): List<Employee>

    fun getCurrentManagerDetails(
        organisationId: Long,
        id: Long,
    ): ManagerDetails

    fun createEmployeeHistory(
        id: Long,
        activatedAt: Timestamp,
    )

    fun updateEmployeeHistory(
        id: Long,
        deactivatedAt: Timestamp,
    )

    fun getEmployeesHistory(id: List<Int>): List<EmployeeHistory>

    fun fetchActiveEmployeesDuringReviewCycle(
        organisationId: Long,
        reviewCycleId: Long,
    ): List<Int>

    fun fetchActiveEmployeesCountDuringReviewCycle(
        organisationId: Long,
        reviewCycleId: Long,
    ): Long

    fun increaseEmployeesExperience()

    fun fetchEmployeeTeamDuringReviewCycle(
        organisationId: Long,
        reviewCycleId: Long,
        employeeId: Long,
    ): Map<Long, String>
}
