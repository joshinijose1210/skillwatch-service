package kpi

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetKpisTeamsMappingsParams(
  val kpiId: Long?
)

class GetKpisTeamsMappingsParamSetter : ParamSetter<GetKpisTeamsMappingsParams> {
  override fun map(ps: PreparedStatement, params: GetKpisTeamsMappingsParams) {
    ps.setObject(1, params.kpiId)
  }
}

data class GetKpisTeamsMappingsResult(
  val departmentId: Long,
  val teamId: Long,
  val designationId: Long
)

class GetKpisTeamsMappingsRowMapper : RowMapper<GetKpisTeamsMappingsResult> {
  override fun map(rs: ResultSet): GetKpisTeamsMappingsResult = GetKpisTeamsMappingsResult(
  departmentId = rs.getObject("department_id") as kotlin.Long,
    teamId = rs.getObject("team_id") as kotlin.Long,
    designationId = rs.getObject("designation_id") as kotlin.Long)
}

class GetKpisTeamsMappingsQuery : Query<GetKpisTeamsMappingsParams, GetKpisTeamsMappingsResult> {
  override val sql: String = """
      |SELECT department_id, team_id, designation_id
      |FROM kpi_department_team_designation_mapping
      |WHERE kpi_id =? ;
      |
      |""".trimMargin()

  override val mapper: RowMapper<GetKpisTeamsMappingsResult> = GetKpisTeamsMappingsRowMapper()

  override val paramSetter: ParamSetter<GetKpisTeamsMappingsParams> =
      GetKpisTeamsMappingsParamSetter()
}
