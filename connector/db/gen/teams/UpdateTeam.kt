package teams

import java.sql.PreparedStatement
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class UpdateTeamParams(
  val teamName: String?,
  val teamStatus: Boolean?,
  val organisationId: Long?,
  val id: Long?
)

class UpdateTeamParamSetter : ParamSetter<UpdateTeamParams> {
  override fun map(ps: PreparedStatement, params: UpdateTeamParams) {
    ps.setObject(1, params.teamName)
    ps.setObject(2, params.teamStatus)
    ps.setObject(3, params.organisationId)
    ps.setObject(4, params.id)
  }
}

class UpdateTeamCommand : Command<UpdateTeamParams> {
  override val sql: String = """
      |UPDATE
      |  teams
      |SET
      |  team_name = ?,
      |  status = ?,
      |  updated_at = CURRENT_TIMESTAMP
      |WHERE
      |  organisation_id = ?
      |  AND id = ? ;
      |
      |""".trimMargin()

  override val paramSetter: ParamSetter<UpdateTeamParams> = UpdateTeamParamSetter()
}
