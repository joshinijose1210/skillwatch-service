package feedbacks

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetFeedbackRequestDetailsParams(
  val requestId: Long?
)

class GetFeedbackRequestDetailsParamSetter : ParamSetter<GetFeedbackRequestDetailsParams> {
  override fun map(ps: PreparedStatement, params: GetFeedbackRequestDetailsParams) {
    ps.setObject(1, params.requestId)
  }
}

data class GetFeedbackRequestDetailsResult(
  val requestId: Long,
  val requestedById: Long,
  val feedbackToId: Long,
  val feedbackFromId: Long?,
  val request: String?,
  val requestedOn: Timestamp,
  val isSubmitted: Boolean?,
  val isExternalRequest: Boolean,
  val goalId: Long?,
  val goalDescription: String?,
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
  val externalFeedbackFromEmail: String?
)

class GetFeedbackRequestDetailsRowMapper : RowMapper<GetFeedbackRequestDetailsResult> {
  override fun map(rs: ResultSet): GetFeedbackRequestDetailsResult =
      GetFeedbackRequestDetailsResult(
  requestId = rs.getObject("request_id") as kotlin.Long,
    requestedById = rs.getObject("requested_by_id") as kotlin.Long,
    feedbackToId = rs.getObject("feedback_to_id") as kotlin.Long,
    feedbackFromId = rs.getObject("feedback_from_id") as kotlin.Long?,
    request = rs.getObject("request") as kotlin.String?,
    requestedOn = rs.getObject("requested_on") as java.sql.Timestamp,
    isSubmitted = rs.getObject("is_submitted") as kotlin.Boolean?,
    isExternalRequest = rs.getObject("is_external_request") as kotlin.Boolean,
    goalId = rs.getObject("goal_id") as kotlin.Long?,
    goalDescription = rs.getObject("goal_description") as kotlin.String?,
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
    externalFeedbackFromEmail = rs.getObject("external_feedback_from_email") as kotlin.String?)
}

class GetFeedbackRequestDetailsQuery : Query<GetFeedbackRequestDetailsParams,
    GetFeedbackRequestDetailsResult> {
  override val sql: String = """
      |SELECT
      |  feedback_request.id AS request_id,
      |  feedback_request.requested_by AS requested_by_id,
      |  feedback_request.feedback_to AS feedback_to_id,
      |  feedback_request.feedback_from AS feedback_from_id,
      |  feedback_request.request,
      |  feedback_request.created_at AS requested_on,
      |  feedback_request.is_submitted,
      |  feedback_request.is_external_request,
      |  COALESCE(goals.id, null) AS goal_id,
      |  COALESCE(goals.description, null) AS goal_description,
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
      |  COALESCE(external_email.email_id, null) AS external_feedback_from_email
      |FROM
      |  feedback_request
      | LEFT JOIN goals ON feedback_request.goal_id = goals.id
      |  INNER JOIN employees AS requested_by
      |    ON feedback_request.requested_by = requested_by.id
      |    AND requested_by.status = true
      |  INNER JOIN employees AS feedback_to
      |    ON feedback_request.feedback_to = feedback_to.id
      |    AND feedback_to.status = true
      |  LEFT JOIN employees AS feedback_from
      |    ON feedback_request.feedback_from = feedback_from.id
      |  LEFT JOIN external_feedback_emails AS external_email
      |    ON feedback_request.feedback_from_external_id = external_email.id
      |WHERE feedback_request.id = ?;
      |""".trimMargin()

  override val mapper: RowMapper<GetFeedbackRequestDetailsResult> =
      GetFeedbackRequestDetailsRowMapper()

  override val paramSetter: ParamSetter<GetFeedbackRequestDetailsParams> =
      GetFeedbackRequestDetailsParamSetter()
}
