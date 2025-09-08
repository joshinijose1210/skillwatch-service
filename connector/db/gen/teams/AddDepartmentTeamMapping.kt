package teams

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddDepartmentTeamMappingParams(
  val departmentId: Long?,
  val teamId: Long?
)

class AddDepartmentTeamMappingParamSetter : ParamSetter<AddDepartmentTeamMappingParams> {
  override fun map(ps: PreparedStatement, params: AddDepartmentTeamMappingParams) {
    ps.setObject(1, params.departmentId)
    ps.setObject(2, params.teamId)
  }
}

class AddDepartmentTeamMappingCommand : Command<AddDepartmentTeamMappingParams> {
  override val sql: String = """
      |INSERT INTO department_team_mapping(department_id, team_id)
      |VALUES ( ?, ?);
      """.trimMargin()

  override val paramSetter: ParamSetter<AddDepartmentTeamMappingParams> =
      AddDepartmentTeamMappingParamSetter()
}
