package employees

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetEmployeeDataByUniqueIdParams(
  val id: Long?
)

class GetEmployeeDataByUniqueIdParamSetter : ParamSetter<GetEmployeeDataByUniqueIdParams> {
  override fun map(ps: PreparedStatement, params: GetEmployeeDataByUniqueIdParams) {
    ps.setObject(1, params.id)
  }
}

data class GetEmployeeDataByUniqueIdResult(
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
  val departmentId: Long?,
  val teamId: Long?,
  val designationId: Long,
  val roleId: Long,
  val firstManagerId: Long?,
  val secondManagerId: Long?
)

class GetEmployeeDataByUniqueIdRowMapper : RowMapper<GetEmployeeDataByUniqueIdResult> {
  override fun map(rs: ResultSet): GetEmployeeDataByUniqueIdResult =
      GetEmployeeDataByUniqueIdResult(
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
    departmentId = rs.getObject("department_id") as kotlin.Long?,
    teamId = rs.getObject("team_id") as kotlin.Long?,
    designationId = rs.getObject("designation_id") as kotlin.Long,
    roleId = rs.getObject("role_id") as kotlin.Long,
    firstManagerId = rs.getObject("first_manager_id") as kotlin.Long?,
    secondManagerId = rs.getObject("second_manager_id") as kotlin.Long?)
}

class GetEmployeeDataByUniqueIdQuery : Query<GetEmployeeDataByUniqueIdParams,
    GetEmployeeDataByUniqueIdResult> {
  override val sql: String = """
      |SELECT
      |    employees.organisation_id,
      |    employees.id,
      |    employees.emp_id,
      |    employees.first_name,
      |    employees.last_name,
      |    employees.email_id,
      |    employees.contact_no,
      |    employees.gender_id,
      |    employees.date_of_joining,
      |    employees.date_of_birth,
      |    employees.experience,
      |    employees.status,
      |    employees.is_consultant,
      |    COALESCE(employees_department_mapping.department_id, null) AS department_id,
      |    employees_team_mapping_view.team_id,
      |    employees_designation_mapping.designation_id,
      |    employees_role_mapping.role_id,
      |    firstManager.manager_id AS first_manager_id,
      |    COALESCE(secondManager.manager_id, null) AS second_manager_id
      |FROM
      |  employees
      |  LEFT JOIN employees_department_mapping ON employees.id = employees_department_mapping.emp_id
      |  LEFT JOIN employees_team_mapping_view ON employees.id = employees_team_mapping_view.emp_id
      |  LEFT JOIN employees_designation_mapping ON employees.id = employees_designation_mapping.emp_id
      |  LEFT JOIN employees_role_mapping ON employees.id = employees_role_mapping.emp_id
      |  LEFT JOIN employee_manager_mapping_view AS firstManager ON employees.id = firstManager.emp_id
      |  AND firstManager.type = 1
      |  LEFT JOIN employee_manager_mapping_view AS secondManager ON employees.id = secondManager.emp_id
      |  AND secondManager.type = 2
      |WHERE
      |  employees.id = ? ;
      """.trimMargin()

  override val mapper: RowMapper<GetEmployeeDataByUniqueIdResult> =
      GetEmployeeDataByUniqueIdRowMapper()

  override val paramSetter: ParamSetter<GetEmployeeDataByUniqueIdParams> =
      GetEmployeeDataByUniqueIdParamSetter()
}
