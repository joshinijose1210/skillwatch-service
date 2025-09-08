package review

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetEmployeesWithIncompleteCheckInWithManagerParams(
  val reviewCycleId: Long?,
  val managerId: Long?
)

class GetEmployeesWithIncompleteCheckInWithManagerParamSetter :
    ParamSetter<GetEmployeesWithIncompleteCheckInWithManagerParams> {
  override fun map(ps: PreparedStatement,
      params: GetEmployeesWithIncompleteCheckInWithManagerParams) {
    ps.setObject(1, params.reviewCycleId)
    ps.setObject(2, params.managerId)
    ps.setObject(3, params.managerId)
    ps.setObject(4, params.managerId)
    ps.setObject(5, params.managerId)
  }
}

data class GetEmployeesWithIncompleteCheckInWithManagerResult(
  val organisationId: Long,
  val id: Long,
  val firstName: String,
  val lastName: String,
  val empId: String,
  val emailId: String,
  val contactNo: String
)

class GetEmployeesWithIncompleteCheckInWithManagerRowMapper :
    RowMapper<GetEmployeesWithIncompleteCheckInWithManagerResult> {
  override fun map(rs: ResultSet): GetEmployeesWithIncompleteCheckInWithManagerResult =
      GetEmployeesWithIncompleteCheckInWithManagerResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    empId = rs.getObject("emp_id") as kotlin.String,
    emailId = rs.getObject("email_id") as kotlin.String,
    contactNo = rs.getObject("contact_no") as kotlin.String)
}

class GetEmployeesWithIncompleteCheckInWithManagerQuery :
    Query<GetEmployeesWithIncompleteCheckInWithManagerParams,
    GetEmployeesWithIncompleteCheckInWithManagerResult> {
  override val sql: String = """
      |SELECT
      |  employees.organisation_id,
      |  employees.id,
      |  employees.first_name,
      |  employees.last_name,
      |  employees.emp_id,
      |  employees.email_id,
      |  employees.contact_no
      |FROM
      |  employees
      |  JOIN employee_manager_mapping_view AS firstManagerMapping ON employees.id = firstManagerMapping.emp_id
      |  AND firstManagerMapping.type = 1
      |  LEFT JOIN employee_manager_mapping_view AS secondManagerMapping ON employees.id = secondManagerMapping.emp_id
      |  AND secondManagerMapping.type = 2
      |  LEFT JOIN review_details ON employees.id = review_details.review_to
      |  AND review_details.review_type_id = 3
      |  AND review_details.review_cycle_id = ?
      |WHERE
      |  ((firstManagerMapping.manager_id = ? AND firstManagerMapping.emp_id != ?) OR
      |  (secondManagerMapping.manager_id = ? AND secondManagerMapping.emp_id != ?))
      |  AND employees.status = TRUE
      |  AND (review_details.published IS NULL OR review_details.published = FALSE) ;
      """.trimMargin()

  override val mapper: RowMapper<GetEmployeesWithIncompleteCheckInWithManagerResult> =
      GetEmployeesWithIncompleteCheckInWithManagerRowMapper()

  override val paramSetter: ParamSetter<GetEmployeesWithIncompleteCheckInWithManagerParams> =
      GetEmployeesWithIncompleteCheckInWithManagerParamSetter()
}
