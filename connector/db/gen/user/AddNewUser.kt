package user

import java.sql.PreparedStatement
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddNewUserParams(
  val first_name: String?,
  val last_name: String?,
  val email_id: String?
)

class AddNewUserParamSetter : ParamSetter<AddNewUserParams> {
  override fun map(ps: PreparedStatement, params: AddNewUserParams) {
    ps.setObject(1, params.first_name)
    ps.setObject(2, params.last_name)
    ps.setObject(3, params.email_id)
  }
}

class AddNewUserCommand : Command<AddNewUserParams> {
  override val sql: String = """
      |INSERT INTO users(first_name, last_name, email_id)
      |VALUES
      |  (?, ?, ?) ;
      """.trimMargin()

  override val paramSetter: ParamSetter<AddNewUserParams> = AddNewUserParamSetter()
}
