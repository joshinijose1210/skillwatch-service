package teams

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetUnlinkedTeamsCountParams(
  val organisationId: Long?
)

class GetUnlinkedTeamsCountParamSetter : ParamSetter<GetUnlinkedTeamsCountParams> {
  override fun map(ps: PreparedStatement, params: GetUnlinkedTeamsCountParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetUnlinkedTeamsCountResult(
  val unlinkedTeamCount: Long?
)

class GetUnlinkedTeamsCountRowMapper : RowMapper<GetUnlinkedTeamsCountResult> {
  override fun map(rs: ResultSet): GetUnlinkedTeamsCountResult = GetUnlinkedTeamsCountResult(
  unlinkedTeamCount = rs.getObject("unlinked_team_count") as kotlin.Long?)
}

class GetUnlinkedTeamsCountQuery : Query<GetUnlinkedTeamsCountParams, GetUnlinkedTeamsCountResult> {
  override val sql: String = """
      |SELECT COUNT(teams.team_id) AS unlinked_team_count
      |FROM teams
      |  LEFT JOIN department_team_mapping ON department_team_mapping.team_id = teams.id
      |  LEFT JOIN departments ON departments.id = department_team_mapping.department_id
      |WHERE teams.organisation_id = ?
      |  AND department_team_mapping.team_id IS NULL
      |  AND teams.team_id != 1 ;
      """.trimMargin()

  override val mapper: RowMapper<GetUnlinkedTeamsCountResult> = GetUnlinkedTeamsCountRowMapper()

  override val paramSetter: ParamSetter<GetUnlinkedTeamsCountParams> =
      GetUnlinkedTeamsCountParamSetter()
}
