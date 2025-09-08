package teams

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetMaxTeamIdParams(
  val organisationId: Long?
)

class GetMaxTeamIdParamSetter : ParamSetter<GetMaxTeamIdParams> {
  override fun map(ps: PreparedStatement, params: GetMaxTeamIdParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetMaxTeamIdResult(
  val maxId: Long?
)

class GetMaxTeamIdRowMapper : RowMapper<GetMaxTeamIdResult> {
  override fun map(rs: ResultSet): GetMaxTeamIdResult = GetMaxTeamIdResult(
  maxId = rs.getObject("max_id") as kotlin.Long?)
}

class GetMaxTeamIdQuery : Query<GetMaxTeamIdParams, GetMaxTeamIdResult> {
  override val sql: String = """
      |SELECT MAX(team_id) as max_id FROM teams WHERE organisation_id = ?;
      |""".trimMargin()

  override val mapper: RowMapper<GetMaxTeamIdResult> = GetMaxTeamIdRowMapper()

  override val paramSetter: ParamSetter<GetMaxTeamIdParams> = GetMaxTeamIdParamSetter()
}
