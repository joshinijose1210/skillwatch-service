package scalereal.core.designations

enum class Designation(
    val designationId: Int,
    val designationName: String,
    val teamId: Int,
) {
    CEO(1, "Chief Executive Officer", 1),
    COO(2, "Chief Operating Officer", 1),
    CTO(3, "Chief Technical Officer", 1),
    CPO(4, "Chief People Officer", 1),
    VP_ENGINEERING(5, "VP of Engineering", 2),
    VP_PEOPLE(6, "VP of People Experience", 2),
    HEAD_ENGINEERING(7, "Head of Engineering", 2),
    HEAD_PEOPLE(8, "Head of People Experience", 2),
    MANAGER_PEOPLE_EXP(9, "People Experience Manager", 3),
    ;

    companion object {
        fun getDesignationsByTeamId(teamId: Int): List<DefaultDesignation> =
            entries
                .filter { it.teamId == teamId }
                .map { DefaultDesignation(it.designationId, it.designationName) }
    }
}

data class DefaultDesignation(
    val designationId: Int,
    val designationName: String,
)
