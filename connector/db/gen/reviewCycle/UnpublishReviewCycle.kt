package reviewCycle

import java.sql.PreparedStatement
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UnpublishReviewCycleParams(
  val publish: Boolean?,
  val id: Long?
)

class UnpublishReviewCycleParamSetter : ParamSetter<UnpublishReviewCycleParams> {
  override fun map(ps: PreparedStatement, params: UnpublishReviewCycleParams) {
    ps.setObject(1, params.publish)
    ps.setObject(2, params.id)
  }
}

class UnpublishReviewCycleCommand : Command<UnpublishReviewCycleParams> {
  override val sql: String = """
      |UPDATE
      |    review_cycle
      |SET
      |    publish = ?
      |WHERE
      |    id = ? ;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UnpublishReviewCycleParams> =
      UnpublishReviewCycleParamSetter()
}
