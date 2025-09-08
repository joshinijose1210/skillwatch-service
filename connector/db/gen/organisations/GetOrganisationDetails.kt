package organisations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetOrganisationDetailsParams(
  val organisation_id: Int?
)

class GetOrganisationDetailsParamSetter : ParamSetter<GetOrganisationDetailsParams> {
  override fun map(ps: PreparedStatement, params: GetOrganisationDetailsParams) {
    ps.setObject(1, params.organisation_id)
  }
}

data class GetOrganisationDetailsResult(
  val id: Int,
  val name: String,
  val organisationSize: Int,
  val contactNo: String,
  val timeZone: String,
  val activeusers: Long?,
  val inactiveusers: Long?
)

class GetOrganisationDetailsRowMapper : RowMapper<GetOrganisationDetailsResult> {
  override fun map(rs: ResultSet): GetOrganisationDetailsResult = GetOrganisationDetailsResult(
  id = rs.getObject("id") as kotlin.Int,
    name = rs.getObject("name") as kotlin.String,
    organisationSize = rs.getObject("organisation_size") as kotlin.Int,
    contactNo = rs.getObject("contact_no") as kotlin.String,
    timeZone = rs.getObject("time_zone") as kotlin.String,
    activeusers = rs.getObject("activeusers") as kotlin.Long?,
    inactiveusers = rs.getObject("inactiveusers") as kotlin.Long?)
}

class GetOrganisationDetailsQuery : Query<GetOrganisationDetailsParams,
    GetOrganisationDetailsResult> {
  override val sql: String = """
      |SELECT
      |  organisations.sr_no AS id,
      |  organisations.name,
      |  organisations.organisation_size,
      |  org_admin.contact_no,
      |  organisations.time_zone,
      |  COUNT(CASE WHEN employees.status = true THEN 1 END) AS activeUsers,
      |  COUNT(CASE WHEN employees.status = false THEN 1 END) AS inactiveUsers
      |FROM
      |  organisations
      |  LEFT JOIN employees ON employees.organisation_id = organisations.sr_no
      |  LEFT JOIN employees as org_admin ON org_admin.id = organisations.admin_id
      |WHERE
      |  organisations.sr_no = ?
      |GROUP BY
      |  organisations.sr_no,
      |  org_admin.contact_no;
      |""".trimMargin()

  override val mapper: RowMapper<GetOrganisationDetailsResult> = GetOrganisationDetailsRowMapper()

  override val paramSetter: ParamSetter<GetOrganisationDetailsParams> =
      GetOrganisationDetailsParamSetter()
}
