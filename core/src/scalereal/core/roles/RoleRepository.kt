package scalereal.core.roles

import scalereal.core.models.domain.ModulePermission
import scalereal.core.models.domain.Permission
import scalereal.core.models.domain.Role
import scalereal.core.models.domain.RoleData
import scalereal.core.models.domain.RoleStatus

interface RoleRepository {
    fun isRoleExists(
        organisationId: Long,
        roleName: String,
    ): RoleStatus

    fun create(
        organisationId: Long,
        roleId: Long,
        roleName: String,
        modulePermission: List<ModulePermission>,
        status: Boolean,
    )

    fun createPermissions(
        roleId: Long,
        modulePermission: List<ModulePermission>,
    )

    fun fetchAll(
        organisationId: Long,
        searchText: String,
        offset: Int,
        limit: Int,
    ): List<Role>

    fun fetchPermissions(roleId: Long): List<ModulePermission>

    fun count(
        organisationId: Long,
        searchText: String,
    ): Int

    fun update(
        organisationId: Long,
        id: Long,
        roleName: String,
        status: Boolean,
    )

    fun updatePermissions(
        roleId: Long,
        modulePermission: List<ModulePermission>,
    )

    fun deletePermission(roleId: Long)

    fun getRoleId(
        organisationId: Long,
        roleName: String,
    ): Long

    fun hasPermission(
        organisationId: Long,
        roleName: MutableCollection<String>,
        moduleName: String,
    ): Permission

    fun getMaxRoleId(organisationId: Long): Long

    fun getRoleDataById(
        id: Long,
        organisationId: Long,
    ): RoleData
}
