package employees

import java.sql.PreparedStatement
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateEmployeeManagerMappingParams(
  val id: Long?,
  val managerType: Int?
)

class UpdateEmployeeManagerMappingParamSetter : ParamSetter<UpdateEmployeeManagerMappingParams> {
  override fun map(ps: PreparedStatement, params: UpdateEmployeeManagerMappingParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.managerType)
  }
}

class UpdateEmployeeManagerMappingCommand : Command<UpdateEmployeeManagerMappingParams> {
  override val sql: String = """
      |UPDATE
      |  employee_manager_mapping
      |SET
      |  updated_at = now(),
      |  is_active = false
      |WHERE
      |  emp_id = ?
      |  AND type = ?
      |  AND is_active = true;
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateEmployeeManagerMappingParams> =
      UpdateEmployeeManagerMappingParamSetter()
}
