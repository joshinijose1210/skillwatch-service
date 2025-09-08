package reviewCycle

import java.math.BigDecimal
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllSelfReviewCycleParams(
  val reviewTypeId: Array<Int>?,
  val reviewToId: Array<Int>?,
  val reviewFromId: Array<Int>?,
  val organisationId: Long?,
  val reviewCycleId: Array<Int>?,
  val offset: Int?,
  val limit: Int?
)

class GetAllSelfReviewCycleParamSetter : ParamSetter<GetAllSelfReviewCycleParams> {
  override fun map(ps: PreparedStatement, params: GetAllSelfReviewCycleParams) {
    ps.setArray(1, ps.connection.createArrayOf("int4", params.reviewTypeId))
    ps.setArray(2, ps.connection.createArrayOf("int4", params.reviewToId))
    ps.setArray(3, ps.connection.createArrayOf("int4", params.reviewFromId))
    ps.setArray(4, ps.connection.createArrayOf("int4", params.reviewToId))
    ps.setObject(5, params.organisationId)
    ps.setArray(6, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setArray(7, ps.connection.createArrayOf("int4", params.reviewCycleId))
    ps.setObject(8, params.offset)
    ps.setObject(9, params.limit)
  }
}

data class GetAllSelfReviewCycleResult(
  val id: Long,
  val startDate: Date,
  val endDate: Date,
  val publish: Boolean,
  val selfReviewStartDate: Date,
  val selfReviewEndDate: Date,
  val draft: Boolean?,
  val published: Boolean?,
  val updatedAt: Timestamp?,
  val averageRating: BigDecimal?
)

class GetAllSelfReviewCycleRowMapper : RowMapper<GetAllSelfReviewCycleResult> {
  override fun map(rs: ResultSet): GetAllSelfReviewCycleResult = GetAllSelfReviewCycleResult(
  id = rs.getObject("id") as kotlin.Long,
    startDate = rs.getObject("start_date") as java.sql.Date,
    endDate = rs.getObject("end_date") as java.sql.Date,
    publish = rs.getObject("publish") as kotlin.Boolean,
    selfReviewStartDate = rs.getObject("self_review_start_date") as java.sql.Date,
    selfReviewEndDate = rs.getObject("self_review_end_date") as java.sql.Date,
    draft = rs.getObject("draft") as kotlin.Boolean?,
    published = rs.getObject("published") as kotlin.Boolean?,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp?,
    averageRating = rs.getObject("average_rating") as java.math.BigDecimal?)
}

class GetAllSelfReviewCycleQuery : Query<GetAllSelfReviewCycleParams, GetAllSelfReviewCycleResult> {
  override val sql: String = """
      |SELECT
      |  review_cycle.id,
      |  review_cycle.start_date,
      |  review_cycle.end_date,
      |  review_cycle.publish,
      |  review_cycle.self_review_start_date,
      |  review_cycle.self_review_end_date,
      |  review_details.draft,
      |  review_details.published,
      |  review_details.updated_at,
      |  review_details.average_rating
      |FROM
      |  review_cycle
      |  LEFT JOIN review_details ON review_cycle.id = review_details.review_cycle_id
      |  AND review_details.review_type_id = ANY (?::INT[])
      |  AND review_details.review_to = ANY (?::INT[])
      |  AND review_details.review_from = ANY (?::INT[])
      |  LEFT JOIN employees ON employees.id = ANY(?::INT[])
      |WHERE
      |  review_cycle.organisation_id = ?
      |  AND (?::INT[] = '{-99}' OR review_cycle.id = ANY (?::INT[]))
      |  AND employees.created_at::date <= review_cycle.end_date
      |ORDER BY
      |  review_cycle.publish DESC,
      |  daterange(start_date, end_date) DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      """.trimMargin()

  override val mapper: RowMapper<GetAllSelfReviewCycleResult> = GetAllSelfReviewCycleRowMapper()

  override val paramSetter: ParamSetter<GetAllSelfReviewCycleParams> =
      GetAllSelfReviewCycleParamSetter()
}
