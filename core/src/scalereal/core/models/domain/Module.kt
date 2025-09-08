package scalereal.core.models.domain

data class Module(
    val moduleId: Int,
    var moduleName: String,
    var mainModule: String?,
)

data class ModulePermission(
    val moduleId: Int?,
    val moduleName: String,
    val view: Boolean?,
    val edit: Boolean?,
)
