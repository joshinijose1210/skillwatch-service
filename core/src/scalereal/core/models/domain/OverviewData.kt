package scalereal.core.models.domain

import com.fasterxml.jackson.annotation.JsonInclude
import java.sql.Date
import java.sql.Timestamp

data class OverviewData(
    val reviewCycleId: Long?,
    val firstName: String,
    val startDate: Date?,
    val endDate: Date?,
    val selfReviewStartDate: Date,
    val selfReviewEndDate: Date,
    val selfReviewDraft: Boolean?,
    val selfReviewPublish: Boolean?,
    val positive: Long?,
    val improvement: Long?,
    val appreciation: Long?,
)

data class AppreciationData(
    val isExternalFeedback: Boolean,
    val appreciationToId: Long,
    val appreciationToEmployeeId: String,
    val appreciationToFirstName: String,
    val appreciationToLastName: String,
    val appreciationToRoleName: String?,
    val appreciation: String,
    val appreciationFromId: Long?,
    val appreciationFromEmployeeId: String?,
    val appreciationFromFirstName: String?,
    val appreciationFromLastName: String?,
    val appreciationFromRoleName: String?,
    val externalFeedbackFromEmailId: String?,
    val submitDate: Timestamp,
    val isDraft: Boolean,
)

data class FeedbacksData(
    val isExternalFeedback: Boolean,
    val feedbackType: String,
    val feedback: String,
    val feedbackFromId: Long?,
    val feedbackFromEmployeeId: String?,
    val feedbackFromFirstName: String?,
    val feedbackFromLastName: String?,
    val feedbackFromRoleName: String?,
    val externalFeedbackFromEmailId: String?,
    val submitDate: Timestamp,
    val isDraft: Boolean,
)

data class EmployeeFeedbackResponse(
    val positiveFeedbackCount: Int,
    val improvementFeedbackCount: Int,
    val appreciationFeedbackCount: Int,
    val totalFeedbacks: Int,
    val feedbacks: List<Any>,
)

data class GoalsResponse(
    val totalGoals: Int,
    val goals: List<Goal>,
)

data class ReviewCycles(
    val reviewCycleId: Int,
    val startDate: Date,
    val endDate: Date,
)

@JsonInclude(JsonInclude.Include.ALWAYS)
data class GoalGroup(
    val startDate: Date,
    val endDate: Date,
    val goals: List<Goal>,
)
