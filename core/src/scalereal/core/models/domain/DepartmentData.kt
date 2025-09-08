package scalereal.core.models.domain

import com.fasterxml.jackson.annotation.JsonInclude
import java.sql.Timestamp

data class DepartmentData(
    val organisationId: Long,
    val departmentName: String,
    val departmentStatus: Boolean,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class DepartmentResults(
    val existingDepartment: Array<String>,
    val addedDepartment: Array<String>,
    val invalidLengthDepartment: Array<String>,
    val invalidCharDepartment: Array<String>,
)

data class DepartmentStatus(
    val exists: Boolean,
    val status: Boolean,
)

data class Department(
    val organisationId: Long,
    val id: Long,
    val departmentId: String,
    val departmentName: String,
    val departmentStatus: Boolean,
    var departmentCreatedAt: Timestamp,
    val departmentUpdatedAt: Timestamp?,
)

data class DepartmentResponse(
    val totalDepartments: Int,
    val departments: List<Department>,
)
