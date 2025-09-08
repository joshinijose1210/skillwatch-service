package scalereal.db.designations

import designations.AddNewDesignationParams
import designations.AddNewDesignationQuery
import designations.AddTeamDesignationMappingCommand
import designations.AddTeamDesignationMappingParams
import designations.DoAllDesignationsHaveActiveKpisForEachKrasParams
import designations.DoAllDesignationsHaveActiveKpisForEachKrasQuery
import designations.GetAllDesignationsParams
import designations.GetAllDesignationsQuery
import designations.GetAllDesignationsResult
import designations.GetDesignationDataByIdParams
import designations.GetDesignationDataByIdQuery
import designations.GetDesignationDataByIdResult
import designations.GetDesignationIdParams
import designations.GetDesignationIdQuery
import designations.GetDesignationsCountParams
import designations.GetDesignationsCountQuery
import designations.GetMaxDesignationIdParams
import designations.GetMaxDesignationIdQuery
import designations.GetUnlinkedDesignationsCountParams
import designations.GetUnlinkedDesignationsCountQuery
import designations.IsDesignationExistsParams
import designations.IsDesignationExistsQuery
import designations.UpdateDesignationsCommand
import designations.UpdateDesignationsParams
import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import scalereal.core.designations.DesignationRepository
import scalereal.core.models.domain.Designation
import scalereal.core.models.domain.DesignationStatus
import scalereal.db.util.getWildCardedString
import javax.sql.DataSource
import kotlin.collections.firstOrNull

@Singleton
class DesignationRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : DesignationRepository {
    override fun isDesignationExists(
        organisationId: Long?,
        teamId: Long,
        designationName: String,
    ): DesignationStatus =
        dataSource.connection.use { connection ->
            IsDesignationExistsQuery()
                .query(
                    connection,
                    IsDesignationExistsParams(organisationId = organisationId, teamId = teamId, designationName = designationName),
                ).map {
                    DesignationStatus(
                        organisationId = organisationId,
                        exists = requireNotNull(it.exists ?: false),
                        status = requireNotNull(it.status ?: false),
                    )
                }.first()
        }

    override fun create(
        id: Long,
        organisationId: Long?,
        teamId: Long,
        designationName: String,
        status: Boolean,
    ): Long =
        dataSource.connection.use { connection ->
            val result =
                AddNewDesignationQuery()
                    .query(
                        connection,
                        AddNewDesignationParams(
                            organisationId = organisationId,
                            designationId = id,
                            designationName = designationName,
                            status = status,
                        ),
                    )

            val designationId =
                result.firstOrNull()?.id
                    ?: throw IllegalStateException("Team insertion failed")

            insertTeamDesignationMapping(designationId, teamId)

            designationId
        }

    override fun insertTeamDesignationMapping(
        designationId: Long,
        teamId: Long,
    ) {
        dataSource.connection.use { connection ->
            AddTeamDesignationMappingCommand()
                .command(
                    connection,
                    AddTeamDesignationMappingParams(designationId = designationId, teamId = teamId),
                )
        }
    }

    override fun fetchAll(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
        teamId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<Designation> =
        dataSource.connection.use { connection ->
            GetAllDesignationsQuery()
                .query(
                    connection,
                    GetAllDesignationsParams(
                        organisationId = organisationId,
                        searchText = getWildCardedString(searchText),
                        departmentId = departmentId.toTypedArray(),
                        teamId = teamId.toTypedArray(),
                        offset = offset,
                        limit = limit,
                    ),
                ).map {
                    it.toDesignation()
                }
        }

    override fun count(
        organisationId: Long,
        searchText: String,
        teamId: List<Int>,
        departmentId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetDesignationsCountQuery()
                .query(
                    connection,
                    GetDesignationsCountParams(
                        organisationId = organisationId,
                        searchText = getWildCardedString(searchText),
                        departmentId = departmentId.toTypedArray(),
                        teamId = teamId.toTypedArray(),
                    ),
                )[0]
                .designationCount
                ?.toInt() ?: 0
        }

    override fun unlinkedDesignationsCount(organisationId: Long): Int =
        dataSource.connection.use { connection ->
            GetUnlinkedDesignationsCountQuery()
                .query(
                    connection,
                    GetUnlinkedDesignationsCountParams(organisationId = organisationId),
                )[0]
                .unlinkedDesignationCount
                ?.toInt() ?: 0
        }

    override fun update(
        organisationId: Long,
        id: Long,
        designationName: String,
        status: Boolean,
    ): Unit =
        dataSource.connection.use { connection ->
            UpdateDesignationsCommand()
                .command(
                    connection,
                    UpdateDesignationsParams(
                        organisationId = organisationId,
                        id = id,
                        designationName = designationName,
                        status = status,
                    ),
                )
        }

    override fun getDesignationId(
        organisationId: Long,
        designationName: String,
        teamId: Long,
    ): Long =
        dataSource.connection.use { connection ->
            GetDesignationIdQuery()
                .query(
                    connection,
                    GetDesignationIdParams(teamId = teamId, designationName = designationName, organisationId = organisationId),
                ).firstNotNullOf { it.id }
        }

    override fun getMaxDesignationId(organisationId: Long): Long {
        dataSource.connection.use { connection ->
            val maxDesignationId =
                GetMaxDesignationIdQuery()
                    .query(
                        connection,
                        GetMaxDesignationIdParams(organisationId = organisationId),
                    ).map { it.maxId }
            return maxDesignationId.firstOrNull() ?: 0
        }
    }

    override fun getDesignationDataById(
        id: Long,
        organisationId: Long,
    ): Designation =
        dataSource.connection.use { connection ->
            GetDesignationDataByIdQuery()
                .query(connection, GetDesignationDataByIdParams(id, organisationId))
                .map { it.toDesignationData() }
        }[0]

    override fun doAllDesignationsHaveActiveKPIsForEachKRA(organisationId: Long): Boolean =
        dataSource.connection.use { connection ->
            DoAllDesignationsHaveActiveKpisForEachKrasQuery()
                .query(connection, DoAllDesignationsHaveActiveKpisForEachKrasParams(organisationId))
                .firstOrNull()
                ?.allDesignationsHaveActiveKpisForKras ?: false
        }

    private fun GetDesignationDataByIdResult.toDesignationData() =
        Designation(
            organisationId = organisationId,
            departmentId = departmentId,
            departmentDisplayId = departmentDisplayId.toString(),
            departmentName = departmentName,
            departmentStatus = departmentStatus,
            teamId = teamId,
            teamName = teamName,
            teamDisplayId = teamDisplayId.toString(),
            teamStatus = teamStatus,
            id = id,
            designationId = designationDisplayId.toString(),
            designationName = designationName,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    private fun GetAllDesignationsResult.toDesignation() =
        Designation(
            organisationId = organisationId,
            departmentId = departmentId,
            departmentDisplayId = departmentDisplayId.toString(),
            departmentName = departmentName,
            departmentStatus = departmentStatus,
            teamId = teamId,
            teamName = teamName,
            teamDisplayId = teamDisplayId.toString(),
            teamStatus = teamStatus,
            id = id,
            designationId = designationId.toString(),
            designationName = designationName,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
