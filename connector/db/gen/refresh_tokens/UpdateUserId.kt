package refresh_tokens

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateUserIdParams(
  val user_id: Long?
)

class UpdateUserIdParamSetter : ParamSetter<UpdateUserIdParams> {
  override fun map(ps: PreparedStatement, params: UpdateUserIdParams) {
    ps.setObject(1, params.user_id)
    ps.setObject(2, params.user_id)
  }
}

class UpdateUserIdCommand : Command<UpdateUserIdParams> {
  override val sql: String = """
      |UPDATE refresh_tokens
      |SET user_id = ?
      |WHERE user_id = ?
      """.trimMargin()

  override val paramSetter: ParamSetter<UpdateUserIdParams> = UpdateUserIdParamSetter()
}
