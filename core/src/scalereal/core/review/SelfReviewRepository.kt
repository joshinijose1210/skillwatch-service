package scalereal.core.review

import scalereal.core.models.domain.AvgReviewRatings
import scalereal.core.models.domain.Employee
import scalereal.core.models.domain.GoalBasicDetails
import scalereal.core.models.domain.Review
import scalereal.core.models.domain.ReviewData
import scalereal.core.models.domain.ReviewDetails
import java.math.BigDecimal

interface SelfReviewRepository {
    fun create(reviewData: ReviewData)

    fun insertReviews(
        reviewDetailsId: Long?,
        review: List<Review>,
    )

    fun fetch(
        reviewTypeId: List<Int>,
        reviewCycleId: Long,
        reviewToId: Long,
        reviewFromId: List<Int>,
    ): List<ReviewDetails>

    fun getReviews(reviewDetailsId: Long?): List<Review>

    fun update(reviewData: ReviewData)

    fun updateReviews(
        reviewDetailsId: Long?,
        reviewData: List<Review>,
    )

    fun fetchInCompleteSelfReviewsEmployees(
        reviewCycleId: Long,
        organisationId: Long,
    ): List<Employee>

    fun isAllManagerReviewCompleted(
        reviewCycleId: Long?,
        managerEmployeeId: Long?,
    ): Boolean?

    fun fetchInCompleteManagerReviewEmployees(
        organisationId: Long,
        reviewCycleId: Long,
        managerId: Long,
    ): List<Employee>

    fun fetchInCompleteCheckInWithManagerEmployees(
        reviewCycleId: Long,
        managerId: Long,
    ): List<Employee>

    fun getReviewRatingGraphData(
        organisationId: Long,
        reviewToId: Long,
        reviewCycleId: List<Int>,
    ): AvgReviewRatings

    fun fetchGoalsWithPastDeadline(): List<GoalBasicDetails>

    fun updateReview(
        reviewId: Long,
        review: String?,
        rating: Int?,
    )

    fun getReviewById(reviewId: Long): Review

    fun getAverageRating(
        reviewCycleId: Long,
        reviewDetailsId: Long,
    ): BigDecimal?
}
