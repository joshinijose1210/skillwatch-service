package scalereal.core.feedbacks

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import scalereal.core.emails.FeedbackReceivedMail
import scalereal.core.emails.RequestFeedbackMail
import scalereal.core.employees.EmployeeRepository
import scalereal.core.exception.InvalidFeedbackDataException
import scalereal.core.exception.UpdateFeedbackException
import scalereal.core.models.domain.AddFeedbackData
import scalereal.core.models.domain.CreateFeedbackParams
import scalereal.core.models.domain.FeedbackCounts
import scalereal.core.models.domain.FeedbackData
import scalereal.core.models.domain.FeedbackGraph
import scalereal.core.models.domain.FeedbackPercentages
import scalereal.core.models.domain.Feedbacks
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.models.domain.UpdateFeedbackParams
import scalereal.core.reviewCycle.ReviewCycleRepository
import scalereal.core.slack.RequestFeedbackSlackNotification
import scalereal.core.slack.SlackService
import java.sql.Date

@Singleton
class FeedbackService(
    private val repository: FeedbackRepository,
    private val feedbackRequestRepository: FeedbackRequestRepository,
    private val feedbackReceivedMail: FeedbackReceivedMail,
    private val requestFeedbackMail: RequestFeedbackMail,
    private val slackService: SlackService,
    private val employeeRepository: EmployeeRepository,
    private val requestFeedbackSlackNotification: RequestFeedbackSlackNotification,
) {
    @Inject
    lateinit var reviewCycleRepository: ReviewCycleRepository

    @OptIn(DelicateCoroutinesApi::class)
    private fun sendFeedbackNotifications(
        feedbackToId: Long,
        feedbackFromId: Long,
        requestId: Long?,
        isDraft: Boolean,
        feedbackItems: List<AddFeedbackData>,
    ) {
        GlobalScope.launch {
            if (!isDraft) {
                if (requestId != null) {
                    feedbackRequestRepository.updateFeedbackRequestStatus(requestId)
                    val feedbackRequestDetails =
                        feedbackRequestRepository.fetchFeedbackRequestDetails(requestId)
                    requestFeedbackMail.sendRequestedFeedbackReceivedMail(
                        requestedById = feedbackRequestDetails.requestedById,
                        feedbackFromId = feedbackFromId,
                    )
                    requestFeedbackSlackNotification.requestedFeedbackReceivedNotification(
                        requestedById = feedbackRequestDetails.requestedById,
                        feedbackFromId = feedbackFromId,
                    )
                }
                feedbackReceivedMail.sendFeedbackReceivedMail(
                    feedbackToId,
                    feedbackFromId,
                )
                feedbackItems
                    .filter { it.markdownText != null }
                    .forEach {
                        sendSlackNotification(
                            feedbackFromId = feedbackFromId,
                            feedbackToId = feedbackToId,
                            feedback = it.markdownText!!,
                            feedbackTypeId = it.feedbackTypeId,
                        )
                    }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun create(createFeedbackParams: CreateFeedbackParams) {
        validateFeedbackData(createFeedbackParams.feedback)
        repository.create(createFeedbackParams)
        sendFeedbackNotifications(
            feedbackToId = createFeedbackParams.feedbackToId,
            feedbackFromId = createFeedbackParams.feedbackFromId,
            requestId = createFeedbackParams.requestId,
            isDraft = createFeedbackParams.isDraft,
            feedbackItems = createFeedbackParams.feedback,
        )
    }

    private fun validateFeedbackData(feedback: List<AddFeedbackData>) {
        if (feedback.size != feedback.distinctBy { it.feedbackTypeId }.size) {
            throw InvalidFeedbackDataException("Cannot add more than one feedback of same type.")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun update(updateFeedbackParams: UpdateFeedbackParams) {
        try {
            val feedbackToAddOrUpdate = updateFeedbackParams.feedbackData.filter { !it.isRemoved }
            if (feedbackToAddOrUpdate.size != feedbackToAddOrUpdate.distinctBy { it.feedbackTypeId }.size) {
                throw InvalidFeedbackDataException("Cannot add more than one feedback of same type.")
            }

            updateFeedbackParams.feedbackData.forEach { feedbackData ->
                when {
                    feedbackData.isNewlyAdded -> {
                        repository.create(
                            CreateFeedbackParams(
                                feedbackToId = updateFeedbackParams.feedbackToId,
                                feedbackFromId = updateFeedbackParams.feedbackFromId,
                                requestId = updateFeedbackParams.requestId,
                                isDraft = updateFeedbackParams.isDraft,
                                feedback =
                                    listOf(
                                        AddFeedbackData(
                                            feedbackTypeId = feedbackData.feedbackTypeId,
                                            feedbackText = feedbackData.feedbackText,
                                            markdownText = feedbackData.markdownText,
                                        ),
                                    ),
                            ),
                        )
                    }

                    feedbackData.isRemoved -> {
                        feedbackData.feedbackId?.let { feedbackId ->
                            repository.delete(feedbackId)
                        } ?: throw IllegalArgumentException("Feedback ID is required for deletion")
                    }

                    else -> {
                        feedbackData.feedbackId?.let { feedbackId ->
                            repository.update(
                                feedbackId = feedbackId,
                                isDraft = updateFeedbackParams.isDraft,
                                feedback = feedbackData.feedbackText,
                                feedbackToId = updateFeedbackParams.feedbackToId,
                                feedbackTypeId = feedbackData.feedbackTypeId,
                                requestId = updateFeedbackParams.requestId,
                            )
                        } ?: throw IllegalArgumentException("Feedback ID is required for update")
                    }
                }
            }
        } catch (_: Exception) {
            throw UpdateFeedbackException("This feedback is in published state, so it cannot be edited")
        }

        sendFeedbackNotifications(
            feedbackToId = updateFeedbackParams.feedbackToId,
            feedbackFromId = updateFeedbackParams.feedbackFromId,
            requestId = updateFeedbackParams.requestId,
            isDraft = updateFeedbackParams.isDraft,
            feedbackItems =
                updateFeedbackParams.feedbackData.map {
                    AddFeedbackData(
                        feedbackTypeId = it.feedbackTypeId,
                        feedbackText = it.feedbackText,
                        markdownText = it.markdownText,
                    )
                },
        )
    }

    fun fetchAllSubmittedFeedbacks(
        organisationId: Long,
        feedbackFromId: Long,
        feedbackToId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
        page: Int,
        limit: Int,
        sortBy: String,
    ): List<FeedbackData> =
        repository.fetchAllSubmittedFeedbacks(
            organisationId = organisationId,
            feedbackFromId = feedbackFromId,
            feedbackToId = feedbackToId,
            feedbackTypeId = feedbackTypeId,
            reviewCycleId = reviewCycleId,
            offset = (page - 1) * limit,
            limit = limit,
            sortBy = sortBy,
        )

    fun countSubmittedFeedbacks(
        organisationId: Long,
        feedbackFromId: Long,
        feedbackToId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int =
        repository.countSubmittedFeedbacks(
            organisationId = organisationId,
            feedbackFromId = feedbackFromId,
            feedbackToId = feedbackToId,
            feedbackTypeId = feedbackTypeId,
            reviewCycleId = reviewCycleId,
        )

    fun fetchAllFeedbacksReceived(
        organisationId: Long,
        feedbackToId: Long,
        feedbackFromId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
        page: Int,
        limit: Int,
        sortBy: String,
    ): List<FeedbackData> =
        repository.fetchAllFeedbacksReceived(
            organisationId = organisationId,
            feedbackToId = feedbackToId,
            feedbackFromId = feedbackFromId,
            feedbackTypeId = feedbackTypeId,
            reviewCycleId = reviewCycleId,
            offset = (page - 1) * limit,
            limit = limit,
            sortBy = sortBy,
        )

    fun countFeedbacksReceived(
        organisationId: Long,
        feedbackToId: Long,
        feedbackFromId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int =
        repository.countFeedbacksReceived(
            organisationId = organisationId,
            feedbackToId = feedbackToId,
            feedbackFromId = feedbackFromId,
            feedbackTypeId = feedbackTypeId,
            reviewCycleId = reviewCycleId,
        )

    fun fetchAllFeedbacks(
        organisationId: Long,
        searchText: String?,
        feedbackTypeId: List<Int>,
        fromDate: String?,
        toDate: String?,
        reviewCycleId: List<Int>,
        page: Int,
        limit: Int,
        sortBy: String?,
    ): List<Feedbacks> =
        repository.fetchAllFeedbacks(
            organisationId = organisationId,
            searchText = searchText ?: "",
            feedbackTypeId = feedbackTypeId,
            fromDate = fromDate,
            toDate = toDate,
            reviewCycleId = reviewCycleId,
            offset = (page - 1) * limit,
            limit = limit,
            sortBy = sortBy,
        )

    fun countAllFeedbacks(
        organisationId: Long,
        searchText: String?,
        feedbackTypeId: List<Int>,
        fromDate: String?,
        toDate: String?,
        reviewCycleId: List<Int>,
    ): Int =
        repository.countAllFeedbacks(
            organisationId = organisationId,
            searchText = searchText ?: "",
            feedbackTypeId = feedbackTypeId,
            fromDate = fromDate,
            toDate = toDate,
            reviewCycleId = reviewCycleId,
        )

    fun getFeedbackGraphData(
        id: Long,
        reviewCycleId: List<Long>,
    ): FeedbackGraph {
        var reviewCycleDates: ReviewCycle
        var totalFeedbackCounts = FeedbackCounts(0, 0, 0, 0, 0, 0)
        var startDate: Date?
        var endDate: Date?
        if (reviewCycleId.contains(-99)) {
            return FeedbackGraph(totalFeedbackCounts, getFeedbackPercentage(totalFeedbackCounts))
        }
        reviewCycleId.forEach { cycleId ->
            reviewCycleDates = reviewCycleRepository.fetchReviewCycle(cycleId)
            startDate = reviewCycleDates.startDate
            endDate = reviewCycleDates.endDate
            val feedbackCounts = repository.fetchEmployeeFeedbackCounts(id, startDate, endDate)
            totalFeedbackCounts += feedbackCounts
        }
        return if (reviewCycleId.isNotEmpty()) {
            FeedbackGraph(
                feedbackCounts = totalFeedbackCounts,
                feedbackPercentages = getFeedbackPercentage(totalFeedbackCounts),
            )
        } else {
            throw IllegalStateException("Feedback not found for the selected review cycle.")
        }
    }

    private fun sendSlackNotification(
        feedbackFromId: Long,
        feedbackToId: Long,
        feedback: String,
        feedbackTypeId: Int,
    ) {
        val feedbackFromDetails = employeeRepository.getEmployeeDataByUniqueId(feedbackFromId)
        val feedbackToDetails = employeeRepository.getEmployeeDataByUniqueId(feedbackToId)
        var feedbackFromName = "*${feedbackFromDetails.getEmployeeNameWithEmployeeId()} (User not on Slack)*"
        var feedbackToName = "*${feedbackToDetails.getEmployeeNameWithEmployeeId()} (User not on Slack)*"
        val feedbackFromSlackUser =
            slackService.getUserByEmailId(feedbackFromDetails.emailId, feedbackFromDetails.organisationId)
        val feedbackToSlackUser =
            slackService.getUserByEmailId(feedbackToDetails.emailId, feedbackToDetails.organisationId)

        if (feedbackFromSlackUser != null) feedbackFromName = "<@${feedbackFromSlackUser.id}>"
        if (feedbackToSlackUser != null) feedbackToName = "<@${feedbackToSlackUser.id}>"
        val formattedFeedback = formatSlackMessage(feedback)
        when (feedbackTypeId) {
            FeedbackTypes.Appreciation.id -> {
                val message =
                    "<!channel>\nCongratulations, $feedbackToName! Appreciation:clap: received from $feedbackFromName! :tada:\n" +
                        "$formattedFeedback"
                slackService.postMessageByWebhook(feedbackFromDetails.organisationId, message)
            }

            FeedbackTypes.Positive.id -> {
                val message =
                    "$feedbackToName Positive feedback :thumbsup: received from $feedbackFromName!\n$formattedFeedback"
                if (feedbackToSlackUser != null) {
                    val layoutBlock = slackService.blockBuilder(message)
                    slackService.sendBlockMessageToUser(
                        feedbackFromDetails.organisationId,
                        layoutBlock,
                        feedbackToSlackUser.id,
                        message,
                    )
                }
            }

            FeedbackTypes.Improvement.id -> {
                val message =
                    "$feedbackToName Improvement :signal_strength: received from $feedbackFromName!\n$formattedFeedback"
                if (feedbackToSlackUser != null) {
                    val layoutBlock = slackService.blockBuilder(message)
                    slackService.sendBlockMessageToUser(
                        feedbackFromDetails.organisationId,
                        layoutBlock,
                        feedbackToSlackUser.id,
                        message,
                    )
                }
            }
        }
    }

    private fun formatSlackMessage(feedback: String): String {
        val regex = Regex("\\[(.*?)]\\((.*?)\\)")
        val formattedFeedback = regex.replace(feedback, "<$2|$1>")
        return formattedFeedback.lines().joinToString("\n") { "> $it" } // Adding block quote to whole message
    }

    fun markFeedbackAsReadOrUnread(
        id: Long,
        isRead: Boolean,
    ) = repository.markFeedbackAsReadOrUnread(id, isRead)

    fun getUnreadFeedbackCount(
        organisationId: Long,
        feedbackToId: Long,
        feedbackFromId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int =
        repository.getUnreadFeedbackCount(
            organisationId = organisationId,
            feedbackToId = feedbackToId,
            feedbackFromId = feedbackFromId,
            feedbackTypeId = feedbackTypeId,
            reviewCycleId = reviewCycleId,
        )
}

private fun getFeedbackPercentage(feedbackCounts: FeedbackCounts): FeedbackPercentages {
    val totalSubmittedFeedback =
        feedbackCounts.submittedPositiveCount + feedbackCounts.submittedImprovementCount + feedbackCounts.submittedAppreciationCount
    val totalReceivedFeedback =
        feedbackCounts.receivedPositiveCount + feedbackCounts.receivedImprovementCount + feedbackCounts.receivedAppreciationCount

    val submittedPositivePercentage =
        if (totalSubmittedFeedback.toInt() != 0) {
            calculatePercentage(
                feedbackCounts.submittedPositiveCount,
                totalSubmittedFeedback,
            )
        } else {
            0.00
        }
    val submittedImprovementPercentage =
        if (totalSubmittedFeedback.toInt() != 0) {
            calculatePercentage(
                feedbackCounts.submittedImprovementCount,
                totalSubmittedFeedback,
            )
        } else {
            0.00
        }
    val submittedAppreciationPercentage =
        if (totalSubmittedFeedback.toInt() != 0) {
            calculatePercentage(
                feedbackCounts.submittedAppreciationCount,
                totalSubmittedFeedback,
            )
        } else {
            0.00
        }

    val receivedPositivePercentage =
        if (totalReceivedFeedback.toInt() != 0) {
            calculatePercentage(
                feedbackCounts.receivedPositiveCount,
                totalReceivedFeedback,
            )
        } else {
            0.00
        }
    val receivedImprovementPercentage =
        if (totalReceivedFeedback.toInt() != 0) {
            calculatePercentage(
                feedbackCounts.receivedImprovementCount,
                totalReceivedFeedback,
            )
        } else {
            0.00
        }
    val receivedAppreciationPercentage =
        if (totalReceivedFeedback.toInt() != 0) {
            calculatePercentage(
                feedbackCounts.receivedAppreciationCount,
                totalReceivedFeedback,
            )
        } else {
            0.00
        }

    return FeedbackPercentages(
        submittedPositivePercentage = submittedPositivePercentage,
        submittedImprovementPercentage = submittedImprovementPercentage,
        submittedAppreciationPercentage = submittedAppreciationPercentage,
        receivedPositivePercentage = receivedPositivePercentage,
        receivedImprovementPercentage = receivedImprovementPercentage,
        receivedAppreciationPercentage = receivedAppreciationPercentage,
    )
}

private fun calculatePercentage(
    feedbackCounts: Long,
    totalFeedbackCounts: Long,
): Double = ((feedbackCounts.toDouble() / totalFeedbackCounts.toDouble()) * 100)
