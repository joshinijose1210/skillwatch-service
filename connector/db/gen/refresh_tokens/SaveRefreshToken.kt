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

data class SaveRefreshTokenParams(
  val user_id: Long?,
  val refresh_token: String?,
  val revoked: Boolean?
)

class SaveRefreshTokenParamSetter : ParamSetter<SaveRefreshTokenParams> {
  override fun map(ps: PreparedStatement, params: SaveRefreshTokenParams) {
    ps.setObject(1, params.user_id)
    ps.setObject(2, params.refresh_token)
    ps.setObject(3, params.revoked)
  }
}

data class SaveRefreshTokenResult(
  val id: Long,
  val userId: Long,
  val refreshToken: String,
  val revoked: Boolean,
  val createdAt: Timestamp,
  val updatedAt: Timestamp?
)

class SaveRefreshTokenRowMapper : RowMapper<SaveRefreshTokenResult> {
  override fun map(rs: ResultSet): SaveRefreshTokenResult = SaveRefreshTokenResult(
  id = rs.getObject("id") as kotlin.Long,
    userId = rs.getObject("user_id") as kotlin.Long,
    refreshToken = rs.getObject("refresh_token") as kotlin.String,
    revoked = rs.getObject("revoked") as kotlin.Boolean,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp?)
}

class SaveRefreshTokenQuery : Query<SaveRefreshTokenParams, SaveRefreshTokenResult> {
  override val sql: String = """
      |INSERT INTO refresh_tokens (user_id, refresh_token, revoked)
      |VALUES (?, ?, ?) RETURNING *
      """.trimMargin()

  override val mapper: RowMapper<SaveRefreshTokenResult> = SaveRefreshTokenRowMapper()

  override val paramSetter: ParamSetter<SaveRefreshTokenParams> = SaveRefreshTokenParamSetter()
}
