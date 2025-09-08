package review

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsManagerReviewCompletedParams(
  val review_cycle_id: Long?,
  val manager_employee_id: Long?
)

class IsManagerReviewCompletedParamSetter : ParamSetter<IsManagerReviewCompletedParams> {
  override fun map(ps: PreparedStatement, params: IsManagerReviewCompletedParams) {
    ps.setObject(1, params.review_cycle_id)
    ps.setObject(2, params.manager_employee_id)
    ps.setObject(3, params.manager_employee_id)
    ps.setObject(4, params.manager_employee_id)
    ps.setObject(5, params.manager_employee_id)
    ps.setObject(6, params.manager_employee_id)
  }
}

data class IsManagerReviewCompletedResult(
  val result: Boolean?
)

class IsManagerReviewCompletedRowMapper : RowMapper<IsManagerReviewCompletedResult> {
  override fun map(rs: ResultSet): IsManagerReviewCompletedResult = IsManagerReviewCompletedResult(
  result = rs.getObject("result") as kotlin.Boolean?)
}

class IsManagerReviewCompletedQuery : Query<IsManagerReviewCompletedParams,
    IsManagerReviewCompletedResult> {
  override val sql: String = """
      |SELECT CASE WHEN (
      |    SELECT COUNT(*) FROM employees
      |      JOIN employee_manager_mapping_view AS firstManagerMapping ON employees.id = firstManagerMapping.emp_id
      |      AND firstManagerMapping.type = 1
      |      LEFT JOIN employee_manager_mapping_view AS secondManagerMapping ON employees.id = secondManagerMapping.emp_id
      |      AND secondManagerMapping.type = 2
      |      LEFT JOIN review_details ON employees.id  = review_details.review_to
      |      AND review_details.review_type_id = 2
      |      AND review_details.review_cycle_id = ?
      |      AND review_details.review_from = ?
      |    WHERE
      |      ((firstManagerMapping.manager_id = ? AND firstManagerMapping.emp_id != ?) OR
      |      (secondManagerMapping.manager_id = ? AND secondManagerMapping.emp_id != ?))
      |      AND employees.status = TRUE
      |      AND ( review_details.published IS NULL OR review_details.published = FALSE )
      |) > 0 THEN false ELSE true END AS result ;
      |""".trimMargin()

  override val mapper: RowMapper<IsManagerReviewCompletedResult> =
      IsManagerReviewCompletedRowMapper()

  override val paramSetter: ParamSetter<IsManagerReviewCompletedParams> =
      IsManagerReviewCompletedParamSetter()
}
