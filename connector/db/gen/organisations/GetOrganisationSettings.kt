package organisations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetOrganisationSettingsParams(
  val id: Int?
)

class GetOrganisationSettingsParamSetter : ParamSetter<GetOrganisationSettingsParams> {
  override fun map(ps: PreparedStatement, params: GetOrganisationSettingsParams) {
    ps.setObject(1, params.id)
  }
}

data class GetOrganisationSettingsResult(
  val isManagerReviewMandatory: Boolean,
  val isAnonymousSuggestionAllowed: Boolean,
  val isBiweeklyFeedbackReminderEnabled: Boolean,
  val timeZone: String
)

class GetOrganisationSettingsRowMapper : RowMapper<GetOrganisationSettingsResult> {
  override fun map(rs: ResultSet): GetOrganisationSettingsResult = GetOrganisationSettingsResult(
  isManagerReviewMandatory = rs.getObject("is_manager_review_mandatory") as kotlin.Boolean,
    isAnonymousSuggestionAllowed = rs.getObject("is_anonymous_suggestion_allowed") as
      kotlin.Boolean,
    isBiweeklyFeedbackReminderEnabled = rs.getObject("is_biweekly_feedback_reminder_enabled") as
      kotlin.Boolean,
    timeZone = rs.getObject("time_zone") as kotlin.String)
}

class GetOrganisationSettingsQuery : Query<GetOrganisationSettingsParams,
    GetOrganisationSettingsResult> {
  override val sql: String = """
      |SELECT
      |    is_manager_review_mandatory,
      |    is_anonymous_suggestion_allowed,
      |    is_biweekly_feedback_reminder_enabled,
      |    time_zone
      |FROM organisations
      |WHERE sr_no = ? ;
      |""".trimMargin()

  override val mapper: RowMapper<GetOrganisationSettingsResult> = GetOrganisationSettingsRowMapper()

  override val paramSetter: ParamSetter<GetOrganisationSettingsParams> =
      GetOrganisationSettingsParamSetter()
}
