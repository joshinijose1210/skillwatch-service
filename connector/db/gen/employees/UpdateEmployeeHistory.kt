package employees

import java.sql.PreparedStatement
import java.sql.Timestamp
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateEmployeeHistoryParams(
  val deactivatedAt: Timestamp?,
  val employeeId: Long?
)

class UpdateEmployeeHistoryParamSetter : ParamSetter<UpdateEmployeeHistoryParams> {
  override fun map(ps: PreparedStatement, params: UpdateEmployeeHistoryParams) {
    ps.setObject(1, params.deactivatedAt)
    ps.setObject(2, params.employeeId)
    ps.setObject(3, params.employeeId)
  }
}

class UpdateEmployeeHistoryCommand : Command<UpdateEmployeeHistoryParams> {
  override val sql: String = """
      |UPDATE employees_history
      |SET deactivated_at = ?
      |WHERE employee_id = ?
      |AND id = (
      |  SELECT MAX(id)
      |  FROM employees_history
      |  WHERE employee_id = ?
      |);
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateEmployeeHistoryParams> =
      UpdateEmployeeHistoryParamSetter()
}
