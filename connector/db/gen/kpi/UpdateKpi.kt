package kpi

import java.sql.PreparedStatement
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateKpiParams(
  val status: Boolean?,
  val organisationId: Long?,
  val id: Long?
)

class UpdateKpiParamSetter : ParamSetter<UpdateKpiParams> {
  override fun map(ps: PreparedStatement, params: UpdateKpiParams) {
    ps.setObject(1, params.status)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.id)
  }
}

class UpdateKpiCommand : Command<UpdateKpiParams> {
  override val sql: String = """
      |UPDATE
      |  kpi
      |SET
      |  status = ?
      |WHERE
      |  kpi.organisation_id = ?
      |  AND id = ?;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateKpiParams> = UpdateKpiParamSetter()
}
