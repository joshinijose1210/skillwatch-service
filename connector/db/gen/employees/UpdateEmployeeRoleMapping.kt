package employees

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateEmployeeRoleMappingParams(
  val id: Long?,
  val roleId: Long?
)

class UpdateEmployeeRoleMappingParamSetter : ParamSetter<UpdateEmployeeRoleMappingParams> {
  override fun map(ps: PreparedStatement, params: UpdateEmployeeRoleMappingParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.roleId)
    ps.setObject(3, params.id)
  }
}

class UpdateEmployeeRoleMappingCommand : Command<UpdateEmployeeRoleMappingParams> {
  override val sql: String = """
      |UPDATE
      |  employees_role_mapping
      |SET
      |  emp_id = ?,
      |  role_id = ?
      |WHERE
      |  emp_id = ?;
      """.trimMargin()

  override val paramSetter: ParamSetter<UpdateEmployeeRoleMappingParams> =
      UpdateEmployeeRoleMappingParamSetter()
}
