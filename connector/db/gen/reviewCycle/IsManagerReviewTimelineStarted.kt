package reviewCycle

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsManagerReviewTimelineStartedParams(
  val todayDate: Date?,
  val organisationId: Long?
)

class IsManagerReviewTimelineStartedParamSetter : ParamSetter<IsManagerReviewTimelineStartedParams>
    {
  override fun map(ps: PreparedStatement, params: IsManagerReviewTimelineStartedParams) {
    ps.setObject(1, params.todayDate)
    ps.setObject(2, params.organisationId)
  }
}

data class IsManagerReviewTimelineStartedResult(
  val exists: Boolean?,
  val id: Long?
)

class IsManagerReviewTimelineStartedRowMapper : RowMapper<IsManagerReviewTimelineStartedResult> {
  override fun map(rs: ResultSet): IsManagerReviewTimelineStartedResult =
      IsManagerReviewTimelineStartedResult(
  exists = rs.getObject("exists") as kotlin.Boolean?,
    id = rs.getObject("id") as kotlin.Long?)
}

class IsManagerReviewTimelineStartedQuery : Query<IsManagerReviewTimelineStartedParams,
    IsManagerReviewTimelineStartedResult> {
  override val sql: String = """
      |WITH subquery AS (
      |    SELECT id
      |    FROM review_cycle
      |    WHERE manager_review_start_date = ?
      |    AND publish = true
      |    AND organisation_id = ?
      |)
      |SELECT EXISTS (SELECT 1 FROM subquery), (SELECT id FROM subquery);
      """.trimMargin()

  override val mapper: RowMapper<IsManagerReviewTimelineStartedResult> =
      IsManagerReviewTimelineStartedRowMapper()

  override val paramSetter: ParamSetter<IsManagerReviewTimelineStartedParams> =
      IsManagerReviewTimelineStartedParamSetter()
}
