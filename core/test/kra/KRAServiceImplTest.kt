package kra

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.exception.InvalidDataException
import scalereal.core.kra.KRARepository
import scalereal.core.kra.KRAService
import scalereal.core.kra.KRAs
import scalereal.core.models.domain.GetAllKRAResponse
import scalereal.core.models.domain.KRAData
import scalereal.core.models.domain.OrganisationDetails
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.models.domain.UpdateKRAWeightageRequest
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.organisations.OrganisationRepository
import scalereal.core.reviewCycle.ReviewCycleRepository
import scalereal.core.userActivity.UserActivityRepository
import java.sql.Date
import java.sql.Timestamp
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class KRAServiceImplTest :
    BehaviorSpec({
        val kraRepository = mockk<KRARepository>()
        val userActivityRepository = mockk<UserActivityRepository>()
        val reviewCycleRepository = mockk<ReviewCycleRepository>()
        val moduleService =
            mockk<ModuleService> {
                every { fetchModuleId(Modules.KRAs.moduleName) } returns 6
            }
        val organisationRepository = mockk<OrganisationRepository>()
        val kraService = KRAService(kraRepository, reviewCycleRepository, userActivityRepository, moduleService, organisationRepository)

        Given("An organisation id") {
            val organisationId = 1L

            When("getting all KRAs for the organisation") {
                val kraList =
                    listOf(
                        GetAllKRAResponse(1, "KRA01", "Results", 60, organisationId),
                        GetAllKRAResponse(2, "KRA02", "Skill", 30, organisationId),
                        GetAllKRAResponse(3, "KRA03", "Attitude Fitment", 10, organisationId),
                    )

                every { kraRepository.getAllKRAs(organisationId) } returns kraList

                val result = kraService.getAllKRAs(organisationId)

                Then("it should return the list of KRAs") {
                    result shouldBe kraList
                    result shouldHaveSize 3
                    result.map { it.name } shouldContainAll listOf("Results", "Skill", "Attitude Fitment")
                }

                verify(exactly = 1) { kraRepository.getAllKRAs(organisationId) }
            }
        }

        Given("An organisation id to create default KRAs") {
            val organisationId = 2L

            val maxSrNo = 0L

            When("default KRAs are being created") {
                every { kraRepository.isKRAExists(organisationId, KRAs.RESULTS_NAME) } returns false
                every { kraRepository.isKRAExists(organisationId, KRAs.SKILL_NAME) } returns false
                every { kraRepository.isKRAExists(organisationId, KRAs.ATTITUDE_FITMENT_NAME) } returns false
                every { kraRepository.getMaxSrNo(organisationId) } returns maxSrNo andThen maxSrNo + 1 andThen maxSrNo + 2
                every { kraRepository.createKRA(any()) } returns Unit

                kraService.createDefaultKRAs(organisationId)

                Then("KRAs should be created with correct details") {
                    verify(exactly = 3) { kraRepository.getMaxSrNo(organisationId) }
                    verify(exactly = 1) {
                        kraRepository.createKRA(KRAData(1, KRAs.SKILL_NAME, 30, 1, organisationId))
                        kraRepository.createKRA(KRAData(2, KRAs.RESULTS_NAME, 60, 1, organisationId))
                        kraRepository.createKRA(KRAData(3, KRAs.ATTITUDE_FITMENT_NAME, 10, 1, organisationId))
                    }
                }
            }
        }

        Given("An organisation id and kra weightage update request") {
            clearAllMocks()
            val organisationId = 1L
            val organisationDetails =
                mockk<OrganisationDetails> {
                    every { id } returns organisationId
                    every { timeZone } returns "Asia/Kolkata"
                }
            val organisationCurrentDate = LocalDate.now(ZoneId.of(organisationDetails.timeZone))
            val userActivityData =
                UserActivityData(
                    actionBy = 1,
                    ipAddress = "127.0.0.1",
                )

            val existingKRAs =
                listOf(
                    GetAllKRAResponse(id = 1, kraId = "KRA001", name = "Results", weightage = 60, organisationId = organisationId),
                    GetAllKRAResponse(id = 2, kraId = "KRA002", name = "Skill", weightage = 30, organisationId = organisationId),
                    GetAllKRAResponse(id = 3, kraId = "KRA003", name = "Attitude Fitment", weightage = 10, organisationId = organisationId),
                )
            val reviewCycle =
                ReviewCycle(
                    organisationId = organisationId,
                    reviewCycleId = 1L,
                    startDate = Date.valueOf(organisationCurrentDate.minusDays(10)),
                    endDate = Date.valueOf(organisationCurrentDate.plusDays(10)),
                    publish = true,
                    lastModified = Timestamp.from(Instant.now(Clock.system(ZoneId.of(organisationDetails.timeZone)))),
                    selfReviewStartDate = Date.valueOf(organisationCurrentDate.plusDays(1)),
                    selfReviewEndDate = Date.valueOf(organisationCurrentDate.plusDays(2)),
                    managerReviewStartDate = Date.valueOf(organisationCurrentDate.plusDays(2)),
                    managerReviewEndDate = Date.valueOf(organisationCurrentDate.plusDays(5)),
                    checkInWithManagerStartDate = Date.valueOf(organisationCurrentDate.plusDays(6)),
                    checkInWithManagerEndDate = Date.valueOf(organisationCurrentDate.plusDays(7)),
                    isSelfReviewDatePassed = false,
                    isManagerReviewDatePassed = false,
                    isCheckInWithManagerDatePassed = false,
                ).withActiveFlags(organisationDetails.timeZone)

            every { organisationRepository.getOrganisationDetails(any()) } returns organisationDetails
            every { kraRepository.getAllKRAs(any()) } returns existingKRAs
            every { kraRepository.updateKRAWeightage(any()) } returns Unit
            every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } returns Unit
            every { reviewCycleRepository.fetchActiveReviewCycle(any()) } returns reviewCycle

            When("the weightage is updated correctly") {
                val updateRequests =
                    listOf(
                        UpdateKRAWeightageRequest(id = 1, weightage = 50),
                        UpdateKRAWeightageRequest(id = 2, weightage = 30),
                        UpdateKRAWeightageRequest(id = 3, weightage = 20),
                    )
                kraService.updateKRAWeightage(organisationId, updateRequests, userActivityData)

                Then("the repository should update the KRAs and log the activity") {
                    verify(exactly = 1) { kraRepository.getAllKRAs(organisationId) }
                    verify(exactly = 1) { kraRepository.updateKRAWeightage(updateRequests) }
                    verify(exactly = 2) {
                        userActivityRepository.addActivity(
                            actionBy = userActivityData.actionBy,
                            moduleId = 6,
                            activity = any(),
                            description = any(),
                            ipAddress = userActivityData.ipAddress,
                        )
                    }
                }
            }

            When("the weightage sum is not 100") {
                val invalidUpdateRequests =
                    listOf(
                        UpdateKRAWeightageRequest(id = 1, weightage = 50),
                        UpdateKRAWeightageRequest(id = 2, weightage = 20),
                        UpdateKRAWeightageRequest(id = 3, weightage = 20),
                    )

                Then("an exception should be thrown") {
                    shouldThrow<InvalidDataException> {
                        kraService.updateKRAWeightage(organisationId, invalidUpdateRequests, userActivityData)
                    }.message shouldBe "The total weightage of all KRAs must be 100"
                }
            }

            When("one or more KRAs have weightage less than 1") {
                val invalidWeightageRequests =
                    listOf(
                        UpdateKRAWeightageRequest(id = 1, weightage = 0),
                        UpdateKRAWeightageRequest(id = 2, weightage = 30),
                        UpdateKRAWeightageRequest(id = 3, weightage = 70),
                    )

                Then("an exception should be thrown") {
                    shouldThrow<InvalidDataException> {
                        kraService.updateKRAWeightage(organisationId, invalidWeightageRequests, userActivityData)
                    }.message shouldBe "Each KRA should have at least 1% weightage."
                }
            }

            When("one or more KRA ids are invalid") {
                val invalidUpdateRequests =
                    listOf(
                        UpdateKRAWeightageRequest(id = 999, weightage = 50),
                        UpdateKRAWeightageRequest(id = 2, weightage = 30),
                        UpdateKRAWeightageRequest(id = 3, weightage = 20),
                    )

                Then("an exception should be thrown") {
                    shouldThrow<InvalidDataException> {
                        kraService.updateKRAWeightage(organisationId, invalidUpdateRequests, userActivityData)
                    }.message shouldBe "One or more KRAs not found"
                }
            }

            When("the self-review period is active") {
                val activeReviewCycle =
                    ReviewCycle(
                        organisationId = organisationId,
                        reviewCycleId = 1L,
                        startDate = Date.valueOf(LocalDate.now().minusDays(10)),
                        endDate = Date.valueOf(LocalDate.now().plusDays(10)),
                        publish = true,
                        lastModified = Timestamp.from(Instant.now()),
                        selfReviewStartDate = Date.valueOf(LocalDate.now().minusDays(1)),
                        selfReviewEndDate = Date.valueOf(LocalDate.now().plusDays(1)),
                        managerReviewStartDate = Date.valueOf(LocalDate.now().plusDays(2)),
                        managerReviewEndDate = Date.valueOf(LocalDate.now().plusDays(5)),
                        checkInWithManagerStartDate = Date.valueOf(LocalDate.now().plusDays(6)),
                        checkInWithManagerEndDate = Date.valueOf(LocalDate.now().plusDays(7)),
                        isSelfReviewDatePassed = false,
                        isManagerReviewDatePassed = false,
                        isCheckInWithManagerDatePassed = false,
                    ).withActiveFlags(organisationDetails.timeZone)

                every { organisationRepository.getOrganisationDetails(any()) } returns organisationDetails
                every { reviewCycleRepository.fetchActiveReviewCycle(any()) } returns activeReviewCycle

                val validUpdateRequests =
                    listOf(
                        UpdateKRAWeightageRequest(id = 1, weightage = 50),
                        UpdateKRAWeightageRequest(id = 2, weightage = 30),
                        UpdateKRAWeightageRequest(id = 3, weightage = 20),
                    )

                Then("an exception should be thrown") {
                    shouldThrow<Exception> {
                        kraService.updateKRAWeightage(organisationId, validUpdateRequests, userActivityData)
                    }.message shouldBe "You cannot edit KRA weightage after Self Review Timeline starts"
                }
            }
        }
    })
