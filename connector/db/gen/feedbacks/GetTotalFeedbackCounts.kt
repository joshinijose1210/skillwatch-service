package feedbacks

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetTotalFeedbackCountsParams(
  val startDate: Date?,
  val endDate: Date?,
  val organisationId: Long?
)

class GetTotalFeedbackCountsParamSetter : ParamSetter<GetTotalFeedbackCountsParams> {
  override fun map(ps: PreparedStatement, params: GetTotalFeedbackCountsParams) {
    ps.setObject(1, params.startDate)
    ps.setObject(2, params.endDate)
    ps.setObject(3, params.organisationId)
  }
}

data class GetTotalFeedbackCountsResult(
  val positiveCount: Long?,
  val improvementCount: Long?,
  val appreciationCount: Long?
)

class GetTotalFeedbackCountsRowMapper : RowMapper<GetTotalFeedbackCountsResult> {
  override fun map(rs: ResultSet): GetTotalFeedbackCountsResult = GetTotalFeedbackCountsResult(
  positiveCount = rs.getObject("positive_count") as kotlin.Long?,
    improvementCount = rs.getObject("improvement_count") as kotlin.Long?,
    appreciationCount = rs.getObject("appreciation_count") as kotlin.Long?)
}

class GetTotalFeedbackCountsQuery : Query<GetTotalFeedbackCountsParams,
    GetTotalFeedbackCountsResult> {
  override val sql: String = """
      |SELECT
      |  COUNT(CASE WHEN feedback_type_id = 1 AND feedback_to = employees.id THEN 1 END) AS positive_count,
      |  COUNT(CASE WHEN feedback_type_id = 2 AND feedback_to = employees.id THEN 1 END) AS improvement_count,
      |  COUNT(CASE WHEN feedback_type_id = 3 AND feedback_to = employees.id THEN 1 END) AS appreciation_count
      |FROM
      |  feedbacks
      |  LEFT JOIN employees
      |  ON employees.id = feedbacks.feedback_to
      |WHERE
      |  DATE(feedbacks.updated_at) >= ?
      |  AND DATE(feedbacks.updated_at) <= ?
      |  AND (feedbacks.is_draft IS NULL OR feedbacks.is_draft = false)
      |  AND employees.organisation_id = ?;
      |""".trimMargin()

  override val mapper: RowMapper<GetTotalFeedbackCountsResult> = GetTotalFeedbackCountsRowMapper()

  override val paramSetter: ParamSetter<GetTotalFeedbackCountsParams> =
      GetTotalFeedbackCountsParamSetter()
}
