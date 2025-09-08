package roles

import java.sql.PreparedStatement
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddPermissionParams(
  val roleId: Long?,
  val moduleId: Int?,
  val view: Boolean?,
  val edit: Boolean?
)

class AddPermissionParamSetter : ParamSetter<AddPermissionParams> {
  override fun map(ps: PreparedStatement, params: AddPermissionParams) {
    ps.setObject(1, params.roleId)
    ps.setObject(2, params.moduleId)
    ps.setObject(3, params.view)
    ps.setObject(4, params.edit)
  }
}

class AddPermissionCommand : Command<AddPermissionParams> {
  override val sql: String = """
      |INSERT INTO module_permissions (role_id, module_id, view, edit)
      |VALUES
      |  (?, ?, ?, ?);
      |""".trimMargin()

  override val paramSetter: ParamSetter<AddPermissionParams> = AddPermissionParamSetter()
}
