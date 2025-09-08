package review

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class DeleteGoalsParams(
  val reviewDetailsId: Long?
)

class DeleteGoalsParamSetter : ParamSetter<DeleteGoalsParams> {
  override fun map(ps: PreparedStatement, params: DeleteGoalsParams) {
    ps.setObject(1, params.reviewDetailsId)
  }
}

class DeleteGoalsCommand : Command<DeleteGoalsParams> {
  override val sql: String = """
      |DELETE FROM
      |  goals
      |WHERE
      |  review_details_id = ?;
      |""".trimMargin()

  override val paramSetter: ParamSetter<DeleteGoalsParams> = DeleteGoalsParamSetter()
}
