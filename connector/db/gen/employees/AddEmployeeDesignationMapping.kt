package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddEmployeeDesignationMappingParams(
  val id: Long?,
  val designation_id: Long?
)

class AddEmployeeDesignationMappingParamSetter : ParamSetter<AddEmployeeDesignationMappingParams> {
  override fun map(ps: PreparedStatement, params: AddEmployeeDesignationMappingParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.designation_id)
  }
}

data class AddEmployeeDesignationMappingResult(
  val empId: Long,
  val designationId: Long
)

class AddEmployeeDesignationMappingRowMapper : RowMapper<AddEmployeeDesignationMappingResult> {
  override fun map(rs: ResultSet): AddEmployeeDesignationMappingResult =
      AddEmployeeDesignationMappingResult(
  empId = rs.getObject("emp_id") as kotlin.Long,
    designationId = rs.getObject("designation_id") as kotlin.Long)
}

class AddEmployeeDesignationMappingQuery : Query<AddEmployeeDesignationMappingParams,
    AddEmployeeDesignationMappingResult> {
  override val sql: String = """
      |INSERT INTO employees_designation_mapping(emp_id,designation_id)
      |VALUES (
      |  ?,
      |  ?
      |  ) RETURNING *;
      |""".trimMargin()

  override val mapper: RowMapper<AddEmployeeDesignationMappingResult> =
      AddEmployeeDesignationMappingRowMapper()

  override val paramSetter: ParamSetter<AddEmployeeDesignationMappingParams> =
      AddEmployeeDesignationMappingParamSetter()
}
