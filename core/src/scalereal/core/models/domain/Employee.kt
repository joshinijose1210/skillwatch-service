package scalereal.core.models.domain

import java.sql.Date

data class Employee(
    val organisationId: Long,
    val id: Long,
    val employeeId: String?,
    val firstName: String,
    val lastName: String,
    val emailId: String,
    val contactNo: String?,
    val departmentName: String?,
    val teamName: String?,
    val designationName: String?,
    val roleName: String?,
    val modulePermission: List<ModulePermission>,
    val onboardingFlow: Boolean?,
    val firstManagerId: Long? = null,
    val secondManagerId: Long? = null,
    val isOrWasManager: Boolean? = false,
) {
    fun getEmployeeNameWithEmployeeId() = " ${this.firstName} ${this.lastName} ( ${this.employeeId} )"
}

data class EmployeeReviewData(
    val id: Long,
    val employeeId: String?,
    val firstName: String,
    val lastName: String,
    val emailId: String,
    val contactNo: Long?,
    val teamName: String?,
    val designationName: String?,
    val roleName: String?,
    val modulePermission: List<ModulePermission>,
    val onboardingFlow: Boolean?,
)

data class EmployeeStatus(
    val exists: Boolean,
    val status: Boolean,
)

data class ValidContactNumber(
    val isValid: Boolean,
    val formattedContact: String?,
)

data class ManagerUpdateDataList(
    val currentManagerId: Long,
    val employeeId: Long,
    val newManagerId: Long,
)

data class EmpData(
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
    val departmentId: Long?,
    val departmentName: String?,
    val teamId: Long?,
    val teamName: String?,
    val designationId: Long?,
    val designationName: String?,
    val roleId: Long?,
    val roleName: String?,
    val firstManagerId: Long?,
    val firstManagerEmployeeId: String?,
    val firstManagerFirstName: String?,
    val firstManagerLastName: String?,
    val secondManagerId: Long?,
    val secondManagerEmployeeId: String?,
    val secondManagerFirstName: String?,
    val secondManagerLastName: String?,
    val employeeNameWithEmployeeId: String?,
)

data class ManagerMapping(
    val firstManagerId: Long?,
    val secondManagerId: Long?,
)

data class ManagerDetails(
    val firstManagerId: Long,
    val firstManagerEmployeeId: String,
    val firstManagerFirstName: String,
    val firstManagerLastName: String,
    val secondManagerId: Long?,
    val secondManagerEmployeeId: String?,
    val secondManagerFirstName: String?,
    val secondManagerLastName: String?,
)
