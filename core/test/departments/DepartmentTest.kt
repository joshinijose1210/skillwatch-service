package departments

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import scalereal.core.departments.DefaultDepartment
import scalereal.core.departments.Department

class DepartmentTest :
    StringSpec({

        "should return all default departments" {
            val expected =
                listOf(
                    DefaultDepartment(1, "Executive Leadership"),
                    DefaultDepartment(2, "Human Resource"),
                )

            val result = Department.getDefaults()
            result shouldContainExactly expected
        }

        "should return correct size of department list" {
            Department.getDefaults().size shouldBe 2
        }

        "should contain HR department with id 2" {
            val hr = DefaultDepartment(2, "Human Resource")
            Department.getDefaults().contains(hr) shouldBe true
        }
    })
