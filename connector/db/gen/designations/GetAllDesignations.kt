package designations

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllDesignationsParams(
  val organisationId: Long?,
  val searchText: String?,
  val teamId: Array<Int>?,
  val departmentId: Array<Int>?,
  val offset: Int?,
  val limit: Int?
)

class GetAllDesignationsParamSetter : ParamSetter<GetAllDesignationsParams> {
  override fun map(ps: PreparedStatement, params: GetAllDesignationsParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.searchText)
    ps.setObject(3, params.searchText)
    ps.setObject(4, params.searchText)
    ps.setObject(5, params.searchText)
    ps.setArray(6, ps.connection.createArrayOf("int4", params.teamId))
    ps.setArray(7, ps.connection.createArrayOf("int4", params.teamId))
    ps.setArray(8, ps.connection.createArrayOf("int4", params.departmentId))
    ps.setArray(9, ps.connection.createArrayOf("int4", params.departmentId))
    ps.setObject(10, params.offset)
    ps.setObject(11, params.limit)
  }
}

data class GetAllDesignationsResult(
  val id: Long,
  val organisationId: Long,
  val designationId: Long,
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

class GetAllDesignationsRowMapper : RowMapper<GetAllDesignationsResult> {
  override fun map(rs: ResultSet): GetAllDesignationsResult = GetAllDesignationsResult(
  id = rs.getObject("id") as kotlin.Long,
    organisationId = rs.getObject("organisation_id") as kotlin.Long,
    designationId = rs.getObject("designation_id") as kotlin.Long,
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

class GetAllDesignationsQuery : Query<GetAllDesignationsParams, GetAllDesignationsResult> {
  override val sql: String = """
      |SELECT
      |  designations.id,
      |  designations.organisation_id,
      |  designations.designation_id,
      |  COALESCE(dtm.department_id, null) AS department_id,
      |  COALESCE(d.department_name, null) AS department_name,
      |  COALESCE(d.department_id, null) AS department_display_id,
      |  COALESCE(d.status, null) AS department_status,
      |  COALESCE(tdm.team_id, null) AS team_id,
      |  COALESCE(t.team_name, null) AS team_name,
      |  COALESCE(t.team_id, null) AS team_display_id,
      |  COALESCE(t.status, null) AS team_status,
      |  designations.designation_name,
      |  designations.status,
      |  designations.created_at,
      |  designations.updated_at
      |FROM designations
      |  LEFT JOIN team_designation_mapping tdm ON tdm.designation_id = designations.id
      |  LEFT JOIN teams t ON t.id = tdm.team_id
      |  LEFT JOIN department_team_mapping dtm ON dtm.team_id = t.id
      |  LEFT JOIN departments d ON d.id = dtm.department_id
      |WHERE designations.organisation_id = ?
      |  AND (cast(? as text) IS NULL
      |  OR UPPER(designations.designation_name) LIKE UPPER(?)
      |  OR UPPER(t.team_name) LIKE UPPER(?)
      |  OR UPPER(d.department_name) LIKE UPPER(?))
      |  AND (?::INT[] = '{-99}' OR tdm.team_id = ANY (?::INT[]))
      |  AND (?::INT[] = '{-99}' OR dtm.department_id = ANY (?::INT[]))
      |ORDER BY designations.created_at DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      |""".trimMargin()

  override val mapper: RowMapper<GetAllDesignationsResult> = GetAllDesignationsRowMapper()

  override val paramSetter: ParamSetter<GetAllDesignationsParams> = GetAllDesignationsParamSetter()
}
