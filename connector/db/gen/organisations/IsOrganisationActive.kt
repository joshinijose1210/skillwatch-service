package organisations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Boolean
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class IsOrganisationActiveParams(
  val emailId: String?
)

class IsOrganisationActiveParamSetter : ParamSetter<IsOrganisationActiveParams> {
  override fun map(ps: PreparedStatement, params: IsOrganisationActiveParams) {
    ps.setObject(1, params.emailId)
  }
}

data class IsOrganisationActiveResult(
  val isActive: Boolean
)

class IsOrganisationActiveRowMapper : RowMapper<IsOrganisationActiveResult> {
  override fun map(rs: ResultSet): IsOrganisationActiveResult = IsOrganisationActiveResult(
  isActive = rs.getObject("is_active") as kotlin.Boolean)
}

class IsOrganisationActiveQuery : Query<IsOrganisationActiveParams, IsOrganisationActiveResult> {
  override val sql: String = """
      |SELECT is_active
      |FROM organisations
      |INNER JOIN employees ON organisations.sr_no = employees.organisation_id
      |WHERE LOWER(employees.email_id) = LOWER(?);
      |""".trimMargin()

  override val mapper: RowMapper<IsOrganisationActiveResult> = IsOrganisationActiveRowMapper()

  override val paramSetter: ParamSetter<IsOrganisationActiveParams> =
      IsOrganisationActiveParamSetter()
}
