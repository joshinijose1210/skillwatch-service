package feedbacks

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.emails.FeedbackReceivedMail
import scalereal.core.emails.RequestFeedbackMail
import scalereal.core.employees.EmployeeRepository
import scalereal.core.feedbacks.FeedbackRepository
import scalereal.core.feedbacks.FeedbackRequestRepository
import scalereal.core.feedbacks.FeedbackService
import scalereal.core.models.domain.AddFeedbackData
import scalereal.core.models.domain.CreateFeedbackParams
import scalereal.core.models.domain.FeedbackCounts
import scalereal.core.models.domain.FeedbackData
import scalereal.core.models.domain.Feedbacks
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.reviewCycle.ReviewCycleRepository
import scalereal.core.slack.RequestFeedbackSlackNotification
import scalereal.core.slack.SlackService
import java.sql.Date
import java.sql.Timestamp

class FeedbackServiceImplTest : StringSpec() {
    private val feedbackRepository = mockk<FeedbackRepository>()
    private val feedbackRequestRepository = mockk<FeedbackRequestRepository>()
    private val feedbackReceivedMail = mockk<FeedbackReceivedMail>()
    private val reviewCycleRepository = mockk<ReviewCycleRepository>()
    private val employeeRepository = mockk<EmployeeRepository>()
    private val requestFeedbackMail = mockk<RequestFeedbackMail>()
    private val slackService = mockk<SlackService>()
    private val requestFeedbackSlackNotification = mockk<RequestFeedbackSlackNotification>()
    private val feedbackService =
        FeedbackService(
            feedbackRepository,
            feedbackRequestRepository,
            feedbackReceivedMail,
            requestFeedbackMail,
            slackService,
            employeeRepository,
            requestFeedbackSlackNotification,
        )

    init {

        "should add/insert new feedback" {
            val createFeedbackParams =
                CreateFeedbackParams(
                    feedback =
                        listOf(
                            AddFeedbackData(
                                feedbackTypeId = 1,
                                feedbackText = "Good Work",
                                markdownText = "",
                            ),
                        ),
                    feedbackToId = 2,
                    feedbackFromId = 1,
                    requestId = null,
                    isDraft = false,
                )

            every { feedbackRepository.create(createFeedbackParams) } returns Unit
            feedbackRepository.create(createFeedbackParams) shouldBe Unit
            verify(exactly = 1) { feedbackRepository.create(createFeedbackParams) }
        }

        "should add requested feedback" {
            val createFeedbackParams =
                CreateFeedbackParams(
                    feedback =
                        listOf(
                            AddFeedbackData(
                                feedbackTypeId = 1,
                                feedbackText = "Great Work",
                                markdownText = "",
                            ),
                        ),
                    feedbackToId = 1,
                    feedbackFromId = 2,
                    requestId = 1,
                    isDraft = false,
                )

            if (createFeedbackParams.requestId != null) {
                every { feedbackRequestRepository.updateFeedbackRequestStatus(createFeedbackParams.requestId!!) } returns Unit
            }
            every { feedbackRepository.create(createFeedbackParams) } returns Unit
            feedbackRepository.create(createFeedbackParams) shouldBe Unit
            verify(exactly = 1) { feedbackRepository.create(createFeedbackParams) }
        }

        "should get feedbacks received by employee" {

            val feedback =
                listOf(
                    FeedbackData(
                        srNo = 1,
                        date = Timestamp.valueOf("2022-11-4 13:9:48.834129"),
                        organisationId = 1,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0050",
                        empFirstName = "Moly",
                        empLastName = "Agarwal",
                        empRoleName = "Employee",
                        feedback = "this is a test feedback",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                        isDraft = false,
                        isExternalFeedback = false,
                        externalFeedbackFromEmailId = null,
                    ),
                    FeedbackData(
                        srNo = 2,
                        date = Timestamp.valueOf("2022-11-6 1:35:21.672150"),
                        organisationId = 1,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 3,
                        feedbackFromEmployeeId = "SR0051",
                        empFirstName = "Rushad",
                        empLastName = "Sheikh",
                        empRoleName = "Employee",
                        feedback = "this is a test feedback",
                        feedbackTypeId = 1,
                        feedbackType = "Positive",
                        isDraft = false,
                        isExternalFeedback = false,
                        externalFeedbackFromEmailId = null,
                    ),
                )

            val organisationId: Long = 1
            val feedbackToId: Long = 1
            val feedbackFromId: List<Int> = listOf(2, 3)
            val tagId: List<Int> = listOf(1)
            every {
                feedbackRepository.fetchAllFeedbacksReceived(
                    organisationId = organisationId,
                    feedbackToId = feedbackToId,
                    feedbackFromId = feedbackFromId,
                    feedbackTypeId = tagId,
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            } returns feedback
            feedbackService.fetchAllFeedbacksReceived(
                organisationId = organisationId,
                feedbackToId = feedbackToId,
                feedbackFromId = feedbackFromId,
                reviewCycleId = listOf(-99),
                feedbackTypeId = tagId,
                page = 1,
                limit = Int.MAX_VALUE,
                sortBy = "dateDesc",
            ) shouldBe feedback
            verify(exactly = 1) {
                feedbackRepository.fetchAllFeedbacksReceived(
                    organisationId = organisationId,
                    feedbackToId = feedbackToId,
                    feedbackFromId = feedbackFromId,
                    reviewCycleId = listOf(-99),
                    feedbackTypeId = tagId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            }
        }

        "should get all Submitted feedbacks" {

            val feedback =
                listOf(
                    FeedbackData(
                        srNo = 3,
                        date = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                        organisationId = 1,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0050",
                        empFirstName = "chetan",
                        empLastName = "varade",
                        empRoleName = "Employee",
                        feedback = "nice work",
                        feedbackTypeId = 1,
                        feedbackType = "Positive",
                        isDraft = false,
                        isExternalFeedback = false,
                        externalFeedbackFromEmailId = null,
                    ),
                )

            val organisationId: Long = 1
            val feedbackFromId: Long = 1
            val feedbackToId: List<Int> = listOf(2, 3)
            val tagId: List<Int> = listOf(0)
            every {
                feedbackRepository.fetchAllSubmittedFeedbacks(
                    organisationId,
                    feedbackFromId,
                    feedbackToId,
                    tagId,
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            } returns feedback

            feedbackService.fetchAllSubmittedFeedbacks(
                organisationId,
                feedbackFromId,
                feedbackToId,
                tagId,
                reviewCycleId = listOf(-99),
                page = 1,
                limit = Int.MAX_VALUE,
                sortBy = "dateDesc",
            ) shouldBe feedback

            verify(exactly = 1) {
                feedbackRepository.fetchAllSubmittedFeedbacks(
                    organisationId,
                    feedbackFromId,
                    feedbackToId,
                    tagId,
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            }
        }

        "should get submitted feedback based on selected tags" {
            val feedback =
                listOf(
                    FeedbackData(
                        srNo = 3,
                        date = Timestamp.valueOf("2022-11-10 19:6:35.142179"),
                        organisationId = 1,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0050",
                        empFirstName = "chetan",
                        empLastName = "varade",
                        empRoleName = "Employee",
                        feedback = "nice work",
                        feedbackTypeId = 1,
                        feedbackType = "Positive",
                        isDraft = false,
                        isExternalFeedback = false,
                        externalFeedbackFromEmailId = null,
                    ),
                )

            val organisationId: Long = 1
            val feedbackFromId: Long = 1
            val feedbackToId: List<Int> = listOf(2, 3)
            val tagId: List<Int> = listOf(1)
            every {
                feedbackRepository.fetchAllSubmittedFeedbacks(
                    organisationId,
                    feedbackFromId,
                    feedbackToId,
                    tagId,
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            } returns feedback

            feedbackService.fetchAllSubmittedFeedbacks(
                organisationId,
                feedbackFromId,
                feedbackToId,
                tagId,
                reviewCycleId = listOf(-99),
                page = 1,
                limit = Int.MAX_VALUE,
                sortBy = "dateDesc",
            ) shouldBe feedback

            verify(exactly = 1) {
                feedbackRepository.fetchAllSubmittedFeedbacks(
                    organisationId = organisationId,
                    feedbackFromId,
                    feedbackToId,
                    tagId,
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            }
        }

        "should get received feedbacks based on selected tags" {

            val feedback =
                listOf(
                    FeedbackData(
                        srNo = 1,
                        date = Timestamp.valueOf("2022-11-4 13:9:48.834129"),
                        organisationId = 1,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0050",
                        empFirstName = "Moly",
                        empLastName = "Agarwal",
                        empRoleName = "Employee",
                        feedback = "this is a test feedback",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                        isDraft = false,
                        isExternalFeedback = false,
                        externalFeedbackFromEmailId = null,
                    ),
                    FeedbackData(
                        srNo = 2,
                        date = Timestamp.valueOf("2022-11-6 1:35:21.672150"),
                        organisationId = 1,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 3,
                        feedbackFromEmployeeId = "SR0051",
                        empFirstName = "Rushad",
                        empLastName = "Sheikh",
                        empRoleName = "Employee",
                        feedback = "this is a test feedback",
                        feedbackTypeId = 1,
                        feedbackType = "Positive",
                        isDraft = false,
                        isExternalFeedback = false,
                        externalFeedbackFromEmailId = null,
                    ),
                )

            val organisationId: Long = 1
            val feedbackToId: Long = 1
            val feedbackFromId: List<Int> = listOf(2, 3)
            val tagId: List<Int> = listOf(1, 3)
            every {
                feedbackRepository.fetchAllFeedbacksReceived(
                    organisationId,
                    feedbackToId,
                    feedbackFromId,
                    tagId,
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            } returns feedback
            feedbackService.fetchAllFeedbacksReceived(
                organisationId,
                feedbackToId,
                feedbackFromId,
                tagId,
                reviewCycleId = listOf(-99),
                page = 1,
                limit = Int.MAX_VALUE,
                sortBy = "dateDesc",
            ) shouldBe feedback
            verify(exactly = 1) {
                feedbackRepository.fetchAllFeedbacksReceived(
                    organisationId,
                    feedbackToId,
                    feedbackFromId,
                    tagId,
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            }
        }
        "should return an empty list when feedback not found" {

            val organisationId: Long = 1
            val feedbackFromId: Long = 1
            val feedbackToId: List<Int> = listOf(2, 3)
            val tagId: List<Int> = listOf(0)
            every {
                feedbackRepository
                    .fetchAllSubmittedFeedbacks(
                        organisationId,
                        feedbackFromId,
                        feedbackToId,
                        tagId,
                        reviewCycleId = listOf(-99),
                        offset = 0,
                        limit = Int.MAX_VALUE,
                        sortBy = "dateDesc",
                    ).isEmpty()
            } returns true

            feedbackService
                .fetchAllSubmittedFeedbacks(
                    organisationId,
                    feedbackFromId,
                    feedbackToId,
                    tagId,
                    reviewCycleId = listOf(-99),
                    page = 1,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                ).isEmpty() shouldBe true

            verify {
                feedbackRepository.fetchAllSubmittedFeedbacks(
                    organisationId,
                    feedbackFromId,
                    feedbackToId,
                    tagId,
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            }
        }

        "should fetch list of all employee feedbacks after selecting dates" {
            val feedbacks =
                listOf(
                    Feedbacks(
                        Date.valueOf("2022-12-30"),
                        false,
                        "nice work",
                        1,
                        "SR0050",
                        2,
                        "SR0046",
                        1,
                        "Positive",
                        "moly agrawal",
                        "gaurav pakhale",
                        "Developer",
                        "kotlin engineer",
                        null,
                        1,
                        false,
                    ),
                )
            every {
                feedbackRepository.fetchAllFeedbacks(
                    organisationId = 1,
                    "SR0050",
                    feedbackTypeId = listOf(-99),
                    fromDate = "2022 - 12 - 30",
                    toDate = "2022 - 12 - 30",
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            } returns feedbacks

            feedbackService.fetchAllFeedbacks(
                organisationId = 1,
                searchText = "SR0050",
                feedbackTypeId = listOf(-99),
                fromDate = "2022 - 12 - 30",
                toDate = "2022 - 12 - 30",
                reviewCycleId = listOf(-99),
                page = 1,
                limit = Int.MAX_VALUE,
                sortBy = "dateDesc",
            ) shouldBe feedbacks
            coVerify {
                feedbackRepository.fetchAllFeedbacks(
                    organisationId = 1,
                    searchText = "SR0050",
                    listOf(-99),
                    fromDate = "2022 - 12 - 30",
                    toDate = "2022 - 12 - 30",
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            }
        }

        "should fetch list of all Positive feedback after selecting dates and search text" {
            val feedbacks =
                listOf(
                    Feedbacks(
                        Date.valueOf("2022-12-30"),
                        false,
                        "nice work",
                        1,
                        "SR0050",
                        2,
                        "SR0046",
                        1,
                        "positive",
                        "moly agrawal",
                        "gaurav pakhale",
                        "Developer",
                        "kotlin engineer",
                        null,
                        1,
                        false,
                    ),
                )
            every {
                feedbackRepository.fetchAllFeedbacks(
                    organisationId = 1,
                    searchText = "moly",
                    feedbackTypeId = listOf(1),
                    fromDate = "2022 - 12 - 30",
                    toDate = "2022 - 12 - 30",
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            } returns feedbacks

            feedbackService.fetchAllFeedbacks(
                organisationId = 1,
                searchText = "moly",
                feedbackTypeId = listOf(1),
                fromDate = "2022 - 12 - 30",
                toDate = "2022 - 12 - 30",
                reviewCycleId = listOf(-99),
                page = 1,
                limit = Int.MAX_VALUE,
                sortBy = "dateDesc",
            ) shouldBe feedbacks
            coVerify {
                feedbackRepository.fetchAllFeedbacks(
                    organisationId = 1,
                    searchText = "moly",
                    listOf(1),
                    fromDate = "2022 - 12 - 30",
                    toDate = "2022 - 12 - 30",
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            }
        }

        "should fetch feedback counts by id given during review cycles dates" {
            val id: Long = 1
            val reviewCycleId: Long = 1
            val reviewCycleDates =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 2,
                    startDate = Date.valueOf("2023-4-1"),
                    endDate = Date.valueOf("2023-9-30"),
                    publish = false,
                    lastModified = Timestamp.valueOf("2023-4-5 13:9:48.834129"),
                    selfReviewStartDate = Date.valueOf("2023-4-30"),
                    selfReviewEndDate = Date.valueOf("2023-5-30"),
                    managerReviewStartDate = Date.valueOf("2023-6-1"),
                    managerReviewEndDate = Date.valueOf("2023-6-30"),
                    checkInWithManagerStartDate = Date.valueOf("2023-7-1"),
                    checkInWithManagerEndDate = Date.valueOf("2023-7-30"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val feedbackCounts =
                FeedbackCounts(
                    submittedPositiveCount = 2,
                    submittedImprovementCount = 0,
                    submittedAppreciationCount = 2,
                    receivedPositiveCount = 2,
                    receivedImprovementCount = 0,
                    receivedAppreciationCount = 4,
                )
            every { reviewCycleRepository.fetchReviewCycle(reviewCycleId) } returns reviewCycleDates
            every {
                feedbackRepository.fetchEmployeeFeedbackCounts(
                    id = id,
                    startDate = reviewCycleDates.startDate,
                    endDate = reviewCycleDates.endDate,
                )
            } returns feedbackCounts
            feedbackRepository.fetchEmployeeFeedbackCounts(
                id = id,
                startDate = reviewCycleDates.startDate,
                endDate = reviewCycleDates.endDate,
            ) shouldBe feedbackCounts
            verify(exactly = 1) {
                feedbackRepository.fetchEmployeeFeedbackCounts(
                    id = id,
                    startDate = reviewCycleDates.startDate,
                    endDate = reviewCycleDates.endDate,
                )
            }
        }

        "should mark feedback as read or unread" {
            val id = 1L
            val isRead = true

            every { feedbackRepository.markFeedbackAsReadOrUnread(id, isRead) } returns Unit

            feedbackService.markFeedbackAsReadOrUnread(id, isRead)

            verify(exactly = 1) {
                feedbackRepository.markFeedbackAsReadOrUnread(id, isRead)
            }
        }
    }
}
