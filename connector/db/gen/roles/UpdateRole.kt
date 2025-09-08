package roles

import java.sql.PreparedStatement
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateRoleParams(
  val roleName: String?,
  val status: Boolean?,
  val id: Long?,
  val organisationId: Long?
)

class UpdateRoleParamSetter : ParamSetter<UpdateRoleParams> {
  override fun map(ps: PreparedStatement, params: UpdateRoleParams) {
    ps.setObject(1, params.roleName)
    ps.setObject(2, params.status)
    ps.setObject(3, params.id)
    ps.setObject(4, params.organisationId)
  }
}

class UpdateRoleCommand : Command<UpdateRoleParams> {
  override val sql: String = """
      |UPDATE
      |  roles
      |SET
      |  role_name = ?,
      |  status = ?,
      |  updated_at = CURRENT_TIMESTAMP
      |WHERE
      |  id = ?
      |  AND organisation_id = ? ;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateRoleParams> = UpdateRoleParamSetter()
}
