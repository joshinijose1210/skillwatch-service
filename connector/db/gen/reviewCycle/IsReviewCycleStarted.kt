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

data class IsReviewCycleStartedParams(
  val todayDate: Date?,
  val organisationId: Long?
)

class IsReviewCycleStartedParamSetter : ParamSetter<IsReviewCycleStartedParams> {
  override fun map(ps: PreparedStatement, params: IsReviewCycleStartedParams) {
    ps.setObject(1, params.todayDate)
    ps.setObject(2, params.organisationId)
  }
}

data class IsReviewCycleStartedResult(
  val exists: Boolean?,
  val id: Long?
)

class IsReviewCycleStartedRowMapper : RowMapper<IsReviewCycleStartedResult> {
  override fun map(rs: ResultSet): IsReviewCycleStartedResult = IsReviewCycleStartedResult(
  exists = rs.getObject("exists") as kotlin.Boolean?,
    id = rs.getObject("id") as kotlin.Long?)
}

class IsReviewCycleStartedQuery : Query<IsReviewCycleStartedParams, IsReviewCycleStartedResult> {
  override val sql: String = """
      |WITH subquery AS (
      |    SELECT id
      |    FROM review_cycle
      |    WHERE start_date = ?
      |    AND publish = true
      |    AND organisation_id = ?
      |)
      |SELECT EXISTS (SELECT 1 FROM subquery), (SELECT id FROM subquery);
      """.trimMargin()

  override val mapper: RowMapper<IsReviewCycleStartedResult> = IsReviewCycleStartedRowMapper()

  override val paramSetter: ParamSetter<IsReviewCycleStartedParams> =
      IsReviewCycleStartedParamSetter()
}
