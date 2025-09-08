package scalereal.core.models.domain

import com.fasterxml.jackson.annotation.JsonInclude
import java.sql.Timestamp
import javax.validation.constraints.NotNull

data class Designation(
    val organisationId: Long?,
    val departmentId: Long?,
    val departmentName: String?,
    val departmentDisplayId: String?,
    val departmentStatus: Boolean?,
    val teamId: Long?,
    val teamName: String?,
    val teamDisplayId: String?,
    val teamStatus: Boolean?,
    val id: Long?,
    val designationId: String,
    val designationName: String,
    val status: Boolean,
    val createdAt: Timestamp,
    val updatedAt: Timestamp?,
) {
    fun getDepartmentsDisplayId() = "DEP${this.departmentDisplayId}"

    fun getTeamsDisplayId() = "T${this.teamDisplayId}"

    fun getDesignationsDisplayId() = "D${this.designationId}"
}

data class DesignationResponse(
    val unlinkedDesignationsCount: Int,
    val totalDesignation: Int,
    val designations: List<Designation>,
)

data class DesignationStatus(
    val organisationId: Long?,
    val exists: Boolean,
    val status: Boolean,
)

data class DesignationData(
    val organisationId: Long,
    @field:NotNull
    val teamId: Long,
    val designationName: String,
    val status: Boolean,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class DesignationResults(
    val existingDesignation: Array<String>,
    val addedDesignation: Array<String>,
    val invalidLengthDesignation: Array<String>,
    val invalidCharDesignation: Array<String>,
)
