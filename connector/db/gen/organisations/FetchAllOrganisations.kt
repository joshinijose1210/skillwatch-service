package organisations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.Long
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

class FetchAllOrganisationsParams

class FetchAllOrganisationsParamSetter : ParamSetter<FetchAllOrganisationsParams> {
  override fun map(ps: PreparedStatement, params: FetchAllOrganisationsParams) {
  }
}

data class FetchAllOrganisationsResult(
  val id: Int,
  val adminId: Long?,
  val organisationName: String,
  val organisationSize: Int,
  val timeZone: String
)

class FetchAllOrganisationsRowMapper : RowMapper<FetchAllOrganisationsResult> {
  override fun map(rs: ResultSet): FetchAllOrganisationsResult = FetchAllOrganisationsResult(
  id = rs.getObject("id") as kotlin.Int,
    adminId = rs.getObject("admin_id") as kotlin.Long?,
    organisationName = rs.getObject("organisation_name") as kotlin.String,
    organisationSize = rs.getObject("organisation_size") as kotlin.Int,
    timeZone = rs.getObject("time_zone") as kotlin.String)
}

class FetchAllOrganisationsQuery : Query<FetchAllOrganisationsParams, FetchAllOrganisationsResult> {
  override val sql: String = """
      |SELECT
      |  organisations.sr_no as id,
      |  organisations.admin_id as admin_id,
      |  organisations.name as organisation_name,
      |  organisations.organisation_size,
      |  organisations.time_zone
      |FROM
      |  organisations
      |WHERE
      |  organisations.is_active = TRUE;
      |""".trimMargin()

  override val mapper: RowMapper<FetchAllOrganisationsResult> = FetchAllOrganisationsRowMapper()

  override val paramSetter: ParamSetter<FetchAllOrganisationsParams> =
      FetchAllOrganisationsParamSetter()
}
