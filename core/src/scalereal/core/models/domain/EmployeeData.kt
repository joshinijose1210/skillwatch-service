package scalereal.core.models.domain

import java.io.File
import java.sql.Date

data class EmployeeData(
    val organisationId: Long,
    val id: Long,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val emailId: String,
    val contactNo: String,
    val genderId: Int?,
    val dateOfBirth: Date?,
    val dateOfJoining: Date?,
    val experienceInMonths: Int?,
    val status: Boolean,
    val isConsultant: Boolean,
    val departmentName: String?,
    val teamName: String?,
    val designationName: String?,
    val roleName: String?,
    val firstManagerId: Long?,
    val firstManagerEmployeeId: String?,
    val firstManagerFirstName: String?,
    val firstManagerLastName: String?,
    val secondManagerId: Long?,
    val secondManagerEmployeeId: String?,
    val secondManagerFirstName: String?,
    val secondManagerLastName: String?,
) {
    fun getEmployeeNameWithEmployeeId() = " ${this.firstName} ${this.lastName} ( ${this.employeeId} )"
}

data class EmployeeResponse(
    val totalEmployees: Int,
    val employees: List<EmployeeData>,
)

data class EmployeesData(
    val organisationId: Long,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val emailId: String,
    val contactNo: String,
    val genderId: Int,
    val dateOfBirth: Date,
    val dateOfJoining: Date,
    val experienceInMonths: Int,
    val status: Boolean,
    val departmentId: Long,
    val teamId: Long,
    val designationId: Long,
    val roleId: Long,
    var firstManagerId: Long?,
    val secondManagerId: Long?,
    val isConsultant: Boolean,
) {
    fun getEmployeeNameWithEmployeeId() = "${this.firstName} ${this.lastName} (${this.employeeId})"
}

data class ManagerData(
    val organisationId: Long,
    val id: Long,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val emailId: String,
    val contactNo: String,
    val status: Boolean,
    val departmentName: String?,
    val teamName: String?,
    val designationName: String?,
    val roleName: String?,
    val firstManagerId: Long?,
    val firstManagerEmployeeId: String?,
    val secondManagerId: Long?,
    val secondManagerEmployeeId: String?,
)

data class ManagerResponse(
    val totalManagers: Int,
    val managers: List<ManagerData>,
)

data class Employees(
    val organisationId: Long,
    val id: Long,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val emailId: String,
    val contactNo: String,
    val genderId: Int?,
    val dateOfBirth: Date?,
    val dateOfJoining: Date?,
    val experienceInMonths: Int?,
    val status: Boolean,
    val departmentId: Long,
    val teamId: Long,
    val designationId: Long,
    val roleId: Long,
    val firstManagerId: Long?,
    val secondManagerId: Long?,
    val isConsultant: Boolean,
) {
    fun getEmployeeNameWithEmployeeId() = "${this.firstName} ${this.lastName} (${this.employeeId})"
}

data class EmployeeDetails(
    val organisationId: Long,
    val id: Long,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val emailId: String,
    val contactNo: String,
    val genderId: Int?,
    val dateOfBirth: Date?,
    val dateOfJoining: Date?,
    val experienceInMonths: Int?,
    val status: Boolean,
    val departmentId: Long?,
    val teamId: Long?,
    val designationId: Long,
    val roleId: Long,
    val firstManagerId: Long?,
    val secondManagerId: Long?,
    val isConsultant: Boolean,
) {
    fun getEmployeeNameWithEmployeeId() = "${this.firstName} ${this.lastName}(${this.employeeId})"
}

data class EmployeeManagerData(
    val organisationId: Long,
    val id: Long,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val emailId: String,
    val firstManagerId: Long?,
    val firstManagerEmployeeId: String?,
    val firstManagerFirstName: String?,
    val firstManagerLastName: String?,
    val secondManagerId: Long?,
    val secondManagerEmployeeId: String?,
    val secondManagerFirstName: String?,
    val secondManagerLastName: String?,
) {
    fun getEmployeeNameWithEmployeeId() = " ${this.firstName} ${this.lastName} ( ${this.employeeId} )"
}

data class EmployeeManager(
    val employeesCount: Int,
    val employeeManagerData: List<EmployeeManagerData>,
)

data class BulkImportResponse(
    val file: File,
    val fileCount: Int,
    val errorCount: Int,
)

data class ReporteesData(
    val organisationId: Long,
    val id: Long,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val emailId: String,
    val firstManagerId: Long?,
    val secondManagerId: Long?,
)

data class ManagerReportees(
    val reporteesCount: Int,
    val managerId: Long,
    val reporteesData: List<ReporteesData>,
)
