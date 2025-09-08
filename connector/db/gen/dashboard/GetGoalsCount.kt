package dashboard

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetGoalsCountParams(
  val reviewCycleId: Long?,
  val organisationId: Long?,
  val reviewToId: Long?
)

class GetGoalsCountParamSetter : ParamSetter<GetGoalsCountParams> {
  override fun map(ps: PreparedStatement, params: GetGoalsCountParams) {
    ps.setObject(1, params.reviewCycleId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.organisationId)
    ps.setObject(4, params.reviewToId)
  }
}

data class GetGoalsCountResult(
  val goalsCount: Long?
)

class GetGoalsCountRowMapper : RowMapper<GetGoalsCountResult> {
  override fun map(rs: ResultSet): GetGoalsCountResult = GetGoalsCountResult(
  goalsCount = rs.getObject("goals_count") as kotlin.Long?)
}

class GetGoalsCountQuery : Query<GetGoalsCountParams, GetGoalsCountResult> {
  override val sql: String = """
      |SELECT COUNT(g.id) AS goals_count
      |FROM
      |  goals g
      |  JOIN review_details ON review_details.id = g.review_details_id
      |  JOIN (
      |    SELECT start_date, end_date
      |    FROM review_cycle
      |    WHERE review_cycle.id = ? AND review_cycle.organisation_id = ?
      |  ) AS data ON data.start_date BETWEEN g.created_at AND g.target_date
      |  JOIN review_cycle ON review_cycle.id = review_details.review_cycle_id AND review_cycle.organisation_id = ?
      |WHERE
      |  review_details.review_to = ?;
      |""".trimMargin()

  override val mapper: RowMapper<GetGoalsCountResult> = GetGoalsCountRowMapper()

  override val paramSetter: ParamSetter<GetGoalsCountParams> = GetGoalsCountParamSetter()
}
