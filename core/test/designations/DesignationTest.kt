package designations

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import scalereal.core.designations.DefaultDesignation
import scalereal.core.designations.Designation

class DesignationTest :
    StringSpec({

        "should return designations for teamId = 1 (C-Suite)" {
            val result = Designation.getDesignationsByTeamId(1)
            val expected =
                listOf(
                    DefaultDesignation(1, "Chief Executive Officer"),
                    DefaultDesignation(2, "Chief Operating Officer"),
                    DefaultDesignation(3, "Chief Technical Officer"),
                    DefaultDesignation(4, "Chief People Officer"),
                )
            result shouldContainExactly expected
        }

        "should return designations for teamId = 2 (Senior Management)" {
            val result = Designation.getDesignationsByTeamId(2)
            val expected =
                listOf(
                    DefaultDesignation(5, "VP of Engineering"),
                    DefaultDesignation(6, "VP of People Experience"),
                    DefaultDesignation(7, "Head of Engineering"),
                    DefaultDesignation(8, "Head of People Experience"),
                )
            result shouldContainExactly expected
        }

        "should return designations for teamId = 3 (People Experience)" {
            val result = Designation.getDesignationsByTeamId(3)
            val expected =
                listOf(
                    DefaultDesignation(9, "People Experience Manager"),
                )
            result shouldContainExactly expected
        }

        "should return empty list for invalid teamId" {
            val result = Designation.getDesignationsByTeamId(99)
            result.isEmpty() shouldBe true
        }
    })
