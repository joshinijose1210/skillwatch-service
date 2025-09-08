package roles

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetRolesCountParams(
  val organisationId: Long?,
  val searchText: String?
)

class GetRolesCountParamSetter : ParamSetter<GetRolesCountParams> {
  override fun map(ps: PreparedStatement, params: GetRolesCountParams) {
    ps.setObject(1, params.organisationId)
    ps.setObject(2, params.searchText)
    ps.setObject(3, params.searchText)
  }
}

data class GetRolesCountResult(
  val roleCount: Long?
)

class GetRolesCountRowMapper : RowMapper<GetRolesCountResult> {
  override fun map(rs: ResultSet): GetRolesCountResult = GetRolesCountResult(
  roleCount = rs.getObject("role_count") as kotlin.Long?)
}

class GetRolesCountQuery : Query<GetRolesCountParams, GetRolesCountResult> {
  override val sql: String = """
      |SELECT COUNT(roles.role_id) AS role_count
      |FROM
      |  roles
      |WHERE
      |  organisation_id = ?
      |  AND (cast(? as text) IS NULL
      |  OR UPPER(roles.role_name) LIKE UPPER(?)) ;
      """.trimMargin()

  override val mapper: RowMapper<GetRolesCountResult> = GetRolesCountRowMapper()

  override val paramSetter: ParamSetter<GetRolesCountParams> = GetRolesCountParamSetter()
}
