package review

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddReviewManagerMappingParams(
  val reviewDetailsId: Long?,
  val firstManagerId: Long?,
  val secondManagerId: Long?
)

class AddReviewManagerMappingParamSetter : ParamSetter<AddReviewManagerMappingParams> {
  override fun map(ps: PreparedStatement, params: AddReviewManagerMappingParams) {
    ps.setObject(1, params.reviewDetailsId)
    ps.setObject(2, params.firstManagerId)
    ps.setObject(3, params.secondManagerId)
  }
}

class AddReviewManagerMappingCommand : Command<AddReviewManagerMappingParams> {
  override val sql: String = """
      |INSERT INTO review_manager_mapping (
      |  review_details_id, first_manager_id, second_manager_id
      |) VALUES (
      |  ?, ?, ?
      |);
      """.trimMargin()

  override val paramSetter: ParamSetter<AddReviewManagerMappingParams> =
      AddReviewManagerMappingParamSetter()
}
