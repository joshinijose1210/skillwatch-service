package roles

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllRolesParams(
  val organisationId: Long?,
  val searchText: String?,
  val offset: Int?,
  val limit: Int?
)

class GetAllRolesParamSetter : ParamSetter<GetAllRolesParams> {
  override fun map(ps: PreparedStatement, params: GetAllRolesParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.searchText)
    ps.setObject(3, params.searchText)
    ps.setObject(4, params.offset)
    ps.setObject(5, params.limit)
  }
}

data class GetAllRolesResult(
  val organisationId: Long,
  val id: Long,
  val roleId: Long,
  val roleName: String,
  val status: Boolean,
  val createdAt: Timestamp,
  val updatedAt: Timestamp?
)

class GetAllRolesRowMapper : RowMapper<GetAllRolesResult> {
  override fun map(rs: ResultSet): GetAllRolesResult = GetAllRolesResult(
  organisationId = rs.getObject("organisation_id") as kotlin.Long,
    id = rs.getObject("id") as kotlin.Long,
    roleId = rs.getObject("role_id") as kotlin.Long,
    roleName = rs.getObject("role_name") as kotlin.String,
    status = rs.getObject("status") as kotlin.Boolean,
    createdAt = rs.getObject("created_at") as java.sql.Timestamp,
    updatedAt = rs.getObject("updated_at") as java.sql.Timestamp?)
}

class GetAllRolesQuery : Query<GetAllRolesParams, GetAllRolesResult> {
  override val sql: String = """
      |SELECT
      |  organisation_id,
      |  id,
      |  role_id,
      |  role_name,
      |  status,
      |  created_at,
      |  updated_at
      |FROM
      |  roles
      |WHERE
      |  organisation_id = ?
      |  AND (cast(? as text) IS NULL
      |  OR UPPER(roles.role_name) LIKE UPPER(?))
      |ORDER BY
      |  created_at DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      |""".trimMargin()

  override val mapper: RowMapper<GetAllRolesResult> = GetAllRolesRowMapper()

  override val paramSetter: ParamSetter<GetAllRolesParams> = GetAllRolesParamSetter()
}
