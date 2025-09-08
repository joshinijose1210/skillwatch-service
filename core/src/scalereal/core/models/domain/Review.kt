package scalereal.core.models.domain

import java.math.BigDecimal
import java.sql.Date
import java.sql.Timestamp

data class Review(
    val reviewId: Long?,
    // Keeping kpiId as id
    val id: Long?,
    val kpiTitle: String?,
    val kpiDescription: String?,
    val kraId: Long?,
    val kraName: String?,
    val review: String?,
    val rating: Int?,
)

data class ReviewDetails(
    val reviewTypeId: Int,
    val reviewDetailsId: Long?,
    val reviewCycleId: Long?,
    val reviewToId: Long?,
    val reviewToEmployeeId: String?,
    val reviewFromId: Long?,
    val reviewFromEmployeeId: String?,
    val draft: Boolean?,
    val published: Boolean?,
    val submittedAt: Timestamp?,
)

data class ReviewResponse(
    val reviewTypeId: Int,
    val reviewDetailsId: Long?,
    val reviewCycleId: Long?,
    val reviewToId: Long?,
    val reviewToEmployeeId: String?,
    val reviewFromId: Long?,
    val reviewFromEmployeeId: String?,
    val draft: Boolean?,
    val published: Boolean?,
    val reviewData: List<Review>,
    val submittedAt: Timestamp?,
)

data class ReviewData(
    val organisationId: Long,
    val reviewTypeId: Int,
    val reviewDetailsId: Long?,
    val reviewCycleId: Long,
    val reviewToId: Long?,
    val reviewFromId: Long?,
    // using at the time of check-in
    val firstManagerId: Long?,
    // using at the time of check-in
    val secondManagerId: Long?,
    val draft: Boolean,
    val published: Boolean,
    val reviewData: List<Review>,
    var averageRating: BigDecimal,
)

data class GoalParams(
    val id: Long,
    val goalId: Int,
    val typeId: Int,
    val assignedTo: Long,
    val createdBy: Long,
    val description: String?,
    val targetDate: Date,
)

fun List<GoalParams>.withSequentialGoalIds(startFrom: Int): List<GoalParams> =
    this.mapIndexed { index, goal ->
        goal.copy(
            goalId = (startFrom + index + 1),
        )
    }

data class CheckInWithManagerRequest(
    val organisationId: Long,
    val reviewTypeId: Int,
    val reviewDetailsId: Long,
    val reviewCycleId: Long,
    val reviewToId: Long,
    val reviewFromId: Long,
    val firstManagerId: Long?,
    val secondManagerId: Long?,
    val draft: Boolean,
    val published: Boolean,
    val reviewData: List<Review>,
    var averageRating: BigDecimal,
    val goals: List<GoalParams>,
)

data class CheckInResponse(
    val reviewTypeId: Int,
    val reviewDetailsId: Long?,
    val reviewCycleId: Long?,
    val reviewToId: Long?,
    val reviewToEmployeeId: String?,
    val reviewFromId: Long?,
    val reviewFromEmployeeId: String?,
    val draft: Boolean?,
    val published: Boolean?,
    val reviewData: List<Review>,
    val goals: List<Goal>,
)

data class AvgReviewRatings(
    val avgSelfReviewRating: BigDecimal,
    val avgFirstManagerRating: BigDecimal,
    val avgSecondManagerReviewRating: BigDecimal,
    val avgCheckInReviewRating: BigDecimal,
)

data class WeightedScore(
    val finalScore: BigDecimal,
    val kraWeightedScores: List<KRAWeightedScore>,
)

data class KRAWeightedScore(
    val kraId: Long,
    val kraName: String,
    val kraWeightage: Int,
    val weightedRating: BigDecimal,
)
