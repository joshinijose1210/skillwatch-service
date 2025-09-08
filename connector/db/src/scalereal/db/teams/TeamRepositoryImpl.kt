package scalereal.db.teams

import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import scalereal.core.models.domain.Team
import scalereal.core.models.domain.TeamStatus
import scalereal.core.teams.TeamRepository
import scalereal.db.util.getWildCardedString
import teams.AddDepartmentTeamMappingCommand
import teams.AddDepartmentTeamMappingParams
import teams.AddNewTeamParams
import teams.AddNewTeamQuery
import teams.GetAllTeamsParams
import teams.GetAllTeamsQuery
import teams.GetAllTeamsResult
import teams.GetMaxTeamIdParams
import teams.GetMaxTeamIdQuery
import teams.GetTeamCountParams
import teams.GetTeamCountQuery
import teams.GetTeamDataByIdParams
import teams.GetTeamDataByIdQuery
import teams.GetTeamDataByIdResult
import teams.GetTeamIdParams
import teams.GetTeamIdQuery
import teams.GetUnlinkedTeamsCountParams
import teams.GetUnlinkedTeamsCountQuery
import teams.IsTeamExistsParams
import teams.IsTeamExistsQuery
import teams.UpdateTeamCommand
import teams.UpdateTeamParams
import javax.sql.DataSource

@Singleton
class TeamRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : TeamRepository {
    override fun create(
        organisationId: Long,
        id: Long,
        departmentId: Long,
        teamName: String,
        teamStatus: Boolean,
    ): Long =
        dataSource.connection.use { connection ->
            val result =
                AddNewTeamQuery()
                    .query(
                        connection,
                        AddNewTeamParams(
                            organisationId = organisationId,
                            id = id,
                            teamName = teamName,
                            teamStatus = teamStatus,
                        ),
                    )

            val teamId =
                result.firstOrNull()?.id
                    ?: throw IllegalStateException("Team insertion failed")

            insertDepartmentTeamMapping(departmentId, teamId)

            teamId
        }

    override fun fetchAll(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
        offset: Int,
        limit: Int,
    ): List<Team> =
        dataSource.connection.use { connection ->
            GetAllTeamsQuery()
                .query(
                    connection,
                    GetAllTeamsParams(
                        organisationId = organisationId,
                        searchText = getWildCardedString(searchText),
                        departmentId = departmentId.toTypedArray(),
                        offset = offset,
                        limit = limit,
                    ),
                ).map { it.toTeam() }
        }

    override fun count(
        organisationId: Long,
        searchText: String,
        departmentId: List<Int>,
    ): Int =
        dataSource.connection.use { connection ->
            GetTeamCountQuery()
                .query(
                    connection,
                    GetTeamCountParams(
                        organisationId = organisationId,
                        searchText = getWildCardedString(searchText),
                        departmentId = departmentId.toTypedArray(),
                    ),
                )[0]
                .teamCount
                ?.toInt() ?: 0
        }

    override fun update(
        organisationId: Long,
        id: Long,
        teamName: String,
        teamStatus: Boolean,
    ): Unit =
        dataSource.connection.use { connection ->
            UpdateTeamCommand()
                .command(
                    connection,
                    UpdateTeamParams(
                        organisationId = organisationId,
                        id = id,
                        teamName = teamName,
                        teamStatus = teamStatus,
                    ),
                )
        }

    override fun isTeamExists(
        organisationId: Long,
        departmentId: Long,
        teamName: String,
    ): TeamStatus =
        dataSource.connection.use { connection ->
            IsTeamExistsQuery()
                .query(
                    connection,
                    IsTeamExistsParams(
                        organisationId = organisationId,
                        departmentId = departmentId,
                        teamName = teamName,
                    ),
                ).map {
                    TeamStatus(
                        exists = requireNotNull(it.exists ?: false),
                        status = requireNotNull(it.status ?: false),
                    )
                }.first()
        }

    override fun getTeamId(
        organisationId: Long,
        departmentId: Long,
        teamName: String,
    ): Long =
        dataSource.connection.use { connection ->
            GetTeamIdQuery()
                .query(
                    connection,
                    GetTeamIdParams(
                        organisationId = organisationId,
                        departmentId = departmentId,
                        teamName = teamName,
                    ),
                ).map { it.id }
                .first()
        }

    override fun getMaxTeamId(organisationId: Long): Long {
        dataSource.connection.use { connection ->
            val maxTeamId =
                GetMaxTeamIdQuery()
                    .query(
                        connection,
                        GetMaxTeamIdParams(organisationId = organisationId),
                    ).map { it.maxId }
            return maxTeamId.firstOrNull() ?: 0
        }
    }

    override fun getTeamDataById(
        id: Long,
        organisationId: Long,
    ): Team =
        dataSource.connection.use { connection ->
            GetTeamDataByIdQuery()
                .query(connection, GetTeamDataByIdParams(id, organisationId))
                .map { it.toTeamData() }
        }[0]

    override fun insertDepartmentTeamMapping(
        departmentId: Long,
        teamId: Long,
    ): Unit =
        dataSource.connection.use { connection ->
            AddDepartmentTeamMappingCommand()
                .command(connection, AddDepartmentTeamMappingParams(departmentId, teamId))
        }

    override fun getUnlinkedTeamsCount(organisationId: Long): Int =
        dataSource.connection.use { connection ->
            GetUnlinkedTeamsCountQuery()
                .query(
                    connection,
                    GetUnlinkedTeamsCountParams(organisationId),
                )[0]
                .unlinkedTeamCount
                ?.toInt() ?: 0
        }

    private fun GetTeamDataByIdResult.toTeamData() =
        Team(
            organisationId = organisationId,
            departmentId = departmentId,
            departmentDisplayId = departmentDisplayId.toString(),
            departmentName = departmentName,
            departmentStatus = departmentStatus,
            id = id,
            teamId = displayId.toString(),
            teamName = teamName,
            teamStatus = status,
            teamCreatedAt = createdAt,
            teamUpdatedAt = updatedAt,
        )

    private fun GetAllTeamsResult.toTeam() =
        Team(
            organisationId = organisationId,
            departmentId = departmentId,
            departmentDisplayId = departmentDisplayId.toString(),
            departmentName = departmentName,
            departmentStatus = departmentStatus,
            id = id,
            teamId = teamId.toString(),
            teamName = teamName,
            teamStatus = status,
            teamCreatedAt = createdAt,
            teamUpdatedAt = updatedAt,
        )
}
