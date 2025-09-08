package scalereal.db.modules

import jakarta.inject.Inject
import jakarta.inject.Singleton
import modules.GetModuleIdParams
import modules.GetModuleIdQuery
import modules.GetModulesParams
import modules.GetModulesQuery
import norm.query
import scalereal.core.models.domain.Module
import scalereal.core.modules.ModuleRepository
import javax.sql.DataSource

@Singleton
class ModuleRepositoryImpl(
    @Inject private val dataSource: DataSource,
) : ModuleRepository {
    override fun fetch(): List<Module> =
        dataSource.connection.use { connection ->
            GetModulesQuery()
                .query(
                    connection,
                    GetModulesParams(),
                ).map {
                    Module(
                        moduleId = it.id,
                        moduleName = it.name,
                        mainModule = null,
                    )
                }
        }

    override fun fetchModuleId(moduleName: String): Int =
        dataSource.connection.use { connection ->
            GetModuleIdQuery()
                .query(
                    connection,
                    GetModuleIdParams(moduleName = moduleName),
                )[0]
                .id
        }
}
