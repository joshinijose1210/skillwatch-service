package teams

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsTeamExistsParams(
  val departmentId: Long?,
  val organisationId: Long?,
  val teamName: String?
)

class IsTeamExistsParamSetter : ParamSetter<IsTeamExistsParams> {
  override fun map(ps: PreparedStatement, params: IsTeamExistsParams) {
    ps.setObject(1, params.departmentId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.teamName)
  }
}

data class IsTeamExistsResult(
  val exists: Boolean?,
  val status: Boolean?
)

class IsTeamExistsRowMapper : RowMapper<IsTeamExistsResult> {
  override fun map(rs: ResultSet): IsTeamExistsResult = IsTeamExistsResult(
  exists = rs.getObject("exists") as kotlin.Boolean?,
    status = rs.getObject("status") as kotlin.Boolean?)
}

class IsTeamExistsQuery : Query<IsTeamExistsParams, IsTeamExistsResult> {
  override val sql: String = """
      |WITH subquery AS (
      |    SELECT status
      |    FROM teams
      |    JOIN department_team_mapping ON teams.id = department_team_mapping.team_id
      |    WHERE
      |    department_team_mapping.department_id = ?
      |    AND organisation_id = ?
      |    AND LOWER(team_name) = LOWER(?)
      |)
      |SELECT EXISTS (SELECT 1 FROM subquery), (SELECT status FROM subquery);
      """.trimMargin()

  override val mapper: RowMapper<IsTeamExistsResult> = IsTeamExistsRowMapper()

  override val paramSetter: ParamSetter<IsTeamExistsParams> = IsTeamExistsParamSetter()
}
