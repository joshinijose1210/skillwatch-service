package teams

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllTeamsParams(
  val organisationId: Long?,
  val searchText: String?,
  val departmentId: Array<Int>?,
  val offset: Int?,
  val limit: Int?
)

class GetAllTeamsParamSetter : ParamSetter<GetAllTeamsParams> {
  override fun map(ps: PreparedStatement, params: GetAllTeamsParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.searchText)
    ps.setObject(3, params.searchText)
    ps.setObject(4, params.searchText)
    ps.setArray(5, ps.connection.createArrayOf("int4", params.departmentId))
    ps.setArray(6, ps.connection.createArrayOf("int4", params.departmentId))
    ps.setObject(7, params.offset)
    ps.setObject(8, params.limit)
  }
}

data class GetAllTeamsResult(
  val id: Long,
  val organisationId: Long,
  val teamId: Long,
  val departmentId: Long?,
  val departmentName: String?,
  val departmentDisplayId: Long?,
  val departmentStatus: Boolean?,
  val teamName: String,
  val createdAt: Timestamp,
  val updatedAt: Timestamp?,
  val status: Boolean
)

class GetAllTeamsRowMapper : RowMapper<GetAllTeamsResult> {
  override fun map(rs: ResultSet): GetAllTeamsResult = GetAllTeamsResult(
  id = rs.getObject("id") as kotlin.Long,
    organisationId = rs.getObject("organisation_id") as kotlin.Long,
    teamId = rs.getObject("team_id") as kotlin.Long,
    departmentId = rs.getObject("department_id") as kotlin.Long?,
    departmentName = rs.getObject("department_name") as kotlin.String?,
    departmentDisplayId = rs.getObject("department_display_id") as kotlin.Long?,
    departmentStatus = rs.getObject("department_status") as kotlin.Boolean?,
    teamName = rs.getObject("team_name") as kotlin.String,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp?,
    status = rs.getObject("status") as kotlin.Boolean)
}

class GetAllTeamsQuery : Query<GetAllTeamsParams, GetAllTeamsResult> {
  override val sql: String = """
      |SELECT
      |  teams.id,
      |  teams.organisation_id,
      |  teams.team_id,
      |  COALESCE(department_team_mapping.department_id, null) AS department_id,
      |  COALESCE(departments.department_name, null) AS department_name,
      |  COALESCE(departments.department_id, null) AS department_display_id,
      |  COALESCE(departments.status, null) AS department_status,
      |  teams.team_name,
      |  teams.created_at,
      |  teams.updated_at,
      |  teams.status
      |FROM
      |  teams
      |  LEFT JOIN department_team_mapping ON department_team_mapping.team_id = teams.id
      |  LEFT JOIN departments ON departments.id = department_team_mapping.department_id
      |WHERE teams.organisation_id = ?
      |    AND (cast(? as text) IS NULL
      |    OR UPPER(teams.team_name) LIKE UPPER(?)
      |    OR UPPER(departments.department_name) LIKE UPPER(?))
      |    AND (?::INT[] = '{-99}' OR department_team_mapping.department_id = ANY (?::INT[]))
      |ORDER BY
      |  teams.created_at DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      |
      |
      |""".trimMargin()

  override val mapper: RowMapper<GetAllTeamsResult> = GetAllTeamsRowMapper()

  override val paramSetter: ParamSetter<GetAllTeamsParams> = GetAllTeamsParamSetter()
}
