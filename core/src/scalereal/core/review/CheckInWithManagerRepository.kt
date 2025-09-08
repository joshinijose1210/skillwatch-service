package scalereal.core.review

import scalereal.core.models.domain.CheckInWithManagerRequest
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.GoalParams
import scalereal.core.models.domain.Review

interface CheckInWithManagerRepository {
    fun createCheckInWithManager(checkInWithManagerRequest: CheckInWithManagerRequest)

    fun updateSummaryReviews(
        reviewDetailsId: Long?,
        review: List<Review>,
    )

    fun createGoals(
        organisationId: Long,
        reviewDetailsId: Long?,
        goals: List<GoalParams>,
    )

    fun isAllCheckInWithManagerCompleted(
        reviewCycleId: Long?,
        managerId: Long?,
    ): Boolean?

    fun getGoals(reviewDetailsId: Long?): List<Goal>

    fun updateGoals(goals: List<Goal>)

    fun deleteGoals(reviewDetailsId: Long)

    fun getGoalsByReviewCycleId(
        goalToId: Long,
        reviewCycleId: Long,
    ): List<Goal>
}
