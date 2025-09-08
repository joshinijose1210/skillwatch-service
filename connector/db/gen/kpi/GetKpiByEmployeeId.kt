package kpi

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetKpiByEmployeeIdParams(
  val reviewToId: Long?,
  val organisationId: Long?
)

class GetKpiByEmployeeIdParamSetter : ParamSetter<GetKpiByEmployeeIdParams> {
  override fun map(ps: PreparedStatement, params: GetKpiByEmployeeIdParams) {
    ps.setObject(1, params.reviewToId)
    ps.setObject(2, params.reviewToId)
    ps.setObject(3, params.organisationId)
  }
}

data class GetKpiByEmployeeIdResult(
  val organisationId: Long,
  val id: Long,
  val kpiId: Long,
  val title: String,
  val description: String,
  val versionNumber: Long,
  val kraId: Long?,
  val kraName: String?
)

class GetKpiByEmployeeIdRowMapper : RowMapper<GetKpiByEmployeeIdResult> {
  override fun map(rs: ResultSet): GetKpiByEmployeeIdResult = GetKpiByEmployeeIdResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    kpiId = rs.getObject("kpi_id") as kotlin.Long,
    title = rs.getObject("title") as kotlin.String,
    description = rs.getObject("description") as kotlin.String,
    versionNumber = rs.getObject("version_number") as kotlin.Long,
    kraId = rs.getObject("kra_id") as kotlin.Long?,
    kraName = rs.getObject("kra_name") as kotlin.String?)
}

class GetKpiByEmployeeIdQuery : Query<GetKpiByEmployeeIdParams, GetKpiByEmployeeIdResult> {
  override val sql: String = """
      |SELECT
      |  kpi.organisation_id,
      |  kpi.id,
      |  kpi.kpi_id,
      |  kpi.title,
      |  kpi.description,
      |  kpi_version_mapping.version_number,
      |  COALESCE(kra.id, null) AS kra_id,
      |  COALESCE(kra.name, null) AS kra_name
      |FROM
      |  kpi
      |  JOIN kpi_version_mapping ON kpi.id = kpi_version_mapping.kpi_id
      |  JOIN kpi_department_team_designation_mapping ON kpi.id = kpi_department_team_designation_mapping.kpi_id
      |  JOIN employees_team_mapping_view ON employees_team_mapping_view.team_id = kpi_department_team_designation_mapping.team_id
      |  JOIN employees_designation_mapping ON employees_designation_mapping.designation_id = kpi_department_team_designation_mapping.designation_id
      |  LEFT JOIN kra_kpi_mapping ON kpi.id = kra_kpi_mapping.kpi_id
      |  LEFT JOIN kra ON kra.id = kra_kpi_mapping.kra_id
      |WHERE
      |  employees_team_mapping_view.emp_id = ?
      |  AND employees_designation_mapping.emp_id = ?
      |  AND kpi.organisation_id = ?
      |  AND kpi.status = true;
      """.trimMargin()

  override val mapper: RowMapper<GetKpiByEmployeeIdResult> = GetKpiByEmployeeIdRowMapper()

  override val paramSetter: ParamSetter<GetKpiByEmployeeIdParams> = GetKpiByEmployeeIdParamSetter()
}
