package feedbacks

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllFeedbacksParams(
  val organisationId: Long?,
  val fromDate: String?,
  val toDate: String?,
  val search: String?,
  val feedbackTypeId: Array<Int>?,
  val reviewCycleId: Array<Int>?,
  val sortBy: String?,
  val offset: Int?,
  val limit: Int?
)

class GetAllFeedbacksParamSetter : ParamSetter<GetAllFeedbacksParams> {
  override fun map(ps: PreparedStatement, params: GetAllFeedbacksParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.fromDate)
    ps.setObject(3, params.toDate)
    ps.setObject(4, params.fromDate)
    ps.setObject(5, params.toDate)
    ps.setObject(6, params.search)
    ps.setObject(7, params.search)
    ps.setObject(8, params.search)
    ps.setObject(9, params.search)
    ps.setObject(10, params.search)
    ps.setObject(11, params.search)
    ps.setObject(12, params.search)
    ps.setObject(13, params.search)
    ps.setObject(14, params.search)
    ps.setObject(15, params.search)
    ps.setArray(16, ps.connection.createArrayOf("int4", params.feedbackTypeId))
    ps.setArray(17, ps.connection.createArrayOf("int4", params.feedbackTypeId))
    ps.setObject(18, params.organisationId)
    ps.setArray(19, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(20, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setObject(21, params.sortBy)
    ps.setObject(22, params.sortBy)
    ps.setObject(23, params.offset)
    ps.setObject(24, params.limit)
  }
}

data class GetAllFeedbacksResult(
  val updatedAt: Date?,
  val feedbackFromId: Long?,
  val feedbackFromEmployeeId: String?,
  val feedbackFromFirstName: String?,
  val feedbackFromLastName: String?,
  val feedbackFromRole: String?,
  val externalFeedbackFromEmail: String?,
  val feedbackToId: Long,
  val feedbackToEmployeeId: String,
  val feedbackToFirstName: String?,
  val feedbackToLastName: String?,
  val feedbackToRole: String?,
  val feedback: String,
  val feedbackTypeId: Int,
  val isDraft: Boolean,
  val name: String,
  val organisationId: Long
)

class GetAllFeedbacksRowMapper : RowMapper<GetAllFeedbacksResult> {
  override fun map(rs: ResultSet): GetAllFeedbacksResult = GetAllFeedbacksResult(
  updatedAt = rs.getObject("updated_at") as java.sql.Date?,
    feedbackFromId = rs.getObject("feedback_from_id") as kotlin.Long?,
    feedbackFromEmployeeId = rs.getObject("feedback_from_employee_id") as kotlin.String?,
    feedbackFromFirstName = rs.getObject("feedback_from_first_name") as kotlin.String?,
    feedbackFromLastName = rs.getObject("feedback_from_last_name") as kotlin.String?,
    feedbackFromRole = rs.getObject("feedback_from_role") as kotlin.String?,
    externalFeedbackFromEmail = rs.getObject("external_feedback_from_email") as kotlin.String?,
    feedbackToId = rs.getObject("feedback_to_id") as kotlin.Long,
    feedbackToEmployeeId = rs.getObject("feedback_to_employee_id") as kotlin.String,
    feedbackToFirstName = rs.getObject("feedback_to_first_name") as kotlin.String?,
    feedbackToLastName = rs.getObject("feedback_to_last_name") as kotlin.String?,
    feedbackToRole = rs.getObject("feedback_to_role") as kotlin.String?,
    feedback = rs.getObject("feedback") as kotlin.String,
    feedbackTypeId = rs.getObject("feedback_type_id") as kotlin.Int,
    isDraft = rs.getObject("is_draft") as kotlin.Boolean,
    name = rs.getObject("name") as kotlin.String,
    organisationId = rs.getObject("organisation_id") as kotlin.Long)
}

class GetAllFeedbacksQuery : Query<GetAllFeedbacksParams, GetAllFeedbacksResult> {
  override val sql: String = """
      |SELECT
      |  feedbacks.updated_at::DATE ,
      |  COALESCE(feedbacks.feedback_from, null) AS feedback_from_id ,
      |  COALESCE(feedback_from_details.emp_id, null) AS feedback_from_employee_id ,
      |  COALESCE(feedback_from_details.first_name, null) AS feedback_from_first_name ,
      |  COALESCE(feedback_from_details.last_name, null) AS feedback_from_last_name ,
      |  COALESCE(feedback_from_role.role_name, null) AS feedback_from_role ,
      |  COALESCE(external_email.email_id, null) AS external_feedback_from_email,
      |  feedbacks.feedback_to AS feedback_to_id ,
      |  feedback_to_details.emp_id AS feedback_to_employee_id ,
      |  COALESCE(feedback_to_details.first_name, null) AS feedback_to_first_name ,
      |  COALESCE(feedback_to_details.last_name, null) AS feedback_to_last_name ,
      |  feedback_to_role.role_name AS feedback_to_role ,
      |  feedbacks.feedback,
      |  feedbacks.feedback_type_id,
      |  feedbacks.is_draft,
      |  feedback_types.name,
      |  feedback_to_details.organisation_id
      |FROM
      |  feedbacks
      |  JOIN employees AS feedback_to_details ON feedback_to_details.id = feedbacks.feedback_to
      |  JOIN employees_role_mapping_view AS feedback_to_role ON feedback_to_details.id = feedback_to_role.emp_id
      |  LEFT JOIN employees AS feedback_from_details ON feedback_from_details.id = feedbacks.feedback_from
      |  LEFT JOIN employees_role_mapping_view AS feedback_from_role ON feedback_from_details.id = feedback_from_role.emp_id
      |  LEFT JOIN external_feedback_emails AS external_email ON external_email.id = feedbacks.feedback_from_external_id
      |  INNER JOIN feedback_types ON feedbacks.feedback_type_id = feedback_types.id
      |  LEFT JOIN review_cycle ON DATE(feedbacks.updated_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
      |  AND review_cycle.organisation_id = ?
      |WHERE
      |  feedbacks.is_draft = false
      |  AND( (CAST (?  AS text) IS  NULL OR  CAST(?  AS text)IS NULL) OR
      |        feedbacks.updated_at::DATE BETWEEN ?::DATE AND ?::DATE)
      |  AND( (CAST(? as text) IS NULL)
      |            OR feedbacks.feedback_to IN
      |            (SELECT id FROM employees WHERE
      |              (UPPER(employees.emp_id) LIKE  UPPER(?)
      |              OR UPPER(employees.first_name) LIKE  UPPER(?)
      |              OR UPPER(employees.last_name) LIKE  UPPER(?)
      |              OR UPPER(employees.first_name) || ' ' || UPPER(employees.last_name) LIKE  UPPER(?) )
      |            )
      |            OR feedbacks.feedback_from IN
      |            (SELECT id FROM employees WHERE
      |              (UPPER(employees.emp_id) LIKE  UPPER(?)
      |              OR UPPER(employees.first_name) LIKE UPPER(?)
      |              OR UPPER(employees.last_name) LIKE UPPER(?)
      |              OR UPPER(employees.first_name) || ' ' || UPPER(employees.last_name) LIKE UPPER(?) )
      |            )
      |            OR feedbacks.feedback_from_external_id IN
      |            (SELECT id FROM external_feedback_emails WHERE
      |                (UPPER(external_feedback_emails.email_id) LIKE UPPER(?))
      |            )
      |        )
      |  AND (?::INT[] = '{-99}' OR feedbacks.feedback_type_id = ANY(?::INT[]))
      |  AND feedback_to_details.organisation_id = ?
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY(?::INT[]))
      |ORDER BY
      |  CASE WHEN ? = 'dateDesc' THEN feedbacks.updated_at END DESC,
      |  CASE WHEN ? = 'dateAsc' THEN feedbacks.updated_at END ASC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      """.trimMargin()

  override val mapper: RowMapper<GetAllFeedbacksResult> = GetAllFeedbacksRowMapper()

  override val paramSetter: ParamSetter<GetAllFeedbacksParams> = GetAllFeedbacksParamSetter()
}
