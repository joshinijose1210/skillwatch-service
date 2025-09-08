package scalereal.core.modules

import scalereal.core.models.domain.Module

interface ModuleRepository {
    fun fetch(): List<Module>

    fun fetchModuleId(moduleName: String): Int
}
