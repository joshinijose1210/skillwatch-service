package login

import java.sql.PreparedStatement
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddPasswordParams(
  val password: String?,
  val email_id: String?
)

class AddPasswordParamSetter : ParamSetter<AddPasswordParams> {
  override fun map(ps: PreparedStatement, params: AddPasswordParams) {
    ps.setObject(1, params.password)
    ps.setObject(2, params.email_id)
  }
}

class AddPasswordCommand : Command<AddPasswordParams> {
  override val sql: String = """
      |UPDATE
      |  employees
      |SET
      |  password = ?
      |WHERE
      |  email_id = ?
      |  AND status = true;
      """.trimMargin()

  override val paramSetter: ParamSetter<AddPasswordParams> = AddPasswordParamSetter()
}
