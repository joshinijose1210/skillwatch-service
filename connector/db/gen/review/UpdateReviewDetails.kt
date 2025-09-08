package review

import java.math.BigDecimal
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class UpdateReviewDetailsParams(
  val draft: Boolean?,
  val published: Boolean?,
  val averageRating: BigDecimal?,
  val reviewTypeId: Int?,
  val reviewDetailsId: Long?,
  val reviewCycleId: Long?,
  val reviewToId: Long?,
  val reviewFromId: Long?
)

class UpdateReviewDetailsParamSetter : ParamSetter<UpdateReviewDetailsParams> {
  override fun map(ps: PreparedStatement, params: UpdateReviewDetailsParams) {
    ps.setObject(1, params.draft)
    ps.setObject(2, params.published)
    ps.setObject(3, params.averageRating)
    ps.setObject(4, params.reviewTypeId)
    ps.setObject(5, params.reviewDetailsId)
    ps.setObject(6, params.reviewCycleId)
    ps.setObject(7, params.reviewToId)
    ps.setObject(8, params.reviewFromId)
  }
}

data class UpdateReviewDetailsResult(
  val id: Long,
  val reviewCycleId: Long?,
  val reviewTo: Long?,
  val reviewFrom: Long?,
  val updatedAt: Timestamp?,
  val draft: Boolean?,
  val published: Boolean?,
  val reviewTypeId: Int,
  val averageRating: BigDecimal?
)

class UpdateReviewDetailsRowMapper : RowMapper<UpdateReviewDetailsResult> {
  override fun map(rs: ResultSet): UpdateReviewDetailsResult = UpdateReviewDetailsResult(
  id = rs.getObject("id") as kotlin.Long,
    reviewCycleId = rs.getObject("review_cycle_id") as kotlin.Long?,
    reviewTo = rs.getObject("review_to") as kotlin.Long?,
    reviewFrom = rs.getObject("review_from") as kotlin.Long?,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp?,
    draft = rs.getObject("draft") as kotlin.Boolean?,
    published = rs.getObject("published") as kotlin.Boolean?,
    reviewTypeId = rs.getObject("review_type_id") as kotlin.Int,
    averageRating = rs.getObject("average_rating") as java.math.BigDecimal?)
}

class UpdateReviewDetailsQuery : Query<UpdateReviewDetailsParams, UpdateReviewDetailsResult> {
  override val sql: String = """
      |UPDATE
      |  review_details
      |SET
      |  updated_at = CURRENT_TIMESTAMP,
      |  draft = ?,
      |  published = ?,
      |  average_rating = ?
      |WHERE
      |  review_details.review_type_id = ?
      |  AND review_details.id = ?
      |  AND review_details.review_cycle_id = ?
      |  AND review_details.review_to = ?
      |  AND review_details.review_from = ?
      |  AND review_details.draft = true RETURNING *;
      |""".trimMargin()

  override val mapper: RowMapper<UpdateReviewDetailsResult> = UpdateReviewDetailsRowMapper()

  override val paramSetter: ParamSetter<UpdateReviewDetailsParams> =
      UpdateReviewDetailsParamSetter()
}
