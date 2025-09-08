package employees

import java.sql.PreparedStatement
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddEmployeeManagerMappingParams(
  val id: Long?,
  val managerId: Long?,
  val type: Int?
)

class AddEmployeeManagerMappingParamSetter : ParamSetter<AddEmployeeManagerMappingParams> {
  override fun map(ps: PreparedStatement, params: AddEmployeeManagerMappingParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.managerId)
    ps.setObject(3, params.type)
  }
}

class AddEmployeeManagerMappingCommand : Command<AddEmployeeManagerMappingParams> {
  override val sql: String = """
      |INSERT INTO employee_manager_mapping(emp_id, manager_id, type, is_active)
      |VALUES(
      |  ?,
      |  ?,
      |  ?,
      |  true
      |  ) ;
      """.trimMargin()

  override val paramSetter: ParamSetter<AddEmployeeManagerMappingParams> =
      AddEmployeeManagerMappingParamSetter()
}
