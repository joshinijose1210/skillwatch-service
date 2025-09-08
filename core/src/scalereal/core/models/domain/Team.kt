package scalereal.core.models.domain

import com.fasterxml.jackson.annotation.JsonInclude
import java.sql.Timestamp

data class Team(
    val organisationId: Long,
    val departmentId: Long?,
    val departmentDisplayId: String?,
    val departmentName: String?,
    val departmentStatus: Boolean?,
    val id: Long,
    val teamId: String,
    val teamName: String,
    val teamStatus: Boolean,
    var teamCreatedAt: Timestamp,
    val teamUpdatedAt: Timestamp?,
) {
    fun fetchDepartmentDisplayId() = if (departmentDisplayId != "null") "DEP$departmentDisplayId" else ""
}

data class TeamResponse(
    val unlinkedTeamsCount: Int,
    val totalTeams: Int,
    val teams: List<Team>,
)

data class TeamStatus(
    val exists: Boolean,
    val status: Boolean,
)

data class TeamData(
    val organisationId: Long,
    val departmentId: Long,
    val teamName: String,
    val teamStatus: Boolean,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class TeamResults(
    val existingTeam: Array<String>,
    val addedTeam: Array<String>,
    val invalidLengthTeam: Array<String>,
    val invalidCharTeam: Array<String>,
)
