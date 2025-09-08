package teams

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import scalereal.core.teams.DefaultTeam
import scalereal.core.teams.Team

class TeamTest :
    StringSpec({

        "should return teams for departmentId = 1" {
            val teams = Team.getTeamsByDepartmentId(1)
            teams shouldContainExactly
                listOf(
                    DefaultTeam(1, "C-Suite"),
                    DefaultTeam(2, "Senior Management"),
                )
        }

        "should return teams for departmentId = 2" {
            val teams = Team.getTeamsByDepartmentId(2)
            teams shouldContainExactly
                listOf(
                    DefaultTeam(3, "People Experience"),
                )
        }

        "should return empty list for invalid departmentId" {
            val teams = Team.getTeamsByDepartmentId(99)
            teams.isEmpty() shouldBe true
        }
    })
