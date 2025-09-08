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

data class EditFeedbackParams(
  val feedback: String?,
  val feedbackToId: Long?,
  val feedbackTypeId: Int?,
  val requestId: Long?,
  val idDraft: Boolean?,
  val id: Long?
)

class EditFeedbackParamSetter : ParamSetter<EditFeedbackParams> {
  override fun map(ps: PreparedStatement, params: EditFeedbackParams) {
    ps.setObject(1, params.feedback)
    ps.setObject(2, params.feedbackToId)
    ps.setObject(3, params.feedbackTypeId)
    ps.setObject(4, params.requestId)
    ps.setObject(5, params.idDraft)
    ps.setObject(6, params.id)
  }
}

data class EditFeedbackResult(
  val srNo: Long,
  val createdAt: Timestamp,
  val feedback: String,
  val feedbackTo: Long,
  val feedbackFrom: Long,
  val feedbackTypeId: Int,
  val requestId: Long?,
  val isDraft: Boolean,
  val updatedAt: Timestamp,
  val feedbackFromExternalId: Long?
)

class EditFeedbackRowMapper : RowMapper<EditFeedbackResult> {
  override fun map(rs: ResultSet): EditFeedbackResult = EditFeedbackResult(
  srNo = rs.getObject("sr_no") as kotlin.Long,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    feedback = rs.getObject("feedback") as kotlin.String,
    feedbackTo = rs.getObject("feedback_to") as kotlin.Long,
    feedbackFrom = rs.getObject("feedback_from") as kotlin.Long,
    feedbackTypeId = rs.getObject("feedback_type_id") as kotlin.Int,
    requestId = rs.getObject("request_id") as kotlin.Long?,
    isDraft = rs.getObject("is_draft") as kotlin.Boolean,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp,
    feedbackFromExternalId = rs.getObject("feedback_from_external_id") as kotlin.Long?)
}

class EditFeedbackQuery : Query<EditFeedbackParams, EditFeedbackResult> {
  override val sql: String = """
      |UPDATE
      |  feedbacks
      |SET
      |  feedback = ?,
      |  feedback_to = ?,
      |  feedback_type_id = ?,
      |  request_id = ?,
      |  is_draft = ?,
      |  updated_at = CURRENT_TIMESTAMP
      |WHERE
      |  sr_no = ?
      |  AND is_draft = true
      |  RETURNING *;
      """.trimMargin()

  override val mapper: RowMapper<EditFeedbackResult> = EditFeedbackRowMapper()

  override val paramSetter: ParamSetter<EditFeedbackParams> = EditFeedbackParamSetter()
}
