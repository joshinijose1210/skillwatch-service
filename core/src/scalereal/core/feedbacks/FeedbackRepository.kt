package scalereal.core.feedbacks

import scalereal.core.models.domain.AnalyticsFeedbackCount
import scalereal.core.models.domain.CreateFeedbackParams
import scalereal.core.models.domain.Feedback
import scalereal.core.models.domain.FeedbackCounts
import scalereal.core.models.domain.FeedbackData
import scalereal.core.models.domain.Feedbacks
import java.sql.Date

interface FeedbackRepository {
    fun create(createFeedbackParams: CreateFeedbackParams)

    fun update(
        feedbackId: Long,
        feedback: String,
        feedbackToId: Long,
        feedbackTypeId: Int,
        requestId: Long?,
        isDraft: Boolean,
    ): Feedback

    fun fetchAllSubmittedFeedbacks(
        organisationId: Long,
        feedbackFromId: Long,
        feedbackToId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
        offset: Int,
        limit: Int,
        sortBy: String,
    ): List<FeedbackData>

    fun countSubmittedFeedbacks(
        organisationId: Long,
        feedbackFromId: Long,
        feedbackToId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int

    fun fetchAllFeedbacksReceived(
        organisationId: Long,
        feedbackToId: Long,
        feedbackFromId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
        offset: Int,
        limit: Int,
        sortBy: String,
    ): List<FeedbackData>

    fun countFeedbacksReceived(
        organisationId: Long,
        feedbackToId: Long,
        feedbackFromId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int

    fun fetchAllFeedbacks(
        organisationId: Long,
        searchText: String,
        feedbackTypeId: List<Int>,
        fromDate: String?,
        toDate: String?,
        reviewCycleId: List<Int>,
        offset: Int,
        limit: Int,
        sortBy: String?,
    ): List<Feedbacks>

    fun countAllFeedbacks(
        organisationId: Long,
        searchText: String,
        feedbackTypeId: List<Int>,
        fromDate: String?,
        toDate: String?,
        reviewCycleId: List<Int>,
    ): Int

    fun fetchEmployeeFeedbackCounts(
        id: Long,
        startDate: Date?,
        endDate: Date?,
    ): FeedbackCounts

    fun fetchTotalFeedbackCounts(
        organisationId: Long,
        startDate: Date,
        endDate: Date,
    ): AnalyticsFeedbackCount

    fun fetchFeedbackById(
        feedbackId: Long,
        organisationId: Long,
    ): FeedbackData

    fun delete(feedbackId: Long)

    fun markFeedbackAsReadOrUnread(
        id: Long,
        isRead: Boolean,
    )

    fun getUnreadFeedbackCount(
        organisationId: Long,
        feedbackToId: Long,
        feedbackFromId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int
}
