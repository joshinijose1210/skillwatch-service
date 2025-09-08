package analytics

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.analytics.AnalyticsRepository
import scalereal.core.analytics.AnalyticsService
import scalereal.core.employees.EmployeeService
import scalereal.core.feedbacks.FeedbackRepository
import scalereal.core.models.EmployeeGender
import scalereal.core.models.domain.AnalyticsFeedbackCount
import scalereal.core.models.domain.AnalyticsFeedbackPercentage
import scalereal.core.models.domain.AnalyticsFeedbackResponse
import scalereal.core.models.domain.AverageAge
import scalereal.core.models.domain.AverageTenure
import scalereal.core.models.domain.EmpData
import scalereal.core.models.domain.EmployeeHistory
import scalereal.core.models.domain.EmployeesType
import scalereal.core.models.domain.EmployeesTypeData
import scalereal.core.models.domain.ExperienceRange
import scalereal.core.models.domain.FeedbackCounts
import scalereal.core.models.domain.GendersCount
import scalereal.core.models.domain.GendersData
import scalereal.core.models.domain.GendersPercentage
import scalereal.core.models.domain.Ratings
import scalereal.core.models.domain.RatingsData
import scalereal.core.models.domain.ReviewCount
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.models.domain.TeamEmployeeCount
import scalereal.core.reviewCycle.ReviewCycleService
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate

class AnalyticsServiceImplTest : StringSpec() {
    private val analyticsRepository = mockk<AnalyticsRepository>()
    private val employeeService = mockk<EmployeeService>()
    private val feedbackRepository = mockk<FeedbackRepository>()
    private val reviewCycleService = mockk<ReviewCycleService>()

    private val service = AnalyticsService(analyticsRepository, employeeService, reviewCycleService, feedbackRepository)

    init {
        "should get ratings data of organization" {
            val ratingsData =
                listOf(
                    RatingsData(
                        reviewCycleId = 1,
                        id = 2,
                        employeeId = "SR0002",
                        firstName = "Moly",
                        lastName = "Agarwal",
                        checkInRating = 3.5,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 1,
                        employeeId = "SR0001",
                        firstName = "Yogesh",
                        lastName = "Jadhav",
                        checkInRating = 4.8,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 3,
                        employeeId = "SR0003",
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        checkInRating = 3.9,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 4,
                        employeeId = "SR0008",
                        firstName = "Ubed",
                        lastName = "Ali",
                        checkInRating = 2.7,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 5,
                        employeeId = "SR0013",
                        firstName = "Abhishek",
                        lastName = "Singh",
                        checkInRating = 1.5,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 6,
                        employeeId = "SR0023",
                        firstName = "Dummy",
                        lastName = "User",
                        checkInRating = 5.0,
                    ),
                )
            val organisationId: Long = 1
            val reviewCycleId: Long = 1
            every { analyticsRepository.getRatings(any(), any()) } returns ratingsData
            val result = service.getRatings(organisationId, reviewCycleId)
            val expectedRatings =
                Ratings(
                    unsatisfactory = 1,
                    needsImprovement = 1,
                    meetsExpectations = 2,
                    exceedsExpectations = 1,
                    outstanding = 1,
                )
            result shouldBe expectedRatings
        }

        "should get meets expectations rating listing" {
            val ratingsData =
                listOf(
                    RatingsData(
                        reviewCycleId = 1,
                        id = 2,
                        employeeId = "SR0002",
                        firstName = "Moly",
                        lastName = "Agarwal",
                        checkInRating = 3.5,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 1,
                        employeeId = "SR0001",
                        firstName = "Yogesh",
                        lastName = "Jadhav",
                        checkInRating = 3.8,
                    ),
                )
            every { analyticsRepository.getRatings(any(), any(), any(), any(), any(), any(), any()) } returns ratingsData
            service.getRatingListing(
                organisationId = 1,
                reviewCycleId = 1,
                ratingType = "meetsExpectations",
                employeeId = listOf(-99),
                page = 1,
                limit = Int.MAX_VALUE,
            ) shouldBe ratingsData
        }

        "should get unsatisfactory rating listing" {
            val ratingsData =
                listOf(
                    RatingsData(
                        reviewCycleId = 1,
                        id = 1,
                        employeeId = "SR0001",
                        firstName = "Yogesh",
                        lastName = "Jadhav",
                        checkInRating = 1.2,
                    ),
                )
            every { analyticsRepository.getRatings(any(), any(), any(), any(), any(), any(), any()) } returns ratingsData
            service.getRatingListing(
                organisationId = 1,
                reviewCycleId = 1,
                ratingType = "unsatisfactory",
                employeeId = listOf(-99),
                page = 1,
                limit = Int.MAX_VALUE,
            ) shouldBe ratingsData
        }

        "should get needs improvement rating listing" {
            val ratingsData =
                listOf(
                    RatingsData(
                        reviewCycleId = 1,
                        id = 1,
                        employeeId = "SR0001",
                        firstName = "Yogesh",
                        lastName = "Jadhav",
                        checkInRating = 2.2,
                    ),
                )
            every { analyticsRepository.getRatings(any(), any(), any(), any(), any(), any(), any()) } returns ratingsData
            service.getRatingListing(
                organisationId = 1,
                reviewCycleId = 1,
                ratingType = "needsImprovement",
                employeeId = listOf(-99),
                page = 1,
                limit = Int.MAX_VALUE,
            ) shouldBe ratingsData
        }

        "should get exceeds expectations rating listing" {
            val ratingsData =
                listOf(
                    RatingsData(
                        reviewCycleId = 1,
                        id = 1,
                        employeeId = "SR0001",
                        firstName = "Yogesh",
                        lastName = "Jadhav",
                        checkInRating = 4.0,
                    ),
                )
            every { analyticsRepository.getRatings(any(), any(), any(), any(), any(), any(), any()) } returns ratingsData
            service.getRatingListing(
                organisationId = 1,
                reviewCycleId = 1,
                ratingType = "exceedsExpectations",
                employeeId = listOf(-99),
                page = 1,
                limit = Int.MAX_VALUE,
            ) shouldBe ratingsData
        }

        "should get outstanding rating listing count" {
            every { analyticsRepository.getRatingListingCount(any(), any(), any(), any(), any()) } returns 1
            service.getRatingListingCount(
                organisationId = 1,
                reviewCycleId = 1,
                ratingType = "outstanding",
                employeeId = listOf(-99),
            ) shouldBe 1
        }

        "should get rankings based on ratings" {
            val organisationId: Long = 1
            val reviewCycleId: Long = 1
            val reviewCycleData =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf("2022-12-12"),
                    endDate = Date.valueOf("2023-02-02"),
                    selfReviewStartDate = Date.valueOf("2022-12-13"),
                    selfReviewEndDate = Date.valueOf("2022-12-14"),
                    managerReviewStartDate = Date.valueOf("2023-01-01"),
                    managerReviewEndDate = Date.valueOf("2023-01-10"),
                    lastModified = Timestamp.valueOf("2023-1-1 11:9:48.834129"),
                    publish = true,
                    checkInWithManagerStartDate = Date.valueOf("2023-01-20"),
                    checkInWithManagerEndDate = Date.valueOf("2023-01-25"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val ratingsData =
                listOf(
                    RatingsData(
                        reviewCycleId = 1,
                        id = 1,
                        employeeId = "SR0006",
                        firstName = "Yogesh",
                        lastName = "Jadhav",
                        checkInRating = 2.8,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 2,
                        employeeId = "SR0002",
                        firstName = "Moly",
                        lastName = "Agarwal",
                        checkInRating = 3.5,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 3,
                        employeeId = "SR0003",
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        checkInRating = 2.9,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 4,
                        employeeId = "SR0013",
                        firstName = "Dummy",
                        lastName = "User",
                        checkInRating = -1.00,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 5,
                        employeeId = "SR0023",
                        firstName = "Dummy1",
                        lastName = "User",
                        checkInRating = 3.5,
                    ),
                )
            val feedbackCount =
                FeedbackCounts(
                    submittedPositiveCount = 0,
                    submittedImprovementCount = 1,
                    submittedAppreciationCount = 2,
                    receivedPositiveCount = 2,
                    receivedImprovementCount = 2,
                    receivedAppreciationCount = 2,
                )
            val expectedResult =
                listOf(
                    RatingsData(
                        reviewCycleId = 1,
                        id = 2,
                        employeeId = "SR0002",
                        firstName = "Moly",
                        lastName = "Agarwal",
                        checkInRating = 3.5,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 5,
                        employeeId = "SR0023",
                        firstName = "Dummy1",
                        lastName = "User",
                        checkInRating = 3.5,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 3,
                        employeeId = "SR0003",
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        checkInRating = 2.9,
                    ),
                    RatingsData(
                        reviewCycleId = 1,
                        id = 1,
                        employeeId = "SR0006",
                        firstName = "Yogesh",
                        lastName = "Jadhav",
                        checkInRating = 2.8,
                    ),
                )

            every { reviewCycleService.fetchReviewCycle(any()) } returns reviewCycleData
            every { analyticsRepository.getRatings(any(), any()) } returns ratingsData
            every { feedbackRepository.fetchEmployeeFeedbackCounts(any(), any(), any()) } returns feedbackCount
            val result = service.getRankings(organisationId, reviewCycleId)
            result shouldBe expectedResult
        }

        "Should get review status of employees" {
            val organisationId: Long = 1
            val reviewCycleId: Long = 1
            val activeEmployees: Long = 4
            val selfReviewData = ReviewCount(completed = 10, inProgress = 5, pending = 15)
            val manager1ReviewData = ReviewCount(completed = 8, inProgress = 3, pending = 19)
            val manager2ReviewData = ReviewCount(completed = 9, inProgress = 4, pending = 17)
            val checkInReviewData = ReviewCount(completed = 7, inProgress = 2, pending = 21)

            every {
                employeeService.fetchActiveEmployeesCountDuringReviewCycle(
                    organisationId,
                    reviewCycleId,
                )
            } returns activeEmployees
            every { analyticsRepository.getSelfReviewStatus(organisationId, reviewCycleId) } returns selfReviewData
            every {
                analyticsRepository.getManager1ReviewStatus(
                    organisationId,
                    reviewCycleId,
                )
            } returns manager1ReviewData
            every {
                analyticsRepository.getManager2ReviewStatus(
                    organisationId,
                    reviewCycleId,
                )
            } returns manager2ReviewData
            every {
                analyticsRepository.getCheckInReviewStatus(
                    organisationId,
                    reviewCycleId,
                )
            } returns checkInReviewData

            val result = service.getReviewStatus(organisationId, reviewCycleId)

            val expectedSelfReviewStatus =
                ReviewCount(
                    completed = selfReviewData.completed,
                    inProgress = selfReviewData.inProgress,
                    pending = activeEmployees - selfReviewData.completed - selfReviewData.inProgress,
                )
            val expectedManager1ReviewStatus =
                ReviewCount(
                    completed = manager1ReviewData.completed,
                    inProgress = manager1ReviewData.inProgress,
                    pending = activeEmployees - manager1ReviewData.completed - manager1ReviewData.inProgress,
                )
            val expectedManager2ReviewStatus =
                ReviewCount(
                    completed = manager2ReviewData.completed,
                    inProgress = manager2ReviewData.inProgress,
                    pending = activeEmployees - manager2ReviewData.completed - manager2ReviewData.inProgress,
                )
            val expectedCheckInReviewStatus =
                ReviewCount(
                    completed = checkInReviewData.completed,
                    inProgress = checkInReviewData.inProgress,
                    pending = activeEmployees - checkInReviewData.completed - checkInReviewData.inProgress,
                )

            result.self shouldBe expectedSelfReviewStatus
            result.manager1 shouldBe expectedManager1ReviewStatus
            result.manager2 shouldBe expectedManager2ReviewStatus
            result.checkIn shouldBe expectedCheckInReviewStatus
        }

        "Should get feedback graph data" {
            val organisationId: Long = 1
            val reviewCycleId: Long = 123
            val feedbackCounts =
                AnalyticsFeedbackCount(
                    positive = 10,
                    improvement = 5,
                    appreciation = 3,
                )
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf("2022-11-07"),
                    endDate = Date.valueOf("2023-02-25"),
                    publish = true,
                    lastModified = Timestamp.valueOf("2023-02-27 13:9:48.834129"),
                    selfReviewStartDate = Date.valueOf("2022-11-10"),
                    selfReviewEndDate = Date.valueOf("2022-11-18"),
                    managerReviewStartDate = Date.valueOf("2022-11-12"),
                    managerReviewEndDate = Date.valueOf("2022-11-20"),
                    checkInWithManagerStartDate = Date.valueOf("2022-11-22"),
                    checkInWithManagerEndDate = Date.valueOf("2022-11-26"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )

            every { reviewCycleService.fetchReviewCycle(reviewCycleId) } returns reviewCycle
            every {
                feedbackRepository.fetchTotalFeedbackCounts(
                    organisationId,
                    reviewCycle.startDate,
                    reviewCycle.endDate,
                )
            } returns feedbackCounts

            val result = service.getFeedbackGraphData(organisationId, reviewCycleId)

            val totalFeedback = feedbackCounts.positive + feedbackCounts.improvement + feedbackCounts.appreciation
            val expectedFeedbackPercentage =
                AnalyticsFeedbackPercentage(
                    positive =
                        if (totalFeedback != 0L) ((feedbackCounts.positive.toDouble() / totalFeedback.toDouble()) * 100) else 0.00,
                    improvement =
                        if (totalFeedback != 0L) ((feedbackCounts.improvement.toDouble() / totalFeedback.toDouble()) * 100) else 0.00,
                    appreciation =
                        if (totalFeedback != 0L) ((feedbackCounts.appreciation.toDouble() / totalFeedback.toDouble()) * 100) else 0.00,
                )
            result shouldBe AnalyticsFeedbackResponse(feedbackCounts, expectedFeedbackPercentage)
        }

        "Should get genders data active in review cycle" {
            val employeesId = listOf(1, 2, 3)

            val employeesData =
                listOf(
                    EmpData(
                        organisationId = 1,
                        id = 1,
                        firstName = "Aamir",
                        lastName = "Islam",
                        emailId = "aamir.islam@scalereal.com",
                        contactNo = "+917376743155",
                        genderId = EmployeeGender.MALE.genderId,
                        dateOfBirth = Date.valueOf("1998-05-01"),
                        dateOfJoining = Date.valueOf("2022-06-13"),
                        experienceInMonths = 8,
                        isConsultant = false,
                        employeeId = "SR0042",
                        status = true,
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Backend",
                        designationId = 2,
                        designationName = "sd2",
                        roleId = 1,
                        roleName = "software engineer",
                        firstManagerId = 4,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        employeeNameWithEmployeeId = "Aamir Islam (SR0042)",
                    ),
                    EmpData(
                        organisationId = 1,
                        id = 2,
                        firstName = "Moly",
                        lastName = "Agarwal",
                        emailId = "moly.agarwal@scalereal.com",
                        contactNo = "+917378783155",
                        genderId = EmployeeGender.FEMALE.genderId,
                        dateOfBirth = Date.valueOf("1998-05-01"),
                        dateOfJoining = Date.valueOf("2022-06-13"),
                        experienceInMonths = 8,
                        isConsultant = false,
                        employeeId = "SR0046",
                        status = true,
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Backend",
                        designationId = 2,
                        designationName = "sd2",
                        roleId = 1,
                        roleName = "software engineer",
                        firstManagerId = 4,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        employeeNameWithEmployeeId = "Moly Agarwal (SR0046)",
                    ),
                    EmpData(
                        organisationId = 1,
                        id = 3,
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        emailId = "rushad.shaikh@scalereal.com",
                        contactNo = "+917389566666",
                        genderId = EmployeeGender.OTHERS.genderId,
                        dateOfBirth = Date.valueOf("1998-05-01"),
                        dateOfJoining = Date.valueOf("2022-06-13"),
                        experienceInMonths = 8,
                        isConsultant = false,
                        employeeId = "SR0055",
                        status = true,
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Backend",
                        designationId = 2,
                        designationName = "sd2",
                        roleId = 1,
                        roleName = "software engineer",
                        firstManagerId = 4,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        employeeNameWithEmployeeId = "Rushad Shaikh (SR0055)",
                    ),
                )
            val expectedData =
                GendersData(
                    gendersCount =
                        GendersCount(
                            malesCount = 1,
                            femalesCount = 1,
                            othersCount = 1,
                        ),
                    gendersPercentage =
                        GendersPercentage(
                            malesPercentage = 33.33,
                            femalesPercentage = 33.33,
                            othersPercentage = 33.33,
                        ),
                )
            every { employeeService.fetchActiveEmployeesDuringReviewCycle(any(), any()) } returns employeesId

            every { employeeService.getEmployeeById(1L) } returns employeesData[0]
            every { employeeService.getEmployeeById(2L) } returns employeesData[1]
            every { employeeService.getEmployeeById(3L) } returns employeesData[2]

            val gendersData = service.getGendersData(1L, 123L)

            gendersData.gendersCount shouldBe expectedData.gendersCount
            gendersData.gendersPercentage.malesPercentage shouldBe (expectedData.gendersPercentage.malesPercentage plusOrMinus 0.01)
            gendersData.gendersPercentage.femalesPercentage shouldBe (expectedData.gendersPercentage.femalesPercentage plusOrMinus 0.01)
            gendersData.gendersPercentage.othersPercentage shouldBe (expectedData.gendersPercentage.othersPercentage plusOrMinus 0.01)
        }

        "should get gender data 0 if employee gender id is not predefined" {
            val employeesId = listOf(1)

            val employeesData =
                EmpData(
                    organisationId = 1,
                    id = 1,
                    firstName = "Dummy",
                    lastName = "User",
                    emailId = "dummy.user@scalereal.com",
                    contactNo = "+917376743155",
                    genderId = 4,
                    dateOfBirth = Date.valueOf("1998-05-01"),
                    dateOfJoining = Date.valueOf("2022-06-13"),
                    experienceInMonths = 8,
                    isConsultant = false,
                    employeeId = "SR0042",
                    status = true,
                    departmentId = 1,
                    departmentName = "Engineering",
                    teamId = 1,
                    teamName = "Backend",
                    designationId = 2,
                    designationName = "sd2",
                    roleId = 1,
                    roleName = "software engineer",
                    firstManagerId = 4,
                    firstManagerEmployeeId = "SR0006",
                    firstManagerFirstName = "Yogesh",
                    firstManagerLastName = "Jadhav",
                    secondManagerId = null,
                    secondManagerEmployeeId = null,
                    secondManagerFirstName = null,
                    secondManagerLastName = null,
                    employeeNameWithEmployeeId = "Dummy User (SR0042)",
                )
            val expectedData =
                GendersData(
                    gendersCount =
                        GendersCount(
                            malesCount = 0,
                            femalesCount = 0,
                            othersCount = 0,
                        ),
                    gendersPercentage =
                        GendersPercentage(
                            malesPercentage = 00.00,
                            femalesPercentage = 00.00,
                            othersPercentage = 00.00,
                        ),
                )

            every { employeeService.fetchActiveEmployeesDuringReviewCycle(any(), any()) } returns employeesId
            every { employeeService.getEmployeeById(any()) } returns employeesData

            val gendersData = service.getGendersData(1L, 123L)
            gendersData shouldBe expectedData
        }

        "Should return Average Tenure of Employees in a Review Cycle" {
            val employeesId = listOf(1, 2, 3)
            val employeesHistory =
                listOf(
                    EmployeeHistory(
                        historyId = 1,
                        employeeId = 1,
                        activatedAt = Timestamp.valueOf("2021-06-01 19:07:25"),
                        deactivatedAt = Timestamp.valueOf("2023-06-01 19:07:25"),
                    ),
                    EmployeeHistory(
                        historyId = 2,
                        employeeId = 2,
                        activatedAt = Timestamp.valueOf("2022-02-01 19:07:25"),
                        deactivatedAt = Timestamp.valueOf("2023-06-01 19:07:25"),
                    ),
                    EmployeeHistory(
                        historyId = 3,
                        employeeId = 3,
                        activatedAt = Timestamp.valueOf("2022-10-01 19:07:25"),
                        deactivatedAt = Timestamp.valueOf("2023-06-01 19:07:25"),
                    ),
                )

            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 123,
                    startDate = Date.valueOf("2023-01-01"),
                    endDate = Date.valueOf("2023-06-01"),
                    publish = false,
                    lastModified = Timestamp.valueOf("2023-01-08 13:9:48.834129"),
                    selfReviewStartDate = Date.valueOf("2023-02-01"),
                    selfReviewEndDate = Date.valueOf("2023-02-15"),
                    managerReviewStartDate = Date.valueOf("2023-02-20"),
                    managerReviewEndDate = Date.valueOf("2023-03-20"),
                    checkInWithManagerStartDate = Date.valueOf("2023-04-01"),
                    checkInWithManagerEndDate = Date.valueOf("2023-05-01"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val expectedTenure =
                AverageTenure(
                    years = 1,
                    months = 5,
                )

            every { employeeService.fetchActiveEmployeesDuringReviewCycle(any(), any()) } returns employeesId
            every { reviewCycleService.fetchReviewCycle(any()) } returns reviewCycle
            every { employeeService.getEmployeesHistory(any()) } returns employeesHistory

            val averageTenure = service.getAverageTenure(1L, 123L)
            averageTenure shouldBe expectedTenure
        }

        "Should return Average Age of Employees in a Review Cycle" {
            val employeesId = listOf(1, 2, 3)
            val reviewCycleEndDate = Date.valueOf("2023-06-01")
            val employeesData =
                listOf(
                    EmpData(
                        organisationId = 1,
                        id = 1,
                        firstName = "Aamir",
                        lastName = "Islam",
                        emailId = "aamir.islam@scalereal.com",
                        contactNo = "+917376743155",
                        genderId = EmployeeGender.MALE.genderId,
                        dateOfBirth = Date.valueOf("1999-10-01"),
                        dateOfJoining = Date.valueOf("2022-06-13"),
                        experienceInMonths = 8,
                        isConsultant = false,
                        employeeId = "SR0042",
                        status = true,
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Backend",
                        designationId = 2,
                        designationName = "sd2",
                        roleId = 1,
                        roleName = "software engineer",
                        firstManagerId = 4,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        employeeNameWithEmployeeId = "Aamir Islam (SR0042)",
                    ),
                    EmpData(
                        organisationId = 1,
                        id = 2,
                        firstName = "Moly",
                        lastName = "Agarwal",
                        emailId = "moly.agarwal@scalereal.com",
                        contactNo = "+917378783155",
                        genderId = EmployeeGender.FEMALE.genderId,
                        dateOfBirth = Date.valueOf("1998-05-01"),
                        dateOfJoining = Date.valueOf("2022-06-13"),
                        experienceInMonths = 8,
                        isConsultant = false,
                        employeeId = "SR0046",
                        status = true,
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Backend",
                        designationId = 2,
                        designationName = "sd2",
                        roleId = 1,
                        roleName = "software engineer",
                        firstManagerId = 4,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        employeeNameWithEmployeeId = "Moly Agarwal (SR0046)",
                    ),
                    EmpData(
                        organisationId = 1,
                        id = 3,
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        emailId = "rushad.shaikh@scalereal.com",
                        contactNo = "+917389566666",
                        genderId = EmployeeGender.MALE.genderId,
                        dateOfBirth = Date.valueOf("1996-02-13"),
                        dateOfJoining = Date.valueOf("2022-06-13"),
                        experienceInMonths = 8,
                        isConsultant = false,
                        employeeId = "SR0055",
                        status = true,
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Backend",
                        designationId = 2,
                        designationName = "sd2",
                        roleId = 1,
                        roleName = "software engineer",
                        firstManagerId = 4,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        employeeNameWithEmployeeId = "Rushad Shaikh (SR0055)",
                    ),
                )
            val expectedAverageAge = AverageAge(25, 4)

            every { employeeService.fetchActiveEmployeesDuringReviewCycle(any(), any()) } returns employeesId
            every { employeeService.getEmployeeById(1) } returns employeesData[0]
            every { employeeService.getEmployeeById(2) } returns employeesData[1]
            every { employeeService.getEmployeeById(3) } returns employeesData[2]
            every { reviewCycleService.fetchReviewCycle(any()).endDate } returns reviewCycleEndDate

            val averageAge = service.getAverageAge(1L, 123L)
            averageAge shouldBe expectedAverageAge
        }

        "getEmployeeCountByTotalExperience should calculate experience ranges correctly" {
            val employeesId = listOf(1, 2, 3)
            val employeesData =
                listOf(
                    EmpData(
                        organisationId = 1,
                        id = 1,
                        firstName = "Aamir",
                        lastName = "Islam",
                        emailId = "aamir.islam@scalereal.com",
                        contactNo = "+917376743155",
                        genderId = EmployeeGender.MALE.genderId,
                        dateOfBirth = Date.valueOf("1999-10-01"),
                        dateOfJoining = Date.valueOf("2021-09-01"),
                        experienceInMonths = 23,
                        isConsultant = false,
                        employeeId = "SR0042",
                        status = true,
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Backend",
                        designationId = 2,
                        designationName = "sd2",
                        roleId = 1,
                        roleName = "software engineer",
                        firstManagerId = 4,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        employeeNameWithEmployeeId = "Aamir Islam (SR0042)",
                    ),
                    EmpData(
                        organisationId = 1,
                        id = 2,
                        firstName = "Moly",
                        lastName = "Agarwal",
                        emailId = "moly.agarwal@scalereal.com",
                        contactNo = "+917378783155",
                        genderId = EmployeeGender.FEMALE.genderId,
                        dateOfBirth = Date.valueOf("1998-05-01"),
                        dateOfJoining = Date.valueOf("2022-12-01"),
                        experienceInMonths = 8,
                        isConsultant = false,
                        employeeId = "SR0046",
                        status = true,
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Backend",
                        designationId = 2,
                        designationName = "sd2",
                        roleId = 1,
                        roleName = "software engineer",
                        firstManagerId = 4,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        employeeNameWithEmployeeId = "Moly Agarwal (SR0046)",
                    ),
                    EmpData(
                        organisationId = 1,
                        id = 3,
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        emailId = "rushad.shaikh@scalereal.com",
                        contactNo = "+917389566666",
                        genderId = EmployeeGender.MALE.genderId,
                        dateOfBirth = Date.valueOf("1996-02-13"),
                        dateOfJoining = Date.valueOf("2022-12-01"),
                        experienceInMonths = 8,
                        isConsultant = false,
                        employeeId = "SR0055",
                        status = true,
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Backend",
                        designationId = 2,
                        designationName = "sd2",
                        roleId = 1,
                        roleName = "software engineer",
                        firstManagerId = 4,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        employeeNameWithEmployeeId = "Rushad Shaikh (SR0055)",
                    ),
                )
            val employeeHistory1 = EmployeeHistory(1, 1, Timestamp.valueOf("2021-09-01 19:07:25"), null)
            val employeeHistory2 = EmployeeHistory(2, 2, Timestamp.valueOf("2022-12-01 19:07:25"), null)
            val employeeHistory3 = EmployeeHistory(3, 3, Timestamp.valueOf("2022-12-01 19:25:02"), null)
            val reviewCycleEndDate = Date.valueOf(LocalDate.now().minusMonths(4))
            val expectedResult =
                listOf(
                    ExperienceRange("0-1 year", 2),
                    ExperienceRange("1-3 years", 1),
                    ExperienceRange("3-7 years", 0),
                    ExperienceRange("7-10 years", 0),
                    ExperienceRange("10-15 years", 0),
                    ExperienceRange("15-20 years", 0),
                    ExperienceRange("20+ years", 0),
                )
            every { employeeService.fetchActiveEmployeesDuringReviewCycle(any(), any()) } returns employeesId

            every { employeeService.getEmployeeById(1) } returns employeesData[0]
            every { employeeService.getEmployeeById(2) } returns employeesData[1]
            every { employeeService.getEmployeeById(3) } returns employeesData[2]

            every { employeeService.getEmployeesHistory(listOf(1)) } returns listOf(employeeHistory1)
            every { employeeService.getEmployeesHistory(listOf(2)) } returns listOf(employeeHistory2)
            every { employeeService.getEmployeesHistory(listOf(3)) } returns listOf(employeeHistory3)

            every { reviewCycleService.fetchReviewCycle(any()).endDate } returns reviewCycleEndDate

            val result = service.getEmployeeCountByTotalExperience(1, 1)

            result shouldBe expectedResult

            verify { employeeService.fetchActiveEmployeesDuringReviewCycle(1, 1) }
            verify { employeeService.getEmployeeById(1) }
            verify { employeeService.getEmployeesHistory(listOf(1)) }
            verify { reviewCycleService.fetchReviewCycle(1) }
        }

        "Should calculate Employee types and percentages during active review cycle" {
            val employeesId = listOf(1, 2, 3)
            val employeesData =
                listOf(
                    EmpData(
                        organisationId = 1,
                        id = 1,
                        firstName = "Aamir",
                        lastName = "Islam",
                        emailId = "aamir.islam@scalereal.com",
                        contactNo = "+917376743155",
                        genderId = EmployeeGender.MALE.genderId,
                        dateOfBirth = Date.valueOf("1999-10-01"),
                        dateOfJoining = Date.valueOf("2021-09-01"),
                        experienceInMonths = 23,
                        isConsultant = false,
                        employeeId = "SR0042",
                        status = true,
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Backend",
                        designationId = 2,
                        designationName = "sd2",
                        roleId = 1,
                        roleName = "software engineer",
                        firstManagerId = 4,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        employeeNameWithEmployeeId = "Aamir Islam (SR0042)",
                    ),
                    EmpData(
                        organisationId = 1,
                        id = 2,
                        firstName = "Moly",
                        lastName = "Agarwal",
                        emailId = "moly.agarwal@scalereal.com",
                        contactNo = "+917378783155",
                        genderId = EmployeeGender.FEMALE.genderId,
                        dateOfBirth = Date.valueOf("1998-05-01"),
                        dateOfJoining = Date.valueOf("2022-12-01"),
                        experienceInMonths = 8,
                        isConsultant = false,
                        employeeId = "SR0046",
                        status = true,
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Backend",
                        designationId = 2,
                        designationName = "sd2",
                        roleId = 1,
                        roleName = "software engineer",
                        firstManagerId = 4,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        employeeNameWithEmployeeId = "Moly Agarwal (SR0046)",
                    ),
                    EmpData(
                        organisationId = 1,
                        id = 3,
                        firstName = "Rushad",
                        lastName = "Shaikh",
                        emailId = "rushad.shaikh@scalereal.com",
                        contactNo = "+917389566666",
                        genderId = EmployeeGender.MALE.genderId,
                        dateOfBirth = Date.valueOf("1996-02-13"),
                        dateOfJoining = Date.valueOf("2022-12-01"),
                        experienceInMonths = 8,
                        isConsultant = true,
                        employeeId = "SR0055",
                        status = true,
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Backend",
                        designationId = 2,
                        designationName = "sd2",
                        roleId = 1,
                        roleName = "software engineer",
                        firstManagerId = 4,
                        firstManagerEmployeeId = "SR0006",
                        firstManagerFirstName = "Yogesh",
                        firstManagerLastName = "Jadhav",
                        secondManagerId = null,
                        secondManagerEmployeeId = null,
                        secondManagerFirstName = null,
                        secondManagerLastName = null,
                        employeeNameWithEmployeeId = "Rushad Shaikh (SR0055)",
                    ),
                )
            val expectedResult =
                EmployeesType(
                    fullTime = EmployeesTypeData(count = 2, percentage = 66.66),
                    consultant = EmployeesTypeData(count = 1, percentage = 33.33),
                )

            every { employeeService.fetchActiveEmployeesDuringReviewCycle(any(), any()) } returns employeesId
            every { employeeService.getEmployeeById(1) } returns employeesData[0]
            every { employeeService.getEmployeeById(2) } returns employeesData[1]
            every { employeeService.getEmployeeById(3) } returns employeesData[2]

            val result = service.getEmployeesType(1, 1)
            result.fullTime.count shouldBe expectedResult.fullTime.count
            result.fullTime.percentage shouldBe (expectedResult.fullTime.percentage plusOrMinus 0.01)
            result.consultant.count shouldBe expectedResult.consultant.count
            result.consultant.percentage shouldBe (expectedResult.consultant.percentage plusOrMinus 0.01)
        }

        "should return employee count in team during review cycle" {
            val organisationId = 1L
            val reviewCycleId = 1L

            val employeeId = listOf(1, 2)
            val teamSummary1 = mapOf(1L to "Frontend")
            val teamSummary2 = mapOf(2L to "Backend")
            val expectedResult =
                listOf(
                    TeamEmployeeCount("Frontend", 1),
                    TeamEmployeeCount("Backend", 1),
                )

            every { employeeService.fetchActiveEmployeesDuringReviewCycle(any(), any()) } returns employeeId
            every {
                employeeService.fetchEmployeeTeamDuringReviewCycle(organisationId, reviewCycleId, any())
            } returnsMany listOf(teamSummary1, teamSummary2)

            val result = service.getEmployeesCountInTeamDuringReviewCycle(organisationId, reviewCycleId)
            result shouldBe expectedResult
        }
    }
}
