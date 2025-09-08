package employees

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateManagerOfAllReporteesParams(
  val currentManagerId: Long?,
  val newManagerId: Long?,
  val employeeId: Long?
)

class UpdateManagerOfAllReporteesParamSetter : ParamSetter<UpdateManagerOfAllReporteesParams> {
  override fun map(ps: PreparedStatement, params: UpdateManagerOfAllReporteesParams) {
    ps.setObject(1, params.currentManagerId)
    ps.setObject(2, params.newManagerId)
    ps.setObject(3, params.currentManagerId)
    ps.setObject(4, params.newManagerId)
    ps.setObject(5, params.employeeId)
  }
}

class UpdateManagerOfAllReporteesCommand : Command<UpdateManagerOfAllReporteesParams> {
  override val sql: String = """
      |UPDATE employee_manager_mapping
      |SET manager_id = CASE
      |    WHEN manager_id = ? AND type = 1 THEN ?
      |    WHEN manager_id = ? AND type = 2 THEN ?
      |    ELSE manager_id
      |  END
      |WHERE emp_id = ?;
      """.trimMargin()

  override val paramSetter: ParamSetter<UpdateManagerOfAllReporteesParams> =
      UpdateManagerOfAllReporteesParamSetter()
}
