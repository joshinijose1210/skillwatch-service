package review

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateReviewManagerMappingParams(
  val firstManagerId: Long?,
  val secondManagerId: Long?,
  val reviewDetailsId: Long?
)

class UpdateReviewManagerMappingParamSetter : ParamSetter<UpdateReviewManagerMappingParams> {
  override fun map(ps: PreparedStatement, params: UpdateReviewManagerMappingParams) {
    ps.setObject(1, params.firstManagerId)
    ps.setObject(2, params.secondManagerId)
    ps.setObject(3, params.reviewDetailsId)
  }
}

class UpdateReviewManagerMappingCommand : Command<UpdateReviewManagerMappingParams> {
  override val sql: String = """
      |UPDATE
      |  review_manager_mapping
      |SET
      |  first_manager_id = ?,
      |  second_manager_id = ?
      |WHERE
      |  review_details_id = ?;
      """.trimMargin()

  override val paramSetter: ParamSetter<UpdateReviewManagerMappingParams> =
      UpdateReviewManagerMappingParamSetter()
}
