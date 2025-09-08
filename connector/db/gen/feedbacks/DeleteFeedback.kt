package feedbacks

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class DeleteFeedbackParams(
  val id: Long?
)

class DeleteFeedbackParamSetter : ParamSetter<DeleteFeedbackParams> {
  override fun map(ps: PreparedStatement, params: DeleteFeedbackParams) {
    ps.setObject(1, params.id)
  }
}

class DeleteFeedbackCommand : Command<DeleteFeedbackParams> {
  override val sql: String = """
      |DELETE FROM feedbacks
      |WHERE sr_no = ?
      |AND is_draft = TRUE;
      |""".trimMargin()

  override val paramSetter: ParamSetter<DeleteFeedbackParams> = DeleteFeedbackParamSetter()
}
