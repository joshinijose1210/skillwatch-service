package feedbacks

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateFeedbackRequestStatusParams(
  val requestId: Long?
)

class UpdateFeedbackRequestStatusParamSetter : ParamSetter<UpdateFeedbackRequestStatusParams> {
  override fun map(ps: PreparedStatement, params: UpdateFeedbackRequestStatusParams) {
    ps.setObject(1, params.requestId)
  }
}

class UpdateFeedbackRequestStatusCommand : Command<UpdateFeedbackRequestStatusParams> {
  override val sql: String = """
      |UPDATE feedback_request
      |SET
      |  is_submitted = true
      |WHERE
      |  feedback_request.id = ?;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateFeedbackRequestStatusParams> =
      UpdateFeedbackRequestStatusParamSetter()
}
