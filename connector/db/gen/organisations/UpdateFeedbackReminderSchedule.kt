package organisations

import java.sql.PreparedStatement
import java.sql.Timestamp
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateFeedbackReminderScheduleParams(
  val organisationId: Long?,
  val lastSentAt: Timestamp?,
  val lastReminderIndex: Int?
)

class UpdateFeedbackReminderScheduleParamSetter : ParamSetter<UpdateFeedbackReminderScheduleParams>
    {
  override fun map(ps: PreparedStatement, params: UpdateFeedbackReminderScheduleParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.lastSentAt)
    ps.setObject(3, params.lastReminderIndex)
  }
}

class UpdateFeedbackReminderScheduleCommand : Command<UpdateFeedbackReminderScheduleParams> {
  override val sql: String = """
      |INSERT INTO feedback_reminder_schedule (
      |    organisation_id,
      |    last_sent_at,
      |    last_reminder_index
      |) VALUES (
      |    ?,
      |    ?,
      |    ?
      |)
      |ON CONFLICT (organisation_id) DO UPDATE
      |SET
      |    last_sent_at = EXCLUDED.last_sent_at,
      |    last_reminder_index = EXCLUDED.last_reminder_index;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateFeedbackReminderScheduleParams> =
      UpdateFeedbackReminderScheduleParamSetter()
}
