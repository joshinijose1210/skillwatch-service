package scalereal.core.models.domain

import java.sql.Date
import java.sql.Timestamp

data class Feedback(
    val id: Long,
    val feedback: String,
    val feedbackToId: Long,
    val feedbackFromId: Long,
    val feedbackTypeId: Int,
    val isDraft: Boolean,
)

data class FeedbackData(
    val srNo: Long,
    val date: Timestamp,
    val organisationId: Long,
    val isExternalFeedback: Boolean,
    val feedbackToId: Long,
    val feedbackToEmployeeId: String?,
    val feedbackFromId: Long?,
    var feedbackFromEmployeeId: String?,
    val empFirstName: String?,
    val empLastName: String?,
    val empRoleName: String?,
    val externalFeedbackFromEmailId: String?,
    val feedback: String,
    val feedbackTypeId: Int,
    val feedbackType: String,
    val isDraft: Boolean,
    val isRead: Boolean = true,
)

data class FeedbackResponse(
    val unReadFeedbackCount: Int? = null,
    val totalFeedbacks: Int,
    val feedbacks: List<FeedbackData>,
)

data class Feedbacks(
    val date: Date?,
    val isExternalFeedback: Boolean,
    val feedback: String,
    val feedbackToId: Long,
    val feedbackToEmployeeId: String?,
    val feedbackFromId: Long?,
    val feedbackFromEmployeeId: String?,
    val feedbackTypeId: Int,
    val feedbackType: String,
    val toEmpName: String?,
    val fromEmpName: String?,
    val toRoleName: String?,
    val fromRoleName: String?,
    val externalFeedbackFromEmailId: String?,
    val organisationId: Long?,
    val isDraft: Boolean,
)

data class FeedbacksResponse(
    val totalFeedbacks: Int,
    val feedbacks: List<Feedbacks>,
)

data class CreateFeedbackParams(
    val feedbackToId: Long,
    val feedbackFromId: Long,
    val requestId: Long?,
    val isDraft: Boolean,
    val feedback: List<AddFeedbackData>,
)

data class UpdateFeedbackParams(
    val feedbackToId: Long,
    val feedbackFromId: Long,
    val feedbackData: List<UpdateFeedbackData>,
    val requestId: Long?,
    val isDraft: Boolean,
)

data class AddFeedbackData(
    val feedbackTypeId: Int,
    val feedbackText: String,
    val markdownText: String? = null,
)

data class UpdateFeedbackData(
    val feedbackId: Long?,
    val feedbackTypeId: Int,
    val feedbackText: String,
    val markdownText: String? = null,
    val isNewlyAdded: Boolean = false,
    val isRemoved: Boolean = false,
) {
    init {
        if (isNewlyAdded && feedbackId != null) {
            throw IllegalArgumentException("Newly added feedback must have a null feedbackId")
        }
        if (!isNewlyAdded && feedbackId == null) {
            throw IllegalArgumentException("Existing feedback must have a non-null feedbackId")
        }
        if (isRemoved && feedbackId == null) {
            throw IllegalArgumentException("Removed feedback must have a non-null feedbackId")
        }
    }
}

data class FeedbackCounts(
    val submittedPositiveCount: Long,
    val submittedImprovementCount: Long,
    val submittedAppreciationCount: Long,
    val receivedPositiveCount: Long,
    val receivedImprovementCount: Long,
    val receivedAppreciationCount: Long,
) {
    operator fun plus(other: FeedbackCounts): FeedbackCounts =
        FeedbackCounts(
            submittedPositiveCount = this.submittedPositiveCount + other.submittedPositiveCount,
            submittedImprovementCount = this.submittedImprovementCount + other.submittedImprovementCount,
            submittedAppreciationCount = this.submittedAppreciationCount + other.submittedAppreciationCount,
            receivedPositiveCount = this.receivedPositiveCount + other.receivedPositiveCount,
            receivedImprovementCount = this.receivedImprovementCount + other.receivedImprovementCount,
            receivedAppreciationCount = this.receivedAppreciationCount + other.receivedAppreciationCount,
        )
}

data class FeedbackPercentages(
    val submittedPositivePercentage: Double,
    val submittedImprovementPercentage: Double,
    val submittedAppreciationPercentage: Double,
    val receivedPositivePercentage: Double,
    val receivedImprovementPercentage: Double,
    val receivedAppreciationPercentage: Double,
)

data class FeedbackGraph(
    val feedbackCounts: FeedbackCounts,
    val feedbackPercentages: FeedbackPercentages,
)
