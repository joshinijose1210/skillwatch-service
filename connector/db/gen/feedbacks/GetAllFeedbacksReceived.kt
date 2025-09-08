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

data class GetAllFeedbacksReceivedParams(
  val organisationId: Long?,
  val feedbackToId: Long?,
  val feedbackFromId: Array<Int>?,
  val feedbackTypeId: Array<Int>?,
  val reviewCycleId: Array<Int>?,
  val sortBy: String?,
  val offset: Int?,
  val limit: Int?
)

class GetAllFeedbacksReceivedParamSetter : ParamSetter<GetAllFeedbacksReceivedParams> {
  override fun map(ps: PreparedStatement, params: GetAllFeedbacksReceivedParams) {
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
    ps.setObject(12, params.sortBy)
    ps.setObject(13, params.sortBy)
    ps.setObject(14, params.offset)
    ps.setObject(15, params.limit)
  }
}

data class GetAllFeedbacksReceivedResult(
  val srNo: Long,
  val updatedAt: Timestamp,
  val feedbackToId: Long,
  val feedbackToEmployeeId: String,
  val feedbackFromId: Long?,
  val organisationId: Long,
  val feedbackFromEmployeeId: String?,
  val feedbackFromFirstName: String?,
  val feedbackFromLastName: String?,
  val feedbackFromRole: String?,
  val externalFeedbackFromEmail: String?,
  val feedback: String,
  val feedbackTypeId: Int,
  val name: String,
  val isDraft: Boolean
)

class GetAllFeedbacksReceivedRowMapper : RowMapper<GetAllFeedbacksReceivedResult> {
  override fun map(rs: ResultSet): GetAllFeedbacksReceivedResult = GetAllFeedbacksReceivedResult(
  srNo = rs.getObject("sr_no") as kotlin.Long,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp,
    feedbackToId = rs.getObject("feedback_to_id") as kotlin.Long,
    feedbackToEmployeeId = rs.getObject("feedback_to_employee_id") as kotlin.String,
    feedbackFromId = rs.getObject("feedback_from_id") as kotlin.Long?,
    organisationId = rs.getObject("organisation_id") as kotlin.Long,
    feedbackFromEmployeeId = rs.getObject("feedback_from_employee_id") as kotlin.String?,
    feedbackFromFirstName = rs.getObject("feedback_from_first_name") as kotlin.String?,
    feedbackFromLastName = rs.getObject("feedback_from_last_name") as kotlin.String?,
    feedbackFromRole = rs.getObject("feedback_from_role") as kotlin.String?,
    externalFeedbackFromEmail = rs.getObject("external_feedback_from_email") as kotlin.String?,
    feedback = rs.getObject("feedback") as kotlin.String,
    feedbackTypeId = rs.getObject("feedback_type_id") as kotlin.Int,
    name = rs.getObject("name") as kotlin.String,
    isDraft = rs.getObject("is_draft") as kotlin.Boolean)
}

class GetAllFeedbacksReceivedQuery : Query<GetAllFeedbacksReceivedParams,
    GetAllFeedbacksReceivedResult> {
  override val sql: String = """
      |SELECT
      |  feedbacks.sr_no,
      |  feedbacks.updated_at,
      |  feedbacks.feedback_to AS feedback_to_id,
      |  emp_to.emp_id AS feedback_to_employee_id,
      |  feedbacks.feedback_from AS feedback_from_id,
      |  emp_to.organisation_id,
      |  COALESCE(emp_from.emp_id, null) AS feedback_from_employee_id,
      |  COALESCE(emp_from.first_name, null) AS feedback_from_first_name,
      |  COALESCE(emp_from.last_name, null) AS feedback_from_last_name,
      |  COALESCE(roles.role_name, null) AS feedback_from_role,
      |  COALESCE(external_email.email_id, null) AS external_feedback_from_email,
      |  feedbacks.feedback,
      |  feedbacks.feedback_type_id,
      |  feedback_types.name,
      |  feedbacks.is_draft
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
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY(?::INT[]))
      |ORDER BY
      |  CASE WHEN ? = 'dateDesc' THEN feedbacks.updated_at END DESC,
      |  CASE WHEN ? = 'dateAsc' THEN feedbacks.updated_at END ASC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      """.trimMargin()

  override val mapper: RowMapper<GetAllFeedbacksReceivedResult> = GetAllFeedbacksReceivedRowMapper()

  override val paramSetter: ParamSetter<GetAllFeedbacksReceivedParams> =
      GetAllFeedbacksReceivedParamSetter()
}
