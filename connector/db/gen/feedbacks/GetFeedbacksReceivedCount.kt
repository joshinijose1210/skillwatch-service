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

data class GetFeedbacksReceivedCountParams(
  val organisationId: Long?,
  val feedbackToId: Long?,
  val feedbackFromId: Array<Int>?,
  val feedbackTypeId: Array<Int>?,
  val reviewCycleId: Array<Int>?
)

class GetFeedbacksReceivedCountParamSetter : ParamSetter<GetFeedbacksReceivedCountParams> {
  override fun map(ps: PreparedStatement, params: GetFeedbacksReceivedCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.organisationId)
    ps.setObject(4, params.organisationId)
    ps.setObject(5, params.feedbackToId)
    ps.setArray(6, ps.connection.createArrayOf("int4", params.feedbackFromId))
    ps.setArray(7, ps.connection.createArrayOf("int4", params.feedbackFromId))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.feedbackTypeId))
    ps.setArray(9, ps.connection.createArrayOf("int4", params.feedbackTypeId))
    ps.setArray(10, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(11, ps.connection.createArrayOf("int4", params.reviewCycleId))
  }
}

data class GetFeedbacksReceivedCountResult(
  val feedbackCount: Long?
)

class GetFeedbacksReceivedCountRowMapper : RowMapper<GetFeedbacksReceivedCountResult> {
  override fun map(rs: ResultSet): GetFeedbacksReceivedCountResult =
      GetFeedbacksReceivedCountResult(
  feedbackCount = rs.getObject("feedback_count") as kotlin.Long?)
}

class GetFeedbacksReceivedCountQuery : Query<GetFeedbacksReceivedCountParams,
    GetFeedbacksReceivedCountResult> {
  override val sql: String = """
      |SELECT COUNT(feedbacks.sr_no) as feedback_count
      |FROM
      |  feedbacks
      |  LEFT JOIN employees AS emp_from ON feedbacks.feedback_from = emp_from.id AND emp_from.organisation_id = ?
      |  INNER JOIN employees AS emp_to ON feedbacks.feedback_to = emp_to.id AND emp_to.organisation_id = ?
      |  LEFT JOIN external_feedback_emails AS external_email
      |      ON feedbacks.feedback_from_external_id = external_email.id
      |      AND external_email.organisation_id = ?
      |  INNER JOIN feedback_types ON feedbacks.feedback_type_id = feedback_types.id
      |  LEFT JOIN employees_role_mapping ON employees_role_mapping.emp_id = emp_from.id
      |  LEFT JOIN roles ON roles.id = employees_role_mapping.role_id
      |  LEFT JOIN review_cycle ON DATE(feedbacks.updated_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
      |  AND review_cycle.organisation_id = ?
      |WHERE
      |  feedbacks.is_draft = false
      |  AND feedbacks.feedback_to = ?
      |  AND (?::INT[] = '{-99}' OR feedbacks.feedback_from = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}' OR feedbacks.feedback_type_id = ANY(?::INT[]))
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY(?::INT[]));
      """.trimMargin()

  override val mapper: RowMapper<GetFeedbacksReceivedCountResult> =
      GetFeedbacksReceivedCountRowMapper()

  override val paramSetter: ParamSetter<GetFeedbacksReceivedCountParams> =
      GetFeedbacksReceivedCountParamSetter()
}
