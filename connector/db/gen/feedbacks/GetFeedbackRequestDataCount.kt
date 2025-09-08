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

data class GetFeedbackRequestDataCountParams(
  val organisationId: Long?,
  val requestedById: Array<Int>?,
  val feedbackFromId: Array<Int>?,
  val feedbackToId: Array<Int>?,
  val isSubmitted: Array<String>?,
  val reviewCycleId: Array<Int>?
)

class GetFeedbackRequestDataCountParamSetter : ParamSetter<GetFeedbackRequestDataCountParams> {
  override fun map(ps: PreparedStatement, params: GetFeedbackRequestDataCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.organisationId)
    ps.setObject(4, params.organisationId)
    ps.setObject(5, params.organisationId)
    ps.setArray(6, ps.connection.createArrayOf("int4", params.requestedById))
    ps.setArray(7, ps.connection.createArrayOf("int4", params.requestedById))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.feedbackFromId))
    ps.setArray(9, ps.connection.createArrayOf("int4", params.feedbackFromId))
    ps.setArray(10, ps.connection.createArrayOf("int4", params.feedbackToId))
    ps.setArray(11, ps.connection.createArrayOf("int4", params.feedbackToId))
    ps.setArray(12, ps.connection.createArrayOf("bool", params.isSubmitted))
    ps.setArray(13, ps.connection.createArrayOf("bool", params.isSubmitted))
    ps.setArray(14, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(15, ps.connection.createArrayOf("int4", params.reviewCycleId))
  }
}

data class GetFeedbackRequestDataCountResult(
  val feedbackRequestCount: Long?
)

class GetFeedbackRequestDataCountRowMapper : RowMapper<GetFeedbackRequestDataCountResult> {
  override fun map(rs: ResultSet): GetFeedbackRequestDataCountResult =
      GetFeedbackRequestDataCountResult(
  feedbackRequestCount = rs.getObject("feedback_request_count") as kotlin.Long?)
}

class GetFeedbackRequestDataCountQuery : Query<GetFeedbackRequestDataCountParams,
    GetFeedbackRequestDataCountResult> {
  override val sql: String = """
      |SELECT COUNT(feedback_request.id) AS feedback_request_count
      |FROM
      |  feedback_request
      |  INNER JOIN employees AS requested_by
      |    ON feedback_request.requested_by = requested_by.id
      |    AND requested_by.organisation_id = ?
      |    AND requested_by.status = true
      |  INNER JOIN employees AS feedback_to
      |    ON feedback_request.feedback_to = feedback_to.id
      |    AND feedback_to.organisation_id = ?
      |    AND feedback_to.status = true
      |  LEFT JOIN employees AS feedback_from
      |    ON feedback_request.feedback_from = feedback_from.id
      |    AND feedback_from.organisation_id = ?
      |  LEFT JOIN external_feedback_emails AS external_email
      |    ON feedback_request.feedback_from_external_id = external_email.id
      |    AND external_email.organisation_id = ?
      |  LEFT JOIN review_cycle ON DATE(feedback_request.created_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
      |    AND review_cycle.organisation_id = ?
      |WHERE
      |  (?::INT[] = '{-99}' OR feedback_request.requested_by = ANY(?::INT[]))
      |  AND (?::INT[] = '{-99}' OR feedback_request.feedback_from = ANY(?::INT[]))
      |  AND (?::INT[] = '{-99}' OR feedback_request.feedback_to = ANY(?::INT[]))
      |  AND (?::BOOL[] = '{true,false}' OR feedback_request.is_submitted = ANY(?::BOOL[]))
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY(?::INT[]));
      """.trimMargin()

  override val mapper: RowMapper<GetFeedbackRequestDataCountResult> =
      GetFeedbackRequestDataCountRowMapper()

  override val paramSetter: ParamSetter<GetFeedbackRequestDataCountParams> =
      GetFeedbackRequestDataCountParamSetter()
}
