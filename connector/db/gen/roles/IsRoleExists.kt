package roles

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsRoleExistsParams(
  val roleName: String?,
  val organisationId: Long?
)

class IsRoleExistsParamSetter : ParamSetter<IsRoleExistsParams> {
  override fun map(ps: PreparedStatement, params: IsRoleExistsParams) {
    ps.setObject(1, params.roleName)
    ps.setObject(2, params.organisationId)
  }
}

data class IsRoleExistsResult(
  val exists: Boolean?,
  val status: Boolean?
)

class IsRoleExistsRowMapper : RowMapper<IsRoleExistsResult> {
  override fun map(rs: ResultSet): IsRoleExistsResult = IsRoleExistsResult(
  exists = rs.getObject("exists") as kotlin.Boolean?,
    status = rs.getObject("status") as kotlin.Boolean?)
}

class IsRoleExistsQuery : Query<IsRoleExistsParams, IsRoleExistsResult> {
  override val sql: String = """
      |WITH subquery AS (
      |    SELECT status
      |    FROM roles
      |    WHERE
      |    LOWER(role_name) = LOWER(?)
      |    AND organisation_id = ?
      |)
      |SELECT EXISTS (SELECT 1 FROM subquery), (SELECT status FROM subquery); 
      """.trimMargin()

  override val mapper: RowMapper<IsRoleExistsResult> = IsRoleExistsRowMapper()

  override val paramSetter: ParamSetter<IsRoleExistsParams> = IsRoleExistsParamSetter()
}
