package scalereal.core.feedbacks

import jakarta.inject.Singleton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import scalereal.core.emails.RequestFeedbackMail
import scalereal.core.exception.InvalidLinkException
import scalereal.core.linkHandling.LinkHandlingService
import scalereal.core.models.domain.AddFeedbackData
import scalereal.core.models.domain.ExternalFeedbackRequestData
import scalereal.core.models.domain.FeedbackRequestData
import scalereal.core.models.domain.FeedbackRequestDetails
import scalereal.core.models.domain.FeedbackRequestParams
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.PendingFeedbackRequestDetails
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.review.CheckInWithManagerService
import scalereal.core.reviewCycle.ReviewCycleService
import scalereal.core.slack.RequestFeedbackSlackNotification

@Singleton
class FeedbackRequestService(
    private val feedbackRequestRepository: FeedbackRequestRepository,
    private val reviewCycleService: ReviewCycleService,
    private val checkInWithManagerService: CheckInWithManagerService,
    private val requestFeedbackMail: RequestFeedbackMail,
    private val requestFeedbackSlackNotification: RequestFeedbackSlackNotification,
    private val linkHandlingService: LinkHandlingService,
    private val organisationRepository: OrganisationRepository,
) {
    fun fetchFeedbackRequestData(
        feedbackRequestParams: FeedbackRequestParams,
        page: Int,
        limit: Int,
    ): List<FeedbackRequestData> =
        feedbackRequestRepository.fetchFeedbackRequestData(feedbackRequestParams, offset = (page - 1) * limit, limit = limit)

    fun countFeedbackRequestData(feedbackRequestParams: FeedbackRequestParams): Int =
        feedbackRequestRepository.countFeedbackRequestData(feedbackRequestParams)

    fun addFeedbackRequest(
        requestedBy: Long,
        feedbackToId: List<Long>,
        feedbackFromId: List<Long>?,
        goalId: Long?,
        request: String,
        feedbackFromEmail: List<String>?,
        isExternalRequest: Boolean,
        organisationId: Long,
        markdownRequest: String,
    ) {
        try {
            when {
                !feedbackFromId.isNullOrEmpty() ->
                    addInternalFeedbackRequest(
                        requestedBy = requestedBy,
                        feedbackToId = feedbackToId,
                        feedbackFromId = feedbackFromId,
                        goalId = goalId,
                        request = request,
                        markdownRequest = markdownRequest,
                    )
                isExternalRequest && !feedbackFromEmail.isNullOrEmpty() ->
                    addExternalFeedbackRequest(
                        isExternalRequest = isExternalRequest,
                        requestedBy = requestedBy,
                        feedbackToId = feedbackToId,
                        feedbackFromEmail = feedbackFromEmail,
                        request = request,
                        organisationId = organisationId,
                    )
                else -> throw Exception("Invalid input for requesting feedback.")
            }
        } catch (e: Exception) {
            throw Exception(e.message.toString())
        }
    }

    private fun sendNotification(
        requestedBy: Long,
        feedbackFromId: List<Long>,
        feedbackToId: List<Long>,
        request: String,
        markdownRequest: String,
    ) {
        requestFeedbackMail.sendRequestReceivedMail(
            requestedById = requestedBy,
            feedbackFromId = feedbackFromId,
            feedbackToId = feedbackToId,
            request = request,
        )
        requestFeedbackSlackNotification.requestReceivedNotification(
            requestedById = requestedBy,
            feedbackFromId = feedbackFromId,
            feedbackToId = feedbackToId,
            request = markdownRequest,
        )
    }

    fun getGoals(
        organisationId: Long,
        feedbackToId: Long,
    ): List<Goal> {
        val reviewCycleId = reviewCycleService.getPreviousReviewCycleId(organisationId).firstOrNull()
        return reviewCycleId?.let {
            val goals =
                checkInWithManagerService.getGoalsByReviewCycleId(
                    reviewCycleId = reviewCycleId,
                    actionItemToId = feedbackToId,
                )
            if (goals.isEmpty()) throw Exception("There is no goal given in the previous review cycle.")
            goals
        } ?: throw Exception("There is no previous review cycle.")
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun addInternalFeedbackRequest(
        requestedBy: Long,
        feedbackToId: List<Long>,
        feedbackFromId: List<Long>,
        goalId: Long?,
        request: String,
        markdownRequest: String,
    ) {
        when {
            (goalId == null || (feedbackToId.size == 1 && feedbackToId.single() == requestedBy)) -> {
                feedbackRequestRepository.addInternalFeedbackRequest(
                    requestedBy = requestedBy,
                    feedbackToId = feedbackToId,
                    goalId = goalId,
                    feedbackFromId = feedbackFromId,
                    request = request,
                )
            }
            (feedbackToId.single() != requestedBy) || feedbackToId.size > 1 ->
                throw Exception("You can not Request Feedback on an Goal for other employee.")
            else -> throw Exception("Invalid input for requesting internal feedback.")
        }
        GlobalScope.launch {
            sendNotification(requestedBy, feedbackFromId, feedbackToId, request, markdownRequest)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun addExternalFeedbackRequest(
        requestedBy: Long,
        feedbackToId: List<Long>,
        feedbackFromEmail: List<String>,
        request: String,
        organisationId: Long,
        isExternalRequest: Boolean,
    ) {
        val feedbackFromId = feedbackRequestRepository.addExternalEmails(feedbackFromEmail, organisationId)
        val requestId =
            feedbackRequestRepository.addExternalFeedbackRequest(
                isExternalRequest = isExternalRequest,
                requestedBy = requestedBy,
                feedbackToId = feedbackToId,
                feedbackFromId = feedbackFromId,
                request = request,
            )
        GlobalScope.launch {
            val organisationName = organisationRepository.getOrganisationDetails(organisationId).name
            requestId.forEach { id ->
                val feedbackRequestData = feedbackRequestRepository.getExternalFeedbackRequestData(requestId = id)
                if (feedbackRequestData != null) {
                    val isSelfRequest = feedbackRequestData.requestedById == feedbackRequestData.feedbackToId
                    requestFeedbackMail.sendExternalFeedbackRequestMail(
                        requestedByName = "${feedbackRequestData.requestedByFirstName} ${feedbackRequestData.requestedByLastName}",
                        feedbackToName = "${feedbackRequestData.feedbackToFirstName} ${feedbackRequestData.feedbackToLastName}",
                        feedbackFromEmail = feedbackRequestData.feedbackFromEmail,
                        request = request,
                        requestId = id,
                        organisationName = organisationName,
                        isSelfRequest = isSelfRequest,
                    )
                }
            }
        }
    }

    fun getExternalFeedbackRequestData(
        linkId: String,
        requestId: Long,
    ): ExternalFeedbackRequestData? {
        try {
            checkExternalFeedbackLinkValidity(linkId)
            return feedbackRequestRepository.getExternalFeedbackRequestData(requestId = requestId)
        } catch (e: Exception) {
            throw Exception(e.message.toString())
        }
    }

    fun getPendingFeedbackRequest(): List<PendingFeedbackRequestDetails> = feedbackRequestRepository.getPendingFeedbackRequest()

    fun addExternalFeedback(
        linkId: String,
        feedbackToId: Long,
        feedbackFromId: Long,
        feedback: List<AddFeedbackData>,
        requestId: Long,
    ) {
        try {
            checkExternalFeedbackLinkValidity(linkId)
            feedbackRequestRepository.updateFeedbackRequestStatus(requestId)
            feedbackRequestRepository.addExternalFeedback(feedback, feedbackToId, feedbackFromId, requestId)
            linkHandlingService.updateLinkDetails(linkId)
        } catch (e: Exception) {
            throw Exception(e.message.toString())
        }
    }

    private fun checkExternalFeedbackLinkValidity(linkId: String) {
        val linkDetails = linkHandlingService.fetchLinkDetails(linkId = linkId)
        when {
            (linkDetails.noOfHit > 0) ->
                throw InvalidLinkException("Feedback already submitted.")
        }
    }

    fun getFeedbackRequestDetailsById(id: Long): FeedbackRequestDetails {
        val feedbackRequest = feedbackRequestRepository.fetchFeedbackRequestDetails(requestId = id)
        val feedback =
            feedbackRequestRepository
                .fetchFeedbackByRequestId(requestId = id)
                .sortedBy { it.feedbackTypeId }
        return FeedbackRequestDetails(
            requestId = feedbackRequest.id,
            isSubmitted = feedbackRequest.isSubmitted,
            isExternalRequest = feedbackRequest.isExternalRequest,
            requestedOn = feedbackRequest.createdAt,
            goalId = feedbackRequest.goalId,
            goalDescription = feedbackRequest.goalDescription,
            requestedById = feedbackRequest.requestedById,
            requestedByEmployeeId = feedbackRequest.requestedByEmployeeId,
            requestedByFirstName = feedbackRequest.requestedByFirstName,
            requestedByLastName = feedbackRequest.requestedByLastName,
            feedbackToId = feedbackRequest.feedbackToId,
            feedbackToEmployeeId = feedbackRequest.feedbackToEmployeeId,
            feedbackToFirstName = feedbackRequest.feedbackToFirstName,
            feedbackToLastName = feedbackRequest.feedbackToLastName,
            feedbackFromId = feedbackRequest.feedbackFromId,
            feedbackFromEmployeeId = feedbackRequest.feedbackFromEmployeeId,
            feedbackFromFirstName = feedbackRequest.feedbackFromFirstName,
            feedbackFromLastName = feedbackRequest.feedbackFromLastName,
            externalFeedbackFromEmail = feedbackRequest.externalFeedbackFromEmail,
            request = feedbackRequest.request,
            feedbackData = feedback,
        )
    }
}
