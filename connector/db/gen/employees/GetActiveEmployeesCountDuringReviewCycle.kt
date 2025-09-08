package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetActiveEmployeesCountDuringReviewCycleParams(
  val organisationId: Long?,
  val reviewCycleId: Long?
)

class GetActiveEmployeesCountDuringReviewCycleParamSetter :
    ParamSetter<GetActiveEmployeesCountDuringReviewCycleParams> {
  override fun map(ps: PreparedStatement, params: GetActiveEmployeesCountDuringReviewCycleParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.reviewCycleId)
  }
}

data class GetActiveEmployeesCountDuringReviewCycleResult(
  val activeEmployees: Long?
)

class GetActiveEmployeesCountDuringReviewCycleRowMapper :
    RowMapper<GetActiveEmployeesCountDuringReviewCycleResult> {
  override fun map(rs: ResultSet): GetActiveEmployeesCountDuringReviewCycleResult =
      GetActiveEmployeesCountDuringReviewCycleResult(
  activeEmployees = rs.getObject("active_employees") as kotlin.Long?)
}

class GetActiveEmployeesCountDuringReviewCycleQuery :
    Query<GetActiveEmployeesCountDuringReviewCycleParams,
    GetActiveEmployeesCountDuringReviewCycleResult> {
  override val sql: String = """
      |SELECT COUNT(DISTINCT e.id) AS active_employees
      |FROM employees e
      |JOIN employees_history eh ON e.id = eh.employee_id
      |JOIN review_cycle rc ON DATE(eh.activated_at) <= rc.end_date
      |AND (DATE(eh.deactivated_at) IS NULL OR DATE(eh.deactivated_at) >= DATE(rc.start_date))
      |WHERE e.organisation_id = ?
      |  AND rc.id = ? ;
      """.trimMargin()

  override val mapper: RowMapper<GetActiveEmployeesCountDuringReviewCycleResult> =
      GetActiveEmployeesCountDuringReviewCycleRowMapper()

  override val paramSetter: ParamSetter<GetActiveEmployeesCountDuringReviewCycleParams> =
      GetActiveEmployeesCountDuringReviewCycleParamSetter()
}
