package feedbacks

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetFetchFeedbackByRequestIdParams(
  val request_id: Long?
)

class GetFetchFeedbackByRequestIdParamSetter : ParamSetter<GetFetchFeedbackByRequestIdParams> {
  override fun map(ps: PreparedStatement, params: GetFetchFeedbackByRequestIdParams) {
    ps.setObject(1, params.request_id)
  }
}

data class GetFetchFeedbackByRequestIdResult(
  val srNo: Long,
  val feedback: String,
  val feedbackTypeId: Int,
  val feedbackType: String,
  val isDraft: Boolean
)

class GetFetchFeedbackByRequestIdRowMapper : RowMapper<GetFetchFeedbackByRequestIdResult> {
  override fun map(rs: ResultSet): GetFetchFeedbackByRequestIdResult =
      GetFetchFeedbackByRequestIdResult(
  srNo = rs.getObject("sr_no") as kotlin.Long,
    feedback = rs.getObject("feedback") as kotlin.String,
    feedbackTypeId = rs.getObject("feedback_type_id") as kotlin.Int,
    feedbackType = rs.getObject("feedback_type") as kotlin.String,
    isDraft = rs.getObject("is_draft") as kotlin.Boolean)
}

class GetFetchFeedbackByRequestIdQuery : Query<GetFetchFeedbackByRequestIdParams,
    GetFetchFeedbackByRequestIdResult> {
  override val sql: String = """
      |SELECT
      |  feedbacks.sr_no,
      |  feedbacks.feedback,
      |  feedbacks.feedback_type_id,
      |  feedback_types.name AS feedback_type,
      |  feedbacks.is_draft
      |FROM
      |  feedbacks
      |  JOIN feedback_types ON feedbacks.feedback_type_id = feedback_types.id
      |WHERE
      |  feedbacks.request_id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetFetchFeedbackByRequestIdResult> =
      GetFetchFeedbackByRequestIdRowMapper()

  override val paramSetter: ParamSetter<GetFetchFeedbackByRequestIdParams> =
      GetFetchFeedbackByRequestIdParamSetter()
}
