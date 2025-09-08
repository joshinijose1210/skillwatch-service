package review

import io.kotest.core.spec.style.StringSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.emails.CheckInWithManagerCompletedMail
import scalereal.core.goals.GoalRepository
import scalereal.core.kra.KRARepository
import scalereal.core.models.domain.CheckInWithManagerRequest
import scalereal.core.models.domain.Goal
import scalereal.core.models.domain.GoalParams
import scalereal.core.models.domain.KRAWeightage
import scalereal.core.models.domain.Review
import scalereal.core.review.CheckInWithManagerRepository
import scalereal.core.review.CheckInWithManagerService
import scalereal.core.review.SelfReviewRepository
import scalereal.core.slack.CheckInSlackNotifications
import java.sql.Date
import java.sql.Timestamp

class CheckInWithManagerServiceImplTest : StringSpec() {
    private val checkInWithManagerRepository = mockk<CheckInWithManagerRepository>()
    private val selfReviewRepository = mockk<SelfReviewRepository>()
    private val checkInWithManagerCompletedMail = mockk<CheckInWithManagerCompletedMail>()
    private val checkInSlackNotifications = mockk<CheckInSlackNotifications>()
    private val kraRepository = mockk<KRARepository>()
    private val goalRepository = mockk<GoalRepository>()
    private val checkInWithManagerService =
        CheckInWithManagerService(
            checkInWithManagerRepository,
            selfReviewRepository,
            checkInWithManagerCompletedMail,
            checkInSlackNotifications,
            kraRepository,
            goalRepository,
        )

    init {
        "should be able to submit self-review" {
            val checkInWithManagerRequest =
                CheckInWithManagerRequest(
                    organisationId = 1,
                    reviewTypeId = 1,
                    reviewDetailsId = 1,
                    reviewCycleId = 1,
                    reviewToId = 1,
                    reviewFromId = 1,
                    draft = true,
                    published = false,
                    firstManagerId = null,
                    secondManagerId = null,
                    reviewData =
                        listOf(
                            Review(
                                reviewId = 1,
                                id = 1,
                                kraId = 1,
                                kraName = "Knowledge & Skills Growth",
                                kpiTitle = null,
                                kpiDescription = null,
                                review = "It is a test review",
                                rating = 5,
                            ),
                        ),
                    goals =
                        listOf(
                            GoalParams(
                                id = 1,
                                goalId = 1,
                                typeId = 1,
                                assignedTo = 1,
                                createdBy = 2,
                                description = "1st goal",
                                targetDate = Date(20 - 2 - 2023),
                            ),
                            GoalParams(
                                id = 2,
                                goalId = 2,
                                typeId = 1,
                                assignedTo = 1,
                                createdBy = 2,
                                description = "2nd goal",
                                targetDate = Date(20 - 2 - 2023),
                            ),
                        ),
                    averageRating = (-1.0).toBigDecimal(),
                )
            every { checkInWithManagerRepository.createCheckInWithManager(checkInWithManagerRequest) } returns Unit
            every { goalRepository.getMaxGoalId(any()) } returns 0
            checkInWithManagerService.createCheckInWithManager(checkInWithManagerRequest)
            verify(exactly = 1) { checkInWithManagerRepository.createCheckInWithManager(checkInWithManagerRequest) }
        }

        "send mail to employee when check in with manager is completed" {
            val checkInWithManagerRequest =
                CheckInWithManagerRequest(
                    organisationId = 1,
                    reviewTypeId = 3,
                    reviewDetailsId = 1,
                    reviewCycleId = 1,
                    reviewToId = 1,
                    reviewFromId = 2,
                    draft = false,
                    published = true,
                    firstManagerId = 2,
                    secondManagerId = null,
                    reviewData =
                        listOf(
                            Review(
                                reviewId = 1,
                                id = 1,
                                kraId = 1,
                                kraName = "Knowledge & Skills Growth",
                                kpiTitle = null,
                                kpiDescription = null,
                                review = "It is a test review",
                                rating = 5,
                            ),
                        ),
                    goals =
                        listOf(
                            GoalParams(
                                id = 1,
                                goalId = 1,
                                typeId = 1,
                                assignedTo = 1,
                                createdBy = 2,
                                description = "1st goal",
                                targetDate = Date(20 - 2 - 2023),
                            ),
                            GoalParams(
                                id = 1,
                                goalId = 2,
                                typeId = 1,
                                assignedTo = 1,
                                createdBy = 2,
                                description = "2nd goal",
                                targetDate = Date(20 - 2 - 2023),
                            ),
                        ),
                    averageRating = (-1.0).toBigDecimal(),
                )

            every {
                kraRepository.getWeightageByIds(any(), any())
            } returns listOf(KRAWeightage(id = 1, weightage = 100))
            every { checkInWithManagerRepository.createCheckInWithManager(checkInWithManagerRequest) } just Runs
            every { checkInWithManagerCompletedMail.sendMailToEmployee(any(), any(), checkInWithManagerRequest.organisationId) } just Runs
            every {
                checkInSlackNotifications.checkInSubmittedToEmployee(
                    checkInWithManagerRequest.reviewToId,
                    checkInWithManagerRequest.organisationId,
                )
            } just Runs
            every { checkInWithManagerRepository.isAllCheckInWithManagerCompleted(any(), any()) } returns false
            every { goalRepository.getMaxGoalId(any()) } returns 0
            checkInWithManagerService.createCheckInWithManager(checkInWithManagerRequest)
            verify(exactly = 1) {
                checkInWithManagerCompletedMail.sendMailToEmployee(
                    checkInWithManagerRequest.reviewToId,
                    checkInWithManagerRequest.reviewCycleId,
                    checkInWithManagerRequest.organisationId,
                )
                checkInSlackNotifications.checkInSubmittedToEmployee(
                    checkInWithManagerRequest.reviewToId,
                    checkInWithManagerRequest.organisationId,
                )
            }
            verify(exactly = 0) {
                checkInWithManagerCompletedMail.sendMailToManager(any(), any(), checkInWithManagerRequest.organisationId)
                checkInSlackNotifications.checkInSubmittedToManager(
                    checkInWithManagerRequest.reviewFromId,
                    checkInWithManagerRequest.organisationId,
                )
            }
        }

        "send mail to manager when check in with manager is completed" {
            val checkInWithManagerRequest =
                CheckInWithManagerRequest(
                    organisationId = 1,
                    reviewTypeId = 3,
                    reviewDetailsId = 1,
                    reviewCycleId = 1,
                    reviewToId = 1,
                    reviewFromId = 2,
                    draft = false,
                    published = true,
                    firstManagerId = 2,
                    secondManagerId = null,
                    reviewData =
                        listOf(
                            Review(
                                reviewId = 1,
                                id = 1,
                                kraId = 1,
                                kraName = "Knowledge & Skills Growth",
                                kpiTitle = null,
                                kpiDescription = null,
                                review = "It is a test review",
                                rating = 5,
                            ),
                        ),
                    goals =
                        listOf(
                            GoalParams(
                                id = 1,
                                goalId = 1,
                                typeId = 1,
                                description = "1st goal",
                                targetDate = Date(20 - 2 - 2023),
                                assignedTo = 1,
                                createdBy = 2,
                            ),
                            GoalParams(
                                id = 1,
                                goalId = 2,
                                typeId = 1,
                                description = "2nd goal",
                                targetDate = Date(20 - 2 - 2023),
                                assignedTo = 1,
                                createdBy = 2,
                            ),
                        ),
                    averageRating = (-1.0).toBigDecimal(),
                )

            every {
                kraRepository.getWeightageByIds(any(), any())
            } returns listOf(KRAWeightage(id = 1, weightage = 100))
            every { checkInWithManagerRepository.createCheckInWithManager(checkInWithManagerRequest) } just Runs
            every { checkInWithManagerCompletedMail.sendMailToEmployee(any(), any(), checkInWithManagerRequest.organisationId) } just Runs
            every {
                checkInSlackNotifications.checkInSubmittedToEmployee(
                    checkInWithManagerRequest.reviewToId,
                    checkInWithManagerRequest.organisationId,
                )
            } just Runs
            every { checkInWithManagerCompletedMail.sendMailToManager(any(), any(), checkInWithManagerRequest.organisationId) } just Runs
            every {
                checkInSlackNotifications.checkInSubmittedToManager(
                    checkInWithManagerRequest.reviewFromId,
                    checkInWithManagerRequest.organisationId,
                )
            } just Runs
            every { checkInWithManagerRepository.isAllCheckInWithManagerCompleted(any(), any()) } returns true
            every { goalRepository.getMaxGoalId(any()) } returns 0
            checkInWithManagerService.createCheckInWithManager(checkInWithManagerRequest)

            verify(exactly = 1) {
                checkInWithManagerCompletedMail.sendMailToManager(
                    checkInWithManagerRequest.reviewFromId,
                    checkInWithManagerRequest.reviewCycleId,
                    checkInWithManagerRequest.organisationId,
                )
                checkInSlackNotifications.checkInSubmittedToManager(
                    checkInWithManagerRequest.reviewFromId,
                    checkInWithManagerRequest.organisationId,
                )
            }
        }

        "should update goal" {
            Goal(
                id = 1,
                goalId = "G1",
                typeId = 1,
                description = "2nd goal",
                createdAt = Timestamp(20 - 1 - 2023),
                targetDate = Date(20 - 2 - 2023),
                progressId = 1,
                progressName = "Pending",
                createdBy = 1,
                assignedTo = 2,
            )
            every {
                checkInWithManagerRepository.updateGoals(
                    goals =
                        listOf(
                            Goal(
                                id = 1,
                                goalId = "G1",
                                typeId = 1,
                                description = "2nd goal",
                                createdAt = Timestamp(20 - 1 - 2023),
                                targetDate = Date(20 - 2 - 2023),
                                progressId = 1,
                                progressName = "Pending",
                                createdBy = 1,
                                assignedTo = 2,
                            ),
                        ),
                )
            } returns Unit
            checkInWithManagerService.updateGoals(
                goals =
                    listOf(
                        Goal(
                            id = 1,
                            goalId = "G1",
                            typeId = 1,
                            description = "2nd goal",
                            createdAt = Timestamp(20 - 1 - 2023),
                            targetDate = Date(20 - 2 - 2023),
                            progressId = 1,
                            progressName = "Pending",
                            createdBy = 1,
                            assignedTo = 2,
                        ),
                    ),
            )

            verify(exactly = 1) {
                checkInWithManagerService.updateGoals(
                    goals =
                        listOf(
                            Goal(
                                id = 1,
                                goalId = "G1",
                                typeId = 1,
                                description = "2nd goal",
                                createdAt = Timestamp(20 - 1 - 2023),
                                targetDate = Date(20 - 2 - 2023),
                                progressId = 1,
                                progressName = "Pending",
                                createdBy = 1,
                                assignedTo = 2,
                            ),
                        ),
                )
            }
        }
    }
}
