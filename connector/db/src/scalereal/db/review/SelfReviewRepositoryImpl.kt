package scalereal.db.review

import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import review.AddReviewDetailsParams
import review.AddReviewDetailsQuery
import review.AddReviewManagerMappingCommand
import review.AddReviewManagerMappingParams
import review.AddSelfReviewCommand
import review.AddSelfReviewParams
import review.GetAllSelfReviewParams
import review.GetAllSelfReviewQuery
import review.GetAllSelfReviewResult
import review.GetAverageRatingParams
import review.GetAverageRatingQuery
import review.GetAverageReviewRatingsParams
import review.GetAverageReviewRatingsQuery
import review.GetEmployeesWithInCompletedManagerReviewsParams
import review.GetEmployeesWithInCompletedManagerReviewsQuery
import review.GetEmployeesWithInCompletedSelfReviewsParams
import review.GetEmployeesWithInCompletedSelfReviewsQuery
import review.GetEmployeesWithIncompleteCheckInWithManagerParams
import review.GetEmployeesWithIncompleteCheckInWithManagerQuery
import review.GetGoalsWithPastDeadlineParams
import review.GetGoalsWithPastDeadlineQuery
import review.GetReviewDetailsParams
import review.GetReviewDetailsQuery
import review.GetReviewParams
import review.GetReviewQuery
import review.IsManagerReviewCompletedParams
import review.IsManagerReviewCompletedQuery
import review.UpdateReviewCommand
import review.UpdateReviewDetailsParams
import review.UpdateReviewDetailsQuery
import review.UpdateReviewParams
import review.UpdateSelfReviewCommand
import review.UpdateSelfReviewParams
import scalereal.core.dto.ReviewType
import scalereal.core.models.domain.AvgReviewRatings
import scalereal.core.models.domain.Employee
import scalereal.core.models.domain.GoalBasicDetails
import scalereal.core.models.domain.Review
import scalereal.core.models.domain.ReviewData
import scalereal.core.models.domain.ReviewDetails
import scalereal.core.review.SelfReviewRepository
import java.math.BigDecimal
import javax.sql.DataSource

@Singleton
class SelfReviewRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : SelfReviewRepository {
    override fun create(reviewData: ReviewData): Unit =
        dataSource.connection.use { connection ->
            AddReviewDetailsQuery()
                .query(
                    connection,
                    AddReviewDetailsParams(
                        reviewData.reviewCycleId,
                        reviewData.reviewToId,
                        reviewData.reviewFromId,
                        reviewData.draft,
                        reviewData.published,
                        reviewData.reviewTypeId,
                        reviewData.averageRating,
                    ),
                ).map {
                    insertReviews(
                        it.id,
                        reviewData.reviewData,
                    )
                    if (reviewData.reviewTypeId == ReviewType.CHECK_IN.id) {
                        insertReviewManagerMapping(
                            it.id,
                            reviewData.firstManagerId,
                            reviewData.secondManagerId,
                        )
                    }
                }.first()
        }

    private fun insertReviewManagerMapping(
        reviewDetailsId: Long?,
        firstManagerId: Long?,
        secondManagerId: Long?,
    ) {
        dataSource.connection.use { connection ->
            AddReviewManagerMappingCommand()
                .command(
                    connection,
                    AddReviewManagerMappingParams(
                        reviewDetailsId = reviewDetailsId,
                        firstManagerId = firstManagerId,
                        secondManagerId = secondManagerId,
                    ),
                )
        }
    }

    override fun insertReviews(
        reviewDetailsId: Long?,
        review: List<Review>,
    ) {
        review.map { reviewData ->
            dataSource.connection.use { connection ->
                AddSelfReviewCommand()
                    .command(
                        connection,
                        AddSelfReviewParams(
                            reviewDetailsId = reviewDetailsId,
                            kpiId = reviewData.id,
                            review = reviewData.review,
                            rating = reviewData.rating,
                        ),
                    )
            }
        }
    }

    override fun fetch(
        reviewTypeId: List<Int>,
        reviewCycleId: Long,
        reviewToId: Long,
        reviewFromId: List<Int>,
    ): List<ReviewDetails> =
        dataSource.connection.use { connection ->
            GetReviewDetailsQuery()
                .query(
                    connection,
                    GetReviewDetailsParams(
                        reviewTypeId = reviewTypeId.toTypedArray(),
                        reviewCycleId = reviewCycleId,
                        reviewToId = reviewToId,
                        reviewFromId = reviewFromId.toTypedArray(),
                    ),
                ).map {
                    ReviewDetails(
                        it.reviewTypeId,
                        it.reviewDetailsId,
                        it.reviewCycleId,
                        it.reviewToId,
                        it.reviewToEmployeeId,
                        it.reviewFromId,
                        it.reviewFromEmployeeId,
                        it.draft ?: false,
                        it.published ?: false,
                        it.updatedAt,
                    )
                }
        }

    override fun getReviews(reviewDetailsId: Long?): List<Review> =
        dataSource.connection.use { connection ->
            GetAllSelfReviewQuery()
                .query(connection, GetAllSelfReviewParams(reviewDetailsId = reviewDetailsId))
                .map { it.toReview() }
        }

    override fun update(reviewData: ReviewData): Unit =
        dataSource.connection.use { connection ->
            UpdateReviewDetailsQuery()
                .query(
                    connection,
                    UpdateReviewDetailsParams(
                        reviewTypeId = reviewData.reviewTypeId,
                        reviewDetailsId = reviewData.reviewDetailsId,
                        reviewCycleId = reviewData.reviewCycleId,
                        reviewToId = reviewData.reviewToId,
                        reviewFromId = reviewData.reviewFromId,
                        draft = reviewData.draft,
                        published = reviewData.published,
                        averageRating = reviewData.averageRating,
                    ),
                ).map {
                    updateReviews(
                        it.id,
                        reviewData.reviewData,
                    )
                }
        }

    override fun updateReviews(
        reviewDetailsId: Long?,
        reviewData: List<Review>,
    ) {
        reviewData.map { review ->
            dataSource.connection.use { connection ->
                UpdateSelfReviewCommand()
                    .command(
                        connection,
                        UpdateSelfReviewParams(
                            reviewDetailsId = reviewDetailsId,
                            kpiId = review.id,
                            review = review.review,
                            rating = review.rating,
                        ),
                    )
            }
        }
    }

    override fun fetchInCompleteSelfReviewsEmployees(
        reviewCycleId: Long,
        organisationId: Long,
    ): List<Employee> =
        dataSource.connection.use { connection ->
            GetEmployeesWithInCompletedSelfReviewsQuery()
                .query(connection, GetEmployeesWithInCompletedSelfReviewsParams(reviewCycleId, organisationId))
                .map {
                    Employee(
                        organisationId = it.organisationId,
                        id = it.id,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        employeeId = it.empId,
                        emailId = it.emailId,
                        contactNo = it.contactNo,
                        departmentName = null,
                        designationName = null,
                        roleName = null,
                        teamName = null,
                        onboardingFlow = null,
                        modulePermission = listOf(),
                    )
                }
        }

    override fun isAllManagerReviewCompleted(
        reviewCycleId: Long?,
        managerEmployeeId: Long?,
    ): Boolean? =
        dataSource.connection.use { connection ->
            IsManagerReviewCompletedQuery()
                .query(connection, IsManagerReviewCompletedParams(reviewCycleId, managerEmployeeId))[0]
                .result
        }

    override fun fetchInCompleteManagerReviewEmployees(
        organisationId: Long,
        reviewCycleId: Long,
        managerId: Long,
    ): List<Employee> =
        dataSource.connection.use { connection ->
            GetEmployeesWithInCompletedManagerReviewsQuery()
                .query(
                    connection,
                    GetEmployeesWithInCompletedManagerReviewsParams(
                        organisationId = organisationId,
                        reviewCycleId = reviewCycleId,
                        managerId = managerId,
                    ),
                ).map {
                    Employee(
                        organisationId = it.organisationId,
                        id = it.id,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        employeeId = it.empId,
                        emailId = it.emailId,
                        contactNo = it.contactNo,
                        departmentName = null,
                        designationName = null,
                        roleName = null,
                        teamName = null,
                        onboardingFlow = null,
                        modulePermission = listOf(),
                    )
                }
        }

    override fun fetchInCompleteCheckInWithManagerEmployees(
        reviewCycleId: Long,
        managerId: Long,
    ): List<Employee> =
        dataSource.connection.use { connection ->
            GetEmployeesWithIncompleteCheckInWithManagerQuery()
                .query(connection, GetEmployeesWithIncompleteCheckInWithManagerParams(reviewCycleId, managerId))
                .map {
                    Employee(
                        organisationId = it.organisationId,
                        id = it.id,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        employeeId = it.empId,
                        emailId = it.emailId,
                        contactNo = it.contactNo,
                        departmentName = null,
                        designationName = null,
                        roleName = null,
                        teamName = null,
                        onboardingFlow = null,
                        modulePermission = listOf(),
                    )
                }
        }

    override fun getReviewRatingGraphData(
        organisationId: Long,
        reviewToId: Long,
        reviewCycleId: List<Int>,
    ): AvgReviewRatings =
        dataSource.connection.use { connection ->
            GetAverageReviewRatingsQuery()
                .query(connection, GetAverageReviewRatingsParams(organisationId, reviewToId, reviewCycleId.toTypedArray()))
                .map {
                    AvgReviewRatings(
                        avgSelfReviewRating = it.selfAverageRating ?: (0.00).toBigDecimal(),
                        avgFirstManagerRating = it.firstManagerAverageRating ?: (0.00).toBigDecimal(),
                        avgSecondManagerReviewRating = it.secondManagerAverageRating ?: (0.00).toBigDecimal(),
                        avgCheckInReviewRating = it.checkInAverageRating ?: (0.00).toBigDecimal(),
                    )
                }.first()
        }

    override fun fetchGoalsWithPastDeadline(): List<GoalBasicDetails> =
        dataSource.connection.use { connection ->
            GetGoalsWithPastDeadlineQuery()
                .query(connection, GetGoalsWithPastDeadlineParams())
                .map {
                    GoalBasicDetails(
                        id = it.id,
                        goalId = it.goalId,
                        assignedTo = it.assignedTo,
                        createdBy = it.createdBy,
                    )
                }
        }

    override fun updateReview(
        reviewId: Long,
        review: String?,
        rating: Int?,
    ) {
        dataSource.connection.use { connection ->
            UpdateReviewCommand()
                .command(
                    connection,
                    UpdateReviewParams(
                        review = review,
                        rating = rating,
                        reviewId = reviewId,
                    ),
                )
        }
    }

    override fun getReviewById(reviewId: Long): Review =
        dataSource.connection.use { connection ->
            GetReviewQuery().query(connection, GetReviewParams(reviewId = reviewId)).first().let {
                Review(
                    reviewId = it.reviewId,
                    id = it.kpiId,
                    kpiTitle = it.title,
                    kpiDescription = it.description,
                    kraId = it.kraId,
                    kraName = it.kraName,
                    review = it.review,
                    rating = it.rating,
                )
            }
        }

    override fun getAverageRating(
        reviewCycleId: Long,
        reviewDetailsId: Long,
    ): BigDecimal? {
        dataSource.connection.use { connection ->
            return GetAverageRatingQuery()
                .query(
                    connection,
                    GetAverageRatingParams(
                        reviewCycleId = reviewCycleId,
                        reviewDetailsId = reviewDetailsId,
                    ),
                ).firstOrNull()
                ?.averageRating
        }
    }
}

private fun GetAllSelfReviewResult.toReview() =
    Review(
        reviewId = reviewId,
        id = kpiId,
        kpiTitle = title,
        kraId = kraId,
        kraName = kraName,
        kpiDescription = description,
        review = review,
        rating = rating,
    )
