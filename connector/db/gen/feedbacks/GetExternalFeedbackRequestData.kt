package feedbacks

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetExternalFeedbackRequestDataParams(
  val requestId: Long?
)

class GetExternalFeedbackRequestDataParamSetter : ParamSetter<GetExternalFeedbackRequestDataParams>
    {
  override fun map(ps: PreparedStatement, params: GetExternalFeedbackRequestDataParams) {
    ps.setObject(1, params.requestId)
  }
}

data class GetExternalFeedbackRequestDataResult(
  val requestId: Long,
  val request: String?,
  val feedbackToId: Long,
  val feedbackFromId: Long?,
  val requestedById: Long,
  val requestedByFirstName: String,
  val requestedByLastName: String,
  val feedbackToFirstName: String,
  val feedbackToLastName: String,
  val feedbackToTeam: String,
  val feedbackFromEmail: String,
  val organisationName: String
)

class GetExternalFeedbackRequestDataRowMapper : RowMapper<GetExternalFeedbackRequestDataResult> {
  override fun map(rs: ResultSet): GetExternalFeedbackRequestDataResult =
      GetExternalFeedbackRequestDataResult(
  requestId = rs.getObject("request_id") as kotlin.Long,
    request = rs.getObject("request") as kotlin.String?,
    feedbackToId = rs.getObject("feedback_to_id") as kotlin.Long,
    feedbackFromId = rs.getObject("feedback_from_id") as kotlin.Long?,
    requestedById = rs.getObject("requested_by_id") as kotlin.Long,
    requestedByFirstName = rs.getObject("requested_by_first_name") as kotlin.String,
    requestedByLastName = rs.getObject("requested_by_last_name") as kotlin.String,
    feedbackToFirstName = rs.getObject("feedback_to_first_name") as kotlin.String,
    feedbackToLastName = rs.getObject("feedback_to_last_name") as kotlin.String,
    feedbackToTeam = rs.getObject("feedback_to_team") as kotlin.String,
    feedbackFromEmail = rs.getObject("feedback_from_email") as kotlin.String,
    organisationName = rs.getObject("organisation_name") as kotlin.String)
}

class GetExternalFeedbackRequestDataQuery : Query<GetExternalFeedbackRequestDataParams,
    GetExternalFeedbackRequestDataResult> {
  override val sql: String = """
      |SELECT
      |  feedback_request.id AS request_id,
      |  feedback_request.request,
      |  feedback_request.feedback_to AS feedback_to_id,
      |  feedback_request.feedback_from_external_id AS feedback_from_id,
      |  requested_by.id AS requested_by_id,
      |  requested_by.first_name AS requested_by_first_name,
      |  requested_by.last_name AS requested_by_last_name,
      |  feedback_to.first_name AS feedback_to_first_name,
      |  feedback_to.last_name AS feedback_to_last_name,
      |  teams.team_name AS feedback_to_team,
      |  external_feedback_emails.email_id AS feedback_from_email,
      |  organisations.name AS organisation_name
      |FROM
      |  feedback_request
      |  INNER JOIN employees AS requested_by
      |    ON feedback_request.requested_by = requested_by.id
      |  INNER JOIN employees AS feedback_to
      |    ON feedback_request.feedback_to = feedback_to.id
      |  INNER JOIN organisations ON organisations.sr_no = feedback_to.organisation_id
      |  INNER JOIN external_feedback_emails ON external_feedback_emails.id = feedback_request.feedback_from_external_id
      |  LEFT JOIN employees_team_mapping_view ON employees_team_mapping_view.emp_id = feedback_to.id
      |  LEFT JOIN teams ON teams.id = employees_team_mapping_view.team_id
      |WHERE
      |    feedback_request.id = ?
      |    AND feedback_request.is_external_request = true ;
      |""".trimMargin()

  override val mapper: RowMapper<GetExternalFeedbackRequestDataResult> =
      GetExternalFeedbackRequestDataRowMapper()

  override val paramSetter: ParamSetter<GetExternalFeedbackRequestDataParams> =
      GetExternalFeedbackRequestDataParamSetter()
}
