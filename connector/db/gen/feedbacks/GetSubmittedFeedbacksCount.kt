package feedbacks

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetSubmittedFeedbacksCountParams(
  val organisationId: Long?,
  val feedbackFromId: Long?,
  val feedbackToId: Array<Int>?,
  val feedbackTypeId: Array<Int>?,
  val reviewCycleId: Array<Int>?
)

class GetSubmittedFeedbacksCountParamSetter : ParamSetter<GetSubmittedFeedbacksCountParams> {
  override fun map(ps: PreparedStatement, params: GetSubmittedFeedbacksCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.organisationId)
    ps.setObject(4, params.feedbackFromId)
    ps.setArray(5, ps.connection.createArrayOf("int4", params.feedbackToId))
    ps.setArray(6, ps.connection.createArrayOf("int4", params.feedbackToId))
    ps.setArray(7, ps.connection.createArrayOf("int4", params.feedbackTypeId))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.feedbackTypeId))
    ps.setArray(9, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(10, ps.connection.createArrayOf("int4", params.reviewCycleId))
  }
}

data class GetSubmittedFeedbacksCountResult(
  val feedbackCount: Long?
)

class GetSubmittedFeedbacksCountRowMapper : RowMapper<GetSubmittedFeedbacksCountResult> {
  override fun map(rs: ResultSet): GetSubmittedFeedbacksCountResult =
      GetSubmittedFeedbacksCountResult(
  feedbackCount = rs.getObject("feedback_count") as kotlin.Long?)
}

class GetSubmittedFeedbacksCountQuery : Query<GetSubmittedFeedbacksCountParams,
    GetSubmittedFeedbacksCountResult> {
  override val sql: String = """
      |SELECT COUNT(feedbacks.feedback_to) as feedback_count
      |FROM
      |  feedbacks
      |  INNER JOIN employees AS emp_to ON feedbacks.feedback_to = emp_to.id AND emp_to.organisation_id = ?
      |  INNER JOIN employees AS emp_from ON feedbacks.feedback_from = emp_from.id AND emp_from.organisation_id = ?
      |  INNER JOIN feedback_types ON feedbacks.feedback_type_id = feedback_types.id
      |  JOIN employees_role_mapping ON employees_role_mapping.emp_id = emp_to.id
      |  JOIN roles ON roles.id = employees_role_mapping.role_id
      |  LEFT JOIN review_cycle ON DATE(feedbacks.updated_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
      |  AND review_cycle.organisation_id = ?
      |WHERE
      |  feedbacks.feedback_from = ?
      |  AND NOT (feedbacks.is_draft = true AND emp_to.status = false)
      |  AND (?::INT[] = '{-99}' OR feedbacks.feedback_to = ANY(?::INT[]))
      |  AND (?::INT[] = '{-99}' OR feedbacks.feedback_type_id = ANY(?::INT[]))
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY(?::INT[]))
      |  AND (feedbacks.request_id IS NULL OR (feedbacks.request_id IS NOT NULL AND feedbacks.is_draft = false));
      |""".trimMargin()

  override val mapper: RowMapper<GetSubmittedFeedbacksCountResult> =
      GetSubmittedFeedbacksCountRowMapper()

  override val paramSetter: ParamSetter<GetSubmittedFeedbacksCountParams> =
      GetSubmittedFeedbacksCountParamSetter()
}
