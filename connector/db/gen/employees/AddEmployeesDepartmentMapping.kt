package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddEmployeesDepartmentMappingParams(
  val id: Long?,
  val departmentId: Long?
)

class AddEmployeesDepartmentMappingParamSetter : ParamSetter<AddEmployeesDepartmentMappingParams> {
  override fun map(ps: PreparedStatement, params: AddEmployeesDepartmentMappingParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.departmentId)
  }
}

data class AddEmployeesDepartmentMappingResult(
  val empId: Long,
  val departmentId: Long
)

class AddEmployeesDepartmentMappingRowMapper : RowMapper<AddEmployeesDepartmentMappingResult> {
  override fun map(rs: ResultSet): AddEmployeesDepartmentMappingResult =
      AddEmployeesDepartmentMappingResult(
  empId = rs.getObject("emp_id") as kotlin.Long,
    departmentId = rs.getObject("department_id") as kotlin.Long)
}

class AddEmployeesDepartmentMappingQuery : Query<AddEmployeesDepartmentMappingParams,
    AddEmployeesDepartmentMappingResult> {
  override val sql: String = """
      |INSERT INTO employees_department_mapping(emp_id,department_id)
      |VALUES ( ?, ?) RETURNING *;
      """.trimMargin()

  override val mapper: RowMapper<AddEmployeesDepartmentMappingResult> =
      AddEmployeesDepartmentMappingRowMapper()

  override val paramSetter: ParamSetter<AddEmployeesDepartmentMappingParams> =
      AddEmployeesDepartmentMappingParamSetter()
}
