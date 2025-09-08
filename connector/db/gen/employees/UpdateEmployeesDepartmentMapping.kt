package employees

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateEmployeesDepartmentMappingParams(
  val id: Long?,
  val departmentId: Long?
)

class UpdateEmployeesDepartmentMappingParamSetter :
    ParamSetter<UpdateEmployeesDepartmentMappingParams> {
  override fun map(ps: PreparedStatement, params: UpdateEmployeesDepartmentMappingParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.departmentId)
    ps.setObject(3, params.id)
  }
}

class UpdateEmployeesDepartmentMappingCommand : Command<UpdateEmployeesDepartmentMappingParams> {
  override val sql: String = """
      |UPDATE
      |  employees_department_mapping
      |SET
      |  emp_id = ?,
      |  department_id = ?
      |WHERE
      |  emp_id = ?;
      """.trimMargin()

  override val paramSetter: ParamSetter<UpdateEmployeesDepartmentMappingParams> =
      UpdateEmployeesDepartmentMappingParamSetter()
}
