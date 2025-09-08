package refresh_tokens

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class FindByRefreshTokenParams(
  val refresh_token: String?
)

class FindByRefreshTokenParamSetter : ParamSetter<FindByRefreshTokenParams> {
  override fun map(ps: PreparedStatement, params: FindByRefreshTokenParams) {
    ps.setObject(1, params.refresh_token)
  }
}

data class FindByRefreshTokenResult(
  val id: Long,
  val userId: Long,
  val refreshToken: String?,
  val revoked: Boolean?,
  val createdAt: Timestamp,
  val updatedAt: Timestamp?
)

class FindByRefreshTokenRowMapper : RowMapper<FindByRefreshTokenResult> {
  override fun map(rs: ResultSet): FindByRefreshTokenResult = FindByRefreshTokenResult(
  id = rs.getObject("id") as kotlin.Long,
    userId = rs.getObject("user_id") as kotlin.Long,
    refreshToken = rs.getObject("refresh_token") as kotlin.String?,
    revoked = rs.getObject("revoked") as kotlin.Boolean?,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp?)
}

class FindByRefreshTokenQuery : Query<FindByRefreshTokenParams, FindByRefreshTokenResult> {
  override val sql: String = """
      |SELECT
      |   id, user_id, refresh_token, revoked, created_at, updated_at
      |FROM
      |   refresh_tokens
      |WHERE
      |   refresh_token = ?;
      |""".trimMargin()

  override val mapper: RowMapper<FindByRefreshTokenResult> = FindByRefreshTokenRowMapper()

  override val paramSetter: ParamSetter<FindByRefreshTokenParams> = FindByRefreshTokenParamSetter()
}
