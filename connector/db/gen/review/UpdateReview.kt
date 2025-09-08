package review

import java.sql.PreparedStatement
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateReviewParams(
  val review: String?,
  val rating: Int?,
  val reviewId: Long?
)

class UpdateReviewParamSetter : ParamSetter<UpdateReviewParams> {
  override fun map(ps: PreparedStatement, params: UpdateReviewParams) {
    ps.setObject(1, params.review)
    ps.setObject(2, params.rating)
    ps.setObject(3, params.reviewId)
  }
}

class UpdateReviewCommand : Command<UpdateReviewParams> {
  override val sql: String = """
      |UPDATE
      |  reviews
      |SET
      |  review = ?,
      |  rating = ?
      |WHERE
      |  reviews.id = ?;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateReviewParams> = UpdateReviewParamSetter()
}
