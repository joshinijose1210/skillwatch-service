package kra

import io.kotest.core.spec.Spec
import io.kotest.matchers.shouldBe
import norm.executeCommand
import scalereal.core.models.domain.GetAllKRAResponse
import scalereal.core.models.domain.KRAData
import scalereal.core.models.domain.UpdateKRAWeightageRequest
import scalereal.db.kra.KRARepositoryImpl
import util.StringSpecWithDataSource
import java.io.File

class KRARepositoryImplTest : StringSpecWithDataSource() {
    private lateinit var kraRepositoryImpl: KRARepositoryImpl

    init {

        "should check if KRA exists" {
            val exists = kraRepositoryImpl.isKRAExists(organisationId = 2L, kra = "Skills")
            exists shouldBe true
        }

        "should return false for non-existent KRA" {
            val exists = kraRepositoryImpl.isKRAExists(organisationId = 2L, kra = "NonExistentKRA")
            exists shouldBe false
        }

        "should return max srNo for an organisation" {
            val maxSrNo = kraRepositoryImpl.getMaxSrNo(organisationId = 2L)
            maxSrNo shouldBe 3L
        }

        "should create a new KRA for another organisation" {
            val newKRA =
                KRAData(
                    srNo = 1L,
                    name = "Results",
                    weightage = 100,
                    versionNumber = 1,
                    organisationId = 1L,
                )
            kraRepositoryImpl.createKRA(newKRA)

            val exists = kraRepositoryImpl.isKRAExists(organisationId = 1L, kra = "Results")
            exists shouldBe true
        }

        "should fetch all KRAs for an organisation" {
            val allKRAs = kraRepositoryImpl.getAllKRAs(organisationId = 2L)
            allKRAs.size shouldBe 3
            // Define expected KRAs data
            val expectedKRAs =
                listOf(
                    GetAllKRAResponse(
                        id = 1L,
                        kraId = "KRA01",
                        name = "Results",
                        weightage = 60,
                        organisationId = 2L,
                    ),
                    GetAllKRAResponse(
                        id = 2L,
                        kraId = "KRA02",
                        name = "Skills",
                        weightage = 30,
                        organisationId = 2L,
                    ),
                    GetAllKRAResponse(
                        id = 3L,
                        kraId = "KRA03",
                        name = "Attitude & Fitment",
                        weightage = 10,
                        organisationId = 2L,
                    ),
                )

            allKRAs.zip(expectedKRAs).forEach { (actual, expected) ->
                actual shouldBe expected
            }
        }

        "should update KRA weightage" {
            val kraWeightage =
                listOf(
                    UpdateKRAWeightageRequest(id = 1, weightage = 90),
                    UpdateKRAWeightageRequest(id = 2, weightage = 10),
                    UpdateKRAWeightageRequest(id = 3, weightage = 0),
                )

            kraRepositoryImpl.updateKRAWeightage(kraWeightage)

            val allKRAs = kraRepositoryImpl.getAllKRAs(organisationId = 2L)
            allKRAs.all { it.weightage in listOf(90, 10, 0) } shouldBe true
        }

        "should get KRA id" {
            val id = kraRepositoryImpl.getKRAId(organisationId = 2L, kra = "Results")
            id shouldBe 1L
        }

        "should validate if all KRAs have active KPIs" {
            val allKRAsHaveActiveKpis = kraRepositoryImpl.doAllKRAsHaveActiveKPIs(organisationId = 2L)
            allKRAsHaveActiveKpis shouldBe true
        }

        "should return false if not all KRAs have active KPIs" {
            val allKRAsHaveActiveKpis = kraRepositoryImpl.doAllKRAsHaveActiveKPIs(organisationId = 1L)
            allKRAsHaveActiveKpis shouldBe false
        }
    }

    override fun beforeSpec(spec: Spec) {
        super.beforeSpec(spec)
        val testDataFile = File("./test-res/kra/kra-test-data.sql").readText().trim()
        dataSource.connection.use {
            it.executeCommand(testDataFile)
        }
        kraRepositoryImpl = KRARepositoryImpl(dataSource)
    }
}
