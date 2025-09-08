package kpi

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import scalereal.core.departments.DepartmentRepository
import scalereal.core.exception.DuplicateDataException
import scalereal.core.kpi.KPIRepository
import scalereal.core.kpi.KPIService
import scalereal.core.kpi.PdfGeneratorService
import scalereal.core.kra.KRARepository
import scalereal.core.models.domain.KPI
import scalereal.core.models.domain.KPIData
import scalereal.core.models.domain.KPIDepartmentTeamDesignations
import scalereal.core.models.domain.KPIOldData
import scalereal.core.models.domain.ReviewCycle
import scalereal.core.models.domain.UserActivityData
import scalereal.core.modules.ModuleService
import scalereal.core.modules.Modules
import scalereal.core.reviewCycle.ReviewCycleRepository
import scalereal.core.userActivity.UserActivityRepository
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate

class KPIServiceTest :
    BehaviorSpec({
        val kpiRepository = mockk<KPIRepository>()
        val reviewCycleRepository = mockk<ReviewCycleRepository>()
        val userActivityRepository = mockk<UserActivityRepository>()
        val departmentRepository = mockk<DepartmentRepository>()
        val kraRepository = mockk<KRARepository>()
        val pdfGeneratorService = mockk<PdfGeneratorService>()
        val moduleService =
            mockk<ModuleService> {
                every { fetchModuleId(Modules.KPIs.moduleName) } returns 7
            }
        val kpiService =
            KPIService(
                kpiRepository,
                reviewCycleRepository,
                userActivityRepository,
                moduleService,
                departmentRepository,
                kraRepository,
                pdfGeneratorService,
            )

        val userActivityData =
            UserActivityData(
                actionBy = 1L,
                ipAddress = "127.0.0.1",
            )

        val organisationTimeZone = "Asia/Kolkata"

        Given("a KPI to add and publish") {
            val kpi =
                KPI(
                    organisationId = 1,
                    id = -999,
                    title = "Communication",
                    description = "Should have good communication skills",
                    kraId = 1,
                    kpiDepartmentTeamDesignations =
                        listOf(
                            KPIDepartmentTeamDesignations(
                                departmentId = 1,
                                teamId = 1,
                                designationIds = listOf(1),
                            ),
                        ),
                    status = true,
                    versionNumber = 1,
                )

            When("review cycle is active") {
                val reviewCycle =
                    ReviewCycle(
                        organisationId = 1,
                        reviewCycleId = 2,
                        startDate = Date.valueOf(LocalDate.now().minusDays(10)),
                        endDate = Date.valueOf(LocalDate.now().plusDays(10)),
                        publish = true,
                        lastModified = Timestamp.valueOf("2023-01-04 13:9:48.834129"),
                        selfReviewStartDate = Date.valueOf(LocalDate.now().minusDays(5)),
                        selfReviewEndDate = Date.valueOf(LocalDate.now().plusDays(2)),
                        managerReviewStartDate = Date.valueOf(LocalDate.now().plusDays(2)),
                        managerReviewEndDate = Date.valueOf(LocalDate.now().plusDays(3)),
                        checkInWithManagerStartDate = Date.valueOf(LocalDate.now().plusDays(4)),
                        checkInWithManagerEndDate = Date.valueOf(LocalDate.now().plusDays(5)),
                        isSelfReviewDatePassed = false,
                        isManagerReviewDatePassed = false,
                        isCheckInWithManagerDatePassed = false,
                    ).withActiveFlags(organisationTimeZone)
                every { reviewCycleRepository.fetchActiveReviewCycle(kpi.organisationId) } returns reviewCycle
                Then("an exception should be thrown") {
                    shouldThrow<Exception> {
                        kpiService.create(kpi, userActivityData)
                    }.message shouldBe "You cannot add new KPI after Self Review Timeline starts"
                }
            }

            When("no review cycle is active") {
                every { reviewCycleRepository.fetchActiveReviewCycle(kpi.organisationId) } returns null
                every { kpiRepository.getMaxKPIId(kpi.organisationId) } returns 0L
                every { kpiRepository.create(any(), any(), any(), any(), any(), any(), any(), any()) } just Runs
                every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } just Runs

                kpiService.create(kpi, userActivityData)

                Then("KPI should be added and user activity logged") {
                    verify(exactly = 1) {
                        kpiRepository.create(
                            organisationId = kpi.organisationId,
                            id = 1L,
                            kpiTitle = kpi.title,
                            kpiDescription = kpi.description,
                            kraId = kpi.kraId,
                            kpiDepartmentTeamDesignations = kpi.kpiDepartmentTeamDesignations,
                            kpiStatus = kpi.status,
                            versionNumber = kpi.versionNumber,
                        )
                    }
                    verify(exactly = 1) {
                        userActivityRepository.addActivity(
                            actionBy = 1L,
                            moduleId = 7,
                            activity = "KPI001 Added and Published",
                            description = "KPI001 Added and Published",
                            ipAddress = "127.0.0.1",
                        )
                    }
                }
            }

            When("there is a violation of unique index on KPI Department Team Designation mapping") {
                every { reviewCycleRepository.fetchActiveReviewCycle(kpi.organisationId) } returns null
                every { kpiRepository.getMaxKPIId(kpi.organisationId) } returns 0L
                every {
                    kpiRepository.create(any(), any(), any(), any(), any(), any(), any(), any())
                } throws Exception("unique_index_kpi_department_team_department_mapping")

                Then("a DuplicateDataException should be thrown for duplicate KPI Department Team Designation") {
                    val exception =
                        shouldThrow<DuplicateDataException> {
                            kpiService.create(kpi, userActivityData)
                        }
                    exception.message shouldBe "Invalid Input Data. Duplicate KPI Department Team Designation getting inserted"
                }
            }

            When("there is a violation of unique index on KPI version mapping") {
                every { reviewCycleRepository.fetchActiveReviewCycle(kpi.organisationId) } returns null
                every { kpiRepository.getMaxKPIId(kpi.organisationId) } returns 0L
                every {
                    kpiRepository.create(any(), any(), any(), any(), any(), any(), any(), any())
                } throws Exception("idx_unique_kpi_version_mapping")

                Then("a DuplicateDataException should be thrown for duplicate KPI Version") {
                    val exception =
                        shouldThrow<DuplicateDataException> {
                            kpiService.create(kpi, userActivityData)
                        }
                    exception.message shouldBe "Invalid Input Data. Duplicate KPI Version getting inserted"
                }
            }
        }

        Given("a KPI to add and unpublish") {
            val kpi =
                KPI(
                    organisationId = 1,
                    id = -999,
                    title = "Coding",
                    description = "Should follow clean code practices",
                    kraId = 2,
                    kpiDepartmentTeamDesignations =
                        listOf(
                            KPIDepartmentTeamDesignations(
                                departmentId = 1,
                                teamId = 1,
                                designationIds = listOf(1, 2),
                            ),
                        ),
                    status = false,
                    versionNumber = 1,
                )

            When("no review cycle is active") {
                every { reviewCycleRepository.fetchActiveReviewCycle(kpi.organisationId) } returns null
                every { kpiRepository.getMaxKPIId(kpi.organisationId) } returns 0L
                every { kpiRepository.create(any(), any(), any(), any(), any(), any(), any(), any()) } just Runs
                every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } just Runs

                kpiService.create(kpi, userActivityData)

                Then("KPI should be added and user activity logged") {
                    verify(exactly = 1) {
                        kpiRepository.create(
                            organisationId = kpi.organisationId,
                            id = 1L,
                            kpiTitle = kpi.title,
                            kpiDescription = kpi.description,
                            kraId = kpi.kraId,
                            kpiDepartmentTeamDesignations = kpi.kpiDepartmentTeamDesignations,
                            kpiStatus = kpi.status,
                            versionNumber = kpi.versionNumber,
                        )
                    }
                    verify(exactly = 1) {
                        userActivityRepository.addActivity(
                            actionBy = 1L,
                            moduleId = 7,
                            activity = "KPI001 Added and Unpublished",
                            description = "KPI001 Added and Unpublished",
                            ipAddress = "127.0.0.1",
                        )
                    }
                }
            }
        }

        Given("a request to update KPI") {
            val teamDesignationMapping =
                listOf(
                    KPIDepartmentTeamDesignations(
                        departmentId = 1,
                        teamId = 1,
                        designationIds = listOf(1, 2),
                    ),
                )

            val kpiOldData =
                KPIOldData(
                    organisationId = 1L,
                    id = 1L,
                    kpiId = "1",
                    title = "Old KPI",
                    description = "Old Description",
                    status = true,
                    kraId = 1L,
                    versionNumber = 1,
                )
            every { kpiRepository.getKPIDataById(any()) } returns kpiOldData
            every { kpiRepository.getMaxVersionNumber(any()) } returns 1L
            every { kpiRepository.update(any(), any(), any()) } just Runs
            every { kpiRepository.create(any(), any(), any(), any(), any(), any(), any(), any()) } just Runs
            every { userActivityRepository.addActivity(any(), any(), any(), any(), any()) } just Runs

            When("KPI title and description is updated") {
                val kpiNewData =
                    KPI(
                        organisationId = 1L,
                        id = 1L,
                        title = "New KPI",
                        description = "New Description",
                        kraId = 1L,
                        kpiDepartmentTeamDesignations = teamDesignationMapping,
                        status = true,
                        versionNumber = 2,
                    )
                every { kpiRepository.getKPIDepartmentTeamDesignationsMapping(kpiNewData.id) } returns listOf()

                Then("KPI should be updated and user activity logged") {
                    kpiService.update(kpiNewData, userActivityData)

                    verify(exactly = 1) { kpiRepository.update(1, 1, false) }
                    verify(exactly = 1) {
                        kpiRepository.create(
                            organisationId = 1,
                            id = 1,
                            kpiTitle = "New KPI",
                            kpiDescription = "New Description",
                            kraId = 1,
                            kpiDepartmentTeamDesignations = teamDesignationMapping,
                            kpiStatus = true,
                            versionNumber = 2,
                        )
                    }
                    verify(exactly = 1) {
                        userActivityRepository.addActivity(
                            actionBy = 1,
                            moduleId = 7,
                            activity = "KPI001 Edited",
                            description = "KPI001 Edited",
                            ipAddress = "127.0.0.1",
                        )
                    }
                }
            }

            When("KPI details is updated and it is unpublished") {
                val kpiNewData =
                    KPI(
                        organisationId = 1L,
                        id = 1L,
                        title = "New KPI",
                        description = "Description updated",
                        kraId = 1L,
                        kpiDepartmentTeamDesignations = teamDesignationMapping,
                        status = false,
                        versionNumber = 2,
                    )
                every { kpiRepository.getKPIDepartmentTeamDesignationsMapping(kpiNewData.id) } returns listOf()

                Then("KPI should be updated and user activity logged") {
                    kpiService.update(kpiNewData, userActivityData)

                    verify(exactly = 1) {
                        kpiRepository.create(
                            organisationId = 1,
                            id = 1,
                            kpiTitle = "New KPI",
                            kpiDescription = "Description updated",
                            kraId = 1,
                            kpiDepartmentTeamDesignations = teamDesignationMapping,
                            kpiStatus = false,
                            versionNumber = 2,
                        )
                    }
                    verify(exactly = 1) {
                        userActivityRepository.addActivity(
                            actionBy = 1,
                            moduleId = 7,
                            activity = "KPI001 Edited and Unpublished",
                            description = "KPI001 Edited and Unpublished",
                            ipAddress = "127.0.0.1",
                        )
                    }
                }
            }

            When("KPI is unpublished") {
                val kpiNewData =
                    KPI(
                        organisationId = 1L,
                        id = 1L,
                        title = "Old KPI",
                        description = "Old Description",
                        kraId = 1L,
                        kpiDepartmentTeamDesignations = teamDesignationMapping,
                        status = false,
                        versionNumber = 2,
                    )
                every { kpiRepository.getKPIDepartmentTeamDesignationsMapping(kpiNewData.id) } returns teamDesignationMapping

                Then("KPI should be updated and user activity logged") {
                    kpiService.update(kpiNewData, userActivityData)

                    verify(exactly = 3) { kpiRepository.update(1, 1, false) }
                    verify(exactly = 1) {
                        userActivityRepository.addActivity(
                            actionBy = 1,
                            moduleId = 7,
                            activity = "KPI001 Unpublished",
                            description = "KPI001 Unpublished",
                            ipAddress = "127.0.0.1",
                        )
                    }
                }
            }

            When("there is a violation of unique index on KPI Department Team Designation mapping") {
                val kpiNewData =
                    KPI(
                        organisationId = 1L,
                        id = 1L,
                        title = "Updated KPI",
                        description = "Updated Description",
                        kraId = 1L,
                        kpiDepartmentTeamDesignations = teamDesignationMapping,
                        status = true,
                        versionNumber = 2,
                    )
                every { kpiRepository.getKPIDepartmentTeamDesignationsMapping(kpiNewData.id) } returns listOf()
                every {
                    kpiRepository.create(any(), any(), any(), any(), any(), any(), any(), any())
                } throws Exception("unique_index_kpi_department_team_department_mapping")

                Then("a DuplicateDataException should be thrown for duplicate KPI Department Team Designation") {
                    val exception =
                        shouldThrow<DuplicateDataException> {
                            kpiService.update(kpiNewData, userActivityData)
                        }
                    exception.message shouldBe "Invalid Input Data. Duplicate KPI Department Team Designation getting inserted"
                }
            }

            When("there is a violation of unique index on KPI version mapping") {
                val kpiNewData =
                    KPI(
                        organisationId = 1L,
                        id = 1L,
                        title = "Updated KPI",
                        description = "Updated Description",
                        kraId = 1L,
                        kpiDepartmentTeamDesignations = teamDesignationMapping,
                        status = true,
                        versionNumber = 2,
                    )
                every { kpiRepository.getKPIDepartmentTeamDesignationsMapping(kpiNewData.id) } returns listOf()
                every {
                    kpiRepository.create(any(), any(), any(), any(), any(), any(), any(), any())
                } throws Exception("idx_unique_kpi_version_mapping")

                Then("a DuplicateDataException should be thrown for duplicate KPI Version") {
                    val exception =
                        shouldThrow<DuplicateDataException> {
                            kpiService.update(kpiNewData, userActivityData)
                        }
                    exception.message shouldBe "Invalid Input Data. Duplicate KPI Version getting inserted"
                }
            }
        }

        Given("a request to fetch KPIs") {
            val kpiList =
                listOf(
                    KPIData(
                        organisationId = 1L,
                        id = 1L,
                        kpiId = "KPI001",
                        title = "Communication",
                        description = "Should have good communication skills",
                        status = true,
                        versionNumber = 1,
                        kraId = 1L,
                        kraName = "KRA Name",
                        kpiDepartmentTeamDesignations = listOf(),
                    ),
                    KPIData(
                        organisationId = 1L,
                        id = 2L,
                        kpiId = "KPI002",
                        title = "Leadership",
                        description = "Exhibit leadership skills",
                        status = false,
                        versionNumber = 1,
                        kraId = 2L,
                        kraName = "Another KRA",
                        kpiDepartmentTeamDesignations = listOf(),
                    ),
                )

            every {
                kpiRepository.fetchKPIs(any(), any(), any(), any(), any(), any(), any(), any(), any())
            } returns kpiList

            When("valid organisationId, searchText and filters are provided") {
                val result =
                    kpiService.fetchKPIs(
                        organisationId = 1L,
                        searchText = "skills",
                        page = 1,
                        limit = 10,
                        departmentId = listOf(1),
                        teamId = listOf(1),
                        designationId = listOf(1),
                        kraId = listOf(1),
                        status = listOf("ACTIVE"),
                    )

                Then("the correct KPIs should be returned") {
                    result shouldBe kpiList
                    verify(exactly = 1) {
                        kpiRepository.fetchKPIs(
                            organisationId = 1L,
                            searchText = "skills",
                            departmentId = listOf(1),
                            teamId = listOf(1),
                            designationId = listOf(1),
                            kraId = listOf(1),
                            status = listOf("ACTIVE"),
                            offset = 0,
                            limit = 10,
                        )
                    }
                }
            }

            When("no KPIs are found") {
                every {
                    kpiRepository.fetchKPIs(any(), any(), any(), any(), any(), any(), any(), any(), any())
                } returns emptyList()

                val result =
                    kpiService.fetchKPIs(
                        organisationId = 1L,
                        searchText = "",
                        page = 1,
                        limit = 10,
                        departmentId = listOf(),
                        teamId = listOf(),
                        designationId = listOf(),
                        kraId = listOf(),
                        status = listOf(),
                    )

                Then("an empty list should be returned") {
                    result shouldBe emptyList()
                }
            }
        }

        Given("a request to count KPIs") {
            every {
                kpiRepository.count(any(), any(), any(), any(), any(), any(), any())
            } returns 5

            When("valid filters are provided") {
                val result =
                    kpiService.count(
                        organisationId = 1L,
                        searchText = "",
                        departmentId = listOf(1),
                        teamId = listOf(1),
                        designationId = listOf(1),
                        kraId = listOf(1),
                        status = listOf("ACTIVE"),
                    )

                Then("the correct count should be returned") {
                    result shouldBe 5
                    verify(exactly = 1) {
                        kpiRepository.count(
                            organisationId = 1L,
                            searchText = "",
                            departmentId = listOf(1),
                            teamId = listOf(1),
                            designationId = listOf(1),
                            kraId = listOf(1),
                            status = listOf("ACTIVE"),
                        )
                    }
                }
            }

            When("no KPIs match the search criteria") {
                every {
                    kpiRepository.count(any(), any(), any(), any(), any(), any(), any())
                } returns 0

                val result =
                    kpiService.count(
                        organisationId = 1L,
                        searchText = "Non-existent",
                        departmentId = listOf(),
                        teamId = listOf(),
                        designationId = listOf(),
                        kraId = listOf(),
                        status = listOf(),
                    )

                Then("the count should be zero") {
                    result shouldBe 0
                }
            }
        }

        Given("a request to fetch KPIs by employeeId") {
            val kpiListByEmployee =
                listOf(
                    KPIData(
                        organisationId = 1L,
                        id = 1L,
                        kpiId = "KPI001",
                        title = "Communication",
                        description = "Should have good communication skills",
                        kraId = 1L,
                        kpiDepartmentTeamDesignations = listOf(),
                        status = true,
                        versionNumber = 1,
                        kraName = "Skills & Knowledge Growth",
                    ),
                )

            every {
                kpiRepository.fetchKPIByEmployeeId(any(), any())
            } returns kpiListByEmployee

            When("a valid organisationId and employeeId is provided") {
                val result =
                    kpiService.fetchKpiByEmployeeId(
                        organisationId = 1L,
                        reviewToId = 123L,
                    )

                Then("the correct KPIs should be returned") {
                    result shouldBe kpiListByEmployee
                    verify(exactly = 1) {
                        kpiRepository.fetchKPIByEmployeeId(1L, 123L)
                    }
                }
            }

            When("no KPIs are found for the given employeeId") {
                every {
                    kpiRepository.fetchKPIByEmployeeId(any(), any())
                } returns emptyList()

                val result =
                    kpiService.fetchKpiByEmployeeId(
                        organisationId = 1L,
                        reviewToId = 999L,
                    )

                Then("an empty list should be returned") {
                    result shouldBe emptyList()
                }
            }
        }
    })
