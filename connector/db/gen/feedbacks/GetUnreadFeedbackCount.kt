package feedbacks

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetUnreadFeedbackCountParams(
  val organisationId: Long?,
  val feedbackToId: Long?,
  val feedbackFromId: Array<Int>?,
  val feedbackTypeId: Array<Int>?,
  val reviewCycleId: Array<Int>?
)

class GetUnreadFeedbackCountParamSetter : ParamSetter<GetUnreadFeedbackCountParams> {
  override fun map(ps: PreparedStatement, params: GetUnreadFeedbackCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.feedbackToId)
    ps.setArray(3, ps.connection.createArrayOf("int4", params.feedbackFromId))
    ps.setArray(4, ps.connection.createArrayOf("int4", params.feedbackFromId))
    ps.setArray(5, ps.connection.createArrayOf("int4", params.feedbackTypeId))
    ps.setArray(6, ps.connection.createArrayOf("int4", params.feedbackTypeId))
    ps.setArray(7, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.reviewCycleId))
  }
}

data class GetUnreadFeedbackCountResult(
  val count: Long?
)

class GetUnreadFeedbackCountRowMapper : RowMapper<GetUnreadFeedbackCountResult> {
  override fun map(rs: ResultSet): GetUnreadFeedbackCountResult = GetUnreadFeedbackCountResult(
  count = rs.getObject("count") as kotlin.Long?)
}

class GetUnreadFeedbackCountQuery : Query<GetUnreadFeedbackCountParams,
    GetUnreadFeedbackCountResult> {
  override val sql: String = """
      |SELECT COUNT(*)
      |FROM feedbacks f
      |LEFT JOIN review_cycle ON DATE(f.updated_at) BETWEEN review_cycle.start_date AND review_cycle.end_date
      |  AND review_cycle.organisation_id = ?
      |WHERE f.feedback_to = ?
      |    AND (?::INT[] = '{-99}' OR f.feedback_from = ANY (?::INT[]))
      |    AND (?::INT[] = '{-99}' OR f.feedback_type_id = ANY(?::INT[]))
      |    AND (?::INT[] = '{-99}' OR review_cycle.id = ANY(?::INT[]))
      |    AND f.is_read = FALSE
      |    AND f.is_draft = FALSE ;
      """.trimMargin()

  override val mapper: RowMapper<GetUnreadFeedbackCountResult> = GetUnreadFeedbackCountRowMapper()

  override val paramSetter: ParamSetter<GetUnreadFeedbackCountParams> =
      GetUnreadFeedbackCountParamSetter()
}
