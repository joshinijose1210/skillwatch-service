package feedbacks

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetFeedbackByIdParams(
  val organisationId: Long?,
  val feedbackId: Long?
)

class GetFeedbackByIdParamSetter : ParamSetter<GetFeedbackByIdParams> {
  override fun map(ps: PreparedStatement, params: GetFeedbackByIdParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.feedbackId)
  }
}

data class GetFeedbackByIdResult(
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
  val isDraft: Boolean,
  val isRead: Boolean
)

class GetFeedbackByIdRowMapper : RowMapper<GetFeedbackByIdResult> {
  override fun map(rs: ResultSet): GetFeedbackByIdResult = GetFeedbackByIdResult(
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
    isDraft = rs.getObject("is_draft") as kotlin.Boolean,
    isRead = rs.getObject("is_read") as kotlin.Boolean)
}

class GetFeedbackByIdQuery : Query<GetFeedbackByIdParams, GetFeedbackByIdResult> {
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
      |  feedbacks.is_draft,
      |  feedbacks.is_read
      |FROM
      |  feedbacks
      |  INNER JOIN employees AS emp_to ON feedbacks.feedback_to = emp_to.id AND emp_to.organisation_id = ?
      |  INNER JOIN employees AS emp_from ON feedbacks.feedback_from = emp_from.id AND emp_from.organisation_id = ?
      |  INNER JOIN feedback_types ON feedbacks.feedback_type_id = feedback_types.id
      |  JOIN employees_role_mapping ON employees_role_mapping.emp_id = emp_to.id
      |  JOIN roles ON roles.id = employees_role_mapping.role_id
      |WHERE
      |  feedbacks.sr_no = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetFeedbackByIdResult> = GetFeedbackByIdRowMapper()

  override val paramSetter: ParamSetter<GetFeedbackByIdParams> = GetFeedbackByIdParamSetter()
}
