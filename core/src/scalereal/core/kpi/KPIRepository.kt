package scalereal.core.kpi

import scalereal.core.models.domain.KPIData
import scalereal.core.models.domain.KPIDepartmentTeamDesignations
import scalereal.core.models.domain.KPIDepartmentTeamDesignationsData
import scalereal.core.models.domain.KPIOldData

interface KPIRepository {
    fun create(
        organisationId: Long,
        id: Long,
        kpiTitle: String,
        kpiDescription: String,
        kraId: Long,
        kpiDepartmentTeamDesignations: List<KPIDepartmentTeamDesignations>,
        kpiStatus: Boolean,
        versionNumber: Long,
    )

    fun fetchKPIs(
        organisationId: Long,
        searchText: String,
        offset: Int,
        limit: Int,
        departmentId: List<Int>,
        teamId: List<Int>,
        designationId: List<Int>,
        kraId: List<Int>,
        status: List<String>,
    ): List<KPIData>

    fun count(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
        teamId: List<Int>,
        designationId: List<Int>,
        kraId: List<Int>,
        status: List<String>,
    ): Int

    fun fetchKPIByEmployeeId(
        organisationId: Long,
        reviewToId: Long,
    ): List<KPIData>

    fun update(
        organisationId: Long,
        id: Long,
        status: Boolean,
    )

    fun getKPIDepartmentTeamDesignationsMapping(kpiId: Long): List<KPIDepartmentTeamDesignations>

    fun insertKPIDepartmentTeamDesignationsMappings(
        kpiId: Long,
        departmentId: Long,
        teamId: Long,
        designationId: Long,
    )

    fun getMaxKPIId(organisationId: Long): Long

    fun getKPIDataById(id: Long): KPIOldData

    fun getMaxVersionNumber(kpiId: Long): Long

    fun addKPIVersionMapping(
        kpiId: Long,
        versionNumber: Long,
    )

    fun getActiveTeamDesignationOfKPI(kpiId: Long): List<KPIDepartmentTeamDesignationsData>

    fun insertKRAKPIMapping(
        kpiId: Long,
        kraId: Long,
    )

    fun hasReviewStartedForEmployeeWithKPI(
        kpiId: Long,
        reviewCycleId: Long,
    ): Boolean
}
