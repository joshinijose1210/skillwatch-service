package feedbacks

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetEmployeeFeedbackCountsParams(
  val id: Long?,
  val startDate: Date?,
  val endDate: Date?
)

class GetEmployeeFeedbackCountsParamSetter : ParamSetter<GetEmployeeFeedbackCountsParams> {
  override fun map(ps: PreparedStatement, params: GetEmployeeFeedbackCountsParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.id)
    ps.setObject(3, params.id)
    ps.setObject(4, params.id)
    ps.setObject(5, params.id)
    ps.setObject(6, params.id)
    ps.setObject(7, params.startDate)
    ps.setObject(8, params.endDate)
    ps.setObject(9, params.id)
    ps.setObject(10, params.id)
  }
}

data class GetEmployeeFeedbackCountsResult(
  val submittedPositiveCount: Long?,
  val submittedImprovementCount: Long?,
  val submittedAppreciationCount: Long?,
  val receivedPositiveCount: Long?,
  val receivedImprovementCount: Long?,
  val receivedAppreciationCount: Long?
)

class GetEmployeeFeedbackCountsRowMapper : RowMapper<GetEmployeeFeedbackCountsResult> {
  override fun map(rs: ResultSet): GetEmployeeFeedbackCountsResult =
      GetEmployeeFeedbackCountsResult(
  submittedPositiveCount = rs.getObject("submitted_positive_count") as kotlin.Long?,
    submittedImprovementCount = rs.getObject("submitted_improvement_count") as kotlin.Long?,
    submittedAppreciationCount = rs.getObject("submitted_appreciation_count") as kotlin.Long?,
    receivedPositiveCount = rs.getObject("received_positive_count") as kotlin.Long?,
    receivedImprovementCount = rs.getObject("received_improvement_count") as kotlin.Long?,
    receivedAppreciationCount = rs.getObject("received_appreciation_count") as kotlin.Long?)
}

class GetEmployeeFeedbackCountsQuery : Query<GetEmployeeFeedbackCountsParams,
    GetEmployeeFeedbackCountsResult> {
  override val sql: String = """
      |SELECT
      |  COUNT(CASE WHEN feedback_type_id = 1 AND feedback_from = ? THEN 1 END) AS submitted_positive_count,
      |  COUNT(CASE WHEN feedback_type_id = 2 AND feedback_from = ? THEN 1 END) AS submitted_improvement_count,
      |  COUNT(CASE WHEN feedback_type_id = 3 AND feedback_from = ? THEN 1 END) AS submitted_appreciation_count,
      |  COUNT(CASE WHEN feedback_type_id = 1 AND feedback_to = ? THEN 1 END) AS received_positive_count,
      |  COUNT(CASE WHEN feedback_type_id = 2 AND feedback_to = ? THEN 1 END) AS received_improvement_count,
      |  COUNT(CASE WHEN feedback_type_id = 3 AND feedback_to = ? THEN 1 END) AS received_appreciation_count
      |FROM
      |  feedbacks
      |WHERE
      |  DATE(updated_at) >= ?
      |  AND DATE(updated_at) <= ?
      |  AND (feedbacks.is_draft IS NULL OR feedbacks.is_draft = false)
      |  AND (feedbacks.feedback_to = ? OR feedbacks.feedback_from = ?) ;
      |""".trimMargin()

  override val mapper: RowMapper<GetEmployeeFeedbackCountsResult> =
      GetEmployeeFeedbackCountsRowMapper()

  override val paramSetter: ParamSetter<GetEmployeeFeedbackCountsParams> =
      GetEmployeeFeedbackCountsParamSetter()
}
