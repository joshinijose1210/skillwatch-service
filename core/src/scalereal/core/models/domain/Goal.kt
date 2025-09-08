package scalereal.core.models.domain

import java.sql.Date
import java.sql.Timestamp

data class Goal(
    val id: Long,
    val goalId: String?,
    val typeId: Int,
    val description: String?,
    val createdAt: Timestamp,
    val targetDate: Date,
    val progressId: Int,
    var progressName: String? = null,
    val createdBy: Long,
    val assignedTo: Long,
)

data class GoalBasicDetails(
    val id: Long,
    val goalId: String?,
    val assignedTo: Long,
    val createdBy: Long,
)

data class GoalDetails(
    val id: Long,
    val goalId: String?,
    val description: String,
    val typeId: Int,
    val typeName: String?,
    val createdBy: Long,
    val createdByName: String?,
    val assignedTo: Long,
    val assignedToName: String?,
    val progressId: Int,
    val progressName: String?,
    val targetDate: Date,
) {
    fun enrich(
        typeMap: Map<Int, String>,
        progressMap: Map<Int, String>,
    ): GoalDetails =
        this.copy(
            typeName = typeMap[this.typeId],
            progressName = progressMap[this.progressId],
        )
}

data class GoalListResponse(
    val totalGoals: Int,
    val goals: List<GoalDetails>,
)

data class CreateGoalRequest(
    val typeId: Int,
    val description: String?,
    val targetDate: Date,
    val assignedTo: Long,
    val goalId: Int? = null,
) {
    fun withGoalId(lastGoalId: Int): CreateGoalRequest = this.copy(goalId = lastGoalId + 1)
}

data class CreateGoalResponse(
    val id: Long,
)
