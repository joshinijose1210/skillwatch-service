package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetEmployeeManagerCountParams(
  val id: Array<Int>?,
  val firstManagerId: Array<Int>?,
  val secondManagerId: Array<Int>?,
  val organisationId: Long?
)

class GetEmployeeManagerCountParamSetter : ParamSetter<GetEmployeeManagerCountParams> {
  override fun map(ps: PreparedStatement, params: GetEmployeeManagerCountParams) {
    ps.setArray(1, ps.connection.createArrayOf("int4", params.id))
    ps.setArray(2, ps.connection.createArrayOf("int4", params.id))
    ps.setArray(3, ps.connection.createArrayOf("int4", params.firstManagerId))
    ps.setArray(4, ps.connection.createArrayOf("int4", params.firstManagerId))
    ps.setArray(5, ps.connection.createArrayOf("int4", params.firstManagerId))
    ps.setArray(6, ps.connection.createArrayOf("int4", params.secondManagerId))
    ps.setArray(7, ps.connection.createArrayOf("int4", params.secondManagerId))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.secondManagerId))
    ps.setObject(9, params.organisationId)
  }
}

data class GetEmployeeManagerCountResult(
  val employeeManagerCount: Long?
)

class GetEmployeeManagerCountRowMapper : RowMapper<GetEmployeeManagerCountResult> {
  override fun map(rs: ResultSet): GetEmployeeManagerCountResult = GetEmployeeManagerCountResult(
  employeeManagerCount = rs.getObject("employee_manager_count") as kotlin.Long?)
}

class GetEmployeeManagerCountQuery : Query<GetEmployeeManagerCountParams,
    GetEmployeeManagerCountResult> {
  override val sql: String = """
      |SELECT COUNT(employees.emp_id) AS employee_manager_count
      |FROM
      |  employees
      |  LEFT JOIN employee_manager_mapping_view AS firstManagerMappingData ON employees.id = firstManagerMappingData.emp_id
      |  AND firstManagerMappingData.type = 1
      |  LEFT JOIN employee_manager_mapping_view AS secondManagerMappingData ON employees.id = secondManagerMappingData.emp_id
      |  AND secondManagerMappingData.type = 2
      |WHERE
      |  (?::INT[] = '{-99}' OR employees.id = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}'
      |  OR (firstManagerMappingData.manager_id = ANY (?::INT[]) AND firstManagerMappingData.emp_id != ANY (?::INT[])))
      |  AND (?::INT[] = '{-99}'
      |  OR (secondManagerMappingData.manager_id = ANY (?::INT[]) AND secondManagerMappingData.emp_id != ANY (?::INT[])))
      |  AND employees.status = true
      |  AND employees.organisation_id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetEmployeeManagerCountResult> = GetEmployeeManagerCountRowMapper()

  override val paramSetter: ParamSetter<GetEmployeeManagerCountParams> =
      GetEmployeeManagerCountParamSetter()
}
