package designations

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetDesignationDataByIdParams(
  val id: Long?,
  val organisationId: Long?
)

class GetDesignationDataByIdParamSetter : ParamSetter<GetDesignationDataByIdParams> {
  override fun map(ps: PreparedStatement, params: GetDesignationDataByIdParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.organisationId)
  }
}

data class GetDesignationDataByIdResult(
  val id: Long,
  val organisationId: Long,
  val designationDisplayId: Long,
  val departmentId: Long?,
  val departmentName: String?,
  val departmentDisplayId: Long?,
  val departmentStatus: Boolean?,
  val teamId: Long?,
  val teamName: String?,
  val teamDisplayId: Long?,
  val teamStatus: Boolean?,
  val designationName: String,
  val status: Boolean,
  val createdAt: Timestamp,
  val updatedAt: Timestamp?
)

class GetDesignationDataByIdRowMapper : RowMapper<GetDesignationDataByIdResult> {
  override fun map(rs: ResultSet): GetDesignationDataByIdResult = GetDesignationDataByIdResult(
  id = rs.getObject("id") as kotlin.Long,
    organisationId = rs.getObject("organisation_id") as kotlin.Long,
    designationDisplayId = rs.getObject("designation_display_id") as kotlin.Long,
    departmentId = rs.getObject("department_id") as kotlin.Long?,
    departmentName = rs.getObject("department_name") as kotlin.String?,
    departmentDisplayId = rs.getObject("department_display_id") as kotlin.Long?,
    departmentStatus = rs.getObject("department_status") as kotlin.Boolean?,
    teamId = rs.getObject("team_id") as kotlin.Long?,
    teamName = rs.getObject("team_name") as kotlin.String?,
    teamDisplayId = rs.getObject("team_display_id") as kotlin.Long?,
    teamStatus = rs.getObject("team_status") as kotlin.Boolean?,
    designationName = rs.getObject("designation_name") as kotlin.String,
    status = rs.getObject("status") as kotlin.Boolean,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp?)
}

class GetDesignationDataByIdQuery : Query<GetDesignationDataByIdParams,
    GetDesignationDataByIdResult> {
  override val sql: String = """
      |SELECT
      |  d.id,
      |  d.organisation_id,
      |  d.designation_id AS designation_display_id,
      |  COALESCE(dtm.department_id, null) AS department_id,
      |  COALESCE(dp.department_name, null) AS department_name,
      |  COALESCE(dp.department_id, null) AS department_display_id,
      |  COALESCE(dp.status, null) AS department_status,
      |  COALESCE(tdm.team_id, null) AS team_id,
      |  COALESCE(t.team_name, null) AS team_name,
      |  COALESCE(t.team_id, null) AS team_display_id,
      |  COALESCE(t.status, null) AS team_status,
      |  d.designation_name,
      |  d.status,
      |  d.created_at,
      |  d.updated_at
      |FROM designations d
      |  LEFT JOIN team_designation_mapping tdm ON tdm.designation_id = d.id
      |  LEFT JOIN teams t ON t.id = tdm.team_id
      |  LEFT JOIN department_team_mapping dtm ON dtm.team_id = t.id
      |  LEFT JOIN departments dp ON dp.id = dtm.department_id
      |WHERE d.id = ?
      |  AND d.organisation_id = ?;
      """.trimMargin()

  override val mapper: RowMapper<GetDesignationDataByIdResult> = GetDesignationDataByIdRowMapper()

  override val paramSetter: ParamSetter<GetDesignationDataByIdParams> =
      GetDesignationDataByIdParamSetter()
}
