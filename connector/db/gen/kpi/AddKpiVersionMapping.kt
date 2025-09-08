package kpi

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddKpiVersionMappingParams(
  val kpiId: Long?,
  val versionNumber: Long?
)

class AddKpiVersionMappingParamSetter : ParamSetter<AddKpiVersionMappingParams> {
  override fun map(ps: PreparedStatement, params: AddKpiVersionMappingParams) {
    ps.setObject(1, params.kpiId)
    ps.setObject(2, params.versionNumber)
  }
}

class AddKpiVersionMappingCommand : Command<AddKpiVersionMappingParams> {
  override val sql: String = """
      |INSERT INTO kpi_version_mapping(kpi_id, version_number)
      |VALUES (?, ?);
      """.trimMargin()

  override val paramSetter: ParamSetter<AddKpiVersionMappingParams> =
      AddKpiVersionMappingParamSetter()
}
