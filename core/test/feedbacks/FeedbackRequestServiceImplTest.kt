package feedbacks

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.emails.RequestFeedbackMail
import scalereal.core.feedbacks.FeedbackRequestRepository
import scalereal.core.feedbacks.FeedbackRequestService
import scalereal.core.linkHandling.LinkHandlingService
import scalereal.core.models.domain.FeedbackDetail
import scalereal.core.models.domain.FeedbackRequest
import scalereal.core.models.domain.FeedbackRequestData
import scalereal.core.models.domain.FeedbackRequestDetails
import scalereal.core.models.domain.FeedbackRequestParams
import scalereal.core.models.domain.Goal
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.review.CheckInWithManagerService
import scalereal.core.reviewCycle.ReviewCycleService
import scalereal.core.slack.RequestFeedbackSlackNotification
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant

class FeedbackRequestServiceImplTest : StringSpec() {
    private val feedbackRequestRepository = mockk<FeedbackRequestRepository>()
    private val reviewCycleService = mockk<ReviewCycleService>()
    private val checkInWithManagerService = mockk<CheckInWithManagerService>()
    private val requestFeedbackMail = mockk<RequestFeedbackMail>()
    private val requestFeedbackSlackNotification = mockk<RequestFeedbackSlackNotification>()
    private val linkHandlingService = mockk<LinkHandlingService>()
    private val organisationRepository = mockk<OrganisationRepository>()
    private val feedbackRequestService =
        FeedbackRequestService(
            feedbackRequestRepository,
            reviewCycleService,
            checkInWithManagerService,
            requestFeedbackMail,
            requestFeedbackSlackNotification,
            linkHandlingService,
            organisationRepository,
        )

    init {
        "should get all feedback requests" {
            val feedbackRequestData =
                listOf(
                    FeedbackRequestData(
                        organisationId = 1,
                        requestId = 1,
                        isSubmitted = false,
                        isExternalRequest = false,
                        requestedOn = null,
                        requestedById = 1,
                        requestedByEmployeeId = "SR0043",
                        requestedByFirstName = "Syed Ubed",
                        requestedByLastName = "Ali",
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackToFirstName = "Syed Ubed",
                        feedbackToLastName = "Ali",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0050",
                        feedbackFromFirstName = "Moly",
                        feedbackFromLastName = "Agarwal",
                        externalFeedbackFromEmail = null,
                        isDraft = false,
                    ),
                    FeedbackRequestData(
                        organisationId = 1,
                        requestId = 2,
                        isSubmitted = false,
                        isExternalRequest = false,
                        requestedOn = null,
                        requestedById = 3,
                        requestedByEmployeeId = "SR0035",
                        requestedByFirstName = "Twinkle",
                        requestedByLastName = "Dahiya",
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0050",
                        feedbackToFirstName = "Moly",
                        feedbackToLastName = "Agarwal",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        feedbackFromFirstName = "Syed Ubed",
                        feedbackFromLastName = "Ali",
                        externalFeedbackFromEmail = null,
                        isDraft = false,
                    ),
                    FeedbackRequestData(
                        organisationId = 1,
                        requestId = 3,
                        isSubmitted = true,
                        isExternalRequest = false,
                        requestedOn = null,
                        requestedById = 3,
                        requestedByEmployeeId = "SR0035",
                        requestedByFirstName = "Twinkle",
                        requestedByLastName = "Dahiya",
                        feedbackToId = 4,
                        feedbackToEmployeeId = "SR0051",
                        feedbackToFirstName = "Rushad",
                        feedbackToLastName = "shaikh",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        feedbackFromFirstName = "Syed Ubed",
                        feedbackFromLastName = "Ali",
                        externalFeedbackFromEmail = null,
                        isDraft = false,
                    ),
                )

            val feedbackRequestParams =
                FeedbackRequestParams(
                    organisationId = 1,
                    requestedById = listOf(-99),
                    feedbackToId = listOf(-99),
                    feedbackFromId = listOf(-99),
                    isSubmitted = listOf("true", "false"),
                    reviewCycleId = listOf(-99),
                    sortBy = "dateDesc",
                )

            every {
                feedbackRequestRepository.fetchFeedbackRequestData(
                    feedbackRequestParams,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            } returns feedbackRequestData

            feedbackRequestService.fetchFeedbackRequestData(
                feedbackRequestParams,
                page = 1,
                limit = Int.MAX_VALUE,
            ) shouldBe feedbackRequestData
            verify(exactly = 1) {
                feedbackRequestRepository.fetchFeedbackRequestData(
                    feedbackRequestParams,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            }
        }

        "should get all pending feedback requests" {

            val feedbackRequestData =
                listOf(
                    FeedbackRequestData(
                        organisationId = 1,
                        requestId = 1,
                        isSubmitted = false,
                        isExternalRequest = false,
                        requestedOn = null,
                        requestedById = 1,
                        requestedByEmployeeId = "SR0043",
                        requestedByFirstName = "Syed Ubed",
                        requestedByLastName = "Ali",
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackToFirstName = "Syed Ubed",
                        feedbackToLastName = "Ali",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0050",
                        feedbackFromFirstName = "Moly",
                        feedbackFromLastName = "Agarwal",
                        externalFeedbackFromEmail = null,
                        isDraft = false,
                    ),
                    FeedbackRequestData(
                        organisationId = 1,
                        requestId = 2,
                        isSubmitted = false,
                        isExternalRequest = false,
                        requestedOn = null,
                        requestedById = 3,
                        requestedByEmployeeId = "SR0035",
                        requestedByFirstName = "Twinkle",
                        requestedByLastName = "Dahiya",
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0050",
                        feedbackToFirstName = "Moly",
                        feedbackToLastName = "Agarwal",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        feedbackFromFirstName = "Syed Ubed",
                        feedbackFromLastName = "Ali",
                        externalFeedbackFromEmail = null,
                        isDraft = false,
                    ),
                )

            val feedbackRequestParams =
                FeedbackRequestParams(
                    organisationId = 1,
                    requestedById = listOf(-99),
                    feedbackToId = listOf(-99),
                    feedbackFromId = listOf(-99),
                    isSubmitted = listOf("false"),
                    reviewCycleId = listOf(-99),
                    sortBy = "dateDesc",
                )

            every {
                feedbackRequestRepository.fetchFeedbackRequestData(feedbackRequestParams, offset = 0, limit = Int.MAX_VALUE)
            } returns feedbackRequestData

            feedbackRequestService.fetchFeedbackRequestData(
                feedbackRequestParams,
                page = 1,
                limit = Int.MAX_VALUE,
            ) shouldBe feedbackRequestData
            verify(exactly = 1) {
                feedbackRequestRepository.fetchFeedbackRequestData(
                    feedbackRequestParams,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            }
        }

        "should get all completed feedback requests" {

            val feedbackRequestData =
                listOf(
                    FeedbackRequestData(
                        organisationId = 1,
                        requestId = 3,
                        isSubmitted = true,
                        isExternalRequest = false,
                        requestedOn = null,
                        requestedById = 3,
                        requestedByEmployeeId = "SR0035",
                        requestedByFirstName = "Twinkle",
                        requestedByLastName = "Dahiya",
                        feedbackToId = 4,
                        feedbackToEmployeeId = "SR0051",
                        feedbackToFirstName = "Rushad",
                        feedbackToLastName = "shaikh",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        feedbackFromFirstName = "Syed Ubed",
                        feedbackFromLastName = "Ali",
                        externalFeedbackFromEmail = null,
                        isDraft = false,
                    ),
                )

            val feedbackRequestParams =
                FeedbackRequestParams(
                    organisationId = 1,
                    requestedById = listOf(-99),
                    feedbackToId = listOf(-99),
                    feedbackFromId = listOf(-99),
                    isSubmitted = listOf("true"),
                    reviewCycleId = listOf(-99),
                    sortBy = "dateDesc",
                )

            every {
                feedbackRequestRepository.fetchFeedbackRequestData(feedbackRequestParams, offset = 0, limit = Int.MAX_VALUE)
            } returns feedbackRequestData

            feedbackRequestService.fetchFeedbackRequestData(
                feedbackRequestParams,
                page = 1,
                limit = Int.MAX_VALUE,
            ) shouldBe feedbackRequestData
            verify(exactly = 1) {
                feedbackRequestRepository.fetchFeedbackRequestData(
                    feedbackRequestParams,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            }
        }

        "should throw an exception when there is no previous review cycle" {
            val reviewCycleId = emptyList<Long>()
            every { reviewCycleService.getPreviousReviewCycleId(any()) } returns reviewCycleId

            val exception =
                shouldThrow<Exception> {
                    feedbackRequestService.getGoals(organisationId = 1, feedbackToId = 2)
                }
            exception.message shouldBe "There is no previous review cycle."
        }

        "should return a list of goals when both conditions are met" {
            val reviewCycleId = 1L
            val goals =
                listOf(
                    Goal(
                        id = 1,
                        goalId = "G1",
                        typeId = 1,
                        description = "Complete tasks on time",
                        createdAt = Timestamp.from(Instant.now()),
                        targetDate = Date.valueOf("2024-12-12"),
                        progressId = 1,
                        progressName = "Pending",
                        createdBy = 1,
                        assignedTo = 2,
                    ),
                )

            every { reviewCycleService.getPreviousReviewCycleId(any()).firstOrNull() } returns reviewCycleId
            every { checkInWithManagerService.getGoalsByReviewCycleId(any(), any()) } returns goals
            every { feedbackRequestService.getGoals(any(), any()) } returns goals
        }

        "should get feedback request details by id" {
            // Given
            val requestId = 1L
            val timestamp = Timestamp.from(Instant.now())

            val feedbackRequest =
                FeedbackRequest(
                    id = requestId,
                    requestedById = 1L,
                    requestedByEmployeeId = "EMP001",
                    requestedByFirstName = "John",
                    requestedByLastName = "Doe",
                    feedbackToId = 2L,
                    feedbackToEmployeeId = "EMP002",
                    feedbackToFirstName = "Jane",
                    feedbackToLastName = "Smith",
                    feedbackFromId = 3L,
                    feedbackFromEmployeeId = "EMP003",
                    feedbackFromFirstName = "Bob",
                    feedbackFromLastName = "Johnson",
                    goalId = 1L,
                    goalDescription = "Complete project documentation",
                    request = "Please provide feedback on the recent project",
                    createdAt = timestamp,
                    isSubmitted = true,
                    isExternalRequest = false,
                    externalFeedbackFromEmail = null,
                )

            val feedbackDetails =
                listOf(
                    FeedbackDetail(
                        feedbackId = 1L,
                        feedback = "Great work on the project!",
                        feedbackTypeId = 1,
                        feedbackType = "Positive",
                        isDraft = false,
                    ),
                    FeedbackDetail(
                        feedbackId = 2L,
                        feedback = "Could improve on documentation",
                        feedbackTypeId = 2,
                        feedbackType = "Improvement",
                        isDraft = false,
                    ),
                )

            // When
            every { feedbackRequestRepository.fetchFeedbackRequestDetails(requestId) } returns feedbackRequest
            every { feedbackRequestRepository.fetchFeedbackByRequestId(requestId) } returns feedbackDetails

            val result = feedbackRequestService.getFeedbackRequestDetailsById(requestId)

            // Then
            result shouldBe
                FeedbackRequestDetails(
                    requestId = requestId,
                    isSubmitted = true,
                    isExternalRequest = false,
                    requestedOn = timestamp,
                    goalId = 1L,
                    goalDescription = "Complete project documentation",
                    requestedById = 1L,
                    requestedByEmployeeId = "EMP001",
                    requestedByFirstName = "John",
                    requestedByLastName = "Doe",
                    feedbackToId = 2L,
                    feedbackToEmployeeId = "EMP002",
                    feedbackToFirstName = "Jane",
                    feedbackToLastName = "Smith",
                    feedbackFromId = 3L,
                    feedbackFromEmployeeId = "EMP003",
                    feedbackFromFirstName = "Bob",
                    feedbackFromLastName = "Johnson",
                    externalFeedbackFromEmail = null,
                    request = "Please provide feedback on the recent project",
                    feedbackData = feedbackDetails,
                )

            verify(exactly = 1) { feedbackRequestRepository.fetchFeedbackRequestDetails(requestId) }
            verify(exactly = 1) { feedbackRequestRepository.fetchFeedbackByRequestId(requestId) }
        }
    }
}
