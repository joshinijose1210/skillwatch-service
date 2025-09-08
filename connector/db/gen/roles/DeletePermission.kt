package roles

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class DeletePermissionParams(
  val roleId: Long?
)

class DeletePermissionParamSetter : ParamSetter<DeletePermissionParams> {
  override fun map(ps: PreparedStatement, params: DeletePermissionParams) {
    ps.setObject(1, params.roleId)
  }
}

class DeletePermissionCommand : Command<DeletePermissionParams> {
  override val sql: String = """
      |DELETE FROM
      |  module_permissions
      |WHERE
      |  role_id = ?;
      """.trimMargin()

  override val paramSetter: ParamSetter<DeletePermissionParams> = DeletePermissionParamSetter()
}
