package review

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.designations.DesignationRepository
import scalereal.core.dto.ReviewType
import scalereal.core.dto.UpdateReviewRequest
import scalereal.core.emails.ManagerReviewSubmittedMail
import scalereal.core.emails.SelfReviewSubmittedMail
import scalereal.core.kra.KRARepository
import scalereal.core.models.domain.GetAllKRAResponse
import scalereal.core.models.domain.KRAWeightage
import scalereal.core.models.domain.KRAWeightedScore
import scalereal.core.models.domain.OrganisationDetails
import scalereal.core.models.domain.Review
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.models.domain.ReviewData
import scalereal.core.models.domain.ReviewDetails
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.review.SelfReviewRepository
import scalereal.core.review.SelfReviewService
import scalereal.core.reviewCycle.ReviewCycleRepository
import scalereal.core.slack.ManagerReviewSlackNotifications
import scalereal.core.slack.SelfReviewSlackNotifications
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class SelfReviewServiceImplTest : StringSpec() {
    private val selfReviewRepository = mockk<SelfReviewRepository>()
    private val reviewCycleRepository = mockk<ReviewCycleRepository>()
    private val selfReviewSubmittedMail = mockk<SelfReviewSubmittedMail>()
    private val managerReviewSubmittedMail = mockk<ManagerReviewSubmittedMail>()
    private val selfReviewSlackNotifications = mockk<SelfReviewSlackNotifications>()
    private val managerReviewSlackNotifications = mockk<ManagerReviewSlackNotifications>()
    private val kraRepository = mockk<KRARepository>()
    private val organisationRepository = mockk<OrganisationRepository>()
    private val designationRepository = mockk<DesignationRepository>()
    private val selfReviewService =
        SelfReviewService(
            selfReviewRepository,
            reviewCycleRepository,
            selfReviewSubmittedMail,
            managerReviewSubmittedMail,
            selfReviewSlackNotifications,
            managerReviewSlackNotifications,
            kraRepository,
            organisationRepository,
        )

    init {
        val organisationDetails =
            mockk<OrganisationDetails> {
                every { id } returns 1
                every { timeZone } returns "Asia/Kolkata"
            }
        val organisationCurrentDate = LocalDate.now(ZoneId.of(organisationDetails.timeZone))
        val organisationCurrentTime = LocalDateTime.now(ZoneId.of(organisationDetails.timeZone))

        "should throw exception while submitting self-review" {
            val reviewData =
                ReviewData(
                    organisationId = 1,
                    reviewTypeId = 1,
                    reviewDetailsId = 1,
                    reviewCycleId = 1,
                    reviewToId = 1,
                    reviewFromId = 1,
                    firstManagerId = null,
                    secondManagerId = null,
                    draft = true,
                    published = false,
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
                    averageRating = (-1.0).toBigDecimal(),
                )

            val activeReviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf(organisationCurrentDate.minusDays(5)),
                    endDate = Date.valueOf(organisationCurrentDate.plusDays(1)),
                    publish = true,
                    selfReviewStartDate = Date.valueOf(organisationCurrentDate.minusDays(4)),
                    selfReviewEndDate = Date.valueOf(organisationCurrentDate.minusDays(3)),
                    managerReviewStartDate = Date.valueOf(organisationCurrentDate.minusDays(4)),
                    managerReviewEndDate = Date.valueOf(organisationCurrentDate.minusDays(3)),
                    checkInWithManagerStartDate = Date.valueOf(organisationCurrentDate.minusDays(1)),
                    checkInWithManagerEndDate = Date.valueOf(organisationCurrentDate),
                    lastModified = null,
                    isSelfReviewDatePassed = true,
                    isManagerReviewDatePassed = true,
                    isCheckInWithManagerDatePassed = false,
                ).withActiveFlags(organisationDetails.timeZone)

            every {
                reviewCycleRepository.fetchActiveReviewCycle(any())
            } returns activeReviewCycle
            every { organisationRepository.getOrganisationDetails(any()) } returns organisationDetails
            val exception = shouldThrow<Exception> { selfReviewService.create(reviewData) }
            exception.message.shouldBe("Deadline for Self Review has passed. Sorry, you’re late!")
        }

        "should fetch self review" {
            val reviewDetails =
                listOf(
                    ReviewDetails(
                        reviewTypeId = 1,
                        reviewDetailsId = 1,
                        reviewCycleId = 1,
                        reviewToId = 1,
                        reviewToEmployeeId = "SR0043",
                        reviewFromId = 1,
                        reviewFromEmployeeId = "SR0043",
                        draft = true,
                        published = false,
                        submittedAt = Timestamp.valueOf("2023-2-2 11:9:48.834129"),
                    ),
                )
            val review =
                listOf(
                    Review(
                        reviewId = 1,
                        id = 1,
                        kraId = 1,
                        kraName = "Knowledge & Skills Growth",
                        kpiTitle = "Code Quality",
                        kpiDescription = "Description of code quality",
                        review = "It is a test review",
                        rating = 5,
                    ),
                )
            every {
                selfReviewRepository.fetch(
                    reviewTypeId = listOf(1),
                    reviewCycleId = 1,
                    reviewToId = 1,
                    reviewFromId = listOf(1),
                )
            } returns reviewDetails
            every { selfReviewRepository.getReviews(reviewDetailsId = 1) } returns review
        }

        "should update self-review" {
            val reviewData =
                ReviewData(
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
                                kpiTitle = "Code Quality",
                                kpiDescription = "Description of code quality",
                                review = "It is a test review",
                                rating = 5,
                            ),
                        ),
                    averageRating = (-1.0).toBigDecimal(),
                )
            every {
                selfReviewRepository.update(
                    reviewData,
                )
            } returns Unit

            every {

                selfReviewRepository.updateReviews(
                    reviewDetailsId = reviewData.reviewDetailsId,
                    reviewData = reviewData.reviewData,
                )
            } returns Unit

            every {
                selfReviewSlackNotifications.selfReviewSubmitted(
                    reviewData.reviewToId,
                    reviewData.organisationId,
                )
            } just Runs

            selfReviewService.update(reviewData = reviewData)
            verify(exactly = 1) {
                selfReviewRepository.update(
                    reviewData,
                )
            }
        }

        "should throw exception while submitting Summary-review" {
            val reviewData =
                ReviewData(
                    organisationId = 1,
                    reviewTypeId = 3,
                    reviewDetailsId = 2,
                    reviewCycleId = 1,
                    reviewToId = 2,
                    reviewFromId = 1,
                    draft = false,
                    published = true,
                    firstManagerId = 1,
                    secondManagerId = 3,
                    reviewData =
                        listOf(
                            Review(
                                reviewId = 1,
                                id = 1,
                                kraId = 1,
                                kraName = "Knowledge & Skills Growth",
                                kpiTitle = null,
                                kpiDescription = null,
                                review = "It is a test summary review",
                                rating = 5,
                            ),
                        ),
                    averageRating = (-1.0).toBigDecimal(),
                )
            val reviewCycleData =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf(organisationCurrentDate.minusDays(5)),
                    endDate = Date.valueOf(organisationCurrentDate.plusDays(1)),
                    publish = true,
                    selfReviewStartDate = Date.valueOf(organisationCurrentDate.minusDays(4)),
                    selfReviewEndDate = Date.valueOf(organisationCurrentDate.minusDays(3)),
                    managerReviewStartDate = Date.valueOf(organisationCurrentDate.minusDays(4)),
                    managerReviewEndDate = Date.valueOf(organisationCurrentDate.minusDays(3)),
                    checkInWithManagerStartDate = Date.valueOf(organisationCurrentDate.minusDays(1)),
                    checkInWithManagerEndDate = Date.valueOf(organisationCurrentDate.minusDays(1)),
                    lastModified = null,
                    isSelfReviewDatePassed = true,
                    isManagerReviewDatePassed = true,
                    isCheckInWithManagerDatePassed = true,
                ).withActiveFlags(organisationDetails.timeZone)
            every {
                reviewCycleRepository.fetchActiveReviewCycle(reviewData.organisationId)
            } returns reviewCycleData
            every {
                reviewCycleRepository.fetchReviewCycle(
                    checkNotNull(reviewData.reviewCycleId),
                )
            } returns reviewCycleData
            every {
                kraRepository.getWeightageByIds(any(), any())
            } returns listOf(KRAWeightage(id = 1, weightage = 100))
            val exception = shouldThrow<Exception> { selfReviewService.create(reviewData) }
            exception.message.shouldBe("Deadline for Check-in with Manager has passed. Sorry, you’re late!")
        }

        "should fetch Summary review" {
            val reviewDetails =
                listOf(
                    ReviewDetails(
                        reviewTypeId = 3,
                        reviewDetailsId = 2,
                        reviewCycleId = 1,
                        reviewToId = 2,
                        reviewToEmployeeId = "SR0051",
                        reviewFromId = 1,
                        reviewFromEmployeeId = "SR0043",
                        draft = false,
                        published = true,
                        submittedAt = Timestamp.valueOf("2023-2-2 11:9:48.834129"),
                    ),
                )
            val review =
                listOf(
                    Review(
                        reviewId = 1,
                        id = 1,
                        kraId = 1,
                        kraName = "Knowledge & Skills Growth",
                        kpiTitle = "Code Quality",
                        kpiDescription = "Description of code quality",
                        review = "It is a test summary review",
                        rating = 5,
                    ),
                )
            every {
                selfReviewRepository.fetch(
                    reviewTypeId = listOf(3),
                    reviewCycleId = 1,
                    reviewToId = 2,
                    reviewFromId = listOf(1),
                )
            } returns reviewDetails
            every { selfReviewRepository.getReviews(reviewDetailsId = 1) } returns review
        }

        "should update Summary-review" {
            val reviewData =
                ReviewData(
                    organisationId = 1,
                    reviewTypeId = 3,
                    reviewDetailsId = 2,
                    reviewCycleId = 1,
                    reviewToId = 2,
                    reviewFromId = 1,
                    draft = false,
                    published = true,
                    firstManagerId = 1,
                    secondManagerId = null,
                    reviewData =
                        listOf(
                            Review(
                                reviewId = 1,
                                id = 1,
                                kraId = 1,
                                kraName = "Knowledge & Skills Growth",
                                kpiTitle = "Code Quality",
                                kpiDescription = "Description of code quality",
                                review = "It is a test summary review",
                                rating = 5,
                            ),
                        ),
                    averageRating = (-1.0).toBigDecimal(),
                )
            every {
                selfReviewRepository.update(
                    reviewData,
                )
            } returns Unit

            every {

                selfReviewRepository.updateReviews(
                    reviewDetailsId = reviewData.reviewDetailsId,
                    reviewData = reviewData.reviewData,
                )
            } returns Unit

            selfReviewService.update(reviewData = reviewData)
            verify(exactly = 1) {
                selfReviewRepository.update(
                    reviewData,
                )
            }
        }

        "send mail to employee when manager review is submitted" {
            val reviewResponse =
                ReviewData(
                    organisationId = 1,
                    reviewTypeId = 2,
                    reviewDetailsId = null,
                    reviewCycleId = 1,
                    reviewToId = 1,
                    reviewFromId = 2,
                    draft = false,
                    published = true,
                    firstManagerId = 3,
                    secondManagerId = 1,
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
                    averageRating = (-1.0).toBigDecimal(),
                )
            val reviewCycleData =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf(LocalDate.now().minusDays(10)),
                    endDate = Date.valueOf(LocalDate.now().plusDays(10)),
                    selfReviewStartDate = Date.valueOf(LocalDate.now().minusDays(9)),
                    selfReviewEndDate = Date.valueOf(LocalDate.now().minusDays(1)),
                    managerReviewStartDate = Date.valueOf(LocalDate.now()),
                    managerReviewEndDate = Date.valueOf(LocalDate.now().plusDays(4)),
                    lastModified = Timestamp.valueOf(LocalDateTime.now()),
                    publish = true,
                    checkInWithManagerStartDate = Date.valueOf(LocalDate.now().plusDays(5)),
                    checkInWithManagerEndDate = Date.valueOf(LocalDate.now().plusDays(8)),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            every {
                reviewCycleRepository.fetchActiveReviewCycle(reviewResponse.organisationId)
            } returns reviewCycleData
            every {
                selfReviewRepository.create(reviewResponse)
            } returns Unit
            every {
                managerReviewSubmittedMail.sendMailToEmployee(any(), any(), any(), reviewCycleData.organisationId)
            } returns Unit
            managerReviewSubmittedMail.sendMailToEmployee(
                reviewResponse.reviewFromId,
                reviewResponse.reviewToId,
                reviewCycleData.reviewCycleId,
                reviewCycleData.organisationId,
            )
            verify {
                managerReviewSubmittedMail.sendMailToEmployee(
                    reviewResponse.reviewFromId,
                    reviewResponse.reviewToId,
                    reviewCycleData.reviewCycleId,
                    reviewCycleData.organisationId,
                )
            }
        }

        "send mail to employee when manager review is updated and submitted" {
            val reviewResponse =
                ReviewData(
                    organisationId = 1,
                    reviewTypeId = 2,
                    reviewDetailsId = null,
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
                    averageRating = (-1.0).toBigDecimal(),
                )
            every { selfReviewService.update(reviewResponse) } returns Unit
            every { managerReviewSubmittedMail.sendMailToEmployee(any(), any(), any(), reviewResponse.organisationId) } returns Unit
            managerReviewSubmittedMail.sendMailToEmployee(
                reviewResponse.reviewFromId,
                reviewResponse.reviewToId,
                reviewResponse.reviewCycleId,
                reviewResponse.organisationId,
            )
            verify {
                managerReviewSubmittedMail.sendMailToEmployee(
                    reviewResponse.reviewFromId,
                    reviewResponse.reviewToId,
                    reviewResponse.reviewCycleId,
                    reviewResponse.organisationId,
                )
            }
        }

        "send mail to manager when manager review is submitted for all employees" {
            val reviewResponse =
                ReviewData(
                    organisationId = 1,
                    reviewTypeId = 2,
                    reviewDetailsId = null,
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
                    averageRating = (-1.0).toBigDecimal(),
                )
            val reviewCycleData =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf(LocalDate.now().minusDays(10)),
                    endDate = Date.valueOf(LocalDate.now().plusDays(10)),
                    selfReviewStartDate = Date.valueOf(LocalDate.now().minusDays(9)),
                    selfReviewEndDate = Date.valueOf(LocalDate.now().minusDays(1)),
                    managerReviewStartDate = Date.valueOf(LocalDate.now()),
                    managerReviewEndDate = Date.valueOf(LocalDate.now().plusDays(4)),
                    lastModified = Timestamp.valueOf(LocalDateTime.now()),
                    publish = true,
                    checkInWithManagerStartDate = Date.valueOf(LocalDate.now().plusDays(5)),
                    checkInWithManagerEndDate = Date.valueOf(LocalDate.now().plusDays(8)),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            every {
                reviewCycleRepository.fetchActiveReviewCycle(reviewResponse.organisationId)
            } returns reviewCycleData
            every {
                reviewCycleRepository.fetchReviewCycle(1)
            } returns reviewCycleData
            every {
                selfReviewRepository.create(reviewResponse)
            } returns Unit
            every {
                managerReviewSubmittedMail.sendMailToManager(any(), any(), reviewResponse.organisationId)
            } returns Unit
            managerReviewSubmittedMail.sendMailToManager(2, reviewResponse.reviewCycleId, reviewResponse.organisationId)
            verify {
                managerReviewSubmittedMail.sendMailToManager(2, reviewResponse.reviewCycleId, reviewResponse.organisationId)
            }
        }

        "should create a self-review successfully and calculate average rating" {
            val reviewData =
                ReviewData(
                    organisationId = 1,
                    reviewTypeId = ReviewType.SELF.id,
                    reviewDetailsId = 1,
                    reviewCycleId = 1,
                    reviewToId = 1,
                    reviewFromId = 1,
                    draft = false,
                    published = true,
                    reviewData =
                        listOf(
                            Review(
                                reviewId = 1,
                                id = 1,
                                kraId = 1,
                                kraName = "Knowledge & Skills Growth",
                                review = "Great work",
                                rating = 5,
                                kpiTitle = "Coding",
                                kpiDescription = "Clean code",
                            ),
                            Review(
                                reviewId = 2,
                                id = 2,
                                kraId = 2,
                                kraName = "Results",
                                review = "Good performance",
                                rating = 4,
                                kpiTitle = "Communication",
                                kpiDescription = "Communication",
                            ),
                            Review(
                                reviewId = 3,
                                id = 3,
                                kpiTitle = "Contribution to growth",
                                kpiDescription = "Contribution to growth",
                                kraId = 3,
                                kraName = "Attitude Fitment",
                                review = "Needs improvement",
                                rating = 2,
                            ),
                        ),
                    averageRating = BigDecimal.ZERO,
                    firstManagerId = 1,
                    secondManagerId = 2,
                )

            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf(organisationCurrentDate.minusDays(10)),
                    endDate = Date.valueOf(organisationCurrentDate.plusDays(10)),
                    selfReviewStartDate = Date.valueOf(organisationCurrentDate.minusDays(9)),
                    selfReviewEndDate = Date.valueOf(organisationCurrentDate.plusDays(1)),
                    managerReviewStartDate = Date.valueOf(organisationCurrentDate),
                    managerReviewEndDate = Date.valueOf(organisationCurrentDate.plusDays(4)),
                    lastModified = Timestamp.valueOf(organisationCurrentTime),
                    publish = true,
                    checkInWithManagerStartDate = Date.valueOf(LocalDate.now().plusDays(5)),
                    checkInWithManagerEndDate = Date.valueOf(LocalDate.now().plusDays(8)),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                ).withActiveFlags(organisationDetails.timeZone)

            val kraWeightages =
                listOf(
                    KRAWeightage(id = 1, weightage = 60),
                    KRAWeightage(id = 2, weightage = 30),
                    KRAWeightage(id = 3, weightage = 10),
                )

            every { reviewCycleRepository.fetchActiveReviewCycle(reviewData.organisationId) } returns reviewCycle
            every { reviewCycleRepository.fetchReviewCycle(reviewData.reviewCycleId) } returns reviewCycle
            every { kraRepository.getWeightageByIds(any(), any()) } returns kraWeightages
            every { selfReviewRepository.create(any()) } returns Unit
            every {
                selfReviewSlackNotifications.selfReviewSubmitted(
                    reviewData.reviewToId,
                    reviewData.organisationId,
                )
            } just Runs
            every {
                selfReviewSubmittedMail.sendMail(
                    reviewData.reviewToId,
                    reviewData.reviewCycleId,
                    reviewData.organisationId,
                )
            } just Runs

            selfReviewService.create(reviewData)

            val expectedAverageRating =
                (
                    BigDecimal
                        .valueOf(5 * 60)
                        .add(BigDecimal.valueOf(4 * 30))
                        .add(BigDecimal.valueOf(2 * 10))
                ).divide(BigDecimal.valueOf(100))

            reviewData.averageRating.shouldBe(expectedAverageRating)
            verify { selfReviewRepository.create(reviewData) }
            verify { selfReviewSubmittedMail.sendMail(1, 1, 1) }
            verify { selfReviewSlackNotifications.selfReviewSubmitted(1, 1) }
        }

        "should update review successfully" {
            val reviewDetails =
                mockk<ReviewDetails> {
                    every { reviewCycleId } returns 9
                    every { published } returns false
                }
            val activeReviewCycle =
                mockk<ReviewCycle> {
                    every { reviewCycleId } returns 9
                    every { startDate } returns Date.valueOf(LocalDate.of(2025, 1, 1))
                    every { endDate } returns Date.valueOf(LocalDate.of(2050, 1, 1))
                    every { selfReviewStartDate } returns Date.valueOf(LocalDate.of(2025, 1, 1))
                    every { selfReviewEndDate } returns Date.valueOf(LocalDate.of(2050, 1, 1))
                    every { managerReviewStartDate } returns Date.valueOf(LocalDate.of(2025, 1, 1))
                    every { managerReviewEndDate } returns Date.valueOf(LocalDate.of(2050, 1, 1))
                    every { checkInWithManagerStartDate } returns Date.valueOf(LocalDate.of(2025, 1, 1))
                    every { checkInWithManagerEndDate } returns Date.valueOf(LocalDate.of(2050, 1, 1))
                    every { isReviewCycleActive } returns true
                    every { isSelfReviewActive } returns true
                    every { isSelfReviewDatePassed } returns false
                }

            val updateReviewRequest =
                UpdateReviewRequest(
                    reviewToId = 1,
                    reviewFromId = 4,
                    organisationId = 1,
                    reviewCycleId = 9,
                    reviewTypeId = 1,
                    reviewId = 1,
                    review = "Updated review",
                    rating = 4,
                )

            every { selfReviewRepository.fetch(any(), any(), any(), any()) } returns listOf(reviewDetails)
            every { selfReviewRepository.updateReview(any(), any(), any()) } returns Unit
            every { reviewCycleRepository.fetchActiveReviewCycle(updateReviewRequest.organisationId) } returns activeReviewCycle
            every { activeReviewCycle.withActiveFlags("Asia/Kolkata") } returns activeReviewCycle

            selfReviewService.updateReview(updateReviewRequest)

            verify(exactly = 1) {
                selfReviewRepository.updateReview(
                    updateReviewRequest.reviewId,
                    updateReviewRequest.review,
                    updateReviewRequest.rating,
                )
            }
        }

        "should retrieve review by ID" {

            val reviewId = 1L
            val expectedReview =
                Review(
                    reviewId = reviewId,
                    id = 10L,
                    kpiTitle = "KPI Title",
                    kpiDescription = "KPI Description",
                    kraId = 2L,
                    kraName = "Results",
                    review = "This is a review",
                    rating = 5,
                )

            every { selfReviewRepository.getReviewById(reviewId) } returns expectedReview

            val result = selfReviewService.getReviewById(reviewId)

            result shouldBe expectedReview

            verify(exactly = 1) { selfReviewRepository.getReviewById(reviewId) }
        }

        "should calculate weighted review score correctly with three KRAs" {
            val reviewCycleId = 1L
            val reviewDetailsId = 1L

            val reviews =
                listOf(
                    Review(
                        reviewId = 1,
                        id = 1,
                        kraId = 1,
                        kraName = "Knowledge and Skills Growth",
                        kpiTitle = "Knowledge and Skill Growth",
                        kpiDescription = "Improvement in technical skills",
                        review = "Great work",
                        rating = 5,
                    ),
                    Review(
                        reviewId = 2,
                        id = 2,
                        kraId = 1,
                        kraName = "Knowledge and Skills Growth",
                        kpiTitle = "Knowledge and Skill Growth",
                        kpiDescription = "Improvement in technical skills",
                        review = "Good performance",
                        rating = 4,
                    ),
                    Review(
                        reviewId = 3,
                        id = 3,
                        kraId = 2,
                        kraName = "Results",
                        kpiTitle = "Results",
                        kpiDescription = "Achievement of goals",
                        review = "Needs improvement",
                        rating = 3,
                    ),
                    Review(
                        reviewId = 4,
                        id = 4,
                        kraId = 3,
                        kraName = "Attitude Fitment",
                        kpiTitle = "Attitude Fitment",
                        kpiDescription = "Alignment with company values",
                        review = "Excellent attitude",
                        rating = 5,
                    ),
                )

            val kraList =
                listOf(
                    GetAllKRAResponse(
                        id = 1,
                        kraId = "KRA01",
                        name = "Knowledge and Skill Growth",
                        weightage = 40,
                        organisationId = 1,
                    ),
                    GetAllKRAResponse(
                        id = 2,
                        kraId = "KRA02",
                        name = "Results",
                        weightage = 35,
                        organisationId = 1,
                    ),
                    GetAllKRAResponse(
                        id = 3,
                        kraId = "KRA03",
                        name = "Attitude Fitment",
                        weightage = 25,
                        organisationId = 1,
                    ),
                )

            val expectedKraWeightedScores =
                listOf(
                    KRAWeightedScore(
                        kraId = 1,
                        kraName = "Knowledge and Skill Growth",
                        kraWeightage = 40,
                        weightedRating = BigDecimal.valueOf(4.5 * 40 / 100).setScale(2, RoundingMode.HALF_EVEN),
                    ),
                    KRAWeightedScore(
                        kraId = 2,
                        kraName = "Results",
                        kraWeightage = 35,
                        weightedRating = BigDecimal.valueOf(3.0 * 35 / 100).setScale(2, RoundingMode.HALF_EVEN),
                    ),
                    KRAWeightedScore(
                        kraId = 3,
                        kraName = "Attitude Fitment",
                        kraWeightage = 25,
                        weightedRating = BigDecimal.valueOf(5.0 * 25 / 100).setScale(2, RoundingMode.HALF_EVEN),
                    ),
                )

            val expectedFinalScore = expectedKraWeightedScores.sumOf { it.weightedRating }

            every { selfReviewRepository.getReviews(reviewDetailsId) } returns reviews
            every { kraRepository.getKraByReviewCycle(reviewCycleId) } returns kraList
            every { selfReviewRepository.getAverageRating(reviewCycleId, reviewDetailsId) } returns null

            val result = selfReviewService.getWeightedReviewScore(reviewCycleId, reviewDetailsId)

            result.finalScore.shouldBe(expectedFinalScore)
            result.kraWeightedScores.shouldBe(expectedKraWeightedScores)

            verify(exactly = 1) { selfReviewRepository.getReviews(reviewDetailsId) }
            verify(exactly = 1) { kraRepository.getKraByReviewCycle(reviewCycleId) }
            verify(exactly = 1) { selfReviewRepository.getAverageRating(reviewCycleId, reviewDetailsId) }
        }
    }
}
