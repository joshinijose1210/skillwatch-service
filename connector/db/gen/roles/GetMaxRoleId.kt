package roles

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetMaxRoleIdParams(
  val organisationId: Long?
)

class GetMaxRoleIdParamSetter : ParamSetter<GetMaxRoleIdParams> {
  override fun map(ps: PreparedStatement, params: GetMaxRoleIdParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetMaxRoleIdResult(
  val maxId: Long?
)

class GetMaxRoleIdRowMapper : RowMapper<GetMaxRoleIdResult> {
  override fun map(rs: ResultSet): GetMaxRoleIdResult = GetMaxRoleIdResult(
  maxId = rs.getObject("max_id") as kotlin.Long?)
}

class GetMaxRoleIdQuery : Query<GetMaxRoleIdParams, GetMaxRoleIdResult> {
  override val sql: String = """
      |SELECT MAX(role_id) as max_id FROM roles WHERE organisation_id = ?;
      |""".trimMargin()

  override val mapper: RowMapper<GetMaxRoleIdResult> = GetMaxRoleIdRowMapper()

  override val paramSetter: ParamSetter<GetMaxRoleIdParams> = GetMaxRoleIdParamSetter()
}
