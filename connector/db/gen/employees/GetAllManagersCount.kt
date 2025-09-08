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

data class GetAllManagersCountParams(
  val organisationId: Long?,
  val moduleId: Array<Int>?
)

class GetAllManagersCountParamSetter : ParamSetter<GetAllManagersCountParams> {
  override fun map(ps: PreparedStatement, params: GetAllManagersCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.organisationId)
    ps.setObject(4, params.organisationId)
    ps.setArray(5, ps.connection.createArrayOf("int4", params.moduleId))
    ps.setObject(6, params.organisationId)
  }
}

data class GetAllManagersCountResult(
  val employeeCount: Long?
)

class GetAllManagersCountRowMapper : RowMapper<GetAllManagersCountResult> {
  override fun map(rs: ResultSet): GetAllManagersCountResult = GetAllManagersCountResult(
  employeeCount = rs.getObject("employee_count") as kotlin.Long?)
}

class GetAllManagersCountQuery : Query<GetAllManagersCountParams, GetAllManagersCountResult> {
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
      |  JOIN employees_role_mapping_view ON employees.id = employees_role_mapping_view.emp_id
      |  AND employees_role_mapping_view.organisation_id = ?
      |  JOIN employee_manager_mapping_view AS firstManagerData ON employees.id = firstManagerData.emp_id
      |  AND firstManagerData.type = 1
      |  LEFT JOIN employee_manager_mapping_view AS secondManagerData ON employees.id = secondManagerData.emp_id
      |  AND secondManagerData.type = 2
      |WHERE
      |  employees_role_mapping_view.id IN (SELECT DISTINCT(role_id) FROM module_permissions WHERE module_id = ANY(?::INT[]) AND edit=true)
      |  AND employees.organisation_id = ? ;
      """.trimMargin()

  override val mapper: RowMapper<GetAllManagersCountResult> = GetAllManagersCountRowMapper()

  override val paramSetter: ParamSetter<GetAllManagersCountParams> =
      GetAllManagersCountParamSetter()
}
