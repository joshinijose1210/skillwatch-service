package employees

import java.sql.PreparedStatement
import java.sql.Timestamp
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateEmployeeTeamMappingParams(
  val leftAt: Timestamp?,
  val id: Long?
)

class UpdateEmployeeTeamMappingParamSetter : ParamSetter<UpdateEmployeeTeamMappingParams> {
  override fun map(ps: PreparedStatement, params: UpdateEmployeeTeamMappingParams) {
    ps.setObject(1, params.leftAt)
    ps.setObject(2, params.id)
  }
}

class UpdateEmployeeTeamMappingCommand : Command<UpdateEmployeeTeamMappingParams> {
  override val sql: String = """
      |UPDATE employees_team_mapping etm
      |SET left_at = ?,
      |    is_active = false
      |WHERE emp_id = ?
      |AND is_active = true;
      """.trimMargin()

  override val paramSetter: ParamSetter<UpdateEmployeeTeamMappingParams> =
      UpdateEmployeeTeamMappingParamSetter()
}
