package scalereal.core.feedbacks

import scalereal.core.models.domain.AddFeedbackData
import scalereal.core.models.domain.ExternalFeedbackRequestData
import scalereal.core.models.domain.FeedbackDetail
import scalereal.core.models.domain.FeedbackRequest
import scalereal.core.models.domain.FeedbackRequestData
import scalereal.core.models.domain.FeedbackRequestParams
import scalereal.core.models.domain.PendingFeedbackRequestDetails

interface FeedbackRequestRepository {
    fun fetchFeedbackRequestData(
        feedbackRequestParams: FeedbackRequestParams,
        offset: Int,
        limit: Int,
    ): List<FeedbackRequestData>

    fun countFeedbackRequestData(feedbackRequestParams: FeedbackRequestParams): Int

    fun updateFeedbackRequestStatus(requestId: Long)

    fun addInternalFeedbackRequest(
        requestedBy: Long,
        feedbackToId: List<Long>,
        feedbackFromId: List<Long>,
        goalId: Long?,
        request: String,
    )

    fun fetchFeedbackRequestDetails(requestId: Long): FeedbackRequest

    fun addExternalFeedbackRequest(
        requestedBy: Long,
        feedbackToId: List<Long>,
        feedbackFromId: List<Long>,
        request: String,
        isExternalRequest: Boolean,
    ): List<Long>

    fun addExternalEmails(
        feedbackFromEmail: List<String>,
        organisationId: Long,
    ): List<Long>

    fun getExternalFeedbackRequestData(requestId: Long): ExternalFeedbackRequestData?

    fun addExternalFeedback(
        feedback: List<AddFeedbackData>,
        feedbackToId: Long,
        feedbackFromId: Long,
        requestId: Long,
    )

    fun getPendingFeedbackRequest(): List<PendingFeedbackRequestDetails>

    fun fetchFeedbackByRequestId(requestId: Long): List<FeedbackDetail>
}
