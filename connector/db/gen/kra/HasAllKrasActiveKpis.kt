package kra

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class HasAllKrasActiveKpisParams(
  val organisationId: Long?
)

class HasAllKrasActiveKpisParamSetter : ParamSetter<HasAllKrasActiveKpisParams> {
  override fun map(ps: PreparedStatement, params: HasAllKrasActiveKpisParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class HasAllKrasActiveKpisResult(
  val allKrasHasActiveKpis: Boolean?
)

class HasAllKrasActiveKpisRowMapper : RowMapper<HasAllKrasActiveKpisResult> {
  override fun map(rs: ResultSet): HasAllKrasActiveKpisResult = HasAllKrasActiveKpisResult(
  allKrasHasActiveKpis = rs.getObject("all_kras_has_active_kpis") as kotlin.Boolean?)
}

class HasAllKrasActiveKpisQuery : Query<HasAllKrasActiveKpisParams, HasAllKrasActiveKpisResult> {
  override val sql: String = """
      |SELECT
      |    NOT EXISTS (
      |        SELECT 1
      |        FROM kra
      |        LEFT JOIN kra_kpi_mapping kkm ON kra.id = kkm.kra_id
      |        LEFT JOIN kpi ON kkm.kpi_id = kpi.id AND kpi.status = true
      |        WHERE kra.organisation_id = ?
      |        GROUP BY kra.id
      |        HAVING COUNT(kpi.id) = 0
      |    ) AS all_kras_has_active_kpis;
      """.trimMargin()

  override val mapper: RowMapper<HasAllKrasActiveKpisResult> = HasAllKrasActiveKpisRowMapper()

  override val paramSetter: ParamSetter<HasAllKrasActiveKpisParams> =
      HasAllKrasActiveKpisParamSetter()
}
