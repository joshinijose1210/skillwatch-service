package dashboard

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

data class GetEmployeeFeedbacksParams(
  val reviewCycleId: Array<Int>?,
  val organisationId: Long?,
  val id: Array<Int>?,
  val feedbackTypeId: Array<Int>?,
  val offset: Int?,
  val limit: Int?
)

class GetEmployeeFeedbacksParamSetter : ParamSetter<GetEmployeeFeedbacksParams> {
  override fun map(ps: PreparedStatement, params: GetEmployeeFeedbacksParams) {
    ps.setArray(1, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setObject(2, params.organisationId)
    ps.setArray(3, ps.connection.createArrayOf("int4", params.id))
    ps.setArray(4, ps.connection.createArrayOf("int4", params.id))
    ps.setArray(5, ps.connection.createArrayOf("int4", params.feedbackTypeId))
    ps.setObject(6, params.offset)
    ps.setObject(7, params.limit)
  }
}

data class GetEmployeeFeedbacksResult(
  val feedbackToId: Long,
  val feedbackToEmployeeId: String,
  val feedbackToFirstName: String,
  val feedbackToLastName: String,
  val feedbackToRoleName: String,
  val feedback: String,
  val feedbackFromId: Long?,
  val feedbackFromEmployeeId: String?,
  val feedbackFromFirstName: String?,
  val feedbackFromLastName: String?,
  val feedbackFromRoleName: String?,
  val externalFeedbackFromEmailId: String?,
  val submitDate: Timestamp,
  val feedbackType: String,
  val isDraft: Boolean
)

class GetEmployeeFeedbacksRowMapper : RowMapper<GetEmployeeFeedbacksResult> {
  override fun map(rs: ResultSet): GetEmployeeFeedbacksResult = GetEmployeeFeedbacksResult(
  feedbackToId = rs.getObject("feedback_to_id") as kotlin.Long,
    feedbackToEmployeeId = rs.getObject("feedback_to_employee_id") as kotlin.String,
    feedbackToFirstName = rs.getObject("feedback_to_first_name") as kotlin.String,
    feedbackToLastName = rs.getObject("feedback_to_last_name") as kotlin.String,
    feedbackToRoleName = rs.getObject("feedback_to_role_name") as kotlin.String,
    feedback = rs.getObject("feedback") as kotlin.String,
    feedbackFromId = rs.getObject("feedback_from_id") as kotlin.Long?,
    feedbackFromEmployeeId = rs.getObject("feedback_from_employee_id") as kotlin.String?,
    feedbackFromFirstName = rs.getObject("feedback_from_first_name") as kotlin.String?,
    feedbackFromLastName = rs.getObject("feedback_from_last_name") as kotlin.String?,
    feedbackFromRoleName = rs.getObject("feedback_from_role_name") as kotlin.String?,
    externalFeedbackFromEmailId = rs.getObject("external_feedback_from_email_id") as kotlin.String?,
    submitDate = rs.getObject("submit_date") as java.sql.Timestamp,
    feedbackType = rs.getObject("feedback_type") as kotlin.String,
    isDraft = rs.getObject("is_draft") as kotlin.Boolean)
}

class GetEmployeeFeedbacksQuery : Query<GetEmployeeFeedbacksParams, GetEmployeeFeedbacksResult> {
  override val sql: String = """
      |SELECT
      |  review.id AS feedback_to_id,
      |  review.emp_id AS feedback_to_employee_id,
      |  review.first_name AS feedback_to_first_name,
      |  review.last_name AS feedback_to_last_name,
      |  feedback_to_role.role_name AS feedback_to_role_name,
      |  feedbacks.feedback AS feedback,
      |  COALESCE(feedback_from.id, null) AS feedback_from_id,
      |  COALESCE(feedback_from.emp_id, null) AS feedback_from_employee_id,
      |  COALESCE(feedback_from.first_name, null) AS feedback_from_first_name,
      |  COALESCE(feedback_from.last_name, null) AS feedback_from_last_name,
      |  COALESCE(feedback_from_role.role_name, null) AS feedback_from_role_name,
      |  COALESCE(external_email.email_id, null) AS external_feedback_from_email_id,
      |  feedbacks.updated_at AS submit_date,
      |  feedback_types.name AS feedback_type,
      |  feedbacks.is_draft
      |FROM
      |  (
      |    SELECT
      |      employees.id,
      |      employees.emp_id,
      |      employees.first_name,
      |      employees.last_name,
      |      review_cycle.id AS review_cycle_id,
      |      review_cycle.start_date AS start_date,
      |      review_cycle.end_date AS end_date
      |    FROM
      |      employees
      |      INNER JOIN review_cycle ON employees.organisation_id = review_cycle.organisation_id
      |    WHERE
      |      review_cycle.id = ANY(?::INT[])
      |      AND employees.organisation_id = ?
      |  ) AS review
      |  LEFT JOIN feedbacks ON review.id = feedbacks.feedback_to
      |    AND DATE(feedbacks.updated_at) BETWEEN review.start_date AND review.end_date
      |  JOIN feedback_types ON feedbacks.feedback_type_id = feedback_types.id
      |  LEFT JOIN employees_role_mapping AS feedback_to_role_mapping ON feedback_to_role_mapping.emp_id = review.id
      |  LEFT JOIN roles AS feedback_to_role ON feedback_to_role.id = feedback_to_role_mapping.role_id
      |  LEFT JOIN employees AS feedback_from ON feedbacks.feedback_from = feedback_from.id
      |  LEFT JOIN employees_role_mapping AS feedback_from_role_mapping ON feedback_from_role_mapping.emp_id = feedback_from.id
      |  LEFT JOIN roles AS feedback_from_role ON feedback_from_role.id = feedback_from_role_mapping.role_id
      |  LEFT JOIN external_feedback_emails AS external_email ON feedbacks.feedback_from_external_id = external_email.id
      |  WHERE
      |    feedbacks.is_draft = false
      |    AND CASE WHEN ?::INT[] = '{-99}' THEN 1 = 1 ELSE feedbacks.feedback_to = ANY (?::INT[]) END
      |    AND feedbacks.feedback_type_id = ANY (?::INT[])
      |    AND DATE(feedbacks.updated_at) BETWEEN review.start_date AND review.end_date
      |ORDER BY
      |  feedbacks.updated_at DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      """.trimMargin()

  override val mapper: RowMapper<GetEmployeeFeedbacksResult> = GetEmployeeFeedbacksRowMapper()

  override val paramSetter: ParamSetter<GetEmployeeFeedbacksParams> =
      GetEmployeeFeedbacksParamSetter()
}
