package kpi

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetKpiDataByIdParams(
  val id: Long?
)

class GetKpiDataByIdParamSetter : ParamSetter<GetKpiDataByIdParams> {
  override fun map(ps: PreparedStatement, params: GetKpiDataByIdParams) {
    ps.setObject(1, params.id)
  }
}

data class GetKpiDataByIdResult(
  val id: Long,
  val title: String,
  val description: String,
  val status: Boolean,
  val displayId: Long,
  val organisationId: Long,
  val versionNumber: Long,
  val kraId: Long?
)

class GetKpiDataByIdRowMapper : RowMapper<GetKpiDataByIdResult> {
  override fun map(rs: ResultSet): GetKpiDataByIdResult = GetKpiDataByIdResult(
  id = rs.getObject("id") as kotlin.Long,
    title = rs.getObject("title") as kotlin.String,
    description = rs.getObject("description") as kotlin.String,
    status = rs.getObject("status") as kotlin.Boolean,
    displayId = rs.getObject("display_id") as kotlin.Long,
    organisationId = rs.getObject("organisation_id") as kotlin.Long,
    versionNumber = rs.getObject("version_number") as kotlin.Long,
    kraId = rs.getObject("kra_id") as kotlin.Long?)
}

class GetKpiDataByIdQuery : Query<GetKpiDataByIdParams, GetKpiDataByIdResult> {
  override val sql: String = """
      |SELECT
      |  kpi.id,
      |  kpi.title,
      |  kpi.description,
      |  kpi.status,
      |  kpi.kpi_id as display_id,
      |  kpi.organisation_id,
      |  kpi_version_mapping.version_number,
      |  COALESCE(kra.id, null) AS kra_id
      |FROM
      |  kpi
      |  JOIN kpi_version_mapping ON kpi.id = kpi_version_mapping.kpi_id
      |  LEFT JOIN kra_kpi_mapping ON kpi.id = kra_kpi_mapping.kpi_id
      |  LEFT JOIN kra ON kra.id = kra_kpi_mapping.kra_id
      |WHERE
      |  kpi.id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetKpiDataByIdResult> = GetKpiDataByIdRowMapper()

  override val paramSetter: ParamSetter<GetKpiDataByIdParams> = GetKpiDataByIdParamSetter()
}
