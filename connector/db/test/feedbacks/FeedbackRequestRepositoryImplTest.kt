package feedbacks

import io.kotest.core.spec.Spec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import norm.executeCommand
import scalereal.core.models.domain.AddFeedbackData
import scalereal.core.models.domain.ExternalFeedbackRequestData
import scalereal.core.models.domain.FeedbackRequest
import scalereal.core.models.domain.FeedbackRequestData
import scalereal.core.models.domain.FeedbackRequestParams
import scalereal.core.models.domain.PendingFeedbackRequestDetails
import scalereal.db.feedbacks.FeedbackRequestRepositoryImpl
import util.StringSpecWithDataSource
import java.io.File
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate

class FeedbackRequestRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var feedbackRequestRepositoryImpl: FeedbackRequestRepositoryImpl
    private val now = Timestamp(System.currentTimeMillis())

    init {

        "should count and fetch all feedback request data" {
            val feedbackRequestParams =
                FeedbackRequestParams(
                    organisationId = 1,
                    requestedById = listOf(-99),
                    feedbackFromId = listOf(-99),
                    feedbackToId = listOf(-99),
                    reviewCycleId = listOf(-99),
                    isSubmitted = listOf("true", "false"),
                    sortBy = "dateAsc",
                )
            val expectedFeedbackRequest =
                listOf(
                    FeedbackRequestData(
                        organisationId = 1,
                        requestId = 1,
                        isSubmitted = true,
                        requestedById = 1,
                        requestedByEmployeeId = "SR0043",
                        requestedByFirstName = "Syed Ubed",
                        requestedByLastName = "Ali",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0050",
                        feedbackFromFirstName = "Moly",
                        feedbackFromLastName = "Agarwal",
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackToFirstName = "Syed Ubed",
                        feedbackToLastName = "Ali",
                        isExternalRequest = false,
                        requestedOn = now,
                        externalFeedbackFromEmail = null,
                        isDraft = false,
                    ),
                    FeedbackRequestData(
                        organisationId = 1,
                        requestId = 2,
                        isSubmitted = false,
                        requestedById = 3,
                        requestedByEmployeeId = "SR0035",
                        requestedByFirstName = "Twinkle",
                        requestedByLastName = "Dahiya",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        feedbackFromFirstName = "Syed Ubed",
                        feedbackFromLastName = "Ali",
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0050",
                        feedbackToFirstName = "Moly",
                        feedbackToLastName = "Agarwal",
                        isExternalRequest = false,
                        requestedOn = now,
                        externalFeedbackFromEmail = null,
                        isDraft = false,
                    ),
                    FeedbackRequestData(
                        organisationId = 1,
                        requestId = 3,
                        isSubmitted = false,
                        requestedById = 3,
                        requestedByEmployeeId = "SR0035",
                        requestedByFirstName = "Twinkle",
                        requestedByLastName = "Dahiya",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        feedbackFromFirstName = "Syed Ubed",
                        feedbackFromLastName = "Ali",
                        feedbackToId = 4,
                        feedbackToEmployeeId = "SR0051",
                        feedbackToFirstName = "Rushad",
                        feedbackToLastName = "Shaikh",
                        isExternalRequest = false,
                        requestedOn = now,
                        externalFeedbackFromEmail = null,
                        isDraft = false,
                    ),
                )

            val countFeedbackRequest =
                feedbackRequestRepositoryImpl.countFeedbackRequestData(
                    feedbackRequestParams,
                )
            val actualFeedbackRequest =
                feedbackRequestRepositoryImpl.fetchFeedbackRequestData(
                    feedbackRequestParams,
                    offset = 0,
                    limit = 10,
                )
            actualFeedbackRequest.size shouldBe countFeedbackRequest
            assertFeedbackRequestData(expectedFeedbackRequest, actualFeedbackRequest)
        }

        "should be able to get all pending feedback request by employee id" {
            val feedbackRequestParams =
                FeedbackRequestParams(
                    organisationId = 1,
                    requestedById = listOf(-99),
                    feedbackFromId = listOf(1),
                    feedbackToId = listOf(-99),
                    reviewCycleId = listOf(-99),
                    isSubmitted = listOf("false"),
                    sortBy = "dateDesc",
                )
            val expectedFeedbackRequest =
                listOf(
                    FeedbackRequestData(
                        organisationId = 1,
                        requestId = 2,
                        isSubmitted = false,
                        requestedById = 3,
                        requestedByEmployeeId = "SR0035",
                        requestedByFirstName = "Twinkle",
                        requestedByLastName = "Dahiya",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        feedbackFromFirstName = "Syed Ubed",
                        feedbackFromLastName = "Ali",
                        feedbackToId = 2,
                        feedbackToEmployeeId = "SR0050",
                        feedbackToFirstName = "Moly",
                        feedbackToLastName = "Agarwal",
                        isExternalRequest = false,
                        requestedOn = now,
                        externalFeedbackFromEmail = null,
                        isDraft = false,
                    ),
                    FeedbackRequestData(
                        organisationId = 1,
                        requestId = 3,
                        isSubmitted = false,
                        requestedById = 3,
                        requestedByEmployeeId = "SR0035",
                        requestedByFirstName = "Twinkle",
                        requestedByLastName = "Dahiya",
                        feedbackFromId = 1,
                        feedbackFromEmployeeId = "SR0043",
                        feedbackFromFirstName = "Syed Ubed",
                        feedbackFromLastName = "Ali",
                        feedbackToId = 4,
                        feedbackToEmployeeId = "SR0051",
                        feedbackToFirstName = "Rushad",
                        feedbackToLastName = "Shaikh",
                        isExternalRequest = false,
                        requestedOn = now,
                        externalFeedbackFromEmail = null,
                        isDraft = false,
                    ),
                )
            val actualFeedbackRequest =
                feedbackRequestRepositoryImpl.fetchFeedbackRequestData(
                    feedbackRequestParams,
                    offset = 0,
                    limit = 10,
                )
            assertFeedbackRequestData(expectedFeedbackRequest, actualFeedbackRequest)
        }

        "should return empty list when there is no pending feedback request for an employee" {
            val feedbackRequestParams =
                FeedbackRequestParams(
                    organisationId = 1,
                    requestedById = listOf(-99),
                    feedbackFromId = listOf(2),
                    feedbackToId = listOf(-99),
                    reviewCycleId = listOf(-99),
                    isSubmitted = listOf("false"),
                    sortBy = "dateDesc",
                )
            feedbackRequestRepositoryImpl.fetchFeedbackRequestData(
                feedbackRequestParams,
                offset = 0,
                limit = 10,
            ) shouldBe emptyList()
        }

        "should be able to fetch all completed feedback request" {
            val feedbackRequestParams =
                FeedbackRequestParams(
                    organisationId = 1,
                    requestedById = listOf(-99),
                    feedbackFromId = listOf(-99),
                    feedbackToId = listOf(-99),
                    reviewCycleId = listOf(-99),
                    isSubmitted = listOf("true"),
                    sortBy = "dateDesc",
                )
            val expectedFeedbackRequest =
                listOf(
                    FeedbackRequestData(
                        organisationId = 1,
                        requestId = 1,
                        isSubmitted = true,
                        requestedById = 1,
                        requestedByEmployeeId = "SR0043",
                        requestedByFirstName = "Syed Ubed",
                        requestedByLastName = "Ali",
                        feedbackFromId = 2,
                        feedbackFromEmployeeId = "SR0050",
                        feedbackFromFirstName = "Moly",
                        feedbackFromLastName = "Agarwal",
                        feedbackToId = 1,
                        feedbackToEmployeeId = "SR0043",
                        feedbackToFirstName = "Syed Ubed",
                        feedbackToLastName = "Ali",
                        isExternalRequest = false,
                        requestedOn = now,
                        externalFeedbackFromEmail = null,
                        isDraft = false,
                    ),
                )
            val actualFeedbackRequest =
                feedbackRequestRepositoryImpl.fetchFeedbackRequestData(
                    feedbackRequestParams,
                    offset = 0,
                    limit = 10,
                )
            assertFeedbackRequestData(expectedFeedbackRequest, actualFeedbackRequest)
        }

        "should add internal feedback request" {
            val requestedBy = 1L
            val feedbackTo = listOf(2L)
            val feedbackFrom = listOf(3L)
            val request = "Internal feedback request"
            val goalId = null
            feedbackRequestRepositoryImpl.addInternalFeedbackRequest(
                requestedBy = requestedBy,
                feedbackToId = feedbackTo,
                feedbackFromId = feedbackFrom,
                goalId = goalId,
                request = request,
            ) shouldBe Unit
        }

        "should be able to add feedback request on an goal" {
            val requestedBy = 2L
            val feedbackTo = listOf(2L)
            val feedbackFrom = listOf(1L)
            val request = "Please provide feedback on goal"
            val goalId = 1L
            feedbackRequestRepositoryImpl.addInternalFeedbackRequest(
                requestedBy = requestedBy,
                feedbackToId = feedbackTo,
                feedbackFromId = feedbackFrom,
                goalId = goalId,
                request = request,
            ) shouldBe Unit
        }

        "should be able to add external email for giving feedback request" {
            feedbackRequestRepositoryImpl.addExternalEmails(
                feedbackFromEmail = listOf("your_email@example.com"),
                organisationId = 1,
            ) shouldBe listOf(1L)
        }

        "should add external feedback request" {
            feedbackRequestRepositoryImpl.addExternalFeedbackRequest(
                requestedBy = 1L,
                feedbackToId = listOf(2, 3),
                feedbackFromId = listOf(1),
                request = "Please give feedback for employee",
                isExternalRequest = true,
            ) shouldBe listOf(6L, 7L)
        }

        "should get external feedback request data" {
            val expectedFeedbackRequest =
                ExternalFeedbackRequestData(
                    requestId = 6,
                    request = "Please give feedback for employee",
                    requestedById = 1,
                    requestedByFirstName = "Syed Ubed",
                    requestedByLastName = "Ali",
                    feedbackToId = 2,
                    feedbackToFirstName = "Moly",
                    feedbackToLastName = "Agarwal",
                    feedbackToTeam = "Backend",
                    feedbackFromId = 1,
                    feedbackFromEmail = "your_email@example.com",
                    organisationName = "ScaleReal",
                )
            val actualFeedbackRequest = feedbackRequestRepositoryImpl.getExternalFeedbackRequestData(6)
            actualFeedbackRequest shouldBe expectedFeedbackRequest
        }

        "should return null when there is no available external feedback request" {
            feedbackRequestRepositoryImpl.getExternalFeedbackRequestData(requestId = 8) shouldBe null
        }

        "should be to update isSubmitted value as true by feedback request id" {
            feedbackRequestRepositoryImpl.updateFeedbackRequestStatus(requestId = 6) shouldBe Unit
        }

        "should be able to fetch feedback request details by feedback request id" {
            val expectedDetails =
                FeedbackRequest(
                    id = 5,
                    requestedById = 2,
                    feedbackToId = 2,
                    feedbackFromId = 1,
                    goalId = 1,
                    goalDescription = "Please improve your coding skills",
                    request = "Please provide feedback on goal",
                    createdAt = now,
                    isSubmitted = false,
                    requestedByEmployeeId = "SR0050",
                    requestedByFirstName = "Moly",
                    requestedByLastName = "Agarwal",
                    feedbackToEmployeeId = "SR0050",
                    feedbackToFirstName = "Moly",
                    feedbackToLastName = "Agarwal",
                    feedbackFromEmployeeId = "SR0043",
                    feedbackFromFirstName = "Syed Ubed",
                    feedbackFromLastName = "Ali",
                    isExternalRequest = false,
                    externalFeedbackFromEmail = null,
                )
            val actualDetails = feedbackRequestRepositoryImpl.fetchFeedbackRequestDetails(requestId = 5)
            actualDetails.id shouldBe expectedDetails.id
            actualDetails.requestedById shouldBe expectedDetails.requestedById
            actualDetails.feedbackToId shouldBe expectedDetails.feedbackToId
            actualDetails.feedbackFromId shouldBe expectedDetails.feedbackFromId
            actualDetails.goalId shouldBe expectedDetails.goalId
            actualDetails.request shouldBe expectedDetails.request
            actualDetails.createdAt shouldBeAfter expectedDetails.createdAt
            actualDetails.isSubmitted shouldBe expectedDetails.isSubmitted
        }

        "should be able to add external feedback" {
            val feedback =
                listOf(
                    AddFeedbackData(
                        feedbackTypeId = 2,
                        feedbackText = "External improvement feedback",
                    ),
                )
            val feedbackToId = 3L
            val feedbackFromId = 1L
            val requestId = 7L
            feedbackRequestRepositoryImpl.addExternalFeedback(
                feedback = feedback,
                feedbackToId = feedbackToId,
                feedbackFromId = feedbackFromId,
                requestId = requestId,
            ) shouldBe Unit
        }

        "should be able to fetch all pending feedback request" {
            val expectedPendingRequest =
                listOf(
                    PendingFeedbackRequestDetails(
                        id = 2,
                        isExternalRequest = false,
                        requestedById = 3,
                        requestedByEmpId = "SR0035",
                        requestedByFirstName = "Twinkle",
                        requestedByLastName = "Dahiya",
                        feedbackFromId = 1,
                        feedbackFromFirstName = "Syed Ubed",
                        feedbackFromLastName = "Ali",
                        feedbackFromEmailId = "syed.ali@scalereal.com",
                        externalFeedbackFromEmailId = null,
                        date = Date.valueOf(LocalDate.now()),
                        organisationName = "ScaleReal",
                        organisationTimeZone = "UTC",
                    ),
                    PendingFeedbackRequestDetails(
                        id = 3,
                        isExternalRequest = false,
                        requestedById = 3,
                        requestedByEmpId = "SR0035",
                        requestedByFirstName = "Twinkle",
                        requestedByLastName = "Dahiya",
                        feedbackFromId = 1,
                        feedbackFromFirstName = "Syed Ubed",
                        feedbackFromLastName = "Ali",
                        feedbackFromEmailId = "syed.ali@scalereal.com",
                        externalFeedbackFromEmailId = null,
                        date = Date.valueOf(LocalDate.now()),
                        organisationName = "ScaleReal",
                        organisationTimeZone = "UTC",
                    ),
                    PendingFeedbackRequestDetails(
                        id = 4,
                        isExternalRequest = false,
                        requestedById = 1,
                        requestedByEmpId = "SR0043",
                        requestedByFirstName = "Syed Ubed",
                        requestedByLastName = "Ali",
                        feedbackFromId = 3,
                        feedbackFromFirstName = "Twinkle",
                        feedbackFromLastName = "Dahiya",
                        feedbackFromEmailId = "twinkle.dahiya@scalereal.com",
                        externalFeedbackFromEmailId = null,
                        date = Date.valueOf(LocalDate.now()),
                        organisationName = "ScaleReal",
                        organisationTimeZone = "UTC",
                    ),
                    PendingFeedbackRequestDetails(
                        id = 5,
                        isExternalRequest = false,
                        requestedById = 2,
                        requestedByEmpId = "SR0050",
                        requestedByFirstName = "Moly",
                        requestedByLastName = "Agarwal",
                        feedbackFromId = 1,
                        feedbackFromFirstName = "Syed Ubed",
                        feedbackFromLastName = "Ali",
                        feedbackFromEmailId = "syed.ali@scalereal.com",
                        externalFeedbackFromEmailId = null,
                        date = Date.valueOf(LocalDate.now()),
                        organisationName = "ScaleReal",
                        organisationTimeZone = "UTC",
                    ),
                    PendingFeedbackRequestDetails(
                        id = 7,
                        isExternalRequest = true,
                        requestedById = 1,
                        requestedByEmpId = "SR0043",
                        requestedByFirstName = "Syed Ubed",
                        requestedByLastName = "Ali",
                        feedbackFromId = null,
                        feedbackFromFirstName = null,
                        feedbackFromLastName = null,
                        feedbackFromEmailId = null,
                        externalFeedbackFromEmailId = "your_email@example.com",
                        date = Date.valueOf(LocalDate.now()),
                        organisationName = "ScaleReal",
                        organisationTimeZone = "UTC",
                    ),
                )
            val actualPendingRequest = feedbackRequestRepositoryImpl.getPendingFeedbackRequest()
            actualPendingRequest.size shouldBe expectedPendingRequest.size
            actualPendingRequest shouldBe expectedPendingRequest
        }
    }

    private fun assertFeedbackRequestData(
        expected: List<FeedbackRequestData>,
        actual: List<FeedbackRequestData>,
    ) {
        actual.size shouldBe expected.size
        actual.mapIndexed { index, actualFeedbackRequest ->
            val expectedFeedbackRequest = expected[index]
            actualFeedbackRequest.organisationId shouldBe expectedFeedbackRequest.organisationId
            actualFeedbackRequest.requestId shouldBe expectedFeedbackRequest.requestId
            actualFeedbackRequest.isExternalRequest shouldBe expectedFeedbackRequest.isExternalRequest
            actualFeedbackRequest.externalFeedbackFromEmail shouldBe expectedFeedbackRequest.externalFeedbackFromEmail
            actualFeedbackRequest.isSubmitted shouldBe expectedFeedbackRequest.isSubmitted
            actualFeedbackRequest.requestedById shouldBe expectedFeedbackRequest.requestedById
            actualFeedbackRequest.requestedByEmployeeId shouldBe expectedFeedbackRequest.requestedByEmployeeId
            actualFeedbackRequest.requestedByFirstName shouldBe expectedFeedbackRequest.requestedByFirstName
            actualFeedbackRequest.requestedByLastName shouldBe expectedFeedbackRequest.requestedByLastName
            actualFeedbackRequest.feedbackFromId shouldBe expectedFeedbackRequest.feedbackFromId
            actualFeedbackRequest.feedbackFromEmployeeId shouldBe expectedFeedbackRequest.feedbackFromEmployeeId
            actualFeedbackRequest.feedbackFromFirstName shouldBe expectedFeedbackRequest.feedbackFromFirstName
            actualFeedbackRequest.feedbackFromLastName shouldBe expectedFeedbackRequest.feedbackFromLastName
            actualFeedbackRequest.feedbackToId shouldBe expectedFeedbackRequest.feedbackToId
            actualFeedbackRequest.feedbackToEmployeeId shouldBe expectedFeedbackRequest.feedbackToEmployeeId
            actualFeedbackRequest.feedbackToFirstName shouldBe expectedFeedbackRequest.feedbackToFirstName
            actualFeedbackRequest.feedbackToLastName shouldBe expectedFeedbackRequest.feedbackToLastName
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)

        val testDataFile = File("./test-res/feedback/feedback-request-test-data.sql").readText().trim()
        dataSource.connection.use {
            it.executeCommand(testDataFile)
        }
        feedbackRequestRepositoryImpl = FeedbackRequestRepositoryImpl(dataSource)
    }
}
