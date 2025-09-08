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

data class GetEmployeeByIdParams(
  val review_cycle_id: Long?,
  val id: Long?
)

class GetEmployeeByIdParamSetter : ParamSetter<GetEmployeeByIdParams> {
  override fun map(ps: PreparedStatement, params: GetEmployeeByIdParams) {
    ps.setObject(1, params.review_cycle_id)
    ps.setObject(2, params.id)
  }
}

data class GetEmployeeByIdResult(
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
  val departmentName: String?,
  val teamId: Long?,
  val teamName: String?,
  val designationId: Long?,
  val designationName: String?,
  val roleId: Long?,
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

class GetEmployeeByIdRowMapper : RowMapper<GetEmployeeByIdResult> {
  override fun map(rs: ResultSet): GetEmployeeByIdResult = GetEmployeeByIdResult(
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
    departmentName = rs.getObject("department_name") as kotlin.String?,
    teamId = rs.getObject("team_id") as kotlin.Long?,
    teamName = rs.getObject("team_name") as kotlin.String?,
    designationId = rs.getObject("designation_id") as kotlin.Long?,
    designationName = rs.getObject("designation_name") as kotlin.String?,
    roleId = rs.getObject("role_id") as kotlin.Long?,
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

class GetEmployeeByIdQuery : Query<GetEmployeeByIdParams, GetEmployeeByIdResult> {
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
      |  COALESCE(employees_department_mapping_view.id, null) AS department_id,
      |  COALESCE(employees_department_mapping_view.department_name, null) AS department_name,
      |  COALESCE(employees_team_mapping_view.team_id, null) AS team_id,
      |  COALESCE(employees_team_mapping_view.team_name, null) AS team_name,
      |  COALESCE(employees_designation_mapping_view.id, null) AS designation_id,
      |  COALESCE(employees_designation_mapping_view.designation_name, null) AS designation_name,
      |  COALESCE(employees_role_mapping_view.id, null) AS role_id,
      |  COALESCE(employees_role_mapping_view.role_name, null) AS role_name,
      |  COALESCE(first_manager_data.id, null) AS first_manager_id,
      |  COALESCE(first_manager_data.emp_id, null) AS first_manager_employee_id,
      |  COALESCE(first_manager_data.first_name, null) AS first_manager_first_name,
      |  COALESCE(first_manager_data.last_name, null) AS first_manager_last_name,
      |  COALESCE(second_manager_data.id, null) AS second_manager_id,
      |  COALESCE(second_manager_data.emp_id, null) AS second_manager_employee_id,
      |  COALESCE(second_manager_data.first_name, null) AS second_manager_first_name,
      |  COALESCE(second_manager_data.last_name, null) AS second_manager_last_name
      |FROM employees
      |  LEFT JOIN employees_department_mapping_view ON employees.id = employees_department_mapping_view.emp_id
      |  LEFT JOIN employees_team_mapping_view ON employees.id = employees_team_mapping_view.emp_id
      |  LEFT JOIN employees_designation_mapping_view ON employees.id = employees_designation_mapping_view.emp_id
      |  LEFT JOIN employees_role_mapping_view ON employees.id = employees_role_mapping_view.emp_id
      |  LEFT JOIN review_cycle AS rc
      |    ON rc.id = ?
      |  LEFT JOIN employee_manager_mapping AS first_manager_mapping
      |    ON employees.id = first_manager_mapping.emp_id
      |    AND first_manager_mapping.type = 1
      |    AND (
      |      (rc.id IS NOT NULL AND
      |        first_manager_mapping.created_at::date <= rc.self_review_end_date
      |        AND (first_manager_mapping.updated_at::date IS NULL OR first_manager_mapping.updated_at::date >= rc.end_date))
      |      OR
      |      (rc.id IS NULL AND first_manager_mapping.is_active = TRUE)
      |    )
      |  LEFT JOIN employees AS first_manager_data ON first_manager_mapping.manager_id = first_manager_data.id
      |  LEFT JOIN employee_manager_mapping AS second_manager_mapping
      |    ON employees.id = second_manager_mapping.emp_id
      |    AND second_manager_mapping.type = 2
      |    AND (
      |      (rc.id IS NOT NULL AND
      |        second_manager_mapping.created_at::date <= rc.self_review_end_date
      |        AND (second_manager_mapping.updated_at::date IS NULL OR second_manager_mapping.updated_at::date >= rc.end_date))
      |      OR
      |      (rc.id IS NULL AND second_manager_mapping.is_active = TRUE)
      |    )
      |  LEFT JOIN employees AS second_manager_data ON second_manager_mapping.manager_id = second_manager_data.id
      |WHERE employees.id = ?;
      |""".trimMargin()

  override val mapper: RowMapper<GetEmployeeByIdResult> = GetEmployeeByIdRowMapper()

  override val paramSetter: ParamSetter<GetEmployeeByIdParams> = GetEmployeeByIdParamSetter()
}
