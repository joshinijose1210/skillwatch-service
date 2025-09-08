package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddEmployeeRoleMappingParams(
  val id: Long?,
  val role_id: Long?
)

class AddEmployeeRoleMappingParamSetter : ParamSetter<AddEmployeeRoleMappingParams> {
  override fun map(ps: PreparedStatement, params: AddEmployeeRoleMappingParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.role_id)
  }
}

data class AddEmployeeRoleMappingResult(
  val empId: Long,
  val roleId: Long
)

class AddEmployeeRoleMappingRowMapper : RowMapper<AddEmployeeRoleMappingResult> {
  override fun map(rs: ResultSet): AddEmployeeRoleMappingResult = AddEmployeeRoleMappingResult(
  empId = rs.getObject("emp_id") as kotlin.Long,
    roleId = rs.getObject("role_id") as kotlin.Long)
}

class AddEmployeeRoleMappingQuery : Query<AddEmployeeRoleMappingParams,
    AddEmployeeRoleMappingResult> {
  override val sql: String = """
      |INSERT INTO employees_role_mapping(emp_id,role_id)
      |VALUES (
      |  ?,
      |  ?
      |  ) RETURNING *;
      |""".trimMargin()

  override val mapper: RowMapper<AddEmployeeRoleMappingResult> = AddEmployeeRoleMappingRowMapper()

  override val paramSetter: ParamSetter<AddEmployeeRoleMappingParams> =
      AddEmployeeRoleMappingParamSetter()
}
