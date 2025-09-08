package employees

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class DeleteEmployeeManagerMappingParams(
  val id: Long?
)

class DeleteEmployeeManagerMappingParamSetter : ParamSetter<DeleteEmployeeManagerMappingParams> {
  override fun map(ps: PreparedStatement, params: DeleteEmployeeManagerMappingParams) {
    ps.setObject(1, params.id)
  }
}

class DeleteEmployeeManagerMappingCommand : Command<DeleteEmployeeManagerMappingParams> {
  override val sql: String = """
      |DELETE FROM
      |  employee_manager_mapping
      |WHERE
      |  emp_id = ?;
      """.trimMargin()

  override val paramSetter: ParamSetter<DeleteEmployeeManagerMappingParams> =
      DeleteEmployeeManagerMappingParamSetter()
}
