package scalereal.core.review

import jakarta.inject.Singleton
import scalereal.core.emails.CheckInWithManagerCompletedMail
import scalereal.core.exception.DuplicateDataException
import scalereal.core.goals.GoalRepository
import scalereal.core.kra.KRARepository
import scalereal.core.models.GoalProgress
import scalereal.core.models.domain.CheckInResponse
import scalereal.core.models.domain.CheckInWithManagerRequest
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.withSequentialGoalIds
import scalereal.core.slack.CheckInSlackNotifications

@Singleton
class CheckInWithManagerService(
    private val repository: CheckInWithManagerRepository,
    private val selfReviewRepository: SelfReviewRepository,
    private val checkInWithManagerCompletedMail: CheckInWithManagerCompletedMail,
    private val checkInSlackNotifications: CheckInSlackNotifications,
    private val kraRepository: KRARepository,
    private val goalRepository: GoalRepository,
) {
    fun createCheckInWithManager(checkInWithManagerRequest: CheckInWithManagerRequest) {
        try {
            if (checkInWithManagerRequest.published) {
                // Group reviews by KRA ID
                val reviewByKRA = checkInWithManagerRequest.reviewData.groupBy { it.kraId }

                // Fetch weightage for all KRAs in one go and map them by kraId
                val kraWeightageMap =
                    kraRepository
                        .getWeightageByIds(reviewByKRA.keys.distinct().toList(), checkInWithManagerRequest.organisationId)
                        .associateBy { it.id }

                // Calculate the weighted average rating for all KRAs
                val totalWeightedAverage =
                    reviewByKRA
                        .map { (kraId, reviews) ->
                            val averageRating = reviews.map { it.rating ?: 0 }.average()
                            val weightage = kraWeightageMap[kraId]?.weightage ?: 0
                            averageRating * weightage / 100
                        }.sum()

                // Set the final average rating as BigDecimal
                checkInWithManagerRequest.averageRating = totalWeightedAverage.toBigDecimal()
            }
            val lastGoalId = goalRepository.getMaxGoalId(checkInWithManagerRequest.organisationId)
            val updatedGoals = checkInWithManagerRequest.goals.withSequentialGoalIds(lastGoalId)

            repository.createCheckInWithManager(checkInWithManagerRequest.copy(goals = updatedGoals))
            if (checkInWithManagerRequest.published) {
                with(checkInWithManagerRequest) {
                    checkInWithManagerCompletedMail.sendMailToEmployee(reviewToId, reviewCycleId, organisationId)
                    checkInSlackNotifications.checkInSubmittedToEmployee(reviewToId, organisationId)
                    if (firstManagerId != null) {
                        if (isAllCheckInWithManagerCompleted(
                                reviewCycleId = reviewCycleId,
                                managerId = firstManagerId,
                            ) == true
                        ) {
                            checkInWithManagerCompletedMail.sendMailToManager(
                                reviewFrom = firstManagerId,
                                reviewCycleId = reviewCycleId,
                                organisationId,
                            )
                            checkInSlackNotifications.checkInSubmittedToManager(reviewFrom = firstManagerId, organisationId)
                        }
                    } else if (secondManagerId != null) {
                        if (isAllCheckInWithManagerCompleted(
                                reviewCycleId = reviewCycleId,
                                managerId = secondManagerId,
                            ) == true
                        ) {
                            checkInWithManagerCompletedMail.sendMailToManager(
                                reviewFrom = secondManagerId,
                                reviewCycleId = reviewCycleId,
                                organisationId,
                            )
                            checkInSlackNotifications.checkInSubmittedToManager(reviewFrom = secondManagerId, organisationId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            when {
                e.localizedMessage.contains("idx_unique_review_manager_mapping")
                -> throw DuplicateDataException("Invalid Input Data. Duplicate review manager getting inserted")
                else -> throw e
            }
        }
    }

    fun fetch(
        reviewTypeId: List<Int>,
        reviewCycleId: Long,
        reviewToId: Long,
        reviewFromId: List<Int>,
    ): List<CheckInResponse> {
        val progressMap =
            GoalProgress
                .getProgressListWithId()
                .associateBy { it.progressId }

        return selfReviewRepository
            .fetch(
                reviewTypeId = reviewTypeId,
                reviewCycleId = reviewCycleId,
                reviewToId = reviewToId,
                reviewFromId = reviewFromId,
            ).map { review ->
                val reviews = selfReviewRepository.getReviews(review.reviewDetailsId)
                val goals =
                    repository.getGoals(review.reviewDetailsId).onEach { item ->
                        item.progressName = progressMap[item.progressId]?.progressName
                    }

                CheckInResponse(
                    review.reviewTypeId,
                    review.reviewDetailsId,
                    review.reviewCycleId,
                    review.reviewToId,
                    review.reviewToEmployeeId,
                    review.reviewFromId,
                    review.reviewFromEmployeeId,
                    review.draft,
                    review.published,
                    reviews,
                    goals,
                )
            }
    }

    private fun isAllCheckInWithManagerCompleted(
        reviewCycleId: Long?,
        managerId: Long?,
    ): Boolean? = repository.isAllCheckInWithManagerCompleted(reviewCycleId, managerId)

    fun updateGoals(goals: List<Goal>) = repository.updateGoals(goals)

    fun getGoalsByReviewCycleId(
        reviewCycleId: Long,
        actionItemToId: Long,
    ): List<Goal> {
        val progressMap =
            GoalProgress
                .getProgressListWithId()
                .associateBy { it.progressId }

        return repository
            .getGoalsByReviewCycleId(
                reviewCycleId = reviewCycleId,
                goalToId = actionItemToId,
            ).onEach { goal ->
                goal.progressName = progressMap[goal.progressId]?.progressName
            }
    }
}
