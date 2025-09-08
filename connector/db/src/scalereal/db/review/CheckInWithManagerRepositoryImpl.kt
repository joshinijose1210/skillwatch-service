package scalereal.db.review

import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import review.AddGoalsCommand
import review.AddGoalsParams
import review.DeleteGoalsCommand
import review.DeleteGoalsParams
import review.GetGoalsByReviewCycleParams
import review.GetGoalsByReviewCycleQuery
import review.GetGoalsByReviewCycleResult
import review.GetGoalsParams
import review.GetGoalsQuery
import review.GetGoalsResult
import review.IsCheckInWithManagerCompletedParams
import review.IsCheckInWithManagerCompletedQuery
import review.UpdateGoalsParams
import review.UpdateGoalsQuery
import review.UpdateReviewDetailsParams
import review.UpdateReviewDetailsQuery
import review.UpdateReviewManagerMappingCommand
import review.UpdateReviewManagerMappingParams
import review.UpdateSelfReviewCommand
import review.UpdateSelfReviewParams
import scalereal.core.models.domain.CheckInWithManagerRequest
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.GoalParams
import scalereal.core.models.domain.Review
import scalereal.core.review.CheckInWithManagerRepository
import javax.sql.DataSource

@Singleton
class CheckInWithManagerRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : CheckInWithManagerRepository {
    override fun createCheckInWithManager(checkInWithManagerRequest: CheckInWithManagerRequest): Unit =
        dataSource.connection.use { connection ->
            UpdateReviewDetailsQuery()
                .query(
                    connection,
                    UpdateReviewDetailsParams(
                        checkInWithManagerRequest.draft,
                        checkInWithManagerRequest.published,
                        checkInWithManagerRequest.averageRating,
                        checkInWithManagerRequest.reviewTypeId,
                        checkInWithManagerRequest.reviewDetailsId,
                        checkInWithManagerRequest.reviewCycleId,
                        checkInWithManagerRequest.reviewToId,
                        checkInWithManagerRequest.reviewFromId,
                    ),
                ).map {
                    updateReviewManagerMapping(
                        checkInWithManagerRequest.reviewDetailsId,
                        checkInWithManagerRequest.firstManagerId,
                        checkInWithManagerRequest.secondManagerId,
                    )
                    updateSummaryReviews(
                        checkInWithManagerRequest.reviewDetailsId,
                        checkInWithManagerRequest.reviewData,
                    )
                    deleteGoals(
                        checkInWithManagerRequest.reviewDetailsId,
                    )
                    createGoals(
                        checkInWithManagerRequest.organisationId,
                        checkInWithManagerRequest.reviewDetailsId,
                        checkInWithManagerRequest.goals,
                    )
                }.first()
        }

    private fun updateReviewManagerMapping(
        reviewDetailsId: Long?,
        firstManagerId: Long?,
        secondManagerId: Long?,
    ) {
        dataSource.connection.use { connection ->
            UpdateReviewManagerMappingCommand()
                .command(
                    connection,
                    UpdateReviewManagerMappingParams(
                        reviewDetailsId = reviewDetailsId,
                        firstManagerId = firstManagerId,
                        secondManagerId = secondManagerId,
                    ),
                )
        }
    }

    override fun updateSummaryReviews(
        reviewDetailsId: Long?,
        review: List<Review>,
    ) {
        review.map { summaryReview ->
            dataSource.connection.use { connection ->
                UpdateSelfReviewCommand()
                    .command(
                        connection,
                        UpdateSelfReviewParams(
                            review = summaryReview.review,
                            rating = summaryReview.rating,
                            reviewDetailsId = reviewDetailsId,
                            kpiId = summaryReview.id,
                        ),
                    )
            }
        }
    }

    override fun createGoals(
        organisationId: Long,
        reviewDetailsId: Long?,
        goals: List<GoalParams>,
    ) {
        goals.map { goal ->
            dataSource.connection.use { connection ->
                AddGoalsCommand()
                    .command(
                        connection,
                        AddGoalsParams(
                            reviewDetailsId = reviewDetailsId,
                            description = goal.description,
                            targetDate = goal.targetDate,
                            goalId = goal.goalId,
                            typeId = goal.typeId,
                            assignedTo = goal.assignedTo,
                            createdBy = goal.createdBy,
                            organisationId = organisationId,
                        ),
                    )
            }
        }
    }

    override fun updateGoals(goals: List<Goal>) {
        goals.map { goal ->
            dataSource.connection.use { connection ->
                UpdateGoalsQuery()
                    .query(connection, UpdateGoalsParams(goal.description, goal.id))
            }
        }
    }

    override fun deleteGoals(reviewDetailsId: Long): Unit =
        dataSource.connection.use { connection ->
            DeleteGoalsCommand()
                .command(connection, DeleteGoalsParams(reviewDetailsId))
        }

    override fun getGoals(reviewDetailsId: Long?): List<Goal> =
        dataSource.connection.use { connection ->
            GetGoalsQuery()
                .query(connection, GetGoalsParams(reviewDetailsId = reviewDetailsId))
                .map { it.toGoals() }
        }

    override fun isAllCheckInWithManagerCompleted(
        reviewCycleId: Long?,
        managerId: Long?,
    ): Boolean? =
        dataSource.connection.use { connection ->
            IsCheckInWithManagerCompletedQuery()
                .query(
                    connection,
                    IsCheckInWithManagerCompletedParams(reviewCycleId = reviewCycleId, managerId = managerId),
                )[0]
                .result
        }

    override fun getGoalsByReviewCycleId(
        goalToId: Long,
        reviewCycleId: Long,
    ): List<Goal> =
        dataSource.connection.use { connection ->
            GetGoalsByReviewCycleQuery()
                .query(
                    connection,
                    GetGoalsByReviewCycleParams(goalToId = goalToId, reviewCycleId = reviewCycleId),
                ).map { it.toGoal() }
        }

    private fun GetGoalsByReviewCycleResult.toGoal() =
        Goal(
            id = id,
            goalId = goalId,
            typeId = typeId,
            description = description,
            createdAt = createdAt,
            targetDate = targetDate,
            progressId = progressId,
            progressName = null,
            createdBy = createdBy,
            assignedTo = assignedTo,
        )

    private fun GetGoalsResult.toGoals() =
        Goal(
            id = id,
            goalId = goalId,
            typeId = typeId,
            description = description,
            createdAt = createdAt,
            targetDate = targetDate,
            progressId = progressId,
            progressName = null,
            createdBy = createdBy,
            assignedTo = assignedTo,
        )
}
