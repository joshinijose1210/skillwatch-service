package refresh_tokens

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class RevokeRefreshTokenParams(
  val userId: Long?,
  val refreshToken: String?
)

class RevokeRefreshTokenParamSetter : ParamSetter<RevokeRefreshTokenParams> {
  override fun map(ps: PreparedStatement, params: RevokeRefreshTokenParams) {
    ps.setObject(1, params.userId)
    ps.setObject(2, params.refreshToken)
  }
}

class RevokeRefreshTokenCommand : Command<RevokeRefreshTokenParams> {
  override val sql: String = """
      |UPDATE refresh_tokens
      |SET revoked = true, updated_at = now()
      |WHERE user_id = ?
      |AND refresh_token = ?;
      """.trimMargin()

  override val paramSetter: ParamSetter<RevokeRefreshTokenParams> = RevokeRefreshTokenParamSetter()
}
