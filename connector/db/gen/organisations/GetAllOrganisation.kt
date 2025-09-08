package organisations

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetAllOrganisationParams(
  val offset: Int?,
  val limit: Int?
)

class GetAllOrganisationParamSetter : ParamSetter<GetAllOrganisationParams> {
  override fun map(ps: PreparedStatement, params: GetAllOrganisationParams) {
    ps.setObject(1, params.offset)
    ps.setObject(2, params.limit)
  }
}

data class GetAllOrganisationResult(
  val firstName: String,
  val lastName: String,
  val emailId: String,
  val createdAt: Date?,
  val organisationId: Int?,
  val organisationSize: Int?,
  val organisationName: String?,
  val organisationTimezone: String?,
  val contactNo: String?
)

class GetAllOrganisationRowMapper : RowMapper<GetAllOrganisationResult> {
  override fun map(rs: ResultSet): GetAllOrganisationResult = GetAllOrganisationResult(
  firstName = rs.getObject("first_name") as kotlin.String,
    lastName = rs.getObject("last_name") as kotlin.String,
    emailId = rs.getObject("email_id") as kotlin.String,
    createdAt = rs.getObject("created_at") as java.sql.Date?,
    organisationId = rs.getObject("organisation_id") as kotlin.Int?,
    organisationSize = rs.getObject("organisation_size") as kotlin.Int?,
    organisationName = rs.getObject("organisation_name") as kotlin.String?,
    organisationTimezone = rs.getObject("organisation_timezone") as kotlin.String?,
    contactNo = rs.getObject("contact_no") as kotlin.String?)
}

class GetAllOrganisationQuery : Query<GetAllOrganisationParams, GetAllOrganisationResult> {
  override val sql: String = """
      |SELECT
      |    users.first_name,
      |    users.last_name,
      |    users.email_id,
      |    users.created_at::DATE,
      |    COALESCE(organisations.sr_no, null) as organisation_id,
      |    COALESCE(organisations.organisation_size, null) as organisation_size,
      |    COALESCE(organisations.name, null) as organisation_name,
      |    COALESCE(organisations.time_zone, null) as organisation_timezone,
      |    COALESCE(employees.contact_no, null) as contact_no
      |FROM users
      |LEFT JOIN employees ON users.email_id = employees.email_id
      |LEFT JOIN organisations on employees.id = organisations.admin_id
      |WHERE users.is_org_admin = TRUE
      |ORDER BY users.id DESC
      |OFFSET (?::INT)
      |LIMIT (?::INT);
      |""".trimMargin()

  override val mapper: RowMapper<GetAllOrganisationResult> = GetAllOrganisationRowMapper()

  override val paramSetter: ParamSetter<GetAllOrganisationParams> = GetAllOrganisationParamSetter()
}
