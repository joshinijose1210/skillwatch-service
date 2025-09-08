package feedbacks

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetFeedbackRequestDataParams(
  val organisationId: Long?,
  val requestedById: Array<Int>?,
  val feedbackFromId: Array<Int>?,
  val feedbackToId: Array<Int>?,
  val isSubmitted: Array<String>?,
  val reviewCycleId: Array<Int>?,
  val sortBy: String?,
  val offset: Int?,
  val limit: Int?
)

class GetFeedbackRequestDataParamSetter : ParamSetter<GetFeedbackRequestDataParams> {
  override fun map(ps: PreparedStatement, params: GetFeedbackRequestDataParams) {
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
    ps.setObject(16, params.sortBy)
    ps.setObject(17, params.sortBy)
    ps.setObject(18, params.offset)
    ps.setObject(19, params.limit)
  }
}

data class GetFeedbackRequestDataResult(
  val requestId: Long,
  val requestedById: Long,
  val feedbackToId: Long,
  val feedbackFromId: Long?,
  val requestedOn: Timestamp,
  val isSubmitted: Boolean?,
  val isExternalRequest: Boolean,
  val requestedByEmployeeId: String,
  val requestedByFirstName: String,
  val requestedByLastName: String,
  val organisationId: Long,
  val feedbackToEmployeeId: String,
  val feedbackToFirstName: String,
  val feedbackToLastName: String,
  val feedbackFromEmployeeId: String?,
  val feedbackFromFirstName: String?,
  val feedbackFromLastName: String?,
  val externalFeedbackFromEmail: String?,
  val isDraft: Boolean?
)

class GetFeedbackRequestDataRowMapper : RowMapper<GetFeedbackRequestDataResult> {
  override fun map(rs: ResultSet): GetFeedbackRequestDataResult = GetFeedbackRequestDataResult(
  requestId = rs.getObject("request_id") as kotlin.Long,
    requestedById = rs.getObject("requested_by_id") as kotlin.Long,
    feedbackToId = rs.getObject("feedback_to_id") as kotlin.Long,
    feedbackFromId = rs.getObject("feedback_from_id") as kotlin.Long?,
    requestedOn = rs.getObject("requested_on") as java.sql.Timestamp,
    isSubmitted = rs.getObject("is_submitted") as kotlin.Boolean?,
    isExternalRequest = rs.getObject("is_external_request") as kotlin.Boolean,
    requestedByEmployeeId = rs.getObject("requested_by_employee_id") as kotlin.String,
    requestedByFirstName = rs.getObject("requested_by_first_name") as kotlin.String,
    requestedByLastName = rs.getObject("requested_by_last_name") as kotlin.String,
    organisationId = rs.getObject("organisation_id") as kotlin.Long,
    feedbackToEmployeeId = rs.getObject("feedback_to_employee_id") as kotlin.String,
    feedbackToFirstName = rs.getObject("feedback_to_first_name") as kotlin.String,
    feedbackToLastName = rs.getObject("feedback_to_last_name") as kotlin.String,
    feedbackFromEmployeeId = rs.getObject("feedback_from_employee_id") as kotlin.String?,
    feedbackFromFirstName = rs.getObject("feedback_from_first_name") as kotlin.String?,
    feedbackFromLastName = rs.getObject("feedback_from_last_name") as kotlin.String?,
    externalFeedbackFromEmail = rs.getObject("external_feedback_from_email") as kotlin.String?,
    isDraft = rs.getObject("is_draft") as kotlin.Boolean?)
}

class GetFeedbackRequestDataQuery : Query<GetFeedbackRequestDataParams,
    GetFeedbackRequestDataResult> {
  override val sql: String = """
      |SELECT
      |  feedback_request.id AS request_id,
      |  feedback_request.requested_by AS requested_by_id,
      |  feedback_request.feedback_to AS feedback_to_id,
      |  feedback_request.feedback_from AS feedback_from_id,
      |  feedback_request.created_at AS requested_on,
      |  feedback_request.is_submitted,
      |  feedback_request.is_external_request,
      |  requested_by.emp_id AS requested_by_employee_id,
      |  requested_by.first_name AS requested_by_first_name,
      |  requested_by.last_name AS requested_by_last_name,
      |  feedback_to.organisation_id AS organisation_id,
      |  feedback_to.emp_id AS feedback_to_employee_id,
      |  feedback_to.first_name AS feedback_to_first_name,
      |  feedback_to.last_name AS feedback_to_last_name,
      |  COALESCE(feedback_from.emp_id, null) AS feedback_from_employee_id,
      |  COALESCE(feedback_from.first_name, null) AS feedback_from_first_name,
      |  COALESCE(feedback_from.last_name, null) AS feedback_from_last_name,
      |  COALESCE(external_email.email_id, null) AS external_feedback_from_email,
      |  COALESCE(d.has_draft_feedback, false) AS is_draft
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
      |  LEFT JOIN feedback_request_draft_flags d
      |     ON d.feedback_request_id = feedback_request.id
      |WHERE
      |  (?::INT[] = '{-99}' OR feedback_request.requested_by = ANY(?::INT[]))
      |  AND (?::INT[] = '{-99}' OR feedback_request.feedback_from = ANY(?::INT[]))
      |  AND (?::INT[] = '{-99}' OR feedback_request.feedback_to = ANY(?::INT[]))
      |  AND (?::BOOL[] = '{true,false}' OR feedback_request.is_submitted = ANY(?::BOOL[]))
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY(?::INT[]))
      |ORDER BY
      |  CASE WHEN ? = 'dateDesc' THEN feedback_request.created_at END DESC,
      |  CASE WHEN ? = 'dateAsc' THEN feedback_request.created_at END ASC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      |""".trimMargin()

  override val mapper: RowMapper<GetFeedbackRequestDataResult> = GetFeedbackRequestDataRowMapper()

  override val paramSetter: ParamSetter<GetFeedbackRequestDataParams> =
      GetFeedbackRequestDataParamSetter()
}
