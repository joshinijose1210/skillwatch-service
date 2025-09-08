package kpi

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetKpisCountParams(
  val organisationId: Long?,
  val search: String?,
  val departmentId: Array<Int>?,
  val teamId: Array<Int>?,
  val designationId: Array<Int>?,
  val kraId: Array<Int>?,
  val status: Array<String>?
)

class GetKpisCountParamSetter : ParamSetter<GetKpisCountParams> {
  override fun map(ps: PreparedStatement, params: GetKpisCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.search)
    ps.setObject(4, params.search)
    ps.setObject(5, params.search)
    ps.setArray(6, ps.connection.createArrayOf("int4", params.departmentId))
    ps.setArray(7, ps.connection.createArrayOf("int4", params.departmentId))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.teamId))
    ps.setArray(9, ps.connection.createArrayOf("int4", params.teamId))
    ps.setArray(10, ps.connection.createArrayOf("int4", params.designationId))
    ps.setArray(11, ps.connection.createArrayOf("int4", params.designationId))
    ps.setArray(12, ps.connection.createArrayOf("int4", params.kraId))
    ps.setArray(13, ps.connection.createArrayOf("int4", params.kraId))
    ps.setArray(14, ps.connection.createArrayOf("bool", params.status))
    ps.setArray(15, ps.connection.createArrayOf("bool", params.status))
  }
}

data class GetKpisCountResult(
  val kpiCount: Long?
)

class GetKpisCountRowMapper : RowMapper<GetKpisCountResult> {
  override fun map(rs: ResultSet): GetKpisCountResult = GetKpisCountResult(
  kpiCount = rs.getObject("kpi_count") as kotlin.Long?)
}

class GetKpisCountQuery : Query<GetKpisCountParams, GetKpisCountResult> {
  override val sql: String = """
      |SELECT COUNT(DISTINCT kpi.id) as kpi_count
      |FROM
      |  kpi
      |  JOIN (SELECT max(kpi.id) as id, kpi.kpi_id, max(kpi_version_mapping.version_number) as version_number, kpi.organisation_id, bool_or(kpi.status) as status
      |        FROM kpi
      |        JOIN kpi_version_mapping ON kpi.id = kpi_version_mapping.kpi_id
      |        WHERE kpi.organisation_id = ?
      |        GROUP BY kpi.kpi_id, kpi.organisation_id) as kpi_data ON kpi_data.id = kpi.id
      |  LEFT JOIN kpi_department_team_designation_mapping ON kpi.id = kpi_department_team_designation_mapping.kpi_id
      |  LEFT JOIN kra_kpi_mapping ON kpi.id = kra_kpi_mapping.kpi_id
      |  LEFT JOIN kra ON kra.id = kra_kpi_mapping.kra_id
      |WHERE
      |  kpi.organisation_id = ?
      |  AND (
      |    cast(? as text) IS NULL
      |    OR UPPER(kpi.title) LIKE '%' || UPPER(?) || '%'
      |    OR UPPER(kpi.description) LIKE '%' || UPPER(?) || '%'
      |  )
      |  AND (?::INT[] = '{-99}' OR kpi_department_team_designation_mapping.department_id = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}' OR kpi_department_team_designation_mapping.team_id = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}' OR kpi_department_team_designation_mapping.designation_id = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}' OR kra_kpi_mapping.kra_id = ANY (?::INT[]))
      |  AND (?::BOOL[] = '{true,false}' OR kpi_data.status = ANY (?::BOOL[]));
      |""".trimMargin()

  override val mapper: RowMapper<GetKpisCountResult> = GetKpisCountRowMapper()

  override val paramSetter: ParamSetter<GetKpisCountParams> = GetKpisCountParamSetter()
}
