package userActivity

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddActivityParams(
  val employeeId: Long?,
  val moduleId: Long?,
  val activity: String?,
  val description: String?,
  val ipAddress: String?
)

class AddActivityParamSetter : ParamSetter<AddActivityParams> {
  override fun map(ps: PreparedStatement, params: AddActivityParams) {
    ps.setObject(1, params.employeeId)
    ps.setObject(2, params.moduleId)
    ps.setObject(3, params.activity)
    ps.setObject(4, params.description)
    ps.setObject(5, params.ipAddress)
  }
}

class AddActivityCommand : Command<AddActivityParams> {
  override val sql: String = """
      |INSERT INTO user_activity(
      |  employee_id,
      |  module_id,
      |  activity,
      |  description,
      |  ip_address
      |)
      |VALUES
      |  (
      |    ?,
      |    ?,
      |    ?,
      |    ?,
      |    ?
      |  ) ;
      """.trimMargin()

  override val paramSetter: ParamSetter<AddActivityParams> = AddActivityParamSetter()
}
