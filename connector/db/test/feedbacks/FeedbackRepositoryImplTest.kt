package feedbacks

import io.kotest.core.spec.Spec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import norm.executeCommand
import scalereal.core.emails.FeedbackReceivedMail
import scalereal.core.emails.RequestFeedbackMail
import scalereal.core.employees.EmployeeRepository
import scalereal.core.feedbacks.FeedbackService
import scalereal.core.models.domain.AddFeedbackData
import scalereal.core.models.domain.AnalyticsFeedbackCount
import scalereal.core.models.domain.CreateFeedbackParams
import scalereal.core.models.domain.FeedbackCounts
import scalereal.core.models.domain.FeedbackData
import scalereal.core.models.domain.Feedbacks
import scalereal.core.slack.RequestFeedbackSlackNotification
import scalereal.core.slack.SlackService
import scalereal.db.feedbacks.FeedbackRepositoryImpl
import scalereal.db.feedbacks.FeedbackRequestRepositoryImpl
import util.StringSpecWithDataSource
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate

class FeedbackRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var feedbackRepositoryImpl: FeedbackRepositoryImpl
    private lateinit var feedbackRequestRepositoryImpl: FeedbackRequestRepositoryImpl
    private lateinit var feedbackService: FeedbackService
    private var feedbackReceivedMail = mockk<FeedbackReceivedMail>()
    private val requestFeedbackMail = mockk<RequestFeedbackMail>()
    private val slackService = mockk<SlackService>()
    private val employeeRepository = mockk<EmployeeRepository>()
    private val requestFeedbackSlackNotification = mockk<RequestFeedbackSlackNotification>()

    init {
        val date = LocalDate.now()
        val now = Timestamp(System.currentTimeMillis())
        "should able to add new feedback" {
            val createFeedbackParams =
                CreateFeedbackParams(
                    feedback =
                        listOf(
                            AddFeedbackData(
                                feedbackTypeId = 3,
                                feedbackText = "Good Work",
                                markdownText = "",
                            ),
                        ),
                    feedbackToId = 1,
                    feedbackFromId = 2,
                    requestId = null,
                    isDraft = false,
                )
            feedbackRepositoryImpl.create(createFeedbackParams) shouldBe Unit
        }

        "should able to add another feedback" {
            val createFeedbackParams =
                CreateFeedbackParams(
                    feedback =
                        listOf(
                            AddFeedbackData(
                                feedbackTypeId = 2,
                                feedbackText = "This is a negative test feedback",
                                markdownText = null,
                            ),
                        ),
                    feedbackToId = 2,
                    feedbackFromId = 3,
                    requestId = null,
                    isDraft = false,
                )
            feedbackRepositoryImpl.create(createFeedbackParams) shouldBe Unit
        }

        "should mark feedback as read when it was previously unread" {
            val feedbackId = 1L
            val organisationId = 1L

            val initialFeedback = feedbackRepositoryImpl.fetchFeedbackById(feedbackId, organisationId)
            initialFeedback.isRead shouldBe false

            feedbackRepositoryImpl.markFeedbackAsReadOrUnread(feedbackId, true)

            val updatedFeedback = feedbackRepositoryImpl.fetchFeedbackById(feedbackId, organisationId)
            updatedFeedback.isRead shouldBe true
        }

        "should mark feedback as unread when it was previously read" {
            val feedbackId = 1L
            val organisationId = 1L

            val initialFeedback = feedbackRepositoryImpl.fetchFeedbackById(feedbackId, organisationId)
            initialFeedback.isRead shouldBe true

            feedbackRepositoryImpl.markFeedbackAsReadOrUnread(feedbackId, false)

            val updatedFeedback = feedbackRepositoryImpl.fetchFeedbackById(feedbackId, organisationId)
            updatedFeedback.isRead shouldBe false
        }

        "should add feedback as special characters" {
            val createFeedbackParams =
                CreateFeedbackParams(
                    feedback =
                        listOf(
                            AddFeedbackData(
                                feedbackTypeId = 1,
                                feedbackText = "~`!@#$%^&*()-_\\=+[]{}|;:'<>,./?",
                                markdownText = null,
                            ),
                        ),
                    feedbackToId = 4,
                    feedbackFromId = 3,
                    requestId = null,
                    isDraft = false,
                )
            feedbackRepositoryImpl.create(createFeedbackParams) shouldBe Unit
        }

        "should add another new feedback" {
            val createFeedbackParams =
                CreateFeedbackParams(
                    feedback =
                        listOf(
                            AddFeedbackData(
                                feedbackTypeId = 1,
                                feedbackText = "Good to see more test cases",
                                markdownText = null,
                            ),
                        ),
                    feedbackToId = 3,
                    feedbackFromId = 1,
                    requestId = null,
                    isDraft = false,
                )
            feedbackRepositoryImpl.create(createFeedbackParams) shouldBe Unit
        }

        "should count and fetch received feedback by employee unique id" {
            val organisationId = 1L
            val feedbackToId = 1L
            val feedbackFromId = listOf(2, 3)
            val feedbackTypeId = listOf(-99)
            val reviewCycleId = listOf(-99)
            val expectedFeedback =
                listOf(
                    FeedbackData(
                        srNo = 6,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0046",
                        empFirstName = "Gaurav",
                        empLastName = "Pakhale",
                        empRoleName = "Employee",
                        externalFeedbackFromEmailId = null,
                        feedback = "Good Work",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                        isDraft = false,
                    ),
                    FeedbackData(
                        srNo = 5,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0046",
                        empFirstName = "Gaurav",
                        empLastName = "Pakhale",
                        empRoleName = "Employee",
                        externalFeedbackFromEmailId = null,
                        feedback = "Test feedback submitted",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                        isDraft = false,
                    ),
                    FeedbackData(
                        srNo = 4,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0046",
                        empFirstName = "Gaurav",
                        empLastName = "Pakhale",
                        empRoleName = "Employee",
                        externalFeedbackFromEmailId = null,
                        feedback = "Test feedback submitted",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                        isDraft = false,
                    ),
                )
            val count =
                feedbackRepositoryImpl.countFeedbacksReceived(
                    organisationId = organisationId,
                    feedbackToId = feedbackToId,
                    feedbackFromId = feedbackFromId,
                    feedbackTypeId = feedbackTypeId,
                    reviewCycleId = reviewCycleId,
                )
            val actualFeedback =
                feedbackRepositoryImpl.fetchAllFeedbacksReceived(
                    organisationId = organisationId,
                    feedbackToId = feedbackToId,
                    feedbackFromId = feedbackFromId,
                    feedbackTypeId = feedbackTypeId,
                    reviewCycleId = reviewCycleId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )

            actualFeedback.size shouldBe count

            assertFeedbackData(expectedFeedback, actualFeedback)
        }

        "should fetch received feedback by employee unique id in asc order" {
            val organisationId = 1L
            val feedbackToId = 1L
            val feedbackFromId = listOf(2, 3)
            val feedbackTypeId = listOf(-99)
            val reviewCycleId = listOf(-99)
            val expectedFeedback =
                listOf(
                    FeedbackData(
                        srNo = 4,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0046",
                        empFirstName = "Gaurav",
                        empLastName = "Pakhale",
                        empRoleName = "Employee",
                        externalFeedbackFromEmailId = null,
                        feedback = "Test feedback submitted",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                        isDraft = false,
                    ),
                    FeedbackData(
                        srNo = 5,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0046",
                        empFirstName = "Gaurav",
                        empLastName = "Pakhale",
                        empRoleName = "Employee",
                        externalFeedbackFromEmailId = null,
                        feedback = "Test feedback submitted",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                        isDraft = false,
                    ),
                    FeedbackData(
                        srNo = 6,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0046",
                        empFirstName = "Gaurav",
                        empLastName = "Pakhale",
                        empRoleName = "Employee",
                        externalFeedbackFromEmailId = null,
                        feedback = "Good Work",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                        isDraft = false,
                    ),
                )

            val actualFeedback =
                feedbackRepositoryImpl.fetchAllFeedbacksReceived(
                    organisationId = organisationId,
                    feedbackToId = feedbackToId,
                    feedbackFromId = feedbackFromId,
                    feedbackTypeId = feedbackTypeId,
                    reviewCycleId = reviewCycleId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateAsc",
                )

            assertFeedbackData(expectedFeedback, actualFeedback)
        }

        "should count and fetch all submitted feedbacks by employee unique id" {
            val organisationId = 1L
            val feedbackFromId = 3L
            val feedbackToId = listOf(-99)
            val feedbackTypeId = listOf(-99)
            val reviewCycleId = listOf(-99)
            val expectedFeedback =
                listOf(
                    FeedbackData(
                        srNo = 8,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 4,
                        feedbackToEmployeeId = "SR0051",
                        feedbackFromId = 3,
                        feedbackFromEmployeeId = "SR0050",
                        empFirstName = "Rushad",
                        empLastName = "Shaikh",
                        empRoleName = "Employee",
                        externalFeedbackFromEmailId = null,
                        feedback = "~`!@#\$%^&*()-_\\=+[]{}|;:'<>,./?",
                        feedbackTypeId = 1,
                        feedbackType = "Positive",
                        isDraft = false,
                    ),
                    FeedbackData(
                        srNo = 7,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        feedbackFromId = 3,
                        feedbackFromEmployeeId = "SR0050",
                        empFirstName = "Gaurav",
                        empLastName = "Pakhale",
                        empRoleName = "Employee",
                        externalFeedbackFromEmailId = null,
                        feedback = "This is a negative test feedback",
                        feedbackTypeId = 2,
                        feedbackType = "Improvement",
                        isDraft = false,
                    ),
                )
            val count =
                feedbackRepositoryImpl.countSubmittedFeedbacks(
                    organisationId = organisationId,
                    feedbackFromId = feedbackFromId,
                    feedbackToId = feedbackToId,
                    feedbackTypeId = feedbackTypeId,
                    reviewCycleId = reviewCycleId,
                )
            val actualFeedback =
                feedbackRepositoryImpl.fetchAllSubmittedFeedbacks(
                    organisationId = organisationId,
                    feedbackFromId = feedbackFromId,
                    feedbackToId = feedbackToId,
                    feedbackTypeId = feedbackTypeId,
                    reviewCycleId = reviewCycleId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )

            actualFeedback.size shouldBe count

            assertFeedbackData(expectedFeedback, actualFeedback)
        }

        "should fetch all submitted feedbacks by employee unique id in asc order" {
            val organisationId = 1L
            val feedbackFromId = 3L
            val feedbackToId = listOf(-99)
            val feedbackTypeId = listOf(-99)
            val reviewCycleId = listOf(-99)
            val expectedFeedback =
                listOf(
                    FeedbackData(
                        srNo = 7,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        feedbackFromId = 3,
                        feedbackFromEmployeeId = "SR0050",
                        empFirstName = "Gaurav",
                        empLastName = "Pakhale",
                        empRoleName = "Employee",
                        externalFeedbackFromEmailId = null,
                        feedback = "This is a negative test feedback",
                        feedbackTypeId = 2,
                        feedbackType = "Improvement",
                        isDraft = false,
                    ),
                    FeedbackData(
                        srNo = 8,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 4,
                        feedbackToEmployeeId = "SR0051",
                        feedbackFromId = 3,
                        feedbackFromEmployeeId = "SR0050",
                        empFirstName = "Rushad",
                        empLastName = "Shaikh",
                        empRoleName = "Employee",
                        externalFeedbackFromEmailId = null,
                        feedback = "~`!@#\$%^&*()-_\\=+[]{}|;:'<>,./?",
                        feedbackTypeId = 1,
                        feedbackType = "Positive",
                        isDraft = false,
                    ),
                )
            val actualFeedback =
                feedbackRepositoryImpl.fetchAllSubmittedFeedbacks(
                    organisationId = organisationId,
                    feedbackFromId = feedbackFromId,
                    feedbackToId = feedbackToId,
                    feedbackTypeId = feedbackTypeId,
                    reviewCycleId = reviewCycleId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateAsc",
                )
            assertFeedbackData(expectedFeedback, actualFeedback)
        }

        "size should be 1 of submitted feedbacks as limit is 1" {
            val feedback =
                feedbackRepositoryImpl.fetchAllSubmittedFeedbacks(
                    organisationId = 1,
                    feedbackFromId = 3,
                    feedbackToId = listOf(-99),
                    feedbackTypeId = listOf(-99),
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = 1,
                    sortBy = "dateDesc",
                )

            feedback.size shouldBe 1
        }

        "should get submitted feedbacks based on selected feedback type" {
            val expectedFeedback =
                listOf(
                    FeedbackData(
                        srNo = 7,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        feedbackFromId = 3,
                        feedbackFromEmployeeId = "SR0050",
                        empFirstName = "Gaurav",
                        empLastName = "Pakhale",
                        empRoleName = "Employee",
                        externalFeedbackFromEmailId = null,
                        feedback = "This is a negative test feedback",
                        feedbackTypeId = 2,
                        feedbackType = "Improvement",
                        isDraft = false,
                    ),
                )
            val actualFeedback =
                feedbackRepositoryImpl.fetchAllSubmittedFeedbacks(
                    organisationId = 1,
                    feedbackFromId = 3,
                    feedbackToId = listOf(-99),
                    feedbackTypeId = listOf(2),
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )

            assertFeedbackData(expectedFeedback, actualFeedback)
        }

        "should get only submitted feedback when it is given on feedback request" {
            val organisationId = 1L
            val feedbackFromId = 2L
            val feedbackToId = listOf(-99)
            val feedbackTypeId = listOf(-99)
            val reviewCycleId = listOf(-99)
            val expectedFeedback =
                listOf(
                    FeedbackData(
                        srNo = 6,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0046",
                        empFirstName = "Ubed",
                        empLastName = "Ali",
                        empRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        feedback = "Good Work",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                        isDraft = false,
                    ),
                    FeedbackData(
                        srNo = 1,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0046",
                        empFirstName = "Ubed",
                        empLastName = "Ali",
                        empRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        feedback = "Test feedback submitted",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                        isDraft = false,
                    ),
                    FeedbackData(
                        srNo = 2,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0046",
                        empFirstName = "Ubed",
                        empLastName = "Ali",
                        empRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        feedback = "Test feedback submitted",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                        isDraft = false,
                    ),
                    FeedbackData(
                        srNo = 7,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        empFirstName = "Gaurav",
                        empLastName = "Pakhale",
                        empRoleName = "Employee",
                        externalFeedbackFromEmailId = null,
                        feedback = "Test feedback submitted on feedback request",
                        feedbackTypeId = 2,
                        feedbackType = "Improvement",
                        isDraft = false,
                    ),
                )
            val count =
                feedbackRepositoryImpl.countSubmittedFeedbacks(
                    organisationId = organisationId,
                    feedbackFromId = feedbackFromId,
                    feedbackToId = feedbackToId,
                    feedbackTypeId = feedbackTypeId,
                    reviewCycleId = reviewCycleId,
                )
            val actualFeedback =
                feedbackRepositoryImpl.fetchAllSubmittedFeedbacks(
                    organisationId = organisationId,
                    feedbackFromId = feedbackFromId,
                    feedbackToId = feedbackToId,
                    feedbackTypeId = feedbackTypeId,
                    reviewCycleId = reviewCycleId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )

            actualFeedback.size shouldBe count

            assertFeedbackData(expectedFeedback, actualFeedback)
        }

        "should get received feedbacks based on selected feedback type" {
            val expectedFeedback =
                listOf(
                    FeedbackData(
                        srNo = 8,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        feedbackToId = 4,
                        feedbackToEmployeeId = "SR0051",
                        feedbackFromId = 3,
                        feedbackFromEmployeeId = "SR0050",
                        empFirstName = "Moly",
                        empLastName = "Agarwal",
                        empRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        feedback = "~`!@#\$%^&*()-_\\=+[]{}|;:'<>,./?",
                        feedbackTypeId = 1,
                        feedbackType = "Positive",
                        isDraft = false,
                    ),
                )
            val actualFeedback =
                feedbackRepositoryImpl.fetchAllFeedbacksReceived(
                    organisationId = 1,
                    feedbackToId = 4,
                    feedbackFromId = listOf(-99),
                    feedbackTypeId = listOf(1, 3),
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )

            assertFeedbackData(expectedFeedback, actualFeedback)
        }

        "should fetch feedback by feedback id" {
            val expectedFeedback =
                FeedbackData(
                    srNo = 9,
                    date = now,
                    organisationId = 1,
                    isExternalFeedback = false,
                    feedbackToId = 2,
                    feedbackToEmployeeId = "SR0046",
                    feedbackFromId = 3,
                    feedbackFromEmployeeId = "SR0050",
                    empFirstName = "Gaurav",
                    empLastName = "Pakhale",
                    empRoleName = "Employee",
                    externalFeedbackFromEmailId = null,
                    feedback = "This is a negative test feedback",
                    feedbackTypeId = 2,
                    feedbackType = "Improvement",
                    isDraft = false,
                )
            val actualFeedback = feedbackRepositoryImpl.fetchFeedbackById(feedbackId = 9, organisationId = 1)

            actualFeedback.srNo shouldBe expectedFeedback.srNo
            actualFeedback.date shouldBeAfter expectedFeedback.date
            actualFeedback.organisationId shouldBe expectedFeedback.organisationId
            actualFeedback.feedbackToId shouldBe expectedFeedback.feedbackToId
            actualFeedback.feedbackToEmployeeId shouldBe expectedFeedback.feedbackToEmployeeId
            actualFeedback.feedbackFromId shouldBe expectedFeedback.feedbackFromId
            actualFeedback.feedbackFromEmployeeId shouldBe expectedFeedback.feedbackFromEmployeeId
            actualFeedback.empFirstName shouldBe expectedFeedback.empFirstName
            actualFeedback.empLastName shouldBe expectedFeedback.empLastName
            actualFeedback.empRoleName shouldBe expectedFeedback.empRoleName
            actualFeedback.feedback shouldBe expectedFeedback.feedback
            actualFeedback.feedbackTypeId shouldBe expectedFeedback.feedbackTypeId
            actualFeedback.feedbackType shouldBe expectedFeedback.feedbackType
            actualFeedback.isDraft shouldBe expectedFeedback.isDraft
            actualFeedback.isExternalFeedback shouldBe expectedFeedback.isExternalFeedback
            actualFeedback.externalFeedbackFromEmailId shouldBe expectedFeedback.externalFeedbackFromEmailId
        }

        "should able to add feedback in draft" {
            val createFeedbackParams =
                CreateFeedbackParams(
                    feedback =
                        listOf(
                            AddFeedbackData(
                                feedbackTypeId = 2,
                                feedbackText = "Improvement feedback saved in draft",
                                markdownText = null,
                            ),
                        ),
                    feedbackToId = 1,
                    feedbackFromId = 2,
                    requestId = null,
                    isDraft = true,
                )
            feedbackRepositoryImpl.create(createFeedbackParams) shouldBe Unit
        }

        "should able to edit feedback" {
            val feedbacks =
                feedbackRepositoryImpl.update(
                    feedbackId = 12,
                    feedback = "Publish Feedback after changing feedback type",
                    feedbackToId = 1,
                    feedbackTypeId = 1,
                    requestId = null,
                    isDraft = false,
                )

            feedbacks.id shouldBe 12
            feedbacks.feedback shouldBe "Publish Feedback after changing feedback type"
            feedbacks.feedbackToId shouldBe 1
            feedbacks.feedbackFromId shouldBe 2
            feedbacks.feedbackTypeId shouldBe 1
            feedbacks.isDraft shouldBe false
        }

        "should count and fetch all feedback while searching by employee name" {
            val organisationId = 1L
            val searchText = "Moly"
            val feedbackTypeId = listOf(-99)
            val fromDate = "2023-01-01"
            val toDate = date.toString()
            val reviewCycleId = listOf(-99)

            val expectedFeedback =
                listOf(
                    Feedbacks(
                        date = Date.valueOf(date.toString()),
                        isExternalFeedback = false,
                        feedback = "Good to see more test cases",
                        feedbackToId = 3,
                        feedbackToEmployeeId = "SR0050",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        feedbackTypeId = 1,
                        feedbackType = "Positive",
                        toEmpName = "Moly Agarwal",
                        fromEmpName = "Ubed Ali",
                        toRoleName = "Manager",
                        fromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        organisationId = 1,
                        isDraft = false,
                    ),
                    Feedbacks(
                        date = Date.valueOf(date.toString()),
                        isExternalFeedback = false,
                        feedback = "~`!@#\$%^&*()-_\\=+[]{}|;:'<>,./?",
                        feedbackToId = 4,
                        feedbackToEmployeeId = "SR0051",
                        feedbackFromId = 3,
                        feedbackFromEmployeeId = "SR0050",
                        feedbackTypeId = 1,
                        feedbackType = "Positive",
                        toEmpName = "Rushad Shaikh",
                        fromEmpName = "Moly Agarwal",
                        toRoleName = "Employee",
                        fromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        organisationId = 1,
                        isDraft = false,
                    ),
                    Feedbacks(
                        date = Date.valueOf(date.toString()),
                        isExternalFeedback = false,
                        feedback = "This is a negative test feedback",
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        feedbackFromId = 3,
                        feedbackFromEmployeeId = "SR0050",
                        feedbackTypeId = 2,
                        feedbackType = "Improvement",
                        toEmpName = "Gaurav Pakhale",
                        fromEmpName = "Moly Agarwal",
                        toRoleName = "Employee",
                        fromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        organisationId = 1,
                        isDraft = false,
                    ),
                    Feedbacks(
                        date = Date.valueOf("2023-05-12"),
                        isExternalFeedback = false,
                        feedback = "Test appreciation feedback submitted",
                        feedbackToId = 3,
                        feedbackToEmployeeId = "SR0050",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                        toEmpName = "Moly Agarwal",
                        fromEmpName = "Ubed Ali",
                        toRoleName = "Manager",
                        fromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        organisationId = 1,
                        isDraft = false,
                    ),
                )

            val count =
                feedbackRepositoryImpl.countAllFeedbacks(
                    organisationId = organisationId,
                    searchText = searchText,
                    feedbackTypeId = feedbackTypeId,
                    fromDate = fromDate,
                    toDate = toDate,
                    reviewCycleId = reviewCycleId,
                )
            val actualFeedback =
                feedbackRepositoryImpl.fetchAllFeedbacks(
                    organisationId = organisationId,
                    searchText = searchText,
                    feedbackTypeId = feedbackTypeId,
                    fromDate = fromDate,
                    toDate = toDate,
                    reviewCycleId = reviewCycleId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            actualFeedback.size shouldBe count
            count shouldBe expectedFeedback.size
            assertFeedbacks(expectedFeedback, actualFeedback)
        }

        "should fetch all feedback while searching by employee ID" {

            val organisationId = 1L
            val searchText = "SR0050"
            val feedbackTypeId = listOf(-99)
            val fromDate = "2023-01-01"
            val toDate = date.toString()
            val reviewCycleId = listOf(-99)

            val expectedFeedback =
                listOf(
                    Feedbacks(
                        date = Date.valueOf(date.toString()),
                        isExternalFeedback = false,
                        feedback = "Good to see more test cases",
                        feedbackToId = 3,
                        feedbackToEmployeeId = "SR0050",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        feedbackTypeId = 1,
                        feedbackType = "Positive",
                        toEmpName = "Moly Agarwal",
                        fromEmpName = "Ubed Ali",
                        toRoleName = "Manager",
                        fromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        organisationId = 1,
                        isDraft = false,
                    ),
                    Feedbacks(
                        date = Date.valueOf(date.toString()),
                        isExternalFeedback = false,
                        feedback = "~`!@#\$%^&*()-_\\=+[]{}|;:'<>,./?",
                        feedbackToId = 4,
                        feedbackToEmployeeId = "SR0051",
                        feedbackFromId = 3,
                        feedbackFromEmployeeId = "SR0050",
                        feedbackTypeId = 1,
                        feedbackType = "Positive",
                        toEmpName = "Rushad Shaikh",
                        fromEmpName = "Moly Agarwal",
                        toRoleName = "Employee",
                        fromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        organisationId = 1,
                        isDraft = false,
                    ),
                    Feedbacks(
                        date = Date.valueOf(date.toString()),
                        isExternalFeedback = false,
                        feedback = "This is a negative test feedback",
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        feedbackFromId = 3,
                        feedbackFromEmployeeId = "SR0050",
                        feedbackTypeId = 2,
                        feedbackType = "Improvement",
                        toEmpName = "Gaurav Pakhale",
                        fromEmpName = "Moly Agarwal",
                        toRoleName = "Employee",
                        fromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        organisationId = 1,
                        isDraft = false,
                    ),
                    Feedbacks(
                        date = Date.valueOf("2023-05-12"),
                        isExternalFeedback = false,
                        feedback = "Test appreciation feedback submitted",
                        feedbackToId = 3,
                        feedbackToEmployeeId = "SR0050",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                        toEmpName = "Moly Agarwal",
                        fromEmpName = "Ubed Ali",
                        toRoleName = "Manager",
                        fromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        organisationId = 1,
                        isDraft = false,
                    ),
                )

            val actualFeedback =
                feedbackRepositoryImpl.fetchAllFeedbacks(
                    organisationId = organisationId,
                    searchText = searchText,
                    feedbackTypeId = feedbackTypeId,
                    fromDate = fromDate,
                    toDate = toDate,
                    reviewCycleId = reviewCycleId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )
            actualFeedback.size shouldBe expectedFeedback.size
            assertFeedbacks(expectedFeedback, actualFeedback)
        }

        "should fetch all feedback while searching by employee ID and feedback type filter" {
            val expectedFeedback =
                listOf(
                    Feedbacks(
                        date = Date.valueOf(date.toString()),
                        isExternalFeedback = false,
                        feedback = "This is a negative test feedback",
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        feedbackFromId = 3,
                        feedbackFromEmployeeId = "SR0050",
                        feedbackTypeId = 2,
                        feedbackType = "Improvement",
                        toEmpName = "Gaurav Pakhale",
                        fromEmpName = "Moly Agarwal",
                        toRoleName = "Employee",
                        fromRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        organisationId = 1,
                        isDraft = false,
                    ),
                )
            val actualFeedback =
                feedbackRepositoryImpl.fetchAllFeedbacks(
                    organisationId = 1,
                    searchText = "SR0050",
                    feedbackTypeId = listOf(2),
                    fromDate = null,
                    toDate = null,
                    reviewCycleId = listOf(-99),
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )

            assertFeedbacks(expectedFeedback, actualFeedback)
        }

        "should fetch count of different types of feedback submitted and received by employee between specific dates" {
            val expectedCount =
                FeedbackCounts(
                    submittedPositiveCount = 1,
                    submittedImprovementCount = 1,
                    submittedAppreciationCount = 2,
                    receivedPositiveCount = 1,
                    receivedImprovementCount = 0,
                    receivedAppreciationCount = 2,
                )
            val actualCount =
                feedbackRepositoryImpl.fetchEmployeeFeedbackCounts(
                    id = 1,
                    startDate = Date.valueOf("2023-4-1"),
                    endDate = Date.valueOf("2023-9-30"),
                )
            actualCount shouldBe expectedCount
        }

        "should fetch count of different types of feedback given in an organisation between specific dates" {
            val expectedCount =
                AnalyticsFeedbackCount(
                    positive = 4,
                    improvement = 2,
                    appreciation = 5,
                )
            val actualCount =
                feedbackRepositoryImpl.fetchTotalFeedbackCounts(
                    organisationId = 1,
                    startDate = Date.valueOf("2023-4-1"),
                    endDate = Date.valueOf(date.toString()),
                )
            actualCount shouldBe expectedCount
        }

        "should add external feedback request" {
            val feedbackFromId =
                feedbackRequestRepositoryImpl.addExternalEmails(
                    feedbackFromEmail = listOf("your_email@example.com"),
                    organisationId = 1,
                )
            feedbackRequestRepositoryImpl.addExternalFeedbackRequest(
                requestedBy = 1L,
                feedbackToId = listOf(2),
                feedbackFromId = feedbackFromId,
                request = "Please give feedback for employee",
                isExternalRequest = true,
            ) shouldBe listOf(3)
        }

        "should add external feedback" {
            val feedback =
                listOf(
                    AddFeedbackData(
                        feedbackTypeId = 1,
                        feedbackText = "External Positive feedback",
                    ),
                    AddFeedbackData(
                        feedbackTypeId = 2,
                        feedbackText = "External Improvement feedback",
                    ),
                    AddFeedbackData(
                        feedbackTypeId = 3,
                        feedbackText = "External Appreciation feedback",
                    ),
                )
            feedbackRequestRepositoryImpl.addExternalFeedback(
                feedback = feedback,
                feedbackToId = 2,
                feedbackFromId = 1,
                requestId = 1,
            ) shouldBe Unit
        }

        "should also count and fetch external feedback in received feedback list" {
            val organisationId = 1L
            val feedbackToId = 2L
            val feedbackFromId = listOf(-99)
            val feedbackTypeId = listOf(-99)
            val reviewCycleId = listOf(-99)
            val expectedFeedback =
                listOf(
                    FeedbackData(
                        srNo = 13,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = true,
                        isDraft = false,
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        feedbackFromId = null,
                        feedbackFromEmployeeId = null,
                        empFirstName = null,
                        empLastName = null,
                        empRoleName = null,
                        externalFeedbackFromEmailId = "your_email@example.com",
                        feedback = "External Appreciation feedback",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                    ),
                    FeedbackData(
                        srNo = 12,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = true,
                        isDraft = false,
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        feedbackFromId = null,
                        feedbackFromEmployeeId = null,
                        empFirstName = null,
                        empLastName = null,
                        empRoleName = null,
                        externalFeedbackFromEmailId = "your_email@example.com",
                        feedback = "External Improvement feedback",
                        feedbackTypeId = 2,
                        feedbackType = "Improvement",
                    ),
                    FeedbackData(
                        srNo = 11,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = true,
                        isDraft = false,
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        feedbackFromId = null,
                        feedbackFromEmployeeId = null,
                        empFirstName = null,
                        empLastName = null,
                        empRoleName = null,
                        externalFeedbackFromEmailId = "your_email@example.com",
                        feedback = "External Positive feedback",
                        feedbackTypeId = 1,
                        feedbackType = "Positive",
                    ),
                    FeedbackData(
                        srNo = 7,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        isDraft = false,
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        feedbackFromId = 3,
                        feedbackFromEmployeeId = "SR0050",
                        empFirstName = "Moly",
                        empLastName = "Agarwal",
                        empRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        feedback = "This is a negative test feedback",
                        feedbackTypeId = 2,
                        feedbackType = "Improvement",
                    ),
                    FeedbackData(
                        srNo = 14,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        isDraft = false,
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        empFirstName = "Ubed",
                        empLastName = "Ali",
                        empRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        feedback = "Test feedback submitted on feedback request",
                        feedbackTypeId = 2,
                        feedbackType = "Improvement",
                    ),
                    FeedbackData(
                        srNo = 1,
                        date = now,
                        organisationId = 1,
                        isExternalFeedback = false,
                        isDraft = false,
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        empFirstName = "Ubed",
                        empLastName = "Ali",
                        empRoleName = "Manager",
                        externalFeedbackFromEmailId = null,
                        feedback = "Test appreciation feedback submitted",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                    ),
                )

            val feedbackCount =
                feedbackRepositoryImpl.countFeedbacksReceived(
                    organisationId = organisationId,
                    feedbackToId = feedbackToId,
                    feedbackFromId = feedbackFromId,
                    feedbackTypeId = feedbackTypeId,
                    reviewCycleId = reviewCycleId,
                )
            val actualFeedback =
                feedbackRepositoryImpl.fetchAllFeedbacksReceived(
                    organisationId = organisationId,
                    feedbackToId = feedbackToId,
                    feedbackFromId = feedbackFromId,
                    feedbackTypeId = feedbackTypeId,
                    reviewCycleId = reviewCycleId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )

            actualFeedback.size shouldBe feedbackCount
            feedbackCount shouldBe expectedFeedback.size
            assertFeedbackData(expectedFeedback, actualFeedback)
        }

        "should fetch feedback submitted by external entity to employee while fetching all feedbacks" {
            val organisationId = 1L
            val searchText = "your_email"
            val feedbackTypeId = listOf(-99)
            val fromDate = null
            val toDate = null
            val reviewCycleId = listOf(-99)
            val expectedFeedback =
                listOf(
                    Feedbacks(
                        date = Date.valueOf(date.toString()),
                        organisationId = 1,
                        isExternalFeedback = true,
                        isDraft = false,
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        toEmpName = "Gaurav Pakhale",
                        toRoleName = "Employee",
                        feedbackFromId = null,
                        feedbackFromEmployeeId = null,
                        fromEmpName = null,
                        fromRoleName = null,
                        externalFeedbackFromEmailId = "your_email@example.com",
                        feedback = "External Appreciation feedback",
                        feedbackTypeId = 3,
                        feedbackType = "Appreciation",
                    ),
                    Feedbacks(
                        date = Date.valueOf(date.toString()),
                        organisationId = 1,
                        isExternalFeedback = true,
                        isDraft = false,
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        toEmpName = "Gaurav Pakhale",
                        toRoleName = "Employee",
                        feedbackFromId = null,
                        feedbackFromEmployeeId = null,
                        fromEmpName = null,
                        fromRoleName = null,
                        externalFeedbackFromEmailId = "your_email@example.com",
                        feedback = "External Improvement feedback",
                        feedbackTypeId = 2,
                        feedbackType = "Improvement",
                    ),
                    Feedbacks(
                        date = Date.valueOf(date.toString()),
                        organisationId = 1,
                        isExternalFeedback = true,
                        isDraft = false,
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0046",
                        toEmpName = "Gaurav Pakhale",
                        toRoleName = "Employee",
                        feedbackFromId = null,
                        feedbackFromEmployeeId = null,
                        fromEmpName = null,
                        fromRoleName = null,
                        externalFeedbackFromEmailId = "your_email@example.com",
                        feedback = "External Positive feedback",
                        feedbackTypeId = 1,
                        feedbackType = "Positive",
                    ),
                )

            val feedbackCount =
                feedbackRepositoryImpl.countAllFeedbacks(
                    organisationId = organisationId,
                    searchText = searchText,
                    feedbackTypeId = feedbackTypeId,
                    fromDate = fromDate,
                    toDate = toDate,
                    reviewCycleId = reviewCycleId,
                )
            val actualFeedback =
                feedbackRepositoryImpl.fetchAllFeedbacks(
                    organisationId = organisationId,
                    searchText = searchText,
                    feedbackTypeId = feedbackTypeId,
                    fromDate = fromDate,
                    toDate = toDate,
                    reviewCycleId = reviewCycleId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    sortBy = "dateDesc",
                )

            actualFeedback.size shouldBe feedbackCount // 3
            feedbackCount shouldBe expectedFeedback.size
            assertFeedbacks(expectedFeedback, actualFeedback)
        }
    }

    private fun assertFeedbackData(
        expected: List<FeedbackData>,
        actual: List<FeedbackData>,
    ) {
        actual.mapIndexed { index, actualFeedback ->
            val expectedFeedback = expected[index]

            actualFeedback.organisationId shouldBe expectedFeedback.organisationId
            actualFeedback.feedbackToId shouldBe expectedFeedback.feedbackToId
            actualFeedback.feedbackToEmployeeId shouldBe expectedFeedback.feedbackToEmployeeId
            actualFeedback.feedbackFromId shouldBe expectedFeedback.feedbackFromId
            actualFeedback.feedbackFromEmployeeId shouldBe expectedFeedback.feedbackFromEmployeeId
            actualFeedback.empFirstName shouldBe expectedFeedback.empFirstName
            actualFeedback.empLastName shouldBe expectedFeedback.empLastName
            actualFeedback.empRoleName shouldBe expectedFeedback.empRoleName
            actualFeedback.feedback shouldBe expectedFeedback.feedback
            actualFeedback.feedbackTypeId shouldBe expectedFeedback.feedbackTypeId
            actualFeedback.feedbackType shouldBe expectedFeedback.feedbackType
            actualFeedback.isDraft shouldBe expectedFeedback.isDraft
            actualFeedback.isExternalFeedback shouldBe expectedFeedback.isExternalFeedback
            actualFeedback.externalFeedbackFromEmailId shouldBe expectedFeedback.externalFeedbackFromEmailId
        }
    }

    private fun assertFeedbacks(
        expected: List<Feedbacks>,
        actual: List<Feedbacks>,
    ) {
        actual.mapIndexed { index, actualFeedback ->
            val expectedFeedback = expected[index]

            actualFeedback.date shouldBe expectedFeedback.date
            actualFeedback.isExternalFeedback shouldBe expectedFeedback.isExternalFeedback
            actualFeedback.feedback shouldBe expectedFeedback.feedback
            actualFeedback.feedbackToId shouldBe expectedFeedback.feedbackToId
            actualFeedback.feedbackToEmployeeId shouldBe expectedFeedback.feedbackToEmployeeId
            actualFeedback.feedbackFromId shouldBe expectedFeedback.feedbackFromId
            actualFeedback.feedbackFromEmployeeId shouldBe expectedFeedback.feedbackFromEmployeeId
            actualFeedback.feedbackTypeId shouldBe expectedFeedback.feedbackTypeId
            actualFeedback.feedbackType shouldBe expectedFeedback.feedbackType
            actualFeedback.toEmpName shouldBe expectedFeedback.toEmpName
            actualFeedback.fromEmpName shouldBe expectedFeedback.fromEmpName
            actualFeedback.toRoleName shouldBe expectedFeedback.toRoleName
            actualFeedback.fromRoleName shouldBe expectedFeedback.fromRoleName
            actualFeedback.externalFeedbackFromEmailId shouldBe expectedFeedback.externalFeedbackFromEmailId
            actualFeedback.organisationId shouldBe expectedFeedback.organisationId
            actualFeedback.isDraft shouldBe expectedFeedback.isDraft
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        dataSource.connection.use {
            it.executeCommand(
                """
                INSERT INTO organisations(sr_no, admin_id, name, is_active, organisation_size)
                VALUES (1, 1, 'Scalereal', true, 50);

                INSERT INTO employees(id, emp_id, first_name, last_name, email_id, contact_no, status, organisation_id)
                 VALUES
                (1, 'SR0043', 'Ubed', 'Ali', 'ubed@gmail.com', 6262209099, true, 1),
                (2, 'SR0046', 'Gaurav', 'Pakhale', 'gaurav@gmail.com', 9999999999, true, 1),
                (3, 'SR0050', 'Moly', 'Agarwal', 'moly@gmail.com', 8576964227, true, 1),
                (4, 'SR0051', 'Rushad', 'Shaikh', 'rushad@gmail.com', 8888888888, true, 1);

                INSERT INTO feedback_types(name)
                VALUES('Positive'),
                ('Improvement'),
                ('Appreciation');

                INSERT INTO roles(organisation_id, role_id, role_name, status) values(1, 1, 'Manager','true'),
                (1, 2, 'Employee','true');

                INSERT INTO employees_role_mapping (emp_id,role_id) values(1,1),
                (2,2),(3, 1),(4, 2);

                INSERT INTO feedback_request(requested_by, feedback_to, feedback_from, request, is_submitted, goal_id)
                VALUES (1, 1, 2, 'Please submit feedback for me based on my performance in PTA', false, null),
                       (2, 2, 1, 'Please give me feedback on how i communicate with client', true, null);

                 INSERT INTO feedbacks(created_at, feedback, feedback_to, feedback_from, feedback_type_id, request_id, is_draft, updated_at)
                 VALUES
                 ('2023-5-10 19:07:33.164443+05:30', 'Test appreciation feedback submitted', 2, 1, 3, null, 'false', '2023-5-10 19:07:33.164443+05:30'),
                 ('2023-05-12 19:07:33.164443+05:30', 'Test appreciation feedback submitted', 3, 1, 3, null, 'false', '2023-05-12 19:07:33.164443+05:30'),
                 ('2023-05-12 19:07:33.164443+05:30', 'Test feedback received', 1, 1, 1, null, 'false', '2023-05-12 19:07:33.164443+05:30'),
                 ('2023-05-15 19:07:33.164443+05:30', 'Test feedback submitted', 1, 2, 3, null, 'false', '2023-05-15 19:07:33.164443+05:30'),
                 ('2023-05-16 19:07:33.164443+05:30', 'Test feedback submitted', 1, 2, 3, null, 'false', '2023-05-16 19:07:33.164443+05:30'),
                 ('2023-05-17 19:07:33.164443+05:30', 'Test feedback submitted in draft on feedback request', 1, 2, 3, 1, 'true', '2023-05-17 19:07:33.164443+05:30'),
                 ('2023-05-17 19:07:33.164443+05:30', 'Test feedback submitted on feedback request', 2, 1, 2, 2, 'false', '2023-05-17 19:07:33.164443+05:30');
                """.trimIndent(),
            )
        }
        feedbackRepositoryImpl = FeedbackRepositoryImpl(dataSource)
        feedbackRequestRepositoryImpl = FeedbackRequestRepositoryImpl(dataSource)
        feedbackService =
            FeedbackService(
                feedbackRepositoryImpl,
                feedbackRequestRepositoryImpl,
                feedbackReceivedMail,
                requestFeedbackMail,
                slackService,
                employeeRepository,
                requestFeedbackSlackNotification,
            )
    }
}
