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

data class GetAllEmployeesCountParams(
  val organisationId: Long?,
  val search: String?,
  val departmentId: Array<Int>?,
  val teamId: Array<Int>?,
  val designationId: Array<Int>?,
  val roleId: Array<Int>?
)

class GetAllEmployeesCountParamSetter : ParamSetter<GetAllEmployeesCountParams> {
  override fun map(ps: PreparedStatement, params: GetAllEmployeesCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.organisationId)
    ps.setObject(4, params.organisationId)
    ps.setObject(5, params.search)
    ps.setObject(6, params.search)
    ps.setObject(7, params.search)
    ps.setObject(8, params.search)
    ps.setObject(9, params.search)
    ps.setObject(10, params.search)
    ps.setArray(11, ps.connection.createArrayOf("int4", params.departmentId))
    ps.setArray(12, ps.connection.createArrayOf("int4", params.departmentId))
    ps.setArray(13, ps.connection.createArrayOf("int4", params.teamId))
    ps.setArray(14, ps.connection.createArrayOf("int4", params.teamId))
    ps.setArray(15, ps.connection.createArrayOf("int4", params.designationId))
    ps.setArray(16, ps.connection.createArrayOf("int4", params.designationId))
    ps.setArray(17, ps.connection.createArrayOf("int4", params.roleId))
    ps.setArray(18, ps.connection.createArrayOf("int4", params.roleId))
    ps.setObject(19, params.organisationId)
  }
}

data class GetAllEmployeesCountResult(
  val employeeCount: Long?
)

class GetAllEmployeesCountRowMapper : RowMapper<GetAllEmployeesCountResult> {
  override fun map(rs: ResultSet): GetAllEmployeesCountResult = GetAllEmployeesCountResult(
  employeeCount = rs.getObject("employee_count") as kotlin.Long?)
}

class GetAllEmployeesCountQuery : Query<GetAllEmployeesCountParams, GetAllEmployeesCountResult> {
  override val sql: String = """
      |SELECT
      |  COUNT(employees.emp_id) as employee_count
      |FROM
      |  employees
      |  LEFT JOIN employees_department_mapping_view ON employees.id = employees_department_mapping_view.emp_id
      |  AND employees_department_mapping_view.organisation_id = ?
      |  LEFT JOIN employees_team_mapping_view ON employees.id = employees_team_mapping_view.emp_id
      |  AND employees_team_mapping_view.organisation_id = ?
      |  LEFT JOIN employees_designation_mapping_view ON employees.id = employees_designation_mapping_view.emp_id
      |  AND employees_designation_mapping_view.organisation_id = ?
      |  LEFT JOIN employees_role_mapping_view ON employees.id = employees_role_mapping_view.emp_id
      |  AND employees_role_mapping_view.organisation_id = ?
      |  LEFT JOIN employee_manager_mapping_view AS firstManagerData ON employees.id = firstManagerData.emp_id
      |  AND firstManagerData.type = 1
      |  LEFT JOIN employee_manager_mapping_view AS secondManagerData ON employees.id = secondManagerData.emp_id
      |  AND secondManagerData.type = 2
      | WHERE
      |  ((cast(? as text) IS NULL)
      |       OR UPPER(employees.emp_id) LIKE UPPER(?)
      |       OR UPPER(employees.first_name) LIKE UPPER(?)
      |       OR UPPER(employees.last_name) LIKE UPPER(?)
      |       OR UPPER(employees.first_name) || ' ' || UPPER(employees.last_name) LIKE UPPER(?)
      |       OR UPPER(employees.email_id) LIKE UPPER(?)
      |  )
      |  AND (?::INT[] = '{-99}' OR employees_department_mapping_view.id = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}' OR employees_team_mapping_view.team_id = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}' OR employees_designation_mapping_view.id = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}' OR employees_role_mapping_view.id = ANY (?::INT[]))
      |  AND employees.organisation_id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetAllEmployeesCountResult> = GetAllEmployeesCountRowMapper()

  override val paramSetter: ParamSetter<GetAllEmployeesCountParams> =
      GetAllEmployeesCountParamSetter()
}
