package scalereal.core.teams

enum class Team(
    val teamId: Int,
    val teamName: String,
    val departmentId: Int,
) {
    C_SUITE(1, "C-Suite", 1),
    SENIOR_MANAGEMENT(2, "Senior Management", 1),
    PEOPLE_EXPERIENCE(3, "People Experience", 2),
    ;

    companion object {
        fun getTeamsByDepartmentId(departmentId: Int): List<DefaultTeam> =
            entries
                .filter { it.departmentId == departmentId }
                .map { DefaultTeam(it.teamId, it.teamName) }
    }
}

data class DefaultTeam(
    val teamId: Int,
    val teamName: String,
)
