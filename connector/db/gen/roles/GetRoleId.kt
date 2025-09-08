package roles

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetRoleIdParams(
  val roleName: String?,
  val organisationId: Long?
)

class GetRoleIdParamSetter : ParamSetter<GetRoleIdParams> {
  override fun map(ps: PreparedStatement, params: GetRoleIdParams) {
    ps.setObject(1, params.roleName)
    ps.setObject(2, params.organisationId)
  }
}

data class GetRoleIdResult(
  val id: Long
)

class GetRoleIdRowMapper : RowMapper<GetRoleIdResult> {
  override fun map(rs: ResultSet): GetRoleIdResult = GetRoleIdResult(
  id = rs.getObject("id") as kotlin.Long)
}

class GetRoleIdQuery : Query<GetRoleIdParams, GetRoleIdResult> {
  override val sql: String = """
      |SELECT id FROM roles WHERE
      |      LOWER(role_name) = LOWER(?)
      |      AND organisation_id = ?
      |      AND roles.status = true;
      """.trimMargin()

  override val mapper: RowMapper<GetRoleIdResult> = GetRoleIdRowMapper()

  override val paramSetter: ParamSetter<GetRoleIdParams> = GetRoleIdParamSetter()
}
