package kpi

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetActiveTeamsDesignationsOfKpiParams(
  val kpiId: Long?
)

class GetActiveTeamsDesignationsOfKpiParamSetter :
    ParamSetter<GetActiveTeamsDesignationsOfKpiParams> {
  override fun map(ps: PreparedStatement, params: GetActiveTeamsDesignationsOfKpiParams) {
    ps.setObject(1, params.kpiId)
  }
}

data class GetActiveTeamsDesignationsOfKpiResult(
  val departmentId: Long?,
  val teamId: Long?,
  val designationId: Long?,
  val departmentName: String?,
  val teamName: String?,
  val designationName: String?
)

class GetActiveTeamsDesignationsOfKpiRowMapper : RowMapper<GetActiveTeamsDesignationsOfKpiResult> {
  override fun map(rs: ResultSet): GetActiveTeamsDesignationsOfKpiResult =
      GetActiveTeamsDesignationsOfKpiResult(
  departmentId = rs.getObject("department_id") as kotlin.Long?,
    teamId = rs.getObject("team_id") as kotlin.Long?,
    designationId = rs.getObject("designation_id") as kotlin.Long?,
    departmentName = rs.getObject("department_name") as kotlin.String?,
    teamName = rs.getObject("team_name") as kotlin.String?,
    designationName = rs.getObject("designation_name") as kotlin.String?)
}

class GetActiveTeamsDesignationsOfKpiQuery : Query<GetActiveTeamsDesignationsOfKpiParams,
    GetActiveTeamsDesignationsOfKpiResult> {
  override val sql: String = """
      |SELECT
      |  COALESCE(kdtdm.department_id, null) AS department_id,
      |  COALESCE(kdtdm.team_id, null)AS team_id,
      |  COALESCE(kdtdm.designation_id, null) AS designation_id,
      |  COALESCE(dep.department_name, null) AS department_name,
      |  COALESCE(t.team_name, null) AS team_name,
      |  COALESCE(d.designation_name, null) AS designation_name
      |FROM
      |  kpi_department_team_designation_mapping kdtdm
      |  LEFT JOIN departments dep ON dep.id = kdtdm.department_id AND dep.status = true
      |  LEFT JOIN teams t ON t.id = kdtdm.team_id AND t.status = true
      |  LEFT JOIN designations d ON d.id = kdtdm.designation_id AND d.status = true
      |WHERE
      |  kpi_id =?;
      |
      |""".trimMargin()

  override val mapper: RowMapper<GetActiveTeamsDesignationsOfKpiResult> =
      GetActiveTeamsDesignationsOfKpiRowMapper()

  override val paramSetter: ParamSetter<GetActiveTeamsDesignationsOfKpiParams> =
      GetActiveTeamsDesignationsOfKpiParamSetter()
}
