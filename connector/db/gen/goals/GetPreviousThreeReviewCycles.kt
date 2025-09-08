package goals

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetPreviousThreeReviewCyclesParams(
  val organisationId: Long?,
  val currentCycleId: Long?
)

class GetPreviousThreeReviewCyclesParamSetter : ParamSetter<GetPreviousThreeReviewCyclesParams> {
  override fun map(ps: PreparedStatement, params: GetPreviousThreeReviewCyclesParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.currentCycleId)
  }
}

data class GetPreviousThreeReviewCyclesResult(
  val id: Long,
  val startDate: Date,
  val endDate: Date
)

class GetPreviousThreeReviewCyclesRowMapper : RowMapper<GetPreviousThreeReviewCyclesResult> {
  override fun map(rs: ResultSet): GetPreviousThreeReviewCyclesResult =
      GetPreviousThreeReviewCyclesResult(
  id = rs.getObject("id") as kotlin.Long,
    startDate = rs.getObject("start_date") as java.sql.Date,
    endDate = rs.getObject("end_date") as java.sql.Date)
}

class GetPreviousThreeReviewCyclesQuery : Query<GetPreviousThreeReviewCyclesParams,
    GetPreviousThreeReviewCyclesResult> {
  override val sql: String = """
      |SELECT id, start_date, end_date
      |FROM review_cycle
      |WHERE organisation_id = ?
      |  AND start_date < (
      |    SELECT start_date FROM review_cycle WHERE id = ?
      |  )
      |ORDER BY start_date DESC
      |LIMIT 3;
      """.trimMargin()

  override val mapper: RowMapper<GetPreviousThreeReviewCyclesResult> =
      GetPreviousThreeReviewCyclesRowMapper()

  override val paramSetter: ParamSetter<GetPreviousThreeReviewCyclesParams> =
      GetPreviousThreeReviewCyclesParamSetter()
}
