package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Array
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetEmployeeHistoryParams(
  val employeeId: Array<Int>?
)

class GetEmployeeHistoryParamSetter : ParamSetter<GetEmployeeHistoryParams> {
  override fun map(ps: PreparedStatement, params: GetEmployeeHistoryParams) {
    ps.setArray(1, ps.connection.createArrayOf("int4", params.employeeId))
  }
}

data class GetEmployeeHistoryResult(
  val id: Long,
  val employeeId: Long,
  val activatedAt: Timestamp,
  val deactivatedAt: Timestamp?
)

class GetEmployeeHistoryRowMapper : RowMapper<GetEmployeeHistoryResult> {
  override fun map(rs: ResultSet): GetEmployeeHistoryResult = GetEmployeeHistoryResult(
  id = rs.getObject("id") as kotlin.Long,
    employeeId = rs.getObject("employee_id") as kotlin.Long,
    activatedAt = rs.getObject("activated_at") as java.sql.Timestamp,
    deactivatedAt = rs.getObject("deactivated_at") as java.sql.Timestamp?)
}

class GetEmployeeHistoryQuery : Query<GetEmployeeHistoryParams, GetEmployeeHistoryResult> {
  override val sql: String = """
      |SELECT
      |eh.id,
      |eh.employee_id,
      |eh.activated_at,
      |eh.deactivated_at
      |FROM employees_history eh
      |WHERE eh.employee_id = ANY (?::INT[]);
      """.trimMargin()

  override val mapper: RowMapper<GetEmployeeHistoryResult> = GetEmployeeHistoryRowMapper()

  override val paramSetter: ParamSetter<GetEmployeeHistoryParams> = GetEmployeeHistoryParamSetter()
}
