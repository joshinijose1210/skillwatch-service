package kpi

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetKpisParams(
  val organisationId: Long?,
  val search: String?,
  val departmentId: Array<Int>?,
  val teamId: Array<Int>?,
  val designationId: Array<Int>?,
  val kraId: Array<Int>?,
  val status: Array<String>?,
  val offset: Int?,
  val limit: Int?
)

class GetKpisParamSetter : ParamSetter<GetKpisParams> {
  override fun map(ps: PreparedStatement, params: GetKpisParams) {
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
    ps.setObject(16, params.offset)
    ps.setObject(17, params.limit)
  }
}

data class GetKpisResult(
  val organisationId: Long,
  val id: Long,
  val kpiId: Long,
  val title: String,
  val description: String,
  val status: Boolean,
  val versionNumber: Long,
  val kraId: Long?,
  val kraName: String?
)

class GetKpisRowMapper : RowMapper<GetKpisResult> {
  override fun map(rs: ResultSet): GetKpisResult = GetKpisResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    kpiId = rs.getObject("kpi_id") as kotlin.Long,
    title = rs.getObject("title") as kotlin.String,
    description = rs.getObject("description") as kotlin.String,
    status = rs.getObject("status") as kotlin.Boolean,
    versionNumber = rs.getObject("version_number") as kotlin.Long,
    kraId = rs.getObject("kra_id") as kotlin.Long?,
    kraName = rs.getObject("kra_name") as kotlin.String?)
}

class GetKpisQuery : Query<GetKpisParams, GetKpisResult> {
  override val sql: String = """
      |SELECT
      |  kpi_data.organisation_id,
      |  kpi_data.id,
      |  kpi_data.kpi_id,
      |  kpi.title,
      |  kpi.description,
      |  kpi.status,
      |  kpi_data.version_number,
      |  COALESCE(kra.id, null) AS kra_id,
      |  COALESCE(kra.name, null) AS kra_name
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
      |  AND (?::BOOL[] = '{true,false}' OR kpi_data.status = ANY (?::BOOL[]))
      |GROUP BY
      |  kpi_data.organisation_id,
      |  kpi_data.id,
      |  kpi_data.kpi_id,
      |  kpi.title,
      |  kpi.description,
      |  kpi.status,
      |  kpi_data.version_number,
      |  kra.id,
      |  kra.name
      |ORDER BY
      |  kpi_data.kpi_id DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      |""".trimMargin()

  override val mapper: RowMapper<GetKpisResult> = GetKpisRowMapper()

  override val paramSetter: ParamSetter<GetKpisParams> = GetKpisParamSetter()
}
