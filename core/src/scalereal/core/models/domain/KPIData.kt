package scalereal.core.models.domain

import java.io.File

data class KPI(
    val organisationId: Long,
    val id: Long,
    var title: String,
    var description: String,
    val kraId: Long,
    val kpiDepartmentTeamDesignations: List<KPIDepartmentTeamDesignations>,
    val status: Boolean,
    val versionNumber: Long,
)

data class KPIDepartmentTeamDesignations(
    val departmentId: Long,
    val teamId: Long,
    val designationIds: List<Long>,
)

data class KPIDepartmentTeamDesignationsData(
    val departmentId: Long?,
    val departmentName: String?,
    val teamId: Long?,
    val teamName: String?,
    val designationIds: List<Long?>,
    val designationNames: List<String?>,
)

data class KPIOldData(
    val organisationId: Long,
    val id: Long,
    val kpiId: String,
    val title: String,
    val description: String,
    val status: Boolean,
    val kraId: Long?,
    val versionNumber: Long,
)

data class KPIData(
    val organisationId: Long,
    val id: Long,
    var kpiId: String,
    val title: String,
    val description: String,
    val status: Boolean,
    val versionNumber: Long,
    val kraId: Long?,
    val kraName: String?,
    var kpiDepartmentTeamDesignations: List<KPIDepartmentTeamDesignationsData>?,
) {
    companion object {
        fun getKPIDisplayId(kpiDisplayId: String) = "KPI${kpiDisplayId.padStart(3, '0')}"
    }
}

data class KPIResponse(
    val totalKPIs: Int,
    val kpis: List<KPIData>,
)

data class KPIImportResponse(
    val file: File,
    val fileCount: Int,
    val errorCount: Int,
)

data class ErrorKPI(
    val kra: String,
    val kpiTitle: String,
    val description: String,
    val status: String,
    val departmentTeamDesignations: List<String>,
    val error: String,
)

data class KPIGroupKey(
    val department: String,
    val team: String,
    val designation: String,
)
