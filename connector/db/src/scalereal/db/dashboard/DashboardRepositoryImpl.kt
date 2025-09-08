package scalereal.db.dashboard

import dashboard.GetEmployeeFeedbacksCountParams
import dashboard.GetEmployeeFeedbacksCountQuery
import dashboard.GetEmployeeFeedbacksParams
import dashboard.GetEmployeeFeedbacksQuery
import dashboard.GetEmployeeFeedbacksResult
import dashboard.GetGoalsCountParams
import dashboard.GetGoalsCountQuery
import dashboard.GetGoalsParams
import dashboard.GetGoalsQuery
import dashboard.GetReviewCycleAndFeedbackOverviewParams
import dashboard.GetReviewCycleAndFeedbackOverviewQuery
import dashboard.GetReviewCycleAndFeedbackOverviewResult
import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.query
import scalereal.core.dashboard.DashboardRepository
import scalereal.core.feedbacks.FeedbackTypes
import scalereal.core.models.domain.AppreciationData
import scalereal.core.models.domain.FeedbacksData
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.OverviewData
import javax.sql.DataSource

@Singleton
class DashboardRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : DashboardRepository {
    override fun fetchFeedbackOverview(
        organisationId: Long,
        id: Long,
    ): List<OverviewData> =
        dataSource.connection.use { connection ->
            GetReviewCycleAndFeedbackOverviewQuery()
                .query(
                    connection,
                    GetReviewCycleAndFeedbackOverviewParams(organisationId = organisationId, id = id),
                ).map { it.toDashboardOverview() }
        }

    override fun fetchEmployeeFeedback(
        organisationId: Long,
        id: List<Int>,
        reviewCycleId: List<Int>,
        feedbackTypeId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<Any> =
        dataSource.connection.use { connection ->
            GetEmployeeFeedbacksQuery()
                .query(
                    connection,
                    GetEmployeeFeedbacksParams(
                        organisationId = organisationId,
                        id = id.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                        feedbackTypeId = feedbackTypeId.toTypedArray(),
                        offset = offset,
                        limit = limit,
                    ),
                ).map {
                    if (feedbackTypeId.contains(FeedbackTypes.Appreciation.id)) {
                        it.toEmployeeAppreciation()
                    } else {
                        it.toEmployeeFeedbacks()
                    }
                }
        }

    override fun countEmployeeFeedback(
        organisationId: Long,
        id: List<Int>,
        reviewCycleId: List<Int>,
        feedbackTypeId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetEmployeeFeedbacksCountQuery()
                .query(
                    connection,
                    GetEmployeeFeedbacksCountParams(
                        organisationId = organisationId,
                        id = id.toTypedArray(),
                        reviewCycleId = reviewCycleId.toTypedArray(),
                        feedbackTypeId = feedbackTypeId.toTypedArray(),
                    ),
                )[0]
                .employeeFeedbacksCount
                ?.toInt() ?: 0
        }

    override fun fetchGoal(
        organisationId: Long,
        id: Long,
        reviewCycleId: Int,
        offset: Int,
        limit: Int,
    ): List<Goal> =
        dataSource.connection.use { connection ->
            GetGoalsQuery()
                .query(
                    connection,
                    GetGoalsParams(
                        organisationId = organisationId,
                        reviewToId = id,
                        reviewCycleId = reviewCycleId.toLong(),
                        offset = offset,
                        limit = limit,
                    ),
                ).map {
                    Goal(
                        id = it.id,
                        goalId = it.goalId,
                        typeId = it.typeId,
                        description = it.description,
                        createdAt = it.createdAt,
                        targetDate = it.targetDate,
                        progressId = it.progressId,
                        progressName = null,
                        createdBy = it.createdBy,
                        assignedTo = it.assignedTo,
                    )
                }
        }

    override fun countGoal(
        organisationId: Long,
        id: Long,
        reviewCycleId: Int,
    ): Int =
        dataSource.connection.use { connection ->
            GetGoalsCountQuery()
                .query(
                    connection,
                    GetGoalsCountParams(
                        organisationId = organisationId,
                        reviewToId = id,
                        reviewCycleId = reviewCycleId.toLong(),
                    ),
                )[0]
                .goalsCount
                ?.toInt() ?: 0
        }

    private fun GetReviewCycleAndFeedbackOverviewResult.toDashboardOverview() =
        OverviewData(
            reviewCycleId = reviewCycleId,
            firstName = firstName,
            startDate = startDate,
            endDate = endDate,
            selfReviewStartDate = selfReviewStartDate,
            selfReviewEndDate = selfReviewEndDate,
            selfReviewDraft = selfreviewdraft,
            selfReviewPublish = selfreviewpublish,
            positive = positive,
            improvement = improvement,
            appreciation = appreciation,
        )

    private fun GetEmployeeFeedbacksResult.toEmployeeAppreciation() =
        AppreciationData(
            isExternalFeedback = externalFeedbackFromEmailId != null,
            appreciationToId = feedbackToId,
            appreciationToEmployeeId = feedbackToEmployeeId,
            appreciationToFirstName = feedbackToFirstName,
            appreciationToLastName = feedbackToLastName,
            appreciationToRoleName = feedbackToRoleName,
            appreciation = feedback,
            appreciationFromId = feedbackFromId,
            appreciationFromEmployeeId = feedbackFromEmployeeId,
            appreciationFromFirstName = feedbackFromFirstName,
            appreciationFromLastName = feedbackFromLastName,
            appreciationFromRoleName = feedbackFromRoleName,
            externalFeedbackFromEmailId = externalFeedbackFromEmailId,
            submitDate = submitDate,
            isDraft = isDraft,
        )

    private fun GetEmployeeFeedbacksResult.toEmployeeFeedbacks() =
        FeedbacksData(
            isExternalFeedback = externalFeedbackFromEmailId != null,
            feedback = feedback,
            feedbackFromId = feedbackFromId,
            feedbackFromEmployeeId = feedbackFromEmployeeId,
            feedbackFromFirstName = feedbackFromFirstName,
            feedbackFromLastName = feedbackFromLastName,
            feedbackFromRoleName = feedbackFromRoleName,
            externalFeedbackFromEmailId = externalFeedbackFromEmailId,
            submitDate = submitDate,
            feedbackType = feedbackType,
            isDraft = isDraft,
        )
}
