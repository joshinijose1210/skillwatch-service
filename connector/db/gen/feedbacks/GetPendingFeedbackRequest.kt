package feedbacks

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

class GetPendingFeedbackRequestParams

class GetPendingFeedbackRequestParamSetter : ParamSetter<GetPendingFeedbackRequestParams> {
  override fun map(ps: PreparedStatement, params: GetPendingFeedbackRequestParams) {
  }
}

data class GetPendingFeedbackRequestResult(
  val id: Long,
  val isExternalRequest: Boolean,
  val requestedById: Long,
  val requestedByEmpId: String,
  val requestedByFirstName: String,
  val requestedByLastName: String,
  val feedbackFromId: Long?,
  val feedbackFromFirstName: String?,
  val feedbackFromLastName: String?,
  val feedbackFromEmailId: String?,
  val externalFeedbackFromEmailId: String?,
  val date: Date?,
  val organisationName: String,
  val organisationTimeZone: String
)

class GetPendingFeedbackRequestRowMapper : RowMapper<GetPendingFeedbackRequestResult> {
  override fun map(rs: ResultSet): GetPendingFeedbackRequestResult =
      GetPendingFeedbackRequestResult(
  id = rs.getObject("id") as kotlin.Long,
    isExternalRequest = rs.getObject("is_external_request") as kotlin.Boolean,
    requestedById = rs.getObject("requested_by_id") as kotlin.Long,
    requestedByEmpId = rs.getObject("requested_by_emp_id") as kotlin.String,
    requestedByFirstName = rs.getObject("requested_by_first_name") as kotlin.String,
    requestedByLastName = rs.getObject("requested_by_last_name") as kotlin.String,
    feedbackFromId = rs.getObject("feedback_from_id") as kotlin.Long?,
    feedbackFromFirstName = rs.getObject("feedback_from_first_name") as kotlin.String?,
    feedbackFromLastName = rs.getObject("feedback_from_last_name") as kotlin.String?,
    feedbackFromEmailId = rs.getObject("feedback_from_email_id") as kotlin.String?,
    externalFeedbackFromEmailId = rs.getObject("external_feedback_from_email_id") as kotlin.String?,
    date = rs.getObject("date") as java.sql.Date?,
    organisationName = rs.getObject("organisation_name") as kotlin.String,
    organisationTimeZone = rs.getObject("organisation_time_zone") as kotlin.String)
}

class GetPendingFeedbackRequestQuery : Query<GetPendingFeedbackRequestParams,
    GetPendingFeedbackRequestResult> {
  override val sql: String = """
      |SELECT
      |    feedback_request.id,
      |    feedback_request.is_external_request,
      |    feedback_request.requested_by AS requested_by_id,
      |    requested_by_details.emp_id AS requested_by_emp_id,
      |    requested_by_details.first_name AS requested_by_first_name,
      |    requested_by_details.last_name AS requested_by_last_name,
      |    COALESCE(feedback_from_details.id, null) AS feedback_from_id,
      |    COALESCE(feedback_from_details.first_name, null) AS feedback_from_first_name,
      |    COALESCE(feedback_from_details.last_name, null) AS feedback_from_last_name,
      |    COALESCE(feedback_from_details.email_id, null) AS feedback_from_email_id,
      |    COALESCE(external_email.email_id, null) AS external_feedback_from_email_id,
      |    DATE(feedback_request.created_at),
      |    organisation_details.name AS organisation_name,
      |    organisation_details.time_zone AS organisation_time_zone
      |FROM feedback_request
      |JOIN employees AS requested_by_details ON requested_by_details.id = feedback_request.requested_by
      |JOIN employees AS feedback_to_details ON feedback_to_details.id = feedback_request.feedback_to
      |JOIN organisations AS organisation_details ON organisation_details.sr_no = feedback_to_details.organisation_id
      |LEFT JOIN employees AS feedback_from_details ON feedback_from_details.id = feedback_request.feedback_from
      |    AND feedback_from_details.status = TRUE
      |LEFT JOIN external_feedback_emails AS external_email ON feedback_request.feedback_from_external_id = external_email.id
      |WHERE feedback_request.is_submitted = FALSE
      |    AND feedback_to_details.status = TRUE ;
      |""".trimMargin()

  override val mapper: RowMapper<GetPendingFeedbackRequestResult> =
      GetPendingFeedbackRequestRowMapper()

  override val paramSetter: ParamSetter<GetPendingFeedbackRequestParams> =
      GetPendingFeedbackRequestParamSetter()
}
