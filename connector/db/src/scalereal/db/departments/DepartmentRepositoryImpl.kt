package scalereal.db.departments

import departments.AddDepartmentParams
import departments.AddDepartmentQuery
import departments.GetAllDepartmentsCountParams
import departments.GetAllDepartmentsCountQuery
import departments.GetAllDepartmentsParams
import departments.GetAllDepartmentsQuery
import departments.GetDepartmentDataByIdParams
import departments.GetDepartmentDataByIdQuery
import departments.GetDepartmentIdParams
import departments.GetDepartmentIdQuery
import departments.GetMaxDepartmentIdParams
import departments.GetMaxDepartmentIdQuery
import departments.IsDepartmentExistsParams
import departments.IsDepartmentExistsQuery
import departments.UpdateDepartmentCommand
import departments.UpdateDepartmentParams
import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import scalereal.core.departments.DepartmentRepository
import scalereal.core.models.domain.Department
import scalereal.core.models.domain.DepartmentStatus
import scalereal.db.util.getWildCardedString
import javax.sql.DataSource

@Singleton
class DepartmentRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : DepartmentRepository {
    override fun create(
        organisationId: Long,
        id: Long,
        departmentName: String,
        departmentStatus: Boolean,
    ): Long =
        dataSource.connection.use { connection ->
            AddDepartmentQuery()
                .query(
                    connection,
                    AddDepartmentParams(
                        organisationId = organisationId,
                        id = id,
                        departmentName = departmentName,
                        departmentStatus = departmentStatus,
                    ),
                ).map { it.id }
                .first()
        }

    override fun isDepartmentExists(
        organisationId: Long,
        departmentName: String,
    ): DepartmentStatus =
        dataSource.connection.use { connection ->
            IsDepartmentExistsQuery()
                .query(
                    connection,
                    IsDepartmentExistsParams(
                        organisationId = organisationId,
                        departmentName = departmentName,
                    ),
                ).map {
                    DepartmentStatus(
                        exists = requireNotNull(it.exists ?: false),
                        status = requireNotNull(it.status ?: false),
                    )
                }.first()
        }

    override fun getMaxDepartmentId(organisationId: Long): Long {
        dataSource.connection.use { connection ->
            val maxDepartmentId =
                GetMaxDepartmentIdQuery()
                    .query(
                        connection,
                        GetMaxDepartmentIdParams(organisationId = organisationId),
                    ).map { it.maxId }
            return maxDepartmentId.firstOrNull() ?: 0
        }
    }

    override fun fetchAll(
        organisationId: Long,
        searchText: String,
        offset: Int,
        limit: Int,
    ): List<Department> =
        dataSource.connection.use { connection ->
            GetAllDepartmentsQuery()
                .query(
                    connection,
                    GetAllDepartmentsParams(
                        organisationId = organisationId,
                        searchText = getWildCardedString(searchText),
                        offset = offset,
                        limit = limit,
                    ),
                ).map {
                    Department(
                        organisationId = it.organisationId,
                        id = it.id,
                        departmentId = it.departmentId.toString(),
                        departmentName = it.departmentName,
                        departmentStatus = it.status,
                        departmentCreatedAt = it.createdAt,
                        departmentUpdatedAt = it.updatedAt,
                    )
                }
        }

    override fun departmentCount(
        organisationId: Long,
        searchText: String,
    ): Int =
        dataSource.connection.use { connection ->
            GetAllDepartmentsCountQuery()
                .query(
                    connection,
                    GetAllDepartmentsCountParams(
                        organisationId = organisationId,
                        searchText = getWildCardedString(searchText),
                    ),
                )[0]
                .departmentCount
                ?.toInt() ?: 0
        }

    override fun update(
        organisationId: Long,
        departmentId: Long,
        departmentName: String,
        departmentStatus: Boolean,
    ): Unit =
        dataSource.connection.use { connection ->
            UpdateDepartmentCommand()
                .command(
                    connection,
                    UpdateDepartmentParams(
                        organisationId = organisationId,
                        departmentName = departmentName,
                        departmentStatus = departmentStatus,
                        id = departmentId,
                    ),
                )
        }

    override fun getDepartmentDataById(
        departmentId: Long,
        organisationId: Long,
    ): Department =
        dataSource.connection.use { connection ->
            GetDepartmentDataByIdQuery()
                .query(
                    connection,
                    GetDepartmentDataByIdParams(id = departmentId, organisationId = organisationId),
                ).map {
                    Department(
                        organisationId = it.organisationId,
                        id = it.id,
                        departmentId = it.displayId.toString(),
                        departmentName = it.departmentName,
                        departmentStatus = it.status,
                        departmentCreatedAt = it.createdAt,
                        departmentUpdatedAt = it.updatedAt,
                    )
                }.first()
        }

    override fun getDepartmentId(
        organisationId: Long,
        departmentName: String,
    ): Long =
        dataSource.connection.use { connection ->
            GetDepartmentIdQuery()
                .query(
                    connection,
                    GetDepartmentIdParams(organisationId = organisationId, departmentName = departmentName),
                ).map { it.id }
                .first()
        }
}
