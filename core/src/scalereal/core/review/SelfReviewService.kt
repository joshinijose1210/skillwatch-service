package scalereal.core.review

import jakarta.inject.Singleton
import scalereal.core.dto.ReviewType
import scalereal.core.dto.UpdateReviewRequest
import scalereal.core.emails.ManagerReviewSubmittedMail
import scalereal.core.emails.SelfReviewSubmittedMail
import scalereal.core.exception.CheckInReviewException
import scalereal.core.exception.DuplicateDataException
import scalereal.core.exception.ReviewCycleException
import scalereal.core.exception.ReviewException
import scalereal.core.kra.KRARepository
import scalereal.core.models.domain.AvgReviewRatings
import scalereal.core.models.domain.Employee
import scalereal.core.models.domain.GoalBasicDetails
import scalereal.core.models.domain.KRAWeightedScore
import scalereal.core.models.domain.Review
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.models.domain.ReviewData
import scalereal.core.models.domain.ReviewResponse
import scalereal.core.models.domain.WeightedScore
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.reviewCycle.ReviewCycleRepository
import scalereal.core.slack.ManagerReviewSlackNotifications
import scalereal.core.slack.SelfReviewSlackNotifications
import java.math.BigDecimal
import java.math.RoundingMode

@Singleton
class SelfReviewService(
    private val repository: SelfReviewRepository,
    private val reviewCycleRepository: ReviewCycleRepository,
    private val selfReviewSubmittedMail: SelfReviewSubmittedMail,
    private val managerReviewSubmittedMail: ManagerReviewSubmittedMail,
    private val selfReviewSlackNotifications: SelfReviewSlackNotifications,
    private val managerReviewSlackNotifications: ManagerReviewSlackNotifications,
    private val kraRepository: KRARepository,
    private val organisationRepository: OrganisationRepository,
) {
    fun create(reviewData: ReviewData) {
        try {
            val organisationTimeZone = organisationRepository.getOrganisationDetails(reviewData.organisationId).timeZone
            val reviewCycle =
                validateAndGetActiveReviewCycle(
                    reviewData.organisationId,
                    reviewData.reviewCycleId,
                    organisationTimeZone,
                )

            if (!reviewData.draft && reviewData.published) {
                reviewData.averageRating = calculateAverageRating(reviewData)
            }

            when (reviewData.reviewTypeId) {
                ReviewType.SELF.id -> validateAndCreateSelfReview(reviewData, reviewCycle)
                ReviewType.MANAGER.id -> validateAndCreateManagerReview(reviewData, reviewCycle)
                ReviewType.CHECK_IN.id -> validateAndCreateCheckInReview(reviewData, reviewCycle)
                else -> throw Exception(
                    "Invalid review type ID '${reviewData.reviewTypeId}'. Only Self(1), Manager(2), and Check-in(3) are allowed.",
                )
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
    ): List<ReviewResponse> =
        repository
            .fetch(
                reviewTypeId = reviewTypeId,
                reviewCycleId = reviewCycleId,
                reviewToId = reviewToId,
                reviewFromId = reviewFromId,
            ).map {
                ReviewResponse(
                    it.reviewTypeId,
                    it.reviewDetailsId,
                    it.reviewCycleId,
                    it.reviewToId,
                    it.reviewToEmployeeId,
                    it.reviewFromId,
                    it.reviewFromEmployeeId,
                    it.draft,
                    it.published,
                    repository.getReviews(it.reviewDetailsId),
                    it.submittedAt,
                )
            }

    fun update(reviewData: ReviewData) {
        try {
            if (reviewData.published) {
                reviewData.averageRating = calculateAverageRating(reviewData)
            }
            repository.update(reviewData)
            when (reviewData.reviewTypeId) {
                ReviewType.SELF.id -> {
                    if (reviewData.published) {
                        kraRepository.addKraByReviewCycle(
                            reviewCycleId = reviewData.reviewCycleId,
                            organisationId = reviewData.organisationId,
                        )

                        selfReviewSubmittedMail.sendMail(reviewData.reviewToId, reviewData.reviewCycleId, reviewData.organisationId)
                        selfReviewSlackNotifications.selfReviewSubmitted(reviewData.reviewToId, reviewData.organisationId)
                    }
                }

                ReviewType.MANAGER.id -> {
                    if (reviewData.published) {
                        kraRepository.addKraByReviewCycle(
                            reviewCycleId = reviewData.reviewCycleId,
                            organisationId = reviewData.organisationId,
                        )

                        managerReviewSubmittedMail.sendMailToEmployee(
                            reviewData.reviewFromId,
                            reviewData.reviewToId,
                            reviewData.reviewCycleId,
                            reviewData.organisationId,
                        )
                        managerReviewSlackNotifications.managerReviewSubmittedToEmployee(
                            reviewData.reviewFromId,
                            reviewData.reviewToId,
                            reviewData.organisationId,
                        )
                        if (isAllManagerReviewCompleted(reviewData.reviewCycleId, reviewData.reviewFromId) == true) {
                            reviewData.reviewFromId?.let { reviewFrom ->
                                managerReviewSubmittedMail.sendMailToManager(
                                    reviewFrom = reviewFrom,
                                    reviewCycleId = reviewData.reviewCycleId,
                                    organisationId = reviewData.organisationId,
                                )
                                managerReviewSlackNotifications.managerReviewSubmittedToManager(
                                    reviewData.reviewFromId,
                                    reviewData.organisationId,
                                )
                            }
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

    fun fetchInCompleteSelfReviewsEmployees(
        reviewCycleId: Long,
        organisationId: Long,
    ): List<Employee> = repository.fetchInCompleteSelfReviewsEmployees(reviewCycleId, organisationId)

    private fun isAllManagerReviewCompleted(
        reviewCycleId: Long?,
        managerEmployeeId: Long?,
    ): Boolean? = repository.isAllManagerReviewCompleted(reviewCycleId, managerEmployeeId)

    fun fetchInCompleteManagerReviewEmployees(
        organisationId: Long,
        reviewCycleId: Long,
        managerId: Long,
    ): List<Employee> = repository.fetchInCompleteManagerReviewEmployees(organisationId, reviewCycleId, managerId)

    fun fetchInCompleteCheckInWithManagerEmployees(
        reviewCycleId: Long,
        managerId: Long,
    ): List<Employee> = repository.fetchInCompleteCheckInWithManagerEmployees(reviewCycleId = reviewCycleId, managerId = managerId)

    fun getReviewRatingGraphData(
        organisationId: Long,
        reviewToId: Long,
        reviewCycleId: List<Int>,
    ): AvgReviewRatings = repository.getReviewRatingGraphData(organisationId, reviewToId, reviewCycleId)

    fun fetchGoalsWithPastDeadline(): List<GoalBasicDetails> = repository.fetchGoalsWithPastDeadline()

    private fun calculateAverageRating(reviewData: ReviewData): BigDecimal {
        // Group reviews by KRA ID
        val reviewByKRA = reviewData.reviewData.groupBy { it.kraId }

        // Fetch weightage for all KRAs in one go and map them by kraId
        val kraWeightageMap =
            kraRepository
                .getWeightageByIds(reviewByKRA.keys.distinct().toList(), reviewData.organisationId)
                .associateBy { it.id }

        // Calculate the weighted average rating for all KRAs
        val totalWeightedAverage =
            reviewByKRA
                .map { (kraId, reviews) ->
                    val averageRating = reviews.map { it.rating ?: 0 }.average()
                    val weightage = kraWeightageMap[kraId]?.weightage ?: 0
                    averageRating * weightage / 100
                }.sum()

        // Return the final average rating as BigDecimal
        return totalWeightedAverage.toBigDecimal()
    }

    fun updateReview(request: UpdateReviewRequest) {
        val organisationTimeZone = organisationRepository.getOrganisationDetails(request.organisationId).timeZone
        val reviewFromDb =
            repository
                .fetch(
                    listOf(request.reviewTypeId),
                    request.reviewCycleId,
                    request.reviewToId,
                    listOf(request.reviewFromId.toInt()),
                ).firstOrNull()

        if (reviewFromDb?.published == true) {
            throw Exception("Review cannot be updated once it is published.")
        }

        val activeReviewCycle =
            validateAndGetActiveReviewCycle(
                request.organisationId,
                request.reviewCycleId,
                organisationTimeZone,
            )

        fun validateAndSave(
            allowDraftBeforeWindowStarts: Boolean,
            isActive: Boolean,
            isDatePassed: Boolean,
            reviewTypeName: String,
        ) {
            when {
                isDatePassed -> throw Exception("Deadline for $reviewTypeName has passed. Sorry, you’re late!")
                isActive || allowDraftBeforeWindowStarts -> repository.updateReview(request.reviewId, request.review, request.rating)
                else -> throw Exception("$reviewTypeName has not started yet!")
            }
        }

        when (request.reviewTypeId) {
            ReviewType.SELF.id ->
                validateAndSave(
                    allowDraftBeforeWindowStarts = true,
                    isActive = activeReviewCycle.isSelfReviewActive,
                    isDatePassed = activeReviewCycle.isSelfReviewDatePassed,
                    reviewTypeName = "Self Review",
                )

            ReviewType.MANAGER.id ->
                validateAndSave(
                    allowDraftBeforeWindowStarts = true,
                    isActive = activeReviewCycle.isManagerReviewActive,
                    isDatePassed = activeReviewCycle.isManagerReviewDatePassed,
                    reviewTypeName = "Manager Review",
                )

            ReviewType.CHECK_IN.id ->
                validateAndSave(
                    allowDraftBeforeWindowStarts = false,
                    isActive = activeReviewCycle.isCheckInWithManagerActive,
                    isDatePassed = activeReviewCycle.isCheckInWithManagerDatePassed,
                    reviewTypeName = "Check-in with Manager",
                )

            else -> throw Exception(
                "Invalid review type ID '${request.reviewTypeId}'. Only Self(1), Manager(2), and Check-in(3) are allowed.",
            )
        }
    }

    fun getReviewById(reviewId: Long): Review = repository.getReviewById(reviewId)

    fun getWeightedReviewScore(
        reviewCycleId: Long,
        reviewDetailsId: Long,
    ): WeightedScore {
        val reviewsGroupedByKra = repository.getReviews(reviewDetailsId).groupBy { it.kraId }
        val kraList = kraRepository.getKraByReviewCycle(reviewCycleId).associateBy { it.id }

        val kraWeightedScores =
            reviewsGroupedByKra.mapNotNull { (kraId, reviews) ->
                kraList[kraId]?.let { kra ->
                    val averageRating = reviews.mapNotNull { it.rating }.average()
                    KRAWeightedScore(
                        kraId = kra.id,
                        kraName = kra.name,
                        kraWeightage = kra.weightage,
                        weightedRating = (averageRating * kra.weightage / 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
                    )
                }
            }

        // Determine the final score
        val finalScore =
            repository.getAverageRating(reviewCycleId, reviewDetailsId)?.takeIf {
                it != "-1.00".toBigDecimal()
            } ?: kraWeightedScores.sumOf { it.weightedRating }

        return WeightedScore(
            finalScore = finalScore,
            kraWeightedScores = kraWeightedScores,
        )
    }

    private fun validateAndGetActiveReviewCycle(
        organisationId: Long,
        reviewCycleId: Long,
        organisationTimeZone: String,
    ): ReviewCycle {
        val activeReviewCycle =
            reviewCycleRepository.fetchActiveReviewCycle(organisationId)
                ?: throw ReviewCycleException("You Can Not Submit Review since Review Cycle is not active!")
        val reviewCycleWithActiveFlags = activeReviewCycle.withActiveFlags(organisationTimeZone)

        if (reviewCycleId != reviewCycleWithActiveFlags.reviewCycleId || !reviewCycleWithActiveFlags.isReviewCycleActive) {
            throw ReviewCycleException("You Can Not Submit Review since this Review Cycle is not active!")
        }

        return reviewCycleWithActiveFlags
    }

    private fun validateAndCreateSelfReview(
        reviewData: ReviewData,
        reviewCycle: ReviewCycle,
    ) = validateAndCreateReview(
        reviewData,
        isActive = reviewCycle.isSelfReviewActive,
        isDatePassed = reviewCycle.isSelfReviewDatePassed,
        reviewTypeName = "Self Review",
    ) { rd ->
        selfReviewSubmittedMail.sendMail(
            rd.reviewToId,
            rd.reviewCycleId,
            rd.organisationId,
        )
        selfReviewSlackNotifications.selfReviewSubmitted(
            rd.reviewToId,
            rd.organisationId,
        )
    }

    private fun validateAndCreateManagerReview(
        reviewData: ReviewData,
        reviewCycle: ReviewCycle,
    ) = validateAndCreateReview(
        reviewData,
        isActive = reviewCycle.isManagerReviewActive,
        isDatePassed = reviewCycle.isManagerReviewDatePassed,
        reviewTypeName = "Manager Review",
    ) { rd ->
        managerReviewSubmittedMail.sendMailToEmployee(
            reviewFrom = rd.reviewFromId,
            reviewTo = rd.reviewToId,
            reviewCycleId = rd.reviewCycleId,
            organisationId = rd.organisationId,
        )
        managerReviewSlackNotifications.managerReviewSubmittedToEmployee(
            reviewFrom = rd.reviewFromId,
            reviewTo = rd.reviewToId,
            organisationId = rd.organisationId,
        )

        if (isAllManagerReviewCompleted(rd.reviewCycleId, rd.reviewFromId) == true) {
            rd.reviewFromId?.let {
                managerReviewSubmittedMail.sendMailToManager(
                    reviewFrom = it,
                    reviewCycleId = rd.reviewCycleId,
                    organisationId = rd.organisationId,
                )
                managerReviewSlackNotifications.managerReviewSubmittedToManager(
                    reviewFrom = it,
                    organisationId = rd.organisationId,
                )
            }
        }
    }

    private fun validateAndCreateReview(
        reviewData: ReviewData,
        isActive: Boolean,
        isDatePassed: Boolean,
        reviewTypeName: String,
        onSubmitted: (ReviewData) -> Unit,
    ) {
        when {
            // Before window starts
            !isActive && !isDatePassed -> {
                when {
                    reviewData.draft && !reviewData.published -> repository.create(reviewData)
                    reviewData.published -> throw ReviewException("You are not allowed to submit before $reviewTypeName window starts!")
                    else -> throw ReviewException("Only draft is allowed before $reviewTypeName window starts!")
                }
            }

            // During window
            isActive -> {
                repository.create(reviewData)

                if (!reviewData.draft && reviewData.published) {
                    kraRepository.addKraByReviewCycle(
                        reviewCycleId = reviewData.reviewCycleId,
                        organisationId = reviewData.organisationId,
                    )
                    onSubmitted(reviewData)
                }
            }

            // After window
            else -> throw ReviewException("Deadline for $reviewTypeName has passed. Sorry, you’re late!")
        }
    }

    private fun validateAndCreateCheckInReview(
        reviewData: ReviewData,
        reviewCycle: ReviewCycle,
    ) = when {
        reviewCycle.isCheckInWithManagerActive -> repository.create(reviewData)
        else -> throw CheckInReviewException("Deadline for Check-in with Manager has passed. Sorry, you’re late!")
    }
}
