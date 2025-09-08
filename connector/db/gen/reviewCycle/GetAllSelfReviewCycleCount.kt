package reviewCycle

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllSelfReviewCycleCountParams(
  val reviewTypeId: Array<Int>?,
  val reviewToId: Array<Int>?,
  val reviewFromId: Array<Int>?,
  val organisationId: Long?,
  val reviewCycleId: Array<Int>?
)

class GetAllSelfReviewCycleCountParamSetter : ParamSetter<GetAllSelfReviewCycleCountParams> {
  override fun map(ps: PreparedStatement, params: GetAllSelfReviewCycleCountParams) {
    ps.setArray(1, ps.connection.createArrayOf("int4", params.reviewTypeId))
    ps.setArray(2, ps.connection.createArrayOf("int4", params.reviewToId))
    ps.setArray(3, ps.connection.createArrayOf("int4", params.reviewFromId))
    ps.setArray(4, ps.connection.createArrayOf("int4", params.reviewToId))
    ps.setObject(5, params.organisationId)
    ps.setArray(6, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(7, ps.connection.createArrayOf("int4", params.reviewCycleId))
  }
}

data class GetAllSelfReviewCycleCountResult(
  val reviewCycleCount: Long?
)

class GetAllSelfReviewCycleCountRowMapper : RowMapper<GetAllSelfReviewCycleCountResult> {
  override fun map(rs: ResultSet): GetAllSelfReviewCycleCountResult =
      GetAllSelfReviewCycleCountResult(
  reviewCycleCount = rs.getObject("review_cycle_count") as kotlin.Long?)
}

class GetAllSelfReviewCycleCountQuery : Query<GetAllSelfReviewCycleCountParams,
    GetAllSelfReviewCycleCountResult> {
  override val sql: String = """
      |SELECT
      |  COUNT(review_cycle.start_date) as review_cycle_count
      |FROM
      |  review_cycle
      |  LEFT JOIN review_details ON review_cycle.id = review_details.review_cycle_id
      |  AND review_details.review_type_id = ANY (?::INT[])
      |  AND review_details.review_to = ANY (?::INT[])
      |  AND review_details.review_from = ANY (?::INT[])
      |  LEFT JOIN employees ON employees.id = ANY(?::INT[])
      |WHERE review_cycle.organisation_id = ?
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY (?::INT[]))
      |  AND employees.created_at::date <= review_cycle.end_date ;
      """.trimMargin()

  override val mapper: RowMapper<GetAllSelfReviewCycleCountResult> =
      GetAllSelfReviewCycleCountRowMapper()

  override val paramSetter: ParamSetter<GetAllSelfReviewCycleCountParams> =
      GetAllSelfReviewCycleCountParamSetter()
}
