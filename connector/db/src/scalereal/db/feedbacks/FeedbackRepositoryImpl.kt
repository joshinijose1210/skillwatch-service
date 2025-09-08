package scalereal.db.feedbacks

import feedbacks.AddFeedbackParams
import feedbacks.AddFeedbackQuery
import feedbacks.DeleteFeedbackCommand
import feedbacks.DeleteFeedbackParams
import feedbacks.EditFeedbackParams
import feedbacks.EditFeedbackQuery
import feedbacks.GetAllFeedbacksCountParams
import feedbacks.GetAllFeedbacksCountQuery
import feedbacks.GetAllFeedbacksParams
import feedbacks.GetAllFeedbacksQuery
import feedbacks.GetAllFeedbacksReceivedParams
import feedbacks.GetAllFeedbacksReceivedQuery
import feedbacks.GetAllFeedbacksReceivedResult
import feedbacks.GetAllFeedbacksResult
import feedbacks.GetAllSubmittedFeedbacksParams
import feedbacks.GetAllSubmittedFeedbacksQuery
import feedbacks.GetAllSubmittedFeedbacksResult
import feedbacks.GetEmployeeFeedbackCountsParams
import feedbacks.GetEmployeeFeedbackCountsQuery
import feedbacks.GetEmployeeFeedbackCountsResult
import feedbacks.GetFeedbackByIdParams
import feedbacks.GetFeedbackByIdQuery
import feedbacks.GetFeedbacksReceivedCountParams
import feedbacks.GetFeedbacksReceivedCountQuery
import feedbacks.GetSubmittedFeedbacksCountParams
import feedbacks.GetSubmittedFeedbacksCountQuery
import feedbacks.GetTotalFeedbackCountsParams
import feedbacks.GetTotalFeedbackCountsQuery
import feedbacks.GetTotalFeedbackCountsResult
import feedbacks.GetUnreadFeedbackCountParams
import feedbacks.GetUnreadFeedbackCountQuery
import feedbacks.MarkFeedbackAsReadOrUnreadCommand
import feedbacks.MarkFeedbackAsReadOrUnreadParams
import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import scalereal.core.feedbacks.FeedbackRepository
import scalereal.core.models.domain.AnalyticsFeedbackCount
import scalereal.core.models.domain.CreateFeedbackParams
import scalereal.core.models.domain.Feedback
import scalereal.core.models.domain.FeedbackCounts
import scalereal.core.models.domain.FeedbackData
import scalereal.core.models.domain.Feedbacks
import scalereal.db.util.getWildCardedString
import java.sql.Date
import javax.sql.DataSource

@Singleton
class FeedbackRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : FeedbackRepository {
    override fun create(createFeedbackParams: CreateFeedbackParams): Unit =
        dataSource.connection.use { connection ->
            createFeedbackParams.feedback.forEach {
                AddFeedbackQuery()
                    .query(
                        connection,
                        AddFeedbackParams(
                            feedback = it.feedbackText,
                            feedbackToId = createFeedbackParams.feedbackToId,
                            feedbackFromId = createFeedbackParams.feedbackFromId,
                            feedbackTypeId = it.feedbackTypeId,
                            requestId = createFeedbackParams.requestId,
                            isDraft = createFeedbackParams.isDraft,
                        ),
                    )
            }
        }

    override fun update(
        feedbackId: Long,
        feedback: String,
        feedbackToId: Long,
        feedbackTypeId: Int,
        requestId: Long?,
        isDraft: Boolean,
    ): Feedback =
        dataSource.connection.use { connection ->
            EditFeedbackQuery()
                .query(
                    connection,
                    EditFeedbackParams(
                        id = feedbackId,
                        feedback = feedback,
                        feedbackToId = feedbackToId,
                        feedbackTypeId = feedbackTypeId,
                        requestId = requestId,
                        idDraft = isDraft,
                    ),
                ).map {
                    Feedback(it.srNo, it.feedback, it.feedbackTo, it.feedbackFrom, it.feedbackTypeId, it.isDraft)
                }.first()
        }

    override fun fetchAllSubmittedFeedbacks(
        organisationId: Long,
        feedbackFromId: Long,
        feedbackToId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
        offset: Int,
        limit: Int,
        sortBy: String,
    ): List<FeedbackData> =
        dataSource.connection.use { connection ->
            GetAllSubmittedFeedbacksQuery()
                .query(
                    connection,
                    GetAllSubmittedFeedbacksParams(
                        organisationId = organisationId,
                        feedbackFromId = feedbackFromId,
                        feedbackToId = feedbackToId.toTypedArray(),
                        feedbackTypeId = feedbackTypeId.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                        offset = offset,
                        limit = limit,
                        sortBy = sortBy,
                    ),
                ).map { it.toSubmittedFeedback() }
        }

    override fun countSubmittedFeedbacks(
        organisationId: Long,
        feedbackFromId: Long,
        feedbackToId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetSubmittedFeedbacksCountQuery()
                .query(
                    connection,
                    GetSubmittedFeedbacksCountParams(
                        organisationId = organisationId,
                        feedbackFromId = feedbackFromId,
                        feedbackToId = feedbackToId.toTypedArray(),
                        feedbackTypeId = feedbackTypeId.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                    ),
                )[0]
                .feedbackCount
                ?.toInt() ?: 0
        }

    override fun fetchAllFeedbacksReceived(
        organisationId: Long,
        feedbackToId: Long,
        feedbackFromId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
        offset: Int,
        limit: Int,
        sortBy: String,
    ): List<FeedbackData> =
        dataSource.connection.use { connection ->
            GetAllFeedbacksReceivedQuery()
                .query(
                    connection,
                    GetAllFeedbacksReceivedParams(
                        organisationId = organisationId,
                        feedbackToId = feedbackToId,
                        feedbackFromId = feedbackFromId.toTypedArray(),
                        feedbackTypeId = feedbackTypeId.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                        offset = offset,
                        limit = limit,
                        sortBy = sortBy,
                    ),
                ).map {
                    it.toFeedbackReceived()
                }
        }

    override fun countFeedbacksReceived(
        organisationId: Long,
        feedbackToId: Long,
        feedbackFromId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetFeedbacksReceivedCountQuery()
                .query(
                    connection,
                    GetFeedbacksReceivedCountParams(
                        organisationId = organisationId,
                        feedbackToId = feedbackToId,
                        feedbackFromId = feedbackFromId.toTypedArray(),
                        feedbackTypeId = feedbackTypeId.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                    ),
                )[0]
                .feedbackCount
                ?.toInt() ?: 0
        }

    override fun fetchAllFeedbacks(
        organisationId: Long,
        searchText: String,
        feedbackTypeId: List<Int>,
        fromDate: String?,
        toDate: String?,
        reviewCycleId: List<Int>,
        offset: Int,
        limit: Int,
        sortBy: String?,
    ): List<Feedbacks> =
        dataSource.connection.use { connection ->
            GetAllFeedbacksQuery()
                .query(
                    connection,
                    GetAllFeedbacksParams(
                        fromDate = fromDate,
                        toDate = toDate,
                        search = getWildCardedString(searchText),
                        offset = offset,
                        limit = limit,
                        organisationId = organisationId,
                        feedbackTypeId = feedbackTypeId.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                        sortBy = sortBy,
                    ),
                ).map { it.toFeedbacks() }
        }

    override fun countAllFeedbacks(
        organisationId: Long,
        searchText: String,
        feedbackTypeId: List<Int>,
        fromDate: String?,
        toDate: String?,
        reviewCycleId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetAllFeedbacksCountQuery()
                .query(
                    connection,
                    GetAllFeedbacksCountParams(
                        fromDate = fromDate,
                        toDate = toDate,
                        search = getWildCardedString(searchText),
                        organisationId = organisationId,
                        feedbackTypeId = feedbackTypeId.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                    ),
                )[0]
                .feedbackCount
                ?.toInt() ?: 0
        }

    override fun fetchEmployeeFeedbackCounts(
        id: Long,
        startDate: Date?,
        endDate: Date?,
    ): FeedbackCounts =
        dataSource.connection.use { connection ->
            GetEmployeeFeedbackCountsQuery()
                .query(
                    connection,
                    GetEmployeeFeedbackCountsParams(id = id, startDate = startDate, endDate = endDate),
                )[0]
                .toFeedbackCounts()
        }

    override fun fetchTotalFeedbackCounts(
        organisationId: Long,
        startDate: Date,
        endDate: Date,
    ): AnalyticsFeedbackCount =
        dataSource.connection.use { connection ->
            GetTotalFeedbackCountsQuery()
                .query(
                    connection,
                    GetTotalFeedbackCountsParams(organisationId = organisationId, startDate = startDate, endDate = endDate),
                )[0]
                .toTotalFeedbackCounts()
        }

    override fun fetchFeedbackById(
        feedbackId: Long,
        organisationId: Long,
    ): FeedbackData =
        dataSource.connection.use { connection ->
            GetFeedbackByIdQuery()
                .query(connection, GetFeedbackByIdParams(feedbackId = feedbackId, organisationId = organisationId))
                .map {
                    FeedbackData(
                        srNo = it.srNo,
                        date = it.updatedAt,
                        organisationId = it.organisationId,
                        feedbackToId = it.feedbackToId,
                        feedbackToEmployeeId = it.feedbackToEmployeeId,
                        feedbackFromId = it.feedbackFromId,
                        feedbackFromEmployeeId = it.feedbackFromEmployeeId,
                        empFirstName = it.firstName,
                        empLastName = it.lastName,
                        empRoleName = it.roleName,
                        feedback = it.feedback,
                        feedbackTypeId = it.feedbackTypeId,
                        feedbackType = it.name,
                        isDraft = it.isDraft,
                        isExternalFeedback = false,
                        externalFeedbackFromEmailId = null,
                        isRead = it.isRead,
                    )
                }.first()
        }

    override fun delete(feedbackId: Long) {
        dataSource.connection.use { connection ->
            DeleteFeedbackCommand()
                .command(connection, DeleteFeedbackParams(id = feedbackId))
        }
    }

    override fun markFeedbackAsReadOrUnread(
        id: Long,
        isRead: Boolean,
    ) {
        dataSource.connection.use { connection ->
            MarkFeedbackAsReadOrUnreadCommand()
                .command(connection, MarkFeedbackAsReadOrUnreadParams(id = id, is_read = isRead))
        }
    }

    override fun getUnreadFeedbackCount(
        organisationId: Long,
        feedbackToId: Long,
        feedbackFromId: List<Int>,
        feedbackTypeId: List<Int>,
        reviewCycleId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetUnreadFeedbackCountQuery()
                .query(
                    connection,
                    GetUnreadFeedbackCountParams(
                        organisationId = organisationId,
                        feedbackFromId = feedbackFromId.toTypedArray(),
                        feedbackTypeId = feedbackTypeId.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                        feedbackToId = feedbackToId,
                    ),
                ).firstOrNull()
                ?.count
                ?.toInt() ?: 0
        }
}

private fun GetTotalFeedbackCountsResult.toTotalFeedbackCounts() =
    AnalyticsFeedbackCount(
        positive = positiveCount ?: 0,
        improvement = improvementCount ?: 0,
        appreciation = appreciationCount ?: 0,
    )

private fun GetEmployeeFeedbackCountsResult.toFeedbackCounts() =
    FeedbackCounts(
        submittedPositiveCount = submittedPositiveCount ?: 0,
        submittedImprovementCount = submittedImprovementCount ?: 0,
        submittedAppreciationCount = submittedAppreciationCount ?: 0,
        receivedPositiveCount = receivedPositiveCount ?: 0,
        receivedImprovementCount = receivedImprovementCount ?: 0,
        receivedAppreciationCount = receivedAppreciationCount ?: 0,
    )

private fun GetAllFeedbacksResult.toFeedbacks() =
    Feedbacks(
        date = updatedAt,
        feedback = feedback,
        feedbackToId = feedbackToId,
        feedbackToEmployeeId = feedbackToEmployeeId,
        feedbackFromId = feedbackFromId,
        feedbackFromEmployeeId = feedbackFromEmployeeId,
        feedbackTypeId = feedbackTypeId,
        feedbackType = name,
        toEmpName = "$feedbackToFirstName $feedbackToLastName",
        fromEmpName = "$feedbackFromFirstName $feedbackFromLastName".takeIf { feedbackFromFirstName != null },
        toRoleName = feedbackToRole,
        fromRoleName = feedbackFromRole,
        organisationId = organisationId,
        isDraft = isDraft,
        isExternalFeedback = externalFeedbackFromEmail != null,
        externalFeedbackFromEmailId = externalFeedbackFromEmail,
    )

private fun GetAllFeedbacksReceivedResult.toFeedbackReceived() =
    FeedbackData(
        srNo = srNo,
        date = updatedAt,
        organisationId = organisationId,
        feedbackToId = feedbackToId,
        feedbackToEmployeeId = feedbackToEmployeeId,
        feedbackFromId = feedbackFromId,
        feedbackFromEmployeeId = feedbackFromEmployeeId,
        empFirstName = feedbackFromFirstName,
        empLastName = feedbackFromLastName,
        empRoleName = feedbackFromRole,
        feedback = feedback,
        feedbackTypeId = feedbackTypeId,
        feedbackType = name,
        isDraft = isDraft,
        isExternalFeedback = externalFeedbackFromEmail != null,
        externalFeedbackFromEmailId = externalFeedbackFromEmail,
    )

private fun GetAllSubmittedFeedbacksResult.toSubmittedFeedback() =
    FeedbackData(
        srNo = srNo,
        date = updatedAt,
        organisationId = organisationId,
        feedbackToId = feedbackToId,
        feedbackToEmployeeId = feedbackToEmployeeId,
        feedbackFromId = feedbackFromId,
        feedbackFromEmployeeId = feedbackFromEmployeeId,
        empFirstName = firstName,
        empLastName = lastName,
        empRoleName = roleName,
        feedback = feedback,
        feedbackTypeId = feedbackTypeId,
        feedbackType = name,
        isDraft = isDraft,
        isExternalFeedback = false,
        externalFeedbackFromEmailId = null,
    )
