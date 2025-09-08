package kpi

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetMaxKpiIdParams(
  val organisationId: Long?
)

class GetMaxKpiIdParamSetter : ParamSetter<GetMaxKpiIdParams> {
  override fun map(ps: PreparedStatement, params: GetMaxKpiIdParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetMaxKpiIdResult(
  val maxId: Long?
)

class GetMaxKpiIdRowMapper : RowMapper<GetMaxKpiIdResult> {
  override fun map(rs: ResultSet): GetMaxKpiIdResult = GetMaxKpiIdResult(
  maxId = rs.getObject("max_id") as kotlin.Long?)
}

class GetMaxKpiIdQuery : Query<GetMaxKpiIdParams, GetMaxKpiIdResult> {
  override val sql: String = """
      |SELECT MAX(kpi_id) as max_id FROM kpi WHERE organisation_id = ?;
      |""".trimMargin()

  override val mapper: RowMapper<GetMaxKpiIdResult> = GetMaxKpiIdRowMapper()

  override val paramSetter: ParamSetter<GetMaxKpiIdParams> = GetMaxKpiIdParamSetter()
}
