package kpi

import io.kotest.core.spec.Spec
import io.kotest.matchers.comparables.beGreaterThan
import io.kotest.matchers.shouldBe
import norm.executeCommand
import scalereal.core.models.domain.KPI
import scalereal.core.models.domain.KPIDepartmentTeamDesignations
import scalereal.core.models.domain.KPIDepartmentTeamDesignationsData
import scalereal.db.kpi.KPIRepositoryImpl
import util.StringSpecWithDataSource
import java.io.File

class KPIRepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var kpiRepositoryImpl: KPIRepositoryImpl

    init {
        "should add new KPI" {
            val kpi =
                KPI(
                    organisationId = 1,
                    id = 1,
                    title = "Task Completion",
                    description = "Should complete the task within estimated period",
                    kraId = 1,
                    kpiDepartmentTeamDesignations =
                        listOf(
                            KPIDepartmentTeamDesignations(
                                departmentId = 1,
                                teamId = 1,
                                designationIds = listOf(4),
                            ),
                        ),
                    status = true,
                    versionNumber = 1,
                )
            kpiRepositoryImpl.create(
                organisationId = kpi.organisationId,
                id = kpi.id,
                kpiTitle = kpi.title,
                kpiDescription = kpi.description,
                kraId = kpi.kraId,
                kpiDepartmentTeamDesignations = kpi.kpiDepartmentTeamDesignations,
                kpiStatus = kpi.status,
                versionNumber = kpi.versionNumber,
            )
        }

        "should add another KPI" {
            val kpi =
                KPI(
                    organisationId = 1,
                    id = 2,
                    title = "Engineering",
                    description = "Should follow clean code",
                    kraId = 1,
                    kpiDepartmentTeamDesignations =
                        listOf(
                            KPIDepartmentTeamDesignations(
                                departmentId = 1,
                                teamId = 1,
                                designationIds = listOf(4),
                            ),
                            KPIDepartmentTeamDesignations(
                                departmentId = 1,
                                teamId = 2,
                                designationIds = listOf(4),
                            ),
                        ),
                    status = false,
                    versionNumber = 1,
                )
            kpiRepositoryImpl.create(
                organisationId = kpi.organisationId,
                id = kpi.id,
                kpiTitle = kpi.title,
                kpiDescription = kpi.description,
                kraId = kpi.kraId,
                kpiDepartmentTeamDesignations = kpi.kpiDepartmentTeamDesignations,
                kpiStatus = kpi.status,
                versionNumber = kpi.versionNumber,
            )
        }

        "should add special characters and numbers in KPI title and description" {
            val kpi =
                KPI(
                    organisationId = 1,
                    id = 3,
                    title = "~`!@#$%^&*()-_\\=+[]{}|;:'<>,./?0987654321",
                    description = "~`!@#$%^&*()-_\\=+[]{}|;:'<>,./?0987654321",
                    kraId = 3,
                    kpiDepartmentTeamDesignations =
                        listOf(
                            KPIDepartmentTeamDesignations(
                                departmentId = 1,
                                teamId = 2,
                                designationIds = listOf(4),
                            ),
                        ),
                    status = true,
                    versionNumber = 1,
                )
            kpiRepositoryImpl.create(
                organisationId = kpi.organisationId,
                id = kpi.id,
                kpiTitle = kpi.title,
                kpiDescription = kpi.description,
                kraId = kpi.kraId,
                kpiDepartmentTeamDesignations = kpi.kpiDepartmentTeamDesignations,
                kpiStatus = kpi.status,
                versionNumber = kpi.versionNumber,
            )
        }

        "should get details of KPIs by indexes" {
            val kpis =
                kpiRepositoryImpl.fetchKPIs(
                    organisationId = 1,
                    searchText = "",
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    departmentId = listOf(-99),
                    teamId = listOf(-99),
                    designationId = listOf(-99),
                    kraId = listOf(-99),
                    status = listOf("true", "false"),
                )

            kpis[0].id shouldBe 11
            kpis[0].title shouldBe "Clean Code"
            kpis[0].description shouldBe "test"
            kpis[0].status shouldBe true
            kpis[0].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 2,
                        teamName = "HR Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )

            kpis[1].id shouldBe 10
            kpis[1].title shouldBe "Communication"
            kpis[1].description shouldBe "test"
            kpis[1].status shouldBe true
            kpis[1].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Devops Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )
        }
        "KPIs should be greater than 0" {
            val kpis =
                kpiRepositoryImpl.fetchKPIs(
                    organisationId = 1,
                    searchText = "",
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    departmentId = listOf(-99),
                    teamId = listOf(-99),
                    designationId = listOf(-99),
                    kraId = listOf(-99),
                    status = listOf("true", "false"),
                )

            kpis.size shouldBe beGreaterThan(0)
        }

        "should find KPIs by KPI title" {
            val kpis =
                kpiRepositoryImpl.fetchKPIs(
                    organisationId = 1,
                    searchText = "Communication",
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    departmentId = listOf(-99),
                    teamId = listOf(-99),
                    designationId = listOf(-99),
                    kraId = listOf(-99),
                    status = listOf("true", "false"),
                )

            kpis[0].id shouldBe 10
            kpis[0].title shouldBe "Communication"
            kpis[0].description shouldBe "test"
            kpis[0].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Devops Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )
            kpis[0].status shouldBe true
        }

        "should find KPIs by KPI description" {
            val kpis =
                kpiRepositoryImpl.fetchKPIs(
                    organisationId = 1,
                    searchText = "clean code",
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    departmentId = listOf(-99),
                    teamId = listOf(-99),
                    designationId = listOf(-99),
                    kraId = listOf(-99),
                    status = listOf("true", "false"),
                )

            kpis[0].id shouldBe 11
            kpis[0].title shouldBe "Clean Code"
            kpis[0].description shouldBe "test"
            kpis[0].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 2,
                        teamName = "HR Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )
            kpis[0].status shouldBe true
        }

        "should able to edit KPI" {

            val kpi =
                KPI(
                    organisationId = 1,
                    id = 1,
                    title = "Task Completion",
                    description = "Should complete the task within estimated period",
                    kraId = 1,
                    kpiDepartmentTeamDesignations =
                        listOf(
                            KPIDepartmentTeamDesignations(
                                departmentId = 1,
                                teamId = 1,
                                designationIds = listOf(4),
                            ),
                        ),
                    status = true,
                    versionNumber = 2,
                )
            kpiRepositoryImpl.update(kpi.organisationId, kpi.id, status = false)
            kpiRepositoryImpl.create(
                organisationId = kpi.organisationId,
                id = kpi.id,
                kpiTitle = kpi.title,
                kpiDescription = kpi.description,
                kraId = kpi.kraId,
                kpiDepartmentTeamDesignations = kpi.kpiDepartmentTeamDesignations,
                kpiStatus = kpi.status,
                versionNumber = kpi.versionNumber,
            )
        }
        "should able to update list of teams and kpi Description" {

            val kpi =
                KPI(
                    organisationId = 1,
                    id = 2,
                    title = "Engineering",
                    description = "Should follow clean code and architecture",
                    kraId = 1,
                    kpiDepartmentTeamDesignations =
                        listOf(
                            KPIDepartmentTeamDesignations(
                                departmentId = 1,
                                teamId = 2,
                                designationIds = listOf(4),
                            ),
                        ),
                    status = false,
                    versionNumber = 1,
                )
            kpiRepositoryImpl.update(kpi.organisationId, kpi.id, status = false)
            kpiRepositoryImpl.create(
                organisationId = kpi.organisationId,
                id = kpi.id,
                kpiTitle = kpi.title,
                kpiDescription = kpi.description,
                kraId = kpi.kraId,
                kpiDepartmentTeamDesignations = kpi.kpiDepartmentTeamDesignations,
                kpiStatus = kpi.status,
                versionNumber = kpi.versionNumber,
            )
        }

        "should able to add more teams to list associated with particular kpi" {

            val kpi =
                KPI(
                    organisationId = 1,
                    id = 3,
                    title = "Special Character KPI",
                    description = "~`!@#$%^&*()-_\\=+[]{}|;:'<>,./?0987654321",
                    kraId = 2,
                    kpiDepartmentTeamDesignations =
                        listOf(
                            KPIDepartmentTeamDesignations(
                                departmentId = 1,
                                teamId = 1,
                                designationIds = listOf(4),
                            ),
                            KPIDepartmentTeamDesignations(
                                departmentId = 1,
                                teamId = 2,
                                designationIds = listOf(4),
                            ),
                        ),
                    status = false,
                    versionNumber = 1,
                )
            kpiRepositoryImpl.update(kpi.organisationId, kpi.id, status = false)
            kpiRepositoryImpl.create(
                organisationId = kpi.organisationId,
                id = kpi.id,
                kpiTitle = kpi.title,
                kpiDescription = kpi.description,
                kraId = kpi.kraId,
                kpiDepartmentTeamDesignations = kpi.kpiDepartmentTeamDesignations,
                kpiStatus = kpi.status,
                versionNumber = kpi.versionNumber,
            )
        }

        "should fetch kpi by employee Id" {
            val kpi = kpiRepositoryImpl.fetchKPIByEmployeeId(organisationId = 1, reviewToId = 1)

            kpi[0].id shouldBe 11
            kpi[0].title shouldBe "Clean Code"
            kpi[0].description shouldBe "test"
            kpi[0].status shouldBe true
        }

        "should fetch kpis by KPI status" {
            val kpisCount =
                kpiRepositoryImpl.count(
                    organisationId = 1,
                    searchText = "",
                    departmentId = listOf(-99),
                    teamId = listOf(1, 2),
                    designationId = listOf(-99),
                    kraId = listOf(-99),
                    status = listOf("true"),
                )

            val kpis =
                kpiRepositoryImpl.fetchKPIs(
                    organisationId = 1,
                    searchText = "",
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    departmentId = listOf(-99),
                    teamId = listOf(1, 2),
                    designationId = listOf(-99),
                    kraId = listOf(-99),
                    status = listOf("true"),
                )

            kpisCount shouldBe 3

            kpis[0].id shouldBe 11
            kpis[0].kpiId shouldBe "KPI005"
            kpis[0].title shouldBe "Clean Code"
            kpis[0].description shouldBe "test"
            kpis[0].status shouldBe true
            kpis[0].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 2,
                        teamName = "HR Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )

            kpis[1].id shouldBe 10
            kpis[1].kpiId shouldBe "KPI004"
            kpis[1].title shouldBe "Communication"
            kpis[1].description shouldBe "test"
            kpis[1].status shouldBe true
            kpis[1].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Devops Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )

            kpis[2].id shouldBe 4
            kpis[2].kpiId shouldBe "KPI001"
            kpis[2].title shouldBe "Task Completion"
            kpis[2].description shouldBe "Should complete the task within estimated period"
            kpis[2].status shouldBe true
            kpis[2].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Devops Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )
        }

        "should fetch kpis based on multiple selected teams and their count" {
            val kpisCount =
                kpiRepositoryImpl.count(
                    organisationId = 1,
                    searchText = "",
                    departmentId = listOf(-99),
                    teamId = listOf(1, 2),
                    designationId = listOf(-99),
                    kraId = listOf(-99),
                    status = listOf("true", "false"),
                )

            val kpis =
                kpiRepositoryImpl.fetchKPIs(
                    organisationId = 1,
                    searchText = "",
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    departmentId = listOf(-99),
                    teamId = listOf(1, 2),
                    designationId = listOf(-99),
                    kraId = listOf(-99),
                    status = listOf("true", "false"),
                )

            kpisCount shouldBe 5

            kpis[0].id shouldBe 11
            kpis[0].kpiId shouldBe "KPI005"
            kpis[0].title shouldBe "Clean Code"
            kpis[0].description shouldBe "test"
            kpis[0].status shouldBe true
            kpis[0].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 2,
                        teamName = "HR Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )

            kpis[1].id shouldBe 10
            kpis[1].kpiId shouldBe "KPI004"
            kpis[1].title shouldBe "Communication"
            kpis[1].description shouldBe "test"
            kpis[1].status shouldBe true
            kpis[1].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Devops Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )

            kpis[2].id shouldBe 6
            kpis[2].kpiId shouldBe "KPI003"
            kpis[2].title shouldBe "Special Character KPI"
            kpis[2].description shouldBe "~`!@#$%^&*()-_\\=+[]{}|;:'<>,./?0987654321"
            kpis[2].status shouldBe false
            kpis[2].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 2,
                        teamName = "HR Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Devops Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )

            kpis[3].id shouldBe 5
            kpis[3].kpiId shouldBe "KPI002"
            kpis[3].title shouldBe "Engineering"
            kpis[3].description shouldBe "Should follow clean code and architecture"
            kpis[3].status shouldBe false
            kpis[3].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 2,
                        teamName = "HR Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )

            kpis[4].id shouldBe 4
            kpis[4].kpiId shouldBe "KPI001"
            kpis[4].title shouldBe "Task Completion"
            kpis[4].description shouldBe "Should complete the task within estimated period"
            kpis[4].status shouldBe true
            kpis[4].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Devops Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )
        }

        "should fetch kpis based on single selected team and their count" {
            val kpisCount =
                kpiRepositoryImpl.count(
                    organisationId = 1,
                    searchText = "",
                    departmentId = listOf(-99),
                    teamId = listOf(1),
                    designationId = listOf(-99),
                    kraId = listOf(-99),
                    status = listOf("true", "false"),
                )

            val kpis =
                kpiRepositoryImpl.fetchKPIs(
                    organisationId = 1,
                    searchText = "",
                    offset = 0,
                    limit = Int.MAX_VALUE,
                    departmentId = listOf(-99),
                    teamId = listOf(1),
                    designationId = listOf(-99),
                    kraId = listOf(-99),
                    status = listOf("true", "false"),
                )

            kpisCount shouldBe 3

            kpis[0].id shouldBe 10
            kpis[0].kpiId shouldBe "KPI004"
            kpis[0].title shouldBe "Communication"
            kpis[0].description shouldBe "test"
            kpis[0].status shouldBe true
            kpis[0].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Devops Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )

            kpis[1].id shouldBe 6
            kpis[1].kpiId shouldBe "KPI003"
            kpis[1].title shouldBe "Special Character KPI"
            kpis[1].description shouldBe "~`!@#$%^&*()-_\\=+[]{}|;:'<>,./?0987654321"
            kpis[1].status shouldBe false
            kpis[1].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 2,
                        teamName = "HR Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Devops Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )

            kpis[2].id shouldBe 4
            kpis[2].kpiId shouldBe "KPI001"
            kpis[2].title shouldBe "Task Completion"
            kpis[2].description shouldBe "Should complete the task within estimated period"
            kpis[2].status shouldBe true
            kpis[2].versionNumber shouldBe 2
            kpis[2].kpiDepartmentTeamDesignations shouldBe
                listOf(
                    KPIDepartmentTeamDesignationsData(
                        departmentId = 1,
                        departmentName = "Engineering",
                        teamId = 1,
                        teamName = "Devops Team",
                        designationIds = listOf(4),
                        designationNames = listOf("Designation-1"),
                    ),
                )
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)

        val testDataFile = File("./test-res/kpi/kpi-test-data.sql").readText().trim()
        dataSource.connection.use {
            it.executeCommand(testDataFile)
        }
        kpiRepositoryImpl = KPIRepositoryImpl(dataSource)
    }
}
