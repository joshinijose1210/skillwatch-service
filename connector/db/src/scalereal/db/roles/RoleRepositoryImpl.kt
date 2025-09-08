package scalereal.db.roles

import jakarta.inject.Inject
import jakarta.inject.Singleton
import norm.command
import norm.query
import roles.AddNewRoleParams
import roles.AddNewRoleQuery
import roles.AddPermissionCommand
import roles.AddPermissionParams
import roles.DeletePermissionCommand
import roles.DeletePermissionParams
import roles.GetAllRolesParams
import roles.GetAllRolesQuery
import roles.GetAllRolesResult
import roles.GetMaxRoleIdParams
import roles.GetMaxRoleIdQuery
import roles.GetPermissionParams
import roles.GetPermissionQuery
import roles.GetRoleDataByIdParams
import roles.GetRoleDataByIdQuery
import roles.GetRoleDataByIdResult
import roles.GetRoleIdParams
import roles.GetRoleIdQuery
import roles.GetRolesCountParams
import roles.GetRolesCountQuery
import roles.HasPermissionsParams
import roles.HasPermissionsQuery
import roles.IsRoleExistsParams
import roles.IsRoleExistsQuery
import roles.UpdatePermissionCommand
import roles.UpdatePermissionParams
import roles.UpdateRoleCommand
import roles.UpdateRoleParams
import scalereal.core.models.domain.ModulePermission
import scalereal.core.models.domain.Permission
import scalereal.core.models.domain.Role
import scalereal.core.models.domain.RoleData
import scalereal.core.models.domain.RoleStatus
import scalereal.core.roles.RoleRepository
import scalereal.db.util.getWildCardedString
import javax.sql.DataSource

@Singleton
class RoleRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : RoleRepository {
    override fun isRoleExists(
        organisationId: Long,
        roleName: String,
    ): RoleStatus =
        dataSource.connection.use { connection ->
            IsRoleExistsQuery()
                .query(connection, IsRoleExistsParams(organisationId = organisationId, roleName = roleName))
                .map { RoleStatus(exists = requireNotNull(it.exists ?: false), status = it.status ?: false) }
                .first()
        }

    override fun create(
        organisationId: Long,
        roleId: Long,
        roleName: String,
        modulePermission: List<ModulePermission>,
        status: Boolean,
    ): Unit =
        dataSource.connection.use { connection ->
            AddNewRoleQuery()
                .query(connection, AddNewRoleParams(organisationId = organisationId, roleId = roleId, roleName = roleName, status = status))
                .map {
                    createPermissions(it.id, modulePermission)
                }
        }

    override fun createPermissions(
        roleId: Long,
        modulePermission: List<ModulePermission>,
    ) {
        modulePermission.map { permission ->
            dataSource.connection.use { connection ->
                AddPermissionCommand()
                    .command(
                        connection,
                        AddPermissionParams(
                            roleId = roleId,
                            moduleId = permission.moduleId,
                            view = permission.view,
                            edit = permission.edit,
                        ),
                    )
            }
        }
    }

    override fun fetchAll(
        organisationId: Long,
        searchText: String,
        offset: Int,
        limit: Int,
    ): List<Role> =
        dataSource.connection.use { connection ->
            GetAllRolesQuery()
                .query(
                    connection,
                    GetAllRolesParams(
                        organisationId = organisationId,
                        searchText = getWildCardedString(searchText),
                        offset = offset,
                        limit = limit,
                    ),
                ).map { it.toRole() }
        }

    override fun fetchPermissions(roleId: Long): List<ModulePermission> =
        dataSource.connection
            .use { connection ->
                GetPermissionQuery()
                    .query(connection, GetPermissionParams(roleId))
            }.map {
                ModulePermission(
                    moduleId = it.moduleId,
                    moduleName = it.moduleName,
                    view = it.view,
                    edit = it.edit,
                )
            }

    override fun count(
        organisationId: Long,
        searchText: String,
    ): Int =
        dataSource.connection.use { connection ->
            GetRolesCountQuery()
                .query(
                    connection,
                    GetRolesCountParams(organisationId = organisationId, searchText = getWildCardedString(searchText)),
                )[0]
                .roleCount
                ?.toInt() ?: 0
        }

    override fun update(
        organisationId: Long,
        id: Long,
        roleName: String,
        status: Boolean,
    ): Unit =
        dataSource.connection.use { connection ->
            UpdateRoleCommand()
                .command(
                    connection,
                    UpdateRoleParams(organisationId = organisationId, id = id, roleName = roleName, status = status),
                )
        }

    override fun updatePermissions(
        roleId: Long,
        modulePermission: List<ModulePermission>,
    ) {
        modulePermission.map { permission ->
            dataSource.connection.use { connection ->
                UpdatePermissionCommand()
                    .command(
                        connection,
                        UpdatePermissionParams(permission.view, permission.edit, roleId, permission.moduleId),
                    )
            }
        }
    }

    override fun deletePermission(roleId: Long) {
        dataSource.connection.use { connection ->
            DeletePermissionCommand()
                .command(connection, DeletePermissionParams(roleId))
        }
    }

    override fun getRoleId(
        organisationId: Long,
        roleName: String,
    ): Long =
        dataSource.connection.use { connection ->
            GetRoleIdQuery()
                .query(connection, GetRoleIdParams(organisationId = organisationId, roleName = roleName))
                .map { it.id }
                .first()
        }

    override fun hasPermission(
        organisationId: Long,
        roleName: MutableCollection<String>,
        moduleName: String,
    ): Permission =
        dataSource.connection.use { connection ->
            HasPermissionsQuery()
                .query(connection, HasPermissionsParams(organisationId, moduleName, roleName.first()))
                .map {
                    Permission(
                        it.view ?: false,
                        it.edit ?: false,
                    )
                }.first()
        }

    override fun getMaxRoleId(organisationId: Long): Long {
        dataSource.connection.use { connection ->
            val maxRoleId =
                GetMaxRoleIdQuery()
                    .query(
                        connection,
                        GetMaxRoleIdParams(organisationId = organisationId),
                    ).map { it.maxId }
            return maxRoleId.firstOrNull() ?: 0
        }
    }

    override fun getRoleDataById(
        id: Long,
        organisationId: Long,
    ): RoleData =
        dataSource.connection.use { connection ->
            GetRoleDataByIdQuery()
                .query(connection, GetRoleDataByIdParams(id = id, organisationId = organisationId))
                .map { it.toRoleData() }
        }[0]

    private fun GetAllRolesResult.toRole() =
        Role(
            organisationId = organisationId,
            id = id,
            roleId = roleId.toString(),
            roleName = roleName,
            modulePermission = listOf(),
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    private fun GetRoleDataByIdResult.toRoleData() =
        RoleData(
            id = id,
            roleId = roleId,
            roleName = roleName,
            status = status,
        )
}
