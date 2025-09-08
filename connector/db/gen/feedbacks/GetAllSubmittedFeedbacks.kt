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

data class GetAllSubmittedFeedbacksParams(
  val organisationId: Long?,
  val feedbackFromId: Long?,
  val feedbackToId: Array<Int>?,
  val feedbackTypeId: Array<Int>?,
  val reviewCycleId: Array<Int>?,
  val sortBy: String?,
  val offset: Int?,
  val limit: Int?
)

class GetAllSubmittedFeedbacksParamSetter : ParamSetter<GetAllSubmittedFeedbacksParams> {
  override fun map(ps: PreparedStatement, params: GetAllSubmittedFeedbacksParams) {
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
    ps.setObject(11, params.sortBy)
    ps.setObject(12, params.sortBy)
    ps.setObject(13, params.offset)
    ps.setObject(14, params.limit)
  }
}

data class GetAllSubmittedFeedbacksResult(
  val srNo: Long,
  val updatedAt: Timestamp,
  val feedbackFromId: Long?,
  val feedbackFromEmployeeId: String,
  val feedbackToId: Long,
  val feedbackToEmployeeId: String,
  val organisationId: Long,
  val firstName: String,
  val lastName: String,
  val roleName: String,
  val feedback: String,
  val feedbackTypeId: Int,
  val name: String,
  val isDraft: Boolean
)

class GetAllSubmittedFeedbacksRowMapper : RowMapper<GetAllSubmittedFeedbacksResult> {
  override fun map(rs: ResultSet): GetAllSubmittedFeedbacksResult = GetAllSubmittedFeedbacksResult(
  srNo = rs.getObject("sr_no") as kotlin.Long,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp,
    feedbackFromId = rs.getObject("feedback_from_id") as kotlin.Long?,
    feedbackFromEmployeeId = rs.getObject("feedback_from_employee_id") as kotlin.String,
    feedbackToId = rs.getObject("feedback_to_id") as kotlin.Long,
    feedbackToEmployeeId = rs.getObject("feedback_to_employee_id") as kotlin.String,
    organisationId = rs.getObject("organisation_id") as kotlin.Long,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    roleName = rs.getObject("role_name") as kotlin.String,
    feedback = rs.getObject("feedback") as kotlin.String,
    feedbackTypeId = rs.getObject("feedback_type_id") as kotlin.Int,
    name = rs.getObject("name") as kotlin.String,
    isDraft = rs.getObject("is_draft") as kotlin.Boolean)
}

class GetAllSubmittedFeedbacksQuery : Query<GetAllSubmittedFeedbacksParams,
    GetAllSubmittedFeedbacksResult> {
  override val sql: String = """
      |SELECT
      |  feedbacks.sr_no,
      |  feedbacks.updated_at,
      |  feedbacks.feedback_from AS feedback_from_id,
      |  emp_from.emp_id AS feedback_from_employee_id,
      |  feedbacks.feedback_to AS feedback_to_id,
      |  emp_to.emp_id AS feedback_to_employee_id,
      |  emp_to.organisation_id,
      |  emp_to.first_name,
      |  emp_to.last_name,
      |  roles.role_name,
      |  feedbacks.feedback,
      |  feedbacks.feedback_type_id,
      |  feedback_types.name,
      |  feedbacks.is_draft
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
      |  AND (feedbacks.request_id IS NULL OR (feedbacks.request_id IS NOT NULL AND feedbacks.is_draft = false))
      |ORDER BY
      |  CASE WHEN ? = 'dateDesc' THEN feedbacks.updated_at END DESC,
      |  CASE WHEN ? = 'dateAsc' THEN feedbacks.updated_at END ASC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      |""".trimMargin()

  override val mapper: RowMapper<GetAllSubmittedFeedbacksResult> =
      GetAllSubmittedFeedbacksRowMapper()

  override val paramSetter: ParamSetter<GetAllSubmittedFeedbacksParams> =
      GetAllSubmittedFeedbacksParamSetter()
}
