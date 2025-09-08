package review

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

data class GetReviewDetailsParams(
  val reviewTypeId: Array<Int>?,
  val reviewCycleId: Long?,
  val reviewToId: Long?,
  val reviewFromId: Array<Int>?
)

class GetReviewDetailsParamSetter : ParamSetter<GetReviewDetailsParams> {
  override fun map(ps: PreparedStatement, params: GetReviewDetailsParams) {
    ps.setArray(1, ps.connection.createArrayOf("int4", params.reviewTypeId))
    ps.setObject(2, params.reviewCycleId)
    ps.setObject(3, params.reviewToId)
    ps.setArray(4, ps.connection.createArrayOf("int4", params.reviewFromId))
  }
}

data class GetReviewDetailsResult(
  val reviewDetailsId: Long,
  val reviewCycleId: Long?,
  val reviewToId: Long?,
  val reviewToEmployeeId: String,
  val reviewFromId: Long?,
  val reviewFromEmployeeId: String,
  val draft: Boolean?,
  val published: Boolean?,
  val reviewTypeId: Int,
  val updatedAt: Timestamp?
)

class GetReviewDetailsRowMapper : RowMapper<GetReviewDetailsResult> {
  override fun map(rs: ResultSet): GetReviewDetailsResult = GetReviewDetailsResult(
  reviewDetailsId = rs.getObject("review_details_id") as kotlin.Long,
    reviewCycleId = rs.getObject("review_cycle_id") as kotlin.Long?,
    reviewToId = rs.getObject("review_to_id") as kotlin.Long?,
    reviewToEmployeeId = rs.getObject("review_to_employee_id") as kotlin.String,
    reviewFromId = rs.getObject("review_from_id") as kotlin.Long?,
    reviewFromEmployeeId = rs.getObject("review_from_employee_id") as kotlin.String,
    draft = rs.getObject("draft") as kotlin.Boolean?,
    published = rs.getObject("published") as kotlin.Boolean?,
    reviewTypeId = rs.getObject("review_type_id") as kotlin.Int,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp?)
}

class GetReviewDetailsQuery : Query<GetReviewDetailsParams, GetReviewDetailsResult> {
  override val sql: String = """
      |SELECT
      |  review_details.id AS review_details_id,
      |  review_details.review_cycle_id,
      |  review_details.review_to AS review_to_id,
      |  reviewToEmployee.emp_id AS review_to_employee_id,
      |  review_details.review_from AS review_from_id,
      |  reviewFromEmployee.emp_id AS review_from_employee_id,
      |  review_details.draft,
      |  review_details.published,
      |  review_details.review_type_id,
      |  review_details.updated_at
      |FROM
      |  review_details
      |  JOIN employees AS reviewToEmployee ON reviewToEmployee.id = review_details.review_to
      |  JOIN employees AS reviewFromEmployee ON reviewFromEmployee.id = review_details.review_from
      |WHERE
      |  review_details.review_type_id = ANY (?::INT[])
      |  AND review_details.review_cycle_id = ?
      |  AND review_details.review_to = ?
      |  AND review_details.review_from = ANY (?::INT[]);
      """.trimMargin()

  override val mapper: RowMapper<GetReviewDetailsResult> = GetReviewDetailsRowMapper()

  override val paramSetter: ParamSetter<GetReviewDetailsParams> = GetReviewDetailsParamSetter()
}
