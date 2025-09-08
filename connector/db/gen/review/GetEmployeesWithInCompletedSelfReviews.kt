package review

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetEmployeesWithInCompletedSelfReviewsParams(
  val review_cycle_id: Long?,
  val organisation_id: Long?
)

class GetEmployeesWithInCompletedSelfReviewsParamSetter :
    ParamSetter<GetEmployeesWithInCompletedSelfReviewsParams> {
  override fun map(ps: PreparedStatement, params: GetEmployeesWithInCompletedSelfReviewsParams) {
    ps.setObject(1, params.review_cycle_id)
    ps.setObject(2, params.organisation_id)
  }
}

data class GetEmployeesWithInCompletedSelfReviewsResult(
  val organisationId: Long,
  val id: Long,
  val firstName: String,
  val lastName: String,
  val empId: String,
  val emailId: String,
  val contactNo: String
)

class GetEmployeesWithInCompletedSelfReviewsRowMapper :
    RowMapper<GetEmployeesWithInCompletedSelfReviewsResult> {
  override fun map(rs: ResultSet): GetEmployeesWithInCompletedSelfReviewsResult =
      GetEmployeesWithInCompletedSelfReviewsResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    empId = rs.getObject("emp_id") as kotlin.String,
    emailId = rs.getObject("email_id") as kotlin.String,
    contactNo = rs.getObject("contact_no") as kotlin.String)
}

class GetEmployeesWithInCompletedSelfReviewsQuery :
    Query<GetEmployeesWithInCompletedSelfReviewsParams,
    GetEmployeesWithInCompletedSelfReviewsResult> {
  override val sql: String = """
      |SELECT
      |    employees.organisation_id,
      |    employees.id,
      |    employees.first_name,
      |    employees.last_name,
      |    employees.emp_id,
      |    employees.email_id,
      |    employees.contact_no
      |FROM employees
      |LEFT JOIN review_details ON
      |    employees.id  = review_details.review_to
      |    AND employees.id = review_details.review_from
      |    AND review_details.review_type_id = 1
      |    AND review_details.review_cycle_id = ?
      |WHERE
      |    employees.organisation_id = ?
      |    AND employees.status = TRUE
      |    AND ( review_details.published IS NULL OR review_details.published = FALSE ) ;
      """.trimMargin()

  override val mapper: RowMapper<GetEmployeesWithInCompletedSelfReviewsResult> =
      GetEmployeesWithInCompletedSelfReviewsRowMapper()

  override val paramSetter: ParamSetter<GetEmployeesWithInCompletedSelfReviewsParams> =
      GetEmployeesWithInCompletedSelfReviewsParamSetter()
}
