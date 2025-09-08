package scalereal.core.designations

import scalereal.core.models.domain.Designation
import scalereal.core.models.domain.DesignationStatus

interface DesignationRepository {
    fun isDesignationExists(
        organisationId: Long?,
        teamId: Long,
        designationName: String,
    ): DesignationStatus

    fun create(
        id: Long,
        organisationId: Long?,
        teamId: Long,
        designationName: String,
        status: Boolean,
    ): Long

    fun fetchAll(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
        teamId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<Designation>

    fun count(
        organisationId: Long,
        searchText: String,
        teamId: List<Int>,
        departmentId: List<Int>,
    ): Int

    fun unlinkedDesignationsCount(organisationId: Long): Int

    fun update(
        organisationId: Long,
        id: Long,
        designationName: String,
        status: Boolean,
    )

    fun getDesignationId(
        organisationId: Long,
        designationName: String,
        teamId: Long,
    ): Long

    fun getMaxDesignationId(organisationId: Long): Long

    fun getDesignationDataById(
        id: Long,
        organisationId: Long,
    ): Designation

    fun insertTeamDesignationMapping(
        designationId: Long,
        teamId: Long,
    )

    fun doAllDesignationsHaveActiveKPIsForEachKRA(organisationId: Long): Boolean
}
