package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllManagersParams(
  val organisationId: Long?,
  val moduleId: Array<Int>?,
  val offset: Int?,
  val limit: Int?
)

class GetAllManagersParamSetter : ParamSetter<GetAllManagersParams> {
  override fun map(ps: PreparedStatement, params: GetAllManagersParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.organisationId)
    ps.setObject(3, params.organisationId)
    ps.setObject(4, params.organisationId)
    ps.setArray(5, ps.connection.createArrayOf("int4", params.moduleId))
    ps.setObject(6, params.organisationId)
    ps.setObject(7, params.offset)
    ps.setObject(8, params.limit)
  }
}

data class GetAllManagersResult(
  val organisationId: Long,
  val id: Long,
  val empId: String,
  val firstName: String,
  val lastName: String,
  val emailId: String,
  val contactNo: String,
  val status: Boolean,
  val departmentName: String?,
  val teamName: String?,
  val designationName: String?,
  val roleName: String?,
  val firstManagerId: Long?,
  val firstManagerEmployeeId: String?,
  val secondManagerId: Long?,
  val secondManagerEmployeeId: String?
)

class GetAllManagersRowMapper : RowMapper<GetAllManagersResult> {
  override fun map(rs: ResultSet): GetAllManagersResult = GetAllManagersResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    empId = rs.getObject("emp_id") as kotlin.String,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    emailId = rs.getObject("email_id") as kotlin.String,
    contactNo = rs.getObject("contact_no") as kotlin.String,
    status = rs.getObject("status") as kotlin.Boolean,
    departmentName = rs.getObject("department_name") as kotlin.String?,
    teamName = rs.getObject("team_name") as kotlin.String?,
    designationName = rs.getObject("designation_name") as kotlin.String?,
    roleName = rs.getObject("role_name") as kotlin.String?,
    firstManagerId = rs.getObject("first_manager_id") as kotlin.Long?,
    firstManagerEmployeeId = rs.getObject("first_manager_employee_id") as kotlin.String?,
    secondManagerId = rs.getObject("second_manager_id") as kotlin.Long?,
    secondManagerEmployeeId = rs.getObject("second_manager_employee_id") as kotlin.String?)
}

class GetAllManagersQuery : Query<GetAllManagersParams, GetAllManagersResult> {
  override val sql: String = """
      |SELECT
      |  employees.organisation_id,
      |  employees.id,
      |  employees.emp_id,
      |  employees.first_name,
      |  employees.last_name,
      |  employees.email_id,
      |  employees.contact_no,
      |  employees.status,
      |  employees_department_mapping_view.department_name,
      |  employees_team_mapping_view.team_name,
      |  employees_designation_mapping_view.designation_name,
      |  employees_role_mapping_view.role_name,
      |  firstManagerData.manager_id AS first_manager_id,
      |  firstManagerData.manager_employee_id AS first_manager_employee_id,
      |  COALESCE(secondManagerData.manager_id, null) AS second_manager_id,
      |  COALESCE(secondManagerData.manager_employee_id, null) AS second_manager_employee_id
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
      |  AND employees.organisation_id = ?
      |  ORDER BY employees.emp_id
      |OFFSET (?::INT)
      |LIMIT (?::INT) ;
      |""".trimMargin()

  override val mapper: RowMapper<GetAllManagersResult> = GetAllManagersRowMapper()

  override val paramSetter: ParamSetter<GetAllManagersParams> = GetAllManagersParamSetter()
}
