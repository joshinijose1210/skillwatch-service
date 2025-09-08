package feedbacks

import java.sql.PreparedStatement
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class MarkFeedbackAsReadOrUnreadParams(
  val is_read: Boolean?,
  val id: Long?
)

class MarkFeedbackAsReadOrUnreadParamSetter : ParamSetter<MarkFeedbackAsReadOrUnreadParams> {
  override fun map(ps: PreparedStatement, params: MarkFeedbackAsReadOrUnreadParams) {
    ps.setObject(1, params.is_read)
    ps.setObject(2, params.id)
  }
}

class MarkFeedbackAsReadOrUnreadCommand : Command<MarkFeedbackAsReadOrUnreadParams> {
  override val sql: String = """
      |UPDATE feedbacks
      |SET is_read = ?
      |WHERE sr_no = ?;
      """.trimMargin()

  override val paramSetter: ParamSetter<MarkFeedbackAsReadOrUnreadParams> =
      MarkFeedbackAsReadOrUnreadParamSetter()
}
