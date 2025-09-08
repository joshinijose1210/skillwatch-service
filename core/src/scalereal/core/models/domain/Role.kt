package scalereal.core.models.domain

import java.sql.Timestamp

data class Role(
    val organisationId: Long,
    val id: Long,
    val roleId: String,
    val roleName: String,
    val modulePermission: List<ModulePermission>,
    val status: Boolean,
    val createdAt: Timestamp,
    val updatedAt: Timestamp?,
)

data class RoleResponse(
    val totalRoles: Int,
    val roles: List<Role>,
)

data class Permission(
    val view: Boolean,
    val edit: Boolean,
)

data class RoleStatus(
    val exists: Boolean,
    val status: Boolean,
)

data class RoleData(
    val id: Long,
    val roleId: Long,
    val roleName: String,
    val status: Boolean,
)
