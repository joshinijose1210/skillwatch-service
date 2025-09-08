package reviewCycle

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetPreviousReviewCycleIdParams(
  val organisationId: Long?
)

class GetPreviousReviewCycleIdParamSetter : ParamSetter<GetPreviousReviewCycleIdParams> {
  override fun map(ps: PreparedStatement, params: GetPreviousReviewCycleIdParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetPreviousReviewCycleIdResult(
  val id: Long
)

class GetPreviousReviewCycleIdRowMapper : RowMapper<GetPreviousReviewCycleIdResult> {
  override fun map(rs: ResultSet): GetPreviousReviewCycleIdResult = GetPreviousReviewCycleIdResult(
  id = rs.getObject("id") as kotlin.Long)
}

class GetPreviousReviewCycleIdQuery : Query<GetPreviousReviewCycleIdParams,
    GetPreviousReviewCycleIdResult> {
  override val sql: String = """
      |SELECT id
      |FROM review_cycle
      |WHERE organisation_id = ?
      |  AND end_date < CURRENT_DATE
      |ORDER BY end_date DESC
      |LIMIT 1;
      """.trimMargin()

  override val mapper: RowMapper<GetPreviousReviewCycleIdResult> =
      GetPreviousReviewCycleIdRowMapper()

  override val paramSetter: ParamSetter<GetPreviousReviewCycleIdParams> =
      GetPreviousReviewCycleIdParamSetter()
}
