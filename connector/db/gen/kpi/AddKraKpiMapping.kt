package kpi

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddKraKpiMappingParams(
  val kraId: Long?,
  val kpiId: Long?
)

class AddKraKpiMappingParamSetter : ParamSetter<AddKraKpiMappingParams> {
  override fun map(ps: PreparedStatement, params: AddKraKpiMappingParams) {
    ps.setObject(1, params.kraId)
    ps.setObject(2, params.kpiId)
  }
}

class AddKraKpiMappingCommand : Command<AddKraKpiMappingParams> {
  override val sql: String = """
      |INSERT INTO kra_kpi_mapping(kra_id, kpi_id)
      |VALUES (?, ?);
      |""".trimMargin()

  override val paramSetter: ParamSetter<AddKraKpiMappingParams> = AddKraKpiMappingParamSetter()
}
