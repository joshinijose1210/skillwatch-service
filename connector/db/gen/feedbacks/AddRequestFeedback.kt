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

data class AddRequestFeedbackParams(
  val requestedBy: Long?,
  val feedbackToId: Long?,
  val feedbackFromId: Long?,
  val request: String?,
  val goalId: Long?
)

class AddRequestFeedbackParamSetter : ParamSetter<AddRequestFeedbackParams> {
  override fun map(ps: PreparedStatement, params: AddRequestFeedbackParams) {
    ps.setObject(1, params.requestedBy)
    ps.setObject(2, params.feedbackToId)
    ps.setObject(3, params.feedbackFromId)
    ps.setObject(4, params.request)
    ps.setObject(5, params.goalId)
  }
}

data class AddRequestFeedbackResult(
  val id: Long,
  val requestedBy: Long,
  val feedbackTo: Long,
  val feedbackFrom: Long?,
  val request: String?,
  val createdAt: Timestamp,
  val isSubmitted: Boolean?,
  val goalId: Long?,
  val isExternalRequest: Boolean,
  val feedbackFromExternalId: Long?
)

class AddRequestFeedbackRowMapper : RowMapper<AddRequestFeedbackResult> {
  override fun map(rs: ResultSet): AddRequestFeedbackResult = AddRequestFeedbackResult(
  id = rs.getObject("id") as kotlin.Long,
    requestedBy = rs.getObject("requested_by") as kotlin.Long,
    feedbackTo = rs.getObject("feedback_to") as kotlin.Long,
    feedbackFrom = rs.getObject("feedback_from") as kotlin.Long?,
    request = rs.getObject("request") as kotlin.String?,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    isSubmitted = rs.getObject("is_submitted") as kotlin.Boolean?,
    goalId = rs.getObject("goal_id") as kotlin.Long?,
    isExternalRequest = rs.getObject("is_external_request") as kotlin.Boolean,
    feedbackFromExternalId = rs.getObject("feedback_from_external_id") as kotlin.Long?)
}

class AddRequestFeedbackQuery : Query<AddRequestFeedbackParams, AddRequestFeedbackResult> {
  override val sql: String = """
      |INSERT INTO feedback_request( requested_by, feedback_to, feedback_from, request, is_submitted, goal_id)
      |VALUES(?, ?, ?, ?, false, ?) RETURNING *;
      |""".trimMargin()

  override val mapper: RowMapper<AddRequestFeedbackResult> = AddRequestFeedbackRowMapper()

  override val paramSetter: ParamSetter<AddRequestFeedbackParams> = AddRequestFeedbackParamSetter()
}
