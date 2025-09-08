package teams

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetTeamDataByIdParams(
  val id: Long?,
  val organisationId: Long?
)

class GetTeamDataByIdParamSetter : ParamSetter<GetTeamDataByIdParams> {
  override fun map(ps: PreparedStatement, params: GetTeamDataByIdParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.organisationId)
  }
}

data class GetTeamDataByIdResult(
  val organisationId: Long,
  val id: Long,
  val displayId: Long,
  val teamName: String,
  val status: Boolean,
  val departmentId: Long?,
  val departmentName: String?,
  val departmentDisplayId: Long?,
  val departmentStatus: Boolean?,
  val createdAt: Timestamp,
  val updatedAt: Timestamp?
)

class GetTeamDataByIdRowMapper : RowMapper<GetTeamDataByIdResult> {
  override fun map(rs: ResultSet): GetTeamDataByIdResult = GetTeamDataByIdResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    displayId = rs.getObject("display_id") as kotlin.Long,
    teamName = rs.getObject("team_name") as kotlin.String,
    status = rs.getObject("status") as kotlin.Boolean,
    departmentId = rs.getObject("department_id") as kotlin.Long?,
    departmentName = rs.getObject("department_name") as kotlin.String?,
    departmentDisplayId = rs.getObject("department_display_id") as kotlin.Long?,
    departmentStatus = rs.getObject("department_status") as kotlin.Boolean?,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp?)
}

class GetTeamDataByIdQuery : Query<GetTeamDataByIdParams, GetTeamDataByIdResult> {
  override val sql: String = """
      |SELECT
      |  teams.organisation_id,
      |  teams.id,
      |  teams.team_id as display_id,
      |  teams.team_name,
      |  teams.status,
      |  COALESCE(department_team_mapping.department_id, null) AS department_id,
      |  COALESCE(departments.department_name, null) AS department_name,
      |  COALESCE(departments.department_id, null) AS department_display_id,
      |  COALESCE(departments.status, null) AS department_status,
      |  teams.created_at,
      |  teams.updated_at
      |FROM
      |  teams
      |  LEFT JOIN department_team_mapping ON department_team_mapping.team_id = teams.id
      |  LEFT JOIN departments ON departments.id = department_team_mapping.department_id
      |WHERE
      |  teams.id = ?
      |  AND teams.organisation_id = ? ;
      |""".trimMargin()

  override val mapper: RowMapper<GetTeamDataByIdResult> = GetTeamDataByIdRowMapper()

  override val paramSetter: ParamSetter<GetTeamDataByIdParams> = GetTeamDataByIdParamSetter()
}
