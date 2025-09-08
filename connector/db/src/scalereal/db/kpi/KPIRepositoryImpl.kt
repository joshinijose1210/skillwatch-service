package scalereal.db.kpi

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kpi.AddKpiDepartmentTeamDesignationsCommand
import kpi.AddKpiDepartmentTeamDesignationsParams
import kpi.AddKpiVersionMappingCommand
import kpi.AddKpiVersionMappingParams
import kpi.AddKraKpiMappingCommand
import kpi.AddKraKpiMappingParams
import kpi.AddNewKpiParams
import kpi.AddNewKpiQuery
import kpi.GetActiveTeamsDesignationsOfKpiParams
import kpi.GetActiveTeamsDesignationsOfKpiQuery
import kpi.GetKpiByEmployeeIdParams
import kpi.GetKpiByEmployeeIdQuery
import kpi.GetKpiByEmployeeIdResult
import kpi.GetKpiDataByIdParams
import kpi.GetKpiDataByIdQuery
import kpi.GetKpisCountParams
import kpi.GetKpisCountQuery
import kpi.GetKpisParams
import kpi.GetKpisQuery
import kpi.GetKpisResult
import kpi.GetKpisTeamsMappingsParams
import kpi.GetKpisTeamsMappingsQuery
import kpi.GetMaxKpiIdParams
import kpi.GetMaxKpiIdQuery
import kpi.GetMaxVersionNumberParams
import kpi.GetMaxVersionNumberQuery
import kpi.HasReviewStartedForEmployeeWithKpiParams
import kpi.HasReviewStartedForEmployeeWithKpiQuery
import kpi.UpdateKpiCommand
import kpi.UpdateKpiParams
import norm.command
import norm.query
import scalereal.core.kpi.KPIRepository
import scalereal.core.models.domain.KPIData
import scalereal.core.models.domain.KPIDepartmentTeamDesignations
import scalereal.core.models.domain.KPIDepartmentTeamDesignationsData
import scalereal.core.models.domain.KPIOldData
import scalereal.db.util.getWildCardedString
import javax.sql.DataSource

@Singleton
class KPIRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : KPIRepository {
    override fun create(
        organisationId: Long,
        id: Long,
        kpiTitle: String,
        kpiDescription: String,
        kraId: Long,
        kpiDepartmentTeamDesignations: List<KPIDepartmentTeamDesignations>,
        kpiStatus: Boolean,
        versionNumber: Long,
    ): Unit =
        dataSource.connection.use { connection ->
            AddNewKpiQuery()
                .query(
                    connection,
                    AddNewKpiParams(
                        organisationId = organisationId,
                        kpiId = id,
                        kpiTitle = kpiTitle,
                        kpiDescription = kpiDescription,
                        kpiStatus = kpiStatus,
                    ),
                ).map {
                    for (departmentTeamDesignations in kpiDepartmentTeamDesignations) {
                        val departmentId = departmentTeamDesignations.departmentId
                        val teamId = departmentTeamDesignations.teamId
                        for (designationId in departmentTeamDesignations.designationIds) {
                            insertKPIDepartmentTeamDesignationsMappings(
                                kpiId = it.id,
                                departmentId = departmentId,
                                teamId = teamId,
                                designationId = designationId,
                            )
                        }
                    }
                    insertKRAKPIMapping(kpiId = it.id, kraId = kraId)
                    addKPIVersionMapping(it.id, versionNumber)
                }.first()
        }

    override fun update(
        organisationId: Long,
        id: Long,
        status: Boolean,
    ): Unit =
        dataSource.connection.use { connection ->
            UpdateKpiCommand()
                .command(
                    connection,
                    UpdateKpiParams(organisationId = organisationId, id = id, status = status),
                )
        }

    override fun getKPIDepartmentTeamDesignationsMapping(kpiId: Long): List<KPIDepartmentTeamDesignations> =
        dataSource.connection.use { connection ->
            GetKpisTeamsMappingsQuery()
                .query(connection, GetKpisTeamsMappingsParams(kpiId))
                .groupBy { Pair(it.departmentId, it.teamId) }
                .map { (key, mappings) ->
                    val (departmentId, teamId) = key
                    KPIDepartmentTeamDesignations(
                        departmentId = departmentId,
                        teamId = teamId,
                        designationIds = mappings.map { it.designationId },
                    )
                }
        }

    override fun insertKPIDepartmentTeamDesignationsMappings(
        kpiId: Long,
        departmentId: Long,
        teamId: Long,
        designationId: Long,
    ) {
        dataSource.connection.use { connection ->
            AddKpiDepartmentTeamDesignationsCommand()
                .command(
                    connection,
                    AddKpiDepartmentTeamDesignationsParams(kpiId, departmentId, teamId, designationId),
                )
        }
    }

    override fun fetchKPIs(
        organisationId: Long,
        searchText: String,
        offset: Int,
        limit: Int,
        departmentId: List<Int>,
        teamId: List<Int>,
        designationId: List<Int>,
        kraId: List<Int>,
        status: List<String>,
    ): List<KPIData> =
        dataSource.connection.use { connection ->
            GetKpisQuery()
                .query(
                    connection,
                    GetKpisParams(
                        organisationId = organisationId,
                        search = getWildCardedString(searchText),
                        departmentId = departmentId.toTypedArray(),
                        teamId = teamId.toTypedArray(),
                        designationId = designationId.toTypedArray(),
                        kraId = kraId.toTypedArray(),
                        status = status.toTypedArray(),
                        offset = offset,
                        limit = limit,
                    ),
                ).map {
                    it.toKpi()
                }
        }

    override fun count(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
        teamId: List<Int>,
        designationId: List<Int>,
        kraId: List<Int>,
        status: List<String>,
    ): Int =
        dataSource.connection.use { connection ->
            GetKpisCountQuery()
                .query(
                    connection,
                    GetKpisCountParams(
                        organisationId = organisationId,
                        search = getWildCardedString(searchText),
                        departmentId = departmentId.toTypedArray(),
                        teamId = teamId.toTypedArray(),
                        designationId = designationId.toTypedArray(),
                        kraId = kraId.toTypedArray(),
                        status = status.toTypedArray(),
                    ),
                )[0]
                .kpiCount
                ?.toInt() ?: 0
        }

    override fun fetchKPIByEmployeeId(
        organisationId: Long,
        reviewToId: Long,
    ): List<KPIData> =
        dataSource.connection.use { connection ->
            GetKpiByEmployeeIdQuery()
                .query(
                    connection,
                    GetKpiByEmployeeIdParams(
                        organisationId = organisationId,
                        reviewToId = reviewToId,
                    ),
                ).map { it.toKpiData() }
        }

    override fun getMaxKPIId(organisationId: Long): Long {
        dataSource.connection.use { connection ->
            val maxKpiId =
                GetMaxKpiIdQuery()
                    .query(
                        connection,
                        GetMaxKpiIdParams(organisationId = organisationId),
                    ).map { it.maxId }
            return maxKpiId.firstOrNull() ?: 0
        }
    }

    override fun getKPIDataById(id: Long): KPIOldData =
        dataSource.connection.use { connection ->
            GetKpiDataByIdQuery()
                .query(connection, GetKpiDataByIdParams(id))
                .map {
                    KPIOldData(
                        organisationId = it.organisationId,
                        id = it.id,
                        kpiId = it.displayId.toString(),
                        title = it.title,
                        description = it.description,
                        status = it.status,
                        versionNumber = it.versionNumber,
                        kraId = it.kraId,
                    )
                }.first()
        }

    override fun getMaxVersionNumber(kpiId: Long): Long =
        dataSource.connection.use { connection ->
            val maxVersion =
                GetMaxVersionNumberQuery()
                    .query(connection, GetMaxVersionNumberParams(kpiId))
                    .map { it.versionNumber }
            return maxVersion.firstOrNull() ?: 0
        }

    override fun addKPIVersionMapping(
        kpiId: Long,
        versionNumber: Long,
    ): Unit =
        dataSource.connection.use { connection ->
            AddKpiVersionMappingCommand()
                .command(connection, AddKpiVersionMappingParams(kpiId, versionNumber))
        }

    override fun insertKRAKPIMapping(
        kpiId: Long,
        kraId: Long,
    ): Unit =
        dataSource.connection.use { connection ->
            AddKraKpiMappingCommand()
                .command(
                    connection,
                    AddKraKpiMappingParams(
                        kraId = kraId,
                        kpiId = kpiId,
                    ),
                )
        }

    override fun getActiveTeamDesignationOfKPI(kpiId: Long): List<KPIDepartmentTeamDesignationsData> =
        dataSource.connection.use { connection ->
            GetActiveTeamsDesignationsOfKpiQuery()
                .query(connection, GetActiveTeamsDesignationsOfKpiParams(kpiId = kpiId))
                .groupBy { Pair(it.departmentId, it.teamId) }
                .map { (key, mappings) ->
                    val (departmentId, teamId) = key
                    val firstMapping = mappings.first()
                    val (designationIds, designationNames) =
                        mappings
                            .map { it.designationId to it.designationName }
                            .unzip()
                    KPIDepartmentTeamDesignationsData(
                        departmentId = departmentId,
                        departmentName = firstMapping.departmentName,
                        teamId = teamId,
                        teamName = firstMapping.teamName,
                        designationIds = designationIds,
                        designationNames = designationNames,
                    )
                }
        }

    override fun hasReviewStartedForEmployeeWithKPI(
        kpiId: Long,
        reviewCycleId: Long,
    ): Boolean =
        dataSource.connection.use { connection ->
            HasReviewStartedForEmployeeWithKpiQuery()
                .query(connection, HasReviewStartedForEmployeeWithKpiParams(reviewCycleId, kpiId))
                .firstOrNull()
                ?.hasStartedReview ?: false
        }

    private fun GetKpisResult.toKpi() =
        KPIData(
            organisationId = organisationId,
            id = id,
            kpiId = KPIData.getKPIDisplayId(kpiId.toString()),
            title = title,
            description = description,
            status = status,
            versionNumber = versionNumber,
            kraId = kraId,
            kraName = kraName,
            kpiDepartmentTeamDesignations = getActiveTeamDesignationOfKPI(id),
        )

    private fun GetKpiByEmployeeIdResult.toKpiData() =
        KPIData(
            organisationId = organisationId,
            id = id,
            kpiId = KPIData.getKPIDisplayId(kpiId.toString()),
            title = title,
            description = description,
            kpiDepartmentTeamDesignations = listOf(),
            status = true,
            versionNumber = versionNumber,
            kraId = kraId,
            kraName = kraName,
        )
}
