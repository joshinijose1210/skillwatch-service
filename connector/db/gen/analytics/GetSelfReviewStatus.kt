package analytics

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetSelfReviewStatusParams(
  val organisationId: Long?,
  val reviewCycleId: Long?
)

class GetSelfReviewStatusParamSetter : ParamSetter<GetSelfReviewStatusParams> {
  override fun map(ps: PreparedStatement, params: GetSelfReviewStatusParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.reviewCycleId)
  }
}

data class GetSelfReviewStatusResult(
  val inProgress: Long?,
  val completed: Long?
)

class GetSelfReviewStatusRowMapper : RowMapper<GetSelfReviewStatusResult> {
  override fun map(rs: ResultSet): GetSelfReviewStatusResult = GetSelfReviewStatusResult(
  inProgress = rs.getObject("in_progress") as kotlin.Long?,
    completed = rs.getObject("completed") as kotlin.Long?)
}

class GetSelfReviewStatusQuery : Query<GetSelfReviewStatusParams, GetSelfReviewStatusResult> {
  override val sql: String = """
      |SELECT
      |  SUM(CASE WHEN self_review.draft = true AND self_review.published = false THEN 1 ELSE 0 END) AS in_progress,
      |  SUM(CASE WHEN self_review.published = true AND self_review.draft = false THEN 1 ELSE 0 END) AS completed
      |FROM
      |  review_cycle
      |  INNER JOIN employees ON employees.organisation_id = review_cycle.organisation_id
      |  JOIN employees_history ON employees_history.employee_id = employees.id
      |    AND (DATE(employees_history.activated_at) <= review_cycle.end_date)
      |    AND (DATE(employees_history.deactivated_at) IS NULL OR DATE(employees_history.deactivated_at) >= review_cycle.start_date)
      |  LEFT JOIN review_details AS self_review ON self_review.review_cycle_id = review_cycle.id
      |  AND self_review.review_to = employees.id
      |  AND self_review.review_type_id = 1
      |WHERE
      |  review_cycle.organisation_id = ?
      |  AND review_cycle.id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetSelfReviewStatusResult> = GetSelfReviewStatusRowMapper()

  override val paramSetter: ParamSetter<GetSelfReviewStatusParams> =
      GetSelfReviewStatusParamSetter()
}
