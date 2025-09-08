package slack

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAccessTokenParams(
  val workspaceId: String?
)

class GetAccessTokenParamSetter : ParamSetter<GetAccessTokenParams> {
  override fun map(ps: PreparedStatement, params: GetAccessTokenParams) {
    ps.setObject(1, params.workspaceId)
  }
}

data class GetAccessTokenResult(
  val accessToken: String
)

class GetAccessTokenRowMapper : RowMapper<GetAccessTokenResult> {
  override fun map(rs: ResultSet): GetAccessTokenResult = GetAccessTokenResult(
  accessToken = rs.getObject("access_token") as kotlin.String)
}

class GetAccessTokenQuery : Query<GetAccessTokenParams, GetAccessTokenResult> {
  override val sql: String = """
      |SELECT
      |   access_token
      |FROM
      |   slack_details
      |WHERE
      |   workspace_id = ? ;
      """.trimMargin()

  override val mapper: RowMapper<GetAccessTokenResult> = GetAccessTokenRowMapper()

  override val paramSetter: ParamSetter<GetAccessTokenParams> = GetAccessTokenParamSetter()
}
