package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetEmployeesWithReviewCyclePermissionParams(
  val module_id: Int?,
  val organisation_id: Long?
)

class GetEmployeesWithReviewCyclePermissionParamSetter :
    ParamSetter<GetEmployeesWithReviewCyclePermissionParams> {
  override fun map(ps: PreparedStatement, params: GetEmployeesWithReviewCyclePermissionParams) {
    ps.setObject(1, params.module_id)
    ps.setObject(2, params.organisation_id)
  }
}

data class GetEmployeesWithReviewCyclePermissionResult(
  val organisationId: Long,
  val id: Long,
  val firstName: String,
  val lastName: String,
  val emailId: String,
  val contactNo: String,
  val empId: String
)

class GetEmployeesWithReviewCyclePermissionRowMapper :
    RowMapper<GetEmployeesWithReviewCyclePermissionResult> {
  override fun map(rs: ResultSet): GetEmployeesWithReviewCyclePermissionResult =
      GetEmployeesWithReviewCyclePermissionResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    emailId = rs.getObject("email_id") as kotlin.String,
    contactNo = rs.getObject("contact_no") as kotlin.String,
    empId = rs.getObject("emp_id") as kotlin.String)
}

class GetEmployeesWithReviewCyclePermissionQuery :
    Query<GetEmployeesWithReviewCyclePermissionParams, GetEmployeesWithReviewCyclePermissionResult>
    {
  override val sql: String = """
      |SELECT
      |  employees.organisation_id,
      |  employees.id,
      |  employees.first_name,
      |  employees.last_name,
      |  employees.email_id,
      |  employees.contact_no,
      |  employees.emp_id
      |FROM employees
      |JOIN employees_role_mapping_view ON employees_role_mapping_view.emp_id = employees.id
      |JOIN module_permissions ON
      |     module_permissions.role_id = employees_role_mapping_view.id
      |     AND module_permissions.module_id = ?
      |     AND edit = true
      |WHERE employees.organisation_id = ?
      |     AND employees.status = true;
      |
      |
      |""".trimMargin()

  override val mapper: RowMapper<GetEmployeesWithReviewCyclePermissionResult> =
      GetEmployeesWithReviewCyclePermissionRowMapper()

  override val paramSetter: ParamSetter<GetEmployeesWithReviewCyclePermissionParams> =
      GetEmployeesWithReviewCyclePermissionParamSetter()
}
