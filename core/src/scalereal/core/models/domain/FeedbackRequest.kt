package scalereal.core.models.domain

import java.sql.Date
import java.sql.Timestamp

data class FeedbackRequestData(
    val organisationId: Long,
    val requestId: Long,
    val isSubmitted: Boolean?,
    val isExternalRequest: Boolean,
    val requestedOn: Timestamp?,
    val requestedById: Long,
    val requestedByEmployeeId: String,
    val requestedByFirstName: String,
    val requestedByLastName: String,
    val feedbackToId: Long,
    val feedbackToEmployeeId: String,
    val feedbackToFirstName: String,
    val feedbackToLastName: String,
    val feedbackFromId: Long?,
    val feedbackFromEmployeeId: String?,
    val feedbackFromFirstName: String?,
    val feedbackFromLastName: String?,
    val externalFeedbackFromEmail: String?,
    val isDraft: Boolean,
)

data class FeedbackRequestResponse(
    val pendingFeedbackRequestCount: Int,
    val totalFeedbackRequestDataCount: Int,
    val feedbackRequestData: List<FeedbackRequestData>,
)

data class FeedbackRequestParams(
    val organisationId: Long,
    val requestedById: List<Int>,
    val feedbackToId: List<Int>,
    val feedbackFromId: List<Int>,
    val isSubmitted: List<String>,
    val reviewCycleId: List<Int>,
    val sortBy: String?,
)

data class FeedbackRequest(
    val id: Long,
    val requestedById: Long,
    val requestedByEmployeeId: String,
    val requestedByFirstName: String,
    val requestedByLastName: String,
    val feedbackToId: Long,
    val feedbackToEmployeeId: String,
    val feedbackToFirstName: String,
    val feedbackToLastName: String,
    val feedbackFromId: Long?,
    val feedbackFromEmployeeId: String?,
    val feedbackFromFirstName: String?,
    val feedbackFromLastName: String?,
    val goalId: Long?,
    val goalDescription: String?,
    val request: String?,
    val createdAt: Timestamp,
    val isSubmitted: Boolean?,
    val isExternalRequest: Boolean,
    val externalFeedbackFromEmail: String?,
)

data class ExternalFeedbackRequestData(
    val requestId: Long,
    val request: String?,
    val requestedById: Long,
    val requestedByFirstName: String,
    val requestedByLastName: String,
    val feedbackToId: Long,
    val feedbackToFirstName: String,
    val feedbackToLastName: String,
    val feedbackToTeam: String,
    val feedbackFromId: Long,
    val feedbackFromEmail: String,
    val organisationName: String,
)

data class PendingFeedbackRequestDetails(
    val id: Long,
    val isExternalRequest: Boolean,
    val requestedById: Long,
    val requestedByEmpId: String,
    val requestedByFirstName: String,
    val requestedByLastName: String,
    val feedbackFromId: Long?,
    val feedbackFromFirstName: String?,
    val feedbackFromLastName: String?,
    val feedbackFromEmailId: String?,
    val externalFeedbackFromEmailId: String?,
    val date: Date,
    val organisationName: String,
    val organisationTimeZone: String,
)

data class FeedbackRequestDetails(
    val requestId: Long,
    val isSubmitted: Boolean?,
    val isExternalRequest: Boolean,
    val requestedOn: Timestamp?,
    val goalId: Long?,
    val goalDescription: String?,
    val requestedById: Long,
    val requestedByEmployeeId: String,
    val requestedByFirstName: String,
    val requestedByLastName: String,
    val feedbackToId: Long,
    val feedbackToEmployeeId: String,
    val feedbackToFirstName: String,
    val feedbackToLastName: String,
    val feedbackFromId: Long?,
    val feedbackFromEmployeeId: String?,
    val feedbackFromFirstName: String?,
    val feedbackFromLastName: String?,
    val externalFeedbackFromEmail: String?,
    val request: String?,
    val feedbackData: List<FeedbackDetail>,
)

data class FeedbackDetail(
    val feedbackId: Long,
    val feedback: String,
    val feedbackTypeId: Int,
    val feedbackType: String,
    val isDraft: Boolean,
)
