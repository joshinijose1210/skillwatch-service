package scalereal.core.teams

import scalereal.core.models.domain.Team
import scalereal.core.models.domain.TeamStatus

interface TeamRepository {
    fun create(
        organisationId: Long,
        id: Long,
        departmentId: Long,
        teamName: String,
        teamStatus: Boolean,
    ): Long

    fun fetchAll(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<Team>

    fun count(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
    ): Int

    fun update(
        organisationId: Long,
        id: Long,
        teamName: String,
        teamStatus: Boolean,
    )

    fun isTeamExists(
        organisationId: Long,
        departmentId: Long,
        teamName: String,
    ): TeamStatus

    fun getTeamId(
        organisationId: Long,
        departmentId: Long,
        teamName: String,
    ): Long

    fun getMaxTeamId(organisationId: Long): Long

    fun getTeamDataById(
        id: Long,
        organisationId: Long,
    ): Team

    fun insertDepartmentTeamMapping(
        departmentId: Long,
        teamId: Long,
    )

    fun getUnlinkedTeamsCount(organisationId: Long): Int
}
