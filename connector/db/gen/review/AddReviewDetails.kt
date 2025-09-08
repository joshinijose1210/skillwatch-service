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

data class AddReviewDetailsParams(
  val reviewCycleId: Long?,
  val reviewToId: Long?,
  val reviewFromId: Long?,
  val draft: Boolean?,
  val published: Boolean?,
  val reviewTypeId: Int?,
  val averageRating: BigDecimal?
)

class AddReviewDetailsParamSetter : ParamSetter<AddReviewDetailsParams> {
  override fun map(ps: PreparedStatement, params: AddReviewDetailsParams) {
    ps.setObject(1, params.reviewCycleId)
    ps.setObject(2, params.reviewToId)
    ps.setObject(3, params.reviewFromId)
    ps.setObject(4, params.draft)
    ps.setObject(5, params.published)
    ps.setObject(6, params.reviewTypeId)
    ps.setObject(7, params.averageRating)
  }
}

data class AddReviewDetailsResult(
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

class AddReviewDetailsRowMapper : RowMapper<AddReviewDetailsResult> {
  override fun map(rs: ResultSet): AddReviewDetailsResult = AddReviewDetailsResult(
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

class AddReviewDetailsQuery : Query<AddReviewDetailsParams, AddReviewDetailsResult> {
  override val sql: String = """
      |INSERT INTO review_details(
      |    review_cycle_id, review_to, review_from, draft, published, review_type_id, average_rating
      |)
      |VALUES(
      |    ?, ?, ?, ?, ?, ?, ?
      |) RETURNING * ;
      |""".trimMargin()

  override val mapper: RowMapper<AddReviewDetailsResult> = AddReviewDetailsRowMapper()

  override val paramSetter: ParamSetter<AddReviewDetailsParams> = AddReviewDetailsParamSetter()
}
