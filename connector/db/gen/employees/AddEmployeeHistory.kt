package employees

import java.sql.PreparedStatement
import java.sql.Timestamp
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddEmployeeHistoryParams(
  val employeeId: Long?,
  val activatedAt: Timestamp?
)

class AddEmployeeHistoryParamSetter : ParamSetter<AddEmployeeHistoryParams> {
  override fun map(ps: PreparedStatement, params: AddEmployeeHistoryParams) {
    ps.setObject(1, params.employeeId)
    ps.setObject(2, params.activatedAt)
  }
}

class AddEmployeeHistoryCommand : Command<AddEmployeeHistoryParams> {
  override val sql: String = """
      |INSERT INTO employees_history(
      |employee_id, activated_at
      |)
      |VALUES(
      |?, ?
      |)
      """.trimMargin()

  override val paramSetter: ParamSetter<AddEmployeeHistoryParams> = AddEmployeeHistoryParamSetter()
}
