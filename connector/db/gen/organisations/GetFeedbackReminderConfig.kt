package organisations

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetFeedbackReminderConfigParams(
  val organisationId: Int?
)

class GetFeedbackReminderConfigParamSetter : ParamSetter<GetFeedbackReminderConfigParams> {
  override fun map(ps: PreparedStatement, params: GetFeedbackReminderConfigParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetFeedbackReminderConfigResult(
  val srNo: Int,
  val isBiweeklyFeedbackReminderEnabled: Boolean,
  val lastSentAt: Timestamp?,
  val lastReminderIndex: Int?
)

class GetFeedbackReminderConfigRowMapper : RowMapper<GetFeedbackReminderConfigResult> {
  override fun map(rs: ResultSet): GetFeedbackReminderConfigResult =
      GetFeedbackReminderConfigResult(
  srNo = rs.getObject("sr_no") as kotlin.Int,
    isBiweeklyFeedbackReminderEnabled = rs.getObject("is_biweekly_feedback_reminder_enabled") as
      kotlin.Boolean,
    lastSentAt = rs.getObject("last_sent_at") as java.sql.Timestamp?,
    lastReminderIndex = rs.getObject("last_reminder_index") as kotlin.Int?)
}

class GetFeedbackReminderConfigQuery : Query<GetFeedbackReminderConfigParams,
    GetFeedbackReminderConfigResult> {
  override val sql: String = """
      |SELECT
      |    organisations.sr_no,
      |    organisations.is_biweekly_feedback_reminder_enabled,
      |    COALESCE(frs.last_sent_at, NULL) AS last_sent_at,
      |    COALESCE(frs.last_reminder_index, NULL) AS last_reminder_index
      |FROM feedback_reminder_schedule frs
      |RIGHT JOIN organisations ON
      |    organisations.sr_no = frs.organisation_id
      |WHERE organisations.sr_no = ? ;
      |""".trimMargin()

  override val mapper: RowMapper<GetFeedbackReminderConfigResult> =
      GetFeedbackReminderConfigRowMapper()

  override val paramSetter: ParamSetter<GetFeedbackReminderConfigParams> =
      GetFeedbackReminderConfigParamSetter()
}
