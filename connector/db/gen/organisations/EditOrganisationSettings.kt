package organisations

import java.sql.PreparedStatement
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import norm.Command
import norm.ParamSetter

data class EditOrganisationSettingsParams(
  val managerReviewMandatory: Boolean?,
  val anonymousSuggestionAllowed: Boolean?,
  val isBiWeeklyFeedbackReminderEnabled: Boolean?,
  val id: Int?
)

class EditOrganisationSettingsParamSetter : ParamSetter<EditOrganisationSettingsParams> {
  override fun map(ps: PreparedStatement, params: EditOrganisationSettingsParams) {
    ps.setObject(1, params.managerReviewMandatory)
    ps.setObject(2, params.anonymousSuggestionAllowed)
    ps.setObject(3, params.isBiWeeklyFeedbackReminderEnabled)
    ps.setObject(4, params.id)
  }
}

class EditOrganisationSettingsCommand : Command<EditOrganisationSettingsParams> {
  override val sql: String = """
      |UPDATE organisations
      |SET
      |  is_manager_review_mandatory = ?,
      |  is_anonymous_suggestion_allowed = ?,
      |  is_biweekly_feedback_reminder_enabled = ?
      |WHERE sr_no = ?;
      |""".trimMargin()

  override val paramSetter: ParamSetter<EditOrganisationSettingsParams> =
      EditOrganisationSettingsParamSetter()
}
