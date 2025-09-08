package teams

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetTeamIdParams(
  val departmentId: Long?,
  val organisationId: Long?,
  val teamName: String?
)

class GetTeamIdParamSetter : ParamSetter<GetTeamIdParams> {
  override fun map(ps: PreparedStatement, params: GetTeamIdParams) {
    ps.setObject(1, params.departmentId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.teamName)
  }
}

data class GetTeamIdResult(
  val id: Long
)

class GetTeamIdRowMapper : RowMapper<GetTeamIdResult> {
  override fun map(rs: ResultSet): GetTeamIdResult = GetTeamIdResult(
  id = rs.getObject("id") as kotlin.Long)
}

class GetTeamIdQuery : Query<GetTeamIdParams, GetTeamIdResult> {
  override val sql: String = """
      |SELECT
      |  id
      |FROM
      |  teams
      |  JOIN department_team_mapping ON teams.id = department_team_mapping.team_id
      |WHERE
      |  department_team_mapping.department_id = ?
      |  AND organisation_id = ?
      |  AND LOWER(team_name) = LOWER(?)
      |  AND teams.status = true;
      """.trimMargin()

  override val mapper: RowMapper<GetTeamIdResult> = GetTeamIdRowMapper()

  override val paramSetter: ParamSetter<GetTeamIdParams> = GetTeamIdParamSetter()
}
