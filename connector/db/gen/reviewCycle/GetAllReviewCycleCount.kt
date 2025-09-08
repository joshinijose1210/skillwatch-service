package reviewCycle

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllReviewCycleCountParams(
  val organisationId: Long?
)

class GetAllReviewCycleCountParamSetter : ParamSetter<GetAllReviewCycleCountParams> {
  override fun map(ps: PreparedStatement, params: GetAllReviewCycleCountParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetAllReviewCycleCountResult(
  val reviewCycleCount: Long?
)

class GetAllReviewCycleCountRowMapper : RowMapper<GetAllReviewCycleCountResult> {
  override fun map(rs: ResultSet): GetAllReviewCycleCountResult = GetAllReviewCycleCountResult(
  reviewCycleCount = rs.getObject("review_cycle_count") as kotlin.Long?)
}

class GetAllReviewCycleCountQuery : Query<GetAllReviewCycleCountParams,
    GetAllReviewCycleCountResult> {
  override val sql: String = """
      |SELECT COUNT(review_cycle.id)
      |as review_cycle_count
      |FROM review_cycle
      |WHERE
      |review_cycle.organisation_id = ? ;
      """.trimMargin()

  override val mapper: RowMapper<GetAllReviewCycleCountResult> = GetAllReviewCycleCountRowMapper()

  override val paramSetter: ParamSetter<GetAllReviewCycleCountParams> =
      GetAllReviewCycleCountParamSetter()
}
