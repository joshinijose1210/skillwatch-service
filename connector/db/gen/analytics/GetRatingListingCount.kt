package analytics

import java.math.BigDecimal
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetRatingListingCountParams(
  val organisationId: Long?,
  val reviewCycleId: Long?,
  val minRange: BigDecimal?,
  val maxRange: BigDecimal?,
  val employeeId: Array<Int>?
)

class GetRatingListingCountParamSetter : ParamSetter<GetRatingListingCountParams> {
  override fun map(ps: PreparedStatement, params: GetRatingListingCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.reviewCycleId)
    ps.setObject(3, params.minRange)
    ps.setObject(4, params.maxRange)
    ps.setObject(5, params.minRange)
    ps.setObject(6, params.maxRange)
    ps.setArray(7, ps.connection.createArrayOf("int4", params.employeeId))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.employeeId))
  }
}

data class GetRatingListingCountResult(
  val ratingListingCount: Long?
)

class GetRatingListingCountRowMapper : RowMapper<GetRatingListingCountResult> {
  override fun map(rs: ResultSet): GetRatingListingCountResult = GetRatingListingCountResult(
  ratingListingCount = rs.getObject("rating_listing_count") as kotlin.Long?)
}

class GetRatingListingCountQuery : Query<GetRatingListingCountParams, GetRatingListingCountResult> {
  override val sql: String = """
      |SELECT COUNT(check_in_review.average_rating) AS rating_listing_count
      |FROM
      |  review_cycle
      |  INNER JOIN employees ON employees.organisation_id = review_cycle.organisation_id
      |  JOIN employees_history ON employees_history.employee_id = employees.id
      |    AND (DATE(employees_history.activated_at) <= review_cycle.end_date)
      |    AND (DATE(employees_history.deactivated_at) IS NULL OR DATE(employees_history.deactivated_at) >= review_cycle.start_date)
      |  LEFT JOIN review_details AS check_in_review ON check_in_review.review_cycle_id = review_cycle.id
      |  AND check_in_review.review_to = employees.id
      |  AND check_in_review.review_type_id = 3
      |  AND check_in_review.draft = false
      |  AND check_in_review.published = true
      |WHERE
      |  review_cycle.organisation_id = ?
      |  AND review_cycle.id = ?
      |  AND (check_in_review.average_rating BETWEEN ?::NUMERIC(10,2) AND ?::NUMERIC(10,2)
      |  OR (?::NUMERIC(10,2) IS NULL OR ?::NUMERIC(10,2) IS NULL))
      |  AND (?::INT[] = '{-99}' OR employees.id = ANY (?::INT[]));
      """.trimMargin()

  override val mapper: RowMapper<GetRatingListingCountResult> = GetRatingListingCountRowMapper()

  override val paramSetter: ParamSetter<GetRatingListingCountParams> =
      GetRatingListingCountParamSetter()
}
