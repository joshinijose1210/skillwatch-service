package reviewCycle

import io.kotest.core.spec.Spec
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import norm.CommandResult
import norm.executeCommand
import scalereal.core.designations.DesignationRepository
import scalereal.core.emails.CheckInWithManagerStartedMail
import scalereal.core.emails.ManagerReviewTimelineStartedMail
import scalereal.core.emails.ReviewCycleStartedEmail
import scalereal.core.emails.ReviewCycleUpdatedEmail
import scalereal.core.emails.SelfReviewTimelineStartedMail
import scalereal.core.employees.EmployeeRepository
import scalereal.core.kra.KRARepository
import scalereal.core.models.domain.ActiveReviewCycle
import scalereal.core.models.domain.CheckInWithManagerData
import scalereal.core.models.domain.CheckInWithManagerParams
import scalereal.core.models.domain.EmployeeReviewDetails
import scalereal.core.models.domain.ManagerReviewCycleData
import scalereal.core.models.domain.MyManagerReviewCycleData
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.models.domain.ReviewCycleTimeline
import scalereal.core.models.domain.StartedReviewCycle
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.reviewCycle.ReviewCycleService
import scalereal.core.slack.CheckInSlackNotifications
import scalereal.core.slack.ManagerReviewSlackNotifications
import scalereal.core.slack.ReviewCycleSlackNotifications
import scalereal.core.slack.SelfReviewSlackNotifications
import scalereal.core.userActivity.UserActivityRepository
import scalereal.db.reviewCycle.ReviewCycleRepositoryImpl
import util.StringSpecWithDataSource
import java.io.File
import java.sql.Date
import java.sql.Timestamp

class ReviewCycleRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var reviewCycleRepositoryImpl: ReviewCycleRepositoryImpl
    private lateinit var service: ReviewCycleService
    private val reviewCycleStartedMail = mockk<ReviewCycleStartedEmail>()
    private val reviewCycleUpdatedMail = mockk<ReviewCycleUpdatedEmail>()
    private val selfReviewTimelineStartedMail = mockk<SelfReviewTimelineStartedMail>()
    private val managerReviewTimelineStartedMail = mockk<ManagerReviewTimelineStartedMail>()
    private val checkInStartedMail = mockk<CheckInWithManagerStartedMail>()
    private val userActivity = mockk<UserActivityRepository>()
    private val kraRepository = mockk<KRARepository>()
    private val designationRepository = mockk<DesignationRepository>()
    private val organisationRepository = mockk<OrganisationRepository>()
    private val moduleService =
        mockk<ModuleService> {
            every { fetchModuleId(Modules.REVIEW_CYCLE.moduleName) } returns 10
        }
    private val employeeRepository = mockk<EmployeeRepository>()
    private val reviewCycleSlackNotifications = mockk<ReviewCycleSlackNotifications>()
    private val selfReviewSlackNotifications = mockk<SelfReviewSlackNotifications>()
    private val managerReviewSlackNotifications = mockk<ManagerReviewSlackNotifications>()
    private val checkInSlackNotifications = mockk<CheckInSlackNotifications>()

    init {

        val now = Timestamp(System.currentTimeMillis())

        "should return empty list when no review cycle found" {
            reviewCycleRepositoryImpl.fetch(organisationId = 1, offset = 10, limit = Int.MAX_VALUE) shouldBe emptyList()
        }

        "should create new review cycle" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 2,
                    startDate = Date.valueOf("2022-10-09"),
                    endDate = Date.valueOf("2022-11-06"),
                    publish = true,
                    lastModified = now,
                    selfReviewStartDate = Date.valueOf("2022-10-10"),
                    selfReviewEndDate = Date.valueOf("2022-10-18"),
                    managerReviewStartDate = Date.valueOf("2022-10-12"),
                    managerReviewEndDate = Date.valueOf("2022-10-12"),
                    checkInWithManagerStartDate = Date.valueOf("2022-11-1"),
                    checkInWithManagerEndDate = Date.valueOf("2022-11-4"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val reviewCycleData = reviewCycleRepositoryImpl.create(reviewCycle)

            assertReviewCycle(reviewCycle, reviewCycleData)
        }

        "should update review cycle by id" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 2,
                    startDate = Date.valueOf("2022-11-01"),
                    endDate = Date.valueOf("2023-01-31"),
                    publish = false,
                    lastModified = now,
                    selfReviewStartDate = Date.valueOf("2022-11-01"),
                    selfReviewEndDate = Date.valueOf("2022-11-30"),
                    managerReviewStartDate = Date.valueOf("2022-12-01"),
                    managerReviewEndDate = Date.valueOf("2022-12-31"),
                    checkInWithManagerStartDate = Date.valueOf("2023-01-01"),
                    checkInWithManagerEndDate = Date.valueOf("2022-01-30"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val reviewCycleData = reviewCycleRepositoryImpl.update(reviewCycle)

            assertReviewCycle(reviewCycle, reviewCycleData)
        }

        "should create another review cycle" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 3,
                    startDate = Date.valueOf("2023-07-01"),
                    endDate = Date.valueOf("2023-12-31"),
                    publish = true,
                    lastModified = now,
                    selfReviewStartDate = Date.valueOf("2023-11-01"),
                    selfReviewEndDate = Date.valueOf("2023-11-30"),
                    managerReviewStartDate = Date.valueOf("2023-11-15"),
                    managerReviewEndDate = Date.valueOf("2023-12-05"),
                    checkInWithManagerStartDate = Date.valueOf("2023-12-06"),
                    checkInWithManagerEndDate = Date.valueOf("2023-12-30"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val reviewCycleData = reviewCycleRepositoryImpl.create(reviewCycle)

            assertReviewCycle(reviewCycle, reviewCycleData)
        }

        "should return count and fetch all review cycles by organisation id" {
            val expectedReviewCycles =
                listOf(
                    ReviewCycle(
                        organisationId = 1,
                        reviewCycleId = 3,
                        startDate = Date.valueOf("2023-07-01"),
                        endDate = Date.valueOf("2023-12-31"),
                        publish = true,
                        lastModified = now,
                        selfReviewStartDate = Date.valueOf("2023-11-01"),
                        selfReviewEndDate = Date.valueOf("2023-11-30"),
                        managerReviewStartDate = Date.valueOf("2023-11-15"),
                        managerReviewEndDate = Date.valueOf("2023-12-05"),
                        checkInWithManagerStartDate = Date.valueOf("2023-12-06"),
                        checkInWithManagerEndDate = Date.valueOf("2023-12-30"),
                        isSelfReviewDatePassed = false,
                        isManagerReviewDatePassed = false,
                        isCheckInWithManagerDatePassed = false,
                    ),
                    ReviewCycle(
                        organisationId = 1,
                        reviewCycleId = 2,
                        startDate = Date.valueOf("2022-11-01"),
                        endDate = Date.valueOf("2023-01-31"),
                        publish = false,
                        lastModified = now,
                        selfReviewStartDate = Date.valueOf("2022-11-01"),
                        selfReviewEndDate = Date.valueOf("2022-11-30"),
                        managerReviewStartDate = Date.valueOf("2022-12-01"),
                        managerReviewEndDate = Date.valueOf("2022-12-31"),
                        checkInWithManagerStartDate = Date.valueOf("2023-01-01"),
                        checkInWithManagerEndDate = Date.valueOf("2022-01-30"),
                        isSelfReviewDatePassed = false,
                        isManagerReviewDatePassed = false,
                        isCheckInWithManagerDatePassed = false,
                    ),
                    ReviewCycle(
                        organisationId = 1,
                        reviewCycleId = 1,
                        startDate = Date.valueOf("2022-01-01"),
                        endDate = Date.valueOf("2022-06-30"),
                        publish = false,
                        lastModified = now,
                        selfReviewStartDate = Date.valueOf("2022-06-01"),
                        selfReviewEndDate = Date.valueOf("2022-06-10"),
                        managerReviewStartDate = Date.valueOf("2022-06-11"),
                        managerReviewEndDate = Date.valueOf("2022-06-20"),
                        checkInWithManagerStartDate = Date.valueOf("2022-06-21"),
                        checkInWithManagerEndDate = Date.valueOf("2022-06-30"),
                        isSelfReviewDatePassed = false,
                        isManagerReviewDatePassed = false,
                        isCheckInWithManagerDatePassed = false,
                    ),
                )

            val count = reviewCycleRepositoryImpl.count(organisationId = 1)
            val actualReviewCycles = reviewCycleRepositoryImpl.fetch(organisationId = 1, offset = 0, limit = Int.MAX_VALUE)

            count shouldBe actualReviewCycles.size

            actualReviewCycles.mapIndexed { index, actual ->
                val expected = expectedReviewCycles[index]
                actual.reviewCycleId shouldBe expected.reviewCycleId
                actual.organisationId shouldBe expected.organisationId
                actual.startDate shouldBe expected.startDate
                actual.endDate shouldBe expected.endDate
                actual.publish shouldBe expected.publish
                actual.selfReviewStartDate shouldBe expected.selfReviewStartDate
                actual.selfReviewEndDate shouldBe expected.selfReviewEndDate
                actual.managerReviewStartDate shouldBe expected.managerReviewStartDate
                actual.managerReviewEndDate shouldBe expected.managerReviewEndDate
                actual.checkInWithManagerStartDate shouldBe expected.checkInWithManagerStartDate
                actual.checkInWithManagerEndDate shouldBe expected.checkInWithManagerEndDate
                expected.lastModified?.let { actual.lastModified?.shouldBeAfter(it) }
            }
        }

        "should return count and fetch self review cycle data" {
            val expectedResult =
                listOf(
                    ActiveReviewCycle(
                        reviewCycleId = 3,
                        startDate = Date.valueOf("2023-07-01"),
                        endDate = Date.valueOf("2023-12-31"),
                        selfReviewStartDate = Date.valueOf("2023-11-01"),
                        selfReviewEndDate = Date.valueOf("2023-11-30"),
                        draft = null,
                        publish = null,
                        updatedAt = null,
                        averageRating = (-1.00).toBigDecimal(),
                        isReviewCyclePublish = true,
                        isSelfReviewDatePassed = false,
                    ),
                    ActiveReviewCycle(
                        reviewCycleId = 2,
                        startDate = Date.valueOf("2022-11-01"),
                        endDate = Date.valueOf("2023-01-31"),
                        selfReviewStartDate = Date.valueOf("2022-11-01"),
                        selfReviewEndDate = Date.valueOf("2022-11-30"),
                        draft = null,
                        publish = null,
                        updatedAt = null,
                        averageRating = (-1.00).toBigDecimal(),
                        isReviewCyclePublish = false,
                        isSelfReviewDatePassed = false,
                    ),
                    ActiveReviewCycle(
                        reviewCycleId = 1,
                        startDate = Date.valueOf("2022-01-01"),
                        endDate = Date.valueOf("2022-06-30"),
                        selfReviewStartDate = Date.valueOf("2022-06-01"),
                        selfReviewEndDate = Date.valueOf("2022-06-10"),
                        draft = false,
                        publish = true,
                        updatedAt = now,
                        averageRating = (3.50).toBigDecimal(),
                        isReviewCyclePublish = false,
                        isSelfReviewDatePassed = false,
                    ),
                )
            val organisationId = 1L
            val reviewTypeId = listOf(1)
            val reviewToId = listOf(2)
            val reviewFromId = listOf(2)
            val reviewCycleId = listOf(-99)
            val count =
                reviewCycleRepositoryImpl.countSelfReviewCycle(
                    organisationId = organisationId,
                    reviewTypeId = reviewTypeId,
                    reviewToId = reviewToId,
                    reviewFromId = reviewFromId,
                    reviewCycleId = reviewCycleId,
                )
            val actualResult =
                reviewCycleRepositoryImpl.fetchSelfReviewCycle(
                    organisationId = organisationId,
                    reviewTypeId = reviewTypeId,
                    reviewToId = reviewToId,
                    reviewFromId = reviewFromId,
                    reviewCycleId = reviewCycleId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            count shouldBe actualResult.size
            actualResult.mapIndexed { index, actual ->
                val expected = expectedResult[index]
                actual.reviewCycleId shouldBe expected.reviewCycleId
                actual.startDate shouldBe expected.startDate
                actual.endDate shouldBe expected.endDate
                actual.selfReviewStartDate shouldBe expected.selfReviewStartDate
                actual.selfReviewEndDate shouldBe expected.selfReviewEndDate
                actual.draft shouldBe expected.draft
                actual.publish shouldBe expected.publish
                actual.isReviewCyclePublish shouldBe expected.isReviewCyclePublish
                actual.averageRating?.stripTrailingZeros() shouldBe expected.averageRating?.stripTrailingZeros()
                expected.updatedAt?.let { actual.updatedAt?.shouldBeAfter(it) }
            }
        }

        "should return count and fetch manager review cycle data given by manager to team" {
            val expectedResult =
                listOf(
                    ManagerReviewCycleData(
                        reviewCycleId = 1,
                        startDate = Date.valueOf("2022-01-01"),
                        endDate = Date.valueOf("2022-06-30"),
                        managerReviewStartDate = Date.valueOf("2022-06-11"),
                        managerReviewEndDate = Date.valueOf("2022-06-20"),
                        team = "Backend",
                        reviewToId = 2,
                        reviewToEmployeeId = "SR0051",
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        draft = false,
                        publish = true,
                        averageRating = (3.50).toBigDecimal(),
                        isReviewCyclePublish = false,
                        isReviewCycleActive = false,
                        isManagerReviewActive = false,
                        isManagerReviewDatePassed = false,
                    ),
                )
            val organisationId = 1L
            val reviewTypeId = 2
            val reviewToId = listOf(-99)
            val reviewFromId = 3L
            val reviewCycleId = listOf(-99)
            val managerReviewDraft = false
            val managerReviewPublished = true

            val count =
                reviewCycleRepositoryImpl.countManagerReviewCycle(
                    organisationId = organisationId,
                    reviewTypeId = reviewTypeId,
                    reviewToId = reviewToId,
                    reviewFromId = reviewFromId,
                    reviewCycleId = reviewCycleId,
                    managerReviewDraft = managerReviewDraft,
                    managerReviewPublished = managerReviewPublished,
                )
            val actualResult =
                reviewCycleRepositoryImpl.fetchManagerReviewCycle(
                    organisationId = organisationId,
                    reviewTypeId = reviewTypeId,
                    reviewToId = reviewToId,
                    reviewFromId = reviewFromId,
                    reviewCycleId = reviewCycleId,
                    managerReviewDraft = managerReviewDraft,
                    managerReviewPublished = managerReviewPublished,
                    limit = Int.MAX_VALUE,
                    offset = 0,
                )
            count shouldBe actualResult.size
            actualResult.mapIndexed { index, actual ->
                val expected = expectedResult[index]
                actual.reviewCycleId shouldBe expected.reviewCycleId
                actual.startDate shouldBe expected.startDate
                actual.endDate shouldBe expected.endDate
                actual.isReviewCyclePublish shouldBe expected.isReviewCyclePublish
                actual.managerReviewStartDate shouldBe expected.managerReviewStartDate
                actual.managerReviewEndDate shouldBe expected.managerReviewEndDate
                actual.team shouldBe expected.team
                actual.reviewToId shouldBe expected.reviewToId
                actual.reviewToEmployeeId shouldBe expected.reviewToEmployeeId
                actual.firstName shouldBe expected.firstName
                actual.lastName shouldBe expected.lastName
                actual.draft shouldBe expected.draft
                actual.publish shouldBe expected.publish
                actual.averageRating?.stripTrailingZeros() shouldBe expected.averageRating?.stripTrailingZeros()
            }
        }

        "should return count and fetch my manager review cycle data received by team member" {
            val expectedResult =
                listOf(
                    MyManagerReviewCycleData(
                        reviewCycleId = 3,
                        startDate = Date.valueOf("2023-07-01"),
                        endDate = Date.valueOf("2023-12-31"),
                        managerReviewStartDate = Date.valueOf("2023-11-15"),
                        managerReviewEndDate = Date.valueOf("2023-12-05"),
                        team = "Backend",
                        reviewToId = 2,
                        reviewToEmployeeId = "SR0051",
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        reviewFromId = 3,
                        reviewFromEmployeeId = "SR0006",
                        managerFirstName = "Yogesh",
                        managerLastName = "Jadhav",
                        draft = null,
                        publish = null,
                        isReviewCyclePublish = false,
                        averageRating = (-1.0).toBigDecimal(),
                        isManagerReviewDatePassed = false,
                    ),
                    MyManagerReviewCycleData(
                        reviewCycleId = 2,
                        startDate = Date.valueOf("2022-11-01"),
                        endDate = Date.valueOf("2023-01-31"),
                        managerReviewStartDate = Date.valueOf("2022-12-01"),
                        managerReviewEndDate = Date.valueOf("2022-12-31"),
                        team = "Backend",
                        reviewToId = 2,
                        reviewToEmployeeId = "SR0051",
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        reviewFromId = 3,
                        reviewFromEmployeeId = "SR0006",
                        managerFirstName = "Yogesh",
                        managerLastName = "Jadhav",
                        draft = null,
                        publish = null,
                        isReviewCyclePublish = false,
                        averageRating = (-1.0).toBigDecimal(),
                        isManagerReviewDatePassed = false,
                    ),
                    MyManagerReviewCycleData(
                        reviewCycleId = 1,
                        startDate = Date.valueOf("2022-01-01"),
                        endDate = Date.valueOf("2022-06-30"),
                        managerReviewStartDate = Date.valueOf("2022-06-11"),
                        managerReviewEndDate = Date.valueOf("2022-06-20"),
                        team = "Backend",
                        reviewToId = 2,
                        reviewToEmployeeId = "SR0051",
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        reviewFromId = 3,
                        reviewFromEmployeeId = "SR0006",
                        managerFirstName = "Yogesh",
                        managerLastName = "Jadhav",
                        draft = false,
                        publish = true,
                        isReviewCyclePublish = false,
                        averageRating = (3.50).toBigDecimal(),
                        isManagerReviewDatePassed = false,
                    ),
                )
            val organisationId = 1L
            val reviewTypeId = 2
            val reviewToId = 2L
            val reviewFromId = listOf(-99)
            val reviewCycleId = listOf(-99)
            val count =
                reviewCycleRepositoryImpl.countMyManagerReviewCycle(
                    organisationId = organisationId,
                    reviewTypeId = reviewTypeId,
                    reviewToId = reviewToId,
                    reviewFromId = reviewFromId,
                    reviewCycleId = reviewCycleId,
                )
            val actualResult =
                reviewCycleRepositoryImpl.fetchMyManagerReviewCycle(
                    organisationId = organisationId,
                    reviewTypeId = reviewTypeId,
                    reviewToId = reviewToId,
                    reviewFromId = reviewFromId,
                    reviewCycleId = reviewCycleId,
                    limit = Int.MAX_VALUE,
                    offset = 0,
                )
            count shouldBe actualResult.size
            actualResult.mapIndexed { index, actual ->
                val expected = expectedResult[index]
                actual.reviewCycleId shouldBe expected.reviewCycleId
                actual.startDate shouldBe expected.startDate
                actual.endDate shouldBe expected.endDate
                actual.managerReviewStartDate shouldBe expected.managerReviewStartDate
                actual.managerReviewEndDate shouldBe expected.managerReviewEndDate
                actual.team shouldBe expected.team
                actual.reviewToId shouldBe expected.reviewToId
                actual.reviewToEmployeeId shouldBe expected.reviewToEmployeeId
                actual.firstName shouldBe expected.firstName
                actual.lastName shouldBe expected.lastName
                actual.reviewFromId shouldBe expected.reviewFromId
                actual.reviewFromEmployeeId shouldBe expected.reviewFromEmployeeId
                actual.managerFirstName shouldBe expected.managerFirstName
                actual.managerLastName shouldBe expected.managerLastName
                actual.draft shouldBe expected.draft
                actual.publish shouldBe expected.publish
                actual.averageRating?.stripTrailingZeros() shouldBe expected.averageRating?.stripTrailingZeros()
            }
        }

        "should return count and fetch check in with manager cycle data" {
            val expectedReview =
                listOf(
                    CheckInWithManagerData(
                        reviewCycleId = 1,
                        startDate = Date.valueOf("2022-01-01"),
                        endDate = Date.valueOf("2022-06-30"),
                        publish = false,
                        checkInStartDate = Date.valueOf("2022-06-21"),
                        checkInEndDate = Date.valueOf("2022-06-30"),
                        reviewToId = 1,
                        reviewToEmployeeId = "SR0043",
                        firstName = "Syed Ubed",
                        lastName = "Ali",
                        selfReviewDraft = false,
                        selfReviewPublish = false,
                        selfAverageRating = (-1.00).toBigDecimal(),
                        firstManagerReviewDraft = false,
                        firstManagerReviewPublish = false,
                        firstManagerAverageRating = (-1.00).toBigDecimal(),
                        secondManagerReviewDraft = false,
                        secondManagerReviewPublish = false,
                        secondManagerAverageRating = (-1.00).toBigDecimal(),
                        checkInFromId = null,
                        checkInDraft = false,
                        checkInPublish = false,
                        checkInAverageRating = (-1.00).toBigDecimal(),
                        firstManagerId = 3,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        isCheckInWithManagerDatePassed = false,
                    ),
                    CheckInWithManagerData(
                        reviewCycleId = 1,
                        startDate = Date.valueOf("2022-01-01"),
                        endDate = Date.valueOf("2022-06-30"),
                        publish = false,
                        checkInStartDate = Date.valueOf("2022-06-21"),
                        checkInEndDate = Date.valueOf("2022-06-30"),
                        reviewToId = 2,
                        reviewToEmployeeId = "SR0051",
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        selfReviewDraft = false,
                        selfReviewPublish = true,
                        selfAverageRating = (3.50).toBigDecimal(),
                        firstManagerReviewDraft = false,
                        firstManagerReviewPublish = true,
                        firstManagerAverageRating = (3.50).toBigDecimal(),
                        secondManagerReviewDraft = false,
                        secondManagerReviewPublish = false,
                        secondManagerAverageRating = (-1.00).toBigDecimal(),
                        checkInFromId = 3,
                        checkInDraft = true,
                        checkInPublish = false,
                        checkInAverageRating = (-1.00).toBigDecimal(),
                        firstManagerId = 3,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        isCheckInWithManagerDatePassed = false,
                    ),
                )
            val checkInParams =
                CheckInWithManagerParams(
                    organisationId = 1,
                    managerId = listOf(-99),
                    reviewCycleId = listOf(1),
                    reviewToId = listOf(1, 2),
                    teamId = listOf(-99),
                    selfReviewDraft = null,
                    selfReviewPublish = null,
                    firstManagerReviewDraft = null,
                    firstManagerReviewPublish = null,
                    secondManagerReviewDraft = null,
                    secondManagerReviewPublish = null,
                    checkInDraft = null,
                    checkInPublished = null,
                    minFilterRating = -1.0,
                    maxFilterRating = 5.0,
                    filterRatingId = -1,
                )
            val count = reviewCycleRepositoryImpl.countCheckInWithManager(checkInParams)
            val actualReview = reviewCycleRepositoryImpl.fetchCheckInWithManager(checkInParams)

            count shouldBe actualReview.size
            actualReview.mapIndexed { index, actual ->
                val expected = expectedReview[index]
                actual.reviewCycleId shouldBe expected.reviewCycleId
                actual.startDate shouldBe expected.startDate
                actual.endDate shouldBe expected.endDate
                actual.publish shouldBe expected.publish
                actual.checkInStartDate shouldBe expected.checkInStartDate
                actual.checkInEndDate shouldBe expected.checkInEndDate
                actual.reviewToId shouldBe expected.reviewToId
                actual.reviewToEmployeeId shouldBe expected.reviewToEmployeeId
                actual.firstName shouldBe expected.firstName
                actual.lastName shouldBe expected.lastName
                actual.selfReviewDraft shouldBe expected.selfReviewDraft
                actual.selfReviewPublish shouldBe expected.selfReviewPublish
                actual.selfAverageRating?.stripTrailingZeros() shouldBe expected.selfAverageRating?.stripTrailingZeros()
                actual.firstManagerReviewDraft shouldBe expected.firstManagerReviewDraft
                actual.firstManagerReviewPublish shouldBe expected.firstManagerReviewPublish
                actual.firstManagerAverageRating?.stripTrailingZeros() shouldBe expected.firstManagerAverageRating?.stripTrailingZeros()
                actual.secondManagerReviewDraft shouldBe expected.secondManagerReviewDraft
                actual.secondManagerReviewPublish shouldBe expected.secondManagerReviewPublish
                actual.secondManagerAverageRating shouldBe expected.secondManagerAverageRating
                actual.checkInFromId shouldBe expected.checkInFromId
                actual.checkInDraft shouldBe expected.checkInDraft
                actual.checkInPublish shouldBe expected.checkInPublish
                actual.checkInAverageRating?.stripTrailingZeros() shouldBe expected.checkInAverageRating?.stripTrailingZeros()
            }
        }

        "should return count 0 and empty list if there is no data available for applied filters" {
            val checkInParams =
                CheckInWithManagerParams(
                    organisationId = 1,
                    managerId = listOf(3),
                    reviewCycleId = listOf(1),
                    reviewToId = listOf(2),
                    teamId = listOf(1),
                    selfReviewDraft = true,
                    selfReviewPublish = true,
                    firstManagerReviewDraft = true,
                    firstManagerReviewPublish = true,
                    secondManagerReviewDraft = null,
                    secondManagerReviewPublish = null,
                    checkInDraft = true,
                    checkInPublished = true,
                    minFilterRating = 4.0,
                    maxFilterRating = 4.9,
                )

            val count = reviewCycleRepositoryImpl.countCheckInWithManager(checkInParams)
            val expectedReview = reviewCycleRepositoryImpl.fetchCheckInWithManager(checkInParams)
            count shouldBe expectedReview.size
            expectedReview shouldBe emptyList()
        }

        "should fetch data for review cycle timeline" {
            val expectedTimelineData =
                listOf(
                    ReviewCycleTimeline(
                        organisationId = 1,
                        reviewCycleId = 3,
                        startDate = Date.valueOf("2023-07-01"),
                        endDate = Date.valueOf("2023-12-31"),
                        publish = true,
                        selfReviewStartDate = Date.valueOf("2023-11-01"),
                        selfReviewEndDate = Date.valueOf("2023-11-30"),
                        managerReviewStartDate = Date.valueOf("2023-11-15"),
                        managerReviewEndDate = Date.valueOf("2023-12-05"),
                        selfReviewDraft = false,
                        selfReviewPublish = false,
                        selfReviewDate = null,
                        selfAverageRating = (-1.00).toBigDecimal(),
                        firstManagerId = 3,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        firstManagerReviewDraft = false,
                        firstManagerReviewPublish = false,
                        firstManagerReviewDate = null,
                        firstManagerAverageRating = (-1.00).toBigDecimal(),
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        secondManagerReviewDraft = false,
                        secondManagerReviewPublish = false,
                        secondManagerReviewDate = null,
                        secondManagerAverageRating = (-1.00).toBigDecimal(),
                        checkInFromId = null,
                        checkInFromEmployeeId = null,
                        checkInFromFirstName = null,
                        checkInFromLastName = null,
                        checkInWithManagerStartDate = Date.valueOf("2023-12-06"),
                        checkInWithManagerEndDate = Date.valueOf("2023-12-30"),
                        checkInWithManagerDraft = false,
                        checkInWithManagerPublish = false,
                        checkInWithManagerDate = null,
                        checkInWithManagerAverageRating = (-1.00).toBigDecimal(),
                        isOrWasManager = true,
                        isSelfReviewDatePassed = false,
                        isManagerReviewDatePassed = false,
                        isCheckInWithManagerDatePassed = false,
                        empDetails =
                            listOf(
                                EmployeeReviewDetails(
                                    id = 1,
                                    employeeId = "SR0043",
                                    firstName = "Syed Ubed",
                                    lastName = "Ali",
                                    checkInFromId = null,
                                    firstManagerId = 3,
                                    secondManagerId = null,
                                    selfReviewDraft = false,
                                    selfReviewPublish = false,
                                    selfReviewDate = null,
                                    firstManagerReviewDraft = false,
                                    firstManagerReviewPublish = false,
                                    firstManagerReviewDate = null,
                                    secondManagerReviewDraft = false,
                                    secondManagerReviewPublish = false,
                                    secondManagerReviewDate = null,
                                    checkInFromEmployeeId = null,
                                    checkInFromFirstName = null,
                                    checkInFromLastName = null,
                                    checkInWithManagerDraft = false,
                                    checkInWithManagerPublish = false,
                                    checkInWithManagerDate = null,
                                ),
                                EmployeeReviewDetails(
                                    id = 2,
                                    employeeId = "SR0051",
                                    firstName = "Rushad",
                                    lastName = "Shaikh",
                                    checkInFromId = null,
                                    firstManagerId = 3,
                                    secondManagerId = null,
                                    selfReviewDraft = false,
                                    selfReviewPublish = false,
                                    selfReviewDate = null,
                                    firstManagerReviewDraft = false,
                                    firstManagerReviewPublish = false,
                                    firstManagerReviewDate = null,
                                    secondManagerReviewDraft = false,
                                    secondManagerReviewPublish = false,
                                    secondManagerReviewDate = null,
                                    checkInFromEmployeeId = null,
                                    checkInFromFirstName = null,
                                    checkInFromLastName = null,
                                    checkInWithManagerDraft = false,
                                    checkInWithManagerPublish = false,
                                    checkInWithManagerDate = null,
                                ),
                            ),
                    ),
                )
            val actualTimelineData = reviewCycleRepositoryImpl.fetchReviewCycleData(organisationId = 1, reviewToId = 3)

            actualTimelineData shouldBe expectedTimelineData
        }

        "should fetch review cycle details by review cycle id" {
            val expectedReviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 2,
                    startDate = Date.valueOf("2022-11-01"),
                    endDate = Date.valueOf("2023-01-31"),
                    publish = false,
                    lastModified = now,
                    selfReviewStartDate = Date.valueOf("2022-11-01"),
                    selfReviewEndDate = Date.valueOf("2022-11-30"),
                    managerReviewStartDate = Date.valueOf("2022-12-01"),
                    managerReviewEndDate = Date.valueOf("2022-12-31"),
                    checkInWithManagerStartDate = Date.valueOf("2023-01-01"),
                    checkInWithManagerEndDate = Date.valueOf("2022-01-30"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val actualReviewCycle = reviewCycleRepositoryImpl.fetchReviewCycle(reviewCycleId = 2)
            assertReviewCycle(expectedReviewCycle, actualReviewCycle)
        }

        "should verify if review cycle is started on specific date" {
            val expectedResult = StartedReviewCycle(exists = true, id = 3)
            reviewCycleRepositoryImpl.isReviewCycleStartedAt(
                date = Date.valueOf("2023-07-01"),
                organisationId = 1,
            ) shouldBe expectedResult
        }

        "should verify if self review timeline is started on specific date" {
            val expectedResult = StartedReviewCycle(exists = false, id = null)
            reviewCycleRepositoryImpl.isSelfReviewStartedAt(
                date = Date.valueOf("2023-07-01"),
                organisationId = 1,
            ) shouldBe expectedResult
        }

        "should verify if manager review timeline is started on specific date" {
            val expectedResult = StartedReviewCycle(exists = true, id = 3)
            reviewCycleRepositoryImpl.isManagerReviewStartedAt(
                date = Date.valueOf("2023-11-15"),
                organisationId = 1,
            ) shouldBe expectedResult
        }

        "should verify if check in with manager timeline is started on specific date" {
            val expectedResult = StartedReviewCycle(exists = false, id = null)
            reviewCycleRepositoryImpl.isCheckInStartedAt(
                date = Date.valueOf("2023-07-01"),
                organisationId = 1,
            ) shouldBe expectedResult
        }

        "should fetch active review cycle by organisation id" {
            val expectedReviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 3,
                    startDate = Date.valueOf("2023-07-01"),
                    endDate = Date.valueOf("2023-12-31"),
                    publish = true,
                    lastModified = now,
                    selfReviewStartDate = Date.valueOf("2023-11-01"),
                    selfReviewEndDate = Date.valueOf("2023-11-30"),
                    managerReviewStartDate = Date.valueOf("2023-11-15"),
                    managerReviewEndDate = Date.valueOf("2023-12-05"),
                    checkInWithManagerStartDate = Date.valueOf("2023-12-06"),
                    checkInWithManagerEndDate = Date.valueOf("2023-12-30"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val actualReviewCycle = reviewCycleRepositoryImpl.fetchActiveReviewCycle(organisationId = 1)
            actualReviewCycle?.let { assertReviewCycle(expectedReviewCycle, it) }
        }

        "should unpublish review cycle by review cycle id" {
            reviewCycleRepositoryImpl.unPublishReviewCycle(reviewCycleId = 3) shouldBe CommandResult(updatedRecordsCount = 1)
        }

        "should return null when no active review cycle" {
            reviewCycleRepositoryImpl.fetchActiveReviewCycle(organisationId = 1) shouldBe null
        }

        "should verify if review submission timeline is started on specific date" {
            reviewCycleRepositoryImpl.isReviewSubmissionStarted(
                organisationId = 1,
                date = Date.valueOf("2023-07-01"),
            ) shouldBe false
        }

        "should fetch previous review cycle id by organisation id" {
            reviewCycleRepositoryImpl.getPreviousReviewCycleId(organisationId = 1) shouldBe listOf(3L)
        }
    }

    private fun assertReviewCycle(
        expectedReviewCycle: ReviewCycle,
        actualReviewCycle: ReviewCycle,
    ) {
        actualReviewCycle.reviewCycleId shouldBe expectedReviewCycle.reviewCycleId
        actualReviewCycle.organisationId shouldBe expectedReviewCycle.organisationId
        actualReviewCycle.startDate shouldBe expectedReviewCycle.startDate
        actualReviewCycle.endDate shouldBe expectedReviewCycle.endDate
        actualReviewCycle.publish shouldBe expectedReviewCycle.publish
        actualReviewCycle.selfReviewStartDate shouldBe expectedReviewCycle.selfReviewStartDate
        actualReviewCycle.selfReviewEndDate shouldBe expectedReviewCycle.selfReviewEndDate
        actualReviewCycle.managerReviewStartDate shouldBe expectedReviewCycle.managerReviewStartDate
        actualReviewCycle.managerReviewEndDate shouldBe expectedReviewCycle.managerReviewEndDate
        actualReviewCycle.checkInWithManagerStartDate shouldBe expectedReviewCycle.checkInWithManagerStartDate
        actualReviewCycle.checkInWithManagerEndDate shouldBe expectedReviewCycle.checkInWithManagerEndDate
        expectedReviewCycle.lastModified?.let { actualReviewCycle.lastModified?.shouldBeAfter(it) }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        val testDataFile = File("./test-res/reviewCycle/review-cycle-data.sql").readText().trim()
        dataSource.connection.use {
            it.executeCommand(testDataFile)
        }
        reviewCycleRepositoryImpl = ReviewCycleRepositoryImpl(dataSource)
        service =
            ReviewCycleService(
                reviewCycleRepositoryImpl,
                reviewCycleStartedMail,
                reviewCycleUpdatedMail,
                selfReviewTimelineStartedMail,
                managerReviewTimelineStartedMail,
                checkInStartedMail,
                userActivity,
                moduleService,
                employeeRepository,
                reviewCycleSlackNotifications,
                selfReviewSlackNotifications,
                managerReviewSlackNotifications,
                checkInSlackNotifications,
                kraRepository,
                designationRepository,
                organisationRepository,
            )
    }
}
