package reviewCycle

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.exactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.designations.DesignationRepository
import scalereal.core.emails.CheckInWithManagerStartedMail
import scalereal.core.emails.ManagerReviewTimelineStartedMail
import scalereal.core.emails.ReviewCycleStartedEmail
import scalereal.core.emails.ReviewCycleUpdatedEmail
import scalereal.core.emails.SelfReviewTimelineStartedMail
import scalereal.core.employees.EmployeeRepository
import scalereal.core.exception.DateException
import scalereal.core.exception.KraKpiAssociationException
import scalereal.core.exception.ReviewCycleException
import scalereal.core.kra.KRARepository
import scalereal.core.models.domain.ActiveReviewCycle
import scalereal.core.models.domain.CheckInWithManagerData
import scalereal.core.models.domain.CheckInWithManagerParams
import scalereal.core.models.domain.ManagerDetails
import scalereal.core.models.domain.ManagerReviewCycleData
import scalereal.core.models.domain.MyManagerReviewCycleData
import scalereal.core.models.domain.OrganisationDetails
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.models.domain.ReviewCycleTimeline
import scalereal.core.models.domain.StartedReviewCycle
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.reviewCycle.ReviewCycleRepository
import scalereal.core.reviewCycle.ReviewCycleService
import scalereal.core.slack.CheckInSlackNotifications
import scalereal.core.slack.ManagerReviewSlackNotifications
import scalereal.core.slack.ReviewCycleSlackNotifications
import scalereal.core.slack.SelfReviewSlackNotifications
import scalereal.core.userActivity.UserActivityRepository
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.ZoneId

class ReviewCycleServiceImplTest : StringSpec() {
    private val reviewCycleRepository = mockk<ReviewCycleRepository>()
    private val reviewCycleStartedMail = mockk<ReviewCycleStartedEmail>()
    private val reviewCycleUpdatedEmail = mockk<ReviewCycleUpdatedEmail>()
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
    private val organisationTimeZone = "Asia/Kolkata"
    private val reviewCycleService =
        ReviewCycleService(
            reviewCycleRepository,
            reviewCycleStartedMail,
            reviewCycleUpdatedEmail,
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

    init {

        val now = Timestamp(System.currentTimeMillis())
        val organisationCurrentDate = LocalDate.now(ZoneId.of(organisationTimeZone))

        "should fetch all review cycles" {
            val reviewCycle =
                listOf(
                    ReviewCycle(
                        organisationId = 1,
                        reviewCycleId = 1,
                        startDate = Date.valueOf(organisationCurrentDate),
                        endDate = Date.valueOf(organisationCurrentDate.plusDays(5)),
                        publish = true,
                        lastModified =
                            Timestamp.valueOf("2025-06-02 01:53:00.834129"),
                        selfReviewStartDate = Date.valueOf(organisationCurrentDate),
                        selfReviewEndDate = Date.valueOf(organisationCurrentDate.plusDays(1)),
                        managerReviewStartDate = Date.valueOf(organisationCurrentDate.plusDays(1)),
                        managerReviewEndDate = Date.valueOf(organisationCurrentDate.plusDays(2)),
                        checkInWithManagerStartDate = Date.valueOf(organisationCurrentDate.plusDays(3)),
                        checkInWithManagerEndDate = Date.valueOf(organisationCurrentDate.plusDays(5)),
                        isReviewCycleActive = true,
                        isSelfReviewActive = true,
                        isManagerReviewActive = false,
                        isCheckInWithManagerActive = false,
                        isSelfReviewDatePassed = false,
                        isManagerReviewDatePassed = false,
                        isCheckInWithManagerDatePassed = false,
                    ).withActiveFlags(organisationTimeZone),
                )
            val organisationDetails =
                mockk<OrganisationDetails> {
                    every { timeZone } returns "Asia/Kolkata"
                }
            every { organisationRepository.getOrganisationDetails(any()) } returns organisationDetails
            every { reviewCycleRepository.fetch(any(), any(), any()) } returns reviewCycle

            reviewCycleService.fetch(organisationId = 1, page = 1, limit = Int.MAX_VALUE) shouldBe reviewCycle

            verify(exactly = 1) {
                organisationRepository.getOrganisationDetails(1)
            }
            verify(exactly = 1) {
                reviewCycleRepository.fetch(
                    organisationId = 1,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            }
        }

        "should return review cycle count" {
            every { reviewCycleRepository.count(any()) } returns 3
            reviewCycleService.count(organisationId = 1) shouldBe 3
            verify(exactly = 1) { reviewCycleRepository.count(organisationId = 1) }
        }

        "should create new review cycle" {
            val reviewCycle =
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
                ).withActiveFlags(organisationTimeZone)
            val userActivityData = UserActivityData(actionBy = 1, ipAddress = "127.0.0.1")

            every { reviewCycleRepository.create(any()) } returns reviewCycle
            every { userActivity.addActivity(any(), any(), any(), any(), any()) } returns Unit
            every { kraRepository.doAllKRAsHaveActiveKPIs(any()) } returns true
            every { designationRepository.doAllDesignationsHaveActiveKPIsForEachKRA(any()) } returns true
            reviewCycleService.create(reviewCycle, userActivityData) shouldBe Unit
            verify(exactly = 1) { reviewCycleRepository.create(reviewCycle) }
        }

        "should throw exception while creating review cycle and end date is earlier than start date" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 4,
                    startDate = Date.valueOf("2022-12-08"),
                    endDate = Date.valueOf("2022-12-04"),
                    publish = false,
                    lastModified = now,
                    selfReviewStartDate = Date.valueOf("2022-12-09"),
                    selfReviewEndDate = Date.valueOf("2022-12-15"),
                    managerReviewStartDate = Date.valueOf("2022-12-16"),
                    managerReviewEndDate = Date.valueOf("2022-12-18"),
                    checkInWithManagerStartDate = Date.valueOf("2022-11-24"),
                    checkInWithManagerEndDate = Date.valueOf("2022-11-27"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val exception = shouldThrow<DateException> { reviewCycleService.create(reviewCycle, userActivityData) }
            exception.message shouldBe "End date should be greater than start date"
        }

        "should throw exception while creating review cycle and self review end date is earlier than self review start date" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 5,
                    startDate = Date.valueOf("2022-12-11"),
                    endDate = Date.valueOf("2022-12-30"),
                    publish = false,
                    lastModified = now,
                    selfReviewStartDate = Date.valueOf("2022-12-16"),
                    selfReviewEndDate = Date.valueOf("2022-12-15"),
                    managerReviewStartDate = Date.valueOf("2022-12-26"),
                    managerReviewEndDate = Date.valueOf("2022-12-29"),
                    checkInWithManagerStartDate = Date.valueOf("2022-12-24"),
                    checkInWithManagerEndDate = Date.valueOf("2022-12-27"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val exception = shouldThrow<DateException> { reviewCycleService.create(reviewCycle, userActivityData) }
            exception.message shouldBe "Self review End date should be greater than Self review start date"
        }

        "should throw exception while creating review cycle and manager review end date is earlier than manager review start date" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 6,
                    startDate = Date.valueOf("2022-12-09"),
                    endDate = Date.valueOf("2023-02-17"),
                    publish = true,
                    lastModified = now,
                    selfReviewStartDate = Date.valueOf("2022-12-10"),
                    selfReviewEndDate = Date.valueOf("2022-12-20"),
                    managerReviewStartDate = Date.valueOf("2023-01-27"),
                    managerReviewEndDate = Date.valueOf("2023-01-26"),
                    checkInWithManagerStartDate = Date.valueOf("2023-02-01"),
                    checkInWithManagerEndDate = Date.valueOf("2023-02-17"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val exception = shouldThrow<DateException> { reviewCycleService.create(reviewCycle, userActivityData) }
            exception.message shouldBe "Manager review End date should be greater than Manager review start date"
        }

        "should throw exception while creating review cycle and check-in end date is earlier than check-in start date" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 6,
                    startDate = Date.valueOf("2022-12-09"),
                    endDate = Date.valueOf("2023-02-17"),
                    publish = true,
                    lastModified = now,
                    selfReviewStartDate = Date.valueOf("2022-12-10"),
                    selfReviewEndDate = Date.valueOf("2022-12-20"),
                    managerReviewStartDate = Date.valueOf("2023-01-27"),
                    managerReviewEndDate = Date.valueOf("2023-01-29"),
                    checkInWithManagerStartDate = Date.valueOf("2023-02-11"),
                    checkInWithManagerEndDate = Date.valueOf("2023-02-10"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val exception = shouldThrow<DateException> { reviewCycleService.create(reviewCycle, userActivityData) }
            exception.message shouldBe "Check-in End date should be greater than Check-in start date"
        }

        "should throw exception when self review and manager review dates are not in between review cycle dates" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 7,
                    startDate = Date.valueOf("2022-12-09"),
                    endDate = Date.valueOf("2022-12-17"),
                    publish = true,
                    lastModified = Timestamp.valueOf("2022-12-02 13:9:48.834129"),
                    selfReviewStartDate = Date.valueOf("2022-12-01"),
                    selfReviewEndDate = Date.valueOf("2022-12-05"),
                    managerReviewStartDate = Date.valueOf("2023-01-16"),
                    managerReviewEndDate = Date.valueOf("2023-01-25"),
                    checkInWithManagerStartDate = Date.valueOf("2022-12-16"),
                    checkInWithManagerEndDate = Date.valueOf("2022-12-17"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val exception = shouldThrow<DateException> { reviewCycleService.create(reviewCycle, userActivityData) }
            exception.message shouldBe
                "Self review and Manager review dates should be in between review cycle dates"
        }

        "should throw exception when check-in review dates are not in between review cycle dates" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 7,
                    startDate = Date.valueOf("2022-12-01"),
                    endDate = Date.valueOf("2022-12-30"),
                    publish = false,
                    lastModified = Timestamp.valueOf("2022-12-02 13:9:48.834129"),
                    selfReviewStartDate = Date.valueOf("2022-12-01"),
                    selfReviewEndDate = Date.valueOf("2022-12-05"),
                    managerReviewStartDate = Date.valueOf("2022-12-06"),
                    managerReviewEndDate = Date.valueOf("2022-12-20"),
                    checkInWithManagerStartDate = Date.valueOf("2022-11-24"),
                    checkInWithManagerEndDate = Date.valueOf("2022-11-27"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )
            val exception = shouldThrow<DateException> { reviewCycleService.create(reviewCycle, userActivityData) }
            exception.message shouldBe "Check-in dates should be in between review cycle dates"
        }

        "should throw exception while creating review cycle" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf("2022-01-01"),
                    endDate = Date.valueOf("2022-06-30"),
                    publish = true,
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
                )
            val userActivityData = UserActivityData(actionBy = 1, ipAddress = "127.0.0.1")

            every { reviewCycleRepository.create(any()) } throws Exception("Review Cycle overlap : no_overlap_org")
            val exception = shouldThrow<DateException> { reviewCycleService.create(reviewCycle, userActivityData) }
            exception.message shouldBe "Review cycle has already been created for the selected range"
        }

        "should throw exception while creating duplicate review cycle" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf("2022-01-01"),
                    endDate = Date.valueOf("2022-06-30"),
                    publish = true,
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
                )
            val userActivityData = UserActivityData(actionBy = 1, ipAddress = "127.0.0.1")

            every { reviewCycleRepository.create(any()) } throws Exception("Review Cycle overlap : no_overlap_review_cycle")
            val exception = shouldThrow<DateException> { reviewCycleService.create(reviewCycle, userActivityData) }
            exception.message shouldBe "Review cycle has already been created for the selected range"
        }

        "should throw exception while creating review cycle and another review cycle is already active" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf("2022-01-01"),
                    endDate = Date.valueOf("2022-06-30"),
                    publish = true,
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
                )
            val userActivityData = UserActivityData(actionBy = 1, ipAddress = "127.0.0.1")

            every { reviewCycleRepository.create(any()) } throws Exception("review_cycle_organisation_id_publish_idx")
            val exception = shouldThrow<ReviewCycleException> { reviewCycleService.create(reviewCycle, userActivityData) }
            exception.message shouldBe "Another Review Cycle is already active."
        }

        "should throw exception while creating review cycle and all KRAs doesn't have at least one KPI" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf(LocalDate.now().minusDays(10)),
                    endDate = Date.valueOf(LocalDate.now().plusDays(10)),
                    selfReviewStartDate = Date.valueOf(LocalDate.now().minusDays(9)),
                    selfReviewEndDate = Date.valueOf(LocalDate.now().plusDays(1)),
                    managerReviewStartDate = Date.valueOf(LocalDate.now()),
                    managerReviewEndDate = Date.valueOf(LocalDate.now().plusDays(4)),
                    lastModified = now,
                    publish = true,
                    checkInWithManagerStartDate = Date.valueOf(LocalDate.now().plusDays(5)),
                    checkInWithManagerEndDate = Date.valueOf(LocalDate.now().plusDays(8)),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val userActivityData = UserActivityData(actionBy = 1, ipAddress = "127.0.0.1")

            every { reviewCycleRepository.create(any()) } returns reviewCycle
            every { userActivity.addActivity(any(), any(), any(), any(), any()) } returns Unit
            every { kraRepository.doAllKRAsHaveActiveKPIs(any()) } returns false
            every { designationRepository.doAllDesignationsHaveActiveKPIsForEachKRA(any()) } returns false
            val exception = shouldThrow<KraKpiAssociationException> { reviewCycleService.create(reviewCycle, userActivityData) }
            exception.message shouldBe "Each KRA must have at least one KPI assigned to it. " +
                "Please review the KRAs and ensure every KRA has a minimum of one associated KPI to proceed."
        }

        "should throw exception while creating review cycle and each designation doesn't have at least one KPI for every KRA" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf(LocalDate.now().minusDays(10)),
                    endDate = Date.valueOf(LocalDate.now().plusDays(10)),
                    selfReviewStartDate = Date.valueOf(LocalDate.now().minusDays(9)),
                    selfReviewEndDate = Date.valueOf(LocalDate.now().plusDays(1)),
                    managerReviewStartDate = Date.valueOf(LocalDate.now()),
                    managerReviewEndDate = Date.valueOf(LocalDate.now().plusDays(4)),
                    lastModified = now,
                    publish = true,
                    checkInWithManagerStartDate = Date.valueOf(LocalDate.now().plusDays(5)),
                    checkInWithManagerEndDate = Date.valueOf(LocalDate.now().plusDays(8)),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val userActivityData = UserActivityData(actionBy = 1, ipAddress = "127.0.0.1")

            every { reviewCycleRepository.create(any()) } returns reviewCycle
            every { userActivity.addActivity(any(), any(), any(), any(), any()) } returns Unit
            every { kraRepository.doAllKRAsHaveActiveKPIs(any()) } returns true
            every { designationRepository.doAllDesignationsHaveActiveKPIsForEachKRA(any()) } returns false
            val exception = shouldThrow<KraKpiAssociationException> { reviewCycleService.create(reviewCycle, userActivityData) }
            exception.message shouldBe "Each designation must have at least one KPI for every KRA. Please review and update."
        }

        "should update review cycle" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 2,
                    startDate = Date.valueOf("2022-12-10"),
                    endDate = Date.valueOf("2023-3-20"),
                    publish = false,
                    lastModified = Timestamp.valueOf("2022-03-20 13:9:48.834129"),
                    selfReviewStartDate = Date.valueOf("2022-12-15"),
                    selfReviewEndDate = Date.valueOf("2022-12-25"),
                    managerReviewStartDate = Date.valueOf("2022-12-26"),
                    managerReviewEndDate = Date.valueOf("2023-01-10"),
                    checkInWithManagerStartDate = Date.valueOf("2023-02-01"),
                    checkInWithManagerEndDate = Date.valueOf("2023-02-27"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            val userActivityData = UserActivityData(actionBy = 1, ipAddress = "127.0.0.1")

            every { reviewCycleRepository.fetchReviewCycle(any()) } returns reviewCycle
            every { reviewCycleRepository.update(reviewCycle) } returns reviewCycle
            every { userActivity.addActivity(any(), any(), any(), any(), any()) } returns Unit
            reviewCycleService.update(reviewCycle, userActivityData, notifyEmployees = true) shouldBe Unit
            verify(exactly = 1) { reviewCycleRepository.update(reviewCycle) }
        }

        "should throw exception while updating the status of a review cycle if another review cycle is already active" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf("2022-01-01"),
                    endDate = Date.valueOf("2022-06-30"),
                    publish = true,
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
                )
            val userActivityData = UserActivityData(actionBy = 1, ipAddress = "127.0.0.1")

            every { reviewCycleRepository.fetchReviewCycle(any()) } returns reviewCycle
            every { reviewCycleRepository.update(any()) } throws Exception("review_cycle_organisation_id_publish_idx")
            val exception = shouldThrow<ReviewCycleException> { reviewCycleService.update(reviewCycle, userActivityData, true) }
            exception.message shouldBe "Another Review Cycle is already active."
        }

        "should throw exception while updating review cycle for duplicate date range" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf("2022-01-01"),
                    endDate = Date.valueOf("2022-06-30"),
                    publish = true,
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
                )
            val userActivityData = UserActivityData(actionBy = 1, ipAddress = "127.0.0.1")

            every { reviewCycleRepository.fetchReviewCycle(any()) } returns reviewCycle
            every { reviewCycleRepository.update(any()) } throws Exception("no_overlap")
            val exception = shouldThrow<DateException> { reviewCycleService.update(reviewCycle, userActivityData, true) }
            exception.message shouldBe "Review cycle has already been created for the selected range"
        }

        "should throw exception while updating review cycle and dates are overlapping" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf("2022-01-01"),
                    endDate = Date.valueOf("2022-06-30"),
                    publish = true,
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
                )
            val userActivityData = UserActivityData(actionBy = 1, ipAddress = "127.0.0.1")

            every { reviewCycleRepository.fetchReviewCycle(any()) } returns reviewCycle
            every { reviewCycleRepository.update(any()) } throws Exception("Review cycle error : no_overlap_review_cycle")
            val exception = shouldThrow<DateException> { reviewCycleService.update(reviewCycle, userActivityData, true) }
            exception.message shouldBe "Review cycle has already been created for the selected range"
        }

        "should fetch self review cycle" {
            val reviewCycle =
                listOf(
                    ActiveReviewCycle(
                        reviewCycleId = 2,
                        startDate = Date.valueOf(organisationCurrentDate),
                        endDate = Date.valueOf(organisationCurrentDate.plusMonths(1)),
                        selfReviewStartDate = Date.valueOf(organisationCurrentDate),
                        selfReviewEndDate = Date.valueOf(organisationCurrentDate.plusDays(10)),
                        draft = null,
                        publish = null,
                        updatedAt = null,
                        averageRating = (-1.00).toBigDecimal(),
                        isReviewCyclePublish = true,
                        isReviewCycleActive = true,
                        isSelfReviewActive = true,
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
                        isReviewCycleActive = false,
                        isSelfReviewActive = false,
                        isSelfReviewDatePassed = true,
                    ),
                )
            val organisationId = 1L
            val reviewTypeId = listOf(1)
            val reviewToId = listOf(2)
            val reviewFromId = listOf(2)
            val reviewCycleId = listOf(-99)
            every { reviewCycleRepository.fetchSelfReviewCycle(any(), any(), any(), any(), any(), any(), any()) } returns reviewCycle
            reviewCycleService.fetchSelfReviewCycle(
                organisationId = organisationId,
                reviewTypeId = reviewTypeId,
                reviewToId = reviewToId,
                reviewFromId = reviewFromId,
                reviewCycleId = reviewCycleId,
                page = 1,
                limit = Int.MAX_VALUE,
            ) shouldBe reviewCycle
            verify(exactly = 1) {
                reviewCycleRepository.fetchSelfReviewCycle(
                    organisationId = organisationId,
                    reviewTypeId = reviewTypeId,
                    reviewToId = reviewToId,
                    reviewFromId = reviewFromId,
                    reviewCycleId = reviewCycleId,
                    offset = 0,
                    limit = Int.MAX_VALUE,
                )
            }
        }

        "should return count of self review cycle" {
            val organisationId = 1L
            val reviewTypeId = listOf(1)
            val reviewToId = listOf(2)
            val reviewFromId = listOf(2)
            val reviewCycleId = listOf(-99)
            every { reviewCycleRepository.countSelfReviewCycle(any(), any(), any(), any(), any()) } returns 2
            reviewCycleService.countSelfReviewCycle(
                organisationId = organisationId,
                reviewTypeId = reviewTypeId,
                reviewToId = reviewToId,
                reviewFromId = reviewFromId,
                reviewCycleId = reviewCycleId,
            ) shouldBe 2
            verify(exactly = 1) {
                reviewCycleRepository.countSelfReviewCycle(organisationId, reviewTypeId, reviewToId, reviewFromId, reviewCycleId)
            }
        }

        "should fetch manager review cycle data" {
            val managerReviewCycle =
                ManagerReviewCycleData(
                    reviewCycleId = 7,
                    startDate = Date.valueOf("2022-12-09"),
                    endDate = Date.valueOf("2023-02-17"),
                    managerReviewStartDate = Date.valueOf("2023-01-16"),
                    managerReviewEndDate = Date.valueOf("2023-01-25"),
                    team = "BE Developer",
                    reviewToId = 2,
                    reviewToEmployeeId = "SR0051",
                    firstName = "Rushad",
                    lastName = "Shaikh",
                    draft = null,
                    publish = null,
                    averageRating = (-1.00).toBigDecimal(),
                    isReviewCyclePublish = false,
                    isReviewCycleActive = false,
                    isManagerReviewActive = false,
                    isManagerReviewDatePassed = true,
                )
            every {
                reviewCycleRepository.fetchManagerReviewCycle(any(), any(), any(), any(), any(), any(), any(), any(), any())
            } returns listOf(managerReviewCycle)
            reviewCycleService.fetchManagerReviewCycle(
                organisationId = 1,
                reviewTypeId = 2,
                reviewToId = listOf(2),
                reviewFromId = 1,
                reviewCycleId = listOf(-99),
                managerReviewDraft = false,
                managerReviewPublished = true,
                page = 1,
                limit = Int.MAX_VALUE,
            ) shouldBe listOf(managerReviewCycle)
            verify(exactly = 1) {
                reviewCycleRepository.fetchManagerReviewCycle(
                    organisationId = 1,
                    reviewTypeId = 2,
                    reviewToId = listOf(2),
                    reviewFromId = 1,
                    reviewCycleId = listOf(-99),
                    managerReviewDraft = false,
                    managerReviewPublished = true,
                    limit = Int.MAX_VALUE,
                    offset = 0,
                )
            }
        }

        "should return count of manager review cycle" {
            val organisationId = 1L
            val reviewTypeId = 2
            val reviewToId = listOf(2)
            val reviewFromId = 1L
            val reviewCycleId = listOf(-99)
            val managerReviewDraft = false
            val managerReviewPublished = true
            every { reviewCycleRepository.countManagerReviewCycle(any(), any(), any(), any(), any(), any(), any()) } returns 5
            reviewCycleService.countManagerReviewCycle(
                organisationId = organisationId,
                reviewTypeId = reviewTypeId,
                reviewToId = reviewToId,
                reviewFromId = reviewFromId,
                reviewCycleId = reviewCycleId,
                managerReviewDraft = managerReviewDraft,
                managerReviewPublished = managerReviewPublished,
            ) shouldBe 5
            verify(exactly = 1) {
                reviewCycleRepository.countManagerReviewCycle(
                    organisationId,
                    reviewTypeId,
                    reviewToId,
                    reviewFromId,
                    reviewCycleId,
                    managerReviewDraft,
                    managerReviewPublished,
                )
            }
        }

        "should fetch my manager review cycle list" {
            val myManagerReviewCycle =
                MyManagerReviewCycleData(
                    reviewCycleId = 7,
                    startDate = Date.valueOf(organisationCurrentDate.minusDays(10)),
                    endDate = Date.valueOf(organisationCurrentDate.plusDays(10)),
                    managerReviewStartDate = Date.valueOf(organisationCurrentDate.minusDays(5)),
                    managerReviewEndDate = Date.valueOf(organisationCurrentDate.plusDays(5)),
                    reviewToId = 2,
                    reviewToEmployeeId = "SR0051",
                    firstName = "Rushad",
                    lastName = "Shaikh",
                    reviewFromId = 3,
                    reviewFromEmployeeId = "SR0006",
                    managerFirstName = "Yogesh",
                    managerLastName = "Jadhav",
                    team = "BE Developer",
                    draft = null,
                    publish = null,
                    isReviewCyclePublish = true,
                    averageRating = (-1.00).toBigDecimal(),
                    isReviewCycleActive = true,
                    isManagerReviewActive = true,
                    isManagerReviewDatePassed = false,
                )
            every {
                reviewCycleRepository.fetchMyManagerReviewCycle(any(), any(), any(), any(), any(), any(), any())
            } returns listOf(myManagerReviewCycle)
            reviewCycleService.fetchMyManagerReviewCycle(
                organisationId = 1,
                reviewTypeId = 2,
                reviewToId = 2,
                reviewFromId = listOf(3),
                reviewCycleId = listOf(-99),
                page = 1,
                limit = Int.MAX_VALUE,
            ) shouldBe listOf(myManagerReviewCycle)
            verify(exactly = 1) {
                reviewCycleRepository.fetchMyManagerReviewCycle(
                    organisationId = 1,
                    reviewTypeId = 2,
                    reviewToId = 2,
                    reviewFromId = listOf(3),
                    reviewCycleId = listOf(-99),
                    limit = Int.MAX_VALUE,
                    offset = 0,
                )
            }
        }

        "should return my manager review cycle count" {
            every { reviewCycleRepository.countMyManagerReviewCycle(any(), any(), any(), any(), any()) } returns 2
            reviewCycleService.countMyManagerReviewCycle(
                organisationId = 1,
                reviewTypeId = 2,
                reviewToId = 2,
                reviewFromId = listOf(3),
                reviewCycleId = listOf(-99),
            ) shouldBe 2
            verify(exactly = 1) {
                reviewCycleRepository.countMyManagerReviewCycle(
                    organisationId = 1,
                    reviewTypeId = 2,
                    reviewToId = 2,
                    reviewFromId = listOf(3),
                    reviewCycleId = listOf(-99),
                )
            }
        }

        "should fetch check in with manager list" {
            val checkInReview =
                listOf(
                    CheckInWithManagerData(
                        reviewCycleId = 1,
                        startDate = Date.valueOf("2025-01-05"),
                        endDate = Date.valueOf(organisationCurrentDate.plusDays(10)),
                        publish = false,
                        checkInStartDate = Date.valueOf("2025-02-16"),
                        checkInEndDate = Date.valueOf(organisationCurrentDate.plusDays(10)),
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
                        isReviewCycleActive = false,
                        isCheckInWithManagerActive = false,
                        isCheckInWithManagerDatePassed = false,
                    ),
                )
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 1,
                    startDate = Date.valueOf("2025-01-05"),
                    endDate = Date.valueOf(organisationCurrentDate.plusDays(10)),
                    publish = true,
                    lastModified = Timestamp.valueOf("2025-01-01 13:9:48.834129"),
                    selfReviewStartDate = Date.valueOf("2025-01-05"),
                    selfReviewEndDate = Date.valueOf("2025-01-25"),
                    managerReviewStartDate = Date.valueOf("2025-01-26"),
                    managerReviewEndDate = Date.valueOf("2025-02-15"),
                    checkInWithManagerStartDate = Date.valueOf("2025-02-16"),
                    checkInWithManagerEndDate = Date.valueOf(organisationCurrentDate.plusDays(10)),
                    isReviewCycleActive = true,
                    isSelfReviewDatePassed = true,
                    isManagerReviewDatePassed = true,
                    isCheckInWithManagerDatePassed = false,
                )
            val checkInParams =
                CheckInWithManagerParams(
                    organisationId = 1,
                    managerId = listOf(1),
                    reviewCycleId = listOf(1),
                    reviewToId = listOf(2),
                    teamId = listOf(1),
                    selfReviewDraft = false,
                    selfReviewPublish = true,
                    firstManagerReviewDraft = false,
                    firstManagerReviewPublish = true,
                    secondManagerReviewDraft = false,
                    secondManagerReviewPublish = false,
                    checkInDraft = false,
                    checkInPublished = true,
                    minFilterRating = 4.0,
                    maxFilterRating = 4.9,
                    filterRatingId = 4,
                )

            val currentManagerDetails =
                ManagerDetails(
                    firstManagerId = 3,
                    firstManagerEmployeeId = "SR0006",
                    firstManagerFirstName = "Yogesh",
                    firstManagerLastName = "Jadhav",
                    secondManagerId = null,
                    secondManagerEmployeeId = null,
                    secondManagerFirstName = null,
                    secondManagerLastName = null,
                )
            every { reviewCycleRepository.fetchCheckInWithManager(any()) } returns checkInReview
            every { reviewCycleRepository.fetchReviewCycle(any()) } returns reviewCycle
            every { employeeRepository.getCurrentManagerDetails(any(), any()) } returns currentManagerDetails
            reviewCycleService.fetchCheckInWithManager(
                checkInWithManagerParams = checkInParams,
                sortRating = "asc",
                page = 1,
                limit = 10,
            ) shouldBe checkInReview
            verify(exactly = 1) { reviewCycleRepository.fetchCheckInWithManager(checkInParams) }
        }

        "should return count of check in with manager data" {
            val checkInParams =
                CheckInWithManagerParams(
                    organisationId = 1,
                    managerId = listOf(1),
                    reviewCycleId = listOf(1),
                    reviewToId = listOf(2),
                    teamId = listOf(1),
                    selfReviewDraft = false,
                    selfReviewPublish = true,
                    firstManagerReviewDraft = false,
                    firstManagerReviewPublish = true,
                    secondManagerReviewDraft = false,
                    secondManagerReviewPublish = false,
                    checkInDraft = false,
                    checkInPublished = true,
                    minFilterRating = 4.0,
                    maxFilterRating = 4.9,
                    filterRatingId = 4,
                )
            every { reviewCycleRepository.countCheckInWithManager(any()) } returns 2
            reviewCycleService.countCheckInWithManager(checkInWithManagerParams = checkInParams) shouldBe 2
            verify(exactly = 1) { reviewCycleRepository.countCheckInWithManager(checkInParams = checkInParams) }
        }

        "should fetch review cycle timeline data" {
            val reviewCycleTimeline =
                ReviewCycleTimeline(
                    organisationId = 1,
                    reviewCycleId = 2,
                    startDate = Date.valueOf("2022-12-01"),
                    endDate = Date.valueOf("2022-12-31"),
                    publish = true,
                    selfReviewStartDate = Date.valueOf("2022-12-05"),
                    selfReviewEndDate = Date.valueOf("2022-12-12"),
                    managerReviewStartDate = Date.valueOf("2022-12-16"),
                    managerReviewEndDate = Date.valueOf("2022-12-23"),
                    checkInWithManagerStartDate = Date.valueOf("2022-12-24"),
                    checkInWithManagerEndDate = Date.valueOf("2022-12-30"),
                    checkInWithManagerDate = Timestamp.valueOf("2022-12-20 13:9:48.834129"),
                    checkInFromId = 1,
                    checkInFromEmployeeId = "SR0006",
                    checkInFromFirstName = "Yogesh",
                    checkInFromLastName = "Jadhav",
                    checkInWithManagerDraft = true,
                    checkInWithManagerPublish = false,
                    selfReviewDraft = true,
                    selfReviewPublish = false,
                    selfReviewDate = Timestamp.valueOf("2022-12-20 13:9:48.834129"),
                    firstManagerId = 1,
                    firstManagerEmployeeId = "SR0006",
                    firstManagerFirstName = "Yogesh",
                    firstManagerLastName = "Jadhav",
                    firstManagerReviewDraft = true,
                    firstManagerReviewPublish = false,
                    firstManagerReviewDate = Timestamp.valueOf("2022-12-20 13:9:48.834129"),
                    secondManagerId = null,
                    secondManagerEmployeeId = null,
                    secondManagerFirstName = null,
                    secondManagerLastName = null,
                    secondManagerReviewDraft = false,
                    secondManagerReviewPublish = false,
                    secondManagerReviewDate = null,
                    empDetails = listOf(),
                    selfAverageRating = (-1.00).toBigDecimal(),
                    firstManagerAverageRating = (-1.00).toBigDecimal(),
                    secondManagerAverageRating = (-1.00).toBigDecimal(),
                    checkInWithManagerAverageRating = (-1.00).toBigDecimal(),
                    isOrWasManager = false,
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                ).withActiveFlags(organisationTimeZone)
            every { reviewCycleRepository.fetchReviewCycleData(any(), any()) } returns listOf(reviewCycleTimeline)
            reviewCycleService.fetchReviewCycleData(organisationId = 1, reviewToId = 1) shouldBe listOf(reviewCycleTimeline)
            verify(exactly = 1) { reviewCycleRepository.fetchReviewCycleData(organisationId = 1, reviewToId = 1) }
        }

        "should fetch review cycle details by review cycle id" {
            val reviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 2,
                    startDate = Date.valueOf("2022-12-10"),
                    endDate = Date.valueOf("2023-3-20"),
                    publish = false,
                    lastModified = Timestamp.valueOf("2022-03-20 13:9:48.834129"),
                    selfReviewStartDate = Date.valueOf("2022-12-15"),
                    selfReviewEndDate = Date.valueOf("2022-12-25"),
                    managerReviewStartDate = Date.valueOf("2022-12-26"),
                    managerReviewEndDate = Date.valueOf("2023-01-10"),
                    checkInWithManagerStartDate = Date.valueOf("2023-02-01"),
                    checkInWithManagerEndDate = Date.valueOf("2023-02-27"),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            every { reviewCycleRepository.fetchReviewCycle(any()) } returns reviewCycle
            reviewCycleService.fetchReviewCycle(reviewCycleId = 2) shouldBe reviewCycle
        }

        "should verify if review cycle is started on current date" {
            val expectedResult = StartedReviewCycle(exists = true, id = 3)
            every { reviewCycleRepository.isReviewCycleStartedAt(any(), any()) } returns expectedResult
            reviewCycleService.isReviewCycleStartedAt(
                organisationId = 1,
                organisationCurrentDate = Date.valueOf(organisationCurrentDate),
            ) shouldBe expectedResult
        }

        "should verify if self review timeline is started on current date" {
            val expectedResult = StartedReviewCycle(exists = false, id = null)
            every { reviewCycleRepository.isSelfReviewStartedAt(any(), any()) } returns expectedResult
            reviewCycleService.isSelfReviewStartedAt(
                organisationId = 1,
                organisationCurrentDate = Date.valueOf(organisationCurrentDate),
            ) shouldBe expectedResult
        }

        "should verify if manager review timeline is started on current date" {
            val expectedResult = StartedReviewCycle(exists = true, id = 3)
            every { reviewCycleRepository.isManagerReviewStartedAt(any(), any()) } returns expectedResult
            reviewCycleService.isManagerReviewStartedAt(
                organisationId = 1,
                organisationCurrentDate = Date.valueOf(organisationCurrentDate),
            ) shouldBe expectedResult
        }

        "should verify if check in with manager timeline is started on specific date" {
            val expectedResult = StartedReviewCycle(exists = false, id = null)
            every { reviewCycleRepository.isCheckInStartedAt(any(), any()) } returns expectedResult
            reviewCycleService.isCheckInStartedAt(
                organisationId = 1,
                organisationCurrentDate = Date.valueOf(organisationCurrentDate),
            ) shouldBe expectedResult
        }

        "should fetch active review cycle with self review active flag by organisation id" {
            val expectedReviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 3,
                    startDate = Date.valueOf(LocalDate.now().minusDays(1)),
                    endDate = Date.valueOf(LocalDate.now().plusDays(30)),
                    publish = true,
                    lastModified = now,
                    selfReviewStartDate = Date.valueOf(LocalDate.now()),
                    selfReviewEndDate = Date.valueOf(LocalDate.now().plusDays(10)),
                    managerReviewStartDate = Date.valueOf(LocalDate.now().plusDays(11)),
                    managerReviewEndDate = Date.valueOf(LocalDate.now().plusDays(20)),
                    checkInWithManagerStartDate = Date.valueOf(LocalDate.now().plusDays(21)),
                    checkInWithManagerEndDate = Date.valueOf(LocalDate.now().plusDays(30)),
                    isReviewCycleActive = true,
                    isSelfReviewActive = true,
                    isManagerReviewActive = false,
                    isCheckInWithManagerActive = false,
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                )
            every { reviewCycleRepository.fetchActiveReviewCycle(any()) } returns expectedReviewCycle
            reviewCycleService.fetchActiveReviewCycle(organisationId = 1) shouldBe expectedReviewCycle
        }

        "should fetch active review cycle with manager review active flag by organisation id" {
            val expectedReviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 3,
                    startDate = Date.valueOf("2025-01-01"),
                    endDate = Date.valueOf(LocalDate.now().plusDays(20)),
                    publish = true,
                    lastModified = now,
                    selfReviewStartDate = Date.valueOf(LocalDate.now().minusDays(10)),
                    selfReviewEndDate = Date.valueOf(LocalDate.now().minusDays(5)),
                    managerReviewStartDate = Date.valueOf(LocalDate.now()),
                    managerReviewEndDate = Date.valueOf(LocalDate.now().plusDays(5)),
                    checkInWithManagerStartDate = Date.valueOf(LocalDate.now().plusDays(10)),
                    checkInWithManagerEndDate = Date.valueOf(LocalDate.now().plusDays(15)),
                    isReviewCycleActive = true,
                    isSelfReviewActive = false,
                    isManagerReviewActive = true,
                    isCheckInWithManagerActive = false,
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                ).withActiveFlags(organisationTimeZone)
            every { reviewCycleRepository.fetchActiveReviewCycle(any()) } returns expectedReviewCycle
            reviewCycleService.fetchActiveReviewCycle(organisationId = 1) shouldBe expectedReviewCycle
        }

        "should fetch active review cycle with check-in review active flag by organisation id" {
            val expectedReviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 3,
                    startDate = Date.valueOf("2023-07-01"),
                    endDate = Date.valueOf(LocalDate.now().plusDays(1)),
                    publish = true,
                    lastModified = now,
                    selfReviewStartDate = Date.valueOf("2023-09-01"),
                    selfReviewEndDate = Date.valueOf("2023-09-30"),
                    managerReviewStartDate = Date.valueOf("2023-10-01"),
                    managerReviewEndDate = Date.valueOf("2023-10-30"),
                    checkInWithManagerStartDate = Date.valueOf("2023-11-01"),
                    checkInWithManagerEndDate = Date.valueOf(LocalDate.now().plusDays(1)),
                    isReviewCycleActive = true,
                    isSelfReviewActive = false,
                    isManagerReviewActive = false,
                    isCheckInWithManagerActive = true,
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                ).withActiveFlags(organisationTimeZone)
            every { reviewCycleRepository.fetchActiveReviewCycle(any()) } returns expectedReviewCycle
            reviewCycleService.fetchActiveReviewCycle(organisationId = 1) shouldBe expectedReviewCycle
        }

        "should fetch active review cycle when no review submission is active by organisation id" {
            val expectedReviewCycle =
                ReviewCycle(
                    organisationId = 1,
                    reviewCycleId = 3,
                    startDate = Date.valueOf("2023-07-01"),
                    endDate = Date.valueOf("2023-12-30"),
                    publish = true,
                    lastModified = now,
                    selfReviewStartDate = Date.valueOf("2023-09-01"),
                    selfReviewEndDate = Date.valueOf("2023-09-30"),
                    managerReviewStartDate = Date.valueOf("2023-10-01"),
                    managerReviewEndDate = Date.valueOf("2023-10-30"),
                    checkInWithManagerStartDate = Date.valueOf("2023-11-01"),
                    checkInWithManagerEndDate = Date.valueOf("2023-11-30"),
                    isSelfReviewActive = false,
                    isManagerReviewActive = false,
                    isCheckInWithManagerActive = false,
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                ).withActiveFlags(organisationTimeZone)
            every { reviewCycleRepository.fetchActiveReviewCycle(any()) } returns expectedReviewCycle
            reviewCycleService.fetchActiveReviewCycle(organisationId = 1) shouldBe expectedReviewCycle
        }

        "should unpublish review cycle by review cycle id" {
            every { reviewCycleRepository.unPublishReviewCycle(any()) } returns Unit
            reviewCycleService.unPublishReviewCycle(reviewCycleId = 3) shouldBe Unit
        }

        "should fetch previous review cycle id by organisation id" {
            every { reviewCycleRepository.getPreviousReviewCycleId(any()) } returns listOf(6L)
            reviewCycleService.getPreviousReviewCycleId(organisationId = 1) shouldBe listOf(6L)
        }
    }
}
