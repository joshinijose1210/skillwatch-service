package feedbacks

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddExternalEmailForFeedbackRequestParams(
  val emailId: String?,
  val organisationId: Long?
)

class AddExternalEmailForFeedbackRequestParamSetter :
    ParamSetter<AddExternalEmailForFeedbackRequestParams> {
  override fun map(ps: PreparedStatement, params: AddExternalEmailForFeedbackRequestParams) {
    ps.setObject(1, params.emailId)
    ps.setObject(2, params.organisationId)
  }
}

data class AddExternalEmailForFeedbackRequestResult(
  val id: Long
)

class AddExternalEmailForFeedbackRequestRowMapper :
    RowMapper<AddExternalEmailForFeedbackRequestResult> {
  override fun map(rs: ResultSet): AddExternalEmailForFeedbackRequestResult =
      AddExternalEmailForFeedbackRequestResult(
  id = rs.getObject("id") as kotlin.Long)
}

class AddExternalEmailForFeedbackRequestQuery : Query<AddExternalEmailForFeedbackRequestParams,
    AddExternalEmailForFeedbackRequestResult> {
  override val sql: String = """
      |INSERT INTO external_feedback_emails (email_id, organisation_id)
      |VALUES (?, ?)
      |ON CONFLICT (email_id, organisation_id) DO UPDATE SET email_id = EXCLUDED.email_id
      |RETURNING id;
      |""".trimMargin()

  override val mapper: RowMapper<AddExternalEmailForFeedbackRequestResult> =
      AddExternalEmailForFeedbackRequestRowMapper()

  override val paramSetter: ParamSetter<AddExternalEmailForFeedbackRequestParams> =
      AddExternalEmailForFeedbackRequestParamSetter()
}
