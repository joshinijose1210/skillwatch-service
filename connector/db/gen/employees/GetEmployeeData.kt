package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetEmployeeDataParams(
  val emailId: String?
)

class GetEmployeeDataParamSetter : ParamSetter<GetEmployeeDataParams> {
  override fun map(ps: PreparedStatement, params: GetEmployeeDataParams) {
    ps.setObject(1, params.emailId)
  }
}

data class GetEmployeeDataResult(
  val organisationId: Long,
  val id: Long,
  val firstName: String,
  val lastName: String,
  val emailId: String,
  val contactNo: String,
  val empId: String,
  val onboardingFlow: Boolean,
  val departmentName: String?,
  val designationName: String?,
  val teamName: String?,
  val roleName: String?,
  val roleId: Long,
  val firstManagerId: Long?,
  val secondManagerId: Long?,
  val isOrWasManager: Boolean?
)

class GetEmployeeDataRowMapper : RowMapper<GetEmployeeDataResult> {
  override fun map(rs: ResultSet): GetEmployeeDataResult = GetEmployeeDataResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    emailId = rs.getObject("email_id") as kotlin.String,
    contactNo = rs.getObject("contact_no") as kotlin.String,
    empId = rs.getObject("emp_id") as kotlin.String,
    onboardingFlow = rs.getObject("onboarding_flow") as kotlin.Boolean,
    departmentName = rs.getObject("department_name") as kotlin.String?,
    designationName = rs.getObject("designation_name") as kotlin.String?,
    teamName = rs.getObject("team_name") as kotlin.String?,
    roleName = rs.getObject("role_name") as kotlin.String?,
    roleId = rs.getObject("role_id") as kotlin.Long,
    firstManagerId = rs.getObject("first_manager_id") as kotlin.Long?,
    secondManagerId = rs.getObject("second_manager_id") as kotlin.Long?,
    isOrWasManager = rs.getObject("is_or_was_manager") as kotlin.Boolean?)
}

class GetEmployeeDataQuery : Query<GetEmployeeDataParams, GetEmployeeDataResult> {
  override val sql: String = """
      |SELECT
      |  employees.organisation_id,
      |  employees.id,
      |  employees.first_name,
      |  employees.last_name,
      |  employees.email_id,
      |  employees.contact_no,
      |  employees.emp_id,
      |  employees.onboarding_flow,
      |  employees_department_mapping_view.department_name,
      |  employees_designation_mapping_view.designation_name,
      |  employees_team_mapping_view.team_name,
      |  employees_role_mapping_view.role_name,
      |  employees_role_mapping_view.id AS role_id,
      |  COALESCE(first_manager_data.manager_id, null) AS first_manager_id,
      |  COALESCE(second_manager_data.manager_id, null) AS second_manager_id,
      |  CASE WHEN is_manager_details.manager_id IS NOT NULL THEN true ELSE false END AS is_or_was_manager
      |FROM
      |  employees
      |  JOIN employees_department_mapping_view ON employees.id = employees_department_mapping_view.emp_id
      |  JOIN employees_team_mapping_view ON employees.id = employees_team_mapping_view.emp_id
      |  JOIN employees_designation_mapping_view ON employees.id = employees_designation_mapping_view.emp_id
      |  JOIN employees_role_mapping_view ON employees.id = employees_role_mapping_view.emp_id
      |  LEFT JOIN employee_manager_mapping_view AS first_manager_data ON employees.id = first_manager_data.emp_id
      |    AND first_manager_data.type = 1
      |  LEFT JOIN employee_manager_mapping_view AS second_manager_data ON employees.id = second_manager_data.emp_id
      |    AND second_manager_data.type = 2
      |  LEFT JOIN employee_manager_mapping AS is_manager_details ON employees.id = is_manager_details.manager_id
      |WHERE
      |  LOWER(employees.email_id) = LOWER(?) ;
      |""".trimMargin()

  override val mapper: RowMapper<GetEmployeeDataResult> = GetEmployeeDataRowMapper()

  override val paramSetter: ParamSetter<GetEmployeeDataParams> = GetEmployeeDataParamSetter()
}
