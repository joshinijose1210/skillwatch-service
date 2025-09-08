package scalereal.core.models.domain

data class SuperAdminResponse(
    val id: Long,
    val emailId: String,
    val firstName: String,
    val lastName: String,
    val modulePermission: List<ModulePermission>,
)
