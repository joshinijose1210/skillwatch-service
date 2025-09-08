package departments

import java.sql.PreparedStatement
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateDepartmentParams(
  val departmentName: String?,
  val departmentStatus: Boolean?,
  val organisationId: Long?,
  val id: Long?
)

class UpdateDepartmentParamSetter : ParamSetter<UpdateDepartmentParams> {
  override fun map(ps: PreparedStatement, params: UpdateDepartmentParams) {
    ps.setObject(1, params.departmentName)
    ps.setObject(2, params.departmentStatus)
    ps.setObject(3, params.organisationId)
    ps.setObject(4, params.id)
  }
}

class UpdateDepartmentCommand : Command<UpdateDepartmentParams> {
  override val sql: String = """
      |UPDATE
      |  departments
      |SET
      |  department_name = ?,
      |  status = ?,
      |  updated_at = CURRENT_TIMESTAMP
      |WHERE
      |  organisation_id = ?
      |  AND id = ? ;
      """.trimMargin()

  override val paramSetter: ParamSetter<UpdateDepartmentParams> = UpdateDepartmentParamSetter()
}
