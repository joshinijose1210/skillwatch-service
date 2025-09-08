package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetEmployeeTeamDuringReviewCycleParams(
  val employeeId: Long?,
  val organisationId: Long?,
  val reviewCycleId: Long?
)

class GetEmployeeTeamDuringReviewCycleParamSetter :
    ParamSetter<GetEmployeeTeamDuringReviewCycleParams> {
  override fun map(ps: PreparedStatement, params: GetEmployeeTeamDuringReviewCycleParams) {
    ps.setObject(1, params.employeeId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.reviewCycleId)
  }
}

data class GetEmployeeTeamDuringReviewCycleResult(
  val reviewCycleId: Long,
  val id: Long,
  val teamId: Long,
  val teamName: String
)

class GetEmployeeTeamDuringReviewCycleRowMapper : RowMapper<GetEmployeeTeamDuringReviewCycleResult>
    {
  override fun map(rs: ResultSet): GetEmployeeTeamDuringReviewCycleResult =
      GetEmployeeTeamDuringReviewCycleResult(
  reviewCycleId = rs.getObject("review_cycle_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    teamId = rs.getObject("team_id") as kotlin.Long,
    teamName = rs.getObject("team_name") as kotlin.String)
}

class GetEmployeeTeamDuringReviewCycleQuery : Query<GetEmployeeTeamDuringReviewCycleParams,
    GetEmployeeTeamDuringReviewCycleResult> {
  override val sql: String = """
      |SELECT
      |    rc.id AS review_cycle_id,
      |    etm.id,
      |    etm.team_id,
      |    t.team_name
      |FROM
      |    review_cycle rc
      |JOIN employees_team_mapping etm
      |ON etm.emp_id = ?
      |AND etm.joined_at <= rc.end_date
      |AND (etm.left_at IS NULL OR etm.left_at >= rc.start_date)
      |JOIN teams t ON etm.team_id = t.id
      |WHERE rc.organisation_id = ?
      |  AND rc.id = ?
      |ORDER BY etm.id
      |DESC LIMIT 1;
      |""".trimMargin()

  override val mapper: RowMapper<GetEmployeeTeamDuringReviewCycleResult> =
      GetEmployeeTeamDuringReviewCycleRowMapper()

  override val paramSetter: ParamSetter<GetEmployeeTeamDuringReviewCycleParams> =
      GetEmployeeTeamDuringReviewCycleParamSetter()
}
