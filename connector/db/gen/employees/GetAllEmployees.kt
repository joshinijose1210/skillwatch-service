package employees

import java.sql.Date
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

data class GetAllEmployeesParams(
  val organisationId: Long?,
  val search: String?,
  val departmentId: Array<Int>?,
  val teamId: Array<Int>?,
  val designationId: Array<Int>?,
  val roleId: Array<Int>?,
  val sortOrder: String?,
  val offset: Int?,
  val limit: Int?
)

class GetAllEmployeesParamSetter : ParamSetter<GetAllEmployeesParams> {
  override fun map(ps: PreparedStatement, params: GetAllEmployeesParams) {
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
    ps.setObject(20, params.sortOrder)
    ps.setObject(21, params.sortOrder)
    ps.setObject(22, params.offset)
    ps.setObject(23, params.limit)
  }
}

data class GetAllEmployeesResult(
  val organisationId: Long,
  val id: Long,
  val empId: String,
  val firstName: String,
  val lastName: String,
  val emailId: String,
  val contactNo: String,
  val genderId: Int?,
  val dateOfJoining: Date?,
  val dateOfBirth: Date?,
  val experience: Int?,
  val status: Boolean,
  val isConsultant: Boolean,
  val departmentName: String?,
  val teamName: String?,
  val designationName: String?,
  val roleName: String?,
  val firstManagerId: Long?,
  val firstManagerEmployeeId: String?,
  val firstManagerFirstName: String?,
  val firstManagerLastName: String?,
  val secondManagerId: Long?,
  val secondManagerEmployeeId: String?,
  val secondManagerFirstName: String?,
  val secondManagerLastName: String?
)

class GetAllEmployeesRowMapper : RowMapper<GetAllEmployeesResult> {
  override fun map(rs: ResultSet): GetAllEmployeesResult = GetAllEmployeesResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    empId = rs.getObject("emp_id") as kotlin.String,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    emailId = rs.getObject("email_id") as kotlin.String,
    contactNo = rs.getObject("contact_no") as kotlin.String,
    genderId = rs.getObject("gender_id") as kotlin.Int?,
    dateOfJoining = rs.getObject("date_of_joining") as java.sql.Date?,
    dateOfBirth = rs.getObject("date_of_birth") as java.sql.Date?,
    experience = rs.getObject("experience") as kotlin.Int?,
    status = rs.getObject("status") as kotlin.Boolean,
    isConsultant = rs.getObject("is_consultant") as kotlin.Boolean,
    departmentName = rs.getObject("department_name") as kotlin.String?,
    teamName = rs.getObject("team_name") as kotlin.String?,
    designationName = rs.getObject("designation_name") as kotlin.String?,
    roleName = rs.getObject("role_name") as kotlin.String?,
    firstManagerId = rs.getObject("first_manager_id") as kotlin.Long?,
    firstManagerEmployeeId = rs.getObject("first_manager_employee_id") as kotlin.String?,
    firstManagerFirstName = rs.getObject("first_manager_first_name") as kotlin.String?,
    firstManagerLastName = rs.getObject("first_manager_last_name") as kotlin.String?,
    secondManagerId = rs.getObject("second_manager_id") as kotlin.Long?,
    secondManagerEmployeeId = rs.getObject("second_manager_employee_id") as kotlin.String?,
    secondManagerFirstName = rs.getObject("second_manager_first_name") as kotlin.String?,
    secondManagerLastName = rs.getObject("second_manager_last_name") as kotlin.String?)
}

class GetAllEmployeesQuery : Query<GetAllEmployeesParams, GetAllEmployeesResult> {
  override val sql: String = """
      |SELECT
      |  employees.organisation_id,
      |  employees.id,
      |  employees.emp_id,
      |  employees.first_name,
      |  employees.last_name,
      |  employees.email_id,
      |  employees.contact_no,
      |  employees.gender_id,
      |  employees.date_of_joining,
      |  employees.date_of_birth,
      |  employees.experience,
      |  employees.status,
      |  employees.is_consultant,
      |  employees_department_mapping_view.department_name,
      |  employees_team_mapping_view.team_name,
      |  employees_designation_mapping_view.designation_name,
      |  employees_role_mapping_view.role_name,
      |  firstManagerData.manager_id AS first_manager_id,
      |  firstManagerData.manager_employee_id AS first_manager_employee_id,
      |  firstManagerData.first_name AS first_manager_first_name,
      |  firstManagerData.last_name AS first_manager_last_name,
      |  COALESCE(secondManagerData.manager_id, null) AS second_manager_id,
      |  COALESCE(secondManagerData.manager_employee_id, null) AS second_manager_employee_id,
      |  COALESCE(secondManagerData.first_name, null) AS second_manager_first_name,
      |  COALESCE(secondManagerData.last_name, null) AS second_manager_last_name
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
      |  AND employees.organisation_id = ?
      |  ORDER BY CASE WHEN ? = 'ASC' THEN employees.emp_id END ASC,
      |           CASE WHEN ? = 'DESC' THEN employees.emp_id END DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      |""".trimMargin()

  override val mapper: RowMapper<GetAllEmployeesResult> = GetAllEmployeesRowMapper()

  override val paramSetter: ParamSetter<GetAllEmployeesParams> = GetAllEmployeesParamSetter()
}
