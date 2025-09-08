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

data class AddExternalFeedbackParams(
  val feedback: String?,
  val feedbackToId: Long?,
  val feedbackFromId: Long?,
  val feedbackTypeId: Int?,
  val requestId: Long?
)

class AddExternalFeedbackParamSetter : ParamSetter<AddExternalFeedbackParams> {
  override fun map(ps: PreparedStatement, params: AddExternalFeedbackParams) {
    ps.setObject(1, params.feedback)
    ps.setObject(2, params.feedbackToId)
    ps.setObject(3, params.feedbackFromId)
    ps.setObject(4, params.feedbackTypeId)
    ps.setObject(5, params.requestId)
  }
}

data class AddExternalFeedbackResult(
  val srNo: Long,
  val createdAt: Timestamp,
  val feedback: String,
  val feedbackTo: Long,
  val feedbackFrom: Long?,
  val feedbackTypeId: Int,
  val requestId: Long?,
  val isDraft: Boolean,
  val updatedAt: Timestamp,
  val feedbackFromExternalId: Long?
)

class AddExternalFeedbackRowMapper : RowMapper<AddExternalFeedbackResult> {
  override fun map(rs: ResultSet): AddExternalFeedbackResult = AddExternalFeedbackResult(
  srNo = rs.getObject("sr_no") as kotlin.Long,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    feedback = rs.getObject("feedback") as kotlin.String,
    feedbackTo = rs.getObject("feedback_to") as kotlin.Long,
    feedbackFrom = rs.getObject("feedback_from") as kotlin.Long?,
    feedbackTypeId = rs.getObject("feedback_type_id") as kotlin.Int,
    requestId = rs.getObject("request_id") as kotlin.Long?,
    isDraft = rs.getObject("is_draft") as kotlin.Boolean,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp,
    feedbackFromExternalId = rs.getObject("feedback_from_external_id") as kotlin.Long?)
}

class AddExternalFeedbackQuery : Query<AddExternalFeedbackParams, AddExternalFeedbackResult> {
  override val sql: String = """
      |INSERT INTO feedbacks(
      |  feedback,
      |  feedback_to,
      |  feedback_from_external_id,
      |  feedback_type_id,
      |  request_id,
      |  is_draft,
      |  updated_at
      |)
      |VALUES
      |  (
      |   ?,
      |   ?,
      |   ?,
      |   ?,
      |   ?,
      |   false,
      |   CURRENT_TIMESTAMP
      |  ) RETURNING *;
      |""".trimMargin()

  override val mapper: RowMapper<AddExternalFeedbackResult> = AddExternalFeedbackRowMapper()

  override val paramSetter: ParamSetter<AddExternalFeedbackParams> =
      AddExternalFeedbackParamSetter()
}
