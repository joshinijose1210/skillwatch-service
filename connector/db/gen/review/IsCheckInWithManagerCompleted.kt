package review

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsCheckInWithManagerCompletedParams(
  val reviewCycleId: Long?,
  val managerId: Long?
)

class IsCheckInWithManagerCompletedParamSetter : ParamSetter<IsCheckInWithManagerCompletedParams> {
  override fun map(ps: PreparedStatement, params: IsCheckInWithManagerCompletedParams) {
    ps.setObject(1, params.reviewCycleId)
    ps.setObject(2, params.managerId)
    ps.setObject(3, params.managerId)
    ps.setObject(4, params.managerId)
    ps.setObject(5, params.managerId)
  }
}

data class IsCheckInWithManagerCompletedResult(
  val result: Boolean?
)

class IsCheckInWithManagerCompletedRowMapper : RowMapper<IsCheckInWithManagerCompletedResult> {
  override fun map(rs: ResultSet): IsCheckInWithManagerCompletedResult =
      IsCheckInWithManagerCompletedResult(
  result = rs.getObject("result") as kotlin.Boolean?)
}

class IsCheckInWithManagerCompletedQuery : Query<IsCheckInWithManagerCompletedParams,
    IsCheckInWithManagerCompletedResult> {
  override val sql: String = """
      |SELECT CASE WHEN (
      |    SELECT COUNT(*) FROM employees
      |      JOIN employee_manager_mapping_view AS firstManagerMapping ON employees.id = firstManagerMapping.emp_id
      |      AND firstManagerMapping.type = 1
      |      LEFT JOIN employee_manager_mapping_view AS secondManagerMapping ON employees.id = secondManagerMapping.emp_id
      |      AND secondManagerMapping.type = 2
      |      LEFT JOIN review_details ON employees.id  = review_details.review_to
      |      AND review_details.review_type_id = 3
      |      AND review_details.review_cycle_id = ?
      |    WHERE
      |      ((firstManagerMapping.manager_id = ? AND firstManagerMapping.emp_id != ?) OR
      |      (secondManagerMapping.manager_id = ? AND secondManagerMapping.emp_id != ?))
      |      AND employees.status = TRUE
      |      AND ( review_details.published IS NULL OR review_details.published = FALSE )
      |) > 0 THEN false ELSE true END AS result ;
      """.trimMargin()

  override val mapper: RowMapper<IsCheckInWithManagerCompletedResult> =
      IsCheckInWithManagerCompletedRowMapper()

  override val paramSetter: ParamSetter<IsCheckInWithManagerCompletedParams> =
      IsCheckInWithManagerCompletedParamSetter()
}
