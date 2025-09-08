package organisations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

class GetAllOrganisationCountParams

class GetAllOrganisationCountParamSetter : ParamSetter<GetAllOrganisationCountParams> {
  override fun map(ps: PreparedStatement, params: GetAllOrganisationCountParams) {
  }
}

data class GetAllOrganisationCountResult(
  val count: Long?
)

class GetAllOrganisationCountRowMapper : RowMapper<GetAllOrganisationCountResult> {
  override fun map(rs: ResultSet): GetAllOrganisationCountResult = GetAllOrganisationCountResult(
  count = rs.getObject("count") as kotlin.Long?)
}

class GetAllOrganisationCountQuery : Query<GetAllOrganisationCountParams,
    GetAllOrganisationCountResult> {
  override val sql: String = """
      |SELECT COUNT(users.email_id) AS count
      |FROM users
      |LEFT JOIN employees ON users.email_id = employees.email_id
      |LEFT JOIN organisations on employees.id = organisations.admin_id
      |WHERE users.is_org_admin = TRUE;
      |""".trimMargin()

  override val mapper: RowMapper<GetAllOrganisationCountResult> = GetAllOrganisationCountRowMapper()

  override val paramSetter: ParamSetter<GetAllOrganisationCountParams> =
      GetAllOrganisationCountParamSetter()
}
