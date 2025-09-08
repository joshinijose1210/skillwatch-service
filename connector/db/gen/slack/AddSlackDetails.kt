package slack

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddSlackDetailsParams(
  val organisationId: Long?,
  val accessToken: String?,
  val channelId: String?,
  val webhook_url: String?,
  val workspaceId: String?
)

class AddSlackDetailsParamSetter : ParamSetter<AddSlackDetailsParams> {
  override fun map(ps: PreparedStatement, params: AddSlackDetailsParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.accessToken)
    ps.setObject(3, params.channelId)
    ps.setObject(4, params.webhook_url)
    ps.setObject(5, params.workspaceId)
  }
}

class AddSlackDetailsCommand : Command<AddSlackDetailsParams> {
  override val sql: String = """
      |INSERT INTO slack_details(
      |  organisation_id, access_token, channel_id, webhook_url, workspace_id
      |)
      |VALUES (
      |    ?,
      |    ?,
      |    ?,
      |    ?,
      |    ?
      | ) ;
      |""".trimMargin()

  override val paramSetter: ParamSetter<AddSlackDetailsParams> = AddSlackDetailsParamSetter()
}
