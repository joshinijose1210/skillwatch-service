package employees

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateEmployeeDesignationMappingParams(
  val id: Long?,
  val designationId: Long?
)

class UpdateEmployeeDesignationMappingParamSetter :
    ParamSetter<UpdateEmployeeDesignationMappingParams> {
  override fun map(ps: PreparedStatement, params: UpdateEmployeeDesignationMappingParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.designationId)
    ps.setObject(3, params.id)
  }
}

class UpdateEmployeeDesignationMappingCommand : Command<UpdateEmployeeDesignationMappingParams> {
  override val sql: String = """
      |UPDATE
      |  employees_designation_mapping
      |SET
      |  emp_id = ?,
      |  designation_id = ?
      |WHERE
      |  emp_id = ?;
      """.trimMargin()

  override val paramSetter: ParamSetter<UpdateEmployeeDesignationMappingParams> =
      UpdateEmployeeDesignationMappingParamSetter()
}
