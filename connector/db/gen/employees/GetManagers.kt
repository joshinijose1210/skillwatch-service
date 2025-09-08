package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetManagersParams(
  val organisationId: Long?
)

class GetManagersParamSetter : ParamSetter<GetManagersParams> {
  override fun map(ps: PreparedStatement, params: GetManagersParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetManagersResult(
  val organisationId: Long,
  val id: Long,
  val empId: String,
  val firstName: String,
  val lastName: String,
  val emailId: String,
  val contactNo: String,
  val status: Boolean
)

class GetManagersRowMapper : RowMapper<GetManagersResult> {
  override fun map(rs: ResultSet): GetManagersResult = GetManagersResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    empId = rs.getObject("emp_id") as kotlin.String,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    emailId = rs.getObject("email_id") as kotlin.String,
    contactNo = rs.getObject("contact_no") as kotlin.String,
    status = rs.getObject("status") as kotlin.Boolean)
}

class GetManagersQuery : Query<GetManagersParams, GetManagersResult> {
  override val sql: String = """
      |SELECT DISTINCT
      |  employees.organisation_id,
      |  employees.id,
      |  employees.emp_id,
      |  employees.first_name,
      |  employees.last_name,
      |  employees.email_id,
      |  employees.contact_no,
      |  employees.status
      |FROM employees
      |  JOIN employee_manager_mapping_view
      |  ON ((employees.id = employee_manager_mapping_view.manager_id AND employee_manager_mapping_view.type = 1)
      |  OR (employees.id = employee_manager_mapping_view.manager_id AND employee_manager_mapping_view.type = 2))
      |WHERE
      |  employees.status = true
      |  AND employees.organisation_id = ? ;
      |""".trimMargin()

  override val mapper: RowMapper<GetManagersResult> = GetManagersRowMapper()

  override val paramSetter: ParamSetter<GetManagersParams> = GetManagersParamSetter()
}
