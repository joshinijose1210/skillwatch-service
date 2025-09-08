package roles

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetRoleDataByIdParams(
  val id: Long?,
  val organisationId: Long?
)

class GetRoleDataByIdParamSetter : ParamSetter<GetRoleDataByIdParams> {
  override fun map(ps: PreparedStatement, params: GetRoleDataByIdParams) {
    ps.setObject(1, params.id)
    ps.setObject(2, params.organisationId)
  }
}

data class GetRoleDataByIdResult(
  val id: Long,
  val roleId: Long,
  val roleName: String,
  val status: Boolean
)

class GetRoleDataByIdRowMapper : RowMapper<GetRoleDataByIdResult> {
  override fun map(rs: ResultSet): GetRoleDataByIdResult = GetRoleDataByIdResult(
  id = rs.getObject("id") as kotlin.Long,
    roleId = rs.getObject("role_id") as kotlin.Long,
    roleName = rs.getObject("role_name") as kotlin.String,
    status = rs.getObject("status") as kotlin.Boolean)
}

class GetRoleDataByIdQuery : Query<GetRoleDataByIdParams, GetRoleDataByIdResult> {
  override val sql: String = """
      |SELECT
      |    id,
      |    role_id,
      |    role_name,
      |    status
      |FROM roles
      |WHERE
      |id = ?
      |AND organisation_id = ? ;
      """.trimMargin()

  override val mapper: RowMapper<GetRoleDataByIdResult> = GetRoleDataByIdRowMapper()

  override val paramSetter: ParamSetter<GetRoleDataByIdParams> = GetRoleDataByIdParamSetter()
}
