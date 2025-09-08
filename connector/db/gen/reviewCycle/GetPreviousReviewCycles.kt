package reviewCycle

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetPreviousReviewCyclesParams(
  val currentCycleId: Long?,
  val organisationId: Long?,
  val numberOfCycles: Long?
)

class GetPreviousReviewCyclesParamSetter : ParamSetter<GetPreviousReviewCyclesParams> {
  override fun map(ps: PreparedStatement, params: GetPreviousReviewCyclesParams) {
    ps.setObject(1, params.currentCycleId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.numberOfCycles)
  }
}

data class GetPreviousReviewCyclesResult(
  val id: Long,
  val startDate: Date,
  val endDate: Date
)

class GetPreviousReviewCyclesRowMapper : RowMapper<GetPreviousReviewCyclesResult> {
  override fun map(rs: ResultSet): GetPreviousReviewCyclesResult = GetPreviousReviewCyclesResult(
  id = rs.getObject("id") as kotlin.Long,
    startDate = rs.getObject("start_date") as java.sql.Date,
    endDate = rs.getObject("end_date") as java.sql.Date)
}

class GetPreviousReviewCyclesQuery : Query<GetPreviousReviewCyclesParams,
    GetPreviousReviewCyclesResult> {
  override val sql: String = """
      |SELECT
      |    rc.id,
      |    rc.start_date,
      |    rc.end_date
      |FROM
      |    review_cycle rc
      |JOIN
      |    review_cycle current_cycle
      |    ON current_cycle.id = ?
      |WHERE
      |    rc.organisation_id = ?
      |    AND rc.start_date < current_cycle.start_date
      |ORDER BY
      |    rc.start_date DESC
      |LIMIT
      |    ?;
      """.trimMargin()

  override val mapper: RowMapper<GetPreviousReviewCyclesResult> = GetPreviousReviewCyclesRowMapper()

  override val paramSetter: ParamSetter<GetPreviousReviewCyclesParams> =
      GetPreviousReviewCyclesParamSetter()
}
