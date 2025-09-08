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

data class IsCheckInTimelineStartedParams(
  val todayDate: Date?,
  val organisationId: Long?
)

class IsCheckInTimelineStartedParamSetter : ParamSetter<IsCheckInTimelineStartedParams> {
  override fun map(ps: PreparedStatement, params: IsCheckInTimelineStartedParams) {
    ps.setObject(1, params.todayDate)
    ps.setObject(2, params.organisationId)
  }
}

data class IsCheckInTimelineStartedResult(
  val exists: Boolean?,
  val id: Long?
)

class IsCheckInTimelineStartedRowMapper : RowMapper<IsCheckInTimelineStartedResult> {
  override fun map(rs: ResultSet): IsCheckInTimelineStartedResult = IsCheckInTimelineStartedResult(
  exists = rs.getObject("exists") as kotlin.Boolean?,
    id = rs.getObject("id") as kotlin.Long?)
}

class IsCheckInTimelineStartedQuery : Query<IsCheckInTimelineStartedParams,
    IsCheckInTimelineStartedResult> {
  override val sql: String = """
      |WITH subquery AS (
      |    SELECT id
      |    FROM review_cycle
      |    WHERE check_in_start_date = ?
      |    AND publish = true
      |    AND organisation_id = ?
      |)
      |SELECT EXISTS (SELECT 1 FROM subquery), (SELECT id FROM subquery);
      """.trimMargin()

  override val mapper: RowMapper<IsCheckInTimelineStartedResult> =
      IsCheckInTimelineStartedRowMapper()

  override val paramSetter: ParamSetter<IsCheckInTimelineStartedParams> =
      IsCheckInTimelineStartedParamSetter()
}
