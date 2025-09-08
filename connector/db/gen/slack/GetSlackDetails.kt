package slack

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetSlackDetailsParams(
  val organisationId: Long?
)

class GetSlackDetailsParamSetter : ParamSetter<GetSlackDetailsParams> {
  override fun map(ps: PreparedStatement, params: GetSlackDetailsParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetSlackDetailsResult(
  val id: Long,
  val accessToken: String,
  val channelId: String,
  val webhookUrl: String
)

class GetSlackDetailsRowMapper : RowMapper<GetSlackDetailsResult> {
  override fun map(rs: ResultSet): GetSlackDetailsResult = GetSlackDetailsResult(
  id = rs.getObject("id") as kotlin.Long,
    accessToken = rs.getObject("access_token") as kotlin.String,
    channelId = rs.getObject("channel_id") as kotlin.String,
    webhookUrl = rs.getObject("webhook_url") as kotlin.String)
}

class GetSlackDetailsQuery : Query<GetSlackDetailsParams, GetSlackDetailsResult> {
  override val sql: String = """
      |SELECT
      |   id,
      |   access_token,
      |   channel_id,
      |   webhook_url
      |FROM
      |   slack_details
      |WHERE
      |   organisation_id = ? ;
      |""".trimMargin()

  override val mapper: RowMapper<GetSlackDetailsResult> = GetSlackDetailsRowMapper()

  override val paramSetter: ParamSetter<GetSlackDetailsParams> = GetSlackDetailsParamSetter()
}
