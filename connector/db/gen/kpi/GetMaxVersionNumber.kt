package kpi

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetMaxVersionNumberParams(
  val kpiId: Long?
)

class GetMaxVersionNumberParamSetter : ParamSetter<GetMaxVersionNumberParams> {
  override fun map(ps: PreparedStatement, params: GetMaxVersionNumberParams) {
    ps.setObject(1, params.kpiId)
  }
}

data class GetMaxVersionNumberResult(
  val versionNumber: Long?
)

class GetMaxVersionNumberRowMapper : RowMapper<GetMaxVersionNumberResult> {
  override fun map(rs: ResultSet): GetMaxVersionNumberResult = GetMaxVersionNumberResult(
  versionNumber = rs.getObject("version_number") as kotlin.Long?)
}

class GetMaxVersionNumberQuery : Query<GetMaxVersionNumberParams, GetMaxVersionNumberResult> {
  override val sql: String = """
      |SELECT
      |  MAX(version_number) AS version_number
      |FROM
      |  kpi
      |  JOIN kpi_version_mapping ON kpi.id = kpi_version_mapping.kpi_id
      |WHERE
      |  kpi.kpi_id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetMaxVersionNumberResult> = GetMaxVersionNumberRowMapper()

  override val paramSetter: ParamSetter<GetMaxVersionNumberParams> =
      GetMaxVersionNumberParamSetter()
}
