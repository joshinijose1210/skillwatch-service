package organisations

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.Int
import kotlin.String
import norm.ParamSetter
import norm.Query
import norm.RowMapper

data class GetOrganisationParams(
  val organisationId: Int?
)

class GetOrganisationParamSetter : ParamSetter<GetOrganisationParams> {
  override fun map(ps: PreparedStatement, params: GetOrganisationParams) {
    ps.setObject(1, params.organisationId)
  }
}

data class GetOrganisationResult(
  val organisationName: String
)

class GetOrganisationRowMapper : RowMapper<GetOrganisationResult> {
  override fun map(rs: ResultSet): GetOrganisationResult = GetOrganisationResult(
  organisationName = rs.getObject("organisation_name") as kotlin.String)
}

class GetOrganisationQuery : Query<GetOrganisationParams, GetOrganisationResult> {
  override val sql: String = """
      |SELECT
      |  name AS organisation_name
      |FROM
      |  organisations
      |WHERE
      |  sr_no = ?;
      |""".trimMargin()

  override val mapper: RowMapper<GetOrganisationResult> = GetOrganisationRowMapper()

  override val paramSetter: ParamSetter<GetOrganisationParams> = GetOrganisationParamSetter()
}
