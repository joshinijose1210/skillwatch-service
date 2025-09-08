package employees

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddEmployeesTeamMappingParams(
  val id: Long?,
  val team_id: Long?,
  val joinedAt: Timestamp?
)

class AddEmployeesTeamMappingParamSetter : ParamSetter<AddEmployeesTeamMappingParams> {
  override fun map(ps: PreparedStatement, params: AddEmployeesTeamMappingParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.team_id)
    ps.setObject(3, params.joinedAt)
  }
}

data class AddEmployeesTeamMappingResult(
  val id: Long,
  val empId: Long,
  val teamId: Long,
  val joinedAt: Timestamp,
  val leftAt: Timestamp?,
  val isActive: Boolean
)

class AddEmployeesTeamMappingRowMapper : RowMapper<AddEmployeesTeamMappingResult> {
  override fun map(rs: ResultSet): AddEmployeesTeamMappingResult = AddEmployeesTeamMappingResult(
  id = rs.getObject("id") as kotlin.Long,
    empId = rs.getObject("emp_id") as kotlin.Long,
    teamId = rs.getObject("team_id") as kotlin.Long,
    joinedAt = rs.getObject("joined_at") as java.sql.Timestamp,
    leftAt = rs.getObject("left_at") as java.sql.Timestamp?,
    isActive = rs.getObject("is_active") as kotlin.Boolean)
}

class AddEmployeesTeamMappingQuery : Query<AddEmployeesTeamMappingParams,
    AddEmployeesTeamMappingResult> {
  override val sql: String = """
      |INSERT INTO employees_team_mapping(emp_id,team_id, joined_at, is_active)
      |VALUES (
      |  ?,
      |  ?,
      |  ?,
      |  true
      |  ) RETURNING *;
      |""".trimMargin()

  override val mapper: RowMapper<AddEmployeesTeamMappingResult> = AddEmployeesTeamMappingRowMapper()

  override val paramSetter: ParamSetter<AddEmployeesTeamMappingParams> =
      AddEmployeesTeamMappingParamSetter()
}
