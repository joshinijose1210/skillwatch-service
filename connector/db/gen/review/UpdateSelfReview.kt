package review

import java.sql.PreparedStatement
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateSelfReviewParams(
  val review: String?,
  val rating: Int?,
  val reviewDetailsId: Long?,
  val kpiId: Long?
)

class UpdateSelfReviewParamSetter : ParamSetter<UpdateSelfReviewParams> {
  override fun map(ps: PreparedStatement, params: UpdateSelfReviewParams) {
    ps.setObject(1, params.review)
    ps.setObject(2, params.rating)
    ps.setObject(3, params.reviewDetailsId)
    ps.setObject(4, params.kpiId)
  }
}

class UpdateSelfReviewCommand : Command<UpdateSelfReviewParams> {
  override val sql: String = """
      |UPDATE
      |  reviews
      |SET
      |  review = ?,
      |  rating = ?
      |WHERE
      |  reviews.review_details_id = ?
      |  AND reviews.kpi_id = ?;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateSelfReviewParams> = UpdateSelfReviewParamSetter()
}
