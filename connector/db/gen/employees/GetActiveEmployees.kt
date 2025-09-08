package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetActiveEmployeesParams(
  val organisationId: Long?
)

class GetActiveEmployeesParamSetter : ParamSetter<GetActiveEmployeesParams> {
  override fun map(ps: PreparedStatement, params: GetActiveEmployeesParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetActiveEmployeesResult(
  val organisationId: Long,
  val id: Long,
  val empId: String,
  val firstName: String,
  val lastName: String,
  val emailId: String,
  val contactNo: String,
  val status: Boolean
)

class GetActiveEmployeesRowMapper : RowMapper<GetActiveEmployeesResult> {
  override fun map(rs: ResultSet): GetActiveEmployeesResult = GetActiveEmployeesResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    empId = rs.getObject("emp_id") as kotlin.String,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    emailId = rs.getObject("email_id") as kotlin.String,
    contactNo = rs.getObject("contact_no") as kotlin.String,
    status = rs.getObject("status") as kotlin.Boolean)
}

class GetActiveEmployeesQuery : Query<GetActiveEmployeesParams, GetActiveEmployeesResult> {
  override val sql: String = """
      |SELECT
      | organisation_id,
      | id,
      | emp_id,
      | first_name,
      | last_name,
      | email_id,
      | contact_no,
      | status
      |FROM
      | employees
      |WHERE
      | status = true
      | AND organisation_id = ? ;
      |""".trimMargin()

  override val mapper: RowMapper<GetActiveEmployeesResult> = GetActiveEmployeesRowMapper()

  override val paramSetter: ParamSetter<GetActiveEmployeesParams> = GetActiveEmployeesParamSetter()
}
