package designations

import java.sql.PreparedStatement
import kotlin.Long
import kotlin.String
import norm.Command
import norm.ParamSetter

data class AddTeamDesignationMappingParams(
  val teamId: Long?,
  val designationId: Long?
)

class AddTeamDesignationMappingParamSetter : ParamSetter<AddTeamDesignationMappingParams> {
  override fun map(ps: PreparedStatement, params: AddTeamDesignationMappingParams) {
    ps.setObject(1, params.teamId)
    ps.setObject(2, params.designationId)
  }
}

class AddTeamDesignationMappingCommand : Command<AddTeamDesignationMappingParams> {
  override val sql: String =
      "INSERT INTO team_designation_mapping(team_id, designation_id)VALUES (?, ?);"

  override val paramSetter: ParamSetter<AddTeamDesignationMappingParams> =
      AddTeamDesignationMappingParamSetter()
}
