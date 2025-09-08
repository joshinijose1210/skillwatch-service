package analytics

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetCheckInReviewStatusParams(
  val organisationId: Long?,
  val reviewCycleId: Long?
)

class GetCheckInReviewStatusParamSetter : ParamSetter<GetCheckInReviewStatusParams> {
  override fun map(ps: PreparedStatement, params: GetCheckInReviewStatusParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.reviewCycleId)
  }
}

data class GetCheckInReviewStatusResult(
  val inProgress: Long?,
  val completed: Long?
)

class GetCheckInReviewStatusRowMapper : RowMapper<GetCheckInReviewStatusResult> {
  override fun map(rs: ResultSet): GetCheckInReviewStatusResult = GetCheckInReviewStatusResult(
  inProgress = rs.getObject("in_progress") as kotlin.Long?,
    completed = rs.getObject("completed") as kotlin.Long?)
}

class GetCheckInReviewStatusQuery : Query<GetCheckInReviewStatusParams,
    GetCheckInReviewStatusResult> {
  override val sql: String = """
      |SELECT
      |    SUM(CASE WHEN check_in_review.draft = true AND check_in_review.published = false THEN 1 ELSE 0 END) AS in_progress,
      |    SUM(CASE WHEN check_in_review.published = true AND check_in_review.draft = false THEN 1 ELSE 0 END) AS completed
      |FROM
      |  review_cycle
      |  INNER JOIN employees ON employees.organisation_id = review_cycle.organisation_id
      |  JOIN employees_history ON employees_history.employee_id = employees.id
      |  AND (DATE(employees_history.activated_at) <= review_cycle.end_date)
      |  AND (DATE(employees_history.deactivated_at) IS NULL OR DATE(employees_history.deactivated_at) >= review_cycle.start_date)
      |  LEFT JOIN review_details AS check_in_review ON check_in_review.review_cycle_id = review_cycle.id
      |  AND check_in_review.review_to = employees.id
      |  AND check_in_review.review_type_id = 3
      |WHERE
      |  review_cycle.organisation_id = ?
      |  AND review_cycle.id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetCheckInReviewStatusResult> = GetCheckInReviewStatusRowMapper()

  override val paramSetter: ParamSetter<GetCheckInReviewStatusParams> =
      GetCheckInReviewStatusParamSetter()
}
