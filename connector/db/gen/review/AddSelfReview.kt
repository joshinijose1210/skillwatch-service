package review

import java.sql.PreparedStatement
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddSelfReviewParams(
  val reviewDetailsId: Long?,
  val kpiId: Long?,
  val review: String?,
  val rating: Int?
)

class AddSelfReviewParamSetter : ParamSetter<AddSelfReviewParams> {
  override fun map(ps: PreparedStatement, params: AddSelfReviewParams) {
    ps.setObject(1, params.reviewDetailsId)
    ps.setObject(2, params.kpiId)
    ps.setObject(3, params.review)
    ps.setObject(4, params.rating)
  }
}

class AddSelfReviewCommand : Command<AddSelfReviewParams> {
  override val sql: String = """
      |INSERT INTO reviews(
      |  review_details_id, kpi_id, review, rating
      |)
      |VALUES(
      |   ?, ?, ?, ?
      |);
      """.trimMargin()

  override val paramSetter: ParamSetter<AddSelfReviewParams> = AddSelfReviewParamSetter()
}
