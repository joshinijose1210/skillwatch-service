package roles

import java.sql.PreparedStatement
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdatePermissionParams(
  val view: Boolean?,
  val edit: Boolean?,
  val roleId: Long?,
  val moduleId: Int?
)

class UpdatePermissionParamSetter : ParamSetter<UpdatePermissionParams> {
  override fun map(ps: PreparedStatement, params: UpdatePermissionParams) {
    ps.setObject(1, params.view)
    ps.setObject(2, params.edit)
    ps.setObject(3, params.roleId)
    ps.setObject(4, params.moduleId)
  }
}

class UpdatePermissionCommand : Command<UpdatePermissionParams> {
  override val sql: String = """
      |UPDATE
      | module_permissions
      |SET
      | view = ?,
      | edit = ?
      |WHERE
      | role_id = ?
      | AND module_id = ? ;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdatePermissionParams> = UpdatePermissionParamSetter()
}
