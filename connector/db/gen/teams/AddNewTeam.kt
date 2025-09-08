package teams

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class AddNewTeamParams(
  val organisationId: Long?,
  val id: Long?,
  val teamName: String?,
  val teamStatus: Boolean?
)

class AddNewTeamParamSetter : ParamSetter<AddNewTeamParams> {
  override fun map(ps: PreparedStatement, params: AddNewTeamParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.id)
    ps.setObject(3, params.teamName)
    ps.setObject(4, params.teamStatus)
  }
}

data class AddNewTeamResult(
  val id: Long,
  val teamName: String,
  val status: Boolean,
  val createdAt: Timestamp,
  val updatedAt: Timestamp?,
  val teamId: Long,
  val organisationId: Long
)

class AddNewTeamRowMapper : RowMapper<AddNewTeamResult> {
  override fun map(rs: ResultSet): AddNewTeamResult = AddNewTeamResult(
  id = rs.getObject("id") as kotlin.Long,
    teamName = rs.getObject("team_name") as kotlin.String,
    status = rs.getObject("status") as kotlin.Boolean,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp?,
    teamId = rs.getObject("team_id") as kotlin.Long,
    organisationId = rs.getObject("organisation_id") as kotlin.Long)
}

class AddNewTeamQuery : Query<AddNewTeamParams, AddNewTeamResult> {
  override val sql: String = """
      |INSERT INTO teams(organisation_id, team_id, team_name, status)
      |VALUES
      |  (?, ?, ?, ?)RETURNING *;
      |
      |""".trimMargin()

  override val mapper: RowMapper<AddNewTeamResult> = AddNewTeamRowMapper()

  override val paramSetter: ParamSetter<AddNewTeamParams> = AddNewTeamParamSetter()
}
