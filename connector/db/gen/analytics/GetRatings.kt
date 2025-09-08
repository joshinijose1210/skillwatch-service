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

data class GetRatingsParams(
  val organisationId: Long?,
  val reviewCycleId: Long?,
  val minRange: BigDecimal?,
  val maxRange: BigDecimal?,
  val employeeId: Array<Int>?,
  val offset: Int?,
  val limit: Int?
)

class GetRatingsParamSetter : ParamSetter<GetRatingsParams> {
  override fun map(ps: PreparedStatement, params: GetRatingsParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.reviewCycleId)
    ps.setObject(3, params.minRange)
    ps.setObject(4, params.maxRange)
    ps.setObject(5, params.minRange)
    ps.setObject(6, params.maxRange)
    ps.setArray(7, ps.connection.createArrayOf("int4", params.employeeId))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.employeeId))
    ps.setObject(9, params.offset)
    ps.setObject(10, params.limit)
  }
}

data class GetRatingsResult(
  val reviewCycleId: Long,
  val employeeId: String,
  val id: Long,
  val firstName: String,
  val lastName: String,
  val checkInAverageRating: BigDecimal?
)

class GetRatingsRowMapper : RowMapper<GetRatingsResult> {
  override fun map(rs: ResultSet): GetRatingsResult = GetRatingsResult(
  reviewCycleId = rs.getObject("review_cycle_id") as kotlin.Long,
    employeeId = rs.getObject("employee_id") as kotlin.String,
    id = rs.getObject("id") as kotlin.Long,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    checkInAverageRating = rs.getObject("check_in_average_rating") as java.math.BigDecimal?)
}

class GetRatingsQuery : Query<GetRatingsParams, GetRatingsResult> {
  override val sql: String = """
      |SELECT
      |  review_cycle.id as review_cycle_id,
      |  employees.emp_id AS employee_id,
      |  employees.id AS id,
      |  employees.first_name,
      |  employees.last_name,
      |  check_in_review.average_rating AS check_in_average_rating
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
      |  AND (?::INT[] = '{-99}' OR employees.id = ANY (?::INT[]))
      |ORDER BY
      |  check_in_review.average_rating DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT) ;
      """.trimMargin()

  override val mapper: RowMapper<GetRatingsResult> = GetRatingsRowMapper()

  override val paramSetter: ParamSetter<GetRatingsParams> = GetRatingsParamSetter()
}
