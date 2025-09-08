package teams

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetTeamCountParams(
  val organisationId: Long?,
  val searchText: String?,
  val departmentId: Array<Int>?
)

class GetTeamCountParamSetter : ParamSetter<GetTeamCountParams> {
  override fun map(ps: PreparedStatement, params: GetTeamCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.searchText)
    ps.setObject(3, params.searchText)
    ps.setObject(4, params.searchText)
    ps.setArray(5, ps.connection.createArrayOf("int4", params.departmentId))
    ps.setArray(6, ps.connection.createArrayOf("int4", params.departmentId))
  }
}

data class GetTeamCountResult(
  val teamCount: Long?
)

class GetTeamCountRowMapper : RowMapper<GetTeamCountResult> {
  override fun map(rs: ResultSet): GetTeamCountResult = GetTeamCountResult(
  teamCount = rs.getObject("team_count") as kotlin.Long?)
}

class GetTeamCountQuery : Query<GetTeamCountParams, GetTeamCountResult> {
  override val sql: String = """
      |SELECT COUNT(teams.team_id) AS team_count
      |FROM
      |  teams
      |  LEFT JOIN department_team_mapping ON department_team_mapping.team_id = teams.id
      |  LEFT JOIN departments ON departments.id = department_team_mapping.department_id
      |WHERE teams.organisation_id = ?
      |    AND (cast(? as text) IS NULL
      |    OR UPPER(teams.team_name) LIKE UPPER(?)
      |    OR UPPER(departments.department_name) LIKE UPPER(?))
      |    AND (?::INT[] = '{-99}' OR department_team_mapping.department_id = ANY (?::INT[]));
      """.trimMargin()

  override val mapper: RowMapper<GetTeamCountResult> = GetTeamCountRowMapper()

  override val paramSetter: ParamSetter<GetTeamCountParams> = GetTeamCountParamSetter()
}
